package slacknotifications.teamcity;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.util.StringUtil;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slacknotifications.SlackNotification;
import slacknotifications.teamcity.payload.SlackNotificationPayloadManager;
import slacknotifications.teamcity.settings.SlackNotificationConfig;
import slacknotifications.teamcity.settings.SlackNotificationContentConfig;
import slacknotifications.teamcity.settings.SlackNotificationMainSettings;
import slacknotifications.teamcity.settings.SlackNotificationProjectSettings;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static slacknotifications.teamcity.BuildStateEnum.BUILD_STARTED;


/**
 * Listen teamcity events and send message to rocketchat.
 */
public class RocketNotificationListener extends BuildServerAdapter {
    private static final String NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME = "slackNotifications";
    private static final String BUILD_STATE_MSG_END = " at buildState responsibilityChanged";
    private static final String BUILD_STATE_MSG_TEMPLATE = "About to process RocketNotifications for %s at buildState %s";
    private static final String BUILD_STATE_MSG_START = "About to process RocketNotifications for ";

	private final SBuildServer teamCityServer;
    private final ProjectSettingsManager projectSettingsManager;
    private final SlackNotificationMainSettings myMainSettings;
    private final SlackNotificationPayloadManager myManager;
    private final SlackNotificationFactory slackNotificationFactory;
	private final NotificationUtility notificationUtility = new NotificationUtility();

    public RocketNotificationListener(SBuildServer sBuildServer,
									  ProjectSettingsManager projectSettingsManager,
                                      SlackNotificationMainSettings configSettings,
									  SlackNotificationPayloadManager manager,
                                      SlackNotificationFactory factory) {

        teamCityServer = sBuildServer;
        this.projectSettingsManager = projectSettingsManager;
        myMainSettings = configSettings;
        myManager = manager;
        slackNotificationFactory = factory;
        Loggers.SERVER.info("RocketNotificationListener :: Starting");
    }

    public void register(){
        teamCityServer.addListener(this);
        Loggers.SERVER.info("RocketNotificationListener :: Registering");
    }

	public void fillStackNotification(SlackNotification slackNotification, SlackNotificationConfig slackNotificationConfig){
        slackNotification.setChannel(StringUtil.isEmpty(slackNotificationConfig.getChannel()) ? myMainSettings.getDefaultChannel() : slackNotificationConfig.getChannel());
        slackNotification.setTeamName(myMainSettings.getTeamName());
        slackNotification.setToken(StringUtil.isEmpty(slackNotificationConfig.getToken()) ? myMainSettings.getToken() : slackNotificationConfig.getToken());
        slackNotification.setIconUrl(myMainSettings.getIconUrl());
        slackNotification.setBotName(myMainSettings.getBotName());
		slackNotification.setEnabled(myMainSettings.getEnabled() && slackNotificationConfig.getEnabled());
		slackNotification.setBuildStates(slackNotificationConfig.getBuildStates());
		slackNotification.setProxy(myMainSettings.getProxyConfig());
        slackNotification.setShowBuildAgent(myMainSettings.getShowBuildAgent());
        slackNotification.setShowElapsedBuildTime(myMainSettings.getShowElapsedBuildTime());
        slackNotification.setShowCommits(myMainSettings.getShowCommits());
        slackNotification.setShowCommitters(myMainSettings.getShowCommitters());
        slackNotification.setShowTriggeredBy(myMainSettings.getShowTriggeredBy());
        slackNotification.setShowFailureReason(myMainSettings.getShowFailureReason() == null ? SlackNotificationContentConfig.DEFAULT_SHOW_FAILURE_REASON : myMainSettings.getShowFailureReason());
        slackNotification.setMaxCommitsToDisplay(myMainSettings.getMaxCommitsToDisplay());
        slackNotification.setMentionChannelEnabled(slackNotificationConfig.getMentionChannelEnabled());
		slackNotification.setMentionSlackUserEnabled(slackNotificationConfig.getMentionSlackUserEnabled());
		slackNotification.setMentionWhoTriggeredEnabled(slackNotificationConfig.isMentionWhoTriggeredEnabled());
		slackNotification.setMentionHereEnabled(slackNotificationConfig.getMentionHereEnabled());
        slackNotification.setShowElapsedBuildTime(myMainSettings.getShowElapsedBuildTime());
        if(slackNotificationConfig.getContent() != null && slackNotificationConfig.getContent().isEnabled()) {
            slackNotification.setBotName(slackNotificationConfig.getContent().getBotName());
            slackNotification.setIconUrl(slackNotificationConfig.getContent().getIconUrl());
            slackNotification.setMaxCommitsToDisplay(slackNotificationConfig.getContent().getMaxCommitsToDisplay());
            slackNotification.setShowBuildAgent(slackNotificationConfig.getContent().getShowBuildAgent());
            slackNotification.setShowElapsedBuildTime(slackNotificationConfig.getContent().getShowElapsedBuildTime());
            slackNotification.setShowCommits(slackNotificationConfig.getContent().getShowCommits());
            slackNotification.setShowCommitters(slackNotificationConfig.getContent().getShowCommitters());
            slackNotification.setShowTriggeredBy(slackNotificationConfig.getContent().getShowTriggeredBy());
            slackNotification.setShowFailureReason(slackNotificationConfig.getContent().getShowFailureReason() == null ? SlackNotificationContentConfig.DEFAULT_SHOW_FAILURE_REASON : slackNotificationConfig.getContent().getShowFailureReason());
        }
		Loggers.ACTIVITIES.debug("SlackNotificationListener :: SlackNotification proxy set to "
				+ slackNotification.getProxyHost() + " for " + slackNotificationConfig.getChannel());
	}

	private void processBuildEvent(SRunningBuild sRunningBuild, BuildStateEnum state) {

		Loggers.SERVER.debug(String.format(BUILD_STATE_MSG_TEMPLATE, sRunningBuild.getProjectId(), state.getShortName()));

		val enableNotifications = getListOfEnabledNotifications(sRunningBuild.getProjectId());

		enableNotifications.stream().forEach(notification -> doPost(sRunningBuild, notification, state));
	}

	private void doPost(SRunningBuild build, NotificationConfigWrapper notificationConfigWrapper, BuildStateEnum state) {
		notificationConfigWrapper.slackNotification.setPayload(myManager.buildContent(build, getPreviousNonPersonalBuild(build), state));
		notificationConfigWrapper.slackNotification.setEnabled(notificationConfigWrapper.whc.isEnabledForBuildType(build.getBuildType()) && notificationConfigWrapper.slackNotification.getBuildStates().enabled(state));

		doPost(notificationConfigWrapper.slackNotification);
	}

	private List<NotificationConfigWrapper> getListOfEnabledNotifications(String projectId) {
		val project = teamCityServer.getProjectManager().findProjectById(projectId);

		val enabledProjectConfigs = getEnabledProjectNotificationConfigs(project.getProjectPath());

		return enabledProjectConfigs.stream()
				.filter(subConfig -> isEnabledSubConfig(subConfig, project))
				.map(this::createNotificationWrapper)
				.collect(Collectors.toList());
	}

	private SlackNotificationProjectSettings getProjectSetting(SProject project) {
		val setting = projectSettingsManager.getSettings(project.getProjectId(), NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME);
		return (SlackNotificationProjectSettings) setting;
	}

	private List<SlackNotificationConfig> getEnabledProjectNotificationConfigs(List<SProject> projectHierarchy) {
		return projectHierarchy.stream()
				.map(this::getProjectSetting)
				.filter(SlackNotificationProjectSettings::isEnabled)
				.map(SlackNotificationProjectSettings::getSlackNotificationsConfigs)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private boolean isEnabledSubConfig(SlackNotificationConfig notificationConfig, SProject project) {
		return notificationConfig.isEnabledForSubProjects() == true
				&& project.getProjectId().equals(project.getProjectId());
	}

	private NotificationConfigWrapper createNotificationWrapper(SlackNotificationConfig notificationConfig){
		val slackNotification = slackNotificationFactory.getSlackNotification();
		fillStackNotification(slackNotification, notificationConfig);
		return new NotificationConfigWrapper(slackNotification, notificationConfig);
	}

	@Override
    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BUILD_STARTED);
    }

    @Override
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_FINISHED);
    }

    @Override
    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_INTERRUPTED);
    }

    @Override
    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BEFORE_BUILD_FINISHED);
	}

	@Override
	public void responsibleChanged(SProject project,
								   Collection<TestName> testNames,
								   ResponsibilityEntry entry,
								   boolean isUserAction) {

		Loggers.SERVER.debug(BUILD_STATE_MSG_START + project.getProjectId() + BUILD_STATE_MSG_END);

		for (NotificationConfigWrapper whcw : getListOfEnabledNotifications(project.getProjectId())){
                        whcw.slackNotification.setPayload(myManager.responsibleChanged(project,
                                testNames,
                                entry,
                                isUserAction));
						whcw.slackNotification.setEnabled(whcw.slackNotification.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.slackNotification);
						//Loggers.ACTIVITIES.debug("SlackNotificationListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());

     	}
	}

	@Override
	public void responsibleChanged(SProject project, TestNameResponsibilityEntry oldTestNameResponsibilityEntry, TestNameResponsibilityEntry newTestNameResponsibilityEntry, boolean isUserAction) {
		Loggers.SERVER.debug(BUILD_STATE_MSG_START + project.getProjectId() + BUILD_STATE_MSG_END);
		for (NotificationConfigWrapper whcw : getListOfEnabledNotifications(project.getProjectId())){
						//SlackNotificationPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
						whcw.slackNotification.setPayload(myManager.responsibleChanged(project,
                                oldTestNameResponsibilityEntry,
                                newTestNameResponsibilityEntry,
                                isUserAction));
						whcw.slackNotification.setEnabled(whcw.slackNotification.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.slackNotification);
						//Loggers.ACTIVITIES.debug("SlackNotificationListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());

     	}
	}

	/**
	 * New version of responsibleChanged, which has some bugfixes, but
	 * is only available in versions 7.0 and above.
	 * @param sBuildType
	 * @param responsibilityEntryOld
	 * @param responsibilityEntryNew
	 * @since 7.0
	 */
	@Override
	public void responsibleChanged(@NotNull SBuildType sBuildType,
            @NotNull ResponsibilityEntry responsibilityEntryOld,
            @NotNull ResponsibilityEntry responsibilityEntryNew){

		Loggers.SERVER.debug(BUILD_STATE_MSG_START + sBuildType.getProjectId() + BUILD_STATE_MSG_END);
		for (NotificationConfigWrapper whcw : getListOfEnabledNotifications(sBuildType.getProjectId())){
						//SlackNotificationPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
                        whcw.slackNotification.setPayload(myManager.responsibleChanged(sBuildType,
                                responsibilityEntryOld,
                                responsibilityEntryNew));
						whcw.slackNotification.setEnabled(whcw.whc.isEnabledForBuildType(sBuildType) && whcw.slackNotification.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.slackNotification);
						//Loggers.ACTIVITIES.debug("SlackNotificationListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());
     	}
	}

	@Override
	public void responsibleRemoved(SProject project, TestNameResponsibilityEntry entry){

	}


	/** doPost used by responsibleChanged
	 *
	 * @param notification
	 */
	public void doPost(SlackNotification notification) {
		notificationUtility.doPost(notification);
	}

	@Nullable
	private SFinishedBuild getPreviousNonPersonalBuild(SRunningBuild paramSRunningBuild)
	  {
	    List<SFinishedBuild> localList = this.teamCityServer.getHistory().getEntriesBefore(paramSRunningBuild, false);

	    for (SFinishedBuild localSFinishedBuild : localList)
	      if (!(localSFinishedBuild.isPersonal())) return localSFinishedBuild;
	    return null;
	}

	private boolean hasBuildChangedHistoricalState(SRunningBuild sRunningBuild){
		SFinishedBuild previous = getPreviousNonPersonalBuild(sRunningBuild);
		if (previous != null){
			if (sRunningBuild.getBuildStatus().isSuccessful()){
				return previous.getBuildStatus().isFailed();
			} else if (sRunningBuild.getBuildStatus().isFailed()) {
				return previous.getBuildStatus().isSuccessful();
			}
		}
		return true;
	}

	/**
	 * An inner class to wrap up the SlackNotification and its SlackNotificationConfig into one unit.
	 *
	 */

	@Data
	private class NotificationConfigWrapper {

		private SlackNotification slackNotification;

		private SlackNotificationConfig whc;

		NotificationConfigWrapper(SlackNotification slackNotification, SlackNotificationConfig slackNotificationConfig) {
			this.slackNotification = slackNotification;
			this.whc = slackNotificationConfig;
		}
		 public void setSlackNotification(SlackNotification slackNotification){
			 this.slackNotification=slackNotification;
		 }
		 public SlackNotification getSlackNotification(){
			 return slackNotification;
		 }
	}

}

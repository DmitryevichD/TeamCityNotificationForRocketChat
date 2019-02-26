package rocketnotifications.teamcity.settings;

import jetbrains.buildServer.serverSide.MainConfigProcessor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jdom.Element;
import rocketnotifications.SlackNotificationProxyConfig;
import rocketnotifications.teamcity.Loggers;

import java.io.IOException;
import java.util.Properties;

import static rocketnotifications.teamcity.RocketNotificationListener.ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME;

public class RocketNotificationMainSettings implements MainConfigProcessor {
	private static final String NAME = RocketNotificationMainSettings.class.getName();
	private RocketNotificationMainConfig slackNotificationMainConfig;
	private SBuildServer server;
    private ServerPaths serverPaths;
    private String version;

    public RocketNotificationMainSettings(SBuildServer server, ServerPaths serverPaths){
        this.serverPaths = serverPaths;
        Loggers.SERVER.debug(NAME + " :: Constructor called");
		this.server = server;
		slackNotificationMainConfig = new RocketNotificationMainConfig(serverPaths);

	}

    public void register(){
        Loggers.SERVER.debug(NAME + ":: Registering");
        server.registerExtension(MainConfigProcessor.class, ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME, this);
    }

//	public String getProxySettingsAsString(){
//		return this.slackNotificationMainConfig.getProxySettingsAsString();
//	}

    @SuppressWarnings("unchecked")
    @Override
    public void readFrom(Element rootElement)
    /* Is passed an Element by TC, and is expected to persist it to the settings object.
     * Old settings should be overwritten.
     */
    {
        if(slackNotificationMainConfig.getConfigFileExists()){
            // The MainConfigProcessor approach has been deprecated.
            // Instead we will use our own config file so we have better control over when it is persisted
            return;
        }
    	Loggers.SERVER.info("SlackNotificationMainSettings: re-reading main settings using old-style MainConfigProcessor. From now on we will use the slack/slack-config.xml file instead of main-config.xml");
    	Loggers.SERVER.debug(NAME + ":readFrom :: " + rootElement.toString());
    	RocketNotificationMainConfig tempConfig = new RocketNotificationMainConfig(serverPaths);
    	Element slackNotificationsElement = rootElement.getChild(ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME);
        tempConfig.readConfigurationFromXmlElement(slackNotificationsElement);
        this.slackNotificationMainConfig = tempConfig;
        tempConfig.save();
    }

    @Override
    public void writeTo(Element parentElement)
    /* Is passed an (probably empty) Element by TC, which is expected to be populated from the settings
     * in memory.
     */
    {

    }

    public RocketNotificationMainConfig getSlackNotificationMainConfig() {
        return slackNotificationMainConfig;
    }

    public void setSlackNotificationMainConfig(RocketNotificationMainConfig slackNotificationMainConfig) {
        this.slackNotificationMainConfig = slackNotificationMainConfig;
    }

//    public String getProxy(){
//    	return this.slackNotificationMainConfig.getProxyConfig().getProxyHost();
//    }

    public String getInfoText(){
    	return this.slackNotificationMainConfig.getSlackNotificationInfoText();
    }

    public String getInfoUrl(){
    	return this.slackNotificationMainConfig.getSlackNotificationInfoUrl();
    }

    public String getDefaultChannel() {
        return this.slackNotificationMainConfig.getDefaultChannel();
    }

    public String getTeamName() {
        return this.slackNotificationMainConfig.getTeamName();
    }

    public String getToken() {
        return this.slackNotificationMainConfig.getToken();
    }

    public String getIconUrl()
    {
        return this.slackNotificationMainConfig.getContent().getIconUrl();
    }

    public String getRocketUrl()
    {
        return this.slackNotificationMainConfig.getContent().getRocketUrl();
    }

    public String getTitle()
    {
        return this.slackNotificationMainConfig.getContent().getTitle();
    }

    public String getEmoji()
    {
        return this.slackNotificationMainConfig.getContent().getEmoji();
    }

    public String getBotName()
    {
        return this.slackNotificationMainConfig.getContent().getBotName();
    }

    public boolean getEnabled(){
        return this.slackNotificationMainConfig.getEnabled();
    }


    public Boolean getShowBuildAgent() {
        return this.slackNotificationMainConfig.getContent().getShowBuildAgent();
    }

    public Boolean getShowElapsedBuildTime() {
        return this.slackNotificationMainConfig.getContent().getShowElapsedBuildTime();
    }

    public boolean getShowCommits(){
        return this.slackNotificationMainConfig.getContent().getShowCommits();
    }

    public boolean getShowCommitters(){
        return this.slackNotificationMainConfig.getContent().getShowCommitters();
    }

    public boolean getShowTriggeredBy(){
        return this.slackNotificationMainConfig.getContent().getShowTriggeredBy();
    }

    public Boolean getShowFailureReason() {
        return this.slackNotificationMainConfig.getContent().getShowFailureReason();
    }

    public Boolean getSlackNotificationShowFurtherReading(){
    	return this.slackNotificationMainConfig.getSlackNotificationShowFurtherReading();
    }

	public void dispose() {
		Loggers.SERVER.debug(NAME + ":dispose() called");
	}

//	public SlackNotificationProxyConfig getProxyConfig() {
//		return this.slackNotificationMainConfig.getProxyConfig();	}


    public int getMaxCommitsToDisplay() {
        return this.slackNotificationMainConfig.getContent().getMaxCommitsToDisplay();
    }

    public void refresh() {
        this.slackNotificationMainConfig.refresh();
    }

    public String getPluginVersion() throws IOException {
        if(version != null){
            return version;
        }
        Properties props = new Properties();
        props.load(RocketNotificationMainSettings.class.getResourceAsStream("/version.txt"));
        version = props.getProperty("version");
        return version;
    }
}

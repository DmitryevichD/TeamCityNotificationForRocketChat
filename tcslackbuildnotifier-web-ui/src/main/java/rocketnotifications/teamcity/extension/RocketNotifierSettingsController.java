package rocketnotifications.teamcity.extension;


import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import rocketnotifications.SlackNotification;
import rocketnotifications.SlackNotificationImpl;
import rocketnotifications.teamcity.payload.RocketNotificationPayloadManager;
import rocketnotifications.teamcity.payload.content.Commit;
import rocketnotifications.teamcity.payload.content.SlackNotificationPayloadContent;
import rocketnotifications.teamcity.settings.RocketNotificationMainConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RocketNotifierSettingsController extends BaseController {

    private static final String CONTROLLER_PATH = "/rocketNotifier/adminSettings.html";
    public static final String EDIT_PARAMETER = "edit";
    public static final String TEST_PARAMETER = "test";
    private static final Object ACTION_ENABLE = "enable";
    private static final String ACTION_PARAMETER = "action";
    private String teamName;
    private String token;
    private String rocketUrl;
    private String titleText;
    private String emoji;
    private String botName;
    private String iconUrl;
    private String defaultChannel;
    private String maxCommitsToDisplay;
    private String showBuildAgent;
    private String showCommits;
    private String showCommitters;
    private String showTriggeredBy;
    private String showElapsedBuildTime;
    private String showFailureReason;
    private String proxyHost;
    private String proxyPort;
    private String proxyUser;
    private String proxyPassword;

    private SBuildServer server;
    private ServerPaths serverPaths;
    private WebControllerManager manager;
    private RocketNotificationMainConfig config;
    private RocketNotificationPayloadManager payloadManager;
    private PluginDescriptor descriptor;

    public RocketNotifierSettingsController(@NotNull SBuildServer server,
                                            @NotNull ServerPaths serverPaths,
                                            @NotNull WebControllerManager manager,
                                            @NotNull RocketNotificationMainConfig config,
                                            RocketNotificationPayloadManager payloadManager,
                                            PluginDescriptor descriptor){

        this.server = server;
        this.serverPaths = serverPaths;
        this.manager = manager;
        this.config = config;
        this.payloadManager = payloadManager;
        this.descriptor = descriptor;

        manager.registerController(CONTROLLER_PATH, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        HashMap<String, Object> params = new HashMap<String, Object>();

        if(request.getParameter(EDIT_PARAMETER) != null){
            params = this.handleConfigurationChange(request);
//            //logger.debug("Updating configuration");
        }
        else if(request.getParameter(TEST_PARAMETER) != null){
            params = this.handleTestNotification(request);
//            //logger.debug("Sending test notification");
        } else if (request.getParameter(ACTION_PARAMETER) != null) {
//            //logger.debug("Changing plugin status");
            this.handlePluginStatusChange(request);
        }
        return new ModelAndView(descriptor.getPluginResourcesPath() + "RocketNotification/ajaxEdit.jsp", params);
    }

    private void handlePluginStatusChange(HttpServletRequest request) {
        //logger.debug("Changing status");
        Boolean disabled = !request.getParameter(ACTION_PARAMETER).equals(ACTION_ENABLE);
        //logger.debug(String.format("Disabled status: %s", disabled));
        this.config.setEnabled(!disabled);
        this.config.save();
    }

    private void setRequestParams(HttpServletRequest request) {
        teamName = request.getParameter("teamName");
        token = request.getParameter("token");
        rocketUrl = request.getParameter("rocketUrl");
        titleText = request.getParameter("titleText");
        emoji = request.getParameter("emoji");
        botName = request.getParameter("botName");
        iconUrl = request.getParameter("iconUrl");
        defaultChannel = request.getParameter("defaultChannel");
        maxCommitsToDisplay = request.getParameter("maxCommitsToDisplay");
        showBuildAgent = request.getParameter("showBuildAgent");
        showCommits = request.getParameter("showCommits");
        showCommitters = request.getParameter("showCommitters");
        showTriggeredBy = request.getParameter("showTriggeredBy");
        showElapsedBuildTime = request.getParameter("showElapsedBuildTime");
        showFailureReason = request.getParameter("showFailureReason");
        proxyHost = request.getParameter("proxyHost");
        proxyPort = request.getParameter("proxyPort");
        proxyUser = request.getParameter("proxyUser");
        proxyPassword = request.getParameter("proxyPassword");
    }

    private HashMap<String, Object> handleTestNotification(HttpServletRequest request) throws IOException, SlackConfigValidationException {
        setRequestParams(request);
        HashMap<String, Object> params = new HashMap<String, Object>();

        Validate(teamName, token, botName, iconUrl, defaultChannel, maxCommitsToDisplay, showBuildAgent, proxyHost, proxyPort, proxyUser, proxyPassword, rocketUrl, titleText);

        SlackNotification notification = createMockNotification(teamName, defaultChannel, botName,
                token, iconUrl, Integer.parseInt(maxCommitsToDisplay),
                Boolean.parseBoolean(showElapsedBuildTime),
                Boolean.parseBoolean(showBuildAgent),
                Boolean.parseBoolean(showCommits),
                Boolean.parseBoolean(showCommitters),
                Boolean.parseBoolean(showTriggeredBy),
                Boolean.parseBoolean(showFailureReason),
                proxyHost, proxyPort, proxyUser, proxyPassword, rocketUrl, titleText, emoji);

        notification.post();

        this.getOrCreateMessages(request).addMessage("notificationSent", "The notification has been sent");

        params.put("messages", "Sent");


        return params;
    }

    private void Validate(String teamName, String token, String botName, String iconUrl, String defaultChannel
            , String maxCommitsToDisplay, String showBuildAgent, String proxyHost, String proxyPort, String proxyUser, String proxyPassword, String rocketUrl, String titleText) throws SlackConfigValidationException {
        if(teamName == null || StringUtil.isEmpty(teamName)
                || token == null || StringUtil.isEmpty(token)
                || rocketUrl == null || StringUtil.isEmpty(rocketUrl)
                || titleText == null || StringUtil.isEmpty(titleText)
                || botName == null || StringUtil.isEmpty(botName)
                || iconUrl == null || StringUtil.isEmpty(iconUrl)
                || defaultChannel == null || StringUtil.isEmpty(defaultChannel)
                || (showBuildAgent.toLowerCase() == "false" && (maxCommitsToDisplay == null || StringUtil.isEmpty(maxCommitsToDisplay)))
                || tryParseInt(maxCommitsToDisplay) == null
                || (!isNullOrEmpty(proxyHost) && isNullOrEmpty(proxyPort))
                || (!isNullOrEmpty(proxyUser) && isNullOrEmpty(proxyPassword))
                || (!isNullOrEmpty(proxyPort) && tryParseInt(proxyPort) == null)
                ){

            throw new SlackConfigValidationException("Could not validate parameters. Please recheck the request.");
        }
    }

    private boolean isNullOrEmpty(String str){
        return str == null || StringUtil.isEmpty(str);
    }

    public SlackNotification createMockNotification(String teamName, String defaultChannel, String botName,
                                                    String token, String iconUrl, Integer maxCommitsToDisplay,
                                                    Boolean showElapsedBuildTime, Boolean showBuildAgent, Boolean showCommits,
                                                    Boolean showCommitters, Boolean showTriggeredBy, Boolean showFailureReason, String proxyHost,
                                                    String proxyPort, String proxyUser, String proxyPassword, String rocketUrl,
                                                    String titleText, String emoji) {
        SlackNotification notification = new SlackNotificationImpl(defaultChannel);
        notification.setTeamName(teamName);
        notification.setBotName(botName);
        notification.setTitleText(titleText);
        notification.setRocketUrl(rocketUrl);
        notification.setEmoji(emoji);
        notification.setToken(token);
        notification.setIconUrl(iconUrl);
        notification.setMaxCommitsToDisplay(maxCommitsToDisplay);
        notification.setShowElapsedBuildTime(showElapsedBuildTime);
        notification.setShowBuildAgent(showBuildAgent);
        notification.setShowCommits(showCommits);
        notification.setShowCommitters(showCommitters);
        notification.setShowTriggeredBy(showTriggeredBy);
        notification.setShowFailureReason(showFailureReason);

        if(proxyHost != null && !StringUtil.isEmpty(proxyHost)){
            Credentials creds = null;
            if(proxyUser != null && !StringUtil.isEmpty(proxyUser)){
                creds = new UsernamePasswordCredentials(proxyUser, proxyPassword);
            }
            notification.setProxy(proxyHost, Integer.parseInt(proxyPort), creds);
        }

        SlackNotificationPayloadContent payload = new SlackNotificationPayloadContent();
        payload.setAgentName("Build Agent 1");

        payload.setBranchDisplayName("master");
        payload.setBranchIsDefault(true);
        payload.setBuildDescriptionWithLinkSyntax(String.format("<http://buildserver/builds/|Failed - My Awesome Build #5>"));
        payload.setBuildFullName("The Awesome Build");
        payload.setBuildId("b123");
        payload.setBuildName("My Awesome Build");
        payload.setBuildResult("Failed");
        payload.setBuildStatusUrl("http://buildserver/builds");
        payload.setBuildTypeId("b123");
        payload.setColor("danger");
        List<Commit> commits = new ArrayList<Commit>();
        commits.add(new Commit("a5bdc78", "Bug fix for that awful thing", "jbloggs", "jbloggs"));
        commits.add(new Commit("cc4500d", "New feature that rocks", "bbrave", "bbrave"));
        commits.add(new Commit("abb23b4", "Merge of branch xyz", "ddruff", "ddruff"));
        payload.setCommits(commits);
        payload.setElapsedTime(13452);
        payload.setFirstFailedBuild(true);
        payload.setIsComplete(true);
        payload.setText("My Awesome build");
        notification.setPayload(payload);
        notification.setEnabled(true);

        return notification;
    }

    public Integer tryParseInt(String str) {
        Integer retVal;
        try {
            retVal = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            retVal = null; // or null if that is your preference
        }
        return retVal;
    }

    private HashMap<String, Object> handleConfigurationChange(HttpServletRequest request) throws IOException, SlackConfigValidationException {
        setRequestParams(request);
        if(!isNullOrEmpty(proxyPassword)){
            proxyPassword = RSACipher.decryptWebRequestData(proxyPassword);
        }

        Validate(teamName, token, botName, iconUrl, defaultChannel, maxCommitsToDisplay,
                showBuildAgent, proxyHost, proxyPort, proxyUser, proxyPassword, rocketUrl, titleText);

        this.config.setTeamName(teamName);
        this.config.setToken(token);
        this.config.getContent().setBotName(botName);
        this.config.getContent().setTitleText(titleText);
        this.config.getContent().setEmoji(emoji);
        this.config.getContent().setIconUrl(iconUrl);
        this.config.getContent().setRocketUrl(rocketUrl);
        this.config.setDefaultChannel(defaultChannel);
        this.config.getContent().setMaxCommitsToDisplay(Integer.parseInt(maxCommitsToDisplay));
        this.config.getContent().setShowBuildAgent(Boolean.parseBoolean(showBuildAgent));
        this.config.getContent().setShowCommits(Boolean.parseBoolean(showCommits));
        this.config.getContent().setShowCommitters(Boolean.parseBoolean(showCommitters));
        this.config.getContent().setShowTriggeredBy(Boolean.parseBoolean(showTriggeredBy));
        this.config.getContent().setShowElapsedBuildTime((Boolean.parseBoolean(showElapsedBuildTime)));
        this.config.getContent().setShowFailureReason((Boolean.parseBoolean(showFailureReason)));


        this.config.setProxyHost(proxyHost);
        this.config.setProxyPort(isNullOrEmpty(proxyPort) ? null : Integer.parseInt(proxyPort));
        this.config.setProxyUsername(proxyUser);
        this.config.setProxyPassword(proxyPassword);


        this.config.save();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("message", "Saved");
        return params;
    }

    public class SlackConfigValidationException extends Exception {
        public SlackConfigValidationException(String message) {
            super(message);
        }
    }
}
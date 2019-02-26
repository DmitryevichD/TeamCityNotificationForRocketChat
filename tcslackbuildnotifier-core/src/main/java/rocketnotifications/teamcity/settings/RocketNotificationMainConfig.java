package rocketnotifications.teamcity.settings;

import com.intellij.openapi.util.JDOMUtil;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import rocketnotifications.SlackNotificationProxyConfig;
import rocketnotifications.teamcity.Loggers;

import java.io.File;
import java.io.IOException;

public class RocketNotificationMainConfig implements ChangeListener {
    public static final String DEFAULT_BOTNAME = "TeamCity";
    public static final String DEFAULT_ICONURL = "http://res.guggy.com/logo_128.png";
	private static final String TOKEN = "token";
	private static final String DEFAULT_CHANNEL = "defaultChannel";
	private static final String ICON_URL = "iconurl";
	private static final String ROCKET_URL = "rocketUrl";
	private static final String TITLE = "title";
	private static final String EMOJI = "emoji";
	private static final String BOT_NAME = "botname";
	private static final String SHOW_BUILD_AGENT = "showBuildAgent";
	private static final String SHOW_COMMITS = "showCommits";
	private static final String SHOW_COMMITTERS = "showCommitters";
	private static final String SHOW_TRIGGERED_BY = "showTriggeredBy";
	private static final String SHOW_FAILURE_REASON = "showFailureReason";
	private static final String MAX_COMMITS_TO_DISPLAY = "maxCommitsToDisplay";
	private static final String SHOW_ELAPSED_BUILD_TIME = "showElapsedBuildTime";
	private static final String HTTPS = "https://";
	private static final String HTTP = "http://";
	private static final String PROXY = "proxy";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String ENABLED = "enabled";
	private static final String TEAM_NAME = "teamName";


    private final FileWatcher myChangeObserver;
	private final File myConfigDir;
	private final File myConfigFile;
	private String slacknotificationInfoUrl = null;
	private String slacknotificationInfoText = null;
	private Boolean slacknotificationShowFurtherReading = true;
//	private Integer proxyPort = null;
//	private String proxyHost = null;
//	private String proxyUsername = null;
//	private String proxyPassword = null;
    private String defaultChannel = null;
    private String teamName;
    private String token;
//	private Boolean proxyShortNames = false;
    private boolean enabled = true;
//    private String title;
//    private String emoji;

	public final String SINGLE_HOST_REGEX = "^[^./~`'\"]+(?:/.*)?$";
	public final String HOSTNAME_ONLY_REGEX = "^([^/]+)(?:/.*)?$";
    private SlackNotificationContentConfig content;
    private boolean configFileExists;




	public RocketNotificationMainConfig(ServerPaths serverPaths) {
        this.content = new SlackNotificationContentConfig();
		this.myConfigDir = new File(serverPaths.getConfigDir(), "rocket");
		this.myConfigFile = new File(this.myConfigDir, "rocket-config.xml");
        configFileExists = this.myConfigFile.exists();
		reloadConfiguration();
		this.myChangeObserver = new FileWatcher(this.myConfigFile);
		this.myChangeObserver.setSleepingPeriod(10000L);
		this.myChangeObserver.registerListener(this);
		this.myChangeObserver.start();
	}

    public void refresh(){
        reloadConfiguration();
    }

	private void reloadConfiguration() {
		Loggers.ACTIVITIES.info("Loading configuration file: " + this.myConfigFile.getAbsolutePath());

		myConfigDir.mkdirs();
		FileUtil.copyResourceIfNotExists(getClass(), "/config_templates/rocket-config.xml", new File(this.myConfigDir, "rocket-config.xml"));

		Document document = parseFile(this.myConfigFile);
		if (document != null)
		{
			Element rootElement = document.getRootElement();
			readConfigurationFromXmlElement(rootElement);
		}
	}

	private Document parseFile(File configFile)
	{
		try
		{
			if (configFile.isFile()) {
				return JDOMUtil.loadDocument(configFile);
			}
		}
		catch (JDOMException e)
		{
			Loggers.ACTIVITIES.error("Failed to parse xml configuration file: " + configFile.getAbsolutePath(), e);
		}
		catch (IOException e)
		{
			Loggers.ACTIVITIES.error("I/O error occurred on attempt to parse xml configuration file: " + configFile.getAbsolutePath(), e);
		}
		return null;
	}

//	public String getProxySettingsAsString(){
//    	return " host:" + this.proxyHost + " port: " + this.proxyPort;
//	}

	public String stripProtocolFromUrl(String url){
		String tmpURL = url;
		if(tmpURL.length() > HTTPS.length()
			&& HTTPS.equalsIgnoreCase(tmpURL.substring(0,HTTPS.length())))
		{
				tmpURL = tmpURL.substring(HTTPS.length());
		} else if (tmpURL.length() > HTTP.length()
			&& HTTP.equalsIgnoreCase(tmpURL.substring(0,HTTP.length())))
		{
				tmpURL = tmpURL.substring(HTTP.length());
		}
		return tmpURL;
	}



	public Element getInfoUrlAsElement(){
		/*
			<info url="http://acme.com/" text="Using SlackNotifications in Acme Inc." />
		 */
		if (this.slacknotificationInfoUrl != null && this.slacknotificationInfoUrl.length() > 0){
			Element e = new Element("info");
			e.setAttribute("url", slacknotificationInfoUrl);
			if (this.slacknotificationInfoText != null && this.slacknotificationInfoText.length() > 0){
				e.setAttribute("text", slacknotificationInfoText);
			} else {
				e.setAttribute("text", slacknotificationInfoUrl);
			}
			e.setAttribute("show-reading", slacknotificationShowFurtherReading.toString());

			return e;
		}
		return null;
	}


//	public Element getProxyAsElement(){
//		/*
//    		  <proxy host="myproxy.mycompany.com" port="8080" >
//      			<noproxy url=".mycompany.com" />
//      			<noproxy url="192.168.0." />
//    		  </proxy>
//		 */
//		if (this.getProxyHost() == null || this.getProxyPort() == null){
//			return null;
//		}
//		Element el = new Element(PROXY);
//		el.setAttribute("host", this.getProxyHost());
//		el.setAttribute("port", String.valueOf(this.getProxyPort()));
//		if (   this.proxyPassword != null && this.proxyPassword.length() > 0
//			&& this.proxyUsername != null && this.proxyUsername.length() > 0 )
//		{
//			el.setAttribute(USERNAME, this.getProxyUsername());
//			el.setAttribute(PASSWORD, this.getProxyPassword());
//
//		}
//		return el;
//	}

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

//	public Integer getProxyPort() {
//		return proxyPort;
//	}
//
//	public void setProxyPort(Integer proxyPort) {
//		this.proxyPort = proxyPort;
//	}
//
//	public String getProxyHost() {
//		return proxyHost;
//	}
//
//	public void setProxyHost(String proxyHost) {
//		this.proxyHost = proxyHost;
//	}
//
//	public String getProxyUsername() {
//		return proxyUsername;
//	}
//
//	public void setProxyUsername(String proxyUsername) {
//		this.proxyUsername = proxyUsername;
//	}
//
//	public String getProxyPassword() {
//		return proxyPassword;
//	}
//
//	public void setProxyPassword(String proxyPassword) {
//		this.proxyPassword = proxyPassword;
//	}
//
//	public Boolean isProxyShortNames() {
//		return proxyShortNames;
//	}
//
//	public void setProxyShortNames(Boolean proxyShortNames) {
//		this.proxyShortNames = proxyShortNames;
//	}

	public String getSlackNotificationInfoUrl() {
		return slacknotificationInfoUrl;
	}

	public String getSlackNotificationInfoText() {
		return slacknotificationInfoText;
	}

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public void setSlackNotificationInfoUrl(String slacknotificationInfoUrl) {
		this.slacknotificationInfoUrl = slacknotificationInfoUrl;
	}

	public void setSlackNotificationInfoText(String slacknotificationInfoText) {
		this.slacknotificationInfoText = slacknotificationInfoText;
	}

	public void setSlackNotificationShowFurtherReading(Boolean slacknotificationShowFurtherReading) {
		this.slacknotificationShowFurtherReading = slacknotificationShowFurtherReading;
	}

	public Boolean getSlackNotificationShowFurtherReading() {
		return slacknotificationShowFurtherReading;
	}


	public synchronized void save()
	{
		this.myChangeObserver.runActionWithDisabledObserver(new Runnable()
		{
			public void run()
			{
				FileUtil.processXmlFile(RocketNotificationMainConfig.this.myConfigFile, new FileUtil.Processor() {
					public void process(Element rootElement) {
                        rootElement.setAttribute("enabled", Boolean.toString(RocketNotificationMainConfig.this.enabled));
                        rootElement.setAttribute(TEAM_NAME, emptyIfNull(RocketNotificationMainConfig.this.teamName));
						rootElement.setAttribute(DEFAULT_CHANNEL, emptyIfNull(RocketNotificationMainConfig.this.defaultChannel));
                        rootElement.setAttribute(TEAM_NAME, emptyIfNull(RocketNotificationMainConfig.this.teamName));
						rootElement.setAttribute(TOKEN, emptyIfNull(RocketNotificationMainConfig.this.token));
						rootElement.setAttribute(ICON_URL, emptyIfNull(RocketNotificationMainConfig.this.content.getIconUrl()));
						rootElement.setAttribute(ROCKET_URL, emptyIfNull(RocketNotificationMainConfig.this.content.getRocketUrl()));
						rootElement.setAttribute(TITLE, emptyIfNull(RocketNotificationMainConfig.this.content.getTitle()));
						rootElement.setAttribute(EMOJI, emptyIfNull(RocketNotificationMainConfig.this.content.getEmoji()));
						rootElement.setAttribute(BOT_NAME, emptyIfNull(RocketNotificationMainConfig.this.content.getBotName()));
                        rootElement.setAttribute(ENABLED, Boolean.toString(RocketNotificationMainConfig.this.enabled));
                        rootElement.setAttribute(TEAM_NAME, emptyIfNull(RocketNotificationMainConfig.this.teamName));
						rootElement.setAttribute("defaultChannel", emptyIfNull(RocketNotificationMainConfig.this.defaultChannel));
                        rootElement.setAttribute(TEAM_NAME, emptyIfNull(RocketNotificationMainConfig.this.teamName));
						rootElement.setAttribute("token", emptyIfNull(RocketNotificationMainConfig.this.token));
						rootElement.setAttribute("iconurl", emptyIfNull(RocketNotificationMainConfig.this.content.getIconUrl()));
						rootElement.setAttribute("botname", emptyIfNull(RocketNotificationMainConfig.this.content.getBotName()));

						if(RocketNotificationMainConfig.this.content.getShowBuildAgent() != null){
							rootElement.setAttribute(SHOW_BUILD_AGENT, Boolean.toString(RocketNotificationMainConfig.this.content.getShowBuildAgent()));
						}
						if(RocketNotificationMainConfig.this.content.getShowElapsedBuildTime() != null) {
							rootElement.setAttribute(SHOW_ELAPSED_BUILD_TIME, Boolean.toString(RocketNotificationMainConfig.this.content.getShowElapsedBuildTime()));
						}
						if(RocketNotificationMainConfig.this.content.getShowCommits() != null) {
							rootElement.setAttribute(SHOW_COMMITS, Boolean.toString(RocketNotificationMainConfig.this.content.getShowCommits()));
						}
						if(RocketNotificationMainConfig.this.content.getShowCommitters() != null) {
							rootElement.setAttribute(SHOW_COMMITTERS, Boolean.toString(RocketNotificationMainConfig.this.content.getShowCommitters()));
						}
						if(RocketNotificationMainConfig.this.content.getShowTriggeredBy() != null) {
							rootElement.setAttribute(SHOW_TRIGGERED_BY, Boolean.toString(RocketNotificationMainConfig.this.content.getShowTriggeredBy()));
						}
                        if(RocketNotificationMainConfig.this.content.getShowFailureReason() != null) {
                            rootElement.setAttribute(SHOW_FAILURE_REASON, Boolean.toString(RocketNotificationMainConfig.this.content.getShowFailureReason()));
                        }
						rootElement.setAttribute(MAX_COMMITS_TO_DISPLAY, Integer.toString(RocketNotificationMainConfig.this.content.getMaxCommitsToDisplay()));

                        rootElement.removeChildren(PROXY);
                        rootElement.removeChildren("info");

//						if(getProxyHost() != null && getProxyHost().length() > 0
//								&& getProxyPort() != null && getProxyPort() > 0 )
//						{
//							rootElement.addContent(getProxyAsElement());
//							Loggers.SERVER.debug(RocketNotificationMainConfig.class.getName() + "writeTo :: proxyHost " + getProxyHost());
//							Loggers.SERVER.debug(RocketNotificationMainConfig.class.getName() + "writeTo :: proxyPort " + getProxyPort());
//						}

						if(getInfoUrlAsElement() != null){
                            rootElement.addContent(getInfoUrlAsElement());
							Loggers.SERVER.debug(RocketNotificationMainConfig.class.getName() + "writeTo :: infoText " + getSlackNotificationInfoText());
							Loggers.SERVER.debug(RocketNotificationMainConfig.class.getName() + "writeTo :: InfoUrl  " + getSlackNotificationInfoUrl());
							Loggers.SERVER.debug(RocketNotificationMainConfig.class.getName() + "writeTo :: show-reading  " + getSlackNotificationShowFurtherReading().toString());
						}
					}
				});
			}
		});
	}

    private String emptyIfNull(String str){
        return str == null ? "" : str;
    }

	@Override
	public void changeOccured(String s) {
		reloadConfiguration();
	}

	public boolean getConfigFileExists() {
		return configFileExists;
	}

	void readConfigurationFromXmlElement(Element slackNotificationsElement) {
        if(slackNotificationsElement != null){
            content.setEnabled(true);
            if(slackNotificationsElement.getAttribute(ENABLED) != null)
            {
                setEnabled(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(ENABLED)));
            }
            if(slackNotificationsElement.getAttribute(DEFAULT_CHANNEL) != null)
            {
                setDefaultChannel(slackNotificationsElement.getAttributeValue(DEFAULT_CHANNEL));
            }
            if(slackNotificationsElement.getAttribute(TEAM_NAME) != null)
            {
                setTeamName(slackNotificationsElement.getAttributeValue(TEAM_NAME));
            }
            if(slackNotificationsElement.getAttribute(TOKEN) != null)
            {
                setToken(slackNotificationsElement.getAttributeValue(TOKEN));
            }
            if(slackNotificationsElement.getAttribute(ICON_URL) != null)
            {
                content.setIconUrl(slackNotificationsElement.getAttributeValue(ICON_URL));
            }
            if(slackNotificationsElement.getAttribute(ROCKET_URL) != null)
            {
                content.setRocketUrl(slackNotificationsElement.getAttributeValue(ROCKET_URL));
            }
			if(slackNotificationsElement.getAttribute(TITLE) != null)
			{
				content.setRocketUrl(slackNotificationsElement.getAttributeValue(TITLE));
			}
			if(slackNotificationsElement.getAttribute(EMOJI) != null)
			{
				content.setRocketUrl(slackNotificationsElement.getAttributeValue(EMOJI));
			}
            if(slackNotificationsElement.getAttribute(BOT_NAME) != null)
            {
                content.setBotName(slackNotificationsElement.getAttributeValue(BOT_NAME));
            }
            if(slackNotificationsElement.getAttribute(SHOW_BUILD_AGENT) != null)
            {
                content.setShowBuildAgent(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_BUILD_AGENT)));
            }
            if(slackNotificationsElement.getAttribute(SHOW_ELAPSED_BUILD_TIME) != null)
            {
                content.setShowElapsedBuildTime(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_ELAPSED_BUILD_TIME)));
            }
            if(slackNotificationsElement.getAttribute(SHOW_COMMITS) != null)
            {
                content.setShowCommits(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_COMMITS)));
            }
            if(slackNotificationsElement.getAttribute(SHOW_COMMITTERS) != null)
            {
                content.setShowCommitters(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_COMMITTERS)));
            }
            if(slackNotificationsElement.getAttribute(SHOW_TRIGGERED_BY) != null)
            {
                content.setShowTriggeredBy(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_TRIGGERED_BY)));
            }
            if(slackNotificationsElement.getAttribute(MAX_COMMITS_TO_DISPLAY) != null)
            {
                content.setMaxCommitsToDisplay(Integer.parseInt(slackNotificationsElement.getAttributeValue(MAX_COMMITS_TO_DISPLAY)));
            }
            if(slackNotificationsElement.getAttribute(SHOW_FAILURE_REASON) != null)
            {
                content.setShowFailureReason(Boolean.parseBoolean(slackNotificationsElement.getAttributeValue(SHOW_FAILURE_REASON)));
            }

//            Element proxyElement = slackNotificationsElement.getChild(PROXY);
//            if(proxyElement != null)
//            {
//                if (proxyElement.getAttribute("proxyShortNames") != null){
//                    setProxyShortNames(Boolean.parseBoolean(proxyElement.getAttributeValue("proxyShortNames")));
//                }
//
//                if (proxyElement.getAttribute("host") != null){
//                    setProxyHost(proxyElement.getAttributeValue("host"));
//                }
//
//                if (proxyElement.getAttribute("port") != null){
//                    setProxyPort(Integer.parseInt(proxyElement.getAttributeValue("port")));
//                }
//
//                if (proxyElement.getAttribute(USERNAME) != null){
//                    setProxyUsername(proxyElement.getAttributeValue(USERNAME));
//                }
//
//                if (proxyElement.getAttribute(PASSWORD) != null){
//                    setProxyPassword(proxyElement.getAttributeValue(PASSWORD));
//                }
//            }
//            else {
//                setProxyHost(null);
//                setProxyPort(null);
//                setProxyUsername(null);
//                setProxyPassword(null);
//            }
        }
    }

//    public SlackNotificationProxyConfig getProxyConfig() {
//        return new SlackNotificationProxyConfig(proxyHost, proxyPort, proxyUsername, proxyPassword);
//    }

    public SlackNotificationContentConfig getContent() {
        if(content == null){
            this.content = new SlackNotificationContentConfig();
        }
        return content;
    }
//
//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public String getEmoji() {
//		return emoji;
//	}
//
//	public void setEmoji(String emoji) {
//		this.emoji = emoji;
//	}
}

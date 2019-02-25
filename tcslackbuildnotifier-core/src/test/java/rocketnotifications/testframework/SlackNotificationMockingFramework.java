package rocketnotifications.testframework;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jdom.JDOMException;
import rocketnotifications.teamcity.payload.RocketNotificationPayloadManager;
import rocketnotifications.teamcity.payload.content.SlackNotificationPayloadContent;
import rocketnotifications.teamcity.settings.SlackNotificationConfig;
import rocketnotifications.teamcity.settings.RocketNotificationProjectSettings;

import java.io.File;
import java.io.IOException;

public interface SlackNotificationMockingFramework {
	
	public SBuildServer getServer();
	public SRunningBuild getRunningBuild();
	public SBuildType getSBuildType();
	public SBuildType getSBuildTypeFromSubProject();
	public SlackNotificationConfig getSlackNotificationConfig();
	public SlackNotificationPayloadContent getSlackNotificationContent();
	public RocketNotificationPayloadManager getSlackNotificationPayloadManager();
	public RocketNotificationProjectSettings getSlackNotificationProjectSettings();
	public void loadSlackNotificationConfigXml(File xmlConfigFile) throws JDOMException, IOException;
	public void loadSlackNotificationProjectSettingsFromConfigXml(File xmlConfigFile) throws IOException, JDOMException;

}

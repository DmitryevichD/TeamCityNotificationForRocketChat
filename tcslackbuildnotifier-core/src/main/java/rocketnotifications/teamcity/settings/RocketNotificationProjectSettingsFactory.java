package rocketnotifications.teamcity.settings;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import rocketnotifications.teamcity.Loggers;

import static rocketnotifications.teamcity.RocketNotificationListener.ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME;


public class RocketNotificationProjectSettingsFactory implements ProjectSettingsFactory {
	
	public RocketNotificationProjectSettingsFactory(ProjectSettingsManager projectSettingsManager){
		Loggers.SERVER.info("SlackNotificationProjectSettingsFactory :: Registering");
		projectSettingsManager.registerSettingsFactory(ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME, this);
	}

	@Override
	public RocketNotificationProjectSettings createProjectSettings(String projectId) {
		Loggers.SERVER.info("SlackNotificationProjectSettingsFactory: re-reading settings for " + projectId);
		RocketNotificationProjectSettings whs = new RocketNotificationProjectSettings();
		return whs;
	}


}

package rocketnotifications.testframework;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jdom.JDOMException;
import rocketnotifications.teamcity.settings.RocketNotificationProjectSettings;

import java.io.File;
import java.io.IOException;

public interface RocketNotificationMockingFramework {

    SBuildServer getServer();

    SBuildType getSBuildType();

    RocketNotificationProjectSettings getSlackNotificationProjectSettings();

    void loadSlackNotificationProjectSettingsFromConfigXml(File xmlConfigFile) throws IOException, JDOMException;
}

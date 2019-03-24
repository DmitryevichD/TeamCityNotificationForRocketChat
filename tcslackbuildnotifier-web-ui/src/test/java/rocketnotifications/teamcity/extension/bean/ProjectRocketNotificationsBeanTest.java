package rocketnotifications.teamcity.extension.bean;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jdom.JDOMException;
import org.junit.Ignore;
import org.junit.Test;
import rocketnotifications.teamcity.BuildStateEnum;
import rocketnotifications.teamcity.settings.RocketNotificationMainSettings;
import rocketnotifications.testframework.RocketNotificationMockingFramework;
import rocketnotifications.testframework.RocketNotificationMockingFrameworkImpl;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.Mockito.mock;

public class ProjectRocketNotificationsBeanTest {

	SortedMap<String, String> map = new TreeMap<String, String>();
	RocketNotificationMockingFramework framework;
    SBuildServer sBuildServer = mock(SBuildServer.class);

	@Test
	public void JsonSerialisationTest() throws JDOMException, IOException {
        ServerPaths serverPaths = mock(ServerPaths.class);
        RocketNotificationMainSettings myMainSettings = new RocketNotificationMainSettings(sBuildServer, serverPaths);
        framework = RocketNotificationMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED);
		framework.loadSlackNotificationProjectSettingsFromConfigXml(new File("../tcslackbuildnotifier-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectSlackNotificationsBean slacknotificationsConfig = ProjectSlackNotificationsBean.build(framework.getSlackNotificationProjectSettings() , framework.getServer().getProjectManager().findProjectById("project01"), myMainSettings);
		System.out.println(ProjectSlackNotificationsBeanJsonSerialiser.serialise(slacknotificationsConfig));
	}

	@Test
	public void JsonBuildSerialisationTest() throws JDOMException, IOException {
        ServerPaths serverPaths = mock(ServerPaths.class);
        RocketNotificationMainSettings myMainSettings = new RocketNotificationMainSettings(sBuildServer, serverPaths);
        framework = RocketNotificationMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED);
		framework.loadSlackNotificationProjectSettingsFromConfigXml(new File("../tcslackbuildnotifier-core/src/test/resources/project-settings-test-all-states-enabled-with-specific-builds.xml"));
		ProjectSlackNotificationsBean slacknotificationsConfig = ProjectSlackNotificationsBean.build(framework.getSlackNotificationProjectSettings() ,framework.getSBuildType() ,framework.getServer().getProjectManager().findProjectById("project01"), myMainSettings);
		System.out.println(ProjectSlackNotificationsBeanJsonSerialiser.serialise(slacknotificationsConfig));
	}

}

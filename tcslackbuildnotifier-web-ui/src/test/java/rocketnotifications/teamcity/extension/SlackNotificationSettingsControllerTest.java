package rocketnotifications.teamcity.extension;



import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.junit.Test;
import rocketnotifications.SlackNotification;
import rocketnotifications.teamcity.payload.RocketNotificationPayloadManager;
import rocketnotifications.teamcity.settings.RocketNotificationMainConfig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlackNotificationSettingsControllerTest {
    SBuildServer sBuildServer = mock(SBuildServer.class);
    WebControllerManager webControllerManager = mock(WebControllerManager.class);

    @Test
    public void createMockNotification_constructsValidNotification(){
        String expectedConfigDirectory = ".";
        ServerPaths serverPaths = mock(ServerPaths.class);
        when(serverPaths.getConfigDir()).thenReturn(expectedConfigDirectory);

        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);

        RocketNotificationMainConfig config = new RocketNotificationMainConfig(serverPaths);

        RocketNotificationPayloadManager payloadManager = new RocketNotificationPayloadManager(sBuildServer);
        RocketNotifierSettingsController controller = new RocketNotifierSettingsController(
                sBuildServer, serverPaths, webControllerManager,
                config, payloadManager, pluginDescriptor);

        SlackNotification notification = controller.createMockNotification(
                "the team",
                "#general",
                "The Bot",
                "tokenthingy",
                RocketNotificationMainConfig.DEFAULT_ICONURL,
                5,
                true,
                true,
                true,
                true,
                true,
                true,
                null, null, null, null);

        assertNotNull(notification);
        assertEquals("the team", notification.getTeamName());
        assertEquals(RocketNotificationMainConfig.DEFAULT_ICONURL, notification.getIconUrl());
    }
}

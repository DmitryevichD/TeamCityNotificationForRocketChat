package rocketnotifications.teamcity;

import org.apache.http.client.HttpClient;
import rocketnotifications.SlackNotification;

public interface SlackNotificationFactory {
	SlackNotification getSlackNotification();

//	SlackNotification getSlackNotification(String channel, String proxy, Integer proxyPort);

    SlackNotification getSlackNotification(HttpClient httpClient, String string);
}

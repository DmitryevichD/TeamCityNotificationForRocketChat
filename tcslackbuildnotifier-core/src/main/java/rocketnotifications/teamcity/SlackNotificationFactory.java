package rocketnotifications.teamcity;

import org.apache.http.client.HttpClient;
import rocketnotifications.SlackNotification;
import rocketnotifications.SlackNotificationProxyConfig;

public interface SlackNotificationFactory {
	public abstract SlackNotification getSlackNotification();
	public abstract SlackNotification getSlackNotification(String channel, String proxy,
                                                           Integer proxyPort);
	public abstract SlackNotification getSlackNotification(String string);

    public abstract SlackNotification getSlackNotification(HttpClient httpClient, String string);

	public abstract SlackNotification getSlackNotification(String channel, String proxy,
                                                           String proxyPortString);
	public abstract SlackNotification getSlackNotification(String string, SlackNotificationProxyConfig pc);
}

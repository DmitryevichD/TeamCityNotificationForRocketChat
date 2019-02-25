package rocketnotifications.teamcity;


import org.apache.http.client.HttpClient;
import rocketnotifications.SlackNotification;
import rocketnotifications.SlackNotificationImpl;
import rocketnotifications.SlackNotificationProxyConfig;

public class RocketNotificationFactoryImpl implements SlackNotificationFactory {
	public SlackNotification getSlackNotification(){
		return new SlackNotificationImpl();
	}

	public SlackNotification getSlackNotification(String channel, String proxy, Integer proxyPort) {
		return new SlackNotificationImpl(channel, proxy, proxyPort);
	}

	public SlackNotification getSlackNotification(String channel) {
		return new SlackNotificationImpl(channel);
	}

    @Override
    public SlackNotification getSlackNotification(HttpClient httpClient, String channel) {
        return new SlackNotificationImpl(httpClient, channel);
    }

    public SlackNotification getSlackNotification(String channel, String proxy, String proxyPort) {
		return new SlackNotificationImpl(channel, proxy, proxyPort);
	}

	public SlackNotification getSlackNotification(String channel, SlackNotificationProxyConfig proxyConfig) {
		return new SlackNotificationImpl(channel, proxyConfig);
	}
}

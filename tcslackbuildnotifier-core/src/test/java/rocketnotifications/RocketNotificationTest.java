package rocketnotifications;

import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rocketnotifications.teamcity.BuildStateEnum;
import rocketnotifications.teamcity.SlackNotificationFactory;
import rocketnotifications.teamcity.RocketNotificationFactoryImpl;
import rocketnotifications.teamcity.payload.content.Commit;
import rocketnotifications.teamcity.payload.content.PostMessageResponse;
import rocketnotifications.teamcity.payload.content.SlackNotificationPayloadContent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RocketNotificationTest {
	public String proxy = "127.0.0.1";
	public Integer proxyPort = 58002;
	String proxyPortString = "58002";
	public Integer webserverPort = 58001;
	public Integer proxyserverPort = 58002;
	public String webserverHost = "127.0.0.1";
	String url = "http://127.0.0.1:58001";

	SlackNotificationFactory factory = new RocketNotificationFactoryImpl();

	@Test
	public void test_BuildStates(){
		assertTrue(BuildStateEnum.BUILD_STARTED.getShortName().equals("buildStarted"));
		assertTrue(BuildStateEnum.BUILD_FINISHED.getShortName().equals("buildFinished"));
		assertTrue(BuildStateEnum.BEFORE_BUILD_FINISHED.getShortName().equals("beforeBuildFinish"));
		assertTrue(BuildStateEnum.RESPONSIBILITY_CHANGED.getShortName().equals("responsibilityChanged"));
		assertTrue(BuildStateEnum.BUILD_INTERRUPTED.getShortName().equals("buildInterrupted"));


	}

//	@Test
//	public void test_URL() {
//		SlackNotification W = factory.getSlackNotification(url, proxy, proxyPort);
//		assertTrue(W.getChannel() == url);
//	}

    @Test
    public void post_whenResponseIsOk_doesNotThrow() throws IOException {
        ArgumentCaptor<HttpPost> requestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, ""));
        PostMessageResponse successfulResponse = new PostMessageResponse();
        successfulResponse.setOk(true);
        successfulResponse.setError("channel_not_found");
        response.setEntity(new StringEntity(successfulResponse.toJson()));

        when(httpClient.execute(requestCaptor.capture())).thenReturn(response);

        SlackNotification w = factory.getSlackNotification(httpClient, "#test-channel");

        SlackNotificationPayloadContent content = new SlackNotificationPayloadContent();
        content.setBuildDescriptionWithLinkSyntax("http://foo");
        content.setCommits(new ArrayList<Commit>());

        w.setPayload(content);
        w.setEnabled(true);
        w.post();

        List<HttpPost> capturedRequests = requestCaptor.getAllValues();
        HttpPost request = capturedRequests.get(0);

        assertNotNull(w.getResponse());
        assertTrue(w.getResponse().getOk());
    }

    @Test
    public void post_whenResponseIsFailure_logsException() throws IOException {
        ArgumentCaptor<HttpPost> requestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, ""));
        PostMessageResponse failedResponse = new PostMessageResponse();
        failedResponse.setOk(false);
        failedResponse.setError("channel_not_found");
        response.setEntity(new StringEntity(failedResponse.toJson()));

        when(httpClient.execute(requestCaptor.capture())).thenReturn(response);

        SlackNotification w = factory.getSlackNotification(httpClient, "#test-channel");

        SlackNotificationPayloadContent content = new SlackNotificationPayloadContent();
        content.setBuildDescriptionWithLinkSyntax("http://foo");
        content.setCommits(new ArrayList<Commit>());

        w.setPayload(content);
        w.setEnabled(true);
        w.post();

        assertNotNull(w.getResponse());
        assertFalse(w.getResponse().getOk());
    }

    @Test
    public void isApiToken_whenApiTokenIsSupplied_returnsTrue(){
        SlackNotificationImpl impl = new SlackNotificationImpl();
        impl.setToken("xoxp-sdsdfs-3453efgeg-35tefb");
        assertTrue(impl.getIsApiToken());
    }

    @Test
    public void isApiToken_whenOAuthAppTokenIsSupplied_returnsTrue(){
        SlackNotificationImpl impl = new SlackNotificationImpl();
        impl.setToken("34tsrfdgdrtyrysdfg");
        assertFalse(impl.getIsApiToken());
    }

	public SlackNotificationTestServer startWebServer(){
		try {
			SlackNotificationTestServer s = new SlackNotificationTestServer(webserverHost, webserverPort);
			s.server.start();
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void stopWebServer(SlackNotificationTestServer s) throws InterruptedException {
		try {
			s.server.stop();
			// Sleep to let the server shutdown cleanly.
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Thread.sleep(1000);
		}
	}
}

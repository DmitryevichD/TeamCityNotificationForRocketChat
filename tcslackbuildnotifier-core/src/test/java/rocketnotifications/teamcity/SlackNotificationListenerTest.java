package rocketnotifications.teamcity;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rocketnotifications.teamcity.RocketNotificationListener.ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import rocketnotifications.SlackNotification;
import rocketnotifications.SlackNotificationImpl;
import rocketnotifications.teamcity.payload.RocketNotificationPayloadManager;
import rocketnotifications.teamcity.payload.content.PostMessageResponse;
import rocketnotifications.teamcity.settings.RocketNotificationMainSettings;
import rocketnotifications.teamcity.settings.RocketNotificationProjectSettings;

public class SlackNotificationListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	RocketNotificationMainSettings configSettings = mock(RocketNotificationMainSettings.class);
	RocketNotificationPayloadManager manager = mock(RocketNotificationPayloadManager.class);
//	SlackNotificationPayload payload = new SlackNotificationPayloadDetailed(manager);
	RocketNotificationProjectSettings projSettings;
	SlackNotificationFactory factory = mock(SlackNotificationFactory.class);
	SlackNotification slacknotification = mock (SlackNotification.class);
	SlackNotification slackNotificationImpl;
	SlackNotification spySlackNotification;
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<SFinishedBuild>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<SFinishedBuild>();
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	RocketNotificationListener rocketNotificationListener;


    @After
    @Before
    public void deleteSlackConfigFile(){
        DeleteConfigFiles();
    }

    private void DeleteConfigFiles() {
        File outputFile = new File("slack", "slack-config.xml");
        outputFile.delete();

        File outputDir = new File("slack");
        outputDir.delete();
    }

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, ""));
        PostMessageResponse successfulResponse = new PostMessageResponse();
        successfulResponse.setOk(true);
        successfulResponse.setError("channel_not_found");
        response.setEntity(new StringEntity(successfulResponse.toJson()));

        when(httpClient.execute(isA(HttpUriRequest.class))).thenReturn(response);
		slackNotificationImpl = new SlackNotificationImpl(httpClient, "");
		spySlackNotification = spy(slackNotificationImpl);
		rocketNotificationListener = new RocketNotificationListener(sBuildServer, settings, configSettings, manager, factory);
		projSettings = new RocketNotificationProjectSettings();
		when(factory.getSlackNotification()).thenReturn(spySlackNotification);
		//when(manager.isRegisteredFormat("JSON")).thenReturn(true);
//		when(manager.getFormat("JSON")).thenReturn(payload);
		//when(manager.getServer()).thenReturn(sBuildServer);
		when(sBuildServer.getProjectManager()).thenReturn(projectManager);
		when(projectManager.findProjectById("project1")).thenReturn(sProject);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.server");
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		sBuildType.setProject(sProject);
		when(settings.getSettings(sRunningBuild.getProjectId(), ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME)).thenReturn(projSettings);
		rocketNotificationListener.register();
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public void testSlackNotificationListener() {
		RocketNotificationListener whl = new RocketNotificationListener(sBuildServer, settings,configSettings, manager, factory);
	}

	@Test
	public void testRegister() {
		RocketNotificationListener whl = new RocketNotificationListener(sBuildServer, settings,configSettings, manager, factory);
		whl.register();
		verify(sBuildServer).addListener(whl);
	}

	private Element getFullConfigElement(){
        return getElement("src/test/resources/main-config-full.xml");
    }

    private Element getElement(String filePath){
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);
        try {
            Document doc = builder.build(filePath);
            return doc.getRootElement();
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

	@Test
	public void testBuildStartedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewSlackNotification("", "project1", "my-channel", "myteam", true, state, true, true, new HashSet<String>(), true, true, true, true);
		when(slacknotification.isEnabled()).thenReturn(state.allEnabled());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);

		rocketNotificationListener.buildStarted(sRunningBuild);
		verify(factory.getSlackNotification(), times(1)).post();
	}

	@Test
	public void testBuildFinishedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewSlackNotification("", "1234", "my-channel", "myteam", true, state , true, true, new HashSet<String>(), true, true, true, true);
		when(slacknotification.isEnabled()).thenReturn(state.allEnabled());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);

		rocketNotificationListener.buildFinished(sRunningBuild);
		verify(factory.getSlackNotification(), times(1)).post();
	}

	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterFailure() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BUILD_FIXED);
		state.enable(BuildStateEnum.BUILD_FINISHED);
		state.enable(BuildStateEnum.BUILD_SUCCESSFUL);
		projSettings.addNewSlackNotification("", "1234", "my-channel", "myteam", true, state, true, true, new HashSet<String>(), true, true, true, true);
		when(slacknotification.isEnabled()).thenReturn(state.enabled(BuildStateEnum.BUILD_FIXED));
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedFailedBuilds);

		rocketNotificationListener.buildFinished(sRunningBuild);
		verify(factory.getSlackNotification(), times(1)).post();
	}

	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterSuccess() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BUILD_FIXED);
		projSettings.addNewSlackNotification("", "1234", "my-channel", "myteam", true, state, true, true, new HashSet<String>(), true, true, true, true);
		when(slacknotification.isEnabled()).thenReturn(state.enabled(BuildStateEnum.BUILD_FIXED));
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);

		rocketNotificationListener.buildFinished(sRunningBuild);
		verify(factory.getSlackNotification(), times(0)).post();
	}

	@Test
	public void testBuildInterruptedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewSlackNotification("", "1234", "my-channel", "myteam", true, state, true, true, new HashSet<String>(), true, true, true, true);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);

		rocketNotificationListener.buildInterrupted(sRunningBuild);
		verify(factory.getSlackNotification(), times(1)).post();
	}

	@Test
	public void testBeforeBuildFinishSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BEFORE_BUILD_FINISHED);
		projSettings.addNewSlackNotification("", "1234", "my-channel", "myteam", true, state, true, true, new HashSet<String>(), true, true, true, true);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);

		rocketNotificationListener.beforeBuildFinish(sRunningBuild);
		verify(factory.getSlackNotification(), times(1)).post();
	}

	@Test
	public void testBuildChangedStatusSRunningBuildStatusStatus() throws FileNotFoundException, IOException {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		sBuildType.setProject(sProject);
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");

		when(settings.getSettings(sRunningBuild.getProjectId(), ROCKET_NOTIFICATIONS_SETTINGS_ATTRIBUTE_NAME)).thenReturn(projSettings);

		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		RocketNotificationListener whl = new RocketNotificationListener(sBuildServer, settings,configSettings, manager, factory);
		Status oldStatus = Status.NORMAL;
		Status newStatus = Status.FAILURE;
		whl.register();
		whl.buildChangedStatus(sRunningBuild, oldStatus, newStatus);
		verify(factory.getSlackNotification(), times(0)).post();
	}

//	@Test
//	public void testResponsibleChangedSBuildTypeResponsibilityInfoResponsibilityInfoBoolean() {
//
//	}

}

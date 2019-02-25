package rocketnotifications.teamcity.extension.bean;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import rocketnotifications.teamcity.TeamCityIdResolver;
import rocketnotifications.teamcity.settings.SlackNotificationConfig;
import rocketnotifications.teamcity.settings.RocketNotificationProjectSettings;

import java.util.ArrayList;
import java.util.List;

public class ProjectAndBuildSlacknotificationsBean {
	SProject project;
	RocketNotificationProjectSettings slackNotificationProjectSettings;
	List<SlackNotificationConfig> projectSlacknotifications;
	List<BuildSlacknotificationsBean> buildSlacknotifications;
	
	public static ProjectAndBuildSlacknotificationsBean newInstance (SProject project, RocketNotificationProjectSettings settings, SBuildType sBuild) {
		ProjectAndBuildSlacknotificationsBean bean = new ProjectAndBuildSlacknotificationsBean();
		bean.project = project;
		bean.slackNotificationProjectSettings = settings;
		
		bean.projectSlacknotifications = settings.getProjectSlackNotificationsAsList();
		bean.buildSlacknotifications = new ArrayList<BuildSlacknotificationsBean>();
		
		if (sBuild != null && sBuild.getProjectId().equals(project.getProjectId())){
			bean.buildSlacknotifications.add(new BuildSlacknotificationsBean(sBuild, settings.getBuildSlackNotificationsAsList(sBuild)));
		}
		return bean;
	}

	public int getProjectSlacknotificationCount(){
		return this.projectSlacknotifications.size();
	}

	public int getBuildSlacknotificationCount(){
		return this.buildSlacknotifications.size();
	}
	
	public SProject getProject() {
		return project;
	}

	public RocketNotificationProjectSettings getSlackNotificationProjectSettings() {
		return slackNotificationProjectSettings;
	}

	public List<SlackNotificationConfig> getProjectSlacknotifications() {
		return projectSlacknotifications;
	}

	public List<BuildSlacknotificationsBean> getBuildSlacknotifications() {
		return buildSlacknotifications;
	}
	
	public String getExternalProjectId(){
		return TeamCityIdResolver.getExternalProjectId(project);
	}

}

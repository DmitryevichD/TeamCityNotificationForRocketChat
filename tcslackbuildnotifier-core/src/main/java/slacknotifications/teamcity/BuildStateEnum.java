package slacknotifications.teamcity;

import lombok.Getter;

public enum BuildStateEnum {
    BUILD_STARTED ("buildStarted", "started"),
    BUILD_FINISHED ("buildFinished", "finished"),
    BEFORE_BUILD_FINISHED ("beforeBuildFinish", "nearly finished"),
	RESPONSIBILITY_CHANGED ("responsibilityChanged", "changed responsibility"),
	BUILD_INTERRUPTED ("buildInterrupted", "been interrupted"),
	BUILD_SUCCESSFUL ("buildSuccessful", "completed successfully"),
	BUILD_FAILED ("buildFailed", "failed"),
	BUILD_FIXED ("buildFixed", "been fixed"),
	BUILD_BROKEN ("buildBroken", "broken");

    @Getter
    private final String shortName;
    @Getter
    private final String descriptionSuffix;

    BuildStateEnum(String shortName, String descriptionSuffix){
    	this.shortName = shortName;
    	this.descriptionSuffix = descriptionSuffix;
    }

	public static BuildStateEnum findBuildState(String stateString){
		for (BuildStateEnum state : BuildStateEnum.values()) {
			if (state.shortName.equalsIgnoreCase(stateString)){
				return state;
			}
		}
		return null;
	}
}

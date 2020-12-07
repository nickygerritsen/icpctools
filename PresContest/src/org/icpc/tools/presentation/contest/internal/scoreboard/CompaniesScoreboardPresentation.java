package org.icpc.tools.presentation.contest.internal.scoreboard;

public class CompaniesScoreboardPresentation extends AbstractGroupScoreboardPreentation {
	@Override
	String getGroupIdToShow() {
		return "4";
	}

	@Override
	protected String getTitle() {
		return "Current Standings - Companies";
	}
}

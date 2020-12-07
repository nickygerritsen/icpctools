package org.icpc.tools.presentation.contest.internal.scoreboard;

import org.icpc.tools.contest.model.IContestListener;
import org.icpc.tools.contest.model.ITeam;
import org.icpc.tools.contest.model.internal.Contest;

import java.util.Arrays;

abstract public class AbstractGroupScoreboardPreentation extends ScoreboardPresentation {
	abstract String getGroupIdToShow();

	private Contest originalContest;
	private final Contest copy = new Contest();
	private final IContestListener listener = (contest, obj, d) -> {
		if (obj instanceof ITeam) {
			if (!Arrays.asList(((ITeam) obj).getGroupIds()).contains(getGroupIdToShow())) {
				return;
			}
		}
		copy.add(obj);
	};

	@Override
	public void init() {
		super.init();

		if (getContest() instanceof Contest) {
			originalContest = (Contest) getContest();
			originalContest.addListenerFromStart(listener);
			setContest(copy);
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		if (originalContest != null) {
			originalContest.removeListener(listener);
		}
	}
}

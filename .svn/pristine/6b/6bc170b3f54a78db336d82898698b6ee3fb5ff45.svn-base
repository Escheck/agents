package parties.in4010.q12015.group15;

import negotiator.Bid;
import negotiator.boaframework.Actions;
import negotiator.timeline.TimeLineInfo;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Accepts only bids with high utility early in negotiation but lowers the
 * standard with time. If it's the last round or very little time is left the
 * agent will accept any offer.
 *
 */
public class AcceptanceStrategy {
	private AdditiveUtilitySpace utilitySpace;

	public AcceptanceStrategy(AdditiveUtilitySpace utilitySpace) {
		this.utilitySpace = utilitySpace;
	}

	/**
	 * @param bid
	 *            from opponent
	 * @param time
	 *            of bid received
	 * @return action Accept if bid is good enough, Reject if it's not
	 */
	public Actions determineAcceptability(Bid bid, TimeLineInfo timeLine,
			double roundTime) {
		System.out.println("---- AS ----");

		// First check if time is running out
		Actions action;
		if (timeLine.getType().equals(Timeline.Type.Rounds)) {
			action = acceptIfLastRound(timeLine);
		} else {
			action = acceptIfLittleTimeIsLeft(timeLine, roundTime);
		}
		if (action != null) {
			return action;
		}

		// If we have enough time we evaluate the bid according to our strategy
		double timeLeft = 1 - timeLine.getTime();
		try {
			double utility = utilitySpace.getUtility(bid);
			System.out.println("AS received bid: " + bid);
			System.out.println("AS util: " + utility);
			System.out.println("AS time left: " + timeLeft);
			if ((timeLeft > 0.9 && timeLeft < 1.0 && utility > 0.95)
					|| (timeLeft > 0.7 && timeLeft < 0.9 && utility > 0.90)
					|| (timeLeft > 0.5 && timeLeft < 0.7 && utility > 0.85)
					|| (timeLeft > 0.3 && timeLeft < 0.5 && utility > 0.80)
					|| (timeLeft > 0.1 && timeLeft < 0.3 && utility > 0.75)
					|| (timeLeft < 0.1 && utility > 0.70)) {
				System.out.println("AS Good enough utility");
				System.out.println("AS action: " + Actions.Accept);
				return Actions.Accept;
			}
		} catch (Exception e) {
			System.out.println("AS Could not decide response to bid");
			e.printStackTrace();
		}

		System.out.println("AS action: " + Actions.Reject);
		return Actions.Reject;
	}

	/**
	 * @param timeLine
	 * @return Accept if it's the last round
	 */
	public Actions acceptIfLastRound(TimeLineInfo timeLine) {
		double totalRounds = timeLine.getTotalTime() - 1;
		double roundsLeft = totalRounds - timeLine.getCurrentTime();

		System.out.println("Total rounds: " + totalRounds);
		System.out.println("Current round: " + timeLine.getCurrentTime());
		System.out.println("Rounds left: " + roundsLeft);

		if (roundsLeft == 0) {
			System.out.println("AS Last round");
			System.out.println("AS action: " + Actions.Accept);
			return Actions.Accept;
		}

		return null;
	}

	/**
	 * @param timeLine
	 * @param roundTime
	 * @return Accept if there is very little time left. We evaluate
	 *         "little time" as 2 times the average time a round takes.
	 */
	public Actions acceptIfLittleTimeIsLeft(TimeLineInfo timeLine,
			double roundTime) {
		double totalSeconds = timeLine.getTotalTime();
		double secondsLeft = totalSeconds - timeLine.getCurrentTime();

		System.out.println("Total Seconds: " + totalSeconds);
		System.out.println("Current seconds passed: "
				+ timeLine.getCurrentTime());
		System.out.println("Seconds left: " + secondsLeft);

		double timeLimit = 2 * roundTime;
		System.out.println("timeLimit: " + timeLimit);
		if (secondsLeft <= timeLimit) {
			System.out.println("AS Last seconds");
			System.out.println("AS action: " + Actions.Accept);
			return Actions.Accept;
		}

		return null;
	}
}
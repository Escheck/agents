package parties.in4010.q12015.group17;

import negotiator.Bid;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

/**
 * This class is responsible for determining whether to accept a bid or not.
 * Though it is currently only a single method, for the sake of future expansion
 * as well as proper segregation it has been placed in its own class.
 */
public class AcceptStrategy {

	private final AbstractUtilitySpace utilitySpace;
	private final TimeLineInfo timeline;

	public AcceptStrategy(AbstractUtilitySpace us, TimeLineInfo t) {
		this.utilitySpace = us;
		this.timeline = t;
	}

	/**
	 * Determines if the given bid is acceptable according to the current time.
	 * 
	 * @param b
	 *            The {@link Bid} to evaluate.
	 * @return {@code TRUE} if this {@link Bid} should be accepted, otherwise
	 *         {@code FALSE}.
	 */
	public boolean determineAcceptability(Bid b) {
		try {
			double utility = utilitySpace.getUtility(b);
			double time = timeline.getTime();

			// First part of concession. A sinoid decay from 1 to 0.8 utility
			// over 0<t<0.8.
			if (time <= 0.8)
				return utility > 1 + (0.1 * ((Math.cos(time * Math.PI * 1.25) - 1)));
			// Fourth part. A linear decay from 0.6 to 0.55 over 0.985 < t < 1.
			else if (time > 0.985)
				return utility > 0.6 - ((time - 0.985) / 0.015) * 0.05;
			// Third part. A linear decay from 0.7 to 0.6 over 0.95<t<0.985.
			else if (time > 0.95)
				return utility > 0.7 - ((time - 0.95) / 0.035) * 0.1;
			// Second part. A linear decay from 0.8 to 0.7 over 0.8<t<0.95.
			else if (time > 0.8)
				return utility > 0.8 - ((time - 0.8) / 0.15) * 0.1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}

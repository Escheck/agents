package negotiator.boaframework.acceptanceconditions.anac2010;

import java.util.Map;

import negotiator.boaframework.AcceptanceStrategy;
import negotiator.boaframework.Actions;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.OpponentModel;

/**
 * This is the decoupled Acceptance Conditions for IAMcrazyHaggler (ANAC2010).
 * The code was taken from the ANAC2010 IAMHaggler and adapted to work within
 * the BOA framework.
 * 
 * Decoupling Negotiating Agents to Explore the Space of Negotiation Strategies
 * T. Baarslag, K. Hindriks, M. Hendrikx, A. Dirkzwager, C.M. Jonker
 * 
 * @author Alex Dirkzwager, Mark Hendrikx
 * @version 18/12/11
 */
public class AC_IAMcrazyHaggler extends AcceptanceStrategy {

	private double maximum_aspiration = 0.85;
	private final double acceptMultiplier = 1.02;

	/**
	 * Empty constructor for the BOA framework.
	 */
	public AC_IAMcrazyHaggler() {
	}

	public AC_IAMcrazyHaggler(NegotiationSession negoSession, OfferingStrategy strat) throws Exception {
		init(negoSession, strat, null, null);
	}

	@Override
	public void init(NegotiationSession negoSession, OfferingStrategy strat, OpponentModel opponentModel,
			Map<String, Double> parameters) throws Exception {
		this.negotiationSession = negoSession;
		offeringStrategy = strat;

		if (negotiationSession.getDiscountFactor() == 0) {
			maximum_aspiration = 0.9;
		}
	}

	@Override
	public Actions determineAcceptability() {
		if (negotiationSession.getOpponentBidHistory() != null
				&& negotiationSession.getOwnBidHistory().getLastBidDetails() != null) {
			double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory().getLastBidDetails()
					.getMyUndiscountedUtil();
			double lastMyBidUtil = negotiationSession.getOwnBidHistory().getLastBidDetails().getMyUndiscountedUtil();
			// accept if the offered utility => 0.98 * previous offer OR
			// accept if the offered utility => 0.98 * nextBid OR
			// accept if the offered utility => 0.98 * maxUtility
			if (lastOpponentBidUtil * acceptMultiplier > lastMyBidUtil
					|| lastOpponentBidUtil * acceptMultiplier > offeringStrategy.getNextBid().getMyUndiscountedUtil()
					|| lastOpponentBidUtil * acceptMultiplier > maximum_aspiration) {
				return Actions.Accept;
			}
		}
		return Actions.Reject;
	}

	@Override
	public String getName() {
		return "2010 - IAMcrazyHaggler";
	}
}

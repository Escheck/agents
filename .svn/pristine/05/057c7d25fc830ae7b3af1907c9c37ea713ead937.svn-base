package agents;

import negotiator.PocketNegotiatorAgent;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * @author W.Pasman PocketNegotiator compatible version of BayesianAgent.
 */
public class BayesianAgentPN extends BayesianAgent implements
		PocketNegotiatorAgent {

	@Override
	public void initPN(AdditiveUtilitySpace mySide,
			AdditiveUtilitySpace otherSide, Timeline tl) {
		utilitySpace = mySide;
		// FIXME set other side for use
		timeline = tl;
		init();

	}

	@Override
	public void updateProfiles(AdditiveUtilitySpace my,
			AdditiveUtilitySpace other) {
		if (my != null) {
			utilitySpace = my;
		}
	}

	@Override
	public void handleAction(Action act) {
		ReceiveMessage(act);
	}

	@Override
	public Action getAction() {
		return chooseAction();
	}

	@Override
	public String getLastBidExplanation() {
		// QUICK HACK. Simplistic explanation.
		if (myLastAction == null) {
			return null;
		}
		if (myLastAction instanceof Accept) {
			return "The last opponent's offer was better than our intended bid.";
		} else if (myLastAction instanceof Offer) {
			if (fOpponentPreviousBid == null) {
				return "First bid is bid with maximum utility";
			}
			return "An estimation was made of the opponent preferences, based on his actual bids. "
					+ "A concession was made, matching the opponents estimated concession.";
		}
		return "It is not clear what happened.";
	}

}

package agents;

import negotiator.PocketNegotiatorAgent;
import negotiator.actions.Action;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * @author W.Pasman simple agent to test PN interface.
 */
public class SimpleAgentPN extends SimpleAgent implements PocketNegotiatorAgent {

	@Override
	public void initPN(AdditiveUtilitySpace mySide,
			AdditiveUtilitySpace otherSide, Timeline tl) {
		utilitySpace = mySide;
		timeline = tl;

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
		return "Simple agent does random bids.";
	}

}

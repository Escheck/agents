package agents;

import negotiator.PocketNegotiatorAgent;
import negotiator.SupportedNegotiationSetting;
import negotiator.actions.Action;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;

public class TimeDependentAgentConcederPN extends TimeDependentAgent implements
		PocketNegotiatorAgent {
	@Override
	public double getE() {
		return 2;
	}

	@Override
	public String getName() {
		return "Conceder";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	/******* implements PocketNegotiatorAgent ***********/

	@Override
	public void initPN(AdditiveUtilitySpace mySide,
			AdditiveUtilitySpace otherSide, Timeline tl) {
		utilitySpace = mySide;
		timeline = tl;
		init();
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
	public void updateProfiles(AdditiveUtilitySpace my,
			AdditiveUtilitySpace other) {
		if (my != null) {
			utilitySpace = my;
			initFields();
		}
	}

	@Override
	public String getLastBidExplanation() {
		if (myHistory.size() == 0) {
			return null;
		}
		// FIXME we can't even see here if last action was an accept.
		return "Conceder sets a minimum utility depending on time. Accept is done when opponent's bid is good enough,"
				+ " otherwise a bid near the minimum utility is proposed ";
	}

}

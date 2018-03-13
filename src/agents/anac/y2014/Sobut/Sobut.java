package agents.anac.y2014.Sobut;

import java.io.Serializable;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.NegotiationResult;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;

public class Sobut extends Agent {
	private double MINIMUM_BID_UTILITY;
	private Bid opponentLastBid;
	private Bid maxBid;

	public Sobut() {
	}

	public void init() {
		Serializable prev = this.loadSessionData();
		if (prev != null) {
			double previousOutcome = (Double) prev;
			double r = Math.min(Math.random(), 0.5);
			MINIMUM_BID_UTILITY = Math.max(Math.max(utilitySpace.getReservationValueUndiscounted(), previousOutcome),
					r);
		} else {
			MINIMUM_BID_UTILITY = utilitySpace.getReservationValueUndiscounted();
		}
		System.out.println("Minimum bid utility: " + MINIMUM_BID_UTILITY);
	}

	public String getVersion() {
		return "1.0";
	}

	public String getName() {
		return "ANAC2014Agent";
	}

	public void endSession(NegotiationResult result) {
		if (result.getMyDiscountedUtility() > MINIMUM_BID_UTILITY) {
			saveSessionData(new Double(result.getMyDiscountedUtility()));
		}
		System.out.println(result);
	}

	public void ReceiveMessage(Action opponentAction) {
		opponentLastBid = DefaultAction.getBidFromAction(opponentAction);
	}

	public Action chooseAction() {
		if (opponentLastBid != null && getUtility(opponentLastBid) >= MINIMUM_BID_UTILITY) {
			return new Accept(getAgentID(), opponentLastBid);
		}
		return getRandomBid(MINIMUM_BID_UTILITY);
	}

	private Action getRandomBid(double target) {
		Bid bid = null;
		try {
			int loops = 0;
			do {
				bid = utilitySpace.getDomain().getRandomBid(null);
				loops++;
			} while (loops < 100000 && utilitySpace.getUtility(bid) < target);
			if (bid == null) {
				if (maxBid == null) {
					// this is a computationally expensive operation, therefore
					// cache result
					maxBid = utilitySpace.getMaxUtilityBid();
				}
				bid = maxBid;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Offer(getAgentID(), bid);
	}

	@Override
	public String getDescription() {
		return "ANAC 2014 - Sobut (compatible with non-linear utility spaces)";
	}
}
package parties.in4010.q12015.group4;

import java.io.Serializable;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.boaframework.NegotiationSession;

/*
 * This is an agent only for two parties negotiation
 */

public class Group4AgentForTwoPartiesNegotiation extends Agent {

	/*
	 * opponentAction ------ the most recent Action opponents performed.
	 */
	private Action opponentAction;
	/*
	 * MINIMUM_BID_UTILITY ------ the minimum acceptable utility of a bid,
	 * decrease over time
	 */
	private double MINIMUM_BID_UTILITY;
	/*
	 * MAXIMUM_BID_UTILITY_OPPONENT_GIVEN ------ the maximum utility of bid that
	 * opponent offered MINIMUM_BID_UTILITY should not be lower than this value,
	 * since opponent is willing to offer a bid with this utility
	 */
	private double MAXIMUM_BID_UTILITY_OPPONENT_GIVEN;

	NegotiationSession negotiationSession;

	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.Agent#getName() return tha name of this agent
	 */
	public String getName() {
		return "Group4PartyAgent";
	}

	public Group4AgentForTwoPartiesNegotiation() {
		MINIMUM_BID_UTILITY = 0.9;
		MAXIMUM_BID_UTILITY_OPPONENT_GIVEN = 0.0;
	}

	/*
	 * Initiate the agent
	 */

	public void inti() {
		// System.out.println("Starting Initialization");
		if (utilitySpace.getReservationValueUndiscounted() != 0) {
			Serializable prev = this.loadSessionData();
			if (prev != null) {
				double previousOutcome = (Double) prev;
				MINIMUM_BID_UTILITY = Math.max(Math.max(
						utilitySpace.getReservationValueUndiscounted(),
						previousOutcome), 0.9);
				// System.out.println(utilitySpace.getReservationValueUndiscounted()
				// + "----" + previousOutcome);
			} else {
				MINIMUM_BID_UTILITY = utilitySpace
						.getReservationValueUndiscounted();
			}
		} else {
			MINIMUM_BID_UTILITY = 0.9;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.Agent#ReceiveMessage(negotiator.actions.Action) receive
	 * opponents' last action
	 */
	public void ReceiveMessage(Action opponentAction) {
		this.opponentAction = opponentAction;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.parties.NegotiationParty#chooseAction(java.util.List)
	 * When the validActions list doesn't contain "Accept", which mean there is
	 * no bid currently, offer a bid that is higher than the MINIMUN_BID_UTILITY
	 * When the validActions list contains "Accept" then accept this bid if it
	 * was acceptable, otherwise offer a new bid;
	 */
	public Action chooseAction() {
		// TODO Auto-generated method stub
		Bid opponentBid = DefaultAction.getBidFromAction(opponentAction);
		if (opponentAction != null
				&& getUtility(opponentBid) > MINIMUM_BID_UTILITY) {
			return new Accept(getAgentID(), opponentBid);
		} else {
			System.out.println("Current time:" + timeline.getCurrentTime()
					+ "  Reservation value:"
					+ utilitySpace.getReservationValue());
			if (getUtility(opponentBid) > MAXIMUM_BID_UTILITY_OPPONENT_GIVEN) {
				MAXIMUM_BID_UTILITY_OPPONENT_GIVEN = getUtility(opponentBid);
			}
			MINIMUM_BID_UTILITY = Math.max((1 - timeline.getCurrentTime()
					/ timeline.getTotalTime()),
					MAXIMUM_BID_UTILITY_OPPONENT_GIVEN);
			return OfferBid();
		}
	}

	/*
	 * Use Random Walker to choose a bid the utility of the chosen bid should be
	 * higher than the MINIMUM_BID_UTILITY (agent should offer a bid that is
	 * acceptable to itself)
	 */
	public Action OfferBid() {
		System.out.println("Minmum Utility = " + MINIMUM_BID_UTILITY);
		Bid bid = utilitySpace.getDomain().getRandomBid(null);
		int loop = 0;
		while (loop <= 10000 && getUtility(bid) < MINIMUM_BID_UTILITY) {
			bid = utilitySpace.getDomain().getRandomBid(null);
			loop++;
		}
		if (loop < 10000) {
			System.out.println(getName()
					+ ":Found bid in 10000 loops, Utility =" + getUtility(bid));
			return new Offer(getAgentID(), bid);
		} else {
			try {
				System.out
						.println(getName()
								+ ": Return maxUtilityBid, Utility ="
								+ getUtility(bid));
				return new Offer(getAgentID(), utilitySpace.getMaxUtilityBid());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}
}

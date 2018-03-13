package parties.in4010.q12015.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;

/**
 * This is an agent for multi-party negotiation
 */

public class Group4 extends AbstractNegotiationParty {

	/**
	 * opponentAction ------ the most recent Action opponents performed.
	 */
	private Action opponentAction;
	/**
	 * MINIMUM_BID_UTILITY ------ the minimum acceptable utility of a bid
	 */
	private double MINIMUM_BID_UTILITY;
	/**
	 * MAXIMUM_BID_UTILITY ------ the maximum acceptable utility of a bid,
	 */
	private double MAXIMUM_BID_UTILITY;
	/**
	 * BEST_BID_OPPONENT_OFFER ------ the bid opponents has offered that has the
	 * highest utility for this agent
	 */
	private Bid BEST_BID_OPPONENT_OFFER;
	/**
	 * BEST_BID_OPPONENT_OFFER_UTILITY; ------ the Utility of
	 * BEST_BID_OPPONENT_OFFER
	 */
	private double BEST_BID_OPPONENT_OFFER_UTILITY;
	/**
	 * MY_LAST_BID ------ the last bid this agent offered
	 */
	private Bid MY_LAST_BID;
	/**
	 * historicalBid ------ a list of historical bid this agent offered
	 */
	private ArrayList<Bid> historicalBid;
	/**
	 * AgentToHistoricalBidMap ------ map which use agentID as the key to store
	 * the historical bids offered by different opponent agents
	 */
	private Map<AgentID, ArrayList<Bid>> AgentToHistoricalBidMap;
	/**
	 * opponentModelMap ------ map which use agentID as the key to store the
	 * opponentModels of different opponent agents
	 */
	private Map<AgentID, Group4OpponentModel> opponentModelMap;

	public Group4() {
		init();
	}

	/**
	 * initiate the agent When the negotiation start, assume an agent want to
	 * reach its highest utility So set the MINIMUM_BID_UTILITY to 0.9
	 */
	public void init() {
		MINIMUM_BID_UTILITY = 0.9;
		MAXIMUM_BID_UTILITY = 1;
		BEST_BID_OPPONENT_OFFER = null;
		MY_LAST_BID = null;
		BEST_BID_OPPONENT_OFFER_UTILITY = 0;
		historicalBid = new ArrayList<Bid>();
		AgentToHistoricalBidMap = new HashMap<>();
		opponentModelMap = new HashMap<>();
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.parties.AbstractNegotiationParty#getDescription() Return
	 * the description of the agent
	 */
	public String getDescription() {
		return "Group4MultiPartyNegotiationAgent";
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see negotiator.parties.NegotiationParty#chooseAction(java.util.List)
	 * When the validActions list doesn't contain "Accept", which means there is no bid currently,
	 * offer a bid that is higher than the MINIMUN_BID_UTILITY
	 * When the validActions list contains "Accept" then 
	 * accept this bid if it was acceptable, otherwise offer a new bid; 
	 */
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// this.showUtilitySpaceDetails();
		if (!validActions.contains(Accept.class)) {
			return offerBid();
		} else {
			if (isAcceptable(opponentAction))
				return new Accept(getPartyId(),
						((ActionWithBid) opponentAction).getBid());
			else
				return offerBid();
		}
	}

	/**
	 * Determine whether a bid is acceptable 1. return true when the utility is
	 * higher or equal to than the MINIMUM_BID_UTILITY 2. return false When the
	 * utility is lower than the MINIMUM_BID_UTILITY
	 * 
	 * @param Action
	 * @return true - bid is acceptable false - bid is not acceptable
	 * 
	 */
	public boolean isAcceptable(Action action) {
		Bid opponentBid = DefaultAction.getBidFromAction(opponentAction);
		if (opponentAction != null
				&& getUtility(opponentBid) >= MINIMUM_BID_UTILITY) {
			return true;
		} else {
			System.out.println("Current time:" + timeline.getCurrentTime()
					+ "  Reservation value:"
					+ utilitySpace.getReservationValue());
			/*
			 * The MINIMUM_BID_UTILITY decrease over time. Set
			 * MINIMUM_BID_UTILITY to MAX(1 - time
			 * spent,BEST_BID_OPPONENT_OFFER_UTILITY)
			 */
			setLowerLimit();
			setUpperLimit();
			return false;
		}
	}

	@Override
	/**
	 * (non-Javadoc)
	 * @see negotiator.parties.AbstractNegotiationParty#receiveMessage(java.lang.Object, negotiator.actions.Action)
	 * receive opponents' last action
	 */
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		this.opponentAction = action;
		if (opponentAction.getAgent() != null
				&& !opponentAction.toString().equals("(Accept)")) {
			System.out.println(opponentAction.getAgent()
					+ opponentAction.toString());
			saveBestBid(opponentAction);
			AgentID agentID = opponentAction.getAgent();
			// this.showBidDetails(opponentBid);
			saveOpponentHistoricalBid(opponentAction);
			System.out.println(agentID);
			opponentModelMap.get(agentID).UpdateOpponentModel(
					AgentToHistoricalBidMap.get(agentID));
		}
	}

	/**
	 * Bidding strategy is based on random walk strategy the utility of the
	 * chosen bid should be higher than the MINIMUM_BID_UTILITY and lower than
	 * MAXIMUM_BID_UTILITY (agent should offer a bid that is acceptable to
	 * itself and is easier to accept by opponents) the chosen bid should be a
	 * better bid to all opponent compared to their last bid try to find a bid
	 * that meet the requirements above in 1000 loops return the last offered
	 * bid failed to find a bid in 1000 loops
	 * 
	 * @return a bid that meet the requirements above;
	 * 
	 */
	public Action offerBid() {
		// System.out.println("Minimum Utility = " + MINIMUM_BID_UTILITY);
		// System.out.println("Maximum Utility = " + MAXIMUM_BID_UTILITY);
		Bid bid = null;
		int loop = 0;
		while (loop <= 1000) {
			bid = utilitySpace.getDomain().getRandomBid(null);
			if (getUtility(bid) >= MINIMUM_BID_UTILITY
					&& getUtility(bid) <= MAXIMUM_BID_UTILITY) {
				if (opponentModelMap.isEmpty()) {
					break;
				}
				boolean worseBid = false;
				for (AgentID id : opponentModelMap.keySet()) {
					Group4OpponentModel om = opponentModelMap.get(id);
					ArrayList<Bid> opponentBids = AgentToHistoricalBidMap
							.get(id);
					if (opponentBids.size() > 0) {
						if (om.evaluateBid(bid) < om.evaluateBid(opponentBids
								.get(opponentBids.size() - 1))) {
							worseBid = true;
							break;
						}
					}
				}
				if (!worseBid) {
					break;
				}
			}
			loop++;
		}
		if (loop < 1000) {
			// System.out.println(this.getDescription() +
			// ":Found bid in 1000 loops, Utility =" + getUtility(bid));
			for (Group4OpponentModel om : opponentModelMap.values()) {
				System.out.println(om.evaluateBid(bid));
			}
			MY_LAST_BID = bid;
			historicalBid.add(bid);
			return new Offer(getPartyId(), bid);
		} else {
			try {
				// System.out.println(this.getDescription() +
				// ": Return maxUtilityBid, Utility =" + getUtility(bid));
				MY_LAST_BID = bid;
				historicalBid.add(bid);
				if (MY_LAST_BID == null) {
					return new Offer(getPartyId(),
							utilitySpace.getMaxUtilityBid());
				} else {
					return new Offer(getPartyId(), MY_LAST_BID);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Save bids offered by opponents
	 * 
	 * @param Action
	 */
	public void saveOpponentHistoricalBid(Action action) {
		Bid bid = DefaultAction.getBidFromAction(action);
		AgentID agentID = action.getAgent();
		ArrayList<Bid> opponentHistoricalBid;
		if (!AgentToHistoricalBidMap.containsKey(agentID)) {
			opponentHistoricalBid = new ArrayList<Bid>();
			opponentModelMap.put(agentID,
					new Group4OpponentModel(utilitySpace.getDomain()));
		} else
			opponentHistoricalBid = AgentToHistoricalBidMap.get(agentID);
		opponentHistoricalBid.add(bid);
		AgentToHistoricalBidMap.put(agentID, opponentHistoricalBid);
	}

	/**
	 * save the best bid offered by opponents
	 * 
	 * @param Action
	 */
	public void saveBestBid(Action Action) {
		if (getUtility(DefaultAction.getBidFromAction(Action)) < MAXIMUM_BID_UTILITY) {
			if (BEST_BID_OPPONENT_OFFER == null) {
				BEST_BID_OPPONENT_OFFER = DefaultAction
						.getBidFromAction(Action);
				BEST_BID_OPPONENT_OFFER_UTILITY = getUtility(BEST_BID_OPPONENT_OFFER);
			} else {
				if (getUtility(BEST_BID_OPPONENT_OFFER) < getUtility(DefaultAction
						.getBidFromAction(Action))) {
					BEST_BID_OPPONENT_OFFER = DefaultAction
							.getBidFromAction(Action);
					BEST_BID_OPPONENT_OFFER_UTILITY = getUtility(BEST_BID_OPPONENT_OFFER);
				}
			}
		}
	}

	/**
	 * count the number that how many time this bid was refused by opponent
	 * 
	 * @param bid
	 * @return refused times
	 */
	public int Refuse_Count(Bid bid) {
		int count = 0;
		for (Bid hisBid : historicalBid) {
			if (hisBid.equals(bid)) {
				count++;
			}
		}
		System.out.println("count =" + count);
		return count;
	}

	/**
	 * set the upper-limit upper-limit = MINIMUM_BID_UTILITY + (0.9 -
	 * MINIMUM_BID_UTILITY) * (0.5 / Refuse Count of MY_LAST_BID) + 0.1 *
	 * (CurrentTime/TotalTime)
	 */
	public void setUpperLimit() {
		if (MY_LAST_BID != null) {
			System.out.println(getUtility(MY_LAST_BID));
			MAXIMUM_BID_UTILITY = MINIMUM_BID_UTILITY
					+ (0.9 - MINIMUM_BID_UTILITY)
					* (0.5 / Refuse_Count(MY_LAST_BID)) + 0.1
					* (timeline.getCurrentTime() / timeline.getTotalTime());
		}
	}

	/**
	 * set the lower-limit lower-limit = Max((0.9 - CurrentTime/TotalTime),
	 * BEST_BID_OPPONENT_OFFER_UTILITY - 0.015 * CurrentTime/TotalTime)
	 */
	public void setLowerLimit() {
		MINIMUM_BID_UTILITY = Math
				.max((0.9 - timeline.getCurrentTime() / timeline.getTotalTime()),
						BEST_BID_OPPONENT_OFFER_UTILITY
								- 0.015
								* (timeline.getCurrentTime() / timeline
										.getTotalTime()));
	}

	/**
	 * show all values for all issues in current utility space (not used)
	 */
	public void showUtilitySpaceDetails() {
		for (Issue issue : utilitySpace.getDomain().getIssues()) {
			IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
			for (ValueDiscrete value : issueDiscrete.getValues()) {
				System.out.println("****" + value.getValue());
			}
		}
	}

	/**
	 * show bid details (not used)
	 */
	public void showBidDetails(Bid bid) {
		try {
			for (Issue issue : bid.getIssues()) {
				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				System.out.println(bid.getValue(issueDiscrete.getNumber()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

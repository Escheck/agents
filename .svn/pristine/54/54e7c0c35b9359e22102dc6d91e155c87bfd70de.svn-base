package parties.in4010.q12015.group8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.ISSUETYPE;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * TODO: 1. Support multiple issue types? 2. It could be possible that
 * calculating bids will take forever if the domain is too broad (multiple
 * issues with a lot of values) we should be protected against such cases.
 */

public class Group8 extends AbstractNegotiationParty {
	private static final double MINIMUM_BID_UTILITY = 0.81; // minimum utility
	private static final double GIVE_IN_THRESHOLD = 0.9; // time/total or
															// round/total
															// threshold
	private static double BID_UTILITY = 0.88; // start threshold
	private static final double FILTER_THRESHOLD = 5; // start opponent modeling
														// after 5 rounds
	private static final double FILTER_STOP = 14; // stop filtering after some
													// rounds

	private ArrayList<Bid> enemyBids = new ArrayList<Bid>();
	private ArrayList<Bid> history = new ArrayList<Bid>();

	private ArrayList<Bid> agentBids = null;
	private int nextBid = -1;

	private Map<AgentID, OpponentModel> opponentModels = new HashMap<AgentID, OpponentModel>();

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		// utilSpace.getReservationValueUndiscounted();
		// TODO: getReservationValueUndiscounted returns the least favourable
		// point at which one will accept, so not really needed?
	}

	private void initBids() throws Exception {
		agentBids = new ArrayList<Bid>();
		for (HashMap<Integer, Value> bid : getAllBids()) {
			Bid b = new Bid(utilitySpace.getDomain(), bid);
			if (canAccept(b)) {
				agentBids.add(b);
			}
		}
	}

	/**
	 * When this function is called, it is expected that the Party chooses one
	 * of the actions from the possible action list and returns an instance of
	 * the chosen action.
	 * 
	 * @param possibleActions
	 * @return accept or offer
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		// Threshold value will become slowly lower
		if (getTimeLeft() >= GIVE_IN_THRESHOLD) {
			// linear slope downwards starting at GIVE_IN_THRESHOLD time
			BID_UTILITY = Math.max(MINIMUM_BID_UTILITY, BID_UTILITY
					- (1.0 - MINIMUM_BID_UTILITY) / (1.0 - GIVE_IN_THRESHOLD) * (getTimeLeft() - GIVE_IN_THRESHOLD));

			try {
				initBids(); // re-init our bids
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			// accept if last offer is good enough and we have played a couple
			// of rounds
			if (possibleActions.contains(Accept.class) && canAccept(enemyBids.get(enemyBids.size() - 1))
					&& enemyBids.size() > MINIMUM_BID_UTILITY) {
				return new Accept(getPartyId(), enemyBids.get(enemyBids.size() - 1));
			} else {
				// do a new bid
				Offer offer = generateBid();
				history.add(offer.getBid());
				return offer;
			}
		} catch (Exception e) {
			System.err.println("Exception in chooseAction: " + e.getMessage());
			// prevent returning nothing
			return new Accept(getPartyId(), enemyBids.get(enemyBids.size() - 1));
		}
	}

	/**
	 * Checks if the utility is high enough.
	 * 
	 * @param b
	 * @return
	 */
	public boolean canAccept(Bid b) {
		return getUtility(b) > BID_UTILITY;
	}

	@Override
	public String getDescription() {
		return "Group 8 Agent";
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		if (action instanceof Offer) {
			Offer offer = (Offer) action;
			Bid curBid = offer.getBid();
			enemyBids.add(curBid); // remember all the bids

			if (opponentModels.get(action.getAgent()) == null) {
				opponentModels.put(action.getAgent(), new OpponentModel((AdditiveUtilitySpace) utilitySpace));
			}
			opponentModels.get(offer.getAgent()).updateOpponentModel(curBid);

			// filter dangerous bids
			if (enemyBids.size() < FILTER_STOP)
				filterBids();
		}
	}

	private Offer generateBid() throws Exception {
		if (agentBids == null)
			initBids();

		nextBid = nextBid == agentBids.size() - 1 ? 0 : ++nextBid;
		return new Offer(getPartyId(), agentBids.get(nextBid));
	}

	/**
	 * Returns a list of possible bids, a bid is a mapping of an integer to a
	 * value.
	 * 
	 * @throws Exception
	 */
	private List<HashMap<Integer, Value>> getAllBids() throws Exception {
		List<Issue> issues = utilitySpace.getDomain().getIssues();
		return getAllBidsForIssue(issues, 0); // recursively generate bids by
												// traversing the tree
	}

	private List<HashMap<Integer, Value>> getAllBidsForIssue(List<Issue> issues, int i) throws Exception {

		if (issues.get(i).getType() != ISSUETYPE.DISCRETE) {
			return null;
		}

		List<HashMap<Integer, Value>> bids;
		if (i == issues.size() - 1) {
			bids = new ArrayList<HashMap<Integer, Value>>();
			bids.add(new HashMap<Integer, Value>());
		} else {
			bids = getAllBidsForIssue(issues, i + 1); // we get all the bids for
		}

		List<HashMap<Integer, Value>> newBids = new ArrayList<HashMap<Integer, Value>>();

		IssueDiscrete issue = (IssueDiscrete) issues.get(i);
		for (ValueDiscrete v : issue.getValues()) {
			for (HashMap<Integer, Value> bid : bids) {
				HashMap<Integer, Value> newBid = new HashMap<Integer, Value>(bid);
				newBid.put(issue.getNumber(), v);
				newBids.add(newBid); // we add a bid for each value of this
										// issue (rest of the bid is recursively
										// calculated)
			}
		}

		return newBids;
	}

	/**
	 * @return return the number of round/time left with respect to the total.
	 */
	private double getTimeLeft() {
		return getTimeLine().getTime();
	}

	/**
	 * Filter all the bids that are "dangerous" unless the bids are really that
	 * good for us.
	 */
	private void filterBids() {

		if (history.size() >= FILTER_THRESHOLD) {
			for (Map.Entry<AgentID, OpponentModel> om : opponentModels.entrySet()) {
				if (om.getValue().isAccurate()) {
					for (Bid b : agentBids) {
						if (om.getValue().isDangerousBid(b)) {
							System.out.println("removed bid " + b);
							agentBids.remove(b);
						}
					}
				} else {
					// System.out.println("Agent " + om.getKey() +
					// " is not accurate enough to calculate dangerous bids");
				}
			}
		}

	}

}

package agents.ai2014.group6;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.timeline.DiscreteTimeline;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

/**
 * This is your negotiation party. Theis agent is juggling weights so only can
 * be used with {@link AdditiveUtilitySpace}.
 */
public class Group6 extends AbstractNegotiationParty {

	private HashMap<Object, Action> history;
	private Map<Object, IOpponentModel> opponentModels;

	// The most recent bid that has been done (either by us or by another agent)
	private Bid mostRecentBid;
	private Bid ourPreviousBid;

	private double reservationValue;

	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace
	 *            Your utility space.
	 * @param deadlines
	 *            The deadlines set for this negotiation.
	 * @param timeline
	 *            Value counting from 0 (start) to 1 (end).
	 * @param randomSeed
	 *            If you use any randomization, use this seed for it.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		reservationValue = utilitySpace.getReservationValue();
		history = new HashMap<Object, Action>();
		opponentModels = new HashMap<Object, IOpponentModel>();
		ourPreviousBid = getMaximumUtilityBid();
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		try {
			// If there is a most recent bid and the agent is allowed to accept
			if (mostRecentBid != null && validActions.contains(Accept.class) && isAcceptable(mostRecentBid)) {
				return new Accept(getPartyId(), mostRecentBid);
			}

			// If there are no previous bids or the agent is not allowed to
			// accept, generate an offer
			mostRecentBid = generateBid();
		} catch (Exception e) {
			e.printStackTrace();

			// If something goes wrong, just accept the offer or create one at
			// random
			if (validActions.contains(Accept.class))
				return new Accept(getPartyId(), mostRecentBid);
			else
				mostRecentBid = generateRandomBid();
		}

		ourPreviousBid = mostRecentBid;
		return new Offer(getPartyId(), mostRecentBid);
	}

	/**
	 * Checks the acceptability of the Bid. Only works as planned if at least
	 * one bidding round has passed.
	 * 
	 * @param bid
	 *            to check
	 * @throws Exception
	 */
	private boolean isAcceptable(Bid bid) throws Exception {
		double currentTime = timeline.getTime();
		double bidUtilityValue = this.utilitySpace.getUtility(bid);

		double minimalAcceptableUtility = calculateMinimalAcceptableUtility(currentTime, this.reservationValue);

		TimeLineInfo localTime = this.timeline;
		switch (localTime.getType()) {
		case Rounds:
			if (((DiscreteTimeline) localTime).getOwnRoundsLeft() == 0)
				return true;
			break;
		default:
			break;
		}

		if (bidUtilityValue >= minimalAcceptableUtility)
			return true;

		return false;
	}

	/**
	 * Function: max(u_r, 0.5)^(t^3)
	 * 
	 * @param time
	 *            (t)
	 * @param reservationValue
	 *            (u_r)
	 * @param nrAccepts
	 *            (n_a)
	 * @param nrOpponents
	 *            (n)
	 * @return minimal acceptable utility value given it's parameters
	 * @throws Exception
	 *             outside boundaries
	 */
	private double calculateMinimalAcceptableUtility(double time, double reservationValue) throws Exception {
		if (time < 0 || time > 1)
			throw new Exception("time " + time + " outside [0,1]");
		if (reservationValue < 0 || reservationValue > 1)
			throw new Exception("reservationValue " + time + " outside [0,1]");

		return Math.pow(Math.max(reservationValue, 0.1), Math.pow(time, 3));
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		history.put(sender, action);

		// If a bid has been done, get the bid
		Bid bid = DefaultAction.getBidFromAction(action);

		if (bid != null) {
			mostRecentBid = bid;

			if (!opponentModels.containsKey(sender))
				opponentModels.put(sender, new FrequencyAnalysisOpponentModel());

			opponentModels.get(sender).learnWeights(mostRecentBid);
		}
	}

	private Queue<Issue> getOrderedIssues() {
		final AdditiveUtilitySpace utilspace1 = (AdditiveUtilitySpace) utilitySpace;
		PriorityQueue<Issue> issues = new PriorityQueue<Issue>(this.utilitySpace.getDomain().getIssues().size(),
				new Comparator<Issue>() {
					@Override
					public int compare(Issue i1, Issue i2) {
						return (int) (utilspace1.getWeight(i1.getNumber()) - utilspace1.getWeight(i2.getNumber()));
					}
				});
		issues.addAll(this.utilitySpace.getDomain().getIssues());
		return issues;
	}

	private Map<Issue, List<Value>> getOpponentMajorityValues() {
		Map<Issue, List<Value>> majorityValues = new HashMap<Issue, List<Value>>();

		for (Issue issue : this.utilitySpace.getDomain().getIssues()) {
			// Map each value to the number of agents that agree on it
			Map<Value, Integer> opponentValues = new HashMap<Value, Integer>();

			for (IOpponentModel oppModel : opponentModels.values()) {
				int n = 0;

				// If there is no entry in the opponent model for this issue,
				// continue
				if (oppModel.getValue(issue.getNumber()) == null)
					continue;

				if (opponentValues.get(oppModel.getValue(issue.getNumber())) != null)
					n = opponentValues.get(oppModel.getValue(issue.getNumber()));

				opponentValues.put(oppModel.getValue(issue.getNumber()), n + 1);
			}

			List<Value> maxValues = new ArrayList<Value>();
			int largestMajority = 0;

			for (Entry<Value, Integer> entry : opponentValues.entrySet()) {
				if (entry.getValue() > largestMajority) {
					maxValues.removeAll(maxValues);
					maxValues.add(entry.getKey());
				} else if (entry.getValue() == largestMajority) {
					maxValues.add(entry.getKey());
				}
			}

			majorityValues.put(issue, maxValues);
		}

		return majorityValues;
	}

	private Bid generateBid() throws Exception {
		// For the other issues, start with own maximum possible bid
		Bid maxBid = getMaximumUtilityBid();
		Bid resBid = maxBid;

		// Sort all issues in from low to high weight
		Queue<Issue> issues = getOrderedIssues();
		Map<Issue, List<Value>> majorityValues = getOpponentMajorityValues();

		// Remove all issues for which the majority agrees with us
		List<Issue> toBeRemoved = new ArrayList<Issue>();
		for (Issue issue : issues) {
			if (majorityValues.get(issue).contains(maxBid.getValue(issue.getNumber()))) {
				toBeRemoved.add(issue);
			}
		}
		issues.removeAll(toBeRemoved);

		// Do a concession on the least important issue(s)
		AdditiveUtilitySpace utilitySpace1 = (AdditiveUtilitySpace) utilitySpace;
		for (Issue leastImportantIssue : issues) {
			List<Value> majorityValue = majorityValues.get(leastImportantIssue);

			double maxUtility = 0;
			Value majorityMaxValue = null;

			for (Value value : majorityValue) {
				double valueUtility = ((EvaluatorDiscrete) utilitySpace1.getEvaluator(leastImportantIssue.getNumber()))
						.getEvaluation((ValueDiscrete) value);

				if (valueUtility > maxUtility) {
					maxUtility = valueUtility;
					majorityMaxValue = value;
				}
			}

			if (majorityMaxValue == null)
				break;

			// Only concede on issues that we did not concede on in the previous
			// bid
			if (ourPreviousBid.getValue(leastImportantIssue.getNumber()).equals(majorityMaxValue))
				continue;

			resBid = resBid.putValue(leastImportantIssue.getNumber(), majorityMaxValue);

			if (!isAcceptable(resBid)) {
				resBid = resBid.putValue(leastImportantIssue.getNumber(),
						maxBid.getValue(leastImportantIssue.getNumber()));
				break;
			}
		}

		return resBid;
	}

	private Bid getMaximumUtilityBid() {
		Bid resBid = generateRandomBid();
		AdditiveUtilitySpace utilitySpace1 = (AdditiveUtilitySpace) utilitySpace;

		for (Issue issue : utilitySpace.getDomain().getIssues()) {
			Value value = ((EvaluatorDiscrete) utilitySpace1.getEvaluator(issue.getNumber())).getMaxValue();
			if (value == null)
				break;
			resBid = resBid.putValue(issue.getNumber(), value);
		}

		return resBid;
	}

	protected AgentID partyId = new AgentID("Group 6");

	@Override
	public String getDescription() {
		return "ai2014 group6";
	}

}
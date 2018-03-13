package parties.in4010.q12015.group12;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

public class Group12 extends AbstractNegotiationParty {

	boolean reset = false;
	double totalTime;
	double threshold;
	int amountIssues;
	BidHistory history;
	List<BidDetails> listBids;
	HashMap<Object, OtherAgent> otherAgents;
	SortedOutcomeSpace possibleBids;

	/**
	 * init is called when a next session starts with the same opponent.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		resetValues();
	}

	/**
	 * Reset all values the agent used
	 */
	private void resetValues() {
		reset = true;
		totalTime = this.getTimeLine().getTotalTime();
		threshold = 1;
		history = new BidHistory();
		otherAgents = new HashMap<Object, OtherAgent>();
		possibleBids = new SortedOutcomeSpace(utilitySpace);
		makeList();
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

		if (!reset) {
			resetValues();
		}

		if (!otherAgents.containsKey(sender)) {
			OtherAgent newAgent = new OtherAgent(String.valueOf(sender), amountIssues);
			otherAgents.put(sender, newAgent);
		}

		if (DefaultAction.getBidFromAction(action) != null) {
			BidDetails bidDetail = new BidDetails(DefaultAction.getBidFromAction(action),
					getUtility(DefaultAction.getBidFromAction(action)));
			freqAnalysis(DefaultAction.getBidFromAction(action), sender);
			otherAgents.get(sender).addBidDetails(bidDetail);
			history.add(bidDetail);
		} else if (action.getClass().getName() == "negotiator.actions.Accept") { // Hier
																					// dingen
																					// aangepast
			freqAnalysis(history.getLastBidDetails().getBid(), sender);
			otherAgents.get(sender).addBidDetails(history.getLastBidDetails());
		}
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

		calculateThreshold();
		if (history.size() != 0 && history.getLastBidDetails().getMyUndiscountedUtil() > threshold) {
			return new Accept(getPartyId(), history.getLastBid());
		}

		return new Offer(getPartyId(), generateBid(threshold));
	}

	/**
	 * Generates bid
	 * 
	 * @param utility
	 * @return
	 */
	private Bid generateBid(double utility) {

		double currentTime = this.getTimeLine().getCurrentTime();
		Bid newBid = generateRandomBid();

		if (!listBids.isEmpty() && (currentTime / totalTime) < 0.5)
			newBid = monotonicStrategy();
		if (getUtility(newBid) > 0.7) {
			return newBid;
		}

		else if (!history.isEmpty() && (currentTime / totalTime) < 0.10) {
			newBid = useFreqAnalysis(utility);
		} else if (!history.isEmpty()) {
			newBid = history.getBestBidDetails().getBid();
		}

		if (getUtility(newBid) < threshold)
			try {
				newBid = this.utilitySpace.getMaxUtilityBid();
			} catch (Exception e) {
				e.printStackTrace();
			}

		return newBid;
	}

	// monotonic strategy
	// listBids contains bids that have not been done so far by this agent
	private Bid monotonicStrategy() {
		Bid bid = listBids.get(0).getBid();
		listBids.remove(0);
		return bid;
	}

	// apply believes of other agents
	// get their most important issue
	// and get from that issue their most import value
	// update bid by using this knowledge
	private Bid useFreqAnalysis(double utility) {
		List<Issue> issues = utilitySpace.getDomain().getIssues();
		Bid returnBid = possibleBids.getBidNearUtility(utility).getBid();
		HashMap<Integer, Value> changes = new HashMap<Integer, Value>();
		Object[] keys = otherAgents.keySet().toArray();

		for (int i = 0; i < keys.length - 1; i++) {
			OtherAgent agent = otherAgents.get(keys[i]);
			if (agent.isAgent()) {
				int issueNmr = agent.heighWeight();
				Value value = agent.getBestValue(issueNmr, issues);

				if (changes.containsKey(issueNmr)) {
					Value valueOld = changes.get(issueNmr);

					Bid possibleBid = returnBid;
					possibleBid = possibleBid.putValue(issueNmr, value);

					Bid possibleBid2 = returnBid;
					possibleBid2 = possibleBid2.putValue(issueNmr, valueOld);

					if (getUtility(possibleBid) > getUtility(possibleBid2)) {
						changes.put(issueNmr, value);
					}

				} else {
					changes.put(issueNmr, value);
				}
			}
		}

		Set<Integer> allChanges = changes.keySet();
		for (int j : allChanges) {
			returnBid = returnBid.putValue(j + 1, changes.get(j));
		}

		return returnBid;
	}

	// used for making a list concerning all bits
	private void makeList() {
		List<Issue> issues = utilitySpace.getDomain().getIssues();
		amountIssues = issues.size();
		listBids = possibleBids.getAllOutcomes();
	}

	// calculate time-dependent threshold
	private void calculateThreshold() {
		double currentTime = this.getTimeLine().getCurrentTime();
		threshold = 1 - Math.pow((currentTime / totalTime), 2);

		// we do not want to get a utility lower then 0.65
		if (threshold < 0.7)
			threshold = 0.7;
	}

	// apply frequency analysis
	private void freqAnalysis(Bid newBid, Object sender) {
		OtherAgent senderAgent = otherAgents.get(sender);
		if (!senderAgent.getHistory().isEmpty()) {
			double[] newWeights = determineWeights(newBid, senderAgent.getHistory().getLastBid(), 0.1,
					senderAgent.getWeights());
			senderAgent.setWeights(newWeights, true);
			senderAgent.setValues(newBid);
		}
	}

	// update weights by using previous and new bid
	private double[] determineWeights(Bid newBid, Bid prevBid, double n, double[] weight) {
		double totalWeight = 0;

		for (int i = 1; i < amountIssues; i++) {
			try {
				if (newBid.getValue(i) == prevBid.getValue(i))
					weight[i] = weight[i] + n;
			} catch (Exception e) {
				e.printStackTrace();
			}

			totalWeight = totalWeight + weight[i];
		}

		if (totalWeight > 0) {
			weight = normalizeWeight(totalWeight, weight);
		}

		return weight;
	}

	// normalize the given weights
	private double[] normalizeWeight(double total, double[] weights) {
		for (int i = 0; i < amountIssues - 1; i++) {
			weights[i] = weights[i] / total;
		}
		return weights;
	}

	@Override
	public String getDescription() {
		return "example party group 12b";
	}
}

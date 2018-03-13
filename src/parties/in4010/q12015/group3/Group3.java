package parties.in4010.q12015.group3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.DefaultAction;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.OutcomeSpace;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

/**
 * This is your negotiation party.
 */
public class Group3 extends AbstractNegotiationParty {
	private double alfa;
	private double beta;

	private double MIN_BID_UTILITY;
	private double LEARNING_TIME;
	private SortedOutcomeSpace sortedBids;
	private OutcomeSpace space;

	private double opponentWeights[];
	private double WEIGHT_PARAMETER = 0.1;

	private List<Action> opponentActions;
	private Bid previousBidOpponent;
	private double HIGH_WEIGHT;
	private int HAMMING_THRESHOLD;

	private List<Double> opponentUtils = new ArrayList<Double>();
	private double ACCEPTANCE_THRESHOLD;

	private int roundsCounter;

	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		this.opponentActions.add(action);
	}

	public String getDescription() {
		return "Group 3.oct-19-4.2";
	}

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		System.out.println("\n");
		System.out.println("####### STARTING SESSION #########");
		System.out.println("Initializing Agent");

		this.space = new OutcomeSpace(info.getUtilitySpace());
		sortedBids = new SortedOutcomeSpace(info.getUtilitySpace());
		this.opponentActions = new ArrayList<Action>();

		super.timeline = timeline;
		super.utilitySpace = info.getUtilitySpace();

		this.LEARNING_TIME = 0.1 * info.getDeadline().getValue();
		this.HAMMING_THRESHOLD = 3;
		this.HIGH_WEIGHT = 0.15;
		this.ACCEPTANCE_THRESHOLD = 0.75;

		this.alfa = 0.05;
		this.beta = -Math.log(alfa) / info.getDeadline().getValue();
		this.MIN_BID_UTILITY = 0.8;

		opponentWeights = new double[info.getUtilitySpace().getDomain().getIssues().size()];
		this.previousBidOpponent = null;

		for (int i = 0; i < info.getUtilitySpace().getDomain().getIssues().size(); i++)
			opponentWeights[i] = 1 / (double) (info.getUtilitySpace().getDomain().getIssues().size());

		roundsCounter = 0;

		System.out.println("Initialization Completed!");
		System.out.println("Opponent Model: " + Arrays.toString(opponentWeights));
	}

	/**
	
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Action action = null;
		Action lastBidResponse = null;
		Bid nextBid = null;

		double opponentUtility, myUtility;

		System.out.println("\nROUND #" + roundsCounter++);

		if (opponentActions.isEmpty())
			return new Offer(getPartyId(), getNextBid(null));

		try {
			lastBidResponse = opponentActions.get(0);

			Bid toBeAccepted = DefaultAction.getBidFromAction(opponentActions.get(opponentActions.size() - 1));

			Bid toBeModeled = DefaultAction.getBidFromAction(lastBidResponse);

			/* First we update the model of our opponent */
			if (lastBidResponse instanceof Offer)
				opponentWeights = this.updateModel(toBeModeled, opponentWeights);

			if (lastBidResponse instanceof Accept) {
				System.out.println("Reoffering previous Bid:");

				toBeModeled = this.previousBidOpponent;
			}

			System.out.println("Opponent Model: " + Arrays.toString(opponentWeights));

			/*
			 * Second, we choose the next bid wrt the preferences of the next
			 * opponent
			 */
			nextBid = this.getNextBid(toBeModeled);

			/* Last, we decide whether to accept the last bid or propose ours */
			myUtility = getUtility(nextBid);
			opponentUtility = getUtility(toBeAccepted);

			if (isAcceptable(opponentUtility, myUtility)) {
				System.out.println("Accepting Bid with utility: " + opponentUtility);
				System.out.println(toBeAccepted.toString());
				action = new Accept(getPartyId(), ((ActionWithBid) getLastReceivedAction()).getBid());
			} else
				action = new Offer(getPartyId(), nextBid);

			/* Update parameters */
			opponentUtils.add(opponentUtility);

			this.previousBidOpponent = toBeModeled;
		} catch (Exception e) {
			System.out.println("CHOOSE ACTION");
			e.printStackTrace();
			System.out.println("List of opponent actions:\n" + opponentActions.toString());
			action = new EndNegotiation(getPartyId());
		}

		this.opponentActions.clear();

		return action;
	}

	/************************
	 * ACCEPTANCE STRATEGY
	 ****************************************/

	// ACcombi(T, MAXw)
	private boolean isAcceptable(double offeredUtilFromOpponent, double myOfferedUtil) {
		double time = timeline.getCurrentTime();
		double timeThreshold = ACCEPTANCE_THRESHOLD * getDeadlines().getValue();

		// ACnext
		if (offeredUtilFromOpponent > myOfferedUtil)
			return true;

		// ACtime
		if (time > timeThreshold) {
			// ACmaxW
			if (offeredUtilFromOpponent > getMaxValue(opponentUtils))
				return true;
		}

		return false;
	}

	public double getMaxValue(List<Double> values) {
		double maxValue = Double.MIN_VALUE;

		try {
			for (Double d : values) {
				if (d > maxValue) {
					maxValue = d;
				}
			}
		} catch (Exception e) {
			System.out.println("GETMAXVALUE");
			e.printStackTrace();
		}

		return maxValue;
	}

	/************************
	 * BIDDING STRATEGY
	 ****************************************/
	private boolean compareValues(Value v1, Value v2) {
		String v1Str = v1.toString();
		String v2Str = v2.toString();

		return v1Str.equals(v2Str);
	}

	private int computeHammingDistance(Bid b1, Bid b2) {
		int nIssues = b1.getIssues().size();
		int distance = 0;

		/*
		 * IF issues are different IF weight for that issue is small distance =
		 * 1 ELSE distance = 2
		 */

		for (int i = 1; i <= nIssues; i++) {
			try {
				if (!compareValues(b1.getValue(i), b2.getValue(i)))
					distance += opponentWeights[i - 1] > HIGH_WEIGHT ? 2 : 1;
			} catch (Exception e) {
				System.out.println("HAMMING");
				e.printStackTrace();
			}
		}

		return distance;
	}

	private Bid getNextBid(Bid opponent) {
		double time = timeline.getCurrentTime();
		double targetUtility;

		if (opponent == null) {
			System.out.println("No previous bids to evaluate.");
			return space.getMaxBidPossible().getBid();
		}

		List<BidDetails> allBids = this.sortedBids.getAllOutcomes();// space.getAllOutcomes();

		double maxUtility = 0;
		Bid maxUtilityBid = null;

		if (time < LEARNING_TIME) {
			System.out.println("Learning period: bidding max.");
			return space.getMaxBidPossible().getBid();
		}

		for (java.util.Iterator<BidDetails> i = allBids.iterator(); i.hasNext();) {
			Bid bid = i.next().getBid();

			if (getUtility(bid) < MIN_BID_UTILITY)
				break;

			if (computeHammingDistance(opponent, bid) < HAMMING_THRESHOLD)

				if (getUtility(bid) > maxUtility) {
					maxUtility = getUtility(bid);
					maxUtilityBid = bid;
				}
		}

		if (maxUtility > MIN_BID_UTILITY) {
			System.out.println("Hamming Utility: " + maxUtility);
			return maxUtilityBid;
		}

		targetUtility = 1 - alfa * Math.exp(beta * (time - LEARNING_TIME));

		System.out.println("Exponential decay: " + targetUtility);

		if (targetUtility < MIN_BID_UTILITY) {
			System.out.println("Lower threshold reached.");
			targetUtility = MIN_BID_UTILITY;
		}

		return sortedBids.getBidNearUtility(targetUtility).getBid();
	}

	/** OPPONENT MODELING */
	private double[] updateModel(Bid newBid, double[] currentWeights) {
		double sum = 0;

		if (previousBidOpponent == null)
			return currentWeights;

		for (int i = 1; i <= newBid.getIssues().size(); i++) {
			try {
				if (newBid.getValue(i) == previousBidOpponent.getValue(i))
					currentWeights[i - 1] += WEIGHT_PARAMETER;

				sum += currentWeights[i - 1];
			} catch (Exception e) {
				System.out.println("UPDATE 1");
				e.printStackTrace();
			}
		}

		try {
			for (int i = 0; i < newBid.getIssues().size(); i++)
				currentWeights[i] = currentWeights[i] / sum;
		} catch (Exception e) {
			System.out.println("UPDATE 2");
			e.printStackTrace();
		}

		return currentWeights;
	}

}

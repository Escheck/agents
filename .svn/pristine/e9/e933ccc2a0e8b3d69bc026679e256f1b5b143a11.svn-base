package parties.in4010.q12015.group20;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

/**
 * This is your negotiation party.
 */
public class Group20 extends AbstractNegotiationParty {

	// Max > Min
	private final static double MINUTILSCORE = 0.7;
	private final static double MAXUTILSCORE = 0.8;

	private Action lastOpponentAction = null;
	private Double[] ourWeights; // store weights of issues
	private Bid lastOpOffer = null;
	private HashMap<Integer, Double[]> choices = new HashMap<Integer, Double[]>(); // store
																					// evaluation
																					// values
																					// for
																					// every
																					// issue
	private HashMap<Integer, Integer[]> agentIssues = new HashMap<Integer, Integer[]>(); // store
																							// evaluation
																							// values
																							// for
																							// every
																							// issue:
																							// <issueID,
																							// values>
	private Double[] agentWeights = null; // save weights of opponents'
											// preference

	/**
	 * Initialization function
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		agentWeights = new Double[utilitySpace.getDomain().getIssues().size()];

		/*******
		 * reservation value is 0, I believe, read somewhere in the assignment
		 ******/
		int numIssues = utilitySpace.getDomain().getIssues().size(); // total
																		// number
																		// of
																		// issues
		ourWeights = new Double[numIssues];

		/********** Store evaluation value for each choice ************/
		List<Issue> issues = utilitySpace.getDomain().getIssues();

		/**************
		 * initialize on the 1st round, every value equals to 1
		 ***************/
		for (Issue lIssue : issues) {
			int issueID = lIssue.getNumber();
			IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue; // TODO add
																	// non
																	// discrete
			Integer[] evals = new Integer[lIssueDiscrete.getNumberOfValues()];
			for (int i = 0; i < lIssueDiscrete.getNumberOfValues(); i++) {
				evals[i] = 1; // initialize every value to 1
			}
			agentIssues.put(issueID, evals);
		}

		for (Issue lIssue : issues) {
			int issueID = lIssue.getNumber();
			agentWeights[issueID - 1] = 1.0 / issues.size(); // initialize the
																// opponents'
																// weight
			ourWeights[issueID - 1] = ((AdditiveUtilitySpace) utilitySpace).getWeight(issueID); // save
			// weights
			// of
			// issues
			// into
			// weights[]

			IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
			Double[] evals = new Double[lIssueDiscrete.getNumberOfValues()];
			EvaluatorDiscrete evaluator = (EvaluatorDiscrete) ((AdditiveUtilitySpace) utilitySpace)
					.getEvaluator(issueID);

			for (int i = 0; i < lIssueDiscrete.getNumberOfValues(); i++) {
				ValueDiscrete value = lIssueDiscrete.getValue(i);
				Double eval = evaluator.getDoubleValue(value); // evaluation of
																// each choice,
																// such as
																// "cocktail","beer
																// only"
				evals[i] = eval; // evals[i] is like 5,25,10,....
			}

			choices.put(issueID, evals);
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
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Action action = null;

		try {
			if (lastOpponentAction == null) {
				action = createBidAction();
			}

			if (lastOpponentAction instanceof Offer) {
				Bid partnerBid = ((Offer) lastOpponentAction).getBid();

				if (isAcceptable(partnerBid)) {
					return new Accept(getPartyId(), partnerBid);
				}
				action = createBidAction();
			}
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			// !!!!best guess if things go
			// wrong.
			return new Accept(getPartyId(), ((ActionWithBid) lastOpponentAction).getBid());
		}
		return action;
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * These offers are used to build a predicted preference profile
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action != null && action instanceof Offer) {
			lastOpponentAction = (Offer) action;

			Bid opponentBid = DefaultAction.getBidFromAction(action);
			predictPreferences(opponentBid);
			lastOpOffer = opponentBid; // save the offer of opponent
		}

	}

	/**
	 * Predict opponents preferences using Frequency Analysis Heuristic Instead
	 * of two preferences, generate a preference profile for all the opponents
	 * 
	 * @param agentID
	 * @param agentBid
	 */
	private void predictPreferences(Bid agentBid) {
		/********** save issues of the opponent ***********/
		List<Issue> opponentIssues = agentBid.getIssues();

		/***************** update each round ********************/
		for (Issue lIssue : opponentIssues) {
			int issueID = lIssue.getNumber(); // issueID starts from 1
			IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue; // TODO add
																	// non
																	// discrete
			lIssueDiscrete.getNumberOfValues();
			int valIndex = 0; // valIndex starts from 0

			try {
				valIndex = lIssueDiscrete.getValueIndex((ValueDiscrete) agentBid.getValue(issueID)); // find
																										// index
																										// of
																										// value
																										// in
																										// issue

				/*********** refresh weights *********/
				if (lastOpOffer != null && lastOpOffer.getValue(issueID).equals(agentBid.getValue(issueID))) {
					Double[] newWeights = refreshWeight(agentWeights, issueID, 0.1).clone();
					agentWeights = newWeights.clone();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (agentIssues.containsKey(issueID)) {

				Integer[] values = agentIssues.get(issueID).clone();
				values[valIndex] = values[valIndex] + 1; // every time the bid
															// appeared, give it
															// plus 1 value
				if (agentIssues.containsKey(issueID)) {
					agentIssues.put(issueID, values.clone());
				}
			}
		}

	}

	/**
	 * Create a bid or accept
	 * 
	 * @return Created action can be offer or accept action
	 */
	private Action createBidAction() {
		Bid nextBid = null;

		try {
			nextBid = getOurNewBid();
		} catch (Exception e) {
			System.out.println("Problem with received bid:" + e + ". cancelling bidding");
		}

		if (nextBid == null) {
			System.out.println("Should not happen.");
			return (new Accept(getPartyId(), ((ActionWithBid) lastOpponentAction).getBid()));
		}
		return (new Offer(getPartyId(), nextBid));
	}

	/**
	 * Checks if we want to accept a bid. Accepting is based on the given bid
	 * having a higher utility value than that we currently have as a minimum
	 * 
	 * @param partnerBid
	 *            the bid we want to check
	 * @return true or false
	 * @throws Exception
	 */
	private boolean isAcceptable(Bid partnerBid) throws Exception {
		boolean isAccept = false;
		double acceptUtil = getMinUtilValue();
		if (acceptUtil < utilitySpace.getUtility(partnerBid)) {
			isAccept = true;
		}
		return isAccept;
	}

	/**
	 * Get the minimum utility value based on the current negotiation time
	 * 
	 * @return minimum utility
	 */
	private double getMinUtilValue() {
		return MINUTILSCORE + (timeline.getTime() * (MAXUTILSCORE - MINUTILSCORE));
	}

	/**
	 * Get a new bid
	 * 
	 * @return a new bid being a combination of opponets possible max utility
	 *         adjusted to our min utility score.
	 * @throws Exception
	 */
	private Bid getOurNewBid() throws Exception {
		Bid maxOppBid = createMaxOpponentBid();
		compromiseBid(maxOppBid);
		return maxOppBid;

	}

	/**
	 * Create the maximum possible bid for the opponents based on the predicted
	 * preference profile that was formed of them.
	 * 
	 * @return Bid with maximum utility score for all opponents
	 * @throws Exception
	 */
	private Bid createMaxOpponentBid() throws Exception {
		Bid bid = null;
		Collection<Integer[]> valueList = agentIssues.values();
		int i = 1;
		HashMap<Integer, Value> tmpValues = new HashMap<Integer, Value>();
		for (Iterator iterator = valueList.iterator(); iterator.hasNext(); i++) {
			int maxIndex = getIndexMaxValue((Integer[]) iterator.next());
			ValueDiscrete maxPreValue = ((IssueDiscrete) ((AdditiveUtilitySpace) utilitySpace).getIssue(i - 1))
					.getValue(maxIndex);
			tmpValues.put(i, maxPreValue);
		}

		bid = new Bid(utilitySpace.getDomain(), tmpValues);
		return bid;
	}

	/**
	 * Get the index of the element with the maximum
	 * 
	 * @param primArray
	 * @return index of maxvalue from primarray
	 */
	private int getIndexMaxValue(Integer[] primArray) {
		int max = -1;
		int index = -1;
		for (int i = 0; i < primArray.length; i++) {
			if (primArray[i] > max) {
				max = primArray[i];
				index = i;
			}
		}
		return index;
	}

	/**
	 * Given a Bid, increase the bid until we get to a value that is acceptable
	 * for us. The issue to change is chosen where our weight is high while the
	 * predicted opponents weight is low. This makes it so that for us there is
	 * a high change in utility while for the opponent the change is minimal.
	 * 
	 * @param maxOppBid
	 *            Bid to adjust
	 * @throws Exception
	 */
	private void compromiseBid(Bid maxOppBid) throws Exception {
		ArrayList<Double> diffList = getDiffList();
		int issueId;
		while (utilitySpace.getUtility(maxOppBid) < getMinUtilValue()) {
			issueId = getMaxID(diffList);
			ValueDiscrete newValue = getMaxIssueValue(issueId);
			maxOppBid = maxOppBid.putValue(issueId, newValue);
		}
	}

	/**
	 * Get the maximal value for a certain discrete issue
	 * 
	 * @param issueId
	 * @return maximum vallue for issueID
	 */
	private ValueDiscrete getMaxIssueValue(int issueId) {
		Double max = Double.MIN_VALUE;
		int idx = -1;
		Double[] values = choices.get(issueId);
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
				idx = i;
			}
		}
		return ((IssueDiscrete) ((AdditiveUtilitySpace) utilitySpace).getIssue(issueId - 1)).getValue(idx);
	}

	/**
	 * Returns the ID of maximum difference, and sets that index to minimal
	 * value
	 * 
	 * @param diffList
	 * @return ID of the issue (starts at 1)
	 */
	private int getMaxID(ArrayList<Double> diffList) {
		int id = -1;
		double maxDiff = Double.MIN_VALUE;

		for (int i = 0; i < diffList.size(); i++) {
			if (diffList.get(i) > maxDiff) {
				id = i + 1;
				maxDiff = diffList.get(i);
			}
		}

		diffList.set(id - 1, -1.0);

		return id;
	}

	/**
	 * Creates a differences list of the differences between our domain weights
	 * and the predicted weights of the opponents.
	 * 
	 * @return list of differences in weight
	 */
	private ArrayList<Double> getDiffList() {
		ArrayList<Double> diffList = new ArrayList<Double>();
		for (int j = 0; j < agentWeights.length; j++) {
			diffList.add(ourWeights[j] - agentWeights[j] + 1);
		}
		return diffList;
	}

	/**
	 * Refresh and update opponents weights
	 * 
	 * @param opWeights
	 * @param issueID
	 * @param n
	 * @return
	 */
	public Double[] refreshWeight(Double[] opWeights, int issueID, double n) {
		opWeights[issueID - 1] = opWeights[issueID - 1] + n; // issueID starts
																// from 1
		Double[] newWeights = opWeights.clone();
		double sum = 0;

		for (int i = 0; i < newWeights.length; i++) {
			sum += newWeights[i];
		}

		for (int i = 0; i < newWeights.length; i++) {
			newWeights[i] = newWeights[i] / sum;
		}

		return newWeights;
	}

	/**
	 * Returns the predicted utility function for a opponent
	 * 
	 * @param agentID
	 * @param bid
	 * @return
	 * @throws Exception
	 */
	public double predictUtility(AgentID agentID, Bid bid) throws Exception {
		double opUtility = 0;
		List<Issue> issueList = bid.getIssues();

		for (Issue lIssue : issueList) {
			int issueID = lIssue.getNumber(); // issueID starts from 1
			IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
			Integer[] values = agentIssues.get(issueID).clone();
			double sumValues = 0;

			for (int i = 0; i < values.length; i++) {
				if (values[i] == null)
					values[i] = 1; // or other values, to escape
									// nullpointerexception;
				sumValues += values[i];
			}

			int valIndex = lIssueDiscrete.getValueIndex((ValueDiscrete) bid.getValue(issueID));
			opUtility += agentWeights[issueID - 1] * values[valIndex] / sumValues;

		}

		return opUtility;

	}

	@Override
	public String getDescription() {
		return "Party group 20";
	}

}
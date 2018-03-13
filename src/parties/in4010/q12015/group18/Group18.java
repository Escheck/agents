package parties.in4010.q12015.group18;

import java.util.ArrayList;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

/**
 * This is our negotiation agent.
 */
public class Group18 extends AbstractNegotiationParty {

	private int LastSenderId;
	private Bid LastBid;
	private boolean bidListInit = false;
	private ArrayList<Bid>[] partiesBidList;
	private ArrayList<String> partiesNameList = new ArrayList<String>();
	private ArrayList<AdditiveUtilitySpace> OpponentUtilities = new ArrayList<AdditiveUtilitySpace>();

	// This function will give the final action by calling the BiddingStrategy
	// and the AcceptStrategie
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		Action upcomingBid = BiddingStrategy();
		Action finalAction = AcceptStrategy(validActions, upcomingBid);
		return finalAction;
	}

	// This function determines the next bid you are going to propose
	public Action BiddingStrategy() {
		Bid[] PossibleBids = new Bid[((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators()];
		Double[] UtilityPossibleBids = new Double[((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators()];

		Bid MyBestBid;
		try {// gets the best bid, if this doesnt work, it will use a random bid
				// to have some thing to work with
			MyBestBid = this.getUtilitySpace().getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
			MyBestBid = generateRandomBid();
		}// generates the max utility bid, in case this can not be found returns
			// a random bid, is bad should be a better solution.

		double TimePercentage = this.getTimeLine().getTime();// should give a
																// percentage of
																// the max time
		double TimeFactor;
		if (TimePercentage < .75) {
			TimeFactor = .9;
		} else if (TimePercentage < .9) {
			TimeFactor = .8;
		} else {
			TimeFactor = .7;
		}// values herein are just arbitrary selected, but because the results
			// are good, they are used

		Bid RandomBid;
		for (int RandomBidNo = 0; RandomBidNo < ((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators(); RandomBidNo++) {
			do {
				RandomBid = generateRandomBid();
			} while (this.getUtility(RandomBid) < this.getUtility(MyBestBid)
					* TimeFactor);
			PossibleBids[RandomBidNo] = RandomBid;

			if (TimePercentage > 0.25) {
				double TotalOpponentUtilityRandomBid = 0;
				double MaxOpponentUtilityRandomBid = 0;
				for (AdditiveUtilitySpace Opponent : OpponentUtilities) {
					try {
						TotalOpponentUtilityRandomBid = TotalOpponentUtilityRandomBid
								+ Opponent
										.getUtility(PossibleBids[RandomBidNo]);
						if (Opponent.getUtility(PossibleBids[RandomBidNo]) > MaxOpponentUtilityRandomBid) {
							MaxOpponentUtilityRandomBid = Opponent
									.getUtility(PossibleBids[RandomBidNo]);
						}
						Opponent.getUtility(PossibleBids[RandomBidNo]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				UtilityPossibleBids[RandomBidNo] = TotalOpponentUtilityRandomBid
						/ OpponentUtilities.size()
						+ getUtility(PossibleBids[RandomBidNo])
						- MaxOpponentUtilityRandomBid;
			} else {
				UtilityPossibleBids[RandomBidNo] = getUtility(RandomBid);
			}
		}

		double BestBidValue = 0;
		int BestBidNo = 0;
		for (int RandomBidNo = 0; RandomBidNo < ((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators(); RandomBidNo++) {
			if (UtilityPossibleBids[RandomBidNo] > BestBidValue) {
				BestBidValue = UtilityPossibleBids[RandomBidNo];
				BestBidNo = RandomBidNo;
			}
		}
		return new Offer(getPartyId(), PossibleBids[BestBidNo]);
	}

	// This function determines if the current bid should be accepted based your
	// new bid
	public Action AcceptStrategy(List<Class<? extends Action>> validActions,
			Action upcomingBid) {

		double TimePercentage = this.getTimeLine().getTime();
		double TimeFactor;

		if (TimePercentage < 0.25) {
			TimeFactor = 0.9; // for the first 25% of the time only accept 0.9
								// or higher
		} else {
			TimeFactor = (0.5) * (1 - TimePercentage) + 0.5; // after that
																// linearly
																// decrease to 0
																// at time = 1
		}

		// System.out.print(timeFactor);
		if (validActions.contains(Accept.class)
				&& (this.getUtility(LastBid) > TimeFactor || this
						.getUtility(LastBid) > this.getUtility(DefaultAction
						.getBidFromAction(upcomingBid)))) {

			try {
				if (TimePercentage > .25
						&& TimePercentage < .95
						&& OpponentUtilities.get(LastSenderId).getUtility(
								LastBid) > this.utilitySpace
								.getUtility(LastBid)) {
					return upcomingBid;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new Accept(getPartyId(), LastBid); // accept if accept is
														// possible and
			// bid is
			// higher then our own bid or bid is higher
			// then the reference which decreases with
			// time.
		} else {
			return upcomingBid;
		}

	}

	// This function receives the message, stores the bid and calls the function
	// UpdateOpponentModel
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		int OpponentIDNo = 0;

		if (!(bidListInit)) {
			initializeBidList(); // initialize structures to keep track of all
									// bids
		} else {
			if (!(sender == null)) {
				String OpponentID = sender.toString(); // determine the sender
														// of
														// the bid
				if (partiesNameList.indexOf(OpponentID) == -1) {
					partiesNameList.add(OpponentID); // if the sender is not yet
														// in the list of
														// participants, add it
				}
				OpponentIDNo = partiesNameList.indexOf(OpponentID);
				LastSenderId = OpponentIDNo;
				if (DefaultAction.getBidFromAction(action) != null) {
					partiesBidList[OpponentIDNo].add(DefaultAction
							.getBidFromAction(action)); // add the bid to the
														// list add index of ID
				}
			}
		}
		LastBid = DefaultAction.getBidFromAction(action); // save bid as last
															// bid
		UpdateOpponentModel(sender, action);
	}

	// This function is used to update the opponent model
	public void UpdateOpponentModel(AgentID sender, Action action) {
		if (sender == null) {
			return;
		}

		int OpponentIDNo = 0;
		ArrayList<Bid> CurrentBidList = null;

		// Different Evaluators are weighted.
		// STEP 0, create structures to save weights
		double[] EvaluatorWeights = new double[((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators()];
		double sumEvaluatorWeights = 0; // needed to normalize

		// STEP 1, extract bid list of current opponent
		String OpponentID = String.valueOf(sender); // determine the sender of
													// the bid

		if (partiesNameList.indexOf(OpponentID) == -1) {
			partiesNameList.add(OpponentID); // if the sender is not yet in the
												// list of participants, add it
		}
		OpponentIDNo = partiesNameList.indexOf(OpponentID);
		CurrentBidList = partiesBidList[OpponentIDNo]; // add the bid to the
														// list add index of ID

		// STEP 2, count number of changes per Evaluator
		for (int EvaluatorNo = 0; EvaluatorNo < ((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators(); EvaluatorNo++) {
			EvaluatorWeights[EvaluatorNo] = OpponentUtilities.get(OpponentIDNo)
					.getEvaluator(EvaluatorNo + 1).getWeight();
			try {
				if (CurrentBidList.size() > 1) {
					if (CurrentBidList
							.get(CurrentBidList.size() - 1)
							.getValue(EvaluatorNo + 1)
							.equals(CurrentBidList.get(
									CurrentBidList.size() - 2).getValue(
									EvaluatorNo + 1))) {
						EvaluatorWeights[EvaluatorNo] = EvaluatorWeights[EvaluatorNo] + 0.1;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			sumEvaluatorWeights = sumEvaluatorWeights
					+ EvaluatorWeights[EvaluatorNo];
		}

		// STEP 3, Normalize and update weights in model
		for (int EvaluatorNo = 0; EvaluatorNo < ((AdditiveUtilitySpace) utilitySpace)
				.getNrOfEvaluators(); EvaluatorNo++) {
			OpponentUtilities
					.get(OpponentIDNo)
					.getEvaluator(EvaluatorNo + 1)
					.setWeight(
							EvaluatorWeights[EvaluatorNo] / sumEvaluatorWeights);
			// Different issues in the Evaluators are checked
			switch (OpponentUtilities.get(OpponentIDNo).getDomain().getIssues()
					.get(EvaluatorNo).getType()) {
			case DISCRETE:
				IssueDiscrete DiscreteIssue = (IssueDiscrete) this.utilitySpace
						.getDomain().getIssues().get(EvaluatorNo);
				double[] ValuesAppearance = new double[DiscreteIssue
						.getNumberOfValues()];

				// STEP 4, Count number of Appearances of values
				double MaxAppearance = 0;
				for (int IssueNo = 0; IssueNo < DiscreteIssue
						.getNumberOfValues(); IssueNo++) {
					for (int BidNo = 0; BidNo < partiesBidList[OpponentIDNo]
							.size(); BidNo++) {
						try {
							if (DiscreteIssue.getValue(IssueNo).equals(
									partiesBidList[OpponentIDNo].get(BidNo)
											.getValue(EvaluatorNo + 1))) {
								ValuesAppearance[IssueNo]++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (ValuesAppearance[IssueNo] > MaxAppearance) {
						MaxAppearance = ValuesAppearance[IssueNo];
					}
				}
				// STEP 5, normalize and update value grades
				for (int IssueNo = 0; IssueNo < DiscreteIssue
						.getNumberOfValues(); IssueNo++) {
					ValuesAppearance[IssueNo] = ValuesAppearance[IssueNo]
							/ MaxAppearance;
					EvaluatorDiscrete CurrentEvaluator = (EvaluatorDiscrete) OpponentUtilities
							.get(OpponentIDNo).getEvaluator(EvaluatorNo + 1);
					try {
						CurrentEvaluator
								.setEvaluationDouble(
										(ValueDiscrete) DiscreteIssue
												.getValue(IssueNo),
										ValuesAppearance[IssueNo]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		}

	}

	// Initialize ArrayLists to record bid by opponents
	public void initializeBidList() {
		partiesBidList = new ArrayList[this.getNumberOfParties() - 1];
		for (int PartyNo = 1; PartyNo < this.getNumberOfParties(); PartyNo++) { // for
																				// every
																				// other
																				// player
																				// make
																				// an
																				// array
																				// list
																				// to
																				// save
																				// all
																				// bids
																				// and
																				// make
																				// a
																				// utility
																				// space
																				// to
																				// save
																				// the
																				// opponent
																				// model
			partiesBidList[PartyNo - 1] = new ArrayList<Bid>();
			try {
				OpponentUtilities.add(new AdditiveUtilitySpace(this
						.getUtilitySpace().getDomain(), ""));
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int EvaluatorNo = 1; EvaluatorNo <= ((AdditiveUtilitySpace) utilitySpace)
					.getNrOfEvaluators(); EvaluatorNo++) {
				OpponentUtilities
						.get(PartyNo - 1)
						.getEvaluator(EvaluatorNo)
						.setWeight(
								1 / (double) ((AdditiveUtilitySpace) utilitySpace)
										.getNrOfEvaluators());
			}
		}

		bidListInit = true;
	}

	// This function provides GENIUS with the name of this agent
	@Override
	public String getDescription() {
		return "Agent 47, [Group 18]";
	}

}

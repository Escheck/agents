package agents.anac.y2015.Mercury;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import negotiator.Bid;
import negotiator.Domain;
//import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.utility.AdditiveUtilitySpace;

//import negotiator.parties.AbstractNegotiationParty;
/**
 * Operations related with the opponent's bid history. The opponent's first 100
 * unique bids are remembered.
 * 
 * @author Justin
 */
public class OpponentBidHistory {

	private ArrayList<Bid> bidHistory;
	private ArrayList<ArrayList<Integer>> opponentBidsStatisticsForReal;
	private ArrayList<HashMap<Value, Integer>> opponentBidsStatisticsDiscrete;
	private ArrayList<ArrayList<Integer>> opponentBidsStatisticsForInteger;
	private int maximumBidsStored = 100;
	private HashMap<Bid, Integer> bidCounter = new HashMap<Bid, Integer>();
	private Bid bid_maximum_from_opponent;// the bid with maximum utility
											// proposed by the opponent so far.
	public ArrayList<Object> partyOrder;
	private ArrayList<Bid> oppMax;
	private ArrayList<Bid> acceHistory;
	private ArrayList<Object> acceHistoryParty;
	private ArrayList<Bid> oppAcce;
	private int partyNum = -1;
	protected AdditiveUtilitySpace fUtilitySpace;

	public OpponentBidHistory() {
		this.bidHistory = new ArrayList<Bid>();
		opponentBidsStatisticsForReal = new ArrayList<ArrayList<Integer>>();
		opponentBidsStatisticsDiscrete = new ArrayList<HashMap<Value, Integer>>();
		opponentBidsStatisticsForInteger = new ArrayList<ArrayList<Integer>>();
	}

	public void initiPartyRec(int num, AdditiveUtilitySpace pUtilitySpace) {
		this.partyOrder = new ArrayList<Object>();
		this.oppMax = new ArrayList<Bid>(); // the best opponent offer from each
											// party
		this.acceHistoryParty = new ArrayList<Object>();
		this.acceHistory = new ArrayList<Bid>();
		this.partyNum = num;
		this.oppAcce = new ArrayList<Bid>(); // the best acceptance from each
												// party

		this.fUtilitySpace = pUtilitySpace;
	}

	public void updateOppRec(Bid oppbid, Object sender) {

		if (partyOrder.size() < partyNum - 1) {
			partyOrder.add(sender);
			oppMax.add(oppbid);
		} else {

			try {
				if (fUtilitySpace.getUtility(oppMax.get(partyOrder
						.indexOf(sender))) < fUtilitySpace.getUtility(oppbid))
					oppMax.set(partyOrder.indexOf(sender), oppbid);

			} catch (Exception e) {
				System.out.println("error in updateOppRec method"
						+ e.getMessage());
			}
		}
	}

	public Bid getBestOpp(Object party) {
		// Bid bid = null;

		return oppMax.get(partyOrder.indexOf(party));
	}

	public Bid getMiniBestOpp() {
		// Bid bid = null;
		// int temp = 0;
		Bid bid = oppMax.get(0);

		for (int i = 0; i < partyNum - 1; i++) {

			try {
				// System.out.println("test in method getMiniBestOpp"+oppMax.get(0)+","+fUtilitySpace.getUtility(oppMax.get(0)));
				// System.out.println("test in method getMiniBestOpp"+oppMax.get(1)+","+fUtilitySpace.getUtility(oppMax.get(1)));

				if (fUtilitySpace.getUtility(bid) > fUtilitySpace
						.getUtility(oppMax.get(i)))
					bid = oppMax.get(i);
			} catch (Exception e) {
				System.out.println("error in method getMiniBestOpp");
			}
		}

		return bid;
	}

	public void updateAccRec(Bid oppbid, Object sender) {
		acceHistory.add(oppbid);
		acceHistoryParty.add(sender);
	}

	public Bid getLastAcce() {
		return acceHistory.get(acceHistory.size() - 1);
	}

	public void addBid(Bid bid, AdditiveUtilitySpace utilitySpace) {
		if (bidHistory.indexOf(bid) == -1) {
			bidHistory.add(bid);
		}
		try {
			if (bidHistory.size() == 1) {
				this.bid_maximum_from_opponent = bidHistory.get(0);
			} else {
				if (utilitySpace.getUtility(bid) > utilitySpace
						.getUtility(this.bid_maximum_from_opponent)) {
					this.bid_maximum_from_opponent = bid;
				}
			}
		} catch (Exception e) {
			System.out.println("error in addBid method" + e.getMessage());
		}
	}

	public Bid getBestBidInHistory() {
		return this.bid_maximum_from_opponent;
	}

	/**
	 * initialization
	 */
	public void initializeDataStructures(Domain domain) {
		try {
			List<Issue> issues = domain.getIssues();
			for (Issue lIssue : issues) {
				switch (lIssue.getType()) {
				case DISCRETE:
					IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
					HashMap<Value, Integer> discreteIssueValuesMap = new HashMap<Value, Integer>();
					for (int j = 0; j < lIssueDiscrete.getNumberOfValues(); j++) {
						Value v = lIssueDiscrete.getValue(j);
						discreteIssueValuesMap.put(v, 0);
					}
					opponentBidsStatisticsDiscrete.add(discreteIssueValuesMap);
					break;

				case REAL:
					IssueReal lIssueReal = (IssueReal) lIssue;
					ArrayList<Integer> numProposalsPerValue = new ArrayList<Integer>();
					int lNumOfPossibleValuesInThisIssue = lIssueReal
							.getNumberOfDiscretizationSteps();
					for (int i = 0; i < lNumOfPossibleValuesInThisIssue; i++) {
						numProposalsPerValue.add(0);
					}
					opponentBidsStatisticsForReal.add(numProposalsPerValue);
					break;

				case INTEGER:
					IssueInteger lIssueInteger = (IssueInteger) lIssue;
					ArrayList<Integer> numOfValueProposals = new ArrayList<Integer>();

					// number of possible value when issue is integer (we should
					// add 1 in order to include all values)
					int lNumOfPossibleValuesForThisIssue = lIssueInteger
							.getUpperBound()
							- lIssueInteger.getLowerBound()
							+ 1;
					for (int i = 0; i < lNumOfPossibleValuesForThisIssue; i++) {
						numOfValueProposals.add(0);
					}
					opponentBidsStatisticsForInteger.add(numOfValueProposals);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("EXCEPTION in initializeDataAtructures");
		}
	}

	/**
	 * This function updates the opponent's Model by calling the
	 * updateStatistics method
	 */
	public void updateOpponentModel(Bid bidToUpdate, Object sender,
			Domain domain, AdditiveUtilitySpace utilitySpace) {
		this.addBid(bidToUpdate, utilitySpace);
		// partyHistory.add(sender);

		if (bidCounter.get(bidToUpdate) == null) {
			bidCounter.put(bidToUpdate, 1);
		} else {
			int counter = bidCounter.get(bidToUpdate);
			counter++;
			bidCounter.put(bidToUpdate, counter);
		}

		if (this.bidHistory.size() <= this.maximumBidsStored) {
			this.updateStatistics(bidToUpdate, false, domain);
		}
	}

	/**
	 * This function updates the statistics of the bids that were received from
	 * the opponent.
	 */
	private void updateStatistics(Bid bidToUpdate, boolean toRemove,
			Domain domain) {
		try {
			List<Issue> issues = domain.getIssues();

			// counters for each type of the issues
			int realIndex = 0;
			int discreteIndex = 0;
			int integerIndex = 0;
			for (Issue lIssue : issues) {
				int issueNum = lIssue.getNumber();
				Value v = bidToUpdate.getValue(issueNum);
				switch (lIssue.getType()) {
				case DISCRETE:
					if (opponentBidsStatisticsDiscrete == null) {
						System.out
								.println("opponentBidsStatisticsDiscrete is NULL");
					} else if (opponentBidsStatisticsDiscrete
							.get(discreteIndex) != null) {
						int counterPerValue = opponentBidsStatisticsDiscrete
								.get(discreteIndex).get(v);
						if (toRemove) {
							counterPerValue--;
						} else {
							counterPerValue++;
						}
						opponentBidsStatisticsDiscrete.get(discreteIndex).put(
								v, counterPerValue);
					}
					discreteIndex++;
					break;

				case REAL:

					IssueReal lIssueReal = (IssueReal) lIssue;
					int lNumOfPossibleRealValues = lIssueReal
							.getNumberOfDiscretizationSteps();
					double lOneStep = (lIssueReal.getUpperBound() - lIssueReal
							.getLowerBound()) / lNumOfPossibleRealValues;
					double first = lIssueReal.getLowerBound();
					double last = lIssueReal.getLowerBound() + lOneStep;
					double valueReal = ((ValueReal) v).getValue();
					boolean found = false;

					for (int i = 0; !found
							&& i < opponentBidsStatisticsForReal.get(realIndex)
									.size(); i++) {
						if (valueReal >= first && valueReal <= last) {
							int countPerValue = opponentBidsStatisticsForReal
									.get(realIndex).get(i);
							if (toRemove) {
								countPerValue--;
							} else {
								countPerValue++;
							}

							opponentBidsStatisticsForReal.get(realIndex).set(i,
									countPerValue);
							found = true;
						}
						first = last;
						last = last + lOneStep;
					}
					// If no matching value was found, update the last cell
					if (found == false) {
						int i = opponentBidsStatisticsForReal.get(realIndex)
								.size() - 1;
						int countPerValue = opponentBidsStatisticsForReal.get(
								realIndex).get(i);
						if (toRemove) {
							countPerValue--;
						} else {
							countPerValue++;
						}

						opponentBidsStatisticsForReal.get(realIndex).set(i,
								countPerValue);
					}
					realIndex++;
					break;

				case INTEGER:

					IssueInteger lIssueInteger = (IssueInteger) lIssue;
					int valueInteger = ((ValueInteger) v).getValue();

					int valueIndex = valueInteger
							- lIssueInteger.getLowerBound(); // For ex.
																// LowerBound
																// index is 0,
																// and the lower
																// bound is 2,
																// the value is
																// 4, so the
																// index of 4
																// would be 2
																// which is
																// exactly 4-2
					int countPerValue = opponentBidsStatisticsForInteger.get(
							integerIndex).get(valueIndex);
					if (toRemove) {
						countPerValue--;
					} else {
						countPerValue++;
					}
					opponentBidsStatisticsForInteger.get(integerIndex).set(
							valueIndex, countPerValue);
					integerIndex++;
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Exception in updateStatistics: "
					+ e.getMessage());
		}
	}

	// choose a bid which is optimal for the opponent among a set of candidate
	// bids.

	public Bid ChooseBid(List<Bid> candidateBids, Domain domain) {

		int upperSearchLimit = 200;// 100;

		int maxIndex = -1;
		Random ran = new Random();
		List<Issue> issues = domain.getIssues();
		int maxFrequency = 0;
		int realIndex = 0;
		int discreteIndex = 0;
		int integerIndex = 0;

		if (candidateBids.size() >= upperSearchLimit) {
			List<Bid> bids = new ArrayList<Bid>();
			for (int i = 0; i < upperSearchLimit; i++) {
				int issueIndex = ran.nextInt(candidateBids.size());
				bids.add(candidateBids.get(issueIndex));
			}
			candidateBids = bids;
		}

		// this whole block of code is to find the best bid
		try {

			for (int i = 0; i < candidateBids.size(); i++) {
				int maxValue = 0;
				realIndex = discreteIndex = integerIndex = 0;
				for (int j = 0; j < issues.size(); j++) {
					Value v = candidateBids.get(i).getValue(
							issues.get(j).getNumber());
					switch (issues.get(j).getType()) {
					case DISCRETE:
						if (opponentBidsStatisticsDiscrete.get(discreteIndex) != null) {
							int counterPerValue = opponentBidsStatisticsDiscrete
									.get(discreteIndex).get(v);
							maxValue += counterPerValue;
						}
						discreteIndex++;
						break;
					case REAL:
						IssueReal lIssueReal = (IssueReal) issues.get(j);
						int lNumOfPossibleRealValues = lIssueReal
								.getNumberOfDiscretizationSteps();
						double lOneStep = (lIssueReal.getUpperBound() - lIssueReal
								.getLowerBound()) / lNumOfPossibleRealValues;
						double first = lIssueReal.getLowerBound();
						double last = lIssueReal.getLowerBound() + lOneStep;
						double valueReal = ((ValueReal) v).getValue();
						boolean found = false;
						for (int k = 0; !found
								&& k < opponentBidsStatisticsForReal.get(
										realIndex).size(); k++) {
							if (valueReal >= first && valueReal <= last) {
								int counterPerValue = opponentBidsStatisticsForReal
										.get(realIndex).get(k);
								maxValue += counterPerValue;
								found = true;
							}
							first = last;
							last = last + lOneStep;
						}
						if (found == false) {
							int k = opponentBidsStatisticsForReal
									.get(realIndex).size() - 1;
							int counterPerValue = opponentBidsStatisticsForReal
									.get(realIndex).get(k);
							maxValue += counterPerValue;
						}
						realIndex++;
						break;

					case INTEGER:
						IssueInteger lIssueInteger = (IssueInteger) issues
								.get(j);
						int valueInteger = ((ValueInteger) v).getValue();
						int valueIndex = valueInteger
								- lIssueInteger.getLowerBound(); // For ex.
																	// LowerBound
																	// index is
																	// 0, and
																	// the lower
																	// bound is
																	// 2, the
																	// value is
																	// 4, so the
																	// index of
																	// 4 would
																	// be 2
																	// which is
																	// exactly
																	// 4-2
						int counterPerValue = opponentBidsStatisticsForInteger
								.get(integerIndex).get(valueIndex);
						maxValue += counterPerValue;
						integerIndex++;
						break;
					}
				}
				if (maxValue > maxFrequency) {// choose the bid with the maximum
												// maxValue
					maxFrequency = maxValue;
					maxIndex = i;
				} else if (maxValue == maxFrequency) {// random exploration
					if (ran.nextDouble() < 0.5) {
						maxFrequency = maxValue;
						maxIndex = i;
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Exception in choosing a bid");
			System.out.println(e.getMessage() + "---" + discreteIndex);
		}
		if (maxIndex == -1) {
			return candidateBids.get(ran.nextInt(candidateBids.size()));
		} else {
			// here we adopt the random exploration mechanism
			if (ran.nextDouble() < 0.95) {
				return candidateBids.get(maxIndex);
			} else {
				return candidateBids.get(ran.nextInt(candidateBids.size()));
			}
		}
	}

	/*
	 * return the best bid from the opponent's bidding history
	 */

	public Bid chooseBestFromHistory(AdditiveUtilitySpace utilitySpace) {
		double max = -1;
		Bid maxBid = null;
		try {
			for (Bid bid : bidHistory) {
				if (max < utilitySpace.getUtility(bid)) {
					max = utilitySpace.getUtility(bid);
					maxBid = bid;
				}
			}
		} catch (Exception e) {
			System.out.println("ChooseBestfromhistory exception");
		}
		return maxBid;
	}

	// one way to predict the concession degree of the opponent
	public double concedeDegree(AdditiveUtilitySpace utilitySpace) {
		int numOfBids = bidHistory.size();
		HashMap<Bid, Integer> bidCounter = new HashMap<Bid, Integer>();
		try {
			for (int i = 0; i < numOfBids; i++) {

				if (bidCounter.get(bidHistory.get(i)) == null) {
					bidCounter.put(bidHistory.get(i), 1);
				} else {
					int counter = bidCounter.get(bidHistory.get(i));
					counter++;
					bidCounter.put(bidHistory.get(i), counter);
				}
			}
		} catch (Exception e) {
			System.out.println("ChooseBestfromhistory exception");
		}
		// System.out.println("the opponent's toughness degree is " +
		// bidCounter.size() + " divided by " +
		// utilitySpace.getDomain().getNumberOfPossibleBids());
		return ((double) bidCounter.size() / utilitySpace.getDomain()
				.getNumberOfPossibleBids());
	}

	public int getSize() {
		int numOfBids = bidHistory.size();
		HashMap<Bid, Integer> bidCounter = new HashMap<Bid, Integer>();
		try {
			for (int i = 0; i < numOfBids; i++) {

				if (bidCounter.get(bidHistory.get(i)) == null) {
					bidCounter.put(bidHistory.get(i), 1);
				} else {
					int counter = bidCounter.get(bidHistory.get(i));
					counter++;
					bidCounter.put(bidHistory.get(i), counter);
				}
			}
		} catch (Exception e) {
			System.out.println("getSize exception");
		}
		return bidCounter.size();
	}

	// Another way to predict the opponent's concession degree
	public double getConcessionDegree() {
		int numOfBids = bidHistory.size();
		double numOfDistinctBid = 0;
		int historyLength = 10;
		double concessionDegree = 0;
		// HashMap<Bid, Integer> bidCounter = new HashMap<Bid, Integer>();
		if (numOfBids - historyLength > 0) {
			try {
				for (int j = numOfBids - historyLength; j < numOfBids; j++) {
					if (bidCounter.get(bidHistory.get(j)) == 1) {
						numOfDistinctBid++;
					}
				}
				concessionDegree = Math
						.pow(numOfDistinctBid / historyLength, 2);
			} catch (Exception e) {
				System.out.println("getConcessionDegree exception");
			}
		} else {
			numOfDistinctBid = this.getSize();
			concessionDegree = Math.pow(numOfDistinctBid / historyLength, 2);
		}
		// System.out.println("the history length is" + bidHistory.size() +
		// "concessiondegree is " + concessionDegree);
		return concessionDegree;
	}

	public Bid getLastOppBid() {
		return bidHistory.get(bidHistory.size() - 1);
	}
}

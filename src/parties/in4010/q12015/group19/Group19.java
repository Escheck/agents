package parties.in4010.q12015.group19;

import java.util.ArrayList;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;

/**
 * Group19 negotiation Agent version 0.2 - 23-10-15 D2 assignment
 */
public class Group19 extends AbstractNegotiationParty {

	private Action lastOfferedBidAction = null; // the last opponent offer
												// action (not Accept)
	private static double MINIMUM_BID_UTILITY = 0.0; // minimum acceptable
														// utility (MAU)
	private static double DISC_SENSITIVITY = 1; // Discount factor sensitivity
												// (larger->faster convergence
												// to mean)
	private static int RAND_BID_AMOUNT = 30; // amount of randomly generated
												// bids to compare for closest
												// to mean
	private static double MEAN_POW_COEFF = 1.25; // power coefficient of mean,
													// for minutil calculation
	private static double MIN_UTIL_BOUND = 0.4; // minimal boundary utility
												// limit

	private int issueN = 0; // number of issues
	private int partiesN = 0; // number of parties
	private int lastOffsteps = 0; // number of steps since last offer
	private int bidNum = -1; // negotiation counter, updates for every received
								// message and action chosen
	private double lowBoundUtil = 0.95; // minimum utility boundary used for
										// acceptance and offering
	private double discMeanUtil = 0.95; // discounted mean utility value of
										// opponent bids
	private ArrayList<BidHistory> BidHist; // Bid History array of all parties
	private ArrayList<ArrayList<Integer>> IssueAmounts = new ArrayList<ArrayList<Integer>>(); // counter
																								// of
																								// total
																								// offered
																								// issue
																								// arguments
	private ArrayList<ArrayList<String>> IssueNames = new ArrayList<ArrayList<String>>(); // list
																							// of
																							// issue
																							// arguments
	private ArrayList<ArrayList<Double>> normIssueVals = new ArrayList<ArrayList<Double>>(); // normalised
																								// issue
																								// amounts

	private void initfunc() throws Exception {
		partiesN = getNumberOfParties();
		MINIMUM_BID_UTILITY = utilitySpace.getReservationValueUndiscounted();
		/* Opponent model initialisation */
		issueN = getRandBid(0).getIssues().size();
		/* Setup BidHistory structure */
		BidHist = new ArrayList<BidHistory>();
		for (int i = 0; i < partiesN; i++) {
			BidHist.add(new BidHistory());
		}

		/* Setup IssueAmounts structure */
		for (int k = 0; k < issueN; k++) {
			IssueAmounts.add(new ArrayList<Integer>());
			IssueNames.add(new ArrayList<String>());
			normIssueVals.add(new ArrayList<Double>());
		}
	}

	/*
	 * chooseAction method; needs to always return a valid action (Accept or
	 * counter offer)
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		if (bidNum == -1) {
			try {
				initfunc();
			} catch (Exception e) {
				System.out.println("!!!initialization failed!!!");
				e.printStackTrace();
			}
		}
		bidNum = bidNum + 1;
		double OppBidUtil = 0;
		/* get Utility value of last opponent action */
		if (DefaultAction.getBidFromAction(lastOfferedBidAction) != null) {
			Bid OppBid = DefaultAction.getBidFromAction(lastOfferedBidAction);
			OppBidUtil = getUtility(OppBid);
		} else {
			OppBidUtil = 0;
		}
		/* should we accept? (checked with isAcceptable) */
		try {
			/*
			 * accept if the opponent offer is better than our acceptance value
			 * (still calculate new acceptance value)
			 */
			if (validActions.contains(Accept.class) && isAcceptable(OppBidUtil)) {
				lastOffsteps = lastOffsteps + 1;
				Bid lastBid = BidHist.get((bidNum - lastOffsteps) % partiesN)
						.getLastBid();
				BH_update(bidNum % partiesN, lastBid);

				return new Accept(getPartyId(), lastBid);
			} else {
				/* otherwise give new offer */
				lastOffsteps = 0;
				return new Offer(getPartyId(), getBid());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("unable to check acceptance");
			return new Offer(getPartyId(), getRandBid(lowBoundUtil));
		}
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		// if just started, initialise group19
		if (bidNum == -1) {
			try {
				initfunc();
			} catch (Exception e) {
				System.out.println("!!!initialization failed!!!");
				e.printStackTrace();
			}
		}
		bidNum = bidNum + 1;
		Bid lastBid = null;
		/* store action as variable and add it to the bid history */
		if (DefaultAction.getBidFromAction(action) != null) {

			lastOffsteps = 0;
			lastOfferedBidAction = action;
			lastBid = DefaultAction.getBidFromAction(lastOfferedBidAction);
			BH_update((bidNum % partiesN), lastBid);
		} else {
			lastOffsteps = lastOffsteps + 1;
			/* add last made offer to the bid history */
			lastBid = BidHist.get(
					((bidNum + partiesN) - lastOffsteps) % partiesN)
					.getLastBid();
			BH_update(bidNum % partiesN, lastBid);
		}
		if (lastBid != null) {
			if (Math.floor(bidNum / partiesN) > 0) {
				//
				lowBoundUtil_update(getUtility(lastBid));
			}
			try {
				// update offered issues arguments counter
				Counter_update(lastBid);
			} catch (Exception e) {
				System.out.println("-- ERROR -- unable to update mean");
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Group 19 - Grandma";
	}

	/*
	 * Checks if the utility of the offered bid is higher than the minimum
	 * accepted (reservation value) and if it is higher than the Lower Boundary
	 * Utility
	 */
	private boolean isAcceptable(double offered) {
		if (offered > lowBoundUtil && offered > MINIMUM_BID_UTILITY) {
			return true;
		} else {
			return false;
		}
	}

	/* private function to get a new valid Bid */
	private Bid getRandBid(double minUtil) {
		Bid newbid;
		int loopcheck = 0;
		do {
			loopcheck++;
			newbid = generateRandomBid();
		} while (getUtility(newbid) < minUtil && loopcheck < 100000);
		return newbid;
	}

	/* --- update Bid History of opponent oppID with bid newBid --- */
	private void BH_update(int oppID, Bid newBid) {
		if (oppID > -1 && oppID < partiesN) {
			BidDetails tempBD = new BidDetails(newBid, getUtility(newBid));
			BidHist.get(oppID).add(tempBD);
		}
	}

	/*
	 * Update counter of offered issue arguments used to calculate the proximity
	 * of eventual bids to the issues most offered by the opponents
	 */
	private void Counter_update(Bid recBid) throws Exception {
		for (int k = 0; k < issueN; k++) {
			boolean found = false;
			if (IssueNames.get(k).isEmpty()) {
				IssueNames.get(k).add(recBid.getValue(k + 1).toString());
				IssueAmounts.get(k).add(1);
				found = true;
			} else {
				// for all existing IssueNames
				for (int j = 0; j < IssueNames.get(k).size(); j++)
					/*
					 * compare bid issue value (ex. "Beer") to existing names in
					 * IssueNames, if it does add one to IssueValues, otherwise
					 * create new IssueNames name and IssueValues value, and add
					 * 1 to it
					 */
					if (recBid.getValue(k + 1).toString()
							.equals(IssueNames.get(k).get(j))) {
						int newval = IssueAmounts.get(k).get(j) + 1;
						ArrayList<Integer> newArr = IssueAmounts.get(k);
						newArr.set(j, newval);
						IssueAmounts.set(k, newArr);
						found = true;
					}
			}
			if (!found) {
				IssueNames.get(k).add(recBid.getValue(k + 1).toString());
				IssueAmounts.get(k).add(1);
			}
		}
		/*
		 * uncomment to display issue arguments count
		 * System.out.println(IssueNames); System.out.println(IssueAmounts);
		 */
	}

	/* calculates normalised issue mean amounts */
	private void normaliseMean() {
		for (int i = 0; i < IssueAmounts.size(); i++) {
			double tot = 0;
			for (int k = 0; k < IssueAmounts.get(i).size(); k++) {
				tot = tot + IssueAmounts.get(i).get(k);
			}
			normIssueVals.get(i).clear();
			ArrayList<Double> issValList = new ArrayList<Double>();
			for (int j = 0; j < IssueAmounts.get(i).size(); j++) {
				issValList.add(IssueAmounts.get(i).get(j) / tot);
			}
			normIssueVals.set(i, issValList);
		}
	}

	/*
	 * Offering strategy get minimum utility value (boundary) -> generate n
	 * random bids above boundary -> -> calculate their proximity to opponents
	 * offering mean -> return bid with highest value
	 */
	private Bid getBid() {
		// set min utility
		double minUt = lowBoundUtil;
		double maxmeanUt = 0;
		Bid finalBid = new Bid(getUtilitySpace().getDomain());
		// if already in 2nd round
		if (Math.floor(bidNum / partiesN) > 0) {
			ArrayList<Bid> BidArr = new ArrayList<Bid>();
			ArrayList<Double> proxArr = new ArrayList<Double>();
			// normalise mean values
			normaliseMean();
			// generate random bids
			for (int i = 0; i < RAND_BID_AMOUNT; i++) {
				BidArr.add(getRandBid(minUt));
				try {
					// calculate proximity value to the opponents offering mean
					proxArr.add(getProx(BidArr.get(i)));
				} catch (Exception e) {
					System.out.println("Unable to calculate mean utility");
					e.printStackTrace();
				}
			}
			// find best proximity value
			for (int p = 0; p < proxArr.size(); p++) {
				if (proxArr.get(p) > maxmeanUt) {
					maxmeanUt = proxArr.get(p);
				}
			}
			// return bid with maximum proximity
			finalBid = BidArr.get(proxArr.indexOf(maxmeanUt));
		} else {
			finalBid = getRandBid(minUt);
		}
		return finalBid;
	}

	/*
	 * calculate the proximity of a bid to the normalised mean point bids with
	 * issue values equal to the most offered ones get higher scores this
	 * results in them being preferred to those with lower values
	 */
	private double getProx(Bid bid) throws Exception {
		double proxVal = 0;
		for (int i = 0; i < IssueNames.size(); i++) {
			for (int k = 0; k < IssueNames.get(i).size(); k++) {
				if (bid.getValue(i + 1).toString()
						.equals(IssueNames.get(i).get(k))) {
					proxVal = proxVal + normIssueVals.get(i).get(k);
				}
			}
		}
		return proxVal;
	}

	/*
	 * Update the Lower Boundary Utility the utility calculated is dependent on
	 * the discounted mean utility and on a time factor (see report)
	 */
	private void lowBoundUtil_update(double lastUt) {
		double time = timeline.getTime();
		// discount factor for discounted mean utility (should stay constant
		// trough negotiation)
		double discFact = DISC_SENSITIVITY
				/ ((bidNum * (partiesN - 1)) / (partiesN * time));
		discMeanUtil = (1 - discFact) * discMeanUtil + discFact * lastUt;
		double tfact = 1 - Math.pow(time, 30);
		// System.out.println("time factor:"+tfact);
		lowBoundUtil = (MIN_UTIL_BOUND + (1 - MIN_UTIL_BOUND)
				* Math.pow(discMeanUtil, MEAN_POW_COEFF))
				* tfact;
		// System.out.println("min utility:"+lowBoundUtil);
	}
}

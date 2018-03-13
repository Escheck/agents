package parties.in4010.q12015.group15;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.Actions;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.protocol.DefaultMultilateralProtocol;
import negotiator.protocol.StackedAlternatingOffersProtocol;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Negotiation party for Group15. Group members: Fabio Izzi, Kristin Fjola
 * Tomasdottir, Valentina Bollini
 * 
 * The bidding strategy is a version of time dependent concession with trade off
 * using opponent modeling.
 * 
 * We look at all offers in a certain range of utility. That range starts high
 * (0.95 - 1) and lowers with time. First 2/3 of the time we choose an offer
 * from that range with the lowest combined Hamming distance to all opponents
 * based on their last bid. Last 1/3 of the time we choose an offer from that
 * range using opponent modeling.
 */
public class Group15 extends AbstractNegotiationParty {

	private double MIN_UTILITY_TO_OFFER;
	private double OFFER_GROUP_SIZE;
	private double TIME_TO_SWITCH_TO_OMS;
	private double TIME_TO_LOWER_MIN_UTILITY;

	// Bidding strategy
	private Bid lastBid;
	private List<Bid> bidsOffered = new ArrayList<>();
	private SortedOutcomeSpace outcomeSpace;
	private double lastTimeMinUtilityWasLowered;
	private boolean isUsingOMS;
	private int rounds = 0;

	// BOA elements
	private OpponentModel om;
	private OMStrategy oms;
	private AcceptanceStrategy as;

	// Time measurements
	long ourStartTime = 0;
	long roundStartTime = 0;
	double timeCount = 0.0;
	double ourTotalTime = 0;
	double roundTotalTime = 0;

	public Group15() {
	}

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		outcomeSpace = new SortedOutcomeSpace(info.getUtilitySpace());
		outcomeSpace.generateAllBids(info.getUtilitySpace());

		MIN_UTILITY_TO_OFFER = 0.95; // Start offering the highest utility for
										// us
		OFFER_GROUP_SIZE = 0.05; // Look at offers in a range of max-min=0.05
		TIME_TO_SWITCH_TO_OMS = 2 / 3.0; // After 2/3 of the time we are
											// confident in our OM
		TIME_TO_LOWER_MIN_UTILITY = (2 / 3.0) / 6.0; // 6 steps of lowering
														// minUtil by 0.05 to
														// get to utility 0.7
		lastTimeMinUtilityWasLowered = timeline.getTime(); // Keep track of when
															// to lower min
															// utility (concede)
		isUsingOMS = false; // Start by not using OMS

		// BOA elements
		om = new OpponentModel((AdditiveUtilitySpace) info.getUtilitySpace());
		as = new AcceptanceStrategy((AdditiveUtilitySpace) info.getUtilitySpace());
		oms = new OMStrategy(om, (AdditiveUtilitySpace) info.getUtilitySpace());
	}

	/**
	 * @param validActions
	 *            : Either a list containing both accept and offer or only
	 *            offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		System.out.println("---- BS Choose action ---");
		rounds++;
		System.out.println("Round: " + rounds);
		double roundTime = measureAverageRoundTime();

		// Accept if offer is good enough according to our AS
		if (validActions.contains(Accept.class)
				&& as.determineAcceptability(lastBid, timeline, roundTime).equals(Actions.Accept)) {
			return new Accept(getPartyId(), lastBid);
		}

		// If we're the first one to make an offer
		if (!validActions.contains(Accept.class)) {
			try {
				return getFirstBid();
			} catch (Exception e) {
				System.out.println("Could not generate first bid");
				e.printStackTrace();
			}
		}

		// Concede to lower minimum utility if a certain time has passed
		if ((timeline.getTime() - lastTimeMinUtilityWasLowered) >= TIME_TO_LOWER_MIN_UTILITY) {
			lowerMinUtility();
		}

		List<BidDetails> possibleBids = getPossibleBids();
		Bid bestBid = null;

		// Switch to using OMS when we're confident in the OM
		if (!isUsingOMS && timeline.getTime() >= TIME_TO_SWITCH_TO_OMS) {
			switchToOms();
		}

		// First we use Hamming Distance, then Opponent Modeling
		if (!isUsingOMS) {
			bestBid = getBidWithSmallestHammingDistance(possibleBids);
		} else {
			bestBid = oms.getBestBid(possibleBids);
		}

		// If something goes wrong in the bidding strategy or if we already
		// offered all bids in the interval
		// we offer a random bid within our utility demand
		if (bestBid == null) {
			bestBid = getRandomBidInInterval(MIN_UTILITY_TO_OFFER, MIN_UTILITY_TO_OFFER + OFFER_GROUP_SIZE);
		}

		System.out.println("---- BS Choose action ---");
		System.out.println("My BID offered: " + bestBid);
		try {
			System.out.println("My BID Util: " + utilitySpace.getUtility(bestBid));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Offer(getPartyId(), bestBid);
	}

	/**
	 * Lowers the minimum utility that we want to offer by a constant
	 */
	public void lowerMinUtility() {
		MIN_UTILITY_TO_OFFER -= OFFER_GROUP_SIZE;
		lastTimeMinUtilityWasLowered = timeline.getTime();
		System.out.println("Lowering MIN_UTILITY_TO_OFFER to: " + MIN_UTILITY_TO_OFFER + " in round: " + rounds);
	}

	/**
	 * @return the bid we want to offer when we're the first one to make a bid
	 * @throws Exception
	 */
	public Action getFirstBid() throws Exception {
		// As our first bid we want to offer our best bid possible
		return new Offer(getPartyId(), utilitySpace.getMaxUtilityBid());
	}

	/**
	 * @return all possible bids in the range of our min utility + constant
	 */
	public List<BidDetails> getPossibleBids() {
		Range range = new Range(MIN_UTILITY_TO_OFFER, MIN_UTILITY_TO_OFFER + OFFER_GROUP_SIZE);
		List<BidDetails> possibleBids = outcomeSpace.getBidsinRange(range);
		return possibleBids;
	}

	/**
	 * Switches from using Hamming distance to choose a bid to use our opponent
	 * modeling instead
	 */
	private void switchToOms() {
		isUsingOMS = true;
		MIN_UTILITY_TO_OFFER = 0.95; // We might choose other offers than before
		TIME_TO_LOWER_MIN_UTILITY = (1 / 3.0) / 6.0; // Only 1/3 of the time is
														// left and we want to
														// concede to utility
														// 0.7
		lastTimeMinUtilityWasLowered = timeline.getTime();
		System.out.println("CHANGING TO OMS in round: " + rounds);
		System.out.println("Setting min utility to: " + MIN_UTILITY_TO_OFFER);
	}

	/**
	 * @param all
	 *            possible bids in some range
	 * @return bid with smallest sum of Hamming distance to all opponents from
	 *         the list of bids
	 */
	private Bid getBidWithSmallestHammingDistance(List<BidDetails> bids) {
		System.out.println("---- BS Hamming ----");
		Bid bestBid = null; // If it doesn't succeed to calc Hamming distance
		int smallestDist = Integer.MAX_VALUE;

		for (BidDetails b : bids) {
			if (bidsOffered.contains(b.getBid())) { // Don't offer same bid
													// twice
				System.out.println("Has offered bid before: " + b.getBid());
				continue;
			}
			int totalDist = 0;

			for (String opponent : om.getOpponents()) {
				totalDist += getHammingDistance(b.getBid(), om.getLastBidFromAgent(opponent));
			}

			if (totalDist < smallestDist) {
				smallestDist = totalDist;
				bestBid = b.getBid();
			}
		}
		System.out.println("Smallest dist found: " + smallestDist);

		bidsOffered.add(bestBid);
		return bestBid;
	}

	/**
	 * @param bid1
	 * @param bid2
	 * @return Hamming distance between bid1 and bid2
	 */
	private int getHammingDistance(Bid bid1, Bid bid2) {
		int dist = 0;
		List<Issue> issues = bid1.getIssues();
		for (Issue issue : issues) {
			int indexOfIssue = issues.indexOf(issue) + 1;
			try {
				if (!bid1.getValue(indexOfIssue).equals(bid2.getValue(indexOfIssue))) {
					dist++;
				}
			} catch (Exception e) {
				System.out.println("BS Could not calculate Hamming distance between " + bid1 + " and " + bid2);
				e.printStackTrace();
			}
		}

		return dist;
	}

	/**
	 * @return average time in seconds for each round
	 */
	public double measureAverageRoundTime() {
		timeCount++;
		double roundAvgTime = 0;
		if (roundStartTime != 0) {
			long roundEndTime = System.nanoTime();
			long roundTime = roundEndTime - roundStartTime;
			double roundTimeSeconds = (double) roundTime / 1000000000.0;
			roundTotalTime += roundTimeSeconds;
			roundAvgTime = (roundTotalTime / (timeCount - 1));
		}
		ourStartTime = System.nanoTime();
		roundStartTime = System.nanoTime();

		return roundAvgTime;
	}

	/**
	 * @param min
	 *            utility wanted
	 * @param max
	 *            utility wanted
	 * @return random bid with utility between min and max
	 */
	private Bid getRandomBidInInterval(double min, double max) {
		System.out.println("WARNING: Offering random bid in range: " + min + " - " + max);
		Random rand = new Random();
		double randomUtility = min + (max - min) * rand.nextDouble();
		BidDetails bidDetails = outcomeSpace.getBidNearUtility(randomUtility);
		return bidDetails.getBid();
	}

	/**
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		System.out.println("--- BS receive msg ---");
		super.receiveMessage(sender, action);
		System.out
				.println("Receive " + action.getClass() + " at time: " + timeline.getTime() + " from agent " + sender);
		if (action instanceof Offer && sender != null) {
			lastBid = DefaultAction.getBidFromAction(action);
			om.updateModel(sender.toString(), lastBid, rounds);
		}
	}

	@Override
	public String getDescription() {
		return "Multi Party Negotiation Agent Group 15";
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return StackedAlternatingOffersProtocol.class;
	}

}
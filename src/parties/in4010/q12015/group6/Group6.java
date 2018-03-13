package parties.in4010.q12015.group6;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;

/**
 * This is your negotiation party.
 */
public class Group6 extends AbstractNegotiationParty {
	// Make data available throughout the code
	private Bid opponentbestbid = null;
	private Bid myBid;
	private Boolean firstround = true;
	private Bid bestBid = null;
	private double utilBestbid = 0D;
	private Bid partnerBid;
	private double offeredUtilFromOpponent = 0;
	private TreeMap<Double, Bid> OpponentModel = new TreeMap<Double, Bid>();
	private double oppUtil;

	private Action actionOfPartner = null;
	private Bid oppBid;

	/**
	 * init is called when a next session starts with the same opponent.
	 */
	// Initially we initialized our opponentModel here but this gave a
	// nullpointerexception.
	public void init() {

	}

	// Receive the incoming action of the other agents.
	public void receiveMessage(java.lang.Object sender, Action opponentAction) {
		// actionOfPartner is defined in the beginning of the code and is
		// defined as private
		// Meaning that other parts of the code can access the opponentAction.
		actionOfPartner = opponentAction;

		// The opponent model is only update if the action of partner is an
		// offer.
		if (actionOfPartner instanceof Offer) {
			oppBid = ((Offer) actionOfPartner).getBid();
			oppUtil = getUtility(oppBid);
			// The current offer is inserted into the opponent model
			updateOpponentModel(oppUtil, oppBid);
		}
		// System.out.println("Size: "+ OpponentModel.size());
	}

	// The Action that is send to genius based on the opponentAction is
	// generated here.
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		// The Utility threshold is retrieved, this will be used to choose
		// whether to accept or make a counter bid.
		double minUtil = getNewThreshold();
		Action action = null;
		try {
			if (validActions.contains(Accept.class)) {
				// if action of partner is instance of accept.
				// decide to accept or offer.

				// System.out.println("Minimum Util: " + minUtil);

				// If the offered utility of the opponent is higher than the
				// utility of
				// our threshold a Accept will be returned.
				if (oppUtil > minUtil) {
					return new Accept(getPartyId(), oppBid);
				}

				// A bid will be retrieved from our bidselector.
				action = chooseRandomBidAction(minUtil);

				Bid myBid = ((Offer) action).getBid();
				double myOfferedUtil = getUtility(myBid);
				System.out.println("Generated bid: " + myOfferedUtil);

				// A set of all the opponent best bids that have been stored are
				// generated.
				Set<Double> keys = OpponentModel.keySet();

				// A random number is generated to
				// randomly select a bid from the opponentModel.
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(5) + 1;
				int keyId = 1;

				// The key of a bid is given an ID so that
				// using the randomInt a bid can be retrieved from
				// the opponentModel.
				for (Double key : keys) {
					if (keyId == randomInt) {
						bestBid = OpponentModel.get(key);
					}
					keyId++;
				}

				// The utility of the retrieved bid is generated here
				utilBestbid = getUtility(bestBid);
				System.out.println("Best bid: " + utilBestbid);

				// Based on the utility of the bestBid that was retrieved
				// a offer will be made. If the utility of the best bid is
				// higher than the offered utility of the opponent a new
				// bid will be placed.
				if (myOfferedUtil < utilBestbid) {
					// System.out.println("Util: " + utilBestbid + " Bid: " +
					// bestBid);
					action = new Offer(getPartyId(), bestBid);
				}
			}

			if (!validActions.contains(Accept.class))
			// if action of partner is not instance of accept
			// choose to offer a counter bid.
			{
				action = chooseRandomBidAction(minUtil);

			}

			// If anything goes wrong
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			action = new Accept(getPartyId(), oppBid); // best guess if things
														// go wrong.
		}
		return action;
	}

	// This is our Bid selector which choose a bid based on
	// a given floor entry. The bid selector works by randomly
	// generating bids. As soon as a bid goes above the utility
	// Threshold the bid will be returned.
	private Action chooseRandomBidAction(Double minUtil) {
		Bid nextBid = null;
		// generate the random bids.
		nextBid = generateRandomBid();
		double util = getUtility(nextBid);

		System.out.println("Minimum Util: " + minUtil);

		// The a new bid will be generated if the utility of
		// the previous random bid is bellow the threshold.
		// Once the generated bid goes above the threshold
		// the nextBid becomes myBid.
		while (util < minUtil) {
			nextBid = generateRandomBid();
			util = getUtility(nextBid);
			System.out.println("Util: " + util);
		}
		// This returns accept if anything goes wrong.
		if (nextBid == null)
			return (new Accept(getPartyId(), oppBid));

		// This returns myBid as an offer.
		return (new Offer(getPartyId(), nextBid));
	}

	// The threshold is generated using a time based
	// method. Based on the time the negotiation has
	// been running a threshold is calculated.
	private double getNewThreshold() {
		double utilThreshold = 0.0;
		double time = timeline.getTime(); // retrieve the elapsed time
		if (time <= (60.0 / 180.0)) {
			utilThreshold = 0.85 + 0.1 * time; // Generate an increasing
												// threshold, starting at 0.85
		}
		if (time <= (140.0 / 180.0) && time >= (60.0 / 180.0)) {
			utilThreshold = 0.85; // The threshold remains steady
		}
		if (time > (140.0 / 180.0) && time < (160.0 / 180.0)) {
			utilThreshold = 1.1 - 0.38 * time; // utilThreshold starts at 0.80
												// and decreases to 0.74
		}
		if (time > (160.0 / 180.0)) {
			utilThreshold = 1.18 - 0.5 * time; // utilThreshold starts at 0.72
												// and decreases to 0.65
		}
		// System.out.println("Time: " + time);
		// System.out.println("Util: " + utilThreshold);
		// Return the calculated threshold
		return utilThreshold;
	}

	// The opponentModel works by placing the opponent bids in order.
	// A max of 5 bids are stored, meaning that the lowest bid will be discarded
	// if the amount of bids exceeds the maximum amount of 5.
	public TreeMap<Double, Bid> updateOpponentModel(double OfferedUtil, Bid OfferedBid) {
		// If the max size is below 5 or the offered utility is above the lowest
		// utility
		// update the opponentmodel.
		if (OpponentModel.size() < 5 || OpponentModel.firstEntry().getKey() < OfferedUtil) {
			// if oppentmodel size is already 5 remove the lowest bid and add
			// the new higher bid.
			if (OpponentModel.size() == 5) {
				// System.out.println("Removed Opponent Model utility:"
				// +OpponentModel.firstEntry().getKey());
				OpponentModel.remove(OpponentModel.firstEntry().getKey());
			}
			// Add the new bid to the opponentmodel
			OpponentModel.put(OfferedUtil, OfferedBid);
			// System.out.println("With utility:" + OfferedUtil);
		}
		// System.out.println("check size: "+ OpponentModel.size());
		return OpponentModel;
	}

	@Override
	public String getDescription() {
		return "in4010.q12015.group6";
	}
}

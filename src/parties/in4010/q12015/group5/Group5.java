package parties.in4010.q12015.group5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * NegotiationParty for Group 5. This agent moves closer to the estimated Nash
 * point as time progresses. For an extended explanation of what our agent does,
 * see our report.
 */
public class Group5 extends AbstractNegotiationParty {

	/**
	 * All possible bids from the SortedOutcomeSpace.
	 */
	private List<BidDetails> allOutcomes;

	/**
	 * The last bid that has been made by any agent (including us).
	 */
	private Bid lastBid;

	/**
	 * The models that we build for every opponent.
	 */
	private Map<Object, OpponentModel> opponentModels;

	/**
	 * The SortedOutcomeSpace of our UtilitySpace.
	 */
	private SortedOutcomeSpace sos;

	/**
	 * The threshold above which we will always accept.
	 */
	private double utilityThreshold;

	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.parties.AbstractNegotiationParty#init(negotiator.utility.
	 * UtilitySpace, negotiator.Deadline, negotiator.session.TimeLineInfo, long,
	 * negotiator.AgentID)
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		opponentModels = new HashMap<Object, OpponentModel>();
		sos = new SortedOutcomeSpace(info.getUtilitySpace());
		allOutcomes = sos.getAllOutcomes();

		double minBid = sos.getMinBidPossible().getMyUndiscountedUtil();
		utilityThreshold = 1 - (1 - minBid) * 0.1; // Get top 90%
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

		try {
			if (action instanceof Offer) {
				lastBid = ((Offer) action).getBid();
				updateModel(sender, lastBid);
			} else if (action instanceof Accept) {
				updateModel(sender, lastBid);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
			Bid bid = generateBid();
			if (acceptBid(bid))
				return new Accept(getPartyId(), lastBid);
			else {
				lastBid = bid;
				return new Offer(getPartyId(), new Bid(bid));
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Accepting is always
			// better than crashing
			return new Accept(getPartyId(), lastBid);
		}
	}

	/**
	 * Generates a new bid according to the bidding strategy. Finds a bid by
	 * looping through all bids and - Calculate the Nash score for the bid based
	 * on the opponent models. - Calculate own utility for the bid. - Take the
	 * weighted average of own utility and the Nash score. This weighted average
	 * starts at 100% own utility and moves to 100% theoretical Nash point. Of
	 * these the top 5 is chosen and from those the one with the highest utility
	 * is chosen.
	 * 
	 * @return The preferred bid based on the chosen strategy.
	 * @throws Exception
	 */
	private Bid generateBid() throws Exception {
		Double time = timeline.getTime();
		double ownFactor = 1 - (time * time); // 1 - x^2
		double nashFactor = 1 - ownFactor;

		// Get modeled utility spaces for opponents
		Set<Object> opponents = opponentModels.keySet();
		Map<Object, AdditiveUtilitySpace> opponentUtilSpaces = new HashMap<Object, AdditiveUtilitySpace>();
		for (Object opponent : opponents) {
			opponentUtilSpaces.put(opponent, opponentModels.get(opponent).getUtilitySpace());
		}

		// weightedAverages contains the top 5 weighted averages found
		Map<BidDetails, Double> weightedAverages = new HashMap<BidDetails, Double>();
		weightedAverages.put(null, 0.);
		for (BidDetails bid : allOutcomes) {
			// First find the minimum in order to know which one we should
			// replace if we find a better one.
			double min = Double.MAX_VALUE;
			BidDetails minBid = null;
			for (BidDetails mapBid : weightedAverages.keySet()) {
				double mapAvg = weightedAverages.get(mapBid);
				if (mapAvg <= min) {
					min = mapAvg;
					minBid = mapBid;
				}
			}

			// If based on our utility alone we won't make it, break because:
			// - Adding the opponents utilities to the nash will only decrease
			// the value (because U <= 1)
			// - The list is sorted in decreasing order so all next bids will be
			// worse.
			double ourUtil = bid.getMyUndiscountedUtil();
			if (ownFactor * ourUtil + nashFactor * ourUtil < min)
				break;

			double nash = ourUtil;
			for (Object opponent : opponents) {
				AdditiveUtilitySpace opponentUtilSpace = opponentUtilSpaces.get(opponent);
				nash *= opponentUtilSpace.getUtility(bid.getBid());
			}

			double weightedAverage = ownFactor * ourUtil + nashFactor * nash;
			if (weightedAverage > min) {
				if (weightedAverages.size() >= 5) {
					weightedAverages.remove(minBid);
				}

				weightedAverages.put(bid, weightedAverage);
			}
		}

		// Find the one that gives the highest utility of the top 5.
		double max = 0;
		BidDetails maxBid = null;
		for (BidDetails mapBid : weightedAverages.keySet()) {
			double mapUtil = mapBid.getMyUndiscountedUtil();
			if (mapUtil > max) {
				max = mapUtil;
				maxBid = mapBid;
			}
		}

		return maxBid.getBid();
	}

	// Accept when either:
	// - It is above our minimum
	// - It is better than we would bid
	// - (late in the bidding), it is better than all the bids in the previous
	// window
	/**
	 * Decides whether to accept the last bid based on the bid that was
	 * generated by the bidding strategy. Acceptance strategy is as follows.
	 * Accept when either: - The bid is above our threshold This is a linear
	 * function from the top 100% of the space to the top 80% of the space - The
	 * bid is better than the bid generated by the strategy. - When 0.95 of the
	 * negotiation has passed, accept if we don't expect a better bid. This is
	 * the case if the bid is better than all the bids we have seen in the
	 * previous time window.
	 * 
	 * @param bid
	 *            The bid generated by the bidding strategy.
	 * @return Whether to accept the current bid.
	 * @throws Exception
	 */
	private boolean acceptBid(Bid bid) throws Exception {
		double time = timeline.getTime();
		double minUtility = getUtilityThreshold(time / 0.5);
		double lastUtility = (lastBid != null) ? utilitySpace.getUtility(lastBid) : 0;
		double bidUtility = utilitySpace.getUtility(bid);
		if (lastUtility >= minUtility || lastUtility >= bidUtility)
			return true;

		if (time >= 0.95) {
			List<Bid> opponentBids = new ArrayList<Bid>();
			for (Object opponent : opponentModels.keySet()) {
				opponentBids.addAll(opponentModels.get(opponent).getBidHistory(1 - time));
			}
			double maxUtility = 0;
			for (Bid oppBid : opponentBids) {
				double utility = utilitySpace.getUtility(oppBid);
				if (utility > maxUtility) {
					maxUtility = utility;
				}
			}
			if (bidUtility >= maxUtility)
				return true;
		}

		return false;
	}

	/**
	 * Linear function from 1 to utilityThreshold, multiplied by the factor (to
	 * reach it sooner, for example)
	 * 
	 * @param factor
	 *            The factor by which to multiply
	 * @return The utilty above which we will always accept.
	 */
	private double getUtilityThreshold(double factor) {
		return 1 - (1 - utilityThreshold) * factor;
	}

	/**
	 * Updates the opponent model with the specified opponents new bid. If no
	 * model is yet present, one will be created and added to the list.
	 * 
	 * @param opponent
	 *            The opponent of which the model needs to be updated.
	 * @param newBid
	 *            The new bid of the opponent
	 * @throws Exception
	 */
	private void updateModel(AgentID opponent, Bid newBid) throws Exception {
		OpponentModel opponentModel = opponentModels.get(opponent);
		if (opponentModel == null) {
			opponentModel = new OpponentModel(utilitySpace.getDomain(), newBid);
			opponentModels.put(opponent, opponentModel);
		} else {
			opponentModel.updateModel(newBid);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see negotiator.parties.AbstractNegotiationParty#getDescription()
	 */
	@Override
	public String getDescription() {
		return "I heard you like parties so we created a party to negotiate about your party";
	}
}

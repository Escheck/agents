package parties.in4010.q12015.group17;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import misc.Range;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OfferingStrategy;
import negotiator.boaframework.SortedOutcomeSpace;

public class OfferStrat extends OfferingStrategy {

	/**
	 * This is the offer strategy class. Class variables.
	 */
	private SortedOutcomeSpace sos;
	private OpMod om;
	private List<BidDetails> bidsList;
	private Range bidRange = new Range(0.8, 1);
	private int RECENT = 5;

	/**
	 * Initialisation method for {@link NegotiationSession},
	 * {@link SortedOutcomeSpace}, {@link OpMod} and bidsList.
	 */
	public void init(NegotiationSession negotiationSession, SortedOutcomeSpace sos, OpMod om) {
		super.init(negotiationSession, new HashMap<String, Double>());
		this.sos = sos;
		this.om = om;
		this.bidsList = sos.getBidsinRange(bidRange);
		System.out.println("Offer strategy init has run!");
	}

	/**
	 * Determines bid for early negotiation, utility starts at one and decreases
	 * with a quadratic function.
	 * 
	 * @param relativeTime
	 *            between 0 and 0.2
	 * @return {@link BidDetails} near a specific utility
	 */
	private BidDetails determineEarlyBid(double relativeTime) {
		double utility = 1 - 2.5 * Math.pow(relativeTime, 2);
		return sos.getBidNearUtility(utility);
	}

	/**
	 * Determines bids for mid negotiation.
	 * 
	 * @return {@link BidDetails} with the highest group-based utility (within
	 *         range of 0.8-1.0 for own utility)
	 * @throws Exception
	 */
	private BidDetails determineMidBid() throws Exception {

		// Reserve value for bestBid, if something goes wrong this makes sure no
		// 'null' is returned at the end
		Iterator<BidDetails> possibleBids = bidsList.iterator();
		double utilBestBid = Double.MIN_VALUE;
		BidDetails bestBid = sos.getMaxBidPossible();

		while (possibleBids.hasNext()) {
			BidDetails currentBid = possibleBids.next();

			if (isRecentBid(currentBid, RECENT)) {
				continue;
			}

			// Group utility of current bid
			// The earlier it is the more important our own weight is
			double utilCurrentBid = om.getGroupUtility(currentBid.getBid(),
					5 - (negotiationSession.getTimeline().getTime() * 4));

			if (utilCurrentBid > utilBestBid) {
				utilBestBid = utilCurrentBid;
				bestBid = currentBid;
			}
		}
		return bestBid;
	}

	/**
	 * Determines bid for late negotiation.
	 * 
	 * @return {@link BidDetails} with the highest group-based utility (within
	 *         range of 0.7-1.0 for own utility)
	 * @throws Exception
	 */
	private BidDetails determineLateBid(double lowerBound) throws Exception {
		if (bidRange.getLowerbound() != lowerBound) {
			bidRange.setLowerbound(lowerBound);
			bidsList = sos.getBidsinRange(bidRange);
		}
		return determineMidBid();
	}

	/**
	 * Determines what decision method should be called depending on the current
	 * time.
	 * 
	 * @returns {@link BidDetails} of the upcoming bid for our agent
	 */
	@Override
	public BidDetails determineNextBid() {
		double relativeTime = negotiationSession.getTimeline().getTime();

		if (relativeTime < 0.2) {
			return determineEarlyBid(relativeTime);
		}

		else if (relativeTime >= 0.2 && relativeTime <= 0.9) {
			try {
				return determineMidBid();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (relativeTime >= 0.9 && relativeTime <= 0.95) {
			try {
				return determineLateBid(0.7);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (relativeTime > 0.95 && relativeTime < 0.985) {
			try {
				return determineLateBid(0.6);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (relativeTime >= 0.985) {
			try {
				return determineLateBid(0.55);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nextBid;
	}

	/**
	 * Checks if the provided {@link Bid} is the same as one of the X previously
	 * offered {@link Bid}s.
	 * 
	 * @param bid
	 *            is the provided {@link Bid}
	 * @param recent
	 *            is the number of previously offered {@link Bid}s checked
	 * @return {@code TRUE} if this bid was recently offered, otherwise
	 *         {@code FALSE}
	 */
	private boolean isRecentBid(BidDetails bid, int recent) {
		List<BidDetails> bidList = negotiationSession.getOwnBidHistory().getHistory();
		for (int i = bidList.size() - 1; i >= bidList.size() - recent; i--) {
			if (i >= 0 && bidList.get(i).getBid().equals(bid.getBid())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Unused silly method which is not needed
	 */
	@Override
	public BidDetails determineOpeningBid() {
		return null;
	}

	@Override
	public String getName() {
		return "in4010 q12015 group17";
	}
}

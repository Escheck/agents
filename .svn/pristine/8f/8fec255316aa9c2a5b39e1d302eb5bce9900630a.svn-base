package agents;

import java.util.ArrayList;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.UtilitySpace;

/**
 * This stores the bidding history and the {@link SimpleBidSpace}. Notice that
 * we do not store the old utilities as in {@link BidHistory} because the
 * utilityspace may change during the session.
 * 
 * @author W.Pasman 2sep14
 *
 */
public class HistorySpace {

	private BidList opponentBids = new BidList();
	private BidList myBids = new BidList();

	/**
	 * outcomeSpace is set only after
	 * {@link #setUtilities(AdditiveUtilitySpace, AdditiveUtilitySpace)} is
	 * called.
	 */
	private SimpleBidSpace outcomeSpace = null;

	HistorySpace() {
	}

	/**
	 * set the utilities.
	 * 
	 * @param my
	 * @param other
	 */
	void setUtilities(UtilitySpace my, UtilitySpace other) {
		outcomeSpace = new SimpleBidSpace(my, other);

	}

	/**
	 * List of bids made by opponent so far.Last one in list = most recent.
	 * 
	 * @return
	 */
	public BidList getOpponentBids() {
		return opponentBids;
	}

	/**
	 * List of bids that I made so far. Last one in list = most recent.
	 * 
	 * @return
	 */
	public BidList getMyBids() {
		return myBids;
	}

	/**
	 * 
	 * @return outcomeSpace, or null if outcomeSpace not yet initialized.
	 */
	public SimpleBidSpace getOutcomeSpace() {
		return outcomeSpace;
	}

	/**
	 * Get the {@link MoveType} of the opponent, nBack steps back in history.
	 * 
	 * @param nBack
	 *            0 for last move, 1 for second last move, etc.
	 * @return MoveType of nth-back move, or null if no such move exists.
	 */
	public MoveType getMoveType(int nBack) {
		int last = opponentBids.size() - 1;
		if (last - nBack - 1 < 0) {
			return null;
		}
		Bid bid = opponentBids.get(last - nBack);
		Bid prevbid = opponentBids.get(last - nBack - 1);

		double deltaSelf, deltaOther;
		try {
			deltaSelf = outcomeSpace.getMyUtilitySpace().getUtility(bid)
					- outcomeSpace.getMyUtilitySpace().getUtility(prevbid);
			deltaOther = outcomeSpace.getOpponentUtilitySpace().getUtility(bid)
					- outcomeSpace.getOpponentUtilitySpace()
							.getUtility(prevbid);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to get utility for bid", e);
		}
		return MoveType.getMoveType(deltaSelf, deltaOther);
	}

	/**
	 * create a silent bid: this bid has similar utilities as mySide last bid
	 * 
	 * @return
	 */
	public Bid getSilentBid() {
		return outcomeSpace.getSilentBid(myBids.last());
	}

}

/**
 * Util class, basically just ArrayList<Bid> with some convenience functions for
 * readability
 */
class BidList extends ArrayList<Bid> {

	/**
	 * Get last bid that was made
	 * 
	 * @return last {@link Bid}
	 */
	public Bid last() {
		if (size() == 0) {
			throw new IllegalStateException("no bids have been made yet");
		}
		return this.get(this.size() - 1);
	}

	/**
	 * get second last bid that was made.
	 * 
	 * @return second last {@link Bid}
	 */
	public Bid getSecondLast() {
		return this.get(this.size() - 2);
	}

}

package parties.in4010.q12015.group7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import negotiator.Bid;

/**
 * Implements a HashMap in which all bids in a bidSpace can be saved
 * 
 * @author svanbekhoven
 *
 */
public class BidSpace {
	/**
	 * HashMap with bid possibilities
	 */
	public HashMap<Double, Bid> bidSpace;

	/**
	 * Constructor
	 */
	public BidSpace() {
		bidSpace = new HashMap<Double, Bid>();
	}

	/**
	 * Add a bid to the bidspace
	 * 
	 * @param bid
	 * @param util
	 */
	public void add(Bid bid, double util) {
		bidSpace.put(util, bid);
	}

	/**
	 * Returns a list with all bids between an min and max utility
	 * 
	 * @param minUtility
	 * @param maxUtility
	 * @return
	 */
	public ArrayList<Bid> getBids(double minUtility, double maxUtility) {
		ArrayList<Bid> bids = new ArrayList<Bid>();
		Set<Double> keys = bidSpace.keySet();
		for (Double key : keys) {
			if (key >= minUtility && key <= maxUtility) {
				bids.add(new Bid(bidSpace.get(key)));
			}
		}

		return bids;
	}

	/**
	 * Generate a pseudo-random bid between a min and max utility
	 * 
	 * @param minUtility
	 * @param maxUtility
	 * @return
	 */
	public Bid generatePseudoRandomBid(double minUtility, double maxUtility) {
		ArrayList<Bid> bids = getBids(minUtility, maxUtility);

		return new Bid(bids.get(new Random().nextInt(bids.size())));
	}
}

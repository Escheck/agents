package agents;

import negotiator.Bid;
import negotiator.PocketNegotiatorAgent;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.UtilitySpace;

/**
 * A utility class for {@link PocketNegotiatorAgent}s. PocketNegotiatorAgents
 * are usually assumed to have the opponent's utility space as well, and to use
 * that (instead of estimating one by itself). However when used from Genius
 * this UtilitySpace will not be available. This class allows to cheaply fake
 * it, using the agent's own utilityspace. <br>
 * <h1>Assumptions</h1>
 * <ul>
 * <li>his first bid is his best bid
 * <li>relative distances in our own bidspace are indicative of consessions of
 * the opponent.
 * <li>our own util space is varied enough regarding utilities, so that the
 * opponent has a chance to make different utilities in our space by changing
 * his bid
 * </ul>
 * 
 * <h1>Tech Notes</h1> UtilitySpace should be an interface but it is a class.
 * This makes the code here messy. We are not shielding out any original
 * UtilitySpace code, but it may crash as we don't initialize it.
 * 
 * @author W.Pasman ripped from {@link SimpleTitForTatPN}.
 *
 */
public class SimpleFakeOpponentUtilitySpace extends AdditiveUtilitySpace {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4744417384585431753L;

	private final UtilitySpace ownSpace;
	private final double firstOpponentBidUtility;

	/**
	 * 
	 * @param utilitySpace
	 *            our OWN {@link AdditiveUtilitySpace} (not the opponent's)
	 * @param firstBid
	 *            first opponent bid.
	 * @throws Exception
	 *             if we can't determine utility of firstBid
	 */
	public SimpleFakeOpponentUtilitySpace(UtilitySpace utilitySpace,
			Bid firstBid) throws Exception {
		if (firstBid == null)
			throw new NullPointerException("bid=null");

		ownSpace = utilitySpace;
		firstOpponentBidUtility = ownSpace.getUtility(firstBid);
	}

	/**
	 * Returns utility distance (absolute value) to the first bid.
	 * 
	 * @param b
	 * @return (absolute) distance between given bid and first bid. Number in
	 *         range [0,1].
	 * @throws Exception
	 */
	private double distanceToFirstBid(Bid b) {
		return Math.abs(ownSpace.getUtility(b) - firstOpponentBidUtility);
	}

	/**
	 * estimates utility of bid for the opponent.
	 */

	public double getUtility(Bid bid) {
		return 1.0 - distanceToFirstBid(bid);

	}
}

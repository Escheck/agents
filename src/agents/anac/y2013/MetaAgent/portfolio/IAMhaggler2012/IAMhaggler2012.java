package agents.anac.y2013.MetaAgent.portfolio.IAMhaggler2012;

import negotiator.Bid;
import negotiator.utility.AdditiveUtilitySpace;
import agents.anac.y2013.MetaAgent.portfolio.IAMhaggler2012.agents2011.IAMhaggler2011;
import agents.anac.y2013.MetaAgent.portfolio.IAMhaggler2012.utility.SouthamptonUtilitySpace;

/**
 * @author Colin Williams
 * 
 *         The IAMhaggler Agent, created for ANAC 2012. Designed by C. R.
 *         Williams, V. Robu, E. H. Gerding and N. R. Jennings.
 * 
 */
public class IAMhaggler2012 extends IAMhaggler2011 {

	private SouthamptonUtilitySpace sus;

	/*
	 * (non-Javadoc)
	 * 
	 * @see agents.southampton.SouthamptonAgent#init()
	 */
	@Override
	public void init() {
		debug = false;
		super.init();
		sus = new agents.anac.y2013.MetaAgent.portfolio.IAMhaggler2012.utility.SouthamptonUtilitySpace(
				(AdditiveUtilitySpace) utilitySpace);
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see agents2011.southampton.IAMhaggler2011#proposeInitialBid()
	 */
	@Override
	protected Bid proposeInitialBid() throws Exception {
		Bid b = sus.getMaxUtilityBid();
		if (utilitySpace.getUtilityWithDiscount(b, timeline) < utilitySpace
				.getReservationValueWithDiscount(timeline)) {
			return null;
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see agents.southampton.SouthamptonAgent#proposeNextBid(negotiator.Bid)
	 */
	@Override
	protected Bid proposeNextBid(Bid opponentBid) throws Exception {
		Bid b = super.proposeNextBid(opponentBid);
		if (utilitySpace.getUtilityWithDiscount(b, timeline) < utilitySpace
				.getReservationValueWithDiscount(timeline)) {
			return proposeInitialBid();
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see agents2011.southampton.IAMhaggler2011#getTarget(double, double)
	 */
	@Override
	protected double getTarget(double opponentUtility, double time) {
		return Math.max(utilitySpace.getReservationValueWithDiscount(time),
				super.getTarget(opponentUtility, time));
	}
}

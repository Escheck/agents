package parties.in4010.q12015.group10;

import java.util.List;

import negotiator.AgentID;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;

public class AcceptanceStrategy {
	/**
	 * support function to create action
	 * 
	 * @param validActions
	 * @param detailsOfPotentialBid
	 * @param detailsOfLatestBidOnTable
	 * @param agent
	 *            the agent ID, needed to create the actions on behalf of this
	 *            agent.
	 * @return
	 */
	static Action getAction(List<Class<? extends Action>> validActions,
			BidDetails detailsOfPotentialBid,
			BidDetails detailsOfLatestBidOnTable, AgentID agent) {
		// Check if we are allowed to accept. We're not allowed to do this when
		// we are the first party to make a bid.
		boolean acceptingIsAllowed = validActions.contains(Accept.class);

		double minUtilityAlwaysAccept = 0.95;

		if (acceptingIsAllowed) {
			// We're not the first: So we can accept, offer, or deny.
			double offeredUtilVal = detailsOfLatestBidOnTable
					.getMyUndiscountedUtil();
			double PotentialUtilVal = detailsOfPotentialBid
					.getMyUndiscountedUtil();

			// If the value we are getting by accepting is higher than what we
			// would get using our own new offer, we accept.
			// We also accept if the utility is higher than our value that we
			// also accept
			if (offeredUtilVal >= PotentialUtilVal
					|| offeredUtilVal >= minUtilityAlwaysAccept) {
				return new Accept(agent, detailsOfPotentialBid.getBid());
			} else {
				detailsOfLatestBidOnTable = detailsOfPotentialBid;
				return new Offer(agent, detailsOfPotentialBid.getBid());
			}
		} else {
			// We are not allowed to accept. We can still offer or deny. Right
			// now only offering is implemented.
			detailsOfLatestBidOnTable = detailsOfPotentialBid;
			return new Offer(agent, detailsOfPotentialBid.getBid());
		}
	}
}

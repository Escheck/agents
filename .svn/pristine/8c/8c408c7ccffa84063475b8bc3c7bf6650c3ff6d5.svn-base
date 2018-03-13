package parties.in4010.q12015.group15;

import java.util.ArrayList;
import java.util.List;

import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This strategy uses Opponent Modeling to find a best bid out of a range of
 * bids. It tries to find the bid closest to the utility for the opponents from
 * their last offers but also with good utility for us. Our utility is valued
 * more important than the utility of our opponents.
 *
 */
public class OMStrategy {

	private OpponentModel om;
	private AdditiveUtilitySpace utilitySpace;
	private List<Bid> bidsOffered = new ArrayList<>();

	public OMStrategy(OpponentModel om, AdditiveUtilitySpace utilitySpace) {
		this.om = om;
		this.utilitySpace = utilitySpace;
	}

	/**
	 * @param a
	 *            range of bids to choose from
	 * @return the best bid out of the list of bids
	 */
	public Bid getBestBid(List<BidDetails> bids) {
		System.out.println("----- OMS -----");
		double highestEvaluation = Integer.MIN_VALUE;
		Bid bestBid = null;
		for (BidDetails bid : bids) {
			System.out.println("OMS bid: " + bid.getBid());
			if (bidsOffered.contains(bid.getBid())) { // Don't offer same bid
														// twice
				System.out.println("Has offered bid before: " + bid.getBid());
				continue;
			}

			double evaluation = 0;
			try {
				double totalWeights = 0;
				for (String opponent : om.getOpponents()) {
					double opponentUtilityForBid = om.getBidEvaluation(
							opponent, bid.getBid());
					double lastUtilityFromOpponentBid = om
							.getLastBidEvaluation(opponent);
					double utilDiff = Math.abs(opponentUtilityForBid
							- lastUtilityFromOpponentBid);

					double weight = 1 + om
							.getAverageBidUtilityChangeForAgent(opponent); // Put
																			// higher
																			// weights
																			// to
																			// non-conceders
					totalWeights += weight;
					evaluation -= weight * utilDiff;
				}
				evaluation += totalWeights
						* utilitySpace.getUtility(bid.getBid()); // Add our
																	// utility
				if (evaluation > highestEvaluation) {
					highestEvaluation = evaluation;
					bestBid = bid.getBid();
				}
			} catch (Exception e) {
				System.out
						.println("OMS Could not calculate highest evaluation from OM");
				e.printStackTrace();
			}

		}

		System.out.println("Highest bid evaluation: " + highestEvaluation);
		System.out.println("for bid: " + bestBid);
		bidsOffered.add(bestBid);
		return bestBid;
	}
}
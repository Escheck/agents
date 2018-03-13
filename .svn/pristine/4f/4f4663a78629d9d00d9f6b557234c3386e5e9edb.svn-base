package parties.in4010.q12015.group10;

import static java.lang.Math.pow;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.OutcomeSpace;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AdditiveUtilitySpace;

public class OfferingStrategy {

	public static Bid createPotentialBid(AdditiveUtilitySpace utilitySpace,
			Opponent[] opponents, TimeLineInfo timeline, Deadline deadLine) {
		System.out.println("OfferingStrategy - createPotentialBid");

		// Settings
		double BandWidth = 0.05;

		// Settings

		// Determine the central Utility for the Boulware strategy
		double time = timeline.getTime();
		// SETTING: MINIMAL ACCEPTABLE END UTILITY
		double uEnd = 0.8; // End utility
		double e = 0.1; // Boulware parameter
		double Pboulware = 1 - pow(time, 1 / e) * (1 - uEnd);

		// Calculate the lower bound of our own utility to later search for the
		// bids that have a higher utility than this lower bound
		double min = Math.max(Pboulware * (1 - BandWidth / 2), 0); // Lowerbound
																	// : 0

		// Get lowerBound from offeringStrategy (different lower bound, one of
		// the lower bounds will be used)
		double lowerBoundStrategyEst = OpponentStrategyEstimator
				.updateAllModels(opponents, deadLine, timeline, utilitySpace);
		min = Math.max(min, lowerBoundStrategyEst); // select highest minimum
													// utility

		Range UtilRange = new Range(min, 1);
		// System.out.println("Create OutcomeSpace");
		OutcomeSpace OS = new OutcomeSpace(utilitySpace);

		// select all the bids with corresponding to range
		List<BidDetails> PotentialBids = OS.getBidsinRange(UtilRange);

		// Now Find bid with highest minimum utility for all players

		// Determine for each opponent the utility for each possible bid
		// Count number of opponents and number of bids

		int NumberOfOpponents = opponents.length;
		int NumberofBids = PotentialBids.size();

		double[][] OpponentBidUtilities = new double[NumberOfOpponents][NumberofBids];
		int OpponentCounter = 0;
		int BidCounter = 0;

		double[] OwnUtilities = new double[NumberofBids];

		// calculates the utilities of all bids for all opponents
		for (Opponent currentOpponent : opponents) {
			// Iterates over all opponents
			BidCounter = 0;
			AdditiveUtilitySpace OpponentUtilities = currentOpponent
					.getEstimatedUtilitySpace();
			for (Iterator<BidDetails> BidDetailsIterator = PotentialBids
					.iterator(); BidDetailsIterator.hasNext();) {
				// Iterates over all possible bids
				BidDetails Biddetails = BidDetailsIterator.next();
				Bid bid = Biddetails.getBid();
				double util = 0;
				try {
					util = OpponentUtilities.getUtility(bid);
				} catch (Exception e1) {
					// System.out.println("Invalid Bid");
					e1.printStackTrace();
				} // Throws Exception when bid is invalid
					// Place calculated Utility in data Matrix
				OpponentBidUtilities[OpponentCounter][BidCounter] = util;

				// Calculate own utility
				try {
					OwnUtilities[BidCounter] = utilitySpace.getUtility(bid);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				BidCounter++;
			}
			OpponentCounter++;
		}

		double minUtil = 100;
		double MaxMinUtil = 0;
		int BestBidNumber_maxmin = -1;
		int BestBidNumber_bestAlternative = -1;

		// now select bid that:
		// has the highest minimum utility
		// has a lower utility for the opponents than our own utility
		for (int i = 0; i < NumberofBids; i++) {
			// Gaat eerst langs de rijen (bids), vervolgens langs de kolommen
			// (opponents)
			// om de bid te vinden die het hoogste minimum heeft
			for (int p = 0; p < NumberOfOpponents; p++) {
				// System.out.println("2nd Iterate over Opponents");
				double util = OpponentBidUtilities[p][i];

				if (util < minUtil) {
					minUtil = util; // Results in lowest utility per bid
				}
			}
			double ownUtil = OwnUtilities[i];
			// if the minimum utility of this bid for all agents is higher than
			// that of another bid for all agents
			// and if the utility is lower than our own utility then update the
			// bestnumber.
			if (minUtil > MaxMinUtil && minUtil < ownUtil) {
				BestBidNumber_maxmin = i;
			} else if (minUtil > MaxMinUtil) {
				BestBidNumber_bestAlternative = i;
			}
		}

		// Determine the best bid from the list of bids according to strategy 3
		BidDetails bd = null;
		if (BestBidNumber_maxmin != -1) { // a good bid has been found
			bd = PotentialBids.get(BestBidNumber_maxmin);
		} else if (BestBidNumber_bestAlternative != -1) {
			bd = PotentialBids.get(BestBidNumber_bestAlternative);
		} else {
			Random r = new Random();
			int maxBound = NumberofBids - 1;
			int minBound = 0;
			int rand = r.nextInt((maxBound - minBound) + 1) + minBound;
			bd = PotentialBids.get(rand);
		}
		Bid offer = bd.getBid();

		return offer;

	}
}

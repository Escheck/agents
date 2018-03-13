package parties.in4010.q12015.group10;

import static java.lang.Math.pow;

import java.util.Iterator;
import java.util.List;

import misc.Range;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.DeadlineType;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.OutcomeSpace;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AdditiveUtilitySpace;

public class OpponentStrategyEstimator {

	static double updateAllModels(Opponent[] opponents, Deadline deadLine,
			TimeLineInfo timeLine, AdditiveUtilitySpace myUtilitySpace) {
		boolean acceptableQuality = false;
		if (deadLine.getType() == DeadlineType.TIME) {
			return 0.0;
		}

		int maxNumberOfBids = deadLine.getValue();
		double nextBidNumberDouble = timeLine.getCurrentTime()
				/ timeLine.getTotalTime();
		int nextBidNumber = ((int) nextBidNumberDouble);

		// generate time array
		double[] totalTimeArray = new double[maxNumberOfBids];
		for (int bidNumber = 0; bidNumber < maxNumberOfBids; bidNumber++) {
			totalTimeArray[bidNumber] = ((double) nextBidNumber)
					/ ((double) maxNumberOfBids);
		}

		// for each opponent
		// u = 1+a*t+b*t^c
		// see report
		double[] a = new double[opponents.length]; // estimated boulware
													// parameter
		double[] b = new double[opponents.length]; // estimated boulware
													// parameter
		double[] c = new double[opponents.length]; // estimated boulware
													// parameter
		double[] d = new double[opponents.length]; // estimated boulware
													// parameter
		double[] errorVariance = new double[opponents.length]; // estimated
																// variance of
																// error for the
																// found fit

		// estimate fitting parameters
		double minimumPassedTime = 0.75; // this is the minimum time that must
											// be passed before we consider
											// getting an estimate
		double powerIncrement = 0.2;
		int maxLoops = 100;
		int spacing = 5;
		int beginOffset = 1;

		// System.out.println("minimumPassedTimeboolean"+minimumPassedTime);
		if (timeLine.getTime() > minimumPassedTime) {
			acceptableQuality = true;
		}

		for (int opponentNumber = 0; opponentNumber < opponents.length; opponentNumber++) {
			if (opponents[opponentNumber].getHistorySize() < 10 * spacing) {
				return 0.0;
			}
		}

		if (acceptableQuality) {
			for (int opponentNumber = 0; opponentNumber < opponents.length; opponentNumber++) {
				double[] bidEval = new double[opponents[opponentNumber]
						.getHistorySize()];
				double[] time = new double[opponents[opponentNumber]
						.getHistorySize()];
				// for each bid in the history
				Bid bid;
				for (int bidNumber = 0; bidNumber < opponents[opponentNumber]
						.getHistorySize(); bidNumber++) {
					bid = opponents[opponentNumber]
							.getBidfromHistory(bidNumber);
					try {
						bidEval[bidNumber] = opponents[opponentNumber]
								.getEstimatedUtilitySpace().getUtility(bid);
					} catch (Exception e) {
						e.printStackTrace();
					}
					time[bidNumber] = ((double) bidNumber)
							/ ((double) deadLine.getValue());
				}

				// begin offset not implemented
				double[] minTime = boulwareParameterEstimator.getMinTime(
						bidEval, time, spacing, beginOffset);
				double[] minBidEval = boulwareParameterEstimator.getMinEval(
						bidEval, time, spacing, beginOffset);

				double[] parABCE = boulwareParameterEstimator.leastSquaresFit(
						minBidEval, minTime, maxLoops, powerIncrement);

				a[opponentNumber] = parABCE[0];
				b[opponentNumber] = parABCE[1];
				c[opponentNumber] = parABCE[2];
				d[opponentNumber] = parABCE[3];
				errorVariance[opponentNumber] = parABCE[4];
				System.out.println("errorVariance"
						+ errorVariance[opponentNumber]);
			}
		}

		// if the minimum variance is higher than the threshold the model is
		// deemed unusable
		double modelQualityThreshold = 0.02;
		for (int n = 0; n < errorVariance.length; n++) {
			if (errorVariance[n] > modelQualityThreshold) {
				acceptableQuality = true;
			}
		}

		// get the full outcome space
		Range UtilRange = new Range(0, 1);
		OutcomeSpace fullOutComeSpace = new OutcomeSpace(myUtilitySpace);
		List<BidDetails> allPossibleBids = fullOutComeSpace
				.getBidsinRange(UtilRange);
		double[] maxUtilityMySpaceOfCommonBids = new double[maxNumberOfBids];
		// for each opponent:
		// get the estimated lowerbound on the bidding utility that they will
		// bid unitl N bids before the deadline
		double t; // non array time variable
		int offsetBidsFromDeadLine = 4;
		double[] lowerBoundOfferedUtility = new double[opponents.length];
		double totalMaxUtilityMySpaceOfCommonBids = 0;
		if (acceptableQuality) {
			// for each future bidnumber
			for (int bidNumber = nextBidNumber; bidNumber < maxNumberOfBids
					- offsetBidsFromDeadLine; bidNumber++) {
				// for each opponent get the lower bound on the expected utility
				for (int opponentNumber = 1; opponentNumber < opponents.length; opponentNumber++) {
					t = totalTimeArray[bidNumber];
					lowerBoundOfferedUtility[opponentNumber] = a[opponentNumber]
							+ b[opponentNumber]
							* t
							+ c[opponentNumber]
							* pow(t, d[opponentNumber]);
				}
				// ////// check the max attainable common utility
				maxUtilityMySpaceOfCommonBids[bidNumber] = 0;
				for (Iterator<BidDetails> BidDetailsIterator = allPossibleBids
						.iterator(); BidDetailsIterator.hasNext();) {
					// Iterates over all possible bids
					BidDetails bidDetails = BidDetailsIterator.next();
					Bid bid = bidDetails.getBid();
					boolean bidInCommonBidRange = true;
					double utilityOfBid;
					for (int opponentNumber = 1; opponentNumber < opponents.length; opponentNumber++) {
						utilityOfBid = 0;
						try {
							utilityOfBid = opponents[opponentNumber]
									.getEstimatedUtilitySpace().getUtility(bid);
						} catch (Exception e1) {
							e1.printStackTrace();
						} // Throws Exception when bid is invalid
						if (utilityOfBid < lowerBoundOfferedUtility[opponentNumber]) {
							bidInCommonBidRange = false;
						}
					}
					if (bidInCommonBidRange) {
						utilityOfBid = 0;
						try {
							utilityOfBid = myUtilitySpace.getUtility(bid);
						} catch (Exception e1) {
							e1.printStackTrace();
						} // Throws Exception when bid is invalid
						if (maxUtilityMySpaceOfCommonBids[bidNumber] < utilityOfBid) {
							maxUtilityMySpaceOfCommonBids[bidNumber] = utilityOfBid;
						}
					}

				}

				// //////
			}
			for (int bidNumber = nextBidNumber; bidNumber < maxNumberOfBids
					- offsetBidsFromDeadLine; bidNumber++) {
				if (totalMaxUtilityMySpaceOfCommonBids < maxUtilityMySpaceOfCommonBids[bidNumber]) {
					totalMaxUtilityMySpaceOfCommonBids = maxUtilityMySpaceOfCommonBids[bidNumber];
				}
			}
		}

		double uncertaintyBandwidth = 0.05;
		return totalMaxUtilityMySpaceOfCommonBids - uncertaintyBandwidth;

	}
}

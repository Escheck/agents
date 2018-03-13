package parties.in4010.q12015.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;

/**
 * Opponent modeling using frequency analysis heuristic
 *
 */
public class Group4OpponentModel {
	private Map<IssueDiscrete, Map<ValueDiscrete, Integer>> issueValueCount;
	private Map<IssueDiscrete, Double> weights;

	public Group4OpponentModel(Domain domain) {
		issueValueCount = new HashMap<>();
		weights = new HashMap<>();
		for (Issue issue : domain.getIssues()) {
			IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
			issueValueCount.put(issueDiscrete,
					new HashMap<ValueDiscrete, Integer>());
			// let each value be 1 at first
			for (ValueDiscrete value : issueDiscrete.getValues()) {
				issueValueCount.get(issueDiscrete).put(value, 1);
			}
			// initiate the weights
			weights.put(issueDiscrete, 1.0 / domain.getIssues().size());
		}
	}

	/**
	 * Evaluates the predicted utility of a given bid, using the information
	 * provided thus far.
	 * 
	 * @param bid
	 *            the bid to be evaluated
	 * @return a value from 0.0 to 1.0 giving the predicted utility of the bid
	 */
	public double evaluateBid(Bid bid) {
		double utility = 0.0;

		// update the weights
		// weights = computeWeights();

		try {
			for (Issue issueRaw : bid.getIssues()) {
				double maxValue = 0;
				ValueDiscrete value = (ValueDiscrete) bid.getValue(issueRaw
						.getNumber());
				IssueDiscrete issue = (IssueDiscrete) issueRaw;

				double issueWeight = weights.get(issue);
				double issueValue = ((double) issueValueCount.get(issue).get(
						value));

				// add 1 to the current value
				// issueValueCount.get(issue).put(value, (int) ((issueValue +
				// 1)));

				// normalize the value to make the max of the value equals 1
				for (ValueDiscrete eachValue : issue.getValues()) {
					double tempValue = ((double) issueValueCount.get(issue)
							.get(eachValue));
					if (tempValue > maxValue)
						maxValue = tempValue;
				}

				// update and normalize the value to make the max of the value
				// equals 1
				// to keep track of the history, only the value that is going to
				// be computed
				// in the utility is normalized
				issueValue /= maxValue;

				// compute the utility
				utility += issueWeight * issueValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return utility;
	}

	public void UpdateOpponentModel(ArrayList<Bid> historicalBid) {
		if (historicalBid.size() > 1) {
			computeWeights(historicalBid.get(historicalBid.size() - 2),
					historicalBid.get(historicalBid.size() - 1));
		}
		if (historicalBid.size() > 0) {
			computeValueCounts(historicalBid.get(historicalBid.size() - 1));
			for (Issue issueRaw : historicalBid.get(historicalBid.size() - 1)
					.getIssues()) {
				IssueDiscrete issueDiscrete = (IssueDiscrete) issueRaw;
				System.out.println(issueRaw.getName() + ": "
						+ weights.get(issueRaw));
				for (ValueDiscrete value : issueDiscrete.getValues()) {
					System.out.println(value.getValue() + ": "
							+ issueValueCount.get(issueDiscrete).get(value));
				}
			}
		}
	}

	/**
	 * Given the current data about the opponent, return the predicted weight
	 * for each issue. If the opponent suggests many different values for that
	 * issue, we assume that it is unimportant for it.
	 * 
	 * @return a map of issues to their respective weights, normalized such that
	 *         the sum of the weights is 1.0
	 */
	private void computeWeights(Bid lastBid, Bid currentBid) {
		try {
			int count = 0;
			ArrayList<Double> weight_Temp = new ArrayList<Double>();
			// Map<IssueDiscrete, Double> weights = new HashMap<>();
			for (Issue issueRaw : lastBid.getIssues()) {
				// ValueDiscrete lastBidValue = (ValueDiscrete)
				// lastBid.getValue(issueRaw.getNumber());
				// ValueDiscrete currentBidValue = (ValueDiscrete)
				// currentBid.getValue(issueRaw.getNumber());
				if (lastBid.getValue(issueRaw.getNumber()).equals(
						currentBid.getValue(issueRaw.getNumber()))) {
					weight_Temp.add(weights.get(issueRaw) + 0.1);
					count++;
				} else {
					weight_Temp.add(weights.get(issueRaw));
				}
				/*
				 * double variance = computeVariance(issue); weights.put(issue,
				 * 1.0/variance);
				 */
			}
			for (Issue issueRaw : lastBid.getIssues()) {
				weights.put((IssueDiscrete) issueRaw,
						weight_Temp.get(issueRaw.getNumber() - 1)
								/ (1 + 0.1 * count));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Normalize weights: make them sum to 1.0
		// double sum = weights.values().stream().reduce(0.0, Double::sum);
		// weights.forEach((k, v) -> weights.put(k, v / sum));
	}

	private void computeValueCounts(Bid bid) {
		try {
			for (Issue issueRaw : bid.getIssues()) {
				ValueDiscrete bidValue = (ValueDiscrete) bid.getValue(issueRaw
						.getNumber());
				int count = issueValueCount.get(issueRaw).get(bidValue);
				issueValueCount.get(issueRaw).put(bidValue, count + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * private double computeVariance(IssueDiscrete issue){ double variance =
	 * 0.0; List<Integer> counts = new
	 * ArrayList<>(issueValueCount.get(issue).values()); int max =
	 * counts.stream().max(Integer::compare).get(); List<Double> weightedCounts
	 * = counts.stream() .mapToDouble(i -> (((double) i) / max)).boxed()
	 * .collect(Collectors.toList());
	 * 
	 * double avg = weightedCounts.stream().reduce(0.0, Double::sum) /
	 * weightedCounts.size(); double variance = weightedCounts.stream()
	 * .mapToDouble(d -> (d - avg) * (d - avg)).sum() / weightedCounts.size();
	 * return variance; }
	 */

}
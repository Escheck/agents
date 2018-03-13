package parties.in4010.q12015.group7;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.issue.Issue;
import negotiator.issue.Value;

/**
 * Opponent Modeler which estimates issue weights and values according to the
 * bids that are added
 * 
 * @author svanbekhoven
 */
public class IssueEstimator {
	/**
	 * Weights of all issues
	 */
	private HashMap<Integer, Double> issueWeights;

	/**
	 * Weights of all issue values
	 */
	private HashMap<Integer, HashMap<Value, Integer>> issueValueWeights;

	/**
	 * Sensitivity parameter
	 */
	private double n;

	/**
	 * Constructor
	 * 
	 * @param n
	 *            sensitivity parameter
	 */
	public IssueEstimator(double n) {
		this.n = n;
		this.issueWeights = new HashMap<Integer, Double>();
		this.issueValueWeights = new HashMap<Integer, HashMap<Value, Integer>>();
	}

	/**
	 * Add a bid to the issue estimator, automatically recalculating the issue
	 * weights and issue value weights
	 * 
	 * @param bid
	 *            bid you want to add to the estimator
	 * @param bidHistory
	 *            bidHistory belonging to the opponent you received the bid from
	 *            (excluding(!) the bid in the first parameter)
	 */
	public void addBid(Bid bid, BidHistory bidHistory) {
		List<Issue> issues = bid.getIssues();

		for (Issue issue : issues) {
			Integer issueNumber = issue.getNumber();
			Value issueValue = null;

			try {
				issueValue = bid.getValue(issueNumber);
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.updateIssueValueWeights(bid, issues, issueNumber, issueValue);
			this.updateIssueWeights(bid, bidHistory, issues, issueNumber,
					issueValue);
		}

		this.normalizeWeights();
	}

	/**
	 * Update the issueValueWeights according to a new bid
	 * 
	 * @param bid
	 * @param issues
	 * @param issueNumber
	 * @param issueValue
	 */
	private void updateIssueValueWeights(Bid bid, List<Issue> issues,
			Integer issueNumber, Value issueValue) {
		if (!issueValueWeights.containsKey(issueNumber)) {
			issueValueWeights.put(issueNumber, new HashMap<Value, Integer>());
		}

		if (!issueValueWeights.get(issueNumber).containsKey(issueValue)) {
			issueValueWeights.get(issueNumber).put(issueValue, 0);
		}

		issueValueWeights.get(issueNumber).put(issueValue,
				issueValueWeights.get(issueNumber).get(issueValue) + 1);
	}

	/**
	 * Update the issueWeights according to a new bid
	 * 
	 * @param bid
	 * @param bidHistory
	 * @param issues
	 * @param issueNumber
	 * @param issueValue
	 */
	private void updateIssueWeights(Bid bid, BidHistory bidHistory,
			List<Issue> issues, Integer issueNumber, Value issueValue) {
		if (!issueWeights.containsKey(issueNumber)) {
			issueWeights.put(issueNumber, (double) (1.0 / issues.size()));
		}

		Bid lastBid = null;
		if (bidHistory.size() != 0) {
			try {
				lastBid = bidHistory.getLastBid();
				if (lastBid.getValue(issueNumber).equals(issueValue)) {
					issueWeights.put(issueNumber, issueWeights.get(issueNumber)
							+ n);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Normalizes the issueWeights
	 */
	private void normalizeWeights() {
		Double sum = 0.0;
		for (Double weight : this.issueWeights.values()) {
			sum += weight;
		}
		for (Map.Entry<Integer, Double> entry : issueWeights.entrySet()) {
			issueWeights.put(entry.getKey(), entry.getValue() / sum);
		}
	}

	/**
	 * Find the maximum weight of all values
	 * 
	 * @param valueWeights
	 * @return maximum value
	 */
	private double maxHashMap(HashMap<Value, Integer> valueWeights) {
		return Collections.max(valueWeights.values());
	}

	/**
	 * Returns the issueWeights
	 * 
	 * @return
	 */
	private HashMap<Integer, Double> getIssueWeights() {
		return this.issueWeights;
	}

	/**
	 * Returns the issueValueWeights
	 * 
	 * @return
	 */
	private HashMap<Integer, HashMap<Value, Double>> getValueWeights() {
		HashMap<Integer, HashMap<Value, Double>> returnValueWeights = new HashMap<Integer, HashMap<Value, Double>>();
		for (Map.Entry<Integer, HashMap<Value, Integer>> entry : issueValueWeights
				.entrySet()) {
			double max = maxHashMap(entry.getValue());
			returnValueWeights
					.put(entry.getKey(), new HashMap<Value, Double>());
			for (Map.Entry<Value, Integer> entryValue : entry.getValue()
					.entrySet()) {
				returnValueWeights.get(entry.getKey()).put(entryValue.getKey(),
						entryValue.getValue() / max);
			}
		}
		return returnValueWeights;
	}

	/**
	 * Returns the utility of the bid for this issueEstimator
	 * 
	 * @param bid
	 * @return
	 */
	public double getUtility(Bid bid) {
		double sum = 0;
		double issueWeight = 0;
		List<Issue> issues = bid.getIssues();
		for (Issue i : issues) {
			issueWeight = 0;
			try {
				if (this.getValueWeights().get(i.getNumber())
						.containsKey(bid.getValue(i.getNumber()))) {
					issueWeight = (double) this.getValueWeights()
							.get(i.getNumber())
							.get(bid.getValue(i.getNumber()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			sum += ((double) this.getIssueWeights().get(i.getNumber()))
					* issueWeight;
		}

		return sum;
	}
}

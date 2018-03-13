package parties.in4010.q12015.group8;

import java.util.HashMap;
import java.util.List;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;

public class OpponentModel {
	private static final double FREQUENCY_THRESHOLD = 0.95;
	private static final double ACCURACY_THRESHOLD = 0.8;
	private static final double NEGLIGIBLE_WEIGHT_THRESHOLD = 0.05;

	private HashMap<Issue, HashMap<Value, Double>> frequencies = new HashMap<Issue, HashMap<Value, Double>>();
	private HashMap<Issue, Double> weights = new HashMap<Issue, Double>();
	private double totalBids = 0.0;
	private Bid previousBid = null;
	private int updatedIssues = 0;
	private AdditiveUtilitySpace utilitySpace;

	/**
	 * Create OppentModel for current utility space
	 * 
	 * @param utilitySpace
	 */
	public OpponentModel(AdditiveUtilitySpace utilitySpace) {
		this.utilitySpace = utilitySpace;
		for (Issue issue : utilitySpace.getDomain().getIssues()) {
			List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();
			HashMap<Value, Double> valueMap = new HashMap<Value, Double>();

			for (Value value : values) {
				valueMap.put(value, 0.0);
			}

			frequencies.put(issue, valueMap);
			weights.put(issue, 1.0 / utilitySpace.getDomain().getIssues()
					.size());
		}
	}

	public void updateOpponentModel(Bid bid) {
		totalBids += 1.0;
		for (int issueIndex = 0; issueIndex < bid.getIssues().size(); issueIndex++) {
			// Update frequency values
			updateFrequency(bid, issueIndex);
			// Update issue weights
			updateWeights(bid, issueIndex);
		}

		for (int issueIndex = 0; issueIndex < bid.getIssues().size(); issueIndex++) {
			double issueWeight = weights.get(bid.getIssues().get(issueIndex));
			issueWeight = issueWeight / (1 + (0.1 * updatedIssues));
			weights.put(bid.getIssues().get(issueIndex), issueWeight);
		}
		previousBid = bid;
		updatedIssues = 0;

		// To test / check values of all weights and frequencies.
		/*
		 * for (Issue issue : utilitySpace.getDomain().getIssues()) {
		 * List<ValueDiscrete> values = ((IssueDiscrete)issue).getValues(); for
		 * (Value value : values) { System.out.println("Frequencies:" + value +
		 * "   " + getFrequency(issue, value)); }
		 * 
		 * System.out.println("Weight:" + issue.toString() + "   " +
		 * getWeight(issue)); } System.out.println(totalBids);
		 */
	}

	public void updateFrequency(Bid bid, int issueIndex) {
		Issue curIssue = bid.getIssues().get(issueIndex);
		Value issueValue = findValue(bid, issueIndex);
		// Update evals : Get the Map with values of the issue and their
		// respective evals
		HashMap<Value, Double> valueMap = frequencies.get(curIssue);
		valueMap.put(issueValue, (valueMap.get(issueValue) + 1.0));
		frequencies.put(curIssue, valueMap);
	}

	public void updateWeights(Bid bid, int issueIndex) {
		if (previousBid != null) {
			Issue curIssue = bid.getIssues().get(issueIndex);
			Value issueValue = findValue(bid, issueIndex);
			Value oldIssueValue = findValue(previousBid, issueIndex);
			if (issueValue.equals(oldIssueValue)) {
				double issueWeight = weights.get(curIssue);
				issueWeight += 0.1;
				weights.put(curIssue, issueWeight);
				updatedIssues += 1;
			}
		}
	}

	/**
	 * Checks if this opponent model is accurate.
	 * 
	 * @return
	 */
	public boolean isAccurate() {
		double accurate = 0;

		for (Issue issue : utilitySpace.getDomain().getIssues()) {
			List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();
			for (Value value : values) {
				if (getFrequency(issue, value) >= FREQUENCY_THRESHOLD) {
					accurate++;
					break; // we think that this issue is accurate enough
				}
			}
		}

		return (accurate / utilitySpace.getDomain().getIssues().size()) > ACCURACY_THRESHOLD;
	}

	/**
	 * Returns dangerous bids based on the current frequency analysis. Watch out
	 * that this is only accurate after a couple of rounds.
	 * 
	 * @return
	 */
	public boolean isDangerousBid(Bid bid) {

		boolean dangerous = true;
		for (Issue issue : bid.getIssues()) {
			if (getWeight(issue) < NEGLIGIBLE_WEIGHT_THRESHOLD)
				continue; // could be dangerous because doesn't matter

			try {
				if (getFrequency(issue, bid.getValue(issue.getNumber())) < FREQUENCY_THRESHOLD) {
					dangerous = false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dangerous = false;
			}
		}

		return dangerous;
	}

	/**
	 * @param bid
	 * @param issueIndex
	 * @return the picked value for given issue idnumber
	 */
	private Value findValue(Bid bid, int issueIndex) {
		Value issueValue = null;
		try {
			issueValue = bid.getValue(issueIndex + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return issueValue;
	}

	public double getWeight(Issue issue) {
		return weights.get(issue);
	}

	public double getFrequency(Issue issue, Value value) {
		return frequencies.get(issue).get(value) / totalBids;
	}

}

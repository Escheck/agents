package parties.in4010.q12015.group12;

import java.util.HashMap;
import java.util.List;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.issue.ISSUETYPE;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;

public class OtherAgent {

	private double[] weights;
	private BidHistory historyAgent;
	private HashMap<Value, Integer> values;
	private boolean agent;

	// constructor of agent
	OtherAgent(String ID, int amountIssues) {
		historyAgent = new BidHistory();
		weights = new double[amountIssues];
		values = new HashMap<Value, Integer>();
		agent = false;

		for (int i = 0; i < amountIssues - 1; i++) {
			weights[i] = (double) (1 / amountIssues);
		}
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] newWeights, boolean bid) {
		agent = bid;
		weights = newWeights;
	}

	// updates values by using received bid
	// amount of time bided so far is in values (hashmap)
	// increase when it is bided again
	public void setValues(Bid bid) {
		for (int i = 1; i < bid.getValues().size(); i++) {
			Value v;
			try {
				v = bid.getValue(i);
				if (!values.containsKey(v)) {
					values.put(v, 1);
				} else {
					int amount = values.get(v);
					amount++;
					values.put(v, amount);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public BidHistory getHistory() {
		if (historyAgent == null)
			System.out.println("null");
		return historyAgent;
	}

	public void addBidDetails(BidDetails newDetails) {
		historyAgent.add(newDetails);
	}

	// we know for sure that this is an agent (because of doing a bid)
	public boolean isAgent() {
		return agent;
	}

	// returns number of issue with highest believed weight for this agent
	public int heighWeight() {
		int issueNmr = 0;
		double weight = 0.0;

		for (int i = 0; i < weights.length - 1; i++) {
			if (weights[i] > weight) {
				issueNmr = i;
				weight = weights[i];
			}
		}
		return issueNmr;
	}

	// gets highest believed value for a certain issue
	public Value getBestValue(int issueNmr, List<Issue> issues) {
		Value vHighest = null;
		int highest = 0;

		if (issues.get(issueNmr).getType() == ISSUETYPE.DISCRETE) {
			IssueDiscrete dIssue = (IssueDiscrete) issues.get(issueNmr);
			List<ValueDiscrete> valuesOfIssue = dIssue.getValues();

			vHighest = valuesOfIssue.get(0);

			for (ValueDiscrete v : valuesOfIssue) {
				if (values.containsKey(v)) {
					if (highest < values.get(v)) {
						vHighest = v;
						highest = values.get(v);
					}
				}
			}
		}

		return vHighest;
	}

}

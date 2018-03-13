package parties.in4010.q12015.group7;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;

/**
 * Keep track of IssueEstimators of multiple opponents with the
 * MultipleIssueEstimator
 * 
 * @author svanbekhoven
 *
 */
public class MultipleIssueEstimator {
	/**
	 * BidHistories of all opponents
	 */
	private HashMap<AgentID, IssueEstimator> issueEstimators;

	/**
	 * IssueEstimator sensitivity parameter
	 */
	private double n;

	/**
	 * Constructor
	 * 
	 * @param n
	 *            sensitivity parameter
	 */
	public MultipleIssueEstimator(double n) {
		this.n = n;
		this.issueEstimators = new HashMap<AgentID, IssueEstimator>();
	}

	/**
	 * Add a bid to the issueEstimator of a particular agent
	 * 
	 * @param agentId
	 * @param bidDetails
	 * @param bidHistory
	 */
	public void addBid(AgentID agentId, BidDetails bidDetails,
			BidHistory bidHistory) {
		if (!this.issueEstimators.containsKey(agentId)) {
			this.issueEstimators.put(agentId, new IssueEstimator(this.n));
		}
		this.issueEstimators.get(agentId).addBid(bidDetails.getBid(),
				bidHistory);
	}

	/**
	 * Returns the issueEstimator of a particular agent
	 * 
	 * @param agentId
	 * @return
	 */
	public IssueEstimator getIssueEstimator(AgentID agentId) {
		return issueEstimators.get(agentId);
	}

	/**
	 * Returns all issueEstimators
	 * 
	 * @return
	 */
	public HashMap<AgentID, IssueEstimator> getIssueEstimators() {
		return issueEstimators;
	}

	/**
	 * Returns the average estimated utility of a bid for all opponents
	 * 
	 * @param bid
	 * @return
	 */
	public double getAverageUtility(Bid bid) {
		double sum = 0;
		for (Map.Entry<AgentID, IssueEstimator> entry : issueEstimators
				.entrySet()) {
			sum += entry.getValue().getUtility(bid);
		}

		return sum / issueEstimators.size();
	}

	/**
	 * Returns the set of AgentIds in this MultipleIssueEstimator
	 * 
	 * @return
	 */
	public Set<AgentID> getAgentIds() {
		return this.issueEstimators.keySet();
	}
}
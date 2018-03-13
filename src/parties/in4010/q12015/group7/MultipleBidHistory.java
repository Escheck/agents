package parties.in4010.q12015.group7;

import java.util.HashMap;
import java.util.Set;

import negotiator.AgentID;
import negotiator.bidding.BidDetails;

/**
 * Keep track of bid histories of multiple agents using this MultipleBidHistory
 * 
 * @author svanbekhoven
 *
 */
public class MultipleBidHistory {
	/**
	 * BidHistories of all agent distinct
	 */
	private HashMap<AgentID, EnhancedBidHistory> bidHistories;

	/**
	 * BidHistories of all agents combined
	 */
	private EnhancedBidHistory combinedBidHistory;

	/**
	 * Constructor
	 */
	public MultipleBidHistory() {
		this.bidHistories = new HashMap<AgentID, EnhancedBidHistory>();
		this.combinedBidHistory = new EnhancedBidHistory();
	}

	/**
	 * Add a bid to a bidHistory of a certain agent
	 * 
	 * @param agentId
	 * @param bidDetails
	 */
	public void addBid(AgentID agentId, BidDetails bidDetails) {
		if (!this.bidHistories.containsKey(agentId)) {
			this.bidHistories.put(agentId, new EnhancedBidHistory());
		}
		this.bidHistories.get(agentId).add(bidDetails);
		this.combinedBidHistory.add(bidDetails);
	}

	/**
	 * Returns the history of a particular agent
	 * 
	 * @param agentId
	 * @return
	 */
	public EnhancedBidHistory getHistory(AgentID agentId) {
		if (!bidHistories.containsKey(agentId))
			return new EnhancedBidHistory();
		return bidHistories.get(agentId);
	}

	/**
	 * Returns the combined history of all agents
	 * 
	 * @return
	 */
	public EnhancedBidHistory getCombinedHistory() {
		return this.combinedBidHistory;
	}

	/**
	 * Returns the set of all agent ids in this multipleBidHistory
	 * 
	 * @return
	 */
	public Set<AgentID> getAgentIds() {
		return this.bidHistories.keySet();
	}

	/**
	 * Returns the minimum toughness of all agents (so it returns the toughness
	 * of the toughest agent)
	 * 
	 * @return
	 */
	public double getMinToughness(int numberOfBids) {
		double min = 1;
		for (AgentID AgentId : this.getAgentIds()) {
			min = Math.min(min,
					this.getHistory(AgentId).getToughness(numberOfBids));
		}
		return min;
	}
}
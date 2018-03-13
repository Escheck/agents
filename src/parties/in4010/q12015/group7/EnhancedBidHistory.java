package parties.in4010.q12015.group7;

import java.util.ArrayList;
import java.util.List;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;

/**
 * Enhancement of the default BidHistory which in addition keeps record of the
 * distance of every bid compared to the previous bid.
 * 
 * @author svanbekhoven
 *
 */
public class EnhancedBidHistory extends BidHistory {
	/**
	 * Serial for serializing purposes
	 */
	private static final long serialVersionUID = 6182046030452141299L;

	/**
	 * ArrayList containing the distances of each bid towards the previous bid
	 */
	private ArrayList<Double> bidDistances;

	/**
	 * Constructor
	 */
	public EnhancedBidHistory() {
		this.bidDistances = new ArrayList<Double>();
	}

	/**
	 * Besides adding the bid to the BidHistory, also add the distance between
	 * the bid to be added and the previous bid to the ArrayList.
	 */
	public void add(BidDetails details) {
		if (this.getLastBid() != null) {
			bidDistances.add(distance(details.getBid(), this.getLastBid()));
		}
		super.add(details);
	}

	/**
	 * Return "toughness" of the bids in this BidHistory Toughness means the
	 * average distance of the last x bids
	 * 
	 * @param numberOfBids
	 * @return toughness
	 */
	public double getToughness(int numberOfBids) {
		if (this.bidDistances.size() == 0)
			return 1;

		double sum = 0.0;
		int count = 0;
		for (int i = this.bidDistances.size() - 1; i >= Math.max(0,
				this.bidDistances.size() - numberOfBids - 1); i--) {
			sum += this.bidDistances.get(i);
			count += 1;
		}

		return sum / ((double) count);
	}

	/**
	 * Calculates the distance between two bids
	 * 
	 * @param bid1
	 * @param bid2
	 * @return distance = (number of differing issues between bid1 and bid2) /
	 *         (number of issues)
	 */
	public double distance(Bid bid1, Bid bid2) {
		double distance = 0;
		List<Issue> issues = bid1.getIssues();
		for (Issue issue : issues) {
			try {
				if (!bid1.getValue(issue.getNumber()).equals(
						bid2.getValue(issue.getNumber()))) {
					distance += 1.0 / ((double) issues.size());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return distance;
	}

	public String toString() {
		String res = "EnhancedBidHistory[";
		for (BidDetails bd : getHistory()) {
			res += "(" + bd.getMyUndiscountedUtil() + ", " + bd.getBid()
					+ "\n\r";
		}
		return res + "]";
	}
}
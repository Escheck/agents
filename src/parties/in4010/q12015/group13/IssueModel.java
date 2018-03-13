package parties.in4010.q12015.group13;

import negotiator.issue.Value;

public interface IssueModel<V extends Value> {
	public void addBid(V v);

	public double estimateUtility(V v);
}

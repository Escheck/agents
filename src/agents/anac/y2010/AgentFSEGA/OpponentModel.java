package agents.anac.y2010.AgentFSEGA;

import negotiator.Bid;
import negotiator.Domain;

public abstract class OpponentModel
{
	protected Domain dDomain;

	public Domain getDomain()
	{
		return dDomain;
	}

	public abstract double getExpectedUtility(Bid pBid) throws Exception;
	
	public abstract void updateBeliefs(Bid pBid) throws Exception;
	
	public abstract double getExpectedWeight(int pIssueNumber);
}

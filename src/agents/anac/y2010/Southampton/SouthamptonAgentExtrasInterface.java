package agents.anac.y2010.Southampton;

import negotiator.Bid;
import negotiator.utility.AdditiveUtilitySpace;
import agents.anac.y2010.Southampton.utils.OpponentModel;

public interface SouthamptonAgentExtrasInterface {

	public abstract void postReceiveAccept(SouthamptonAgent agent, Bid myLastBid, AdditiveUtilitySpace utilitySpace, OpponentModel opponentModel);

	public abstract void postSendAccept(SouthamptonAgent agent, Bid myLastBid, AdditiveUtilitySpace utilitySpace, OpponentModel opponentModel, Bid opponentBid);

	public abstract void preProposeNextBid(SouthamptonAgent agent, Bid myLastBid, AdditiveUtilitySpace utilitySpace, OpponentModel opponentModel, Bid opponentBid);

	public abstract void postProposeNextBid(SouthamptonAgent agent, Bid myLastBid, AdditiveUtilitySpace utilitySpace, OpponentModel opponentModel, Bid bid)
			throws Exception;

	public abstract void chooseAction(SouthamptonAgent agent, long agentTimeSpent);

	public abstract void ReceiveMessage(SouthamptonAgent agent, long agentTimeSpent);

	public abstract void log(SouthamptonAgent agent, String message);

	public abstract void chooseAction();

	public abstract void ReceiveMessage();

}
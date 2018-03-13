package agents.anac.y2010.Southampton.utils;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;

/**
 * This factory class has been created to allow Actions to be constructed.
 * 
 * @author Colin Williams
 * 
 */
public class ActionCreator {

	public static Action createOffer(Agent agent, Bid bid) {
		return new Offer(agent.getAgentID(), bid);
	}

	public static Action createAccept(Agent agent, Bid oppBid) {
		return new Accept(agent.getAgentID(), oppBid);
	}

}

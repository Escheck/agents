package timeIndependantAgents;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import agents.SimpleAgent;
import negotiator.Agent;
import negotiator.Bid;
import negotiator.BidIterator;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.actions.Reject;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.timeline.Timeline;
import negotiator.utility.UtilitySpace;

/**
 * @author Matthes Koenen e1127522
 * 
 *         
 **/
public class StrictlyMonotonicAgent extends Agent {
	private Action actionOfPartner = null;
	private Bid lastPartnerBid;
	private TreeMap<Double, Bid> listOfAllBids;
	private Bid lastBid;
	private UtilitySpace privUt;
	

	/**
	 * Note: {@link SimpleAgent} does not account for the discount factor in its
	 * computations
	 */
	private static double MINIMUM_BID_UTILITY = 0.0;

	/**
	 * init is called when a next session starts with the same opponent.
	 */
	@Override
	public void init() {	
		try {
			lastBid = null;
			listOfAllBids = new TreeMap<Double, Bid>();
			getAllBids();
			//printAllBids();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return "Strictly Monotonic Concession Agent";
	}

	@Override
	public void ReceiveMessage(Action opponentAction) {
		actionOfPartner = opponentAction;
		if (actionOfPartner instanceof Offer) {
			lastPartnerBid = ((Offer) actionOfPartner).getBid();
		}
	}

	@Override
	public Action chooseAction() {
		Action action = null;

		try {
			if (lastBid == null){
				// First round. place our best bid.
				lastBid = utilitySpace.getMaxUtilityBid();
				action = new Offer(getAgentID(), lastBid);
			}

			if (actionOfPartner instanceof Offer || actionOfPartner instanceof Reject) {
				lastBid = listOfAllBids.get(listOfAllBids.lowerEntry(getUtil(lastBid)).getKey());
				//System.out.println(getAgentID()+ " " +getUtil(lastBid));

				//Check if our next offer is lower than our reservation level
				if(checkReject(lastBid)){
					action = new EndNegotiation(getAgentID());
					//Check if Opponent offer is acceptable
				}else if(checkAcceptance(lastPartnerBid)){
					action = new Accept(getAgentID(), lastBid);
				}else{
					action = new Offer(getAgentID(), lastBid);
				}
			}

		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			if (lastPartnerBid != null) {
				action = new Accept(getAgentID(), lastPartnerBid);
			} else {
				action = new EndNegotiation(getAgentID());
			}
		}
		return action;
	}

	private void getAllBids() throws Exception {
		BidIterator iterator = new BidIterator(utilitySpace.getDomain());
		while (iterator.hasNext()) {
			Bid tempBid = iterator.next();

			//System.out.println(getAgentID() + " " +tempBid + " util: " + getUtil(tempBid));

			listOfAllBids.put(getUtil(tempBid), tempBid);
		}	
	}

	private Double getUtil(Bid bid) throws Exception {
		// round util to 3 decimals, to limit our space.
		return Math.round(1000. * utilitySpace.getUtility(bid)) / 1000.;
		
	}

	private boolean checkAcceptance(Bid bid) throws Exception{
		if(getUtil(bid)>= getUtility(lastBid)){
			return true;
		}else{		
			return false;
		}
	}

	private boolean checkReject(Bid bid) throws Exception{
		if(getUtil(bid)<= utilitySpace.getReservationValue()){
			return true;
		}else{		
			return false;
		}
	}

	private void printAllBids(){
		for(Entry<Double, Bid> entry : listOfAllBids.entrySet()) {
			System.out.println(getAgentID() + " " +entry.getValue() + " util: " + entry.getKey());	
		}	
	}	

}



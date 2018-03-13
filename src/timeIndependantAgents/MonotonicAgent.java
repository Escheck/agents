package timeIndependantAgents;

import java.util.ArrayList;
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
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.UtilitySpace;

/**
 * @author Matthes Koenen e1127522
 * 
 *         
 **/
public class MonotonicAgent extends Agent {
	private Action actionOfPartner = null;
	private Bid lastPartnerBid;
	private Bid lastBid; // Last sent bid 
	private Bid actualBid; // bid to be sent next
	private TreeMap<Double, List<Bid>> listOfAllBids;  // TreeMap die als Key die Utility der bids hat und als Value eine Liste mit allen Bids die genau die utility haben
	private HashMap.Entry<Double, List<Bid>> oneEntry;  // Ein Eintrag der TreeMap der die Utility und alle Bids enthält die dies Utility haben
	private UtilitySpace utilitySpace2;  //nutzen um immer get max utility aufzurufen und dann den eintrag zu löschen. sodass das beste nächste angebot immer in maxUtility steht
	private int lastBidIndex;  // index des letzten bids der Liste mit der gleichen utlity
	
	
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
			actualBid = null;
			lastPartnerBid = null;
			lastBidIndex = 1;
			utilitySpace2 = utilitySpace.copy();
			listOfAllBids = new TreeMap<Double, List<Bid>>();
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
		return "Monotonic Concession Agent";
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
				actualBid = utilitySpace.getMaxUtilityBid();
				action = new Offer(getAgentID(), actualBid);
				lastBid = actualBid;
			}else if (actionOfPartner instanceof Offer) {
				double lastBidUtil = getUtil(lastBid);
				Entry<Double, List<Bid>> actualUtilEntryList = listOfAllBids.floorEntry(lastBidUtil);
				
				if (lastBidIndex < actualUtilEntryList.getValue().size()){
					actualBid = listOfAllBids.get(actualUtilEntryList.getKey()).get(lastBidIndex);
					lastBidIndex++;
				} else {
					actualUtilEntryList = listOfAllBids.lowerEntry(lastBidUtil);
					lastBidIndex = 0;
					actualBid = listOfAllBids.get(actualUtilEntryList.getKey()).get(lastBidIndex);
					lastBidIndex++;
				}
				
				System.out.println(getAgentID()+ " " +getUtil(actualBid));

				//Check if our next offer is lower than our reservation level
				if(checkReject(actualBid)){
					action = new EndNegotiation(getAgentID());
					//Check if Opponent offer is acceptable
				}else if(checkAcceptance(lastPartnerBid)){
					action = new Accept(getAgentID(), lastBid);
				}else{
					lastBid = actualBid;
					action = new Offer(getAgentID(), actualBid);
				}
			}else if (actionOfPartner instanceof Reject){
				//What should happen here?
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
			List<Bid> bidList = new ArrayList<Bid>();	
			if (listOfAllBids.get(getUtil(tempBid)) != null){
				bidList.addAll(listOfAllBids.get(getUtil(tempBid)));
			} 
			bidList.add(tempBid);	
			
			listOfAllBids.put(getUtil(tempBid), bidList);
		}	
		System.out.println("break");
	}


	private Double getUtil(Bid bid) throws Exception {
		// round util to 3 decimals, to limit our space.
		return Math.round(1000. * utilitySpace.getUtility(bid)) / 1000.;
	}

	private boolean checkAcceptance(Bid partnerBid) throws Exception{
		if(getUtil(partnerBid)>= getUtility(actualBid)){
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
		for(Entry<Double, List<Bid>> entry : listOfAllBids.entrySet()) {
			for(Bid b: entry.getValue()){
				System.out.println(getAgentID() + " Bid: " +b.toString() + " util: " + entry.getKey());		
			}
		}	
	}	

}



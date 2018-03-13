package timeIndependantAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.EvaluatorReal;
import negotiator.utility.UtilitySpace;

/**
 * @author Matthes Koenen e1127522
 * 
 *         
 **/
public class LexicographicAgent extends Agent {
	private Action actionOfPartner = null;
	private Bid lastPartnerBid;
	private Bid lastBid; // Last sent bid 
	private Bid actualBid; // bid to be sent next
	private TreeMap<Double, List<Bid>> listOfAllBids; // TreeMap die als Key die Utility der bids hat und als Value eine Liste mit allen Bids die genau die utility haben
	private TreeMap<Integer, Double> listOfWeightedIssues;
	private Evaluator discreteEvaluator;
	private AdditiveUtilitySpace castedAdditiveUtilitySpace;
	
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
			listOfAllBids = new TreeMap<Double, List<Bid>>();
			listOfWeightedIssues = new TreeMap<Integer, Double>();
			castedAdditiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
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
		return "Lexicographic Agent";
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
								
				listOfAllBids.remove(getWeightedUtiliy(actualBid));
				//listOfAllBids.get(getUtil(actualBid)).remove(actualBid);
			}else if (actionOfPartner instanceof Offer) {
				double lastBidUtil = getWeightedUtiliy(lastBid);
				boolean foundBid = false;
				while(listOfAllBids.floorEntry(lastBidUtil) != null && !foundBid) {
					
					Entry<Double, List<Bid>> actualUtilEntryList = listOfAllBids.floorEntry(lastBidUtil);
					for (Bid b: actualUtilEntryList.getValue()) {
						int differences = 0;
						Iterator<HashMap.Entry<Integer, Value>> iterator = b.getValues().entrySet().iterator();
						
						while (iterator.hasNext() && differences < 2) {
							HashMap.Entry<Integer, Value> entry = iterator.next();
							if(!entry.getValue().equals(lastBid.getValue(entry.getKey()))){
								differences++;
							}
						}
						if(differences==1){
							// Der Bid mit der höchsten Utility und einer unterschiedlichen Issue wurde gefunden
							actualBid = b;
							listOfAllBids.remove(getWeightedUtiliy(actualBid));
							foundBid=true;
							break;
						} 
						//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 		
					}
					//Alle Bids mit der gleichen Utility durchgegangen => Utility verringern
					lastBidUtil = listOfAllBids.lowerKey(lastBidUtil);
				}

				//System.out.println(getAgentID()+ " " +getUtil(actualBid));

				//Check if our next offer is lower than our reservation level
				if(checkReject(actualBid)){
					action = new EndNegotiation(getAgentID());
					//Check if Opponent offer is acceptable
				}else if(checkAcceptance(lastPartnerBid)){
					action = new Accept(getAgentID(), lastBid);
				}else{
					lastBid = actualBid;
				
						//System.out.println(getAgentID() +": " + actualBid.toString() + " Util: " + getWeightedUtiliy(actualBid));
				
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
		//Additiver utilitspace macht nichts anderes als issue.getWeight*issue.getValue() für jede Issue und summiert das auf.
		//Ich aendere das weight und multipliziere es mit Integer.MAX_VALUE. Damit sollte die Utility in erster Linie von der Issue mit den hoechsten weight abhaengen, 
		//da dieser Wert durch die multiplikation so gross wird dass alle anderen irrelevant sind
		//Vorgehen aus: Internet Multiattribute Group Decision Support in Electronic Commerce - Beroggi (2003), p 484
		
		//UtilitySpace und issues holen
		List<Issue> all_Issues= utilitySpace.getDomain().getIssues();	
		
		//Das Weight pro Issue holen
		for (Issue issue : all_Issues){
			double weight = castedAdditiveUtilitySpace.getWeight(issue.getNumber());
			listOfWeightedIssues.put(issue.getNumber(), weight);
			//System.out.println("Issue: "+issue + ", weight" + weight);
		}
		
		//Alle Weights der Issues mit Integer.MAX_VALUE multiplizieren und in die getAllBids TreeMap speichern. Dann kann in der Methode chooseAction die TreeMap wieder verwendet werden
		//wie in den anderen Agents auch
				
		BidIterator iterator = new BidIterator(utilitySpace.getDomain());
		while (iterator.hasNext()) {
			Bid tempBid = iterator.next();
			List<Bid> bidList = new ArrayList<Bid>(); //bidList enhaelt alle Bids mit der gleichen utility	
			//Ab hier unterscheidet sich der LexicograpficAgent. getUtil wird nicht aufgerufen sondern selber berechnet. 
			bidList.add(tempBid);
			//TODO TreeMap in Liste umwandeln? Reihenfolge ist durch die sortierung gegeben dann einfach von oben nach unten durch laufen.
			listOfAllBids.put(getWeightedUtiliy(tempBid), bidList);
		}	
	}

	
	private double getWeightedUtiliy(Bid bid){
		double util = 0;	
		double issueUtil = 0;
		for(Issue issue: bid.getIssues()){
			issueUtil = castedAdditiveUtilitySpace.getEvaluation(issue.getNumber(), bid);	
			util += issueUtil * listOfWeightedIssues.get(issue.getNumber()) * Integer.MAX_VALUE;
		}
		return util;
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



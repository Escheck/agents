package timeIndependantAgents;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import com.sun.xml.bind.v2.runtime.reflect.ListIterator;

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
	private List<Bid> listOfAllBids; // Liste die alle Bids enthaelt. Wird Sortiert, darum muessen die Offers der Liste nur noch noch der Reihe nach durchgegangen werden
	private TreeMap<Integer, Double> listOfWeightedIssues;
	private AdditiveUtilitySpace castedAdditiveUtilitySpace;
	private List<Bid> sentOffers;
	private List<Bid> allBidsList;
	private List<Issue> sortByWeightIssueList;
	private Integer ArrayPositionNextOffer;
	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";

	//CSV file header
	private String FILE_HEADER = "Agent,";
	/**
	 * init is called when a next session starts with the same opponent.
	 */
	@Override
	public void init() {	
		try {
			lastBid = null;
			actualBid = null;
			lastPartnerBid = null;
			listOfWeightedIssues = new TreeMap<Integer, Double>();
			castedAdditiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
			sentOffers = new ArrayList<Bid>();
			allBidsList = new ArrayList<Bid>();
			sortByWeightIssueList = new ArrayList<Issue>();
			getAllBidsList();
			printAllBids(allBidsList, "AllBids_"+getAgentID()+getName());
			getOrderedIssueList();
			ArrayPositionNextOffer = 0;
			listOfAllBids = getAllBids();
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
				actualBid = listOfAllBids.get(ArrayPositionNextOffer);
				ArrayPositionNextOffer++;
				action = new Offer(getAgentID(), actualBid);
				lastBid = actualBid;
				sentOffers.add(actualBid);
				
				//listOfAllBids.get(getUtil(actualBid)).remove(actualBid);
			}else if (actionOfPartner instanceof Offer) {
				double utilNextBid = getUtil(listOfAllBids.get(ArrayPositionNextOffer));
				double utilPrevBid = getUtil(listOfAllBids.get(ArrayPositionNextOffer-1));
				while(utilNextBid > utilPrevBid){
					ArrayPositionNextOffer++;
					utilNextBid = getUtil(listOfAllBids.get(ArrayPositionNextOffer));
				}
				actualBid = listOfAllBids.get(ArrayPositionNextOffer);
				ArrayPositionNextOffer++;
				action = new Offer(getAgentID(), actualBid);
				//Check if our next offer is lower than our reservation level
				if(checkReject(actualBid)){
					action = new EndNegotiation(getAgentID());
					//Check if Opponent offer is acceptable
				}else if(checkAcceptance(lastPartnerBid)){
					action = new Accept(getAgentID(), lastBid);

				}else{
					lastBid = actualBid;				
					sentOffers.add(actualBid);
					action = new Offer(getAgentID(), actualBid);
				}
			}else if (actionOfPartner instanceof Reject){
				//What should happen here?
			}
			this.printAllBids(sentOffers, getAgentID().toString()+getName());
			
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
	
	private List<Bid> getAllBids() throws Exception {
		BidIterator iterator = new BidIterator(utilitySpace.getDomain());
		List<Bid> bidList = new ArrayList<Bid>();
		while (iterator.hasNext()) {
			Bid tempBid = iterator.next();
			bidList.add(tempBid);
		}
		orderLexico(bidList);
		return bidList;
	}
	
	private void getOrderedIssueList() throws Exception {
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
			sortByWeightIssueList.add(issue);
			//System.out.println("Issue: "+issue + ", weight" + weight);
		}

		Collections.sort(sortByWeightIssueList , new Comparator<Issue>() {
			@Override
			public int compare(Issue issue1, Issue issue2)
			{	
				return new Double(castedAdditiveUtilitySpace.getWeight(issue2.getNumber())).compareTo(castedAdditiveUtilitySpace.getWeight(issue1.getNumber()));
			}
		});
	}
	
	private void orderLexico(List<Bid> list){
		Collections.sort(list, new Comparator<Bid>() {
			@Override
			public int compare(Bid bid1, Bid bid2)
			{	
				for (Issue issue: sortByWeightIssueList){
					double valueIssue1 = castedAdditiveUtilitySpace.getEvaluation(issue.getNumber(), bid1);
					double valueIssue2 = castedAdditiveUtilitySpace.getEvaluation(issue.getNumber(), bid2);
					int compareValue = new Double(valueIssue2).compareTo(valueIssue1);				        	
					if (compareValue != 0) {
						return compareValue; 
					}
				}
				//Beide bids sind identisch. Sollte niemals rauskommen da irgendwann in der for Schleife ein Unterschied sein muss. 
				return 0;
			}
		});
	}
	
	private Double getUtil(Bid bid) throws Exception {
		// round util to 3 decimals, to limit our space.
		return utilitySpace.getUtility(bid);
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

	private void printAllBids(List<Bid> bids, String filename) throws FileNotFoundException, UnsupportedEncodingException{
		//		for(Entry<Double, List<Bid>> entry : listOfAllBids.entrySet()) {
		//			for(Bid b: entry.getValue()){
		//				System.out.println(getAgentID() + " Bid: " +b.toString() + " util: " + entry.getKey());		
		//			}
		//		}
		PrintWriter writer = new PrintWriter("C:\\Users\\Matthes\\Downloads\\" + filename + ".csv", "UTF-8");
		FILE_HEADER = "Agent,";

		for (Issue issue: utilitySpace.getDomain().getIssues()){
			double weight = castedAdditiveUtilitySpace.getWeight(issue.getNumber());
			FILE_HEADER = FILE_HEADER + issue +"(Weight: " + weight + " )"+ COMMA_DELIMITER + issue+"-Utility"+ COMMA_DELIMITER;
		}
		FILE_HEADER = FILE_HEADER + "Utility";
		writer.println(FILE_HEADER);

		for(Bid tempBid: bids){
			String line = getAgentID() + COMMA_DELIMITER;
			for (Entry<Integer, Value> entry: tempBid.getValues().entrySet()){
				line = line + entry.getValue() + COMMA_DELIMITER + castedAdditiveUtilitySpace.getEvaluation(entry.getKey(), tempBid)  + COMMA_DELIMITER ;
			}
			writer.println(line + utilitySpace.getUtility(tempBid));
		}
		writer.flush();
		writer.close();
	}	

	private void getAllBidsList(){
		BidIterator iterator = new BidIterator(utilitySpace.getDomain());
		while(iterator.hasNext()){
			allBidsList.add(iterator.next());
		}
	}

}



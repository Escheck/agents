package timeIndependantAgents;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.UtilitySpace;

/**
 * @author Matthes Koenen e1127522
 * 
 *         
 **/
public class LeastCostIssueAgent extends Agent {
	private Action actionOfPartner = null;
	private Bid lastPartnerBid;
	private Bid lastBid; // Last sent bid 
	private Bid actualBid; // bid to be sent next
	private TreeMap<Double, List<Bid>> listOfAllBids;  // TreeMap die als Key die Utility der bids hat und als Value eine Liste mit allen Bids die genau die utility haben
	private HashMap.Entry<Double, List<Bid>> oneEntry;  // Ein Eintrag der TreeMap der die Utility und alle Bids enthält die dies Utility haben
	private UtilitySpace utilitySpace2;  //nutzen um immer get max utility aufzurufen und dann den eintrag zu löschen. sodass das beste nächste angebot immer in maxUtility steht
	private AdditiveUtilitySpace castedAdditiveUtilitySpace;	
	private List<Bid> sentOffers;
	private List<Bid> allBidsList;

	/**
	 * Note: {@link SimpleAgent} does not account for the discount factor in its
	 * computations
	 */
	private static double MINIMUM_BID_UTILITY = 0.0;
	//Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
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
			utilitySpace2 = utilitySpace.copy();
			listOfAllBids = new TreeMap<Double, List<Bid>>();
			sentOffers = new ArrayList<Bid>();
			allBidsList = new ArrayList<Bid>();
			castedAdditiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
			getAllBids();
			getAllBidsList();
			printAllBids(allBidsList, "AllBids_"+getAgentID()+getName());
			
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
		return "Least-Cost-Issue Agent";
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
				listOfAllBids.get(getUtil(actualBid)).remove(actualBid);
				sentOffers.add(actualBid);
			}else if (actionOfPartner instanceof Offer) {
				double lastBidUtil = getUtil(lastBid);
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
							listOfAllBids.get(getUtil(b)).remove(b);
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

	private void getAllBids() throws Exception {
		BidIterator iterator = new BidIterator(utilitySpace.getDomain());
		while (iterator.hasNext()) {
			Bid tempBid = iterator.next();

			//System.out.println(getAgentID() + " " +tempBid + " util: " + getUtil(tempBid));
			List<Bid> bidList = new ArrayList<Bid>(); //bidList enhaelt alle Bids mit der gleichen utility	
			double util = getUtil(tempBid);
			if (listOfAllBids.get(getUtil(tempBid)) != null){
				bidList.addAll(listOfAllBids.get(util));
			} 
			bidList.add(tempBid);	

			listOfAllBids.put(getUtil(tempBid), bidList);
		}	
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



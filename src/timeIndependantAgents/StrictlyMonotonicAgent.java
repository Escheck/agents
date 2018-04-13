package timeIndependantAgents;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import negotiator.utility.AdditiveUtilitySpace;
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
	private AdditiveUtilitySpace castedAdditiveUtilitySpace;
	private List<Bid> sentOffers;
	private List<Bid> allBidsList;

	/**
	 * Note: {@link SimpleAgent} does not account for the discount factor in its
	 * computations
	 */
	private static double MINIMUM_BID_UTILITY = 0.0;
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
			listOfAllBids = new TreeMap<Double, Bid>();
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
				sentOffers.add(lastBid);
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
					sentOffers.add(lastBid);
				}
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
			
			listOfAllBids.put(getUtil(tempBid), tempBid);
		}	
	}

	private Double getUtil(Bid bid) throws Exception {
		return utilitySpace.getUtility(bid);
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



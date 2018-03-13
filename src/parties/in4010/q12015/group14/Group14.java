package parties.in4010.q12015.group14;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
//import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.OutcomeSpace;
//import negotiator.issue.Issue;
//import negotiator.issue.IssueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group14 extends AbstractNegotiationParty {

	private Random randomGen = new Random();
	private Action lastAction = null;
	private AdditiveUtilitySpace myUtilSpace;
	private TimeLineInfo myTimeLine;
	private OutcomeSpace outSpace;
	private List<BidDetails> allbids, allbidsByNash; // eventually a sorted list
														// of all bids
	private List<BidDetails> firstSetBids, secondSetBids, thirdSetBids, fourthSetBids, fifthSetBids, lastSetBids;
	// a hash map from the opponent agents to the opponentmodelling objects
	private HashMap<String, OpponentModelling> oppModelHashMap = new HashMap<String, OpponentModelling>();
	private BidHistory oppBidHistory;

	private int count = 0;
	// private int count2 = 0;
	private int roundNr = 0;

	/**
	 * This method is called to initialize our agent
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		firstSetBids = new ArrayList<BidDetails>();
		secondSetBids = new ArrayList<BidDetails>();
		thirdSetBids = new ArrayList<BidDetails>();
		fourthSetBids = new ArrayList<BidDetails>();
		fifthSetBids = new ArrayList<BidDetails>();
		lastSetBids = new ArrayList<BidDetails>();
		oppBidHistory = new BidHistory();
		myUtilSpace = (AdditiveUtilitySpace) info.getUtilitySpace();
		myTimeLine = info.getTimeline();
		outSpace = new OutcomeSpace(info.getUtilitySpace());
		allbids = outSpace.getAllOutcomes(); // get all possible outcomes
		Collections.sort(allbids); // sort the in descending order
		// allbids.remove(0); //remove the best bid in the list
		makeLists(allbids);
	};

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// No offer to consider so we propose the one with max utility for us
		roundNr++;

		if (!validActions.contains(Accept.class))
			return maxUtilBid(myUtilSpace);
		/*
		 * if (lastAction instanceof Offer){ //Bid bidOfOffer=((Offer)
		 * lastAction).getBid();
		 * 
		 * //Analysis on Opponent Modeling if ((myTimeLine.getTime()>0.1 &&
		 * count2==0) || (myTimeLine.getTime()>0.2 && count==1) ||
		 * (myTimeLine.getTime()>0.3 && count==2) || (myTimeLine.getTime()>0.4
		 * && count==3) || (myTimeLine.getTime()>0.5 && count==4) ||
		 * (myTimeLine.getTime()>0.6 && count==5) || (myTimeLine.getTime()>0.7
		 * && count==6) || (myTimeLine.getTime()>0.8 && count==7) ||
		 * (myTimeLine.getTime()>0.9 && count==8)){ System.out.println("TIME: "
		 * + Double.toString(myTimeLine.getTime())); for(Map.Entry<String,
		 * OpponentModelling> entry: oppModelHashMap.entrySet()){
		 * System.out.println("The name of the agent is :"+entry.getKey());
		 * entry.getValue().printOppModel(); } count2++; } }
		 */

		// At certain times, we want to evaluate our bids using the latest
		// oppModel
		if ((myTimeLine.getTime() > 0.4 && count == 0) || (myTimeLine.getTime() > 0.55 && count == 1)
				|| (myTimeLine.getTime() > 0.65 && count == 2) || (myTimeLine.getTime() > 0.75 && count == 3)
				|| (myTimeLine.getTime() > 0.85 && count == 4) || (myTimeLine.getTime() > 0.95 && count == 5)) {
			int phase = count + 1;
			System.out.println("Starting phase " + phase + " at t=" + myTimeLine.getTime() + " and round=" + roundNr);
			allbidsByNash = orderListByNash(allbids);
			makeLists(allbidsByNash);

			count++;
		}

		return myOffer();
		// return new EndNegotiation();

	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		/*
		 * if(action instanceof Offer){ lastAction=action; }
		 */
		if (action instanceof Offer) {
			lastAction = action;
			Bid curBid = DefaultAction.getBidFromAction(lastAction);
			if (!(sender == null)) { // if don't have ourselves as a
										// bid offerer
				// update the opponent modelling for this agent given its offer
				if (!oppModelHashMap.containsKey(sender.toString())) {
					oppModelHashMap.put(sender.toString(), new OpponentModelling(sender.toString(),
							myUtilSpace.getDomain().getIssues(), curBid, getPartyId()));
					// here we get the very first offer of the current agent
				} else {
					oppModelHashMap.get(sender.toString()).updateModel(curBid);
				}

				// add the offer to the bid history
				Bid bidOfLastOffer = ((Offer) lastAction).getBid();
				BidDetails lastbd = new BidDetails(bidOfLastOffer, getUtility(bidOfLastOffer), myTimeLine.getTime());
				oppBidHistory.add(lastbd);
			}

		}

		// System.out.println("The sender of this action is
		// "+sender.toString());
	}

	/**
	 * We have to respond with an Action after we consult our acceptance
	 * strategy
	 * 
	 * @return An Action which is either Accept or a new Offer by us
	 */
	public Action myOffer() {
		Offer oppOffer = (Offer) lastAction;
		BidDetails ourNextBid;
		if (myTimeLine.getTime() < 0.4) { // timeslot 1
			ourNextBid = new BidDetails(maxUtilBid(myUtilSpace).getBid(), getUtility(maxUtilBid(myUtilSpace).getBid()));
		} else if (myTimeLine.getTime() < 0.55) { // timeslot 2.1
			int index = randomGen.nextInt(firstSetBids.size());
			ourNextBid = firstSetBids.get(index);
		} else if (myTimeLine.getTime() < 0.65) { // timeslot 2.2
			int index = randomGen.nextInt(secondSetBids.size());
			ourNextBid = secondSetBids.get(index);
		} else if (myTimeLine.getTime() < 0.75) { // timeslot 2.3
			int index = randomGen.nextInt(thirdSetBids.size());
			ourNextBid = thirdSetBids.get(index);
		} else if (myTimeLine.getTime() < 0.85) { // timeslot 2.4
			int index = randomGen.nextInt(fourthSetBids.size());
			ourNextBid = fourthSetBids.get(index);
		} else if (myTimeLine.getTime() < 0.95) { // timeslot 2.5
			int index = randomGen.nextInt(fifthSetBids.size());
			ourNextBid = fifthSetBids.get(index);
		} else { // timeslot 3
			int index = randomGen.nextInt(lastSetBids.size());
			ourNextBid = lastSetBids.get(index);
		}

		// if our utility from this offer is bigger than that we are going to
		// propose we have to accept
		if (getUtility(oppOffer.getBid()) >= getUtility(ourNextBid.getBid())) {
			return new Accept(getPartyId(), oppOffer.getBid());
		} else {
			// System.out.println("Time is:"+myTimeLine.getTime()+" and the
			// offer's utility is :"+getUtility(ourNextBid.getBid()));
			return new Offer(getPartyId(), ourNextBid.getBid());
		}
	}

	/**
	 * Divides the bidList into separate lists filtered for certain Utility
	 * intervals
	 * 
	 * @param bidList
	 */
	public void makeLists(List<BidDetails> bidList) {
		firstSetBids.clear();
		secondSetBids.clear();
		thirdSetBids.clear();
		fourthSetBids.clear();
		fifthSetBids.clear();
		lastSetBids.clear();

		// 1st range: [1.0,0.9], 2nd range: [0.95,0.8], 3rd range: [0.85,0.75]
		for (BidDetails bd : bidList) {
			double util = getUtility(bd.getBid());
			if (util > 0.95) {
				firstSetBids.add(bd);
			}
			if (util > 0.9) {
				secondSetBids.add(bd);
			}
			if (util > 0.85) {
				thirdSetBids.add(bd);
			}
			if (util > 0.8) {
				fourthSetBids.add(bd);
			}
			if (util > 0.75) {
				fifthSetBids.add(bd);
			}
		}

		BidDetails maxUtilbd = new BidDetails(maxUtilBid(myUtilSpace).getBid(),
				getUtility(maxUtilBid(myUtilSpace).getBid()));

		// System.out.println("list sizes are 2.1:"+firstSetBids.size()+" -
		// 2.2:"+secondSetBids.size()+" - 2.3:"+thirdSetBids.size()+" -
		// 2.4:"+fourthSetBids.size()+" - 2.5:"+fifthSetBids.size());

		// 1
		if (firstSetBids.isEmpty())
			System.err.println("firstSetBids is empty at t=" + myTimeLine.getTime());
		else if (firstSetBids.size() > 10)
			firstSetBids = firstSetBids.subList(0, 10);
		firstSetBids.add(maxUtilbd);

		// 2
		if (secondSetBids.isEmpty())
			System.err.println("secondSetBids is empty at t=" + myTimeLine.getTime());
		else if (secondSetBids.size() > 12)
			secondSetBids = secondSetBids.subList(0, 12);
		secondSetBids.add(maxUtilbd);

		// 3
		if (thirdSetBids.isEmpty())
			System.err.println("thirdSetBids is empty at t=" + myTimeLine.getTime());
		else if (thirdSetBids.size() > 15)
			thirdSetBids = thirdSetBids.subList(0, 15);
		thirdSetBids.add(maxUtilbd);

		// 4
		if (fourthSetBids.isEmpty())
			System.err.println("fourthSetBids is empty at t=" + myTimeLine.getTime());
		else if (fourthSetBids.size() > 18)
			fourthSetBids = fourthSetBids.subList(0, 18);
		fourthSetBids.add(maxUtilbd);

		// 5
		if (fifthSetBids.isEmpty())
			System.err.println("fifthSetBids is empty at t=" + myTimeLine.getTime());
		else if (fifthSetBids.size() > 20)
			fifthSetBids = fifthSetBids.subList(0, 20);
		fifthSetBids.add(maxUtilbd);

		// Lastly the list using BidHistory
		// if(oppBidHistory.size()>10)
		lastSetBids = oppBidHistory.getNBestBids(10);
		// else
		// lastSetBids = oppBidHistory.getHistory();

	}

	/**
	 * Order the list of bids by descending estimated Nash product
	 * 
	 * @param bidList
	 * @return The newly ordered List of bids
	 */
	public List<BidDetails> orderListByNash(List<BidDetails> bidList) {
		// System.out.println("1 size = "+bidList.size());

		List<BidDetails> orderedList = new ArrayList<BidDetails>();
		orderedList.add(bidList.get(1));

		List<Double> nashProdList = new ArrayList<Double>();
		double nashProd = bidList.get(0).getMyUndiscountedUtil();
		for (Map.Entry<String, OpponentModelling> entry : oppModelHashMap.entrySet()) {
			nashProd = nashProd * entry.getValue().getOppUtil(bidList.get(1).getBid());
		}
		nashProdList.add(nashProd);

		BidDetails bd;
		for (int j = 2; j < bidList.size(); j++) {
			bd = bidList.get(j);
			nashProd = bd.getMyUndiscountedUtil();
			for (Map.Entry<String, OpponentModelling> entry : oppModelHashMap.entrySet()) {
				nashProd = nashProd * entry.getValue().getOppUtil(bd.getBid());
			}
			for (int i = 0; i < nashProdList.size(); i++) {
				if (nashProd > nashProdList.get(i)) {
					orderedList.add(i, bd);
					nashProdList.add(i, nashProd);
					i = nashProdList.size();
				}
				if (i == nashProdList.size() - 1) {
					orderedList.add(i, bd);
					nashProdList.add(i, nashProd);
					i = nashProdList.size();
				}
			}
		}
		// System.out.println("1 size = "+orderedList.size());
		return orderedList;
	}

	/**
	 * Returns the bid with the highest Utility for us out of the whole
	 * UtilitySpace
	 * 
	 * @param us
	 *            The UtilitySpace object
	 * @return The Offer with maximum utility, in case of exception an offer
	 *         with a random bid
	 */
	public Offer maxUtilBid(AdditiveUtilitySpace us) {
		try {
			return new Offer(getPartyId(), us.getMaxUtilityBid());
		} catch (Exception e) {
			System.err.println("Error generating maximum utility!");
			e.printStackTrace();
			return new Offer(getPartyId(), generateRandomBid());
		}
	}

	/**
	 * Just a string describing Group14 Agent
	 */
	@Override
	public String getDescription() {
		return "Party Group 14";
	}

}

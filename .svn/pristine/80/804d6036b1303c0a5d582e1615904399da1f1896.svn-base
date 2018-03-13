package agents.ai2014.group3;

import java.util.ArrayList;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.DeadlineType;
//import negotiator.NegoRound;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group3 extends AbstractNegotiationParty {

	private int roundN = 0; // Number of the round we are in
	private Bid lastbid; // LastBid we received from someone
	private List<BidHistory> bidhistorylist; // List of the lists of Bids
												// received, one list for every
												// agent
	private List<AgentID> partylist; // List of Agents
	private ArrayList<Bid> possibleBids; // List of all possible Bids in this
											// utility Space
	private ArrayList<Bid> alreadyProposed; // List of all Bids, we received and
											// then reproposed
	private ArrayList<AgentUtils> agentUtilsList;

	// List of AgentUtils that are objects that model the opponents utility
	// function and give their utility to a specific Bid

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		// Initialize the lists
		bidhistorylist = new ArrayList<BidHistory>();
		partylist = new ArrayList<AgentID>();
		alreadyProposed = new ArrayList<Bid>();
		possibleBids = BidGenerator.BidList(((AdditiveUtilitySpace) utilitySpace));
		agentUtilsList = new ArrayList<AgentUtils>();

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		roundN++; // Update the round number
		// System.out.println("Round N� "+ roundN);
		// System.out.println("I am " + this.getPartyId().toString());
		if (!validActions.contains(Accept.class)) {
			// No Offer on the table yet

			// Generate all possible bids for this utility space
			double[] utils = BidGenerator.utilitylist(possibleBids, this);
			// for(int i=0;i<possibleBids.size();i++) {
			// System.out.println("Bid N� "+ i+" utility: " + utils[i] ) ;
			// }

			// System.out.println("Deadline"+ deadlines.toString());
			// System.out.println("Deadline "+ this.roundDeadline());

			// Offer Maximum Utility
			try {
				// System.out.println("Offering maximum");
				return new Offer(getPartyId(), utilitySpace.getMaxUtilityBid());
			} catch (Exception e) {
				e.printStackTrace();
				return new Offer(getPartyId(), generateRandomBid());
			}
		}

		// If accepting conditions are met, accept
		if (Strategy.acceptingConditions(this)) {
			// System.out.println("Accepting");
			return new Accept(getPartyId(), lastbid);
		}

		// System.out.println("My Next Bid Utility"+
		// Strategy.nextBidUtility(this));

		// Check if we should offer an offer we received already, we will only
		// resend an offer one time
		if (Strategy.offerPreviousOffer(this)) {
			Bid toOffer = Strategy.bestPreviousBid(this);

			// Add this offer to list so we don't send it again
			alreadyProposed.add(toOffer);
			// System.out.println("Offering previous bid, of Utility "+
			// getUtility(toOffer));
			return new Offer(getPartyId(), toOffer);
		}

		// Generate new offer with that desired utility
		Bid toOffer = Strategy.calculateMyBid(this);
		// System.out.println("Generating a new Bid, of Utility "+
		// getUtility(toOffer));
		return new Offer(getPartyId(), toOffer);
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
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		// Here you can listen to other parties' messages

		if (action instanceof Offer) {
			if (!partylist.contains(/* action.getAgent() */(AgentID) sender)) {
				// We have never seen this agent
				// System.out.println("New Agent: " +
				// action.getAgent().toString());
				partylist.add((AgentID) sender); // add
													// it
													// to
													// our
													// list
				BidHistory newAgentBidHistory = new BidHistory(); // create a
																	// new agent
																	// list and
																	// add it.
				bidhistorylist.add(newAgentBidHistory);
				// create a new agentUtils and add it
				AgentUtils newAgentUtils = new AgentUtils(((AgentID) sender), newAgentBidHistory,
						((AdditiveUtilitySpace) utilitySpace).getNrOfEvaluators());
				agentUtilsList.add(newAgentUtils);
			}
			lastbid = DefaultAction.getBidFromAction(action);
			// add the bid to the bidhistory
			bidhistorylist.get(partylist.indexOf(((AgentID) sender))).add(new BidDetails(lastbid, getUtility(lastbid)));
			// Loop through our agent List to find the one that sent the message
			// and update his AgentUtils
			for (int i = 0; i < agentUtilsList.size(); i++) {
				if (agentUtilsList.get(i).agent == ((AgentID) sender)) {
					agentUtilsList.get(i).recalculateUtilFunction();
					break;
				}
			}
			// System.out.println(this.getPartyId().toString() +
			// " Received bid of utility:" + getUtility(lastbid));
		}

	}

	// Extra methods

	public ArrayList<AgentUtils> getAgentUtilsList() {
		return agentUtilsList;
	}

	public int roundDeadline() {
		return getDeadlines().getType() == DeadlineType.ROUND ? getDeadlines().getValue() : 0;
	}

	public int getRoundN() {
		return roundN;
	}

	public List<AgentID> getPartylist() {
		return partylist;
	}

	public Bid getLastbid() {
		return lastbid;
	}

	public BidHistory getBidhistory(AgentID Agent) {
		return bidhistorylist.get(partylist.indexOf(Agent));
	}

	public ArrayList<Bid> getAlreadyProposed() {
		return alreadyProposed;
	}

	public ArrayList<Bid> getPossibleBids() {
		return possibleBids;
	}

	protected AgentID partyId = new AgentID("Group 3");

	@Override
	public String getDescription() {
		return "ai2014 group3";
	}

}

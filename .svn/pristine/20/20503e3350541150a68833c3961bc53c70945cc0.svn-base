package parties.in4010.q12015.group10;

import java.util.ArrayList;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Negotiation party
 */
public class Group10 extends AbstractNegotiationParty {

	private int numberOfOpponents; // Number of opponents in this session. The
									// protocol tells us this
	private Opponent[] opponents; // Array of opponents. Initially empty (until
									// we know how many opponents there are)
	private List<AgentID> opponentAgentIDList = new ArrayList<AgentID>(); // List
																			// of
																			// opponent
																			// AgentIDs.
																			// Not
																			// the
																			// actual
																			// opponents,
																			// just
																			// their
																			// IDs.
	private BidDetails detailsOfLatestBidOnTable; // Details of latest bid.
													// Either made by us, or by
													// the opponent.
	private Deadline deadLine;

	private OpponentStrategyEstimator myOpponentStrategyEstimator;

	/**
	 * Initialization. This adds to the existing initialization so we can
	 * initialize our own variables, too.
	 */
	@Override
	public void init(NegotiationInfo info) {
		// Original (hidden) initialization
		super.init(info);

		// Own initialization code
		deadLine = info.getDeadline();
	}

	// Here we create and initialize opponents. Note that this is called only
	// after the protocol
	// informs us about how many agents take part in this negotiation.
	private void initOpponents() {
		// Make array of objects
		opponents = new Opponent[numberOfOpponents];

		// Initialize each of them by calling their constructors
		for (int opponentNumber = 0; opponentNumber < numberOfOpponents; opponentNumber++) {
			opponents[opponentNumber] = new Opponent(opponentNumber, (AdditiveUtilitySpace) utilitySpace, deadLine);
		}
	}

	/**
	 * All protocol information and opponent actions will be received as a
	 * message. This function determines if the data comes from the protocol or
	 * an opponent, and starts the appropriate function to process it.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		// Determine who is sending something. Not sure this is the best way to
		// go.
		if (sender == null) {
			// We are receiving a message from the protocol. Go Process it.
			receiveProtocolMessage(action);
		} else {
			// We are receiving a message from an agent. Go Process it.
			receiveOpponentMessage(action.getAgent(), action);
		}

	}

	/**
	 * All protocol information is processed here
	 */

	// This runs when the ReceiveMessage function detects that the Protocol
	// sends something
	private void receiveProtocolMessage(Action action) {
		// Check that the action is of the type inform
		if (action instanceof Inform) {
			// Explicitly make it of type inform
			Inform InformMessage = (Inform) action;

			// Get the name of the inform message
			String messageName = InformMessage.getName();

			// For each message we can expect, define what to do
			switch (messageName) {
			case "NumberOfAgents":
				// If we get information about the number of agents, store it,
				// and initialize the agents.
				numberOfOpponents = (int) InformMessage.getValue() - 1; // Don't
																		// count
																		// myself
				initOpponents(); // Run the opponent initialization.
				break;
			default:
			}
		}
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * It is processed here.
	 */

	// This runs when the ReceiveMessage function detects that an opponent sends
	// something
	private void receiveOpponentMessage(AgentID agentID, Action action) {

		double timeNow = timeline.getTime();

		int opponentNumber; // Soon to be found numeric value associated with
							// the agent that sends a message

		// See if we encountered this agent before
		if (opponentAgentIDList.contains(agentID)) {
			// Opponent has been seen befor, his number is:
			opponentNumber = opponentAgentIDList.indexOf(agentID);
		} else {// We see this agent for the first time.

			// The opponents are numbered in the order we first encounter them.
			// At first, agentIDList has length 0, so the first opponent will
			// get
			// number 0. The next opponent will get number 1, and son on.
			opponentNumber = opponentAgentIDList.size();

			opponentAgentIDList.add(agentID); // Add it to the list of agents
			opponents[opponentNumber].setAgentID(agentID);// Store the ID in the
															// appropriate
															// object.
		}

		// Now that we know the opponent number, we can check out what he's
		// doing, such as offering or bidding

		if (action instanceof Offer) {
			// The opponent has made a new offer.
			Bid latestBidOnTable = DefaultAction.getBidFromAction(action); // Get
																			// the
			// bid that
			// he
			// offered.
			double UndiscountedUtilofLatestBid = getUtility(latestBidOnTable); // Find
																				// the
																				// corresponding
																				// utility
																				// for
																				// group
																				// 10
			detailsOfLatestBidOnTable = new BidDetails(latestBidOnTable, UndiscountedUtilofLatestBid, timeNow); // Combine
																												// the
																												// offer,
																												// utility
																												// and
																												// time
																												// in
																												// the
																												// BidDetailsObject.

			// Store the details of which offer the opponent is making
			opponents[opponentNumber].StoreOfferedBid(detailsOfLatestBidOnTable);
		} else if (action instanceof Accept) {
			// The opponent is accepting the bid that is currently on the table
			opponents[opponentNumber].StoreAcceptedBid(detailsOfLatestBidOnTable); // Store
																					// the
																					// details
																					// of
																					// which
																					// offer
																					// he
																					// is
																					// accepting
		}

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 */
	@Override
	public Action chooseAction(java.util.List<Class<? extends Action>> validActions) {
		OpponentModelEstimator.updateAllModels(opponents, timeline);

		Bid potentialBid = OfferingStrategy.createPotentialBid((AdditiveUtilitySpace) utilitySpace, opponents, timeline,
				deadLine);

		// Create a BidDetails object of our potential bid
		double timeNow = timeline.getTime(); // The current time
		double UndiscountedUtilofPotentialBid = getUtility(potentialBid); // Find
																			// the
																			// corresponding
																			// utility
																			// for
																			// group
																			// 10
		BidDetails detailsOfPotentialBid = new BidDetails(potentialBid, UndiscountedUtilofPotentialBid, timeNow); // Combine
																													// the
																													// offer,
																													// utility
																													// and
																													// time
																													// in
																													// the
																													// BidDetailsObject.

		// Based on the valid actions, potential and latest bid on the table,
		// choose what to do.
		Action chosenAction = AcceptanceStrategy.getAction(validActions, detailsOfPotentialBid,
				detailsOfLatestBidOnTable, getPartyId());

		// Return our decision to Genius
		return chosenAction;

	}

	/**
	 * Description of our agent that appears in Genius
	 */

	@Override
	public String getDescription() {
		return "Group10";
	}

}

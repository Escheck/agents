package agents.ai2014.group5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.xml.SimpleElement;

/**
 * This is our negotiation agent. It uses the BOA framework.
 */
public class Group5 extends AbstractNegotiationParty {
	// The bidding strategy
	private BiddingStrategy bidding;

	// Approximate models of each opponent's utility functions
	private Map<String, OpponentModel> opponentModels;

	// Issues (issue index-1 and issue name)
	private List<String> issues;

	// Values (issue index-1, value names, and value index-1)
	private List<Map<String, Integer>> values;

	// Values (issue index-1, value index-1, value names)
	private List<Map<Integer, String>> valuesRev;

	/**
	 * This constructor is called by genius.
	 *
	 * @param utilitySpace
	 *            Your utility space.
	 * @param deadlines
	 *            The deadlines set for this negotiation.
	 * @param timeline
	 *            Value counting from 0 (start) to 1 (end).
	 * @param randomSeed
	 *            If you use any randomization, use this seed for it.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		List<Issue> domainIssues = utilitySpace.getDomain().getIssues();

		opponentModels = new HashMap<String, OpponentModel>();
		issues = new ArrayList<String>(Collections.nCopies(domainIssues.size(), (String) null));
		values = new ArrayList<Map<String, Integer>>(
				Collections.nCopies(domainIssues.size(), (Map<String, Integer>) null));
		valuesRev = new ArrayList<Map<Integer, String>>(
				Collections.nCopies(domainIssues.size(), (Map<Integer, String>) null));

		for (Issue issue : domainIssues) {
			// Store issue
			String issueName = issue.toString();
			issues.set(issue.getNumber() - 1, issueName);

			// Store issue values
			List<SimpleElement> xmlValues = issue.toXML().getChildElementsAsList();
			Map<String, Integer> tmpValueNames = new HashMap<String, Integer>();
			Map<Integer, String> tmpValueNamesRev = new HashMap<Integer, String>();
			for (SimpleElement value : xmlValues) {
				String valueName = value.getAttribute("value");
				Integer valueIndex = Integer.parseInt(value.getAttribute("index"));
				tmpValueNames.put(valueName, valueIndex - 1);
				tmpValueNamesRev.put(valueIndex - 1, valueName);
			}
			values.set(issue.getNumber() - 1, tmpValueNames);
			valuesRev.set(issue.getNumber() - 1, tmpValueNamesRev);
		}

		bidding = new BiddingStrategy(utilitySpace.getDomain(), opponentModels, values, valuesRev, this);
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
		if (bidding.deadline == null) {
			bidding.deadline = getDeadlines().getType() == DeadlineType.ROUND ? getDeadlines().getValue() : 0;
		}
		// Consult the bidding strategy!
		Bid ourBid = bidding.generateBid();
		boolean randomBidCreated = false;
		if (ourBid == null) {
			// The bidding strategy could not find a good offer so just do
			// something
			ourBid = generateRandomBid();
			randomBidCreated = true;
		}
		if (validActions.contains(Accept.class) && shouldAccept(bidding.currentOpponentBid, ourBid, bidding.lastBid)) {
			// The most recent bid is acceptable
			return new Accept(getPartyId(), bidding.currentOpponentBid);
		}

		// The most recent bid is not acceptable so offer our own
		if (!randomBidCreated) {
			bidding.lastBid = ourBid;
		}
		return new Offer(getPartyId(), ourBid);
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
		Bid bid = DefaultAction.getBidFromAction(action);
		if (bid != null) {
			// The action is an offer
			bidding.updateOffer(bid);
		}

		// Update our model of the opponent
		String opponent = sender.toString();
		if (!opponentModels.containsKey(opponent)) {
			opponentModels.put(opponent, new OpponentModel(issues, values, this));
		}
		opponentModels.get(opponent).updateModel(bid);
	}

	/**
	 * Acceptance strategy. This method determines whether or not to accept an
	 * offer. An offer is accepted if the bidding strategy has not found a bid
	 * that's better for us than the offer. At the later stages of the
	 * negotiation the agent may concede a lot
	 * 
	 * @param theirBid
	 *            Newest bid from an opponent
	 * @param ourBid
	 *            Our best bid for this round
	 */
	public boolean shouldAccept(Bid theirBid, Bid ourBid, Bid ourPreviousBid) {
		if (theirBid == null || ourBid == null) {
			// Cannot accept non-existing bids
			return false;
		}
		double u1, u2;
		try {
			u1 = utilitySpace.getUtility(theirBid);
			u2 = utilitySpace.getUtility(ourBid);
			if (ourPreviousBid != null) {
				// Average utilities of this bid and the last bid
				u2 = (u2 + utilitySpace.getUtility(ourPreviousBid)) / 2;
			}
		} catch (Exception e) {
			println("Exception when calculating utilities in \"shouldAccept\"");
			return false;
		}

		Integer deadline = bidding.deadline;
		if (deadline == null) {
			bidding.setDeadline(getDeadlines().getType() == DeadlineType.ROUND ? getDeadlines().getValue() : 0);
			deadline = bidding.deadline;
		}
		// int roundsLeft = Integer.MAX_VALUE;
		// double discount = 0.0;
		// if (bidding.deadline != null) {
		// roundsLeft = bidding.deadline-(bidding.round+1);
		// discount = (10.0-(roundsLeft))/10.0;
		// }
		// if (roundsLeft <= 10) {
		// println("Discounting by " + discount);
		// return u1 >= u2-discount;
		// } else {
		// return u1 >= u2;
		// }
		int roundsLeft = bidding.deadline - (bidding.round + 1);
		return roundsLeft < 3 || u1 > u2;
	}

	public void println(String str) {
		// System.out.println("[" + this + "] " + str);
	}

	protected AgentID partyId = new AgentID("Group 5");

	@Override
	public String getDescription() {
		return "ai2014 group5";
	}

}

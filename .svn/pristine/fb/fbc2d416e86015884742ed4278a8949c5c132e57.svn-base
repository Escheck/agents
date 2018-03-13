package agents.ai2014.group1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agents.anac.y2010.Yushu.Utility;
import agents.anac.y2011.IAMhaggler2011.RandomBidCreator;
import agents.anac.y2012.MetaAgent.agents.MrFriendly.OpponentModel;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group1 extends AbstractNegotiationParty {

	private Bid bid;
	private List<String> opponents = new ArrayList<String>();

	private int totalRounds = 0;
	private int currentRound = 0;

	private Bid myLastBid;

	private List<String> myOpponents = new ArrayList<String>();
	private HashMap<String, OpponentModel> opponentModels = new HashMap<String, OpponentModel>();
	private List<Issue> issueList = new ArrayList<Issue>();

	private RandomBidCreator rbc = new RandomBidCreator();

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		// initialize issueList
		issueList = this.utilitySpace.getDomain().getIssues();
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
		totalRounds = getDeadlines().getType() == DeadlineType.ROUND ? getDeadlines().getValue() : 0;
		// System.out.println("totalRounds: " + totalRounds);
		currentRound++;
		// System.out.println("agentID: " + getPartyId().toString() +
		// "currentRound: " + currentRound);
		int roundsToGo = totalRounds - currentRound;
		double threshold = 1.0;
		Bid myBid;
		double currentUtility = getUtility(bid);

		// Depending on the deadline, adjust the threshold.
		if ((double) roundsToGo > 0.5 * totalRounds) {
			threshold = 0.95;
		} else if ((double) roundsToGo > 0.3 * totalRounds) {
			threshold = 0.9;
		} else if (roundsToGo > 3) {
			threshold = 0.80;
		} else {
			threshold = 0.6;
		}

		if (currentUtility > threshold) {
			return new Accept(getPartyId(), bid);
		} else { // generate bid taking others'utilities into account
					// generate a bid with high utility for me if it's my first
					// bid
					// otherwise, take my last bid as base for my new bid
			double newUtility = 0;
			if (myLastBid != null) {
				Bid tempBid;
				boolean allOpponentsUtilHigh;
				double oppThreshold = threshold - 0.2;
				int counter = 0;
				do {
					if (counter == 100) {
						oppThreshold -= 0.05;
						counter = 0;
					}
					tempBid = rbc.getBid(((AdditiveUtilitySpace) utilitySpace), threshold - 0.05, threshold + 0.05);
					newUtility = getUtility(tempBid);
					allOpponentsUtilHigh = checkUtils(oppThreshold, tempBid);
					counter++;
				} while ((newUtility < threshold - 0.05 && !allOpponentsUtilHigh));
				myBid = tempBid;
				// System.out.println("Bid generated: " + myBid.toString() +
				// " || utility : " + getUtility(myBid));
			}
			// first round
			else {
				try {
					myBid = Utility.getRandomBid(((AdditiveUtilitySpace) utilitySpace));
				} catch (Exception e1) {
					do {
						myBid = generateRandomBid();
					} while ((newUtility < 0.9));
					e1.printStackTrace();
				}
				newUtility = getUtility(myBid);
			}
		}
		myLastBid = myBid;
		return new Offer(getPartyId(), myBid);
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
		// First check if the opponent is new or not
		// If so, create a new opponent model for that opponent
		if (!myOpponents.contains(sender.toString())) {
			myOpponents.add(sender.toString());
			addOpponentModel(sender.toString());
		}

		// Check if the message received is a bid or an accept
		if (action.toString() == "(Accept)") {
			// Do nothing.
		} else if (action instanceof Offer) {
			// Get bid.
			bid = ((Offer) action).getBid();

			// if the corresponding opponentModel should exist and the current
			// bid can be added
			if (opponentModels.containsKey(sender)) {
				opponentModels.get(sender).addOpponentBid(bid);
			}
		}
	}

	private void addOpponentModel(String opponent) {
		if (issueList != null && !opponentModels.containsKey(opponent)) {
			opponentModels.put(opponent, new OpponentModel(issueList, 1, timeline));
		}
	}

	private boolean checkUtils(double threshold, Bid bid) {
		for (String opponent : opponents) {
			OpponentModel oppModel = opponentModels.get(opponent);
			if (oppModel.getEstimatedUtility(bid) <= threshold) {
				return false;
			}
		}
		return true;
	}

	protected AgentID partyId = new AgentID("Group 1");

	@Override
	public String getDescription() {
		return "ai2014 group1";
	}

}

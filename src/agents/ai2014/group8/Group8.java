package agents.ai2014.group8;

import java.util.ArrayList;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

/**
 * This is your negotiation party.
 */
public class Group8 extends AbstractNegotiationParty {

	// utility value above which bids will be accepted
	private double acceptanceValue;

	// number of rounds available for negotiation
	private double totalRounds;

	// number of rounds of negotiation completed
	private int roundCounter;

	private static final double INITIALACCEPTVALUE = 0.5;

	// most recent bid
	private Bid mostRecentBid;

	// most recent bidder
	private AgentID mostRecentBidder;

	// list of opponents modeled by opponentModel class
	private List<OpponentModel> opponents;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		// get total number of rounds available from the environment
		this.totalRounds = timeline.getTotalTime() - 1;

		this.roundCounter = 0;

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
		this.roundCounter++;

		this.acceptanceValue = 0.99 + 1 - Math.pow(Math.pow(1.9, 1 / this.totalRounds),
				Math.pow(INITIALACCEPTVALUE, (this.totalRounds) / this.roundCounter) * (this.roundCounter - 1));

		// first bid - offer a bid with high utility value for self
		if (!validActions.contains(Accept.class)) {
			Bid firstBid = this.generateHigherUtilityBid(this.acceptanceValue).get(0);

			return new Offer(getPartyId(), firstBid);
		}
		// choosing action after a bid has been made
		else {
			// evaluate the utility of most recent bid
			double utilityOfMostRecentBid;

			try {
				utilityOfMostRecentBid = this.utilitySpace.getUtility(this.mostRecentBid);
			} catch (Exception e) {
				utilityOfMostRecentBid = 0;
				e.printStackTrace();
			}

			// accept offer if utility is higher than our acceptance value
			if (utilityOfMostRecentBid >= this.acceptanceValue) {
				return new Accept(getPartyId(), mostRecentBid);
			}
			// make counter offer
			else {
				// generate some high utility bids
				List<Bid> possibleBids = generateHigherUtilityBid(this.acceptanceValue);

				// when too few rounds have passed, offer a random high utility
				// bid
				if (this.roundCounter < 5) {
					return new Offer(getPartyId(), possibleBids.get(0));
				}
				// among high utility bids choose one that has best utility for
				// next agent
				else {
					// find bid with highest utility for next agent
					double maxUtilityForOpponent = 0.0;
					Bid bestBidForNextAgent = null;

					OpponentModel nextAgent = getNextAgentModel();

					for (Bid bid : possibleBids) {
						double bidUtility = nextAgent.EvaluateBidUtility(bid);

						if (bidUtility > maxUtilityForOpponent) {
							bestBidForNextAgent = bid;
							maxUtilityForOpponent = bidUtility;
						}
					}

					return new Offer(getPartyId(), bestBidForNextAgent);
				}
			}
		}
	}

	/**
	 * find next agent among list of opponents
	 * 
	 * @return model of next agent
	 */
	private OpponentModel getNextAgentModel() {
		OpponentModel nextAgent = null;

		for (OpponentModel opponent : this.opponents) {
			if (opponent.agent.equals(this.mostRecentBidder)) {
				int indexOfPreviousBidder = this.opponents.indexOf(mostRecentBidder);

				int indexOfNextAgent = (indexOfPreviousBidder + 1) % this.opponents.size();

				nextAgent = this.opponents.get(indexOfNextAgent);
				break;
			}
		}
		return nextAgent;
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
		// handle first message from framework to initialize list of opponents
		if (sender == null) {
			super.receiveMessage(sender, action);

			// get number of agents in the negotiation
			Inform agentsInformation = (Inform) action;
			int numberOfAgents = (Integer) agentsInformation.getValue();

			// initialize list of other parties
			if (numberOfAgents > 1) {
				this.opponents = new ArrayList<OpponentModel>();
			}
			return;
		}

		this.mostRecentBidder = sender;

		List<Issue> issues = getUtilitySpace().getDomain().getIssues();
		// add sender agent to list of other parties if not present
		if (this.opponents.contains(new OpponentModel(sender, issues)) == false) {
			OpponentModel newOpponent = new OpponentModel(sender, issues);
			this.opponents.add(newOpponent);
		}

		// store sender agent bid in its model among list of opponents
		if ((action instanceof Offer)) {
			mostRecentBid = ((Offer) action).getBid();

			OpponentModel senderModel = null;

			for (OpponentModel opponent : this.opponents) {
				if (opponent.agent == sender) {
					senderModel = opponent;
					break;
				}
			}

			if (senderModel != null) {
				try {
					senderModel.AddBid(mostRecentBid);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * generates random bids which have higher utility than the parameter
	 *
	 * @param utilityValue
	 *            lower bound of utility value for randomly generated bids
	 */
	private List<Bid> generateHigherUtilityBid(double utilityValue) {
		Bid randomBid;
		List<Bid> randomBidsList = new ArrayList<Bid>();

		double util;
		do {
			randomBid = generateRandomBid();

			try {
				util = utilitySpace.getUtility(randomBid);
			} catch (Exception e) {
				util = 0.0;
			}

			if (util > utilityValue && util < (utilityValue + 0.05)) {
				randomBidsList.add(randomBid);
			}
		} while (randomBidsList.size() < 10);

		return randomBidsList;
	}

	protected AgentID partyId = new AgentID("Group 8");

	@Override
	public String getDescription() {
		return "ai2014 group8";
	}

}

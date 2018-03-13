package parties.in4010.q12015.group22;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;

/**
 * An attempt to improve the AbstractNegotiationParty.
 * 
 * @author Wesley Quispel 4014901 Group 22 AI Techniques
 */

public class Group22 extends AbstractNegotiationParty {

	private Action opponentAction;
	private static final int AMOUNT_OF_RANDOM_BIDS = 30;
	private static final double MINIMUM_UTILITY_BASENUMBER = 0.8;
	private static final double MAXIMUM_UTILITY_BASENUMBER = 1.0;

	/**
	 * The actions that come in from opponents are checked if they are an
	 * offer/accept. It is then decided whether if it is acceptable or to make a
	 * counter offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		try {
			if (!validActions.contains(Accept.class)) {
				if (!isAcceptable()) {
					return makeOffer();
				}
			} else {
				if (isAcceptable()) {
					return new Accept(getPartyId(),
							((ActionWithBid) getLastReceivedAction()).getBid());
				} else {
					return makeOffer();
				}
			}
		} catch (Exception e) {
			System.err.println("CBAgent: Making new bid was unsuccessful: "
					+ e.getMessage());
		}
		return new Accept(getPartyId(),
				((ActionWithBid) getLastReceivedAction()).getBid());
	}

	/**
	 * Creates an offer after AMOUNT_OF_RANDOM_BIDS iterations with the highest
	 * utility.
	 * 
	 * @return offer The best offer after AMOUNT_OF_RANDOM_BIDS iterations.
	 */
	private Action makeOffer() {
		try {
			double bestUtility = 0.0;
			Bid bestBid = null;

			for (int i = 0; i < AMOUNT_OF_RANDOM_BIDS; i++) {
				Bid randomBid = generateRandomBid();
				double randomUtility = getUtility(randomBid);
				if (randomUtility > bestUtility) {
					if (isAcceptable(randomUtility)) {
						bestUtility = randomUtility;
						bestBid = randomBid;
					}
				}
			}
			return new Offer(getPartyId(), bestBid);
		} catch (Exception e) {
			System.err.println("CBAgent: Could not make offer: "
					+ e.getMessage());
		}
		return makeOffer();
	}

	public boolean isAcceptable(double utility) {
		try {
			double currentTime = timeline.getTime();
			int numberOfParties = 0;
			if (getNumberOfParties() <= 6) {
				numberOfParties = getNumberOfParties();
			} else {
				numberOfParties = 6;
			}
			if (0 < utility && utility <= 1) {
				if (utility > (MINIMUM_UTILITY_BASENUMBER - ((0.15 * currentTime) + (0.02 * numberOfParties)))) {
					if (utility < (MAXIMUM_UTILITY_BASENUMBER - ((0.25 * currentTime) + (0.02 * numberOfParties)))) {
						return true;
					}
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			System.err
					.println("CBAgent: Could not see whether CBAgent offer was acceptable: "
							+ e.getMessage());
		}
		return false;
	}

	public boolean isAcceptable() {
		try {
			Bid opponentBid = DefaultAction.getBidFromAction(opponentAction);
			double opponentUtility = getUtility(opponentBid);
			int numberOfParties = 0;
			if (getNumberOfParties() <= 6) {
				numberOfParties = getNumberOfParties();
			} else {
				numberOfParties = 6;
			}
			double currentTime = timeline.getTime();
			if (0 < opponentUtility && opponentUtility <= 1) {
				if (opponentUtility > (MINIMUM_UTILITY_BASENUMBER - ((0.15 * currentTime) + (0.02 * numberOfParties)))) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			System.err
					.println("CBAgent: Could not see whether opponent offer was acceptable: "
							+ e.getMessage());
		}
		return false;
	}

	// ++++++++++++++++++++++++++++BACKGROUND+METHODS++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

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
		super.receiveMessage(sender, action);
		opponentAction = action;
		// Here you hear other parties' messages
	}

	@Override
	public String getDescription() {
		return "CBAgent";
	}

}

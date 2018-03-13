package parties.in4010.q12015.group16;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.NoAction;
import negotiator.actions.Offer;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group16 extends AbstractNegotiationParty {
	// Utility value below which no bids will be accepted
	private double reservationValue = 0.7;
	// Utility value of the worst bid we can make
	private double bidLowerBound = 0.9;
	// Utility value of the best bid we can make
	private double bidUpperBound = 1.0;

	// The general model
	private GeneralModel GM;

	// Index for all possible bids sorted by utility
	private SortedOutcomeSpace outcomeSpace;

	// The most recent action and offer received from an opponent, updated in
	// receiveMessage
	private Action lastReceivedAction = null;
	private Bid lastReceivedOffer = null;

	/**
	 * This is the first call made to a NegotiationParty after its
	 * instantiation. Tells which utility space and timeline we are running in.
	 * This is called one time only.
	 * 
	 * @param utilSpace
	 *            (a copy of/ read-only version of) the UtilitySpace to be used
	 *            for this session.
	 * @param timeline
	 *            the TimeLineInfo about current session.
	 * @param agentID
	 *            the AgentID.
	 * @throws RuntimeException
	 *             if init() fails.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		utilitySpace.setDiscount(0);
		utilitySpace.setReservationValue(reservationValue);

		try {
			outcomeSpace = new SortedOutcomeSpace(utilitySpace);
		} catch (Exception e) {
			e.printStackTrace();
		}

		GM = new GeneralModel((AdditiveUtilitySpace) utilitySpace, outcomeSpace);
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
		Action action = new NoAction(getPartyId());

		if (lastReceivedAction instanceof Offer || lastReceivedAction instanceof Accept) {
			if (isAcceptable(lastReceivedOffer)) {
				return new Accept(getPartyId(), lastReceivedOffer);
			}
		}
		if (validActions.contains(Offer.class)) {
			return new Offer(getPartyId(), getBid());
		}

		return action;
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
		super.receiveMessage(sender, action);
		lastReceivedAction = action;

		if (action instanceof Offer) {
			try {
				GM.getOpponent(action.getAgent()).update(((Offer) action).getBid());
				lastReceivedOffer = ((Offer) action).getBid();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * If we're in the first half of the negotiation, returns bids within the
	 * [bidLowerBound, bidUpperBound] interval. If we're in the second half of
	 * the negotiation, tries to get the next bid from the GeneralModel. If this
	 * fails, falls back to the first method of returning bids.
	 * 
	 * @return bid to offer next
	 */
	private Bid getBid() {
		Bid bid = null;
		if (getTimeLine().getTime() <= 0.65) {
			bid = getBidWithinBounds();
		} else {
			try {
				if (!GM.isGenerated())
					GM.generate();
				bid = GM.getProposal();
				if (bid == null) {
					bidLowerBound = bidLowerBound - 0.1;
					bidUpperBound = bidUpperBound - 0.1;
					bid = getBidWithinBounds();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bid;
	}

	/**
	 * @return bid with utility in the [bidLowerBound, bidUpperBound] interval.
	 */
	private Bid getBidWithinBounds() {
		double randomUtility = bidLowerBound + (bidUpperBound - bidLowerBound) * rand.nextDouble();
		return outcomeSpace.getBidNearUtility(randomUtility).getBid();
	}

	/**
	 * Determines whether b is acceptable
	 * 
	 * @param b
	 *            bid to accept or reject
	 * @return true if b should be accepted
	 */
	private boolean isAcceptable(Bid b) {
		if (getTimeLine().getTime() <= 0.5) {
			// Before half of the negotiation, accept a bid if its utility is
			// equal to or higher than the utility of the best bid we can make
			return getUtility(b) >= bidUpperBound;
		} else if (getTimeLine().getTime() <= 0.97) {
			// Concede after half of the negotiation
			return getUtility(b) >= reservationValue + 0.15;
		} else {
			// In the last phase of a negotiation, accept a bid if its utility
			// is equal to or higher than our reservation value
			return getUtility(b) >= reservationValue;
		}
	}

	@Override
	public String getDescription() {
		return "This is Agent 16, deal with it";
	}
}

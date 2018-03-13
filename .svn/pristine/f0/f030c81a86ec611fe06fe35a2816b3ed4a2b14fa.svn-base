package negotiator.parties;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Feedback;
import negotiator.Vote;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.GiveFeedback;
import negotiator.actions.InformVotingResult;
import negotiator.actions.OfferForFeedback;
import negotiator.actions.OfferForVoting;
import negotiator.actions.VoteForOfferAcceptance;
import negotiator.protocol.MediatorFeedbackBasedProtocol;
import negotiator.protocol.MultilateralProtocol;

/**
 * Hill climber implementation for the mediator protocol with feedback.
 * <p/>
 * This implementation was adapted from Reyhan's original code and refitted in
 * the new framework
 * <p/>
 * <u>Possible bug:</u><br>
 * Possibly, there is a small bug in this code, which you will encounter when
 * running this agent for an extended period of time (i.e. minutes)
 *
 * @author David Festen
 * @author Reyhan (Orignal code)
 */
public class FeedbackHillClimber extends AbstractNegotiationParty {
	private double lastBidUtility;
	private double lastAcceptedUtility;
	private double currentBidUtility;
	private Feedback currentFeedback;
	private Vote currentVote;
	private boolean voteTime;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		lastBidUtility = 0.0;
		lastAcceptedUtility = 0.0;
		currentBidUtility = 0.0;
		currentFeedback = Feedback.SAME;
		voteTime = false;
	}

	/**
	 * When this class is called, it is expected that the Party chooses one of
	 * the actions from the possible action list and returns an instance of the
	 * chosen action. This class is only called if this {@link NegotiationParty}
	 * is in the
	 * {@link negotiator.protocol .DefaultProtocol#getRoundStructure(java.util.List, negotiator.session.Session)}
	 * .
	 *
	 * @param possibleActions
	 *            List of all actions possible.
	 * @return The chosen action
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (voteTime) {
			return (new VoteForOfferAcceptance(getPartyId(), currentVote));
		} else {
			return (new GiveFeedback(getPartyId(), currentFeedback));
		}
	}

	/**
	 * This method is called when an observable action is performed. Observable
	 * actions are defined in
	 * {@link MultilateralProtocol#getActionListeners(java.util.List)}
	 *
	 * @param sender
	 *            The initiator of the action
	 * @param arguments
	 *            The action performed
	 */
	@Override
	public void receiveMessage(AgentID sender, Action arguments) {

		if (arguments instanceof InformVotingResult) {
			// update the utility of last accepted bid by all
			if (((InformVotingResult) arguments).getVotingResult() == Vote.ACCEPT) {
				lastAcceptedUtility = currentBidUtility;
			}
			return;
		}

		Bid receivedBid = DefaultAction.getBidFromAction(arguments);
		if (receivedBid == null) {
			return;
		}

		if (getDeadlines().getType() == DeadlineType.TIME) {
			currentBidUtility = getUtilityWithDiscount(receivedBid);
		} else {
			currentBidUtility = getUtility(receivedBid);
		}

		if (arguments instanceof OfferForFeedback) {
			currentFeedback = Feedback.madeupFeedback(lastBidUtility, currentBidUtility);
			voteTime = false;
		}
		if (arguments instanceof OfferForVoting) {
			voteTime = true;
			if (lastAcceptedUtility <= currentBidUtility) {
				currentVote = Vote.ACCEPT;
			} else {
				currentVote = Vote.REJECT;
			}
		}

		lastBidUtility = currentBidUtility;
	}

	@Override
	public Class<? extends MultilateralProtocol> getProtocol() {
		return MediatorFeedbackBasedProtocol.class;
	}

	@Override
	public String getDescription() {
		return "Feedback Hillclimber";
	}

}

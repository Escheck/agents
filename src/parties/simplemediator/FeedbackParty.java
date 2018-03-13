package parties.simplemediator;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Feedback;
import negotiator.Vote;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.GiveFeedback;
import negotiator.actions.InformVotingResult;
import negotiator.actions.OfferForFeedback;
import negotiator.actions.OfferForVoting;
import negotiator.actions.VoteForOfferAcceptance;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.protocol.DefaultMultilateralProtocol;
import negotiator.protocol.SimpleMediatorBasedProtocol;
import negotiator.timeline.Timeline.Type;

public class FeedbackParty extends AbstractNegotiationParty {

	private double lastBidUtility;
	private double lastAcceptedUtility;
	private double currentBidUtility;
	private Feedback currentFeedback;
	private Vote currentVote;
	private boolean voteTime;

	public FeedbackParty() {
		super();
		lastBidUtility = 0.0;
		lastAcceptedUtility = 0.0;
		currentBidUtility = 0.0;
		currentFeedback = Feedback.SAME;
		voteTime = false;
	}

	@Override
	public void receiveMessage(AgentID sender, Action opponentAction) {

		if (opponentAction instanceof InformVotingResult) {

			if (((InformVotingResult) opponentAction).getVotingResult() == Vote.ACCEPT) // update
																						// the
																						// utility
																						// of
																						// last
																						// accepted
																						// bid
																						// by
																						// all
				lastAcceptedUtility = currentBidUtility;
			return;
		}

		Bid receivedBid = DefaultAction.getBidFromAction(opponentAction);
		if (receivedBid == null)
			return;

		if (getTimeLine().getType() == Type.Time)
			currentBidUtility = getUtilityWithDiscount(receivedBid);
		else
			currentBidUtility = getUtility(receivedBid);

		if (opponentAction instanceof OfferForFeedback) {
			currentFeedback = Feedback.madeupFeedback(lastBidUtility, currentBidUtility);
			voteTime = false;
		}
		if (opponentAction instanceof OfferForVoting) {
			voteTime = true;
			if (lastAcceptedUtility <= currentBidUtility)
				currentVote = Vote.ACCEPT;
			else
				currentVote = Vote.REJECT;
		}

		lastBidUtility = currentBidUtility;

	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		if (voteTime)
			return (new VoteForOfferAcceptance(getPartyId(), currentVote));
		else
			return (new GiveFeedback(getPartyId(), currentFeedback));

	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return SimpleMediatorBasedProtocol.class;
	}

	@Override
	public String getDescription() {
		return "Feedback Negotiator";
	}

}

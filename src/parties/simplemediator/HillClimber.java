package parties.simplemediator;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Vote;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.InformVotingResult;
import negotiator.actions.VoteForOfferAcceptance;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.protocol.DefaultMultilateralProtocol;
import negotiator.protocol.SimpleMediatorBasedProtocol;
import negotiator.timeline.Timeline.Type;

public class HillClimber extends AbstractNegotiationParty {

	private double lastAcceptedBidUtility;
	private double lastReceivedBidUtility;
	private Vote currentVote;

	public HillClimber() {
		super();
		lastAcceptedBidUtility = 0.0;
		lastReceivedBidUtility = 0.0;
	}

	@Override
	public void receiveMessage(AgentID sender, Action act) {

		if (act instanceof InformVotingResult) {
			/*
			 * update the utility of last accepted bid by all
			 */
			if (((InformVotingResult) act).getVotingResult() == Vote.ACCEPT)
				lastAcceptedBidUtility = lastReceivedBidUtility;
			return;
		}

		Bid receivedBid = DefaultAction.getBidFromAction(act);
		if (receivedBid == null)
			return;

		if (getTimeLine().getType() == Type.Time)
			lastReceivedBidUtility = getUtilityWithDiscount(receivedBid);
		else
			lastReceivedBidUtility = getUtility(receivedBid);

		if (lastAcceptedBidUtility <= lastReceivedBidUtility)
			currentVote = Vote.ACCEPT;
		else
			currentVote = Vote.REJECT;

	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		return (new VoteForOfferAcceptance(getPartyId(), currentVote));
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return SimpleMediatorBasedProtocol.class;
	}

	@Override
	public String getDescription() {
		return "HillClimber party";
	}

}

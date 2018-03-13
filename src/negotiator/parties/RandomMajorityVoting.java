package negotiator.parties;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.OfferForVoting;
import negotiator.actions.Reject;
import negotiator.protocol.AlternatingMajorityConsensusProtocol;
import negotiator.protocol.DefaultMultilateralProtocol;

/**
 * Random agent suited for AlternatingOfferMajorityVotingProtocol
 * <p/>
 * This party should be run with {@link negotiator.protocol.MediatorProtocol}
 *
 * @author W.Pasman
 */
public class RandomMajorityVoting extends AbstractNegotiationParty {

	private boolean lastOfferIsAcceptable = false;

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (possibleActions.contains(OfferForVoting.class)) {
			return new OfferForVoting(getPartyId(), this.generateRandomBid());
		} else {
			if (lastOfferIsAcceptable) {
				return new Accept(getPartyId(), ((ActionWithBid) getLastReceivedAction()).getBid());
			}
			return new Reject(getPartyId(), ((ActionWithBid) getLastReceivedAction()).getBid());
		}
	}

	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		super.receiveMessage(sender, arguments);
		if (arguments instanceof OfferForVoting) {
			if (isAcceptable(((OfferForVoting) arguments).getBid())) {
				lastOfferIsAcceptable = true;
			}
			lastOfferIsAcceptable = false;
		}
	}

	protected boolean isAcceptable(Bid bid) {
		double lastReceivedBidUtility = getUtility(bid);
		double reservationValue = (timeline == null) ? utilitySpace.getReservationValue()
				: utilitySpace.getReservationValueWithDiscount(timeline);

		if (lastReceivedBidUtility >= reservationValue) {
			return true;
		}
		return false;
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return AlternatingMajorityConsensusProtocol.class;
	}

	@Override
	public String getDescription() {
		return "Random Majority Voting Party";
	}

}

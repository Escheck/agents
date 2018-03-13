package negotiator.parties;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.InformVotingResult;
import negotiator.actions.OfferForVoting;
import negotiator.actions.Reject;

/**
 * Implementation of a party that uses hill climbing strategy to get to an
 * agreement.
 * <p/>
 * This party should be run with {@link negotiator.protocol.MediatorProtocol}
 *
 * @author David Festen
 * @author Reyhan
 */
public class HillClimberMajorityVoting extends AbstractNegotiationParty {

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

	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof OfferForVoting) {
			if (isAcceptable(((OfferForVoting) action).getBid())) {
				lastOfferIsAcceptable = true;
			}
			lastOfferIsAcceptable = false;
		} else if (action instanceof InformVotingResult) {
			// WHATEVER.
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
	public String getDescription() {
		return "Hill Climber Majority Voting";
	}

}

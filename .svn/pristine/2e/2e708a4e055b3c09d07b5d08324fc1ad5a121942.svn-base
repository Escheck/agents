package parties.simplemediator;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Vote;
import negotiator.actions.Action;
import negotiator.actions.InformVotingResult;
import negotiator.actions.OfferForVoting;
import negotiator.actions.VoteForOfferAcceptance;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.Mediator;
import negotiator.parties.NegotiationInfo;
import negotiator.protocol.DefaultMultilateralProtocol;
import negotiator.protocol.MultilateralProtocol;
import negotiator.protocol.SimpleMediatorBasedProtocol;

/**
 * This mediator generates random bids until all agents accept. Then, it
 * randomly flips one issue of the current offer to generate a new offer. It
 * keeps going until the deadline is reached.
 * <p/>
 * This class was adapted from Reyhan's parties.PureRandomFlippingMediator class
 * (see svn history for details about that class), and adapted to fit into the
 * new framework.
 */
public class RandomFlippingMediator extends AbstractNegotiationParty implements Mediator {

	/**
	 * Holds whether the current offer is acceptable by all parties.
	 */
	private boolean isAcceptable;

	/**
	 * The most recently accepted bid.
	 */
	private Bid lastAcceptedBid;

	/**
	 * The most recently proposed bid.
	 */
	private Bid lastProposedBid;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		isAcceptable = true;
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {

		if (possibleActions.contains(OfferForVoting.class)) {
			// check if last proposition was acceptable
			if (isAcceptable) {
				lastAcceptedBid = lastProposedBid;
			}

			// init acceptable to true for next round
			isAcceptable = true;

			if (lastAcceptedBid == null) {
				lastProposedBid = generateRandomBid();
				return new OfferForVoting(getPartyId(), lastProposedBid);
			} else {
				lastProposedBid = modifyLastBidRandomly();
				return new OfferForVoting(getPartyId(), lastProposedBid);
			}
		} else if (possibleActions.contains(InformVotingResult.class)) {
			return new InformVotingResult(getPartyId(), lastProposedBid, isAcceptable ? Vote.ACCEPT : Vote.REJECT);
		}

		throw new IllegalArgumentException(
				"RandomFlippingMediator used with wrong protocol: can not handle request for " + possibleActions);
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
		if (arguments instanceof VoteForOfferAcceptance) {
			isAcceptable &= ((VoteForOfferAcceptance) arguments).getVote() == Vote.ACCEPT;
		}
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return SimpleMediatorBasedProtocol.class;
	}

	/**
	 * modifies the most recently accepted bid by changing a single issue value.
	 *
	 * @return the modified bid
	 */
	private Bid modifyLastBidRandomly() {
		try {
			List<Issue> issues = utilitySpace.getDomain().getIssues();
			Bid modifiedBid = new Bid(lastAcceptedBid);
			Value newValue;
			Issue currentIssue;

			int currentIndex;
			do {
				currentIssue = issues.get(rand.nextInt(issues.size()));
				currentIndex = currentIssue.getNumber();
				newValue = getRandomValue(currentIssue);
			} while (newValue.equals(lastAcceptedBid.getValue(currentIndex)));

			modifiedBid = modifiedBid.putValue(currentIndex, newValue);

			return modifiedBid;
		} catch (Exception e) {
			System.out.println(
					"Can not generate random bid or receiveMessage preference list " + "problem:" + e.getMessage());
			return null;
		}
	}

	@Override
	public String getDescription() {
		return "Random Flipping Mediator";
	}
}

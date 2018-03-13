package parties.in4010.q12015.group17;

import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.SessionData;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

/**
 * Group 17's negotiation party. It is inspired by the BOA framework and has
 * split up its offer strategy, opponent modelling and accept strategy. These
 * classes are {@link OfferStrat}, {@link OpMod} and {@link AcceptStrategy}.
 * Essentially, the only function this class has is to correctly forward the
 * data and requests it receives to the former three classes and then return
 * their responses.
 */
public class Group17 extends AbstractNegotiationParty {
	private boolean initOM = true;
	private boolean initOS = true;
	private OpMod om;
	private NegotiationSession negotiationSession;
	private OfferStrat os;
	private AcceptStrategy acceptStrategy;
	private SortedOutcomeSpace sos;
	private boolean acceptOffer = false;
	private Bid lastOffer;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		sos = new SortedOutcomeSpace(utilitySpace);
		SessionData sessionData = new SessionData();
		acceptStrategy = new AcceptStrategy(utilitySpace, timeline);
		om = new OpMod();
		negotiationSession = new NegotiationSession(sessionData, info.getUtilitySpace(), info.getTimeline());
		os = new OfferStrat();
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            A list containing both {@link Accept} and {@link Offer} or
	 *            only {@link Offer}
	 * @return The chosen {@link Action}.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		if (initOS) { // Verify if the init function has run.
			os.init(negotiationSession, sos, om);
			initOS = false;
		}

		if (acceptOffer) {
			return new Accept(getPartyId(), lastOffer);
		} else {
			BidDetails bd = os.determineNextBid();
			negotiationSession.getOwnBidHistory().add(bd);
			return new Offer(getPartyId(), bd.getBid());
		}
	}

	/**
	 * All {@link Action}s performed by the other parties will be received as a
	 * message. You can use this information to your advantage, for example to
	 * predict their utility.
	 *
	 * @param sender
	 *            The party that performed the {@link Action}.
	 * @param action
	 *            The {@link Action} that the sending party performed.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (initOM) { // Verify if the init function has run.
			om.init(negotiationSession, new HashMap<String, Double>());
			initOM = false;
		}

		if (action instanceof Offer) {
			lastOffer = ((Offer) action).getBid();
			try { // Update the model according to the offer presented to us.
				om.updateModel(lastOffer, sender.toString());
			} catch (Exception e) {
				e.printStackTrace();
			} // Determine if we want to accept this offer.
			acceptOffer = acceptStrategy.determineAcceptability(lastOffer);
		}
	}

	@Override
	public String getDescription() {
		return "IN4010 Group 17's agent party";
	}
}

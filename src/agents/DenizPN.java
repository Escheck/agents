package agents;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.PocketNegotiatorAgent;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.analysis.BidPoint;
import negotiator.issue.Value;
import negotiator.timeline.DiscreteTimeline;
import negotiator.timeline.Timeline;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Agent proposed by Reyhan Aydogan #944 , conform the 'optimal bidder' agent by
 * Tim Baarslag but fitted to the needs of PocketNegotiator. It was modified
 * according to the design document in NegotiatorGUI/doc. Designed for use both
 * as opponent agent and as suggest-a-bid-agent. Notice, this agent only works
 * with a {@link DiscreteTimeline} just like the {@link OptimalBidder}.
 * 
 * @author W.Pasman 2sep2014.
 * @modified W.Pasman 8oct2014 new concession procedure see Deniz documentation
 * 
 */
public class DenizPN extends Agent implements PocketNegotiatorAgent {

	/**
	 * We have a hard coded reservation value now. To be fixed later to use
	 * utilitySpace.getReservationValue()
	 */
	private final double RESERVATION_VALUE = 0.5;

	/**
	 * Deniz agent walks away after 15 minutes #999. This time starts running
	 * the moment this bot instance is created, which happens the moment the
	 * user has placed his first bid.
	 */
	private Date startTime = new Date();;

	/**
	 * The move that our agent determines to make.
	 */
	private enum MyMoves {
		/** best of concession or accept */
		CONCEDE,
		/** best of concession, accept, or opponent bid projected onto pareto */
		CONCEDE_OR_PARETO,
		/** repeat our last bid */
		SAME,
		/** a bid with same utils as our last bid, but different */
		SILENT,
		/** stop the negotiation. */
		STOP
	};

	HistorySpace historySpace = new HistorySpace();

	/** Human readable explanation of chosen last bid */
	private String lastBidExplanation = null;

	private static final DecimalFormat twoDigits = new DecimalFormat("#0.00");

	/************* implements PocketNegotiatorAgent ***************/
	@Override
	public void initPN(AdditiveUtilitySpace mySide,
			AdditiveUtilitySpace otherSide, Timeline tl) {
		updateProfiles(mySide, otherSide);
		timeline = tl;
	}

	@Override
	public void handleAction(Action act) {
		ReceiveMessage(act);

	}

	@Override
	public Action getAction() {
		return chooseAction();
	}

	@Override
	public void updateProfiles(AdditiveUtilitySpace myUtilities,
			AdditiveUtilitySpace opponentUtilities) {
		utilitySpace = myUtilities; // keep Agent happy... For Genius
		historySpace.setUtilities(myUtilities, opponentUtilities);

		/*
		 * DEBUG: print out the bid space System.out.println("udpate profiles");
		 * BidIterator it = new BidIterator(myUtilities.getDomain()); try {
		 * while (it.hasNext()) { Bid bid = it.next(); System.out.println("" +
		 * bid + " " + myUtilities.getUtility(bid) + " " +
		 * opponentUtilities.getUtility(bid));
		 * 
		 * } } catch (Exception e) { e.printStackTrace(); }
		 */
	}

	/**************** extends Agent *******************/
	@Override
	public void init() {
	}

	@Override
	public String getName() {
		return "Deniz agent PN";
	}

	@Override
	public void ReceiveMessage(Action opponentAction) {
		try {
			if (opponentAction instanceof Offer) {
				receiveMessage1(((Offer) opponentAction).getBid());
			}
		} catch (Exception e) {
			// this should be fatal. But
			// we can't do much, ReceiveMessage does not allow throw.
			e.printStackTrace();
		}
	}

	/**
	 * Save the bid in the history. If this is the first bid and we are running
	 * in Genius, we use this bid to estimate the opponent's utility space in a
	 * simplistic way.
	 * 
	 * @param bid
	 * @throws Exception
	 */
	private void receiveMessage1(Bid bid) throws Exception {
		historySpace.getOpponentBids().add(bid);

		if (historySpace.getOutcomeSpace() == null) {
			// if this is null, we are running in Genius.
			historySpace.setUtilities(utilitySpace,
					new SimpleFakeOpponentUtilitySpace(utilitySpace, bid));
		}
	}

	@Override
	public Action chooseAction() {
		/**
		 * default action. Also returned if exception occurs. Notice, may be
		 * better to Accept but Reyhan thinks Genius gives us the
		 * #RESERVATION_VALUE anyway if we walk away. Also if Deniz is used from
		 * PN as a we do not want to give the user a wrong Accept suggestion if
		 * the agent crashes. #972
		 */
		lastBidExplanation = "Chosen a default fallback, something failed while chosing the next action.";
		Action action = new EndNegotiation(getAgentID());

		try {
			action = chooseAction1();

			Bid effectiveBid = getEffectiveBid(action);
			if (effectiveBid != null) {
				historySpace.getMyBids().add(effectiveBid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return action;
	}

	/**
	 * get the 'effective bid' of an action. For an offer, the effective bid is
	 * the bid contained in the offer. For an accept, it is the opponent's last
	 * bid. For a endnegotiation, we return null.
	 * 
	 * Throws exception if action is accept and opponent made no bid yet.
	 * 
	 * @param action
	 */
	private Bid getEffectiveBid(Action action) {
		if (action instanceof Offer) {
			return ((Offer) action).getBid();
		} else if (action instanceof Accept) {
			return historySpace.getOpponentBids().last();
		}
		return null;
	}

	/**
	 * get next bid. returns offer with max util bid if historySpace null (which
	 * can happen if running from Genius and opponent did not yet place bid) or
	 * if we did not yet place initial bid.
	 * 
	 * @return chosen bid, Accept or Stop..
	 * @throws Exception
	 *             if fatal problem occurs somewhere in our code.
	 */
	private Action chooseAction1() throws Exception {
		// 1+.. because the current round does not count.
		if (historySpace.getOpponentBids().isEmpty()
				|| ((DiscreteTimeline) timeline).getOwnTotalRounds() == 1 + ((DiscreteTimeline) timeline)
						.getOwnRoundsLeft()) {
			// First round. place our best bid
			lastBidExplanation = "Picked the highest-utility bid as the first offer.";
			return new Offer(getAgentID(), utilitySpace.getMaxUtilityBid());
		}

		// below this point, both sides did initial bid
		Action myNextAction;
		switch (checkDeadlineAndDetermineMyMove()) {
		case CONCEDE:
			myNextAction = new Offer(getAgentID(), getConcessionBid().getBid());
			break;
		case CONCEDE_OR_PARETO:
			myNextAction = new Offer(getAgentID(), getConcedeOrPareto()
					.getBid());
			break;
		case SAME:
			myNextAction = new Offer(getAgentID(), historySpace.getMyBids()
					.last());
			break;
		case SILENT:
			myNextAction = new Offer(getAgentID(), historySpace.getSilentBid());
			break;
		case STOP:
			myNextAction = new EndNegotiation(getAgentID());
			break;
		default:
			throw new IllegalStateException("internal error, unknown move");
		}

		if (isAcceptBetter(myNextAction)) {
			return new Accept(getAgentID(), historySpace.getOpponentBids()
					.last());
		}

		return myNextAction;
	}

	/**
	 * Checks if accepting opponent's last bid has higher util than doing
	 * #mynextaction.
	 * 
	 * @param mynextaction
	 *            the next bid that WE will place, if we do not accept the
	 *            opponents bid.
	 * 
	 * @return true if the opponent's last bid is better than mynextaction
	 *         effectivebid, or if the opponent's last bid has utility > current
	 *         target utility (see {@link #getTargetUtility()}.
	 * @throws Exception
	 *             if opp did not make a bid.
	 */
	private boolean isAcceptBetter(Action mynextaction) throws Exception {
		// first check if opponent's last bid was good.
		Bid lastopponentbid = historySpace.getOpponentBids().last();
		double lastutil = historySpace.getOutcomeSpace().getMyUtilitySpace()
				.getUtility(lastopponentbid);

		if (lastutil < RESERVATION_VALUE) {
			return false;
		}

		if (lastutil >= getTargetUtility(((DiscreteTimeline) timeline)
				.getOwnRoundsLeft())) {
			return true;
		}

		// check if accept is better than our next action.
		Bid mynextbid = getEffectiveBid(mynextaction);
		if (mynextbid == null) { // we want to stop the nego
			return false;
		}

		double mynextbidutility = historySpace.getOutcomeSpace()
				.getMyUtilitySpace().getUtility(mynextbid);

		// suggest accept if it has higher util than our current action.
		return mynextbidutility < lastutil;
	}

	/**
	 * Check the deadline. If across, do {@link MyMoves#SILENT} or
	 * {@link MyMoves#STOP}. If not, return a move according to the table.
	 * 
	 * @return next planned move.
	 */
	private MyMoves checkDeadlineAndDetermineMyMove() {
		int roundsleft = ((DiscreteTimeline) timeline).getOwnRoundsLeft();
		Date now = new Date();
		long diff_ms = now.getTime() - startTime.getTime();
		double minutesSinceStart = diff_ms / 60000;

		if (minutesSinceStart > 15) {
			lastBidExplanation = "After 15 minutes into the negotiation, Deniz decides to stop the negotiation.";
			return MyMoves.STOP;
		}

		if (roundsleft < 0) { // pass the deadline?
			if (roundsleft < -3) { // pass the extra-time?
				lastBidExplanation = "We are 3 rounds past the deadline. Deniz decides to stop the negotiation.";
				return MyMoves.STOP;
			}
			lastBidExplanation = "We reached the deadline. Deniz sticks with the last bid.";
			return MyMoves.SILENT;
		}
		return determineMyMove();
	}

	/**
	 * Determine a move according to the table. The move that we do depends on
	 * the opponent's moves. Actually we are using a reversed version of the
	 * table (columns are the opponent moves: his current, one back and two
	 * back) and after the arrow it's the move that we decide to do: {self means
	 * selfish or silent; nonself means any other}.
	 * 
	 * 
	 * <ol>
	 * <li>self , self , self -> SAME
	 * <li>self, self, nonself -> CONCEDE_OR_ACCEPT
	 * <li>self, nonself -> SILENT
	 * <li>nice/concession/fortunate -> CONCEDE_OR_ACCEPT
	 * <li>unfortunate -> PARETO_OR_CONCEDE_OR_ACCEPT
	 * </ol>
	 * 
	 * <br>
	 * This code does not check against the possibility to accept. This is
	 * because this code does not compute the effective offer, it only suggests
	 * to do eg a concession.
	 * 
	 * Assumptions:
	 * <ul>
	 * <li>we did one offer and the other side did already 2 offers at least.
	 * <li>we are still before the deadline.
	 * </ul>
	 * 
	 * @return
	 */
	private MyMoves determineMyMove() {
		// check opponent previous moves. See the table above.
		MoveType move0 = historySpace.getMoveType(0);
		if (isSelfish(move0)) {
			if (isSelfishOrUnfortunate(historySpace.getMoveType(1))) {
				if (isSelfishOrUnfortunate(historySpace.getMoveType(2))) {
					lastBidExplanation = "The opponent did three selfish moves in a row. Deniz sticks with the last bid.";
					return MyMoves.SAME;
				}
				// last 2 moves selfish, but before that not.
				lastBidExplanation = "The opponent did a two selfish moves in a row. Deniz does a concession.";
				return MyMoves.CONCEDE;
			}
			// current move selfish, previous move not
			lastBidExplanation = "The opponent did a selfish move. Deniz does a silent bid.";
			return MyMoves.SILENT;
		}
		// current move not selfish
		if (move0 == MoveType.UNFORTUNATE) {
			lastBidExplanation = "The opponent did an unfortunate move. Deniz chooses between concession "
					+ "or moving his bid back to the pareto frontier";
			return MyMoves.CONCEDE_OR_PARETO;
		}
		lastBidExplanation = "The opponent made a concession. Deniz makes a concession too. ";
		return MyMoves.CONCEDE;
	}

	/**
	 * Check if move is selfish
	 * 
	 * @param move
	 *            the {@link MoveType} that was done. Can be null
	 * @return true if move is selfish. Returns false if move=null.
	 */
	private boolean isSelfish(MoveType move) {
		return move == MoveType.SELFISH || move == MoveType.SILENT;
	}

	/**
	 * Check if move is selfish or unfortunate
	 * 
	 * @param move
	 *            the {@link MoveType} that was done. Can be null
	 * @return true if move is selfish. Returns false if move=null.
	 */
	private boolean isSelfishOrUnfortunate(MoveType move) {
		return move == MoveType.SELFISH || move == MoveType.SILENT
				|| move == MoveType.UNFORTUNATE;
	}

	/**
	 * check the concession bid against the on-pareto-projected-other-bid and
	 * return the one with the highest our-utility
	 * 
	 * @return concession, accept or paretoprojected opponent bid.
	 * @throws Exception
	 */
	private BidPoint getConcedeOrPareto() throws Exception {
		return bestBid(projectOtherBidToPareto(), getConcessionBid());
	}

	/**
	 * create a new bid by mapping the other's last bid to the pareto frontier.
	 * *
	 * 
	 * @return opponent's last bid mapped to pareto, or concession bid.
	 */
	private BidPoint projectOtherBidToPareto() {
		SimpleBidSpace space = historySpace.getOutcomeSpace();
		double otherUtil;
		try {
			otherUtil = space.getOpponentUtilitySpace().getUtility(
					historySpace.getOpponentBids().last());
		} catch (Exception e) {
			throw new IllegalStateException("getUtility failed", e);
		}

		return space.getPareto().getBidNearOpponentUtility(otherUtil);
	}

	/**
	 * Get a plausible bid for the previous round. May be different from the
	 * actual bid placed then.
	 * 
	 * The previous bid may not be the bid that we intended to make. For example
	 * Deniz may not have been asked at all next round (user did not ask for a
	 * bid suggestion), or the user may have placed a bid different from the bid
	 * that Deniz would have liked to place. Basically we can't trust the
	 * history for our next bid.
	 * 
	 * @return
	 * @throws Exception
	 */
	private BidPoint getPlausiblePreviousBid() throws Exception {
		double prevBidUtility = getTargetUtility(((DiscreteTimeline) timeline)
				.getOwnRoundsLeft() + 1);
		return historySpace.getOutcomeSpace().getPareto()
				.getBidWithMinimumUtility(prevBidUtility);

	}

	/**
	 * Tries to find a concession bid. A concession is a bid that has higher or
	 * equal opponent utility than our hypothetical previous bid. The starting
	 * point is a bid from the optimalbidding strategy (see
	 * {@link OptimalBidder}). But instead of using that right away, we pick the
	 * bid that in the neighbourhood that has a minimal hamming distance with
	 * the opponent's last bid. <br>
	 * The 'neighbourhood' is all the concessions with a utility for ourself
	 * between our current optimal bid and our previous bid. <br>
	 * If there are no concessions at all in this neighbourhood, this function
	 * may return the previous bid.
	 * 
	 * <h1>Assumes</h1>
	 * <ul>
	 * <li>both sides placed at least one bid. also, {@link #lastBidExplanation}
	 * is assumed to have been set to some partial explanation already.
	 * </ul>
	 * 
	 * @return concession bid
	 * @throws Exception
	 */
	private BidPoint getConcessionBid() throws Exception {
		int turnsleft = ((DiscreteTimeline) timeline).getOwnRoundsLeft();
		double targetUtility = getTargetUtility(turnsleft);
		BidPoint plausiblePrevBid = getPlausiblePreviousBid();

		Set<BidPoint> concessions = historySpace.getOutcomeSpace()
				.getBetweenUtility(targetUtility,
						plausiblePrevBid.getUtilityA(),
						plausiblePrevBid.getUtilityB(), false);

		lastBidExplanation += "Deniz currently aims at a bid of utility "
				+ twoDigits.format(targetUtility) + ". ";
		if (concessions.isEmpty()) {
			lastBidExplanation += "However, there are no bids available with that utility, "
					+ "and therefore Deniz considers bids closer to the previous bid.";
			// note, this may give multiple bids.
			concessions = historySpace.getOutcomeSpace().getBetweenUtility(
					targetUtility, plausiblePrevBid.getUtilityA(),
					plausiblePrevBid.getUtilityB(), true);
		} else {
			lastBidExplanation += "Deniz picked a such bid, close to the opponent's bid, and on the pareto frontier.";
		}

		Bid lastopponentbid = historySpace.getOpponentBids().last();

		return getSmallestHamming(concessions, lastopponentbid);
	}

	/**
	 * Find in a set of BidPoints the one that has highest similarity (see
	 * {@link Bid#getSimilarity(Bid)}) to given target bid.
	 * 
	 * @param bids
	 *            set of bids to pick from
	 * @param targetbid
	 *            the
	 * @return a bid that has highest similarity with given target bid. May
	 *         return null if the given bids list is empty.
	 */
	private BidPoint getSmallestHamming(Set<BidPoint> bids, Bid targetbid) {
		BidPoint bestBid = null;
		double bestsimilarity = -1; // lower than worst similarity
		for (BidPoint bidpoint : bids) {
			double similarity = getSimilarity(bidpoint.getBid(), targetbid);
			if (similarity > bestsimilarity) {
				bestBid = bidpoint;
				bestsimilarity = similarity;
			}
		}
		return bestBid;
	}

	/**
	 * Calculates similarity with another bid. Similarity is measured by
	 * counting the amount of values that are equal across both bids and then
	 * dividing that by the total amount of values in both bids. A similarity of
	 * 1.0 indicates that the bids are one and the same, and a similarity of 0.0
	 * indicates that they are completely different.
	 * 
	 * @param my
	 *            my {@link Bid}
	 * @param other
	 *            The other {@link Bid} to compare with.
	 * @return value between 0 and 1. 1 means totally the same bid, 0 totally
	 *         different.
	 */
	public double getSimilarity(Bid my, Bid other) {
		double sum = 0.0;
		double total = 0.0;
		HashMap<Integer, Value> fValues = my.getValues();
		HashMap<Integer, Value> otherfValues = other.getValues();
		Set<Entry<Integer, Value>> value_set_this = fValues.entrySet();
		Iterator<Entry<Integer, Value>> value_it_this = value_set_this
				.iterator();
		Set<Entry<Integer, Value>> value_set_other = otherfValues.entrySet();
		Iterator<Entry<Integer, Value>> value_it_other = value_set_other
				.iterator();
		while (value_it_this.hasNext()) {
			int ind = ((Entry<Integer, Value>) value_it_this.next()).getKey();
			((Entry<Integer, Value>) value_it_other.next()).getKey();
			// Objective isn't recognized here, GKW. hdv
			Object tmpobj = utilitySpace.getDomain().getObjectivesRoot()
					.getObjective(ind);
			if (tmpobj != null) {
				if (fValues.get(ind).equals(otherfValues.get(ind))) {
					sum += 1.0;
				}
				total += 1.0;
			} else {
				System.out.println("objective with index " + ind
						+ " does not exist");
			}

		}
		if (total == 0.0) {
			return 0.0;
		}
		return sum / total;

	}

	/**
	 * Get the bid that has biggest myUtility.
	 * 
	 * @param point1
	 *            first bid
	 * @param point2
	 *            second bid
	 * @return best bid of the two points provided.
	 */
	private BidPoint bestBid(BidPoint point1, BidPoint point2) {
		if (isBetter(point1, point2)) {
			return point1;
		}
		return point2;
	}

	/**
	 * Would be nice to have this in BidPoint.
	 * 
	 * @param point1
	 * @param point2
	 * @return true if point1 is better for us than point2.
	 */
	private boolean isBetter(BidPoint point1, BidPoint point2) {
		return point1.getUtilityA() >= point2.getUtilityA();
	}

	/****************** OptimalBidder code *************/
	/**
	 * Optimal bidder basically means that we try to reach a target utility
	 * depending on the time left.
	 */

	/**
	 * Target utility that we want given a number of turns left. This is the
	 * normalized value, considering the current maximum utility. When we are
	 * past deadline, we stick to the minimum target util. This is conform the
	 * discussion with Tim in #957 (oct2014).
	 * 
	 * @param myTurnsLeft
	 *            the number of turns left. Typically you use something like<br>
	 * 
	 *            <code>((DiscreteTimeline) timeline).getOwnRoundsLeft()</code>
	 * 
	 * @return current targetutility.
	 * @throws IllegalStateException
	 *             if the {@link #RESERVATION_VALUE} is bigger than the maximum
	 *             utility bid.
	 */
	private double getTargetUtility(int myTurnsLeft) throws Exception {

		if (myTurnsLeft < 0) {
			myTurnsLeft = 0;
		}
		AdditiveUtilitySpace us = (AdditiveUtilitySpace) historySpace
				.getOutcomeSpace().getMyUtilitySpace();
		double maxutil = us.getUtility(us.getMaxUtilityBid());
		if (RESERVATION_VALUE > maxutil) {
			throw new IllegalStateException(
					"reservation value is larger than the max utility bid.");
		}

		return RESERVATION_VALUE + targetUtilNormalized(myTurnsLeft)
				* (maxutil - RESERVATION_VALUE);
	}

	/**
	 * Get a normalized target utility ranging from 0.5 to 1. Computation of the
	 * bid for round j as in prop 4.3. Basically this increases with increasing
	 * number of remaining rounds. When many rounds are left, target utility=1.
	 * When no rounds are left, this decreases to 0.5 + 0.5*reservation value. <br>
	 * <b>WARNING</b>this is the unscaled utility. If the utility range is not
	 * entirely [0,1], a scaling function applies #957, see also
	 * {@link #getTargetUtility()}.
	 * 
	 * <h1>Discussion</h1> Tim explained why the target utility is not
	 * {@link #RESERVATION_VALUE} when turnsLeft=0. This is because you never
	 * want to go all the way down to your reservation value when bidding and is
	 * intended this way. This targetUtil function is used to make concessions,
	 * not for acceptance strategy. For accepting, we may still accept at the
	 * {@link #RESERVATION_VALUE}. <br>
	 * After more discussion, Tim proposed this version. See #957.
	 * 
	 * Wouter: After more discussion and #975, we decided to change this
	 * behaviour. In the last turn, we want to have 0 so that we can scale this
	 * to RESERVATION_VALUE in the scaled function. This is a straightforward
	 * modification from the previous version (which returned 0.5 when
	 * turnsleft=0) because we are shifting back exactly 1 turn.
	 * 
	 * @param myTurnsLeft
	 *            minimum=0 when we are in the last round
	 * @return normalized ( range <0.5-1] ) target utility.
	 */
	private double targetUtilNormalized(int myTurnsLeft) {
		if (myTurnsLeft == 0) {
			return 0;
		} else {
			return 0.5 + 0.5 * Math.pow(targetUtilNormalized(myTurnsLeft - 1),
					2);
		}
	}

	@Override
	public String getLastBidExplanation() {
		return lastBidExplanation;
	}

}

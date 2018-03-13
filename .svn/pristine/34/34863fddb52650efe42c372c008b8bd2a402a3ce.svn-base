package agents.ai2014.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group4 extends AbstractNegotiationParty {

	private Double currentUtility = 0.0;
	private Double threshold;
	private Double RESERVATION_VALUE;
	private final Double STARTING_THRESHOLD = 0.9;
	private final Double COMPROMISE_RATE = 4.0; // 1 is linear, higher is slower
												// to compromise
	private int turns;
	private int round = 0;

	private Bid highestBid;
	private Bid lastGivenBid;
	private Bid lastReceivedBid;
	private HashMap<String, Party> parties = new HashMap<String, Party>();
	private ArrayList<List<ValueDiscrete>> values = new ArrayList<List<ValueDiscrete>>();
	private BidGenerator bidGenerator;

	private HashMap<Bid, Double> possibleBids = new HashMap<Bid, Double>();
	private AbstractUtilitySpace utilitySpace;

	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace
	 *            Your utility space.
	 * @param deadlines
	 *            The deadlines set for this negotiation.
	 * @param timeline
	 *            Value counting from 0 (start) to 1 (end).
	 * @param randomSeed
	 *            If you use any randomisation, use this seed for it.
	 */
	@Override
	public void init(NegotiationInfo info) {
		// Make sure that this constructor calls it's parent.
		super.init(info);

		this.utilitySpace = info.getUtilitySpace();
		for (Issue issue : utilitySpace.getDomain().getIssues()) {
			values.add(((IssueDiscrete) issue).getValues());
		}

		// creates the generator
		generatePossibleBids(0, null);
		turns = getDeadlines().getType() == DeadlineType.ROUND ? getDeadlines().getValue() : 0; // -1
		// helps
		// with
		// very
		// low
		// deadline,
		// doesn't
		// hurt
		// large.
		bidGenerator = new BidGenerator(this, possibleBids, turns);

		RESERVATION_VALUE = utilitySpace.getReservationValue();
	}

	private void generatePossibleBids(int n, HashMap<Integer, Value> bidValues) {
		if (n >= values.size()) {
			Bid b = null;

			try {
				b = new Bid(utilitySpace.getDomain(), bidValues);
				possibleBids.put(b, getUtility(b));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		for (Value v : values.get(n)) {
			HashMap<Integer, Value> currentBid;

			if (n == 0) {
				currentBid = new HashMap<Integer, Value>();
			} else {
				currentBid = (HashMap<Integer, Value>) bidValues.clone();
			}

			currentBid.put(n + 1, v);
			generatePossibleBids(n + 1, currentBid);
		}
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
		round++;
		threshold = STARTING_THRESHOLD
				- (STARTING_THRESHOLD - RESERVATION_VALUE) * Math.pow((double) round / (double) turns, COMPROMISE_RATE);

		/*
		 * if(round >= turns){ return new Accept(); }
		 */

		if (!validActions.contains(Accept.class) || currentUtility < threshold) {
			Bid b = null;
			// if it's first turn, get out with best possible bid
			if (lastGivenBid == null) {
				try {
					b = utilitySpace.getMaxUtilityBid();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// do something to get the bid as answer
			else {
				// it generates the best not used bid
				int index = 0;
				do {
					b = bidGenerator.generateBestBid();

					index++;
					if (index > 100) {
						index = 0;
						threshold = threshold - 0.01; // safety measure
					}
				} while (getUtility(b) < threshold);

			}
			setLastGivenBid(b);
			return new Offer(getPartyId(), b);

		} else {
			return new Accept(getPartyId(), lastReceivedBid);
		}
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

		if (!parties.containsKey(sender.toString())) {
			Party party = new Party(sender.toString(), utilitySpace.getDomain(), sender);
			parties.put(sender.toString(), party);
		}

		if (lastReceivedBid != null) {
			parties.get(sender.toString()).updateWithBid(lastReceivedBid, action);
		}

		if (action instanceof Offer) {
			lastReceivedBid = DefaultAction.getBidFromAction(action);
			currentUtility = getUtility(lastReceivedBid);
			updateHighestBid(lastReceivedBid);
		} else if (action instanceof Accept) {
		}

	}

	private void updateHighestBid(Bid b) {
		// TODO it check if it is the highest and in case update it
	}

	// *******GETTER AND SETTER********

	public Double getCurrentUtility() {
		return currentUtility;
	}

	public void setCurrentUtility(Double currentUtility) {
		this.currentUtility = currentUtility;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public Bid getHighestBid() {
		return highestBid;
	}

	public void setHighestBid(Bid highestBid) {
		this.highestBid = highestBid;
	}

	public Bid getLastGivenBid() {
		return lastGivenBid;
	}

	public void setLastGivenBid(Bid lastGivenBid) {
		this.lastGivenBid = lastGivenBid;
	}

	public HashMap<String, Party> getParties() {
		return parties;
	}

	public void setParties(HashMap<String, Party> parties) {
		this.parties = parties;
	}

	public int getTurns() {
		return turns;
	}

	protected AgentID partyId = new AgentID("Group 4");

	@Override
	public String getDescription() {
		return "ai2014 group4";
	}

}

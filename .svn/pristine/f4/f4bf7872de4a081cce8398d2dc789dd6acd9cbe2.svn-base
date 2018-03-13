package agents.ai2014.group5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;

/**
 * Implementation of the bidding strategy. 1) In the first round the agent will
 * offer a bid with max utility for itself. 2) In the subsequent rounds, until a
 * specific time has elapsed, the agent will bid selfishly and only concede a
 * tiny bit. This is because the opponent models are unreliable in the
 * beginning. 3) Afterwards, it will use a tit-for-tat strategy to hopefully
 * reach a Nash point, while increasingly concede utility in the later rounds.
 * To achieve this it will interact with the opponent models to compare the
 * estimated utilities of the opponents when deciding on bids.
 */
public class BiddingStrategy {
	// The minimum utility for our agent that we will consider when computing
	// bids
	private static final double MIN_UTIL = 0.3;

	private Random randGen;

	// Domain of agent, all bids in this domain, and best bids in domain
	private Domain domain;
	private List<Bid> allBids;
	private List<Bid> maxBids;

	// The agent and fields from the agent
	private Group5 agent;
	private Map<String, OpponentModel> opponentModels;
	private List<Map<String, Integer>> values;
	private List<Map<Integer, String>> valuesRev;

	// Currently calculated Nash point
	private Bid curNash;

	// The last bid made by us
	public Bid lastBid;

	// The last bid and most recent bid received from an opponent
	public Bid prevOpponentBid, currentOpponentBid;

	// The current round
	public int round = 0;

	// The deadline (can be null!)
	public Integer deadline;

	public BiddingStrategy(Domain domain,
			Map<String, OpponentModel> opponentModels,
			List<Map<String, Integer>> values,
			List<Map<Integer, String>> valuesRev, Group5 agent) {
		this.domain = domain;
		this.opponentModels = opponentModels;
		this.values = values;
		this.valuesRev = valuesRev;
		this.agent = agent;
		this.randGen = new Random();
		this.maxBids = new ArrayList<Bid>();

		createAllBids();
	}

	/**
	 * Computes all bids in the domain that have an utility of at least
	 * {@link #MIN_UTIL}.
	 */
	private void createAllBids() {
		// Count bids
		List<Integer> numValuesEach = new ArrayList<Integer>();
		int numBids = 1;
		for (int i = 0; i < values.size(); i++) {
			int x = values.get(i).size();
			numValuesEach.add(x);
			numBids *= x;
		}

		// Compute bids represented as lists of value indices
		List<List<Integer>> bidIndices = new ArrayList<List<Integer>>(
				Collections.nCopies(numBids, (List<Integer>) null));
		for (int i = 0; i < bidIndices.size(); i++) {
			bidIndices.set(i, new ArrayList<Integer>(values.size()));
		}
		int bound = numBids;
		for (int i : numValuesEach) {
			bound /= i;
			int count = 0;
			int j = 0;
			for (List<Integer> k : bidIndices) {
				k.add(j);
				count++;
				if (count == bound) {
					j++;
					count = 0;
				}
				if (j == i) {
					j = 0;
				}
			}
		}

		// Create actual bids from value indices lists
		allBids = new ArrayList<Bid>();
		for (int i = 0; i < bidIndices.size(); i++) {
			Bid bid = createBid(bidIndices.get(i));
			if (agent.getUtility(bid) >= MIN_UTIL) {
				allBids.add(bid);
			}
		}
	}

	private Bid createBid(List<Integer> valueIndices) {
		HashMap<Integer, Value> v = new HashMap<Integer, Value>();
		for (int i = 0; i < valuesRev.size(); i++) {
			String vname = valuesRev.get(i).get(valueIndices.get(i));
			v.put(i + 1, new ValueDiscrete(vname));
		}
		try {
			return new Bid(domain, v);
		} catch (Exception e) {
			agent.println("Error: cannot create bid " + v);
		}
		return null;
	}

	/**
	 * Creates a bid according to the bidding strategy See the documentation of
	 * this class above for a description of the bidding strategy
	 */
	public Bid generateBid() {
		// New round, new Nash point
		round++;
		updateNashProduct();

		// 1) Offer the best bid for us in the first round
		if (lastBid == null || currentOpponentBid == null) {
			return generateMaxBid();
		}

		// 2.1) Allow to concede a bit more each round
		int cutoff = deadline == null ? 10 : Math.max(deadline / 2, 10);
		double maxConcession = 0.01;
		if (deadline != null) {
			// double c = 2.25;
			double c = 3.5;
			maxConcession = Math.exp((round - cutoff) / (deadline / c))
					/ (1.5 * Math.exp(c));
			agent.println("Max concession: " + maxConcession);
		}

		// 2.2) Do not use the opponent models in the beginning as they are
		// unreliable
		if (round <= cutoff) {
			Bid b = selfishStep(maxConcession);
			if (b == null) {
				agent.println("Warning: could not make a selfish offer");
				return null;
			}
			agent.println("Made selfish move: " + agent.getUtility(b) + ", "
					+ b);
			return b;
		}

		// 3.1) Estimate Nash point
		if (curNash == null) {
			// Huh
			Bid b = selfishStep(maxConcession);
			agent.println("No Nash bid!?");
			return b;
		}

		// 3.2) Find concession towards Nash point
		double nashBidUtil = agent.getUtility(curNash);
		double prevOpponentBidUtil = agent.getUtility(prevOpponentBid);
		double curOpponentBidUtil = agent.getUtility(currentOpponentBid);
		double opponentConcession = (prevOpponentBidUtil - curOpponentBidUtil)
				/ Math.abs(nashBidUtil - prevOpponentBidUtil);

		// 3.3) Mirror bid in our utility relative to Nash utility
		double lastBidUtil = agent.getUtility(lastBid);
		double concession = opponentConcession
				* Math.abs(nashBidUtil - lastBidUtil);
		concession = Math.min(maxConcession, concession);
		if (Double.isNaN(opponentConcession) || opponentConcession < 0.001) {
			// But if the opponent did not succeed in increasing our utility
			// then do not concede too much of our utility when offering the
			// next bid
			concession = 0.01;
		}

		// 3.4) Make a nice move
		Bid niceMove = niceStepClosestsNash(lastBid, agent.getUtility(lastBid),
				concession);
		if (niceMove == null || niceMove.equals(lastBid)) {
			agent.println("Warning: could not make a new nice move (making selfish move instead): "
					+ niceMove);
			return selfishStep(maxConcession);
		}
		agent.println("Made nice move: " + agent.getUtility(niceMove) + ", "
				+ niceMove);
		return niceMove;
	}

	/**
	 * Product of the (estimated) utilities of all agents, including us
	 */
	public double utilityProduct(Bid bid) {
		double p = agent.getUtility(bid);
		for (OpponentModel m : opponentModels.values()) {
			p *= m.expectedUtilityOf(bid);
		}
		return p;
	}

	/**
	 * Finds a bid with the maximal utility product Should be called at every
	 * round
	 */
	private void updateNashProduct() {
		double max = Double.MIN_VALUE;
		for (Bid bid : allBids) {
			double u = utilityProduct(bid);
			if (u > max) {
				max = u;
				curNash = bid;
			}
		}
	}

	/**
	 * A bid that is the best possible for us
	 */
	private Bid generateMaxBid() {
		if (maxBids.size() == 0) {
			double max = Double.MIN_VALUE;
			for (Bid bid : allBids) {
				max = Math.max(agent.getUtility(bid), max);
			}
			for (Bid bid : allBids) {
				if (Math.abs(agent.getUtility(bid) - max) < 0.001) {
					maxBids.add(bid);
				}
			}
			agent.println("Found " + maxBids.size() + " max bids");
		}
		if (maxBids.size() > 0) {
			int randomIndex = randGen.nextInt(maxBids.size());
			return maxBids.get(randomIndex);
		}
		return null;
	}

	/**
	 * Distance to Nash product projected to our utility
	 */
	private double distanceToNash(Bid bid) {
		return Math.abs(utilityProduct(curNash) - utilityProduct(bid));
	}

	/**
	 * Finds the bid closest to the estimated Nash product with an utility
	 * approximately equal to <code>curUtil</code> but with a difference of up
	 * to <code>concession</code>.
	 */
	private Bid niceStepClosestsNash(Bid lastBid, double curUtil,
			double concession) {
		if (Double.isNaN(concession)) {
			agent.println("Warning: concession is NaN");
			concession = 0.001;
		}
		double minDist = Double.MAX_VALUE;
		Bid niceBid = null;
		for (Bid bid : allBids) {
			if (lastBid != null && lastBid.equals(bid)) {
				continue;
			}
			double util = agent.getUtility(bid);
			if (Math.abs(curUtil - util) <= concession) {
				double dist = distanceToNash(bid);
				if (dist < minDist) {
					minDist = dist;
					niceBid = bid;
				}
			}
		}
		return niceBid;
	}

	/**
	 * Finds a good bid that does not consider the opponent's preferences
	 */
	private Bid selfishStep(double concession) {
		if (Double.isNaN(concession)) {
			agent.println("Warning: concession is NaN");
			concession = 0.05;
		}
		List<Bid> bids = new ArrayList<Bid>();
		for (Bid bid : allBids) {
			double util = agent.getUtility(bid);
			if (Math.abs(1.0 - util) <= concession) {
				bids.add(bid);
			}
		}
		if (bids.size() == 0) {
			return null;
		}
		int randomIndex = randGen.nextInt(bids.size());
		return bids.get(randomIndex);
	}

	public void updateOffer(Bid bid) {
		prevOpponentBid = currentOpponentBid;
		currentOpponentBid = bid;
	}

	public void setDeadline(Integer newDeadline) {
		if (newDeadline != null && newDeadline != deadline) {
			deadline = newDeadline;
		}
	}
}

package parties.in4010.q12015.group7;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

/**
 * Negotiation Party of Group 7
 */
public class Group7 extends AbstractNegotiationParty {
	/**
	 * Default toughness of this agent 0 = very tough, 1 = easily gives in
	 */
	static final double DEFAULT_TOUGHNESS = 0.2;

	/**
	 * The amount the toughness is increased according to the number of
	 * opponents (so that the agent gives in more easily!) when calculating the
	 * minimum utility
	 */
	static final double[] ADD_TOUGHNESS_NR_PARTIES = { 0.00, // (this never
																// happens, 0
																// parties, no
																// opponents)
			0.00, // (this never happens, 1 party, no opponents)
			0.00, // 2 parties, 1 opponent
			0.10, // 3 parties, 2 opponents
			0.15, // 4 parties, 3 opponents
			0.20 // 5 parties, 4 opponents
	};

	/**
	 * Minimum toughness of this agent (even when copying opponent) 0 = very
	 * tough, 1 = easily gives in
	 */
	static final double MIN_TOUGHNESS = 0.03;

	/**
	 * Minimum acceptable toughness of opponent before copying toughness of
	 * opponent (should not be lower than default_toughness) 0 = very tough, 1 =
	 * easily gives in
	 */
	static final double MIN_ACCEPTABLE_TOUGHNESS = 0.2;

	/**
	 * Time after which the agent stops copying the opponents toughness
	 */
	static final double MAX_TIME_COPY_TOUGHNESS = 0.8;

	/**
	 * Time after which the agent accepts if a bid is higher than its dynamic
	 * minimum utility
	 */
	static final double TIME_ACCEPT_ABOVE_MIN_UTILITY = 0.5;

	/**
	 * Number of bids the toughness estimation of the opponent depends on
	 */
	static final int TOUGHNESS_NUM_BIDS = 10;

	/**
	 * Sensitivity parameter for estimation of issue and value weights
	 */
	static final double ESTIMATOR_N = 0.1;

	/**
	 * Maximum amount the received bid is less than the utility of our last bid
	 * in order to accept the received bid
	 */
	static final double MAX_DIFF_UTILITY = 0.025;

	/**
	 * Time after which the agent uses bid optimization for opponents
	 */
	static final double MAX_TIME_BID_WITH_ESTIMATOR = 0.33;

	/**
	 * Number of bids we want to accept with a certain probability
	 */
	static final int NUM_BIDS_ACCEPT_PROBABILITY = 30;

	/**
	 * Turn the logger on or off. Turning the logger on will result in a file
	 * for every negotation in the folder ../logs/
	 */
	static final boolean CREATE_LOGS = false;

	/**
	 * BidHistory of all opponents
	 */
	private MultipleBidHistory opBidHistory;

	/**
	 * Value and issue weight estimator for all opponents
	 */
	private MultipleIssueEstimator opIssueEstimator;

	/**
	 * BidHistory for own bids
	 */
	private EnhancedBidHistory myBidHistory;

	/**
	 * Bidspace for own bids
	 */
	private BidSpace bidSpace;

	/**
	 * The maximum possible utility for our agent (for some reason this is not
	 * always 1!)
	 */
	private double maxUtility;

	/**
	 * Toughness of the agent from 0 to 1 0 = very tough, 1 = easily gives in
	 */
	private double toughness;

	/**
	 * Logger
	 */
	private Logger logger;

	/**
	 * Constructor for this agent
	 */
	public Group7() {
		opIssueEstimator = new MultipleIssueEstimator(ESTIMATOR_N);
		opBidHistory = new MultipleBidHistory();
		myBidHistory = new EnhancedBidHistory();
		bidSpace = new BidSpace();
		logger = new Logger(CREATE_LOGS);
	}

	/**
	 * Initiates the agent
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		toughness = DEFAULT_TOUGHNESS;

		Bid bid = null;
		try {
			bid = this.utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.maxUtility = getUtility(bid);
		this.populateBidSpace();
	}

	/**
	 * Populates the bidSpace with 1000 bids above the minimum minimum utility
	 */
	public void populateBidSpace() {
		Bid bid = null;
		try {
			bid = this.utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		bidSpace.add(bid, getUtility(bid));
		for (int i = 0; i < 1000; i++) {
			bid = generateRandomBid();
			while (getUtility(bid) < 1 - DEFAULT_TOUGHNESS) {
				bid = generateRandomBid();
			}
			bidSpace.add(bid, getUtility(bid));
		}
	}

	/**
	 * Function which gets called if it is our turn to bid / accept / decline
	 * 
	 * @return Offfer
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		adjustToughness();

		logger.logln("");
		logger.logln("Time: " + this.getTimeLine().getTime());
		logger.logln("Toughness: " + this.toughness);
		logger.logln("Max utility: " + this.maxUtility);
		logger.logln("Min utility: " + this.minUtility());
		logger.logln("Probability of acceptance: " + this.probAcceptance());
		if (this.acceptWithProbability()) {
			logger.logln("In state of accepting with certain probability...");
		}
		logger.logln("");
		if (!validActions.contains(Accept.class)) {
			Bid bid = firstBid();
			return generateOffer(bid);
		} else if ((myBidHistory.getLastBid() != null && getUtility(this.opBidHistory.getCombinedHistory()
				.getLastBid()) > (getUtility(this.myBidHistory.getLastBid()) - MAX_DIFF_UTILITY))) {

			logger.logln("");
			logger.logln("-- ACCEPTED");
			logger.logln("My previous bid: " + this.myBidHistory.getLastBid());
			logger.logln("Accepted bid with u = " + getUtility(this.opBidHistory.getCombinedHistory().getLastBid())
					+ ", because little difference with last bid with u = " + getUtility(myBidHistory.getLastBid()));
			logger.logln("");
			return new Accept(getPartyId(), this.opBidHistory.getCombinedHistory().getLastBid());

		} else if (this.getTimeLine().getTime() > TIME_ACCEPT_ABOVE_MIN_UTILITY
				&& getUtility(this.opBidHistory.getCombinedHistory().getLastBid()) > this.minUtility()) {

			logger.logln("");
			logger.logln("-- ACCEPTED");
			logger.logln("Accepted bid with u = " + getUtility(this.opBidHistory.getCombinedHistory().getLastBid())
					+ ", because its utility was higher than the minimal required utility u = " + this.minUtility()
					+ ".");
			logger.logln("");
			return new Accept(getPartyId(), this.opBidHistory.getCombinedHistory().getLastBid());

		} else if (new Random().nextDouble() < Math.min(1, this.probAcceptance()) && this.acceptWithProbability()) {

			logger.logln("");
			logger.logln("-- ACCEPTED");
			logger.logln("Accepted because of the acceptance probability of " + this.probAcceptance() + ".");
			logger.logln("");
			return new Accept(getPartyId(), this.opBidHistory.getCombinedHistory().getLastBid());
		} else {
			Bid bid;
			if (this.getTimeLine().getTime() > MAX_TIME_BID_WITH_ESTIMATOR) {
				bid = regularBidMaxOpponentUtilityEstimation();
			} else {
				bid = normalBid();
			}
			return generateOffer(bid);
		}
	}

	public double probAcceptance() {
		return Math.exp(-(Math.abs(this.maxUtility - getUtility(this.opBidHistory.getCombinedHistory().getLastBid())))
				/ (this.getTimeLine().getTime() / 10));
	}

	public boolean acceptWithProbability() {
		// If there is only one party and we have not performed any bid, return
		// false
		if (this.myBidHistory.size() == 0)
			return false;

		// If we have time left for numberOfBids bids left
		if (1 - this.getTimeLine().getTime() < NUM_BIDS_ACCEPT_PROBABILITY
				* (this.getTimeLine().getTime() / this.myBidHistory.size()))
			return true;

		// Else return false
		return false;
	}

	/**
	 * Returns the bid to be performed as the first bid
	 * 
	 * @return first maximum utility bid
	 */
	public Bid firstBid() {
		Bid bid = null;
		try {
			bid = utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bid;
	}

	/**
	 * Returns a bid corresponding to the agents dynamic minimum utility and the
	 * highest average estimated utility for the opponents
	 * 
	 * @return a bid
	 */
	public Bid regularBidMaxOpponentUtilityEstimation() {
		Bid maxBid = null;
		double maxUtil = 0;
		double util;

		ArrayList<Bid> list = bidSpace.getBids(this.minUtility(), this.maxUtility);
		for (Bid myBid : list) {
			util = this.opIssueEstimator.getAverageUtility(myBid);
			if (util > maxUtil) {
				maxBid = myBid;
				maxUtil = util;
			}
		}

		return maxBid;
	}

	/**
	 * Returns a bid corresponding to the minimum utility
	 * 
	 * @return a regular bid
	 */
	public Bid normalBid() {
		Bid myBid = bidSpace.generatePseudoRandomBid(this.minUtility(), this.maxUtility);

		return myBid;
	}

	/**
	 * Logs the bid, adds the bid to the BidHistory, generates an offer with the
	 * bid.
	 * 
	 * @param bid
	 * @return offer according to bid
	 */
	public Offer generateOffer(Bid bid) {
		logger.logln("");
		logger.logln("-- BID OFFERED: ");
		logger.logln("Bid: " + bid);
		logger.logln("Utility: " + getUtility(bid));
		for (Map.Entry<AgentID, IssueEstimator> entry : this.opIssueEstimator.getIssueEstimators().entrySet()) {
			logger.logln("Opponent Toughness: "
					+ this.opBidHistory.getHistory(entry.getKey()).getToughness(TOUGHNESS_NUM_BIDS));
			logger.logln("Estimated Opponent Utility (" + entry.getKey() + "): "
					+ this.opIssueEstimator.getIssueEstimator(entry.getKey()).getUtility(bid));
		}
		logger.logln(" ");

		myBidHistory.add(new BidDetails(new Bid(bid), getUtility(bid)));

		logger.logln(myBidHistory.getLastBid());

		return new Offer(getPartyId(), bid);
	}

	/**
	 * Returns the minimum acceptable utility of this agent according to the
	 * time and toughness
	 * 
	 * @return minimum utility
	 */
	public double minUtility() {
		return this.maxUtility - (this.getTimeLine().getTime() * this.toughness);
	}

	/**
	 * Adjust the toughness of this agent according to the toughness of
	 * opponents
	 */
	public void adjustToughness() {
		if (this.opBidHistory.getMinToughness(TOUGHNESS_NUM_BIDS) < DEFAULT_TOUGHNESS
				&& this.getTimeLine().getTime() < MAX_TIME_COPY_TOUGHNESS) {
			this.toughness = Math.max(MIN_TOUGHNESS, this.opBidHistory.getMinToughness(TOUGHNESS_NUM_BIDS));
		} else {
			this.toughness = DEFAULT_TOUGHNESS + this.getAdditionalToughnessNumberOfParties();
		}
	}

	/**
	 * Get less toughness according to the number parties
	 */
	public double getAdditionalToughnessNumberOfParties() {
		if (this.getNumberOfParties() < ADD_TOUGHNESS_NR_PARTIES.length) {
			return ADD_TOUGHNESS_NR_PARTIES[this.getNumberOfParties()];
		}
		return ADD_TOUGHNESS_NR_PARTIES[ADD_TOUGHNESS_NR_PARTIES.length - 1];
	}

	/**
	 * Function that is called when a message is received by one of the
	 * opponents
	 */
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		AgentID agentId = new AgentID(String.valueOf(sender));

		if (action.getClass() == Offer.class) {
			Offer opponentOffer = (Offer) action;
			this.opIssueEstimator.addBid(agentId,
					new BidDetails(new Bid(opponentOffer.getBid()), getUtility(opponentOffer.getBid())),
					this.opBidHistory.getHistory(agentId));
			this.opBidHistory.addBid(agentId,
					new BidDetails(new Bid(opponentOffer.getBid()), getUtility(opponentOffer.getBid())));

			logger.logln("");
			logger.logln("-- BID FROM PARTY " + agentId);
			logger.logln("Bid: " + opponentOffer.getBid());
			logger.logln("Utility: " + getUtility(opponentOffer.getBid()));
			logger.logln("");
		} else {
			logger.logln("");
			logger.logln("-- ACCEPTANCE FROM PARTY " + agentId);
			logger.logln("");
		}
	}

	@Override
	public String getDescription() {
		return "Party Group 7";
	}
}

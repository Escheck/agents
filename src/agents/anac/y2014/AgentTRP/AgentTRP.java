package agents.anac.y2014.AgentTRP;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.NegotiationResult;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;

/**
 * This agent is an example of how to create an ANAC2013 agent which learns
 * during the tournament. This agent is a variant of the random agent.
 * 
 * @author M. Hendrikx
 */
public class AgentTRP extends Agent {

	/** The minimum utility a bid should have to be accepted or offered. */
	private double MINIMUM_BID_UTILITY;
	/** The opponent's last action. */
	private Bid opponentLastBid;
	/** Ã§â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Â®Bid */
	private Bid opponentBid;
	/**
	 * Ã¨â€¡ÂªÃ¥Ë†â€ Ã£ï¿½Â«Ã£ï¿½Â¨Ã£ï¿½Â£Ã£ï¿½Â¦Ã¤Â¸â‚¬Ã§â€¢ÂªÃ¨â€°Â¯Ã£ï¿½â€žÃ§
	 * â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Å’Ã¦ï¿½ï¿½Ã¦Â¡Ë†Ã£ï¿½â€”Ã£ï¿½Å¸Bid
	 */
	private Bid myBestBidByOpponent;
	/**
	 * Ã¨â€¡ÂªÃ¥Ë†â€ Ã£ï¿½Â«Ã£ï¿½Â¨Ã£ï¿½Â£Ã£ï¿½Â¦Ã¤Â¸â‚¬Ã§â€¢ÂªÃ¨â€°Â¯Ã£ï¿½
	 * â€žBid
	 */
	private Bid myBestBid;
	/**
	 * Ã£ï¿½Å Ã¤Âºâ€™Ã£ï¿½â€žÃ£ï¿½Â«Ã£ï¿½Â¨Ã£ï¿½Â£Ã£ï¿½Â¦Ã¨â€°Â¯Ã£ï¿½â€¢Ã£ï¿½ï¿½
	 * Ã£ï¿½â€ Ã£ï¿½ÂªÃ¦â€°â€¹
	 */
	private Bid bestBid;
	/** Ã¥Â¦Â¥Ã¥ï¿½â€�Ã£ï¿½Â®Ã¥Â¢Æ’Ã§â€¢Å’ */
	private double[] minimumUtilities;

	// Ã¥Â¼Â·Ã¦Â°â€”Ã£ï¿½Â¨Ã£ï¿½â€¹Ã¥Â¼Â±Ã¦Â°â€”Ã£ï¿½Â¨Ã£ï¿½â€¹Ã£ï¿½Â®Ã¦Ë†Â¦Ã§â€¢Â¥
	/**
	 * 0 Ã©ï¿½Å¾Ã¥Â¦Â¥Ã¥ï¿½â€� + Ã¦Å½Â¢Ã§Â´Â¢Ã¦Â°â€”Ã¥â€˜Â³ 1
	 * Ã§â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Å’Ã¥Â¼Â·Ã¦Â°â€”Ã£ï¿½Â¨Ã¥Ë†Â¤Ã¦â€“Â­ ->
	 * Ã§â€ºÂ¸Ã¦â€°â€¹
	 * Ã£ï¿½Â®Ã£Æ’â„¢Ã£â€šÂ¹Ã£Æ’Ë†Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°Ã¦Å â€¢Ã£ï¿½â€™Ã£â€šâ€¹ 2
	 * Ã¦â„¢Â®Ã©â‚¬Å¡ -> SA
	 */
	private int mode;

	/** 1Ã¨Â©Â¦Ã¥ï¿½Ë†Ã£ï¿½Â®Ã§Â·ï¿½Ã¤ÂºÂ¤Ã¦Â¸â€°Ã¥â€ºÅ¾Ã¦â€¢Â° */
	private int round;

	private Random rand = new Random();

	private TRPSessionData mySessionData;

	public AgentTRP() {
	}

	/**
	 * Initialize the target utility to MAX(rv, max). Where rv is the
	 * reservation value of the preference profile and max is the highest
	 * utility received on the current preference profile.
	 */
	public void init() {
		// System.out.println("SessionsNr : " + sessionNr);
		round = 0;
		/*
		 * Ã¦Ë†Â¦Ã§â€¢Â¥Ã£â€šâ€™Ã¨Â¨Â­Ã¥Â®Å¡Ã£ï¿½â„¢Ã£â€šâ€¹
		 * Ã©ï¿½Â¸Ã¦Å Å¾Ã£ï¿½Â®Ã¦â€“Â¹Ã¦Â³â€¢ 1.Ã¥Â¯Â¾Ã¦Ë†Â¦Ã¥â€ºÅ¾Ã¦â€¢Â°
		 * 2.Ã¦Â±ÂºÃ¨Â£â€šÃ¥â€ºÅ¾Ã¦â€¢Â°
		 * 3.Ã¦Å“â‚¬Ã¥Â¾Å’Ã£ï¿½Â®Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°
		 * 4.Ã¥â€°ï¿½Ã¥â€ºÅ¾Ã£ï¿½Â®Ã§ÂµÅ’Ã©ï¿½Å½Ã¦â„¢â€šÃ©â€“â€œ
		 */

		// Ã¥ï¿½Ë†Ã¦â€žï¿½Ã§Å½â€¡
		double rate;

		// System.out.print(mySessionData.breakCount);
		// Ã¦Ë†Â¦Ã§Â¸Â¾Ã£â€šâ€™Ã£Æ’Â­Ã£Æ’Â¼Ã£Æ’â€°
		if (sessionNr == 0) {
			mySessionData = null;
			rate = 0;
		} else {
			mySessionData = initPrevSessionData();
			myBestBidByOpponent = new Bid(mySessionData.opponentBestBid);
			rate = (sessionNr - mySessionData.breakCount) / sessionNr;
		}

		// Ã¥Ë†ï¿½Ã¦Ë†Â¦
		if (sessionNr < 2) {
			// minimumUtilities = new
			// double[]{0.9,0.9,0.9,0.9,0.9,0.8,0.8,0.8,0.8,0.8};
			mode = 0; // 0
			return;
		} else if (rate > 0.5) {
			// minimumUtilities = new
			// double[]{0.9,0.9,0.9,0.9,0.9,0.8,0.8,0.8,0.75,0.75};
			mode = 3;
			return;
		} else {
			// minimumUtilities = new
			// double[]{0.9,0.9,0.9,0.9,0.9,0.8,0.8,0.8,0.75,0.75};
			mode = 2;
			return;
		}
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return "AgentTRP";
	}

	// Ã¥â€°ï¿½Ã¥â€ºÅ¾Ã£ï¿½Â®Ã¤ÂºÂ¤Ã¦Â¸â€°Ã§Âµï¿½Ã¦Å¾Å“Ã£â€šâ€™Ã¥â€˜Â¼Ã£ï¿½Â³Ã¥â€¡ÂºÃ£ï¿½â„¢
	public TRPSessionData initPrevSessionData() {
		TRPSessionData prev = (TRPSessionData) this.loadSessionData();
		prev.printAll();
		myBestBidByOpponent = prev.opponentBestBid;
		myBestBid = prev.myBestBid;
		return prev;
	}

	/**
	 * Set the target utility for the next match on the same preference profile.
	 * If the received utility is higher than the current target, save the
	 * received utility as the new target utility.
	 */
	// Ã§Âµâ€šÃ£â€šï¿½Ã£ï¿½Â£Ã£ï¿½Å¸Ã£ï¿½Â¨Ã£ï¿½ï¿½Ã£ï¿½Â«Ã¥â€˜Â¼Ã£ï¿½Â°Ã£â€šÅ’Ã£â€šâ€¹
	public void endSession(NegotiationResult result) {

		/*
		 * -- Ã¤ÂºÂ¤Ã¦Â¸â€°Ã§Âµï¿½Ã¦Å¾Å“Ã£â€šâ€™Ã¤Â¿ï¿½Ã¥Â­Ëœ --
		 * Ã¤Â¿ï¿½Ã¥Â­ËœÃ£ï¿½â„¢Ã£ï¿½Â¹Ã£ï¿½ï¿½Ã¦Æ’â€¦Ã¥Â Â±
		 * Ã¥ï¿½Ë†Ã¦â€žï¿½Ã£Æ’â€œÃ£Æ
		 * ’Æ’Ã£Æ’â€°Ã¯Â¼Å’Ã¥ï¿½Ë†Ã¦â€žï¿½Ã¦â„¢â€šÃ©â€“â€œ,
		 * Ã¥ï¿½Ë†Ã¦â€žï¿½Ã£ï¿½â€”Ã£ï¿½Å¸Ã£ï¿½â€¹,
		 * Ã¦Â±ÂºÃ¨Â£â€šÃ¥â€ºÅ¾Ã¦â€¢Â°Ã¯Â
		 * ¼Å’Ã¦Â¤Å“Ã§Â´Â¢Ã¯Â¼Å’Ã£Æ’Â©Ã£â€šÂ¦Ã£Æ’Â³Ã£Æ’â€°Ã¦â€¢Â°
		 */

		Bid lastBid;
		// Ã¦Å“â‚¬Ã¥Â¾Å’Ã£ï¿½Â®Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°
		if (mySessionData == null) {
			lastBid = result.getLastBid();
		} else {
			// Ã¤Â»Å Ã£ï¿½Â¾Ã£ï¿½Â§Ã£ï¿½Â§Ã¤Â¸â‚¬Ã§â€¢ÂªÃ£ï¿½â€žÃ£ï¿½â€žÃ¥ï¿½Ë†Ã¦â€žï¿½Ã§Âµï¿½Ã¦Å¾Å“Ã£â€šâ€™Ã¦Â Â¼Ã§Â´ï¿½
			if (getUtility(mySessionData.lastBid) < getUtility(result.getLastBid())) {
				lastBid = result.getLastBid();
			} else {
				lastBid = mySessionData.lastBid;
			}
		}
		// Ã¦Å“â‚¬Ã¥Â¾Å’Ã£ï¿½Â®Ã¦â„¢â€šÃ©â€“â€œ
		double lastTime = timeline.getTime();
		// Ã¥ï¿½Ë†Ã¦â€žï¿½Ã£ï¿½â€”Ã£ï¿½Å¸Ã£ï¿½â€¹Ã¥ï¿½Â¦Ã£ï¿½â€¹
		Boolean isAgree = result.isAgreement();

		boolean reset = false;
		if (sessionNr == 0) {
			reset = true;
		}
		TRPSessionData mySessionData = new TRPSessionData(lastBid, lastTime, isAgree, myBestBid, myBestBidByOpponent,
				round, reset);
		this.saveSessionData(mySessionData);
	}

	/**
	 * Retrieve the bid from the opponent's last action.
	 */
	// Ã£Æ’Â¡Ã£Æ’Æ’Ã£â€šÂ»Ã£Æ’Â¼Ã£â€šÂ¸Ã£â€šâ€™Ã¥ï¿½â€”Ã£ï¿½â€˜Ã¥ï¿½â€“Ã£ï¿½Â£Ã£ï¿½Å¸Ã£ï¿½Â¨Ã£ï¿½ï¿½Ã£ï¿½Â®Ã¥â€¡Â¦Ã§ï¿½â€ 
	public void ReceiveMessage(Action opponentAction) {
		opponentLastBid = DefaultAction.getBidFromAction(opponentAction);
		opponentBid = DefaultAction.getBidFromAction(opponentAction);
		if (myBestBidByOpponent == null || opponentBid == null) { // bestBidÃ£ï¿½Å’Ã£ï¿½ÂªÃ£ï¿½â€žÃ£ï¿½Â¨Ã£ï¿½ï¿½
			myBestBidByOpponent = utilitySpace.getDomain().getRandomBid(null);
		} else {
			if (getUtility(opponentBid) >= getUtility(myBestBidByOpponent)) { // bestBid
																				// Ã£â€šË†Ã£â€šÅ Ã£â€šË†Ã£ï¿½â€žÃ£â€šâ€šÃ£ï¿½Â®Ã£â€šâ€™Ã¦ï¿½ï¿½Ã¦Â¡Ë†Ã£ï¿½â€”Ã£ï¿½Å¸Ã£ï¿½Â¨Ã£ï¿½ï¿½
				myBestBidByOpponent = new Bid(opponentBid);
			}
			if (myBestBid == null || getUtility(opponentBid) >= getUtility(myBestBid)) {
				myBestBid = new Bid(opponentBid);
			}
		}
	}

	/**
	 * Accept if the utility of the opponent's is higher than the target
	 * utility; else return a random bid with a utility at least equal to the
	 * target utility.
	 */
	public Action chooseAction() {
		double time = timeline.getTime(); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã¦â„¢â€šÃ¥Ë†Â»

		// Ã£ï¿½â€œÃ£ï¿½Â®Ã¥â‚¬Â¤Ã£â€šË†Ã£â€šÅ Ã¥Â¤Â§Ã£ï¿½ï¿½Ã£ï¿½â€žBidÃ£ï¿½Â¯Ã¥ï¿½â€”Ã§ï¿½â€ Ã£ï¿½â„¢Ã£â€šâ€¹
		double minimun_utility = acceptBorder(time);
		round += 1; // Ã¤ÂºÂ¤Ã¦Â¸â€°Ã¥â€ºÅ¾Ã¦â€¢Â°Ã£â€šâ€™Ã£â€šÂ«Ã£â€šÂ¦Ã£Æ’Â³Ã£Æ’Ë†

		/** Accept */
		if (opponentLastBid != null && getUtility(opponentLastBid) >= minimun_utility) {
			return new Accept(getAgentID(), opponentLastBid);
		}
		if (mySessionData != null && getUtility(mySessionData.lastBid) < 0.6) {
			return mode(time);
		}

		// Ã¦Å“â‚¬Ã¥Ë†ï¿½Ã£ï¿½â€¹Ã¦Å“â‚¬Ã¥Â¾Å’Ã£ï¿½Â§1Ã¦Ë†Â¦Ã¤Â»Â¥Ã¤Â¸Å Ã¤ÂºÂ¤Ã¦Â¸â€°Ã£ï¿½â€”Ã£ï¿½Â¦Ã£Æ’â€¡Ã£Æ’Â¼Ã£â€šÂ¿Ã£ï¿½Å’Ã£ï¿½â€šÃ£â€šâ€¹Ã£ï¿½Â¨Ã£ï¿½ï¿½
		if ((time < 0.15 || time >= 0.85) && mySessionData != null) {
			// Ã§â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Å’Ã©Æ’Â½Ã¥ï¿½Ë†Ã£ï¿½Â®Ã£ï¿½â€žÃ£ï¿½â€žÃ£Æ’â€œÃ£Æ’Æ’Ã£Æ’Ë†Ã£â€šâ€™Ã¦ï¿½ï¿½Ã¦Â¡Ë†Ã£ï¿½â€”Ã£ï¿½Å¸Ã¥Â Â´Ã¥ï¿½Ë†
			// 0.85Ã¤Â»Â¥Ã¤Â¸Å Ã¥ï¿½â€“Ã£â€šÅ’Ã£â€šâ€¹Ã£ï¿½ÂªÃ£â€šâ€°
			if (mySessionData.isAgree && getUtility(mySessionData.lastBid) > 0.85) {
				if (mySessionData.isAgree && round < 100) {
					return new Offer(getAgentID(), mySessionData.lastBid);
				}
			}
		}

		switch (mode) {
		case 0:
			try {
				return mode0(time);
			} catch (Exception e) {
				e.printStackTrace();
			}
		case 1:
			return mode1(time);
		case 2:
			try {
				return mode2(time);
			} catch (Exception e) {
				e.printStackTrace();
			}
		case 3:
			return mode3(time);
		default:
			return mode(time);
		}
	}

	// Ã¥Â¼Â·Ã¦Â°â€”
	private double acceptBorder(double time) {
		return 0.85;
	}

	// Ã¥Â¦Â¥Ã¥ï¿½â€�Ã£â€šâ€™Ã£ï¿½Â»Ã£ï¿½Â¼Ã£ï¿½â€”Ã£ï¿½ÂªÃ£ï¿½â€žÃ£Æ’Â¢Ã£Æ’Â¼Ã£Æ’â€°
	private Action mode0(double time) throws Exception {
		if (time < 0.05) {
			return getBid0();
		} else if (time < 0.1) {
			return getBidA();
		} else if (time < 0.5) {
			return getBid0();
		} else if (time < 0.98) {
			return getBidA();
		} else {
			return getBidC();
		}
		// if(time< 0.99){
		// return getBidC();
		// } else {
		// // Ã£ï¿½ÂµÃ£ï¿½â€“Ã£ï¿½â€˜Ã£ï¿½Å¸Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°
		// return getBidD();
		// }
	}

	// Ã¦Å ËœÃ£â€šÅ’Ã£â€šâ€¹Ã£Æ’Â¢Ã£Æ’Â¼Ã£Æ’â€°
	private Action mode1(double time) {
		if (time < 0.95) {
			return getBid0();
		} else {
			return new Offer(getAgentID(), myBestBidByOpponent);
		}
	}

	// Ã¨Â¿â€˜Ã¥â€šï¿½Ã¦Å½Â¢Ã§Â´Â¢
	private Action mode2(double time) throws Exception {
		if (time < 0.05) {
			return getBid0();
		} else {
			return getBidB();
		}
	}

	// Ã¥Â¼Â·Ã¨ÂªÂ¿Ã£ï¿½â„¢Ã£â€šâ€¹Ã£Æ’Â¢Ã£Æ’Â¼Ã£Æ’â€°
	private Action mode3(double time) {
		if (getUtility(myBestBidByOpponent) > getUtility(mySessionData.lastBid)) {
			return new Offer(getAgentID(), myBestBidByOpponent);
		} else {
			return new Offer(getAgentID(), mySessionData.lastBid);
		}
	}

	// Ã¤Â¸â‚¬Ã¥Ë†â€¡Ã¥Â¦Â¥Ã¥ï¿½â€�Ã£ï¿½â€”Ã£ï¿½ÂªÃ£ï¿½â€ž
	private Action mode(double time) {
		if (time < 0.99) {
			return getBidA();
		} else {
			return getBidC();
		}
	}

	private void beforeOffer(Bid bid) {
		// System.out.println("offer : " + getUtility(bid));
	}

	// Ã¥Â¼Â±SA(Ã¥Ë†Â¶Ã©â„¢ï¿½Ã£ï¿½Å’Ã§Â·Â©Ã£ï¿½â€ž)
	private Action getBid0() {
		Bid bid = utilitySpace.getDomain().getRandomBid(null);
		if (getUtility(bid) > 0.80) {
			// Ã£ï¿½Å¸Ã£ï¿½Â¾Ã£ï¿½Â«Ã£ï¿½â€ Ã£ï¿½Â¾Ã£ï¿½ï¿½Ã£ï¿½â€žÃ£ï¿½ï¿½
			beforeOffer(bid);
			return new Offer(getAgentID(), bid);
		} else {
			return getBidA();
		}
	}

	// Ã¥Â¼Â·SA(Ã¦â„¢Â®Ã©â‚¬Å¡Ã£ï¿½Â®SA
	// Ã¨â€¡ÂªÃ¥Ë†â€ Ã£ï¿½Â«Ã£ï¿½Â¨Ã£ï¿½Â£Ã£ï¿½Â¦Ã©Â«ËœÃ£ï¿½â€žÃ£â€šâ€šÃ£ï¿½Â®Ã£â€šâ€™Ã©ï¿½Â¸Ã¦Å Å¾)
	private Action getBidA() {
		double t0 = 10000;
		double t1 = 0.001;
		Bid baseBid;
		if (myBestBid == null || rand.nextInt(10) != 0) {
			baseBid = utilitySpace.getDomain().getRandomBid(null);
			Bid bid = getSA(baseBid, t0, t1);
			beforeOffer(bid);
			return new Offer(getAgentID(), bid);
		} else {
			return new Offer(getAgentID(), myBestBid);
		}
		// Ã£Æ’Â©Ã£Æ’Â³Ã£Æ’â‚¬Ã£Æ’Â BidÃ£â€šâ€™Ã¥â€¦Æ’Ã£ï¿½Â«SA
	}

	// bidÃ£â€šâ€™SAÃ£ï¿½Â«Ã£â€šË†Ã£â€šÅ Ã¦â€�Â¹Ã¥â€“â€žÃ£ï¿½â„¢Ã£â€šâ€¹
	private Bid getSA(Bid bid, double t0, double t1) {
		int step = 1;
		double def = 0.99;
		int issueSize = bid.getIssues().size();
		double bid_util;

		try {
			do {
				double current_util = utilitySpace.getUtility(bid); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®bidÃ£ï¿½Â®util
				Bid newBid = new Bid(bid); // Ã¥Â¤â€°Ã¦â€ºÂ´Ã£ï¿½â€¢Ã£â€šÅ’Ã£â€šâ€¹Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®bid
				int selectedIssueId = rand.nextInt(issueSize); // issueÃ£ï¿½Â®Ã©ï¿½Â¸Ã¦Å Å¾
				IssueInteger issueInteger = (IssueInteger) bid.getIssues().get(selectedIssueId); // Ã£â‚¬â‚¬Ã©ï¿½Â¸Ã¦Å Å¾Ã£ï¿½â€¢Ã£â€šÅ’Ã£ï¿½Å¸issue
				int issueId = issueInteger.getNumber(); // Ã©ï¿½Â¸Ã¦Å Å¾Ã£ï¿½â€”Ã£ï¿½Å¸issueÃ£ï¿½Â®index
				ValueInteger issueValue = (ValueInteger) bid.getValue(issueId); // issueÃ£ï¿½Â®indexÃ£ï¿½Â®Ã©Æ’Â¨Ã¥Ë†â€ Ã£ï¿½Â®Ã¥â‚¬Â¤
				int issueValueInt = Integer.valueOf(issueValue.toString()).intValue(); // intÃ¥Å¾â€¹Ã£ï¿½Â¸
				int max = issueInteger.getUpperBound(); // Ã©ï¿½Â¸Ã¦Å Å¾Ã£ï¿½â€¢Ã£â€šÅ’Ã£ï¿½Å¸issueÃ£ï¿½Â®Ã£Æ’â€°Ã£Æ’Â¡Ã£â€šÂ¤Ã£Æ’Â³Ã£ï¿½Â®Ã¦Å“â‚¬Ã¥Â¤Â§Ã¥â‚¬Â¤
				int min = issueInteger.getLowerBound(); // Ã©ï¿½Â¸Ã¦Å Å¾Ã£ï¿½â€¢Ã£â€šÅ’Ã£ï¿½Å¸issueÃ£ï¿½Â®Ã£Æ’â€°Ã£Æ’Â¡Ã£â€šÂ¤Ã£Æ’Â³Ã£ï¿½Â®Ã¦Å“â‚¬Ã¥Â°ï¿½Ã¥â‚¬Â¤

				int flag = rand.nextBoolean() ? 1 : -1;

				if (issueValueInt >= min && issueValueInt <= max) {
					if (issueValueInt + step > max) {
						flag = -1;
					} else if (issueValueInt - step < min) {
						flag = 1;
					}
				}

				Value valueInteger = new ValueInteger(issueValueInt + flag * step);
				newBid = newBid.putValue(issueId, valueInteger);

				double newBid_util;
				newBid_util = utilitySpace.getUtility(newBid);
				double bf_cost = 1.0 - current_util;
				double af_cost = 1.0 - newBid_util;

				double p = Math.pow(Math.E, -Math.abs(af_cost - bf_cost) / t0);

				if (af_cost < bf_cost || rand.nextDouble() < p) {
					bid = new Bid(newBid); // bidÃ£â€šâ€™Ã¦â€ºÂ´Ã¦â€“Â°
				}
				t0 *= def;
				bid_util = newBid_util;
			} while (t0 > t1);

			// myBestBidÃ£ï¿½Â®Ã¨Â¨Â­Ã¥Â®Å¡
			// myBestBidÃ£ï¿½Å’Ã£ï¿½ÂªÃ£ï¿½â€žÃ£ï¿½Â¨Ã£ï¿½ï¿½
			if (myBestBid == null) {
				myBestBid = new Bid(bid);
			} else {
				renewBids(bid);
			}

			if (bid_util <= 0.9 && myBestBid != null) {
				bid = new Bid(myBestBid); // 0.9Ã¤Â»Â¥Ã¤Â¸Å Ã£ï¿½Â®Ã¥â‚¬Â¤Ã£ï¿½Å’Ã¥ï¿½â€“Ã£â€šÅ’Ã£ï¿½ÂªÃ£ï¿½â€¹Ã£ï¿½Â£Ã£ï¿½Å¸Ã£ï¿½Â¨Ã£ï¿½ï¿½
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bid;
	}

	// Ã¨Â¿â€˜Ã¥â€šï¿½Ã¦Å½Â¢Ã§Â´Â¢(Ã§â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Â®Ã¥â€¡ÂºÃ¦â€“Â¹)Ã£ï¿½Â«Ã£â€šË†Ã£â€šâ€¹BidÃ£ï¿½Â®Ã¥ï¿½â€“Ã¥Â¾â€”
	private Action getBidB() throws Exception {
		try {
			if (myBestBidByOpponent == null) {
				return getBidA();
			} else {
				Bid bid = new Bid(myBestBidByOpponent);
				System.out.println("before : " + getUtility(bid));
				Bid newBid = new Bid(nearSearch(bid));
				System.out.println("after  :" + getUtility(newBid));
				if (bestBid == null || getUtility(newBid) > getUtility(bestBid)) {
					bestBid = new Bid(newBid);
				}
				// newBidÃ£ï¿½Å’0.8Ã¤Â»Â¥Ã¤Â¸â€¹Ã£ï¿½ÂªÃ£â€šâ€°Ã¦Å½Â¡Ã§â€�Â¨Ã£ï¿½â€”Ã£ï¿½ÂªÃ£ï¿½â€ž
				if (getUtility(newBid) < 0.8) {
					return getBidA();
				} else {
					return new Offer(getAgentID(), newBid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getBidA();
	}

	// baseBidÃ£ï¿½Â®Ã¨Â¿â€˜Ã¥â€šï¿½Ã£â€šâ€™Ã¦Å½Â¢Ã§Â´Â¢Ã£ï¿½â„¢Ã£â€šâ€¹
	private Bid nearSearch(Bid baseBid) throws Exception {

		List<Issue> issues = utilitySpace.getDomain().getIssues(); // Ã¥â€¦Â¨issueÃ£ï¿½Â®Ã¥ï¿½â€“Ã¥Â¾â€”
		Bid nextBid = new Bid(baseBid); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®BidÃ£â€šâ€™Ã£â€šÂ³Ã£Æ’â€�Ã£Æ’Â¼
		int numberIndexes = utilitySpace.getDomain().getIssues().size(); // Ã£â€šÂ¤Ã£â€šÂ·Ã£Æ’Â¥Ã£Æ’Â¼Ã£ï¿½Â®Ã¦â€¢Â°
		for (int i = 0; i < numberIndexes; i++) {
			int index = rand.nextInt(issues.size());
			IssueInteger lIssueInteger = (IssueInteger) issues.get(index); // Ã¦Å’â€¡Ã¥Â®Å¡Ã£ï¿½â€”Ã£ï¿½Å¸indexÃ£ï¿½Â®issueÃ£â€šâ€™Ã¥ï¿½â€“Ã¥Â¾â€”
			int issueNumber = lIssueInteger.getNumber(); // issueÃ£ï¿½Â®index 0
															// Ã£â‚¬Å“ 9

			int optionIndex = 0; // Ã¥Â¤â€°Ã¦â€ºÂ´Ã£ï¿½â„¢Ã£â€šâ€¹ValueÃ¥â‚¬Â¤

			optionIndex = selectIndex(lIssueInteger, baseBid);

			nextBid = nextBid.putValue(issueNumber, new ValueInteger(optionIndex)); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®BidÃ£ï¿½â€¹Ã£â€šâ€°IssueÃ£ï¿½Â®Ã¥â‚¬Â¤Ã£â€šâ€™Ã¥â€¦Â¥Ã£â€šÅ’Ã¦â€ºÂ¿Ã£ï¿½Ë†Ã£â€šâ€¹
		}
		return nextBid;
	}

	private int selectIndex(IssueInteger issue, Bid baseBid) {
		int issueNumber = issue.getNumber();
		int issueIndexMin = issue.getLowerBound(); // issueÃ£ï¿½Â®Ã¤Â¸â€¹Ã©â„¢ï¿½Ã¥â‚¬Â¤
		int issueIndexMax = issue.getUpperBound(); // issueÃ£ï¿½Â®Ã¤Â¸Å Ã©â„¢ï¿½Ã¥â‚¬Â¤
		int candidate = issueIndexMin;
		Bid sBid = new Bid(baseBid);
		for (int i = issueIndexMin; i < issueIndexMax; i++) {
			Bid tempBid = new Bid(baseBid);
			tempBid = tempBid.putValue(issueNumber, new ValueInteger(i));
			if (getUtility(sBid) <= getUtility(tempBid)) {
				candidate = i;
			}
			sBid = sBid.putValue(issueNumber, new ValueInteger(candidate));
		}
		return candidate;
	}

	// Ã¥â€°ï¿½Ã¥â€ºÅ¾Ã¥ï¿½Ë†Ã¦â€žï¿½Ã£ï¿½â€”Ã£ï¿½Å¸Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°Ã£â€šâ€™Ã¦ï¿½ï¿½Ã¦Â¡Ë†
	private Action getBidC() {
		if (mySessionData != null) {
			renewBids(mySessionData.lastBid);
			return new Offer(getAgentID(), mySessionData.lastBid);
		} else {
			return getBidA();
		}
	}

	// Ã¦â€žï¿½Ã¥â€ºÂ³Ã§Å¡â€žÃ£ï¿½Â«Ã£ï¿½Å¡Ã£â€šâ€°Ã£ï¿½â€”Ã£ï¿½Å¸Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’â€°Ã£â€šâ€™Ã¨Â¿â€�Ã£ï¿½â„¢
	private Action getBidD() throws Exception {
		Bid baseBid = utilitySpace.getDomain().getRandomBid(null);
		System.out.println("noisy!!");
		do {
			baseBid = nearSearchWithNoise(baseBid);
		} while (getUtility(baseBid) > 0.55);
		return new Offer(getAgentID(), nearSearchWithNoise(baseBid));
	}

	private Bid nearSearchWithNoise(Bid baseBid) throws Exception {

		List<Issue> issues = utilitySpace.getDomain().getIssues(); // Ã¥â€¦Â¨issueÃ£ï¿½Â®Ã¥ï¿½â€“Ã¥Â¾â€”
		Bid nextBid = new Bid(baseBid); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®BidÃ£â€šâ€™Ã£â€šÂ³Ã£Æ’â€�Ã£Æ’Â¼
		int numberIndexes = utilitySpace.getDomain().getIssues().size(); // Ã£â€šÂ¤Ã£â€šÂ·Ã£Æ’Â¥Ã£Æ’Â¼Ã£ï¿½Â®Ã¦â€¢Â°
		for (int i = 0; i < numberIndexes; i++) {
			int index = rand.nextInt(issues.size());
			IssueInteger lIssueInteger = (IssueInteger) issues.get(index); // Ã¦Å’â€¡Ã¥Â®Å¡Ã£ï¿½â€”Ã£ï¿½Å¸indexÃ£ï¿½Â®issueÃ£â€šâ€™Ã¥ï¿½â€“Ã¥Â¾â€”
			int issueNumber = lIssueInteger.getNumber(); // issueÃ£ï¿½Â®index 0
															// Ã£â‚¬Å“ 9

			int optionIndex = 0; // Ã¥Â¤â€°Ã¦â€ºÂ´Ã£ï¿½â„¢Ã£â€šâ€¹ValueÃ¥â‚¬Â¤

			optionIndex = selectIndexWithNoise(lIssueInteger, baseBid);

			nextBid = nextBid.putValue(issueNumber, new ValueInteger(optionIndex)); // Ã§ï¿½Â¾Ã¥Å“Â¨Ã£ï¿½Â®BidÃ£ï¿½â€¹Ã£â€šâ€°IssueÃ£ï¿½Â®Ã¥â‚¬Â¤Ã£â€šâ€™Ã¥â€¦Â¥Ã£â€šÅ’Ã¦â€ºÂ¿Ã£ï¿½Ë†Ã£â€šâ€¹
		}
		return nextBid;
	}

	private int selectIndexWithNoise(IssueInteger issue, Bid baseBid) {
		int issueNumber = issue.getNumber();
		int issueIndexMin = issue.getLowerBound(); // issueÃ£ï¿½Â®Ã¤Â¸â€¹Ã©â„¢ï¿½Ã¥â‚¬Â¤
		int issueIndexMax = issue.getUpperBound(); // issueÃ£ï¿½Â®Ã¤Â¸Å Ã©â„¢ï¿½Ã¥â‚¬Â¤
		int candidate = issueIndexMin;
		Bid sBid = new Bid(baseBid);
		for (int i = issueIndexMin; i < issueIndexMax; i++) {
			Bid tempBid = new Bid(baseBid);
			tempBid = tempBid.putValue(issueNumber, new ValueInteger(i));
			// Ã¥Â¤Å¡Ã¥Â°â€˜Ã¨â€¡ÂªÃ¥Ë†â€ Ã£ï¿½Â«Ã£ï¿½Â¨Ã£ï¿½Â£Ã£ï¿½Â¦Ã¦Å“â€°Ã¥Ë†Â©Ã£ï¿½Â«Ã£ï¿½â„¢Ã£â€šâ€¹
			if (getUtility(sBid) <= getUtility(tempBid) && rand.nextInt(10) == 0) {
				candidate = i;
			}
			sBid = sBid.putValue(issueNumber, new ValueInteger(candidate));
		}
		return candidate;
	}

	private void renewBids(Bid bid) {
		if (myBestBid == null) {
			myBestBid = new Bid(bid);
		} else if (getUtility(myBestBid) < getUtility(bid)) {
			// System.out.printf("modified %f to %f\n",
			// getUtility(myBestBid), getUtility(bid));
			myBestBid = new Bid(bid);
		}
	}

	@Override
	public String getDescription() {
		return "ANAC 2014 - AgentTRP (compatible with non-linear utility spaces)";
	}
}

class TRPSessionData implements Serializable {
	Bid lastBid; // Ã§â€ºÂ´Ã¥â€°ï¿½Ã£ï¿½Â®Ã¥ï¿½Ë†Ã¦â€žï¿½Ã£ï¿½â€”Ã£ï¿½Å¸Ã£Æ’â€œÃ£Æ’Æ’Ã£Æ’Ë†
	double lastTime; // Ã§â€ºÂ´Ã¥â€°ï¿½Ã£ï¿½Â®Ã¦â„¢â€šÃ©â€“â€œ
	boolean isAgree; // Ã¥Â®Å¸Ã©Å¡â€ºÃ¥ï¿½Ë†Ã¦â€žï¿½Ã£ï¿½â€”Ã£ï¿½Å¸Ã£ï¿½â€¹
	static int breakCount = 0; // Ã¦Â±ÂºÃ¨Â£â€šÃ¥â€ºÅ¾Ã¦â€¢Â°
	Bid myBestBid; // Ã¦Å“â‚¬Ã£â€šâ€šÃ¥Å Â¹Ã§â€�Â¨Ã£ï¿½Â®Ã©Â«ËœÃ£ï¿½â€žÃ£Æ’â€œÃ£Æ’Æ’Ã£Æ’Ë†
	Bid opponentBestBid; // Ã§â€ºÂ¸Ã¦â€°â€¹Ã£ï¿½Å’Ã¦ï¿½ï¿½Ã¦Â¡Ë†Ã£ï¿½â€”Ã£ï¿½Å¸Ã¤Â¸â‚¬Ã§â€¢ÂªÃ¨â€°Â¯Ã£ï¿½â€žÃ£Æ’â€œÃ£Æ’Æ’Ã£Æ’Ë†
	int round;

	public TRPSessionData(Bid lastBid, double lastTime, boolean isAgree, Bid myBestBid, Bid myBestBidByOpponent,
			int round, boolean reset) {
		this.lastBid = lastBid;
		this.lastTime = lastTime;
		this.isAgree = isAgree;

		if (reset) {
			this.breakCount = 0;
		}
		if (!isAgree) {
			this.breakCount++;
		}
		this.myBestBid = myBestBid;
		this.opponentBestBid = myBestBidByOpponent;
		this.round = round;
	}

	public void printAll() {
		// System.out.println("----------------------------------");
		// System.out.println("lastBid :" + lastBid);
		// System.out.println("time :" + lastTime);
		// System.out.println("isAgree :" + isAgree);
		// System.out.println("breakCo :" + breakCount);
		// System.out.println("round :" + round);
	}

}

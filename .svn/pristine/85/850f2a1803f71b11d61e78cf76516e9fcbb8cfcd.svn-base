package agents.anac.y2014.Aster;

import java.io.Serializable;
import java.util.ArrayList;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.NegotiationResult;


public class MyPrevSessionData implements Serializable {
	private static final long serialVersionUID = 1L;

	ArrayList<Bid> agreementBidList;
	BidHistory opponentBidHistory;
	Boolean isAgreement;
	Bid lastBid;
	double opponentPrevMaxUtility;
	double opponentPrevMinUtility;
	double endNegotiationTime;
	boolean immediateDecision;

	public MyPrevSessionData(ArrayList<Bid> agreementBidList, BidHistory opponentBidHistory, NegotiationResult result, double opponentPrevMaxUtility, double opponentPrevMinUtility, double endNegotiationTime, boolean immediateDecision) {
		this.agreementBidList = agreementBidList;
		this.opponentBidHistory = opponentBidHistory;
		this.isAgreement = result.isAgreement();
		this.lastBid = result.getLastBid();
		this.opponentPrevMaxUtility = opponentPrevMaxUtility;
		this.opponentPrevMinUtility = opponentPrevMinUtility;
		this.endNegotiationTime = endNegotiationTime;
		this.immediateDecision = immediateDecision;
	}
}

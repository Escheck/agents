package agents.anac.y2014.BraveCat.OpponentModels.DBOMModel;

import java.util.ArrayList;
import java.util.List;

import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;
import negotiator.utility.AdditiveUtilitySpace;
import agents.anac.y2014.BraveCat.OpponentModels.OpponentModel;
import agents.anac.y2014.BraveCat.necessaryClasses.NegotiationSession;

public class DBOMModel extends OpponentModel {
	// ----------------------------------------------------------------------------------------------------------------------------------------------
	List<Bid> opponentUniqueBidHistory;

	private OpponentUtilitySimilarityBasedEstimator s;

	@Override
	public void init(NegotiationSession negoSession) throws Exception {
		System.out.println("Opponent Model Initializing...");
		this.negotiationSession = negoSession;
		s = new OpponentUtilitySimilarityBasedEstimator(negotiationSession, 100);
		opponentUniqueBidHistory = new ArrayList();
		System.out.println("Opponent Model Initialized Successfully!");
	}

	@Override
	public void updateModel(Bid bid, double time) {
	}

	@Override
	public double getBidEvaluation(BidDetails bid) throws Exception {
		double estimatedBidEvaluation = 0;
		estimatedBidEvaluation = s.GetBidUtility(bid);
		// System.out.println(estimatedBidEvaluation);
		return estimatedBidEvaluation
				* this.negotiationSession.getUtilitySpace().getUtility(
						bid.getBid());
	}

	@Override
	public double getWeight(Issue issue) {
		return ((AdditiveUtilitySpace) this.negotiationSession
				.getUtilitySpace()).getWeight(issue.getNumber());
	}

	@Override
	public String getName() {
		return "DBOMModel";
	}
}
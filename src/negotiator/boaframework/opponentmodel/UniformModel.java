package negotiator.boaframework.opponentmodel;

import java.util.Map;

import negotiator.Bid;
import negotiator.boaframework.NegotiationSession;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.opponentmodel.tools.UtilitySpaceAdapter;
import negotiator.issue.Issue;

/**
 * Simple baseline opponent model which always returns the same preference.
 * 
 * Tim Baarslag, Koen Hindriks, Mark Hendrikx, Alex Dirkzwager and Catholijn M.
 * Jonker. Decoupling Negotiating Agents to Explore the Space of Negotiation
 * Strategies
 * 
 * @author Mark Hendrikx
 */
public class UniformModel extends OpponentModel {

	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		this.negotiationSession = negotiationSession;
		this.opponentUtilitySpace = new UtilitySpaceAdapter(this, negotiationSession.getUtilitySpace().getDomain());
	}

	@Override
	public void updateModel(Bid bid, double time) {
	}

	@Override
	public double getBidEvaluation(Bid bid) {
		return 0.5;
	}

	public double getWeight(Issue issue) {
		return 1.0 / (double) negotiationSession.getIssues().size();
	}

	@Override
	public String getName() {
		return "Uniform Model";
	}
}
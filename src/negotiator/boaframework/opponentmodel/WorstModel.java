package negotiator.boaframework.opponentmodel;

import javax.swing.JOptionPane;

import negotiator.Bid;
import negotiator.boaframework.OpponentModel;
import negotiator.boaframework.opponentmodel.tools.UtilitySpaceAdapter;
import negotiator.protocol.BilateralAtomicNegotiationSession;
import negotiator.tournament.TournamentConfiguration;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * The theoretically worst opponent model. Note that for using this model
 * experimentalSetup should be enabled in global.
 * 
 * Tim Baarslag, Koen Hindriks, Mark Hendrikx, Alex Dirkzwager and Catholijn M.
 * Jonker. Decoupling Negotiating Agents to Explore the Space of Negotiation
 * Strategies
 * 
 * @author Mark Hendrikx
 */
public class WorstModel extends OpponentModel {

	private UtilitySpaceAdapter worstUtilitySpace;

	@Override
	public void setOpponentUtilitySpace(
			BilateralAtomicNegotiationSession session) {

		if (TournamentConfiguration.getBooleanOption(
				"accessPartnerPreferences", false)) {
			opponentUtilitySpace = (AdditiveUtilitySpace) session
					.getAgentAUtilitySpace();
			if (negotiationSession.getUtilitySpace().getFileName()
					.equals(opponentUtilitySpace.getFileName())) {
				opponentUtilitySpace = (AdditiveUtilitySpace) session
						.getAgentBUtilitySpace();
			}
		} else {
			JOptionPane
					.showMessageDialog(
							null,
							"This opponent model needs access to the opponent's\npreferences. See tournament options.",
							"Model error", 0);
			System.err.println("Global.experimentalSetup should be enabled!");
		}
		this.worstUtilitySpace = new UtilitySpaceAdapter(this,
				opponentUtilitySpace.getDomain());
	}

	@Override
	public void setOpponentUtilitySpace(
			AdditiveUtilitySpace opponentUtilitySpace) {
		this.opponentUtilitySpace = opponentUtilitySpace;
		this.worstUtilitySpace = new UtilitySpaceAdapter(this,
				opponentUtilitySpace.getDomain());
	}

	@Override
	public double getBidEvaluation(Bid bid) {
		try {
			return 1.0 - opponentUtilitySpace.getUtility(bid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getName() {
		return "Worst Model";
	}

	public AdditiveUtilitySpace getOpponentUtilitySpace() {
		return worstUtilitySpace;
	}

	public void updateModel(Bid opponentBid, double time) {
	}
}
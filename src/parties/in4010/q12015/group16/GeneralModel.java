package parties.in4010.q12015.group16;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import misc.Range;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Model representing the 'average' of all negotiators during a session. This is
 * done by combining all opponent utility spaces and our own utility space
 * halfway through the negotiation into one general utility space. This
 * combining is done by averaging all issue weights and values in all spaces,
 * and putting the average values in the general utility space.
 */
public class GeneralModel {
	private HashMap<AgentID, Opponent> opponentList;

	private int i = -1;

	private AdditiveUtilitySpace utilSpaceAgent16;
	private AdditiveUtilitySpace utilSpaceGeneral;
	private SortedOutcomeSpace outcomeSpace;

	private ArrayList<AgentID> opponentIDs;
	private ArrayList<Bid> proposals;

	public boolean generated = false;

	/**
	 * @param utilSpace
	 *            utility space of Group16
	 * @param sortedOutcomeSpace
	 *            all possible bids sorted according to utilSpace
	 */
	public GeneralModel(AdditiveUtilitySpace utilSpace,
			SortedOutcomeSpace sortedOutcomeSpace) {
		utilSpaceAgent16 = utilSpace;
		// Create general utility space by copying ours; otherwise we can't do
		// 'new SortedOutcomeSpace(utilSpaceGeneral)' in 'generate()'
		utilSpaceGeneral = new AdditiveUtilitySpace(utilSpaceAgent16);
		outcomeSpace = sortedOutcomeSpace;

		opponentList = new HashMap<AgentID, Opponent>();
		opponentIDs = new ArrayList<AgentID>();
		proposals = new ArrayList<Bid>();
	}

	/**
	 * Retrieve the indicated agent when a bid is received, so that this bid can
	 * be added to the correct opponent's bidList.
	 * 
	 * @param opponentID
	 *            ID of opponent to retrieve
	 * @return opponent indicated with opponentID
	 */
	public Opponent getOpponent(AgentID opponentID) {
		if (opponentList.get(opponentID) != null) {
			return opponentList.get(opponentID);
		} else if (opponentList.get(opponentID) == null) {
			opponentList.put(opponentID, new Opponent(utilSpaceGeneral,
					utilSpaceAgent16));
			opponentIDs.add(opponentID);
			return opponentList.get(opponentID);
		} else {
			return null;
		}
	}

	/**
	 * Generates a table of proposals when the normalized time = 0.5.
	 */
	public void generate() {
		calculateOpponentsValues();
		combineUtilitySpaces();
		getBidsForOwnOutcomeSpace();
		setGenerated(true);
	}

	/**
	 * Calls the value calculation method for all opponents.
	 */
	private void calculateOpponentsValues() {
		for (int i = 0; i < opponentIDs.size(); i++) {
			try {
				opponentList.get(opponentIDs.get(i)).calculateValues();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a single utility space out of all opponent utility spaces by
	 * averaging issue weights and values.
	 */
	private void combineUtilitySpaces() {
		AdditiveUtilitySpace opponentUtilitySpace = null;
		double sumOfWeights = 0;
		double weight = 0;
		for (Issue issue : utilSpaceGeneral.getDomain().getIssues()) {
			for (int i = 0; i < opponentIDs.size(); i++) {
				opponentUtilitySpace = opponentList.get(opponentIDs.get(i))
						.getUtilSpace();

				// Average issue weights
				weight = utilSpaceGeneral.getWeight(issue.getNumber())
						+ opponentUtilitySpace.getWeight(issue.getNumber());
				utilSpaceGeneral.setWeight(issue, weight);
				sumOfWeights += weight;

				// Average values
				EvaluatorDiscrete evOpp = (EvaluatorDiscrete) opponentUtilitySpace
						.getEvaluator(issue.getNumber());
				Set<ValueDiscrete> valueList = evOpp.getValues();
				for (ValueDiscrete val : valueList) {
					// Get evaluation for general model
					EvaluatorDiscrete evGen = (EvaluatorDiscrete) utilSpaceGeneral
							.getEvaluator(issue.getNumber());
					// Sum evaluations and update evaluation for general model
					evGen.addEvaluation(val,
							(evGen.getValue(val) + evOpp.getValue(val)));
				}
			}
		}

		// Divide all issue weights by (#opponents * sum of all issue weights)
		double divisionFactor = opponentIDs.size() * sumOfWeights;
		for (Issue issue : utilSpaceGeneral.getDomain().getIssues()) {
			utilSpaceGeneral.setWeight(issue,
					utilSpaceGeneral.getWeight(issue.getNumber())
							/ divisionFactor);
		}

		// TODO: include utilSpaceGeneral.isComplete()
	}

	/**
	 * Fills the proposals field by sorting halfOfAllBids according to our
	 * utility space.
	 */
	private void getBidsForOwnOutcomeSpace() {
		// Order the table to our utility profile, select all bids above our
		// reservation value
		// and add them to proposals
		List<BidDetails> bids = outcomeSpace.getBidsinRange(new Range(
				utilSpaceAgent16.getReservationValue(), 1));
		Bid bid;
		try {
			for (BidDetails bidDetails : bids) {
				bid = bidDetails.getBid();
				if (utilSpaceGeneral.getUtility(bid) >= 0.5) {
					proposals.add(bid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Increments the index counter used to access the proposals field, and
	 * returns the bid at that index if it exists or null otherwise.
	 * 
	 * @return bid from proposals
	 */
	public Bid getProposal() {
		Bid proposal = null;
		i++;
		if (i < proposals.size()) {
			proposal = proposals.get(i);
		}
		return proposal;
	}

	public AdditiveUtilitySpace getUtilSpaceGeneral() {
		return utilSpaceGeneral;
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
}

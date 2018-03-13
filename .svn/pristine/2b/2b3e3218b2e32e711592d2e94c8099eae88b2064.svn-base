package parties.in4010.q12015.group15;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import negotiator.Bid;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

/**
 * This Opponent Model uses frequency analysis to make preference profiles for
 * all opponents. The issues that change the least often have higher weight. The
 * options that appear the most often have the highest values. The opponent's
 * strategy is evaluated in a way that it's possible to see if he concedes a lot
 * or not.
 * <p>
 * Opponent model uses weights and therefore only supports
 * {@link AdditiveUtilitySpace}.
 */
public class OpponentModel {

	// Preference profile
	private Map<String, AdditiveUtilitySpace> opponentUtilitySpaces = new HashMap<>();
	private Map<String, AdditiveUtilitySpace> lastOpponentUtilitySpaces = new HashMap<>();
	private Map<String, Bid> lastBids = new HashMap<>();
	private AdditiveUtilitySpace emptyOpponentUtilitySpace;
	private double n;

	// Opponent strategy
	private Map<String, Map<BidUtilChange, Double>> utilChanges = new HashMap<>();

	public OpponentModel(AdditiveUtilitySpace utilSpace) {
		System.out.println("INIT OpponentModel");
		this.n = 0.1;

		initOpponentProfiles(utilSpace);
	}

	/**
	 * Set up an empty profile for an opponent. All issues get equal weights and
	 * all options get equal values.
	 * 
	 * @param utilSpace
	 */
	public void initOpponentProfiles(AdditiveUtilitySpace utilSpace) {
		emptyOpponentUtilitySpace = new AdditiveUtilitySpace(utilSpace);
		double totalIssues = emptyOpponentUtilitySpace.getNrOfEvaluators();
		double equalWeight = 1 / totalIssues;
		for (Entry<Objective, Evaluator> eval : emptyOpponentUtilitySpace
				.getEvaluators()) {
			emptyOpponentUtilitySpace.unlock(eval.getKey());
			eval.getValue().setWeight(equalWeight);

			try {
				for (ValueDiscrete vd : ((IssueDiscrete) eval.getKey())
						.getValues()) {
					((EvaluatorDiscrete) eval.getValue()).setEvaluation(vd, 1);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param agent
	 * @param bid
	 * @return utility for agent for bid
	 */
	public double getBidEvaluation(String agent, Bid bid) {
		try {
			return opponentUtilitySpaces.get(agent).getUtility(bid);
		} catch (Exception e) {
			System.out.println("OM Could not get bid evaluation");
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param agent
	 * @return utility for agent for his last offered bid
	 */
	public double getLastBidEvaluation(String agent) {
		try {
			Bid lastBid = lastBids.get(agent);
			return opponentUtilitySpaces.get(agent).getUtility(lastBid);
		} catch (Exception e) {
			System.out.println("OM Could not get last bid evaluation");
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param agentID
	 * @return true if agent is offering his first bid, otherwise false
	 */
	private boolean isFirstBidFromAgent(String agentID) {
		return !opponentUtilitySpaces.containsKey(agentID);
	}

	/**
	 * Adds agent to opponent profiles and util change history
	 * 
	 * @param agent
	 */
	private void addAgent(String agent, Bid opponentBid) {
		System.out.println("First bid for " + agent);
		opponentUtilitySpaces.put(agent, new AdditiveUtilitySpace(
				emptyOpponentUtilitySpace));
		lastBids.put(agent, opponentBid);

		HashMap<BidUtilChange, Double> utilChangeValues = new HashMap<>();
		utilChangeValues.put(BidUtilChange.AVERAGE_UTIL_CHANGE, 0.0);
		utilChangeValues.put(BidUtilChange.TOTAL_UTIL_CHANGE, 0.0);
		utilChanges.put(agent, utilChangeValues);
	}

	/**
	 * Updates opponent model and opponent strategy model for agent regarding to
	 * his latest bid
	 * 
	 * @param agent
	 * @param opponentBid
	 */
	public void updateModel(String agent, Bid opponentBid, int rounds) {
		System.out.println("----- OM -----");
		System.out.println("Updating OM for: " + agent);

		if (isFirstBidFromAgent(agent)) {
			addAgent(agent, opponentBid);
			return;
		}

		System.out.println("Last bid: " + lastBids.get(agent));
		System.out.println("Curr bid: " + opponentBid);

		updateStrategy(agent, opponentBid, rounds);
		updatePreferenceProfile(agent, opponentBid);

	}

	/**
	 * Updates the opponent's preference profile with frequency analysis
	 * 
	 * @param agent
	 * @param opponentBid
	 */
	private void updatePreferenceProfile(String agent, Bid opponentBid) {
		saveLastUtilitySpace(agent);

		for (int i = 1; i <= opponentUtilitySpaces.get(agent)
				.getNrOfEvaluators(); i++) {
			Evaluator eval = opponentUtilitySpaces.get(agent).getEvaluator(i);
			try {
				Value currentVal = opponentBid.getValue(i);
				Value lastVal = lastBids.get(agent).getValue(i);

				// weights
				if (currentVal.equals(lastVal)) {
					double currentWeight = eval.getWeight();
					eval.setWeight(currentWeight + n);
				}

				// values
				ValueDiscrete currentValDiscrete = (ValueDiscrete) currentVal;
				EvaluatorDiscrete evalDiscrete = (EvaluatorDiscrete) eval;
				double valueBefore = evalDiscrete
						.getDoubleValue(currentValDiscrete);
				evalDiscrete.setEvaluationDouble(currentValDiscrete,
						valueBefore + 1.0);
			} catch (Exception e) {
				System.out
						.println("OM Could not update preference profile for "
								+ agent);
				e.printStackTrace();
			}
		}

		normalizeWeights(agent);

		lastBids.put(agent, opponentBid);
	}

	/**
	 * Updates information to model the opponent's strategy. It keeps track of
	 * the average change in the opponent's utility for his offers.
	 * 
	 * @param agent
	 * @param opponentBid
	 * @param rounds
	 */
	private void updateStrategy(String agent, Bid opponentBid, int rounds) {
		double currentBidUtil = getBidEvaluation(agent, opponentBid);
		double lastBidUtil = getLastBidEvaluation(agent);
		double utilDiff = currentBidUtil - lastBidUtil;

		double currentTotalUtilChange = utilChanges.get(agent).get(
				BidUtilChange.TOTAL_UTIL_CHANGE);
		double totalUtilChangeAfter = currentTotalUtilChange + utilDiff;

		utilChanges.get(agent).put(BidUtilChange.TOTAL_UTIL_CHANGE,
				totalUtilChangeAfter);
		utilChanges.get(agent).put(BidUtilChange.AVERAGE_UTIL_CHANGE,
				totalUtilChangeAfter / rounds);
	}

	/**
	 * Normalizes weights for the opponent model for agent.
	 * 
	 * @param agent
	 */
	private void normalizeWeights(String agent) {
		double sum = 0;

		for (Entry<Objective, Evaluator> eval : opponentUtilitySpaces
				.get(agent).getEvaluators()) {
			double weight = eval.getValue().getWeight();
			sum += weight;
		}

		for (Entry<Objective, Evaluator> eval : opponentUtilitySpaces
				.get(agent).getEvaluators()) {
			double weight = eval.getValue().getWeight();
			eval.getValue().setWeight(weight / sum);
		}
	}

	/**
	 * @param agent
	 * @return last bid from agent
	 */
	public Bid getLastBidFromAgent(String agent) {
		return lastBids.get(agent);
	}

	/**
	 * @return all opponents in current negotiation
	 */
	public List<String> getOpponents() {
		List<String> opponents = new ArrayList<>();
		for (String opponent : opponentUtilitySpaces.keySet()) {
			opponents.add(opponent);
		}
		return opponents;
	}

	/**
	 * @param agent
	 * @return utility space for agent
	 */
	public AdditiveUtilitySpace getUtilitySpace(String agent) {
		return opponentUtilitySpaces.get(agent);
	}

	/**
	 * @param agent
	 * @return utility space for agent before the last update
	 */
	public AdditiveUtilitySpace getLastUtilitySpace(String agent) {
		return lastOpponentUtilitySpaces.get(agent);
	}

	/**
	 * Saves the agent's last utility space
	 * 
	 * @param agent
	 */
	private void saveLastUtilitySpace(String agent) {
		lastOpponentUtilitySpaces.put(agent, new AdditiveUtilitySpace(
				getUtilitySpace(agent)));
	}

	/**
	 * @param agent
	 * @return the average of the utility change in the agent's bid
	 */
	public double getAverageBidUtilityChangeForAgent(String agent) {
		return utilChanges.get(agent).get(BidUtilChange.AVERAGE_UTIL_CHANGE);
	}
}

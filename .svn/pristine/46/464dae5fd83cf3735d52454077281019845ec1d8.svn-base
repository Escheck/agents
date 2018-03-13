package agents.ai2014.group12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

/**
 * The Preference class will hold the issues, their weights and for each issue
 * their values and the order of these values. Only compatible with
 * {@link AdditiveUtilitySpace}.
 *
 */
public class Preference {
	ArrayList<PreferenceBlock> preferenceList = new ArrayList<PreferenceBlock>();

	/**
	 * First constructor for the Preference class, this is to create a
	 * Preference profile for the agent that is currently in turn. This
	 * Preference object will not be updated. The Utilityspace is used to fill
	 * the values of each issue and their order.
	 * 
	 * @param utilitySpace
	 *            of the player in turn
	 */
	public Preference(AdditiveUtilitySpace utilitySpace) {
		Domain domain = utilitySpace.getDomain();
		int size = utilitySpace.getEvaluators().size();
		System.out.println("size: " + size);
		List<Objective> objectives = domain.getObjectives();
		for (int i = 1; i <= size; i++) {
			IssueDiscrete issue = (IssueDiscrete) objectives.get(i);
			Double weight = utilitySpace.getWeight(i);
			Evaluator evalor = utilitySpace.getEvaluator(i);
			EvaluatorDiscrete evalorDis = (EvaluatorDiscrete) evalor;
			// Define each issue with every weight attached
			PreferenceBlock prefBlock = new PreferenceBlock(evalorDis,
					issue.getName(), weight);
			prefBlock.normalizeNodeValues();
			Node highest = prefBlock.getHighestPreference();
			highest.setFlag(true);
			preferenceList.add(prefBlock);
		}
		// Possibility to randomize the other values of each issue.
	}

	/**
	 * This constructor is used to create a Preference object for an Agent of
	 * which you have received a bid for the first time. The utilitySpace of the
	 * agent receiving the message will be used for the initial issue, value and
	 * order setup. This will be later updated using the bids of the agent to
	 * which this preference belongs to.
	 * 
	 * @param utilitySpace
	 *            of the agent receiving the message
	 * @param bid
	 */
	public Preference(AdditiveUtilitySpace utilitySpace, Bid bid) {
		Domain domain = utilitySpace.getDomain();
		int size = utilitySpace.getEvaluators().size();
		System.out.println("size: " + size);
		List<Objective> objectives = domain.getObjectives();
		for (int i = 1; i <= size; i++) {
			IssueDiscrete issue = (IssueDiscrete) objectives.get(i);
			double weight = 1.0 / size;
			Evaluator evalor = utilitySpace.getEvaluator(i);
			System.out.println(evalor);
			EvaluatorDiscrete evalorDis = (EvaluatorDiscrete) evalor;
			// Define an issue with all weights the same
			PreferenceBlock prefBlock = new PreferenceBlock(evalorDis,
					issue.getName(), weight);
			prefBlock.normalizeNodeValues();
			Node highest = prefBlock.getHighestPreference();
			highest.setFlag(true);
			preferenceList.add(prefBlock);
		}
		setHighestIssuePreference(bid);
		// Possibility to randomize the other values of each issue.
	}

	/**
	 * Updates the preference of a party given the current bid. It considers the
	 * first bid to be the agents highest utility bid. It will not take into
	 * account the nodes flagged value for this first bid.
	 * 
	 * @param bid
	 */
	public void setHighestIssuePreference(Bid bid) {
		HashMap<Integer, Value> values = bid.getValues();
		for (int i = 1; i <= values.size(); i++) {
			ValueDiscrete value = (ValueDiscrete) values.get(i);
			PreferenceBlock preferBlock = preferenceList.get(i - 1);
			Node currentHighest = preferBlock.getHighestPreference();
			if (!currentHighest.getName().equals(value.getValue())) {
				// preferBlock.orderNodesLowToHigh();
				ArrayList<Node> nodes = preferBlock.nodeList;

				int index = preferBlock.indexOf(value.getValue());

				for (int j = index; j < nodes.size() - 1; j++) {
					nodes.get(j).setName(nodes.get(j + 1).getName());
				}
				nodes.get(nodes.size() - 1).setName(value.getValue());
			}
		}
	}

	/**
	 * This method updates the preference order of an already initialized
	 * preference object This will used the flagged status to determine what
	 * nodes have alreay been set. It will determine what values have been
	 * changed and were not yet flagged and will set those in the appropriate
	 * order and update their flagged status.
	 * 
	 * @param bid
	 */
	public void updatePreferenceOrder(Bid bid) {
		// First update the sequence of the values of an issue
		HashMap<Integer, Value> values = bid.getValues();
		for (int i = 1; i <= values.size(); i++) {
			ValueDiscrete value = (ValueDiscrete) values.get(i);
			PreferenceBlock preferBlock = preferenceList.get(i - 1);
			ArrayList<Node> nodesWithoutFlag = preferBlock
					.getValuesWithoutFlag();
			for (int j = 0; i < nodesWithoutFlag.size(); i++) {
				// If not flagged yet, flag it and set it to the next value
				// after the last flagged
				if (nodesWithoutFlag.get(i).getName().equals(value.getValue())) {
					int highestIndexWithoutFlag = preferBlock
							.getHighestIndexWithoutFlag();

					ArrayList<Node> nodes = preferBlock.nodeList;
					int index = preferBlock.indexOf(value.getValue());

					for (int k = index; k < nodes.size() - 1
							&& !nodes.get(k + 1).getFlag(); k++) {
						nodes.get(k).setName(nodes.get(k + 1).getName());
					}
					nodes.get(highestIndexWithoutFlag)
							.setName(value.getValue());
					nodes.get(highestIndexWithoutFlag).setFlag(true);
				}
			}
		}
	}

	/**
	 * This method updates the issue weights given the previous bid and the new
	 * bid. Each weight of an issue that has been changed will be decreased and
	 * then normalized.
	 * 
	 * @param previousBids
	 * @param newBid
	 */
	public void updateIssueWeights(ArrayList<Bid> previousBids, Bid newBid) {
		Bid previousBid = previousBids.get(previousBids.size() - 1);
		ArrayList<Integer> changedIssues = getChangedIndices(previousBid,
				newBid);

		// Decrease the weights that have been changed
		for (int i = 0; i < changedIssues.size(); i++) {
			preferenceList.get(changedIssues.get(i)).weight -= 0.50 / changedIssues
					.size();
		}

		// Sum the weights
		double sum = 0.0;
		for (int i = 0; i < preferenceList.size(); i++) {
			sum += preferenceList.get(i).weight;
		}
		// Normalize the weights
		for (int i = 0; i < preferenceList.size(); i++) {
			preferenceList.get(i).weight = preferenceList.get(i).weight / sum;
		}
	}

	/**
	 * Method that returns the changed indices by comparing the previous bid and
	 * the new bid
	 * 
	 * @param previousBid
	 * @param newBid
	 * @return list of integers with the changed indices
	 */
	public ArrayList<Integer> getChangedIndices(Bid previousBid, Bid newBid) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		HashMap<Integer, Value> valuesNewBid = newBid.getValues();
		HashMap<Integer, Value> valuesPreviousBid = previousBid.getValues();
		for (int i = 1; i <= valuesNewBid.size(); i++) {
			ValueDiscrete valueNewBid = (ValueDiscrete) valuesNewBid.get(i);
			ValueDiscrete valuePreviousBid = (ValueDiscrete) valuesPreviousBid
					.get(i);
			if (!valueNewBid.getValue().equals(valuePreviousBid.getValue())) {
				// i-1 to get the right index in the preferenceList
				indices.add(i - 1);
			}
		}
		return indices;
	}

	/**
	 * Calculates the utility value for a bid given the current preference
	 * profile. This gives the agent the possibility to calculate other agents
	 * utilities without knowing their exact preference profile.
	 * 
	 * @param bid
	 * @return double
	 */
	public double getUtilityValue(Bid bid) {
		HashMap<Integer, Value> bidvalues = bid.getValues();
		double returnUtility = 0;
		for (PreferenceBlock block : preferenceList) {
			double weight = block.weight;
			for (Map.Entry<Integer, Value> entry : bidvalues.entrySet()) {
				Value value = entry.getValue();
				for (Node node : block.nodeList) {
					if (node.getName() == value.toString()) {
						returnUtility += node.getValue() * weight;
					}
				}
			}
		}
		return returnUtility;
	}
}

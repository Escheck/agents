package parties.in4010.q12015.group16;

import java.util.ArrayList;
import java.util.HashMap;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

public class Opponent {
	private AdditiveUtilitySpace utilSpace, U16;

	// The learning rate of the weights
	private double n = 0.05;

	private ArrayList<Bid> bidList;

	public Opponent(AdditiveUtilitySpace utilSpaceGeneral,
			AdditiveUtilitySpace utilSpaceAgent16) {
		U16 = new AdditiveUtilitySpace(utilSpaceAgent16);
		utilSpace = new AdditiveUtilitySpace(utilSpaceGeneral.getDomain());
		// Initializing all weights as 1/NrOfIssues:
		double initWeight = (double) 1
				/ (utilSpace.getDomain().getObjectives().size() - 1);

		for (int i = 1; i <= utilSpace.getDomain().getIssues().size(); i++) {
			utilSpace.addEvaluator(utilSpace.getDomain().getObjectivesRoot()
					.getObjective(i), U16.getEvaluator(i));
			utilSpace.getEvaluator(i).setWeight(initWeight);
		}

		bidList = new ArrayList<Bid>();
	}

	public void update(Bid bid) {
		bidList.add(bid);
		double error = updateIssueWeights();
		validateIssueWeights(error);
	}

	/**
	 * Applies frequency analysis to update issue weights.
	 * 
	 * @return average deviation of the sum of the new issue weights from
	 *         (1/#issues)
	 */
	private double updateIssueWeights() {
		Value val = null;
		double sumOfWeights = 0;

		// Adding 'n' to the weights of the issues that have changed
		for (int i = 1; i <= utilSpace.getDomain().getIssues().size(); i++) {
			try {
				val = bidList.get(bidList.size() - 2).getValue(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (bidList.get(bidList.size() - 1).getValues().containsValue(val)) {
				utilSpace.getEvaluator(i).setWeight(
						utilSpace.getEvaluator(i).getWeight() + n);
			}
			sumOfWeights += utilSpace.getWeight(i);
		}
		double error = (sumOfWeights - 1)
				/ utilSpace.getDomain().getIssues().size();
		return error;
	}

	/**
	 * Validates the issue weights acquired from frequency analysis and keeps
	 * them inside the [0, 1] range.
	 * 
	 * @param error
	 *            average deviation of the sum of the new issue weights from
	 *            (1/#issues)
	 */
	private void validateIssueWeights(double error) {
		double checkSum;
		do {
			checkSum = 0;
			// Adjusting all weights (and checking impossible values <0 or >1)
			for (int i = 1; i <= utilSpace.getDomain().getIssues().size(); i++) {
				utilSpace.getEvaluator(i).setWeight(
						utilSpace.getEvaluator(i).getWeight() - error);
				// To prevent weights from getting below 0 or above 1:
				if (utilSpace.getEvaluator(i).getWeight() <= 0) {
					utilSpace.getEvaluator(i).setWeight(0);
				}
				if (utilSpace.getEvaluator(i).getWeight() > 1) {
					utilSpace.getEvaluator(i).setWeight(1);
				}
				checkSum += utilSpace.getWeight(i);
			}
			error = (checkSum - 1) / utilSpace.getDomain().getIssues().size();
		} while (0.9999 > checkSum || checkSum > 1.0001);
	}

	/**
	 * Calculates the values for all issues at the halfway time of the
	 * negotiation.
	 * 
	 * @throws Exception
	 */
	public void calculateValues() throws Exception {
		for (Issue issue : utilSpace.getDomain().getIssues()) {
			HashMap<Value, Integer> counter = new HashMap<Value, Integer>();
			ArrayList<Value> values = new ArrayList<Value>();
			Value val = null;
			// Count how many times each value occurs per issue
			for (int i = 0; i < bidList.size(); i++) {
				val = bidList.get(i).getValue(issue.getNumber());
				if (!counter.containsKey(val)) {
					counter.put(val, 1);
					values.add(val);
				} else if (counter.containsKey(val)) {
					counter.put(val, counter.get(val) + 1);
				}
			}

			// Add the evaluators with their values to the utility space
			EvaluatorDiscrete ev = (EvaluatorDiscrete) U16.getEvaluator(issue
					.getNumber());
			for (Value value : values) {
				ev.setEvaluation(value, counter.get(value));
			}
			// Add the updated evaluator to the utility space
			utilSpace.addEvaluator(issue, ev);
		}
	}

	public AdditiveUtilitySpace getUtilSpace() {
		return utilSpace;
	}

	public ArrayList<Bid> getBidHistory() {
		return bidList;
	}
}

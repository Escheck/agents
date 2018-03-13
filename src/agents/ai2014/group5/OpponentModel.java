package agents.ai2014.group5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import negotiator.Bid;

/**
 * An opponent model constructs a model of an opponent's negotiation profile so
 * that the opponent's utilities of bids can be estimated. These models make use
 * of a reinforcement learning method to learn the opponents' profiles from the
 * bids made by the opponents. This method is described in the paper for the
 * agent HardHeaded from the ANAC 2011 competition.
 */
class OpponentModel {
	// Weight increment during learning
	private static final double EPSILON = 0.1;

	// The last bid offered by the opponent
	private Bid lastBid;

	// Issue weights
	private List<Double> weights;

	// Issue values
	private List<List<Integer>> values;

	// Map of issue value names to indexes for each issue
	private List<Map<String, Integer>> valueNames;

	// Issue indexes-1 and their names
	private List<String> issueNames;

	private Group5 agent;

	@SuppressWarnings("unchecked")
	public OpponentModel(List<String> issueNames,
			List<Map<String, Integer>> valueNames, Group5 agent) {
		lastBid = null;
		this.valueNames = valueNames;
		this.issueNames = issueNames;
		this.agent = agent;

		// Uniformly distribute the weights
		weights = new ArrayList<Double>(Collections.nCopies(issueNames.size(),
				1.0 / issueNames.size()));

		// Initialize issue values
		values = new ArrayList<List<Integer>>();
		for (int i = 0; i < issueNames.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>(
					Collections.nCopies(valueNames.get(i).size(), 1));
			values.add((List<Integer>) tmp.clone());
		}
	}

	/**
	 * Updates the model given the new bid received from the opponent. If an
	 * issue value has changed since the last bid then the weight for the
	 * corresponding issue and the issue value will be changed.
	 */
	public void updateModel(Bid bid) {
		if (bid == null) {
			// The bid does not exists, the action was therefore not an offer
			return;
		}

		if (lastBid != null) {
			// This is not the first bid, so update the model
			for (int i = 0; i < issueNames.size(); i++) {
				String prevV = null, newV = null;
				try {
					prevV = lastBid.getValue(i + 1).toString();
					newV = bid.getValue(i + 1).toString();
				} catch (Exception e) {
					agent.println("Error in \"updateModel\": getValue(" + i + 1
							+ ") fails for bid " + bid + " or bid " + lastBid);
				}

				if (prevV != null && newV != null && prevV.equals(newV)) {
					// Update weight and issue value for this issue
					int vi = valueNames.get(i).get(newV);
					weights.set(i, weights.get(i) + EPSILON);
					values.get(i).set(vi, values.get(i).get(vi) + 1);
				}
			}

			// Normalize weights
			double norm = 0.0;
			for (double w : weights) {
				norm += w;
			}
			for (int i = 0; i < weights.size(); i++) {
				weights.set(i, weights.get(i) / norm);
			}
		}

		lastBid = bid;
	}

	/**
	 * Calculates the expected utility of a bid.
	 * 
	 * @param Bid
	 *            to calculate utility of
	 * @return Utility of bid for the opponent of this model
	 */
	public double expectedUtilityOf(Bid bid) {
		double u = 0.0;
		for (int i = 0; i < issueNames.size(); i++) {
			String tmp = null;
			try {
				// Get the name of the issue value used in the bid
				tmp = bid.getValue(i + 1).toString();
			} catch (Exception e) {
				agent.println("Error in \"expectedUtiliyOf\": getValue(" + i
						+ 1 + ") fails for bid " + bid);
			}
			if (tmp != null) {
				// Calculate and normalize estimated chosen issue value
				int eIndex = valueNames.get(i).get(tmp);
				int eNorm = 0;
				for (int v = 0; v < values.get(i).size(); v++) {
					eNorm += values.get(i).get(v);
				}
				double e = ((double) values.get(i).get(eIndex)) / eNorm;

				// Increment utility for this issue
				u += weights.get(i) * e;
			}
		}
		return u;
	}
}
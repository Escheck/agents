package agents;

import negotiator.utility.AdditiveUtilitySpace;
import agents.bayesianopponentmodel.BayesianOpponentModelScalable;

public class BayesianAgentNS extends BayesianAgent {

	@Override
	public String getName() {
		return "Bayesian Scalable";
	}

	@Override
	protected void prepareOpponentModel() {
		fOpponentModel = new BayesianOpponentModelScalable(
				(AdditiveUtilitySpace) utilitySpace);
	}

}

package parties.in4010.q12015.group10;

import negotiator.timeline.TimeLineInfo;

public class OpponentModelEstimator {

	public static void updateAllModels(Opponent[] opponents,
			TimeLineInfo timeline) {

		double weightIncrement = 0.1;
		// for every opponent: update the model
		for (int opponentNumber = 0; opponentNumber < opponents.length; opponentNumber++) {

			int totalNumberOfValues = opponents[opponentNumber].getListSize();
			int numberOfIssues = opponents[opponentNumber]
					.getEstimatedUtilitySpace().getNrOfEvaluators();
			double[] weights = new double[numberOfIssues];
			double[] valueValue = new double[totalNumberOfValues];

			// define new weight:
			// weight = previous weight + (increment if value has not changed)
			// Normalize weights
			double sumWeight = 0;
			for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
				weights[issueIndex] = opponents[opponentNumber].getEvaluator(
						issueIndex).getWeight();
				if (opponents[opponentNumber].IssueUnChanged(issueIndex)) {
					weights[issueIndex] = weights[issueIndex] + weightIncrement;
				}
				sumWeight = sumWeight + weights[issueIndex];
			}
			// normalize weights
			for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
				weights[issueIndex] = weights[issueIndex] / sumWeight;
				opponents[opponentNumber].setWeightAtIndex(issueIndex,
						weights[issueIndex]);
			}

			// Value "values" are calculated as follows:
			// valueValue = (value frequency )/ (max frequency of this issue)
			// or do the same as with the weights: if value is chosen, do plus
			// 0.1, and divide by the maximum
			int parIssueIndex;
			for (int listIndex = 0; listIndex < totalNumberOfValues; listIndex++) {
				parIssueIndex = opponents[opponentNumber]
						.getIssueIndexFromListIndex(listIndex);
				valueValue[listIndex] = opponents[opponentNumber]
						.getValueFreq(listIndex);
				if (opponents[opponentNumber].getIssueMaxFreq(parIssueIndex) == 0) {
					valueValue[listIndex] = 1;
				} else {
					valueValue[listIndex] = valueValue[listIndex]
							/ opponents[opponentNumber]
									.getIssueMaxFreq(parIssueIndex);
				}
				opponents[opponentNumber].setValueValueAtIndex(parIssueIndex,
						listIndex, valueValue[listIndex]);
				opponents[opponentNumber].setValueValueListAtIndex(listIndex,
						valueValue[listIndex]);
			}
		}

	}

}
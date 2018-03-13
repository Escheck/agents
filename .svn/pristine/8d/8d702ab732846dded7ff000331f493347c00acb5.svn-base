package agents.anac.y2016.agenthp2;

import java.util.HashMap;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.EvaluatorInteger;

public class module_BidGenerate {

	private Bid maximumEvaluationBid = null; // 評価が最大のBid格納用

	/**
	 * コンストラクタ
	 * 
	 * @param utilitySpace
	 *            自分の効用関数
	 */
	public module_BidGenerate(AbstractUtilitySpace utilitySpace) {

		// 評価が最大のBid作成
		HashMap<Integer, Value> template = new HashMap<Integer, Value>();
		for (Issue tmp : utilitySpace.getDomain().getIssues()) {

			// 論点のEvaluator取得
			int issue_num = tmp.getNumber(); // 論点番号
			Evaluator evaluator = ((AdditiveUtilitySpace) utilitySpace)
					.getEvaluator(issue_num);

			switch (tmp.getType()) {

			// 論点が離散値の場合
			case DISCRETE:
				EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) evaluator;
				template.put(issue_num, evaluatorDiscrete.getMaxValue());
				break;

			// 論点が連続値の場合
			case INTEGER:
				EvaluatorInteger evaluatorInteger = (EvaluatorInteger) evaluator;
				double upperValueEvaluation = evaluatorInteger
						.getUtilHighestValue();
				double lowerValueEvaluation = evaluatorInteger
						.getUtilLowestValue();
				int maxInt = -1;
				if (upperValueEvaluation > lowerValueEvaluation) {
					maxInt = evaluatorInteger.getUpperBound();
				} else {
					maxInt = evaluatorInteger.getLowerBound();
				}
				ValueInteger maxValue = new ValueInteger(maxInt);
				template.put(issue_num, (Value) maxValue);
				break;

			case OBJECTIVE:
			case REAL:
			case UNKNOWN:
			default:
				break;
			}
		}
		maximumEvaluationBid = new Bid(utilitySpace.getDomain(), template);
	}

	/**
	 * 自分の効用が最大になるBidを返す
	 * 
	 * @return maximumEvaluationBid 自分の効用が最大のBid
	 */
	public Bid getMaximumEvaluationBid() {

		return maximumEvaluationBid;
	}
}

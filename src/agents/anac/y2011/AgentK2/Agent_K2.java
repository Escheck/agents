package agents.anac.y2011.AgentK2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import negotiator.Agent;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.ActionWithBid;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;

public class Agent_K2 extends Agent {

	private Action partner = null;
	private HashMap<Bid, Double> offeredBidMap;
	private double target;
	private double bidTarget;
	private double sum;
	private double sum2;
	private int rounds;
	private double tremor;
	private final boolean TEST_EQUIVALENCE = false;
	private Random random100;
	private Random random200;
	private Random random300;
	private Random random400;

	public void init() {
		offeredBidMap = new HashMap<Bid, Double>();
		target = 1.0;
		bidTarget = 1.0;
		sum = 0.0;
		sum2 = 0.0;
		rounds = 0;
		tremor = 2.0;

		if (TEST_EQUIVALENCE) {
			random100 = new Random(100);
			random200 = new Random(200);
			random300 = new Random(300);
			random400 = new Random(400);
		} else {
			random100 = new Random();
			random200 = new Random();
			random300 = new Random();
			random400 = new Random();
		}
	}

	@Override
	public String getName() {
		return "Agent_K2";
	}

	@Override
	public String getVersion() {
		return "0.31415_discountFix";
	}

	public void ReceiveMessage(Action opponentAction) {
		partner = opponentAction;
	}

	public Action chooseAction() {
		Action action = null;
		try {
			if (partner == null) {
				action = selectBid();
			}
			if (partner instanceof Offer) {
				Bid offeredBid = ((Offer) partner).getBid();

				double p = acceptProbability(offeredBid);

				if (p > random100.nextDouble()) {
					action = new Accept(getAgentID(), offeredBid);
				} else {
					action = selectBid();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			action = new Accept(getAgentID(), ((ActionWithBid) partner).getBid());
		}
		return action;
	}

	private Action selectBid() {
		Bid nextBid = null;

		ArrayList<Bid> bidTemp = new ArrayList<Bid>();

		for (Bid bid : offeredBidMap.keySet()) {
			if (offeredBidMap.get(bid) > target) {
				bidTemp.add(bid);
			}
		}

		int size = bidTemp.size();
		if (size > 0) {
			int sindex = (int) Math.floor(random200.nextDouble() * size);
			nextBid = bidTemp.get(sindex);
		} else {
			double searchUtil = 0.0;
			try {
				int loop = 0;
				while (searchUtil < bidTarget) {
					if (loop > 500) {
						bidTarget -= 0.01;
						loop = 0;
					}
					nextBid = searchBid();
					searchUtil = utilitySpace.getUtility(nextBid);
					loop++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (nextBid == null) {
			return (new Accept(getAgentID(), ((ActionWithBid) partner).getBid()));
		}
		return (new Offer(getAgentID(), nextBid));
	}

	private Bid searchBid() throws Exception {
		HashMap<Integer, Value> values = new HashMap<Integer, Value>();
		List<Issue> issues = utilitySpace.getDomain().getIssues();

		Bid bid = null;

		for (Issue lIssue : issues) {
			switch (lIssue.getType()) {
			case DISCRETE:
				IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
				int optionIndex = random300.nextInt(lIssueDiscrete.getNumberOfValues());
				values.put(lIssue.getNumber(), lIssueDiscrete.getValue(optionIndex));
				break;
			case REAL:
				IssueReal lIssueReal = (IssueReal) lIssue;
				int optionInd = random300.nextInt(lIssueReal.getNumberOfDiscretizationSteps() - 1);
				values.put(lIssueReal.getNumber(),
						new ValueReal(lIssueReal.getLowerBound()
								+ (lIssueReal.getUpperBound() - lIssueReal.getLowerBound()) * (double) (optionInd)
										/ (double) (lIssueReal.getNumberOfDiscretizationSteps())));
				break;
			case INTEGER:
				IssueInteger lIssueInteger = (IssueInteger) lIssue;
				int optionIndex2 = lIssueInteger.getLowerBound()
						+ random300.nextInt(lIssueInteger.getUpperBound() - lIssueInteger.getLowerBound());
				values.put(lIssueInteger.getNumber(), new ValueInteger(optionIndex2));
				break;
			default:
				throw new Exception("issue type " + lIssue.getType() + " not supported by SimpleAgent2");
			}
		}

		bid = new Bid(utilitySpace.getDomain(), values);
		return bid;
	}

	double acceptProbability(Bid offeredBid) throws Exception {

		double offeredUtility = utilitySpace.getUtility(offeredBid);
		offeredBidMap.put(offeredBid, offeredUtility);

		sum += offeredUtility;
		sum2 += offeredUtility * offeredUtility;
		rounds++;

		double mean = sum / rounds;

		double variance = (sum2 / rounds) - (mean * mean);

		double deviation = Math.sqrt(variance * 12);
		if (Double.isNaN(deviation)) {
			deviation = 0.0;
		}

		double time = timeline.getTime();

		double t = time * time * time;

		if (offeredUtility < 0 || offeredUtility > 1.05) {
			throw new Exception("utility " + offeredUtility + " outside [0,1]");
		}

		if (t < 0 || t > 1) {
			throw new Exception("time " + t + " outside [0,1]");
		}

		if (offeredUtility > 1.) {
			offeredUtility = 1;
		}

		double estimateMax = mean + ((1 - mean) * deviation);

		double alpha = 1 + tremor + (10 * mean) - (2 * tremor * mean);
		double beta = alpha + (random400.nextDouble() * tremor) - (tremor / 2);

		double preTarget = 1 - (Math.pow(time, alpha) * (1 - estimateMax));
		double preTarget2 = 1 - (Math.pow(time, beta) * (1 - estimateMax));

		double ratio = (deviation + 0.1) / (1 - preTarget);
		if (Double.isNaN(ratio) || ratio > 2.0) {
			ratio = 2.0;
		}

		double ratio2 = (deviation + 0.1) / (1 - preTarget2);
		if (Double.isNaN(ratio2) || ratio2 > 2.0) {
			ratio2 = 2.0;
		}

		target = ratio * preTarget + 1 - ratio;
		bidTarget = ratio2 * preTarget2 + 1 - ratio2;

		double m = t * (-300) + 400;
		if (target > estimateMax) {
			double r = target - estimateMax;
			double f = 1 / (r * r);
			if (f > m || Double.isNaN(f))
				f = m;
			double app = r * f / m;
			target = target - app;
		} else {
			target = estimateMax;
		}

		if (bidTarget > estimateMax) {
			double r = bidTarget - estimateMax;
			double f = 1 / (r * r);
			if (f > m || Double.isNaN(f))
				f = m;
			double app = r * f / m;
			bidTarget = bidTarget - app;
		} else {
			bidTarget = estimateMax;
		}

		// test code for Discount Factor
		double discount_utility = utilitySpace.getUtilityWithDiscount(offeredBid, time);

		double discount_ratio = discount_utility / offeredUtility;
		if (!Double.isNaN(discount_utility) && !Double.isNaN(discount_ratio)) {
			target *= discount_ratio;
			bidTarget *= discount_ratio;
		}

		double utilityEvaluation = offeredUtility - estimateMax;
		double satisfy = offeredUtility - target;

		double p = (Math.pow(time, alpha) / 5) + utilityEvaluation + satisfy;
		if (p < 0.1) {
			p = 0.0;
		}

		return p;
	}

	@Override
	public String getDescription() {
		return "ANAC 2011 - AgentK2 (compatible with non-linear utility spaces)";
	}
}

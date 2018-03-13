package agents.ai2014.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import negotiator.Bid;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;

/**
 * This uses getEvaluators and hence only supports {@link AdditiveUtilitySpace}.
 *
 */
public class BidGenerator {

	private Group4 agent;

	// Unused bids
	private HashMap<Bid, Double> bidMap = new HashMap<Bid, Double>();

	private HashMap<Bid, Double> alreadyUsedBids = new HashMap<Bid, Double>();
	private ArrayList<Party> parties = new ArrayList<Party>();
	private int turnsLeft;

	// Based on chance, so they have to sum up to 1.00
	private Double divisionUs = 0.75;
	private Double divisionOther = 1 - divisionUs;

	private Double chosenIssuePercentage;

	private Random random = new Random();

	public BidGenerator(Group4 agent, HashMap<Bid, Double> map, int deadline) {
		this.agent = agent;
		bidMap = map;
		turnsLeft = deadline;
	}

	/**
	 * Returns the best issue value of the other parties.
	 * 
	 * @param issueNr
	 *            The issue that will be checked.
	 * @return ValueDiscrete The best value.
	 */
	private ValueDiscrete getTheirBestValue(int issueNr) {
		HashMap<String, Double> valueWeights = new HashMap<String, Double>();

		// Zero-based index
		issueNr = issueNr - 1;

		// sum up all the values of all the parties
		for (int i = 0; i < parties.size(); i++) {
			for (Entry<String, Double> e : parties.get(i).getIssueModels()
					.get(issueNr).getUtility().entrySet()) {
				Double issWeight = parties.get(i).getIssueModels().get(issueNr)
						.getValue();
				if (valueWeights.get(e.getKey()) == null) {
					valueWeights.put(e.getKey(), e.getValue() * issWeight);
				} else {
					valueWeights.put(e.getKey(), valueWeights.get(e.getKey())
							+ (e.getValue() * issWeight));
				}
			}
		}

		// take out best value
		String bestValueKey = null;
		Double bestValueWeight = 0.0;
		ValueDiscrete previousValue = null;

		try {
			// Because bid values use one-based indices, +1
			previousValue = (ValueDiscrete) agent.getLastGivenBid().getValue(
					issueNr + 1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for (Entry<String, Double> e : valueWeights.entrySet()) {
			if (e.getValue() > bestValueWeight) {
				if (!previousValue.getValue().equals(e.getKey())) {
					bestValueWeight = e.getValue();
					bestValueKey = e.getKey();
				}
			}
		}

		// get the ValueDiscrete -----i use the first party since everyone has
		// same discrete issues
		ValueDiscrete value = null;
		int size = parties.get(0).getIssueModels().get(issueNr).getValues()
				.size();
		for (int i = 0; i < size; i++) {

			Party randomParty = parties.get(0);
			IssueModel issueModel = randomParty.getIssueModels().get(issueNr);

			if (issueModel.getValues().get(i).getValue().equals(bestValueKey)) {
				value = issueModel.getValues().get(i);
			}
		}

		return value;
	}

	/**
	 * Returns the index of the issue picked and updates the
	 * chosenIssuePercentage with the percentage.
	 * 
	 * @return IssueNr The chosen issue to be changed in the next bid (one-based
	 *         index).
	 */
	private int getWeightedRandomIssueIndex() {
		HashMap<String, Double> issueWeightsOfOtherParties = new HashMap<String, Double>();

		// gets the sum of all weights of all parties for the issues
		for (Party p : parties) {
			for (IssueModel issueModel : p.getIssueModels()) {
				String key = issueModel.getName();
				Double weight;
				if (!issueWeightsOfOtherParties.containsKey(key)) {
					weight = issueModel.getValue();
					issueWeightsOfOtherParties.put(key, weight);
				} else {
					weight = issueWeightsOfOtherParties.get(key)
							+ issueModel.getValue();
					issueWeightsOfOtherParties.put(key, weight);
				}
			}
		}

		// get total weight to normalize it
		Double totalWeight = 0.0;
		for (Entry<String, Double> e : issueWeightsOfOtherParties.entrySet()) {
			totalWeight += e.getValue();
		}
		// generate other parties weights
		ArrayList<Double> otherWeights = new ArrayList<Double>();
		for (Entry<String, Double> e : issueWeightsOfOtherParties.entrySet()) {
			Double temp = e.getValue();
			e.setValue(temp / totalWeight);
			otherWeights.add(temp / totalWeight);
		}
		// make other party weights the inverse
		// otherWeights = getInverseValues(otherWeights);
		// NO! We want to change the issues that they *DO* care about!

		// get my weights
		ArrayList<Double> myWeights = new ArrayList<Double>();
		for (Entry<Objective, Evaluator> e : ((AdditiveUtilitySpace) agent
				.getUtilitySpace()).getEvaluators()) {
			myWeights.add(e.getValue().getWeight());
		}
		// make my party weights the inverse
		myWeights = getInverseValues(myWeights);

		// get the array of overall weights
		ArrayList<Double> finalWeights = new ArrayList<Double>();
		for (int i = 0; i < myWeights.size(); i++) {
			Double mine = myWeights.get(i) * divisionUs;
			Double other = otherWeights.get(i) * divisionOther;
			finalWeights.add(mine + other);
		}
		// pick the Issue by throwing the dice
		Double diceThrow = random.nextDouble();
		Double tempWeight = 0.0;
		Integer issueNr = null;
		for (int i = 0; i < finalWeights.size(); i++) {
			tempWeight += finalWeights.get(i);
			issueNr = i;
			chosenIssuePercentage = finalWeights.get(i);
			if (tempWeight >= diceThrow) {
				break;
			}
		}

		return (int) issueNr + 1;
	}

	private ArrayList<Double> getInverseValues(ArrayList<Double> values) {
		ArrayList<Double> returnValues = new ArrayList<Double>();
		Double sumOfDivision = 0.0;
		for (int i = 0; i < values.size(); i++) {
			sumOfDivision += 1 / values.get(i);
		}
		for (int i = 0; i < values.size(); i++) {
			returnValues.add((1 / values.get(i)) / sumOfDivision);
		}
		return returnValues;
	}

	/**
	 * Generate the best overall bid, even those already offered.
	 * 
	 * @return Bid The generated bid.
	 */
	public Bid generateBestOverallBid() {
		Bid bestBid = null;

		if (alreadyUsedBids.size() == 0) {
			bestBid = getBestUtilityBid(bidMap);
		} else {
			bestBid = getBestUtilityBid(alreadyUsedBids);
		}

		return bestBid;
	}

	/**
	 * Gets the best value for an issue, with all other issues set.
	 * 
	 * @param condition
	 *            The pre-set issues.
	 * @param issueNr
	 *            The id of the issue to be changed.
	 * @return ValueDiscrete The best value.
	 */
	public ValueDiscrete getValueBidOneOut(HashMap<Integer, Value> condition,
			int issueNr) {
		ValueDiscrete bestValue = null;
		double maxUtility = 0.0;
		AdditiveUtilitySpace us = (AdditiveUtilitySpace) agent
				.getUtilitySpace();
		ValueDiscrete previousValue = null;

		try {
			previousValue = (ValueDiscrete) agent.getLastGivenBid().getValue(
					issueNr);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Zero-based index
		IssueDiscrete issue = (IssueDiscrete) us.getDomain().getIssues()
				.get(issueNr - 1);

		for (int j = 0; j < issue.getNumberOfValues(); j++) {
			if (!previousValue.getValue().equals(issue.getValue(j).getValue())) {
				HashMap<Integer, Value> values = (HashMap<Integer, Value>) condition
						.clone();
				values.put(issueNr, issue.getValue(j));

				try {
					Bid currentBid = new Bid(us.getDomain(), values);

					if (maxUtility < agent.getUtility(currentBid)) {
						maxUtility = agent.getUtility(currentBid);
						bestValue = issue.getValue(j);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bestValue;
	}

	/**
	 * Gets the bid with the highest utility
	 * 
	 * @param bids
	 *            The list of bids with their utility to choose from.
	 * @return Bid The bid with the highest utility.
	 */
	private Bid getBestUtilityBid(HashMap<Bid, Double> bids) {
		Double bestUtility = 0.0;
		Bid bestBid = null;

		for (Entry<Bid, Double> e : bids.entrySet()) {
			if (e.getValue() > bestUtility) {
				bestUtility = e.getValue();
				bestBid = e.getKey();
			}
		}

		// updates in already used(if not already used)
		if (!(bids.equals(alreadyUsedBids))) {
			alreadyUsedBids.put(bestBid, bestUtility);
			bidMap.remove(bestBid);
		}
		return bestBid;

	}

	/**
	 * Generates the best bid based on the weights of both us and the parties
	 * 
	 * @return Bid The best bid with the new issue value.
	 */
	public Bid generateBestBid() {

		if (parties.size() < agent.getNumberOfParties()) {
			parties.clear();
			for (Entry<String, Party> e : agent.getParties().entrySet()) {
				parties.add(e.getValue());
			}
		}

		int issueNr = getWeightedRandomIssueIndex();
		Bid finalBid = null;
		double randomD = random.nextDouble();
		Bid lastGivenBid = agent.getLastGivenBid();
		HashMap<Integer, Value> lastValues = (HashMap<Integer, Value>) lastGivenBid
				.getValues().clone();
		ValueDiscrete value = null;

		// They win by chance
		if (chosenIssuePercentage >= randomD) {
			value = getTheirBestValue(issueNr);
			lastValues.put(issueNr, value);
			try {
				finalBid = new Bid(agent.getUtilitySpace().getDomain(),
						lastValues);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// We win by chance
		else {
			try {
				HashMap<Integer, Value> condition = (HashMap<Integer, Value>) lastValues
						.clone();
				condition.remove(issueNr);
				value = getValueBidOneOut(condition, issueNr);
				condition.put(issueNr, value);
				finalBid = new Bid(agent.getUtilitySpace().getDomain(),
						condition);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return finalBid;
	}

}

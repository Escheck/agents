package agents.ai2014.group12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.Bid;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;

public class BidGenerator {
	static ArrayList<Bid> bidCombinations;
	static AbstractUtilitySpace utilitySpace;

	/**
	 * This function returns the best bid that an agent can make with regards to
	 * other agents.
	 * 
	 * @param ut
	 * @param preference
	 * @param acceptingValue
	 * @param otherAgentsPreference
	 * @return
	 * @throws Exception
	 */

	public static Bid generateBid(AbstractUtilitySpace ut,
			Preference preference, double acceptingValue,
			HashMap<String, Preference> otherAgentsPreference) throws Exception {
		System.out.println("Begin combinations");
		utilitySpace = ut;
		bidCombinations = new ArrayList<Bid>();

		if (acceptingValue == 0.9) {
			return ut.getMaxUtilityBid();
		}

		cartesianProductW(preference);

		ArrayList<Bid> filteredBidCombinations = filterCombos(acceptingValue,
				0.1);

		Bid returnBid = new Bid(ut.getDomain());
		returnBid = getBestBid(filteredBidCombinations, ut, preference,
				otherAgentsPreference);

		return returnBid;
	}

	/**
	 * A wrapper function for the cartesianProduct of a agents preferences. Will
	 * make a List<List<Node>> data structured that will be used as input for
	 * cartesianProduct
	 * 
	 * @param preference
	 * @throws Exception
	 */

	private static void cartesianProductW(Preference preference)
			throws Exception {
		List<List<Node>> blocks = new ArrayList<List<Node>>();

		for (int i = 0; i < preference.preferenceList.size(); i++) {
			PreferenceBlock block = preference.preferenceList.get(i);
			List<Node> nodes = new ArrayList<Node>();
			for (int j = 0; j < block.nodeList.size(); j++) {
				nodes.add(block.nodeList.get(j));
			}
			blocks.add(nodes);
		}

		List<List<Node>> resultTest = cartesianProduct(blocks);
		for (int i = 0; i < resultTest.size(); i++) {
			List<Node> list = resultTest.get(i);
			HashMap<Integer, Value> hsmp = new HashMap<Integer, Value>();
			Bid bid = new Bid(utilitySpace.getDomain());
			for (int j = 0; j < list.size(); j++) {
				Node node = list.get(j);
				ValueDiscrete val = new ValueDiscrete(node.getName());
				Value value = (Value) val;
				hsmp.put(j + 1, value);
				bid = new Bid(utilitySpace.getDomain(), hsmp);
			}
			bidCombinations.add(bid);
		}
	}

	/**
	 * Will return a cartesianProduct list of the input list
	 * 
	 * @param lists
	 * @return
	 */
	protected static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
		List<List<T>> resultLists = new ArrayList<List<T>>();
		if (lists.size() == 0) {
			resultLists.add(new ArrayList<T>());
			return resultLists;
		} else {
			List<T> firstList = lists.get(0);
			List<List<T>> remainingLists = cartesianProduct(lists.subList(1,
					lists.size()));
			for (T condition : firstList) {
				for (List<T> remainingList : remainingLists) {
					ArrayList<T> resultList = new ArrayList<T>();
					resultList.add(condition);
					resultList.addAll(remainingList);
					resultLists.add(resultList);
				}
			}
		}
		return resultLists;
	}

	/**
	 * Returns the best bid of the bid list, will calculate all the values of
	 * the bids and takes the best average bid for everybody and returns that
	 * Bid.
	 * 
	 * @param bids
	 * @param ut
	 * @param preference
	 * @param otherAgentsPreference
	 * @return
	 * @throws Exception
	 */
	private static Bid getBestBid(ArrayList<Bid> bids, AbstractUtilitySpace ut,
			Preference preference,
			HashMap<String, Preference> otherAgentsPreference) throws Exception {
		ArrayList<ArrayList<Double>> list = new ArrayList<ArrayList<Double>>();
		for (Bid bid : bids) {
			ArrayList<Double> bidValues = new ArrayList<Double>();
			for (Map.Entry<String, Preference> entry : otherAgentsPreference
					.entrySet()) {
				Preference p = (Preference) entry.getValue();
				double bidValueP = p.getUtilityValue(bid);
				bidValues.add(bidValueP);
			}
		}

		ArrayList<Double> averageValueOfBid = new ArrayList<Double>();
		for (ArrayList<Double> listOfABid : list) {
			double averageWeight = 0;
			for (double bidListValues : listOfABid) {
				averageWeight += bidListValues;
			}
			averageValueOfBid.add(averageWeight);
		}

		int returnIndex = 0;
		double biggest = 0;
		for (int i = 0; i < averageValueOfBid.size(); i++) {
			if (averageValueOfBid.get(i) > biggest) {
				biggest = averageValueOfBid.get(i);
				returnIndex = i;
			}
		}
		return bids.get(returnIndex);
	}

	/**
	 * Returns an filtered list of the bidcombinations. The filter is an range
	 * from the accepting value to a range percentage above the accepting value.
	 * 
	 * @param acceptingValue
	 * @param range
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Bid> filterCombos(double acceptingValue,
			double range) throws Exception {
		ArrayList<Bid> goodBidCombos = new ArrayList<Bid>();

		double minValue = acceptingValue;
		double maxValue = acceptingValue + (acceptingValue * range);

		for (int i = 0; i < bidCombinations.size(); i++) {
			Bid bid = bidCombinations.get(i);
			double value = utilitySpace.getUtility(bid);
			if (minValue < value && maxValue > value) {
				goodBidCombos.add(bid);

			}
		}

		if (goodBidCombos.size() != 0) {
			return goodBidCombos;
		} else
			return filterCombos(acceptingValue, range * 1.5);

	}
}

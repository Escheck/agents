package agents.ai2014.group10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;

public class OpponentBidLists {

	private ArrayList<Object> senders;
	private HashMap<Object, ArrayList<Bid>> map;
	private boolean single_list;
	private AdditiveUtilitySpace utilitySpace;

	public OpponentBidLists(AdditiveUtilitySpace utilitySpace,
			boolean single_list) {
		senders = new ArrayList<Object>();
		map = new HashMap<Object, ArrayList<Bid>>();
		this.single_list = single_list;
		this.utilitySpace = utilitySpace;
		if (single_list) {
			ArrayList<Bid> bid_list = new ArrayList<Bid>();
			map.put(this, bid_list);
		}
	}

	private boolean hasSender(Object sender) {
		return (senders.indexOf(sender) != -1);
	}

	private void addSender(Object sender) {
		if (!single_list) {
			ArrayList<Bid> bid_list = new ArrayList<Bid>();
			map.put(sender, bid_list);
		}
		senders.add(sender);
	}

	public ArrayList<Object> getSenders() {
		return senders;
	}

	public void insertBid(Object sender, Bid bid) {
		if (!hasSender(sender)) {
			addSender(sender);
		}
		ArrayList<Bid> bid_list;
		if (single_list) {
			bid_list = map.get(this);
		} else {
			bid_list = map.get(sender);
		}
		bid_list.add(bid);
	}

	public ArrayList<Bid> getBids(Object sender) {
		if (single_list) {
			return map.get(this);
		} else {
			return map.get(sender);
		}
	}

	public ArrayList<Entry<Pair<Integer, Value>, Integer>> getMostFrequentIssueValues(
			Object sender) {
		ArrayList<Bid> bid_list = getBids(sender);
		HashMap<Pair<Integer, Value>, Integer> value_count = new HashMap<Pair<Integer, Value>, Integer>();
		for (int i = 0; i < bid_list.size(); i++) {
			Bid bid = bid_list.get(i);
			List<Issue> issues = bid.getIssues();
			HashMap<Integer, Value> values = bid.getValues();

			for (int j = 0; j < values.size(); j++) {
				Integer issue_id = issues.get(j).getNumber();
				Value value_id = values.get(j + 1);
				Pair<Integer, Value> pair = new Pair<Integer, Value>(issue_id,
						value_id);
				if (value_count.containsKey(pair)) {
					int counter = value_count.get(pair);
					value_count.put(pair, ++counter);
				} else {
					value_count.put(pair, 1);
				}
			}
		}
		ArrayList<Entry<Pair<Integer, Value>, Integer>> sorted_list = new ArrayList<Entry<Pair<Integer, Value>, Integer>>(
				((Map<Pair<Integer, Value>, Integer>) value_count).entrySet());
		Collections.sort(sorted_list,
				new Comparator<Entry<Pair<Integer, Value>, Integer>>() {
					public int compare(Entry<Pair<Integer, Value>, Integer> o1,
							Entry<Pair<Integer, Value>, Integer> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});
		return sorted_list;
	}

	public ArrayList<Entry<Pair<Integer, Value>, Double>> weightIssueValues(
			ArrayList<Entry<Pair<Integer, Value>, Integer>> sorted_list) {
		HashMap<Pair<Integer, Value>, Double> map = new HashMap<Pair<Integer, Value>, Double>();
		for (Entry<Pair<Integer, Value>, Integer> entry : sorted_list) {
			double value = (double) entry.getValue();
			int issue_id = entry.getKey().getInteger();
			Evaluator evaluator = utilitySpace.getEvaluator(issue_id);
			double weight = evaluator.getWeight();
			double evaluation;
			try {
				Bid temp = utilitySpace.getMaxUtilityBid();
				temp = temp.putValue(issue_id, entry.getKey().getValue());
				evaluation = evaluator.getEvaluation(utilitySpace, temp,
						issue_id);
			} catch (Exception e) {
				evaluation = 1.0;
			}
			map.put(entry.getKey(), value * weight * evaluation);
		}
		ArrayList<Entry<Pair<Integer, Value>, Double>> weighted_list = new ArrayList<Entry<Pair<Integer, Value>, Double>>(
				((Map<Pair<Integer, Value>, Double>) map).entrySet());
		Collections.sort(weighted_list,
				new Comparator<Entry<Pair<Integer, Value>, Double>>() {
					public int compare(Entry<Pair<Integer, Value>, Double> o1,
							Entry<Pair<Integer, Value>, Double> o2) {
						return o1.getValue().compareTo(o2.getValue());
					}
				});
		return weighted_list;
	}
}
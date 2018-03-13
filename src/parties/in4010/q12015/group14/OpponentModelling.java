package parties.in4010.q12015.group14;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
//import negotiator.utility.UtilitySpace;
// the class deploys frequency analysis to estimate the preference profile of an agent
// a class to get opponent modelling for an agent

public class OpponentModelling {

	// private String agent; //the agent this opponent modelling will be applied
	// to
	private Bid lastBid = null; // save the last Bid made by the agent
	private List<Issue> domainIssues = new ArrayList<Issue>();
	private ArrayList<IssueDiscrete> domainIssuesDiscrete = new ArrayList<IssueDiscrete>();
	// private ArrayList<Integer> noOfvaluesPerIssue=new ArrayList<Integer>();
	private ArrayList<Double> estWeights = new ArrayList<Double>(); // the
																	// weights
																	// estimation
																	// for the
																	// preference
																	// profile
																	// of this
																	// agent
	private double n = 0.1; // n double to update the weight values at each
							// offer
	private double p = 5;
	private ArrayList<HashMap<String, ArrayList<Double>>> estValues = new ArrayList<HashMap<String, ArrayList<Double>>>();
	private AgentID opponentID;

	// arraylist of issues each of one having a hashmap to contain all possible
	// values and an arraylist to contain the estimated value
	// and the frequency this value appeared so far

	// initialization
	public OpponentModelling(String senderAgent, List<Issue> domIss, Bid b,
			AgentID opponentID) {
		this.opponentID = opponentID;
		domainIssues = domIss;
		for (Issue iss : domainIssues) {
			IssueDiscrete temp = (IssueDiscrete) iss;
			domainIssuesDiscrete.add(temp);
			// noOfvaluesPerIssue.add(temp.getNumberOfValues()); //the number of
			// values per issue stored in this arraylist
		}
		// lastBid=b; //save the first ever bid of the opponent agent
		initWeights();
		// create domainIssueDiscrete arraylist
		initValues();
		updateModel(b);
		// agent=senderAgent;
	}

	// you get the value of every issue given the bid of the opponent and update
	// its model
	public void updateModel(Bid oppBid) {
		double sum = 0; // sum of the weights
		for (int j = 0; j < domainIssuesDiscrete.size(); j++) {
			try {
				String val = oppBid.getValue(j + 1).toString(); // YOU NEED J+1
																// ISSUE INDECES
																// START FROM 1
				if (lastBid == null) { // we must update the model with the
										// first bid given
					estValues.get(j).get(val).set(0, 1.0);
					estValues.get(j).get(val).set(1, 1.0);
				} else { // if this is not the first bid
					double prevFreq = estValues.get(j).get(val).get(1);
					estValues.get(j).get(val).set(1, prevFreq + 1); // update
																	// the total
																	// count/appearance
																	// of this
																	// specific
																	// value
					updateValuesInIssue(j);
					// update weights here
					if (oppBid.getValue(j + 1).equals(lastBid.getValue(j + 1))) {
						estWeights.set(j, estWeights.get(j) + n);
					}
				}
			} catch (Exception e) {
				System.out
						.println("Exception thrown in updateModel, while trying to get the value of an issue from a Bid");
			}
			sum = sum + estWeights.get(j);
		}
		for (int index = 0; index < estWeights.size(); index++) {
			double temp = estWeights.get(index);
			estWeights.set(index, temp / sum);
		}
		lastBid = oppBid;
	}

	// for a mentioned issue, update all the values ( =freq/maxFreq )
	private void updateValuesInIssue(int issueIndex) {
		// first get the max
		double max = 0;
		for (Map.Entry<String, ArrayList<Double>> entry : estValues.get(
				issueIndex).entrySet()) {
			if (entry.getValue().get(1) > max) {
				max = entry.getValue().get(1);
			}
		}

		// now update values
		for (Map.Entry<String, ArrayList<Double>> entry : estValues.get(
				issueIndex).entrySet()) {
			double temp = Math.pow((entry.getValue().get(1) / max), (1 / p));
			entry.getValue().set(0, temp);
		}
	}

	// initialize the weights of the opponent modeling ( =1/nummberOfIssues )
	private void initWeights() {
		double noIssues = domainIssues.size();
		double initWeightValues = 1.0 / (double) noIssues;
		for (int i = 0; i < noIssues; i++) {
			estWeights.add(initWeightValues); // the initial weights are added
												// by the order of the issues
			estValues.add(new HashMap<String, ArrayList<Double>>()); // initialize
																		// the
																		// arraylist
																		// of
																		// hashmaps
		}
	}

	// initialize the values of the opponent modeling
	private void initValues() {
		int index = 0;
		for (IssueDiscrete isDis : domainIssuesDiscrete) {
			for (int i = 0; i < isDis.getNumberOfValues(); i++) {
				String valueName = isDis.getStringValue(i);
				ArrayList<Double> temp1 = new ArrayList<Double>();
				temp1.add(0.0); // estimation of value
				temp1.add(0.0); // frequency of the value
				estValues.get(index).put(valueName, temp1);
			}
			index++;
		}
	}

	// get the utility of the bid using this opponent modeling
	public double getOppUtil(Bid b) {
		double util = 0;
		for (int j = 0; j < domainIssuesDiscrete.size(); j++) {
			try {
				String val = b.getValue(j + 1).toString();
				util = util + estWeights.get(j)
						* estValues.get(j).get(val).get(0);
			} catch (Exception e) {
				System.out
						.println("Exception thrown in getOpputil, while trying to get the value of an issue from a Bid");
			}
		}
		return util;
	}

	// get the bid with the highest utility from the ArrayList (method similar
	// to maxOppUtilIndex)
	public Offer maxOppUtilBid(ArrayList<Bid> bidList) {
		try {
			double maxUtil = getOppUtil(bidList.get(0));
			int maxIndex = 0;
			for (int i = 1; i < bidList.size(); i++) {
				if (getOppUtil(bidList.get(i)) > maxUtil) {
					maxUtil = getOppUtil(bidList.get(i));
					maxIndex = i;
				}
			}
			return new Offer(opponentID, bidList.get(maxIndex));
		} catch (Exception e) {
			System.err
					.println("Error generating maximum utility using maxOppUtilBid!");
			e.printStackTrace();
			return null;
		}
	}

	// get the index with the highest utility from the ArrayList (method similar
	// to maxOppUtilBid)
	public int maxOppUtilIndex(ArrayList<Bid> bidList) {
		try {
			double maxUtil = getOppUtil(bidList.get(0));
			int maxIndex = 0;
			for (int i = 1; i < bidList.size(); i++) {
				if (getOppUtil(bidList.get(i)) > maxUtil) {
					maxUtil = getOppUtil(bidList.get(i));
					maxIndex = i;
				}
			}
			return maxIndex;
		} catch (Exception e) {
			System.err
					.println("Error generating maximum utility using maxOppUtilBid!");
			e.printStackTrace();
			return 0;
		}
	}

	// print the estimated weights and values of this opponent model
	public void printOppModel() {
		System.out.println("The number of issues are: "
				+ domainIssuesDiscrete.size());
		printWeights();
		for (int i = 0; i < domainIssuesDiscrete.size(); i++) {
			System.out.println("Values for issue #" + i + ":");
			printMap(estValues.get(i));
		}
	}

	// print the values (contents of the hashmap)
	public void printMap(HashMap<String, ArrayList<Double>> hm) {
		for (Map.Entry<String, ArrayList<Double>> entry : hm.entrySet()) {
			System.out.println("Preference: " + entry.getKey());
			System.out.println("Value: " + entry.getValue().get(0)
					+ "  -  Frequency: " + entry.getValue().get(1));
		}
	}

	// print the issue weights
	public void printWeights() {
		for (int index = 0; index < estWeights.size(); index++) {
			System.out.println("The estimated weight of issue #" + index
					+ " is :" + estWeights.get(index));
		}
	}

}

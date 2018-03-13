package parties.in4010.q12015.group10;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.Deadline;
import negotiator.bidding.BidDetails;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.xml.SimpleElement;

//import negotiator.Bid;

public class Opponent {

	// estimated utilities

	private AgentID agentID; // Actual name (could be anything, like
								// "DoubleOSeven")
	private int myPartyNumber; // Our numeric representation of the party
								// (instead of the string that could be
								// anything, like "DoubleOSeven"))

	private BidHistory myMadeBidHistory = new BidHistory(); // Offers that I
															// made, along with
															// the time at which
															// I made them, and
															// the utility for
															// Group 10.
	private BidHistory myAcceptedBidHistory = new BidHistory();// Offers that I
																// accepted,
																// along with
																// the time at
																// which I
																// accepted
																// them, and the
																// utility for
																// Group 10.

	private AdditiveUtilitySpace myEstUtilitySpace;

	private boolean hasMadeFirstBid = false;

	private int numberOfIssues;
	private int[] numberOfValuesPerIssue;

	private int listSize;

	private Deadline myDeadline;

	// create a bidhistory in which you can select the nth bid
	private ArrayList<BidDetails> customBidHistory = new ArrayList<BidDetails>();

	// All these arrays are based on the same list index:

	// Ind. Iss. Val freq valueUnchanged
	// 0 Issue 0 value0 1 false
	// 1 value1 10 true
	// 2 value2 1 false
	// 3 Issue 1 value0 6 etc
	// 4 value1 6
	// 5 Issue 2 value0 3
	// 6 value1 3
	// 7 value2 0
	// 8 value3 3
	// 9 value4 3
	// 10 Issue 3 value0 etc
	// 11 value1

	private Value[] valueList;
	private double[] valueValueList;
	private int[] valueIndexList;
	private int[] issueIndexList;
	private double[] freqList;
	private boolean[] valueUnchangedList;

	private EvaluatorDiscrete[] myEvaluatorDiscrete;

	private int bidGetValueIndexOffset = 1;

	// constructor
	Opponent(int givenPartyNumber, AdditiveUtilitySpace myUtilitySpace,
			Deadline givenDeadline) {
		myDeadline = givenDeadline;
		myPartyNumber = givenPartyNumber;
		myEstUtilitySpace = new AdditiveUtilitySpace(myUtilitySpace);

		numberOfIssues = myEstUtilitySpace.getNrOfEvaluators();
		numberOfValuesPerIssue = new int[numberOfIssues];

		// Extract the total value space
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			numberOfValuesPerIssue[issueIndex] = ((IssueDiscrete) myEstUtilitySpace
					.getDomain().getIssues().get(issueIndex))
					.getNumberOfValues();
			listSize = listSize + numberOfValuesPerIssue[issueIndex];
		}

		// Define the table
		valueList = new Value[listSize]; // a list of all values, for all issues
		valueValueList = new double[listSize]; // a list of all valueValues
		valueIndexList = new int[listSize]; // a list of all value indexes
											// within the issue
		issueIndexList = new int[listSize]; // a list of all issue indexes
											// corresponding to the value
		freqList = new double[listSize]; // a list of the number of occurance
											// (number of times) a value has
											// been included in a bid
		valueUnchangedList = new boolean[listSize]; // a list containing whether
													// a value has remained the
													// same
		// initialize the table
		int listIndex = 0;
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			for (int valueIndex = 0; valueIndex < numberOfValuesPerIssue[issueIndex]; valueIndex++) {
				valueList[listIndex] = ((IssueDiscrete) myEstUtilitySpace
						.getDomain().getIssues().get(issueIndex))
						.getValue(valueIndex);
				valueValueList[listIndex] = 1;
				valueIndexList[listIndex] = valueIndex;
				issueIndexList[listIndex] = issueIndex;
				freqList[listIndex] = 0;
				valueUnchangedList[listIndex] = false;

				listIndex++;
			}
		}
		// construct the evaluator functions

		myEvaluatorDiscrete = new EvaluatorDiscrete[myEstUtilitySpace
				.getNrOfEvaluators()];

		for (int issueIndex = 0; issueIndex < myEstUtilitySpace
				.getNrOfEvaluators(); issueIndex++) {
			myEvaluatorDiscrete[issueIndex] = new EvaluatorDiscrete();
			for (int valueIndex = 0; valueIndex < ((IssueDiscrete) myEstUtilitySpace
					.getDomain().getIssues().get(issueIndex))
					.getNumberOfValues(); valueIndex++) {
				myEvaluatorDiscrete[issueIndex].addEvaluation(
						((IssueDiscrete) myEstUtilitySpace.getDomain()
								.getIssues().get(issueIndex))
								.getValue(valueIndex), 1);
			}
			myEvaluatorDiscrete[issueIndex]
					.setWeight(1 / ((double) myEstUtilitySpace
							.getNrOfEvaluators()));
			myEstUtilitySpace.addEvaluator((IssueDiscrete) myEstUtilitySpace
					.getDomain().getIssues().get(issueIndex),
					myEvaluatorDiscrete[issueIndex]);
		}
	}

	// Define methods for Opponents class

	void setAgentID(AgentID givenAgentID) {
		agentID = givenAgentID;
	}

	void StoreOfferedBid(BidDetails madeBidDetails) {
		// check whether this is the first Bid
		Bid previousBid;
		if (hasMadeFirstBid) {
			previousBid = myMadeBidHistory.getLastBid();
			// reset the unchanged list
			for (int listIndex = 0; listIndex < listSize; listIndex++) {
				valueUnchangedList[listIndex] = false;
			}
		} else {
			previousBid = null;
		}

		myMadeBidHistory.add(madeBidDetails);
		customBidHistory.add(madeBidDetails);

		// update the frequency and unchanged list
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			try {
				for (int listIndex = 0; listIndex < listSize; listIndex++) {
					// When reading the values in a bid, the index starts at
					// ONE!!!
					if (myMadeBidHistory.getLastBid()
							.getValue(issueIndex + bidGetValueIndexOffset)
							.equals(valueList[listIndex])) {
						// updating consecutive list, unless this is the first
						// bid.
						if (hasMadeFirstBid) {
							if (previousBid.getValue(
									issueIndex + bidGetValueIndexOffset)
									.equals(valueList[listIndex])) {
								valueUnchangedList[listIndex] = true;
							}
						}
						// updating the frequency list
						double freqScale = 1;
						double freqStep = 1;
						if (valueUnchangedList[listIndex]) {
							for (int listIndex2 = 0; listIndex2 < listSize; listIndex2++) {
								if (issueIndexList[listIndex2] == issueIndexList[listIndex]) {
									freqList[listIndex2] = freqList[listIndex2]
											+ freqStep;
								}
							}

						} else {
							freqScale = 2;
						}
						freqList[listIndex] = freqList[listIndex] + freqStep
								* freqScale;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println("throw/catch triggered, in class(opponent) when reading the value of a bid");
				System.out.println("thrown issueIndex" + issueIndex);

			}
		}
		if (!hasMadeFirstBid) {
			hasMadeFirstBid = true;
		}

	}

	void setValueValueAtIndex(int issueIndex, int listIndex, double evaluationIn) {
		try {
			myEvaluatorDiscrete[issueIndex].setEvaluationDouble(
					(ValueDiscrete) valueList[listIndex], evaluationIn);
			myEstUtilitySpace.addEvaluator((IssueDiscrete) myEstUtilitySpace
					.getDomain().getIssues().get(issueIndex),
					myEvaluatorDiscrete[issueIndex]);
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("catch throw triggered: in Class Opponent.setValueValue");
		}
	}

	void setValueValueListAtIndex(int listIndex, double valueValueIn) {
		valueValueList[listIndex] = valueValueIn;
	}

	void setWeightAtIndex(int issueIndex, double weightIn) {
		myEvaluatorDiscrete[issueIndex].setWeight(weightIn);
		myEstUtilitySpace.addEvaluator((IssueDiscrete) myEstUtilitySpace
				.getDomain().getIssues().get(issueIndex),
				myEvaluatorDiscrete[issueIndex]);
	}

	void StoreAcceptedBid(BidDetails acceptedBidDetails) {
		myAcceptedBidHistory.add(acceptedBidDetails);
	}

	AdditiveUtilitySpace getEstimatedUtilitySpace() {
		return myEstUtilitySpace;
	}

	IssueDiscrete getIssue(int issueIndex) {
		return ((IssueDiscrete) myEstUtilitySpace.getDomain().getIssues()
				.get(issueIndex));
	}

	boolean IssueUnChanged(int issueIndex) {
		boolean issueConsecutive = false;
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			if ((issueIndex == issueIndexList[listIndex])
					& (valueUnchangedList[listIndex])) {
				issueConsecutive = true;
			}
		}
		return issueConsecutive;
	}

	double getValueFreq(int listIndexIn) {
		return freqList[listIndexIn];
	}

	double getIssueMaxFreq(int issueIndex) {
		double maxIssueFreq = 0;
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			if (issueIndex == issueIndexList[listIndex]) {
				if (maxIssueFreq <= freqList[listIndex]) {
					maxIssueFreq = freqList[listIndex];
				}
			}
		}
		return maxIssueFreq;
	}

	int getIssueIndexFromListIndex(int listIndexIn) {
		// returns the issue index given the list index
		return issueIndexList[listIndexIn];
	}

	int getValueIndexFromListIndex(int listIndexIn) {
		// returns the value index given the list index
		return valueIndexList[listIndexIn];
	}

	ValueDiscrete getValueInList(int ListIndexIn) {
		// returns the value given the list
		return (ValueDiscrete) valueList[ListIndexIn];
	}

	double getValueValueInList(int ListIndexIn) {
		// returns the value given the list
		return valueValueList[ListIndexIn];
	}

	double getMaxValueValuePerIssue(int issueIndex) {
		double maxIssueValueValue = 0;
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			if (issueIndex == issueIndexList[listIndex]) {
				if (maxIssueValueValue <= valueValueList[listIndex]) {
					maxIssueValueValue = valueValueList[listIndex];
				}
			}
		}
		return maxIssueValueValue;
	}

	int getListSize() {
		return listSize;
	}

	EvaluatorDiscrete getEvaluator(int issueIndex) {
		return myEvaluatorDiscrete[issueIndex];
	}

	int getHistorySize() {
		return myMadeBidHistory.size();
	}

	void printTable() {
		System.out.println(" ");
		System.out
				.println("-------------------------------------------------------");
		System.out.println("Printing Opponent Data Table: opp nr : "
				+ myPartyNumber);
		System.out
				.println("-------------------------------------------------------");
		System.out.println("LastBid: " + myMadeBidHistory.getLastBid());

		System.out.println("----- begin table ---------");
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			System.out.println(valueList[listIndex] + "-- issue nr: "
					+ issueIndexList[listIndex] + " -- value nr: "
					+ valueIndexList[listIndex] + " -- freq: "
					+ freqList[listIndex] + " -- unchanged: "
					+ valueUnchangedList[listIndex]);
		}
		System.out.println("----- end table ---------");
		System.out.println("----- Evaluator functions --------------");
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			System.out.println("Evaluator function Nr " + issueIndex
					+ " -- weight: "
					+ myEvaluatorDiscrete[issueIndex].getWeight()
					+ " -- descr: " + myEvaluatorDiscrete[issueIndex]);
		}
		System.out.println("----- End evaluator functions ---------");
		System.out
				.println("-------------------------------------------------------");
		System.out.println("End openent data listing");
		System.out
				.println("-------------------------------------------------------");
		System.out.println(" ");
	}

	void printValueValueDifference() {
		double[] realValueValueList = { 1, 0.66, 0.66, 0.33, 0.33, 1, 0.66,
				0.33, 0.5, 0.25, 1, 0.25, 0.25, 0.25, 1, 0.5, 0.66, 1, 0.33,
				0.66, 1, 0.33, 0.33 };
		double[] realweight = { 0.19, 0.28, 0.19, 0.05, 0.19, 0.1 };

		// error calculation
		double errorValuesSquare = 0;
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			if (valueValueList[listIndex] > 0.01) {
				errorValuesSquare = errorValuesSquare
						+ (valueValueList[listIndex] - realValueValueList[listIndex])
						* (valueValueList[listIndex] - realValueValueList[listIndex]);
			}
		}
		double errorIssuesSquare = 0;
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			errorIssuesSquare = errorIssuesSquare
					+ (myEvaluatorDiscrete[issueIndex].getWeight() - realweight[issueIndex])
					* (myEvaluatorDiscrete[issueIndex].getWeight() - realweight[issueIndex]);
		}

		System.out.println(" ");
		System.out
				.println("-------------------------------------------------------");
		System.out.println("Printing Opponent ValueValues: opp nr : "
				+ myPartyNumber);
		System.out
				.println("-------------------------------------------------------");

		System.out
				.println("----- begin modeling results --------------------------");
		for (int listIndex = 0; listIndex < listSize; listIndex++) {
			System.out.println(valueList[listIndex] + " -- issue nr: "
					+ issueIndexList[listIndex] + " -- valueValue: "
					+ valueValueList[listIndex] + " -- realValue: "
					+ realValueValueList[listIndex]);
		}
		System.out
				.println("-------------------------------------------------------");
		System.out.println("values errorSquare : " + errorValuesSquare);
		System.out
				.println("-------------------------------------------------------");
		System.out
				.println("----- weights -----------------------------------------");
		for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
			System.out.println("Issue Nr " + issueIndex + " -- weight: "
					+ myEvaluatorDiscrete[issueIndex].getWeight()
					+ " -- realweight: " + realweight[issueIndex]);
		}
		System.out
				.println("-------------------------------------------------------");
		System.out.println("weights errorSquare : " + errorIssuesSquare);
		System.out
				.println("-------------------------------------------------------");
		System.out.println("End openent modeling results");
		System.out
				.println("-------------------------------------------------------");
		System.out.println(" ");
	}

	public Bid getBidfromHistory(int bidNumber) {
		Bid bid = customBidHistory.get(bidNumber).getBid();
		return bid;
	}

	public void ExportEstimateUtilSpace(String utilSpaceFileNameWithoutExtension) {
		String fileName = utilSpaceFileNameWithoutExtension + ".txt";
		String fileLocation = "exportedUtilsEstimates";
		PrintWriter writer;
		try {

			writer = new PrintWriter(new FileWriter(fileLocation + "/"
					+ fileName));

			writer.println(Integer.toString(numberOfIssues));
			// weights
			for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
				writer.println(String.format("%1.4f",
						myEvaluatorDiscrete[issueIndex].getWeight()));
			}
			// numberOfValuesPerIssue[]
			for (int issueIndex = 0; issueIndex < numberOfIssues; issueIndex++) {
				writer.println(Integer
						.toString(numberOfValuesPerIssue[issueIndex]));
			}
			// valueValues
			for (int listIndex = 0; listIndex < listSize; listIndex++) {
				writer.println(String
						.format("%1.4f", valueValueList[listIndex]));
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void ExportProfile(AdditiveUtilitySpace utilitySpaceToExport,
			String profileFileNameWithoutExtension) {

		// This function saves the specified utilityspace under the given name.
		// ACTUAL time and date and file
		// extension (.xml) are added automatically. For example, this:
		// ExportProfile(utilSpace, "someName");
		// results in for example: "someName-20151020-163758.xml"

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		Date date = new Date();
		String dateStr = dateFormat.format(date);

		String profileFolder = "exportedProfiles";

		// String profileFileName = profileFolder + "/" +
		// profileFileNameWithoutExtension + "-" + dateStr + ".xml";
		String profileFileName = profileFolder + "/"
				+ profileFileNameWithoutExtension + ".xml";

		try {
			SimpleElement profileElement = utilitySpaceToExport.toXML();
			profileElement.saveToFile(profileFileName);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erorr exporting profile");
		}

	}

}

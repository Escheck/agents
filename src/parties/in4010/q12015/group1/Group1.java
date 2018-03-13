package parties.in4010.q12015.group1;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import misc.Range;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.parties.AbstractNegotiationParty;

public class Group1 extends AbstractNegotiationParty {
	private double MINIMUM_BID_UTILITY;
	private Bid opponentLastBid;
	public Action action = null;
	Bid bid = null;
	BidHistory bidHistory;
	SortedOutcomeSpace sortedOutcomeSpace;
	List<BidDetails> bidDetailsList;
	HashMap<Integer, Value> values = new HashMap<Integer, Value>();
	int counter = 0;

	public void OpponentAction(Action opponentAction) {
		bidHistory = new BidHistory();
		opponentLastBid = DefaultAction.getBidFromAction(opponentAction); // Extracting
		// Bid from
		// opponent
		// Action
		System.out.println(opponentAction.toString());
		if (getUtility(opponentLastBid) != 0) {
			BidDetails bd = new BidDetails(opponentLastBid,
					getUtility(opponentLastBid)); // Extracting Bid details of
													// each Bid
			System.out.println("He " + bd.toString());
			bidHistory.add(bd);
			bidHistory.sortToUtility(); // Sorting Bid History by Utility values
		}
		System.out.println("Opponent's bid history is :"
				+ bidHistory.toString());
	}

	public void init() // Setting minimum Bid Utility value as per time
						// variation
	{
		if (timeline.getTime() < 0.9)
			MINIMUM_BID_UTILITY = 0.8;
		else if (timeline.getTime() < 0.96 && timeline.getTime() > 0.9)
			MINIMUM_BID_UTILITY = 0.7;
		else
			MINIMUM_BID_UTILITY = 0.3;
		System.out.println("Minium bid utility is :" + MINIMUM_BID_UTILITY);
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		init();
		if (!validActions.contains(Accept.class)) // Checking for first Bid
		{
			try {
				return new Offer(getPartyId(), generateNewBid());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			if (opponentLastBid != null
					&& getUtility(opponentLastBid) >= MINIMUM_BID_UTILITY) // point
																			// of
																			// acceptance
				return new Accept(getPartyId(), opponentLastBid);
			else {
				try {
					return new Offer(getPartyId(), generateNewBid());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

	private Bid generateNewBid() throws Exception // generating a new Bid in
													// Cyclic manner
	{
		System.out.println(counter);
		int size = bidDetailsList.size();
		BidDetails p = bidDetailsList.get(counter);
		bid = p.getBid();
		if (counter + 1 == size)
			counter = 0;
		else
			counter++;
		return bid;
	}

	private Bid generateNewBidContinous() throws Exception // Placing bids for
															// continuous domain
	{
		List<Issue> issues = utilitySpace.getDomain().getIssues();
		Random randomnr = new Random();
		do {
			for (Issue lIssue : issues) {
				switch (lIssue.getType()) {
				case DISCRETE:
					IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
					int optionIndex = randomnr.nextInt(lIssueDiscrete
							.getNumberOfValues());
					values.put(lIssue.getNumber(),
							lIssueDiscrete.getValue(optionIndex));
					break;
				case REAL:
					IssueReal lIssueReal = (IssueReal) lIssue;
					int optionInd = randomnr.nextInt(lIssueReal
							.getNumberOfDiscretizationSteps() - 1);
					values.put(
							lIssueReal.getNumber(),
							new ValueReal(lIssueReal.getLowerBound()
									+ (lIssueReal.getUpperBound() - lIssueReal
											.getLowerBound())
									* (double) (optionInd)
									/ (double) (lIssueReal
											.getNumberOfDiscretizationSteps())));
					break;
				case INTEGER:
					IssueInteger lIssueInteger = (IssueInteger) lIssue;
					int optionIndex2 = lIssueInteger.getLowerBound()
							+ randomnr.nextInt(lIssueInteger.getUpperBound()
									- lIssueInteger.getLowerBound());
					values.put(lIssueInteger.getNumber(), new ValueInteger(
							optionIndex2));
					break;
				default:
					System.out.println("not in any type");
				}
			}
			bid = new Bid(utilitySpace.getDomain(), values);
		} while (getUtility(bid) < MINIMUM_BID_UTILITY);
		for (Integer name : values.keySet()) {
			String key = name.toString();
			String value = values.get(name).toString();
			System.out.println(key + " " + value);
		}
		return bid;
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		OpponentAction(action);
		if (timeline.getTime() < 0.9) {
			Range r = new Range(0.8, 1); // extracting bids in utility range of
											// 1 and 0.8
			sortedOutcomeSpace = new SortedOutcomeSpace(this.getUtilitySpace());
			bidDetailsList = sortedOutcomeSpace.getBidsinRange(r);
			;
		} else if (timeline.getTime() > 0.9 && timeline.getTime() < 0.96) {
			Range r = new Range(0.7, 1); // extracting bids from opponents bids
											// which has utility range from 1
											// to0.7 for us
			sortedOutcomeSpace = new SortedOutcomeSpace(this.getUtilitySpace());
			bidDetailsList = sortedOutcomeSpace.getBidsinRange(r);
			bidDetailsList.addAll(bidHistory.getHistory());
		} else {
			Range r = new Range(0.5, 0.7); // extracting bids in utility range
											// of 0.7 and 0.5
			sortedOutcomeSpace = new SortedOutcomeSpace(this.getUtilitySpace());
			bidDetailsList = sortedOutcomeSpace.getBidsinRange(r);
		}
	}

	@Override
	public String getDescription() {
		return "Negotiation Agent Group 1";
	}
}

package agents.ai2014.group9;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import misc.Range;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.ValueDiscrete;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

public class Group9 extends AbstractNegotiationParty {
	private SortedOutcomeSpace outcomeSpace;
	private Bid lastBid = null;
	private double lastBidUtility = 0;
	private Bid myLastBid = null;
	private double myLastUtility = 1;

	private HashMap<Object, ArrayList<Bid>> partyBids;
	private HashMap<Object, float[]> partyIssueWeights;
	private HashMap<Object, HashMap<Integer, int[]>> partyIssueCounter;

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		partyBids = new HashMap<Object, ArrayList<Bid>>();
		partyIssueWeights = new HashMap<Object, float[]>();
		partyIssueCounter = new HashMap<Object, HashMap<Integer, int[]>>();
		outcomeSpace = new SortedOutcomeSpace(utilitySpace);
	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	// Bidding and acceptance strategy
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		// If a negotiating party makes an offer with >0.9 utility, we accept
		// it.
		if (lastBidUtility > 0.9) {
			return new Accept(getPartyId(), lastBid);
		}

		Bid newBid = null;
		double newBidUtility = 0;
		// if I have never offered a bid before, bid my max utility bid
		try {
			if (!validActions.contains(Accept.class) || myLastBid == null) {
				newBid = utilitySpace.getMaxUtilityBid();
				newBidUtility = utilitySpace.getUtility(newBid);
				myLastBid = newBid;
				myLastUtility = newBidUtility;
				return new Offer(getPartyId(), newBid);
			}
		} catch (Exception e) {
		}

		// I am a conceder, so calculate a utility threshold
		double concederThreshold = 1;
		if (timeline.getTime() > 0.75) {
			double progress = (timeline.getTime() - 0.75) / (1.0 - 0.75);
			concederThreshold = Math.sqrt(Math.sqrt(1.0 - progress)); // fourth
																		// root
		}

		// concede until I think I've conceded more than necessary for an equal
		// gain outcome.
		myLastUtility = getBidUtility(myLastBid);
		boolean KS = true; // Kalai-Smorodinsky point
		for (Object key : partyBids.keySet()) {
			double approximate = approximateUtility(key, myLastBid);
			double diff = Math.abs(myLastUtility - approximate);
			if (diff > 0.2) {
				KS = false;
			}
		}
		// if the bid I made earlier qualifies as an estimated Kalai-Smorodinsky
		// point, I'll offer that bid again.
		if (KS && myLastUtility <= concederThreshold) {
			return new Offer(getPartyId(), myLastBid);
		}

		// explore the bids with a personal utility slightly lower than my
		// previous bid
		List<BidDetails> myPossibleBids = outcomeSpace.getBidsinRange(new Range(myLastUtility, myLastUtility));
		double i = 0.02;
		if (myLastUtility <= concederThreshold) { // if the threshold is not yet
													// relevant
			while ((myPossibleBids.size() < 3) && (i < 1)) {
				double lw = myLastUtility - i;
				double up = myLastUtility;
				myPossibleBids = outcomeSpace.getBidsinRange(new Range(lw, up));
				i += 0.02;
			}
		} else {
			while ((myPossibleBids.size() < 3) && (i < 1)) {
				double lw = concederThreshold - i;
				double up = concederThreshold;
				myPossibleBids = outcomeSpace.getBidsinRange(new Range(lw, up));
				i += 0.02;
			}
		}

		// Check for each selected bid which one has the highest mutual utility
		int bestBidIndex = 0;
		double maxCommonUtility = 0;
		for (int j = 1; j < myPossibleBids.size(); j++) {
			Bid currentBid = myPossibleBids.get(j).getBid();
			double commonUtility = 1;
			try {
				commonUtility = utilitySpace.getUtility(currentBid);
				if (commonUtility == myLastUtility)
					continue;
			} catch (Exception e) {
			}

			for (Object key : partyBids.keySet()) {
				commonUtility *= approximateUtility(key, currentBid);
			}

			if (commonUtility > maxCommonUtility) {
				maxCommonUtility = commonUtility;
				bestBidIndex = j;
			}
		}
		newBid = myPossibleBids.get(bestBidIndex).getBid();
		newBidUtility = getBidUtility(newBid);
		// Bid the selected bid, unless its utility is lower than what we were
		// offered.
		if (newBidUtility < lastBidUtility) {
			return new Accept(getPartyId(), lastBid);
		} else {
			myLastBid = newBid;
			lastBid = myLastBid;
			try {
				lastBidUtility = utilitySpace.getUtility(lastBid);
			} catch (Exception e) {
			}
			return new Offer(getPartyId(), newBid);
		}
	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		// Here you can listen to other parties' messages
		if (!partyBids.containsKey(sender) && sender != null) {
			partyBids.put(sender, new ArrayList<Bid>());
		}

		// if another player sends an offer
		if ((action instanceof Offer)) {
			lastBid = ((Offer) action).getBid();
			partyBids.get(sender).add(lastBid);
			try {
				lastBidUtility = utilitySpace.getUtility(lastBid);
			} catch (Exception e) {
			}
		} else if (action instanceof Accept) {
			// if another player accepts a bid, they like this bid.
			if (lastBid != null)
				partyBids.get(sender).add(lastBid);
		}

		CalculateOpponentUtilityParameters(sender);
	}

	// simple try catch prevention
	private double getBidUtility(Bid b) {
		if (b == null)
			return 0;

		try {
			return utilitySpace.getUtility(b);
		} catch (Exception e) {
		}
		return 0;
	}

	// calculate the parameters that we use to predict the opponents utilities
	private void CalculateOpponentUtilityParameters(Object sender) {
		float n = 0.1f;

		if (!partyBids.containsKey(sender))
			return;

		ArrayList<Bid> bidList = partyBids.get(sender);
		if (bidList.size() == 1) { // the first time
			Bid first = bidList.get(0);

			// initialize the arrays
			List<Issue> issues = first.getIssues();
			int issueSize = issues.size();
			partyIssueWeights.put(sender, new float[issueSize]);
			partyIssueCounter.put(sender, new HashMap<Integer, int[]>());

			// go over each issue
			for (int i = 0; i < issueSize; i++) {
				Issue iss = first.getIssues().get(i);
				int isn = iss.getNumber();
				partyIssueWeights.get(sender)[i] = 1f / issueSize;

				int numValues = 1;
				int issueInd = 0;
				try {
					// get the correct issue type
					if (iss instanceof IssueDiscrete) {
						numValues = ((IssueDiscrete) iss).getValues().size();
						ValueDiscrete val = (ValueDiscrete) first.getValue(isn);
						issueInd = ((IssueDiscrete) iss).getValueIndex(val);
					} else if (iss instanceof IssueInteger) {
						numValues = ((IssueInteger) iss).getUpperBound() - ((IssueInteger) iss).getLowerBound() + 1;
						ValueInteger val = (ValueInteger) first.getValue(isn);
						issueInd = val.getValue() - ((IssueInteger) iss).getLowerBound();
					} else if (iss instanceof IssueReal) {
						// put them in 5 bins
						numValues = 5;
						ValueReal val = (ValueReal) first.getValue(isn);
						double normalized = (((IssueReal) iss).getUpperBound() - val.getValue())
								/ (((IssueReal) iss).getUpperBound() - ((IssueReal) iss).getLowerBound());
						issueInd = (int) Math.floor(normalized * 5);
						if (issueInd >= 5)
							issueInd = 4;
					}
				} catch (Exception e) {
				}

				partyIssueCounter.get(sender).put(isn, new int[numValues]);
				partyIssueCounter.get(sender).get(isn)[issueInd] = 10;
				// set counter to 10, we assume they start with their most
				// important
			}
		} else if (bidList.size() >= 2) { // not the first time
			int issueSize = bidList.get(0).getIssues().size();
			int lastBidIndex = bidList.size() - 1;
			float totalWeight = 0;

			// go over each issue
			for (int i = 0; i < issueSize; i++) {
				Issue iss = bidList.get(lastBidIndex).getIssues().get(i);
				int isn = iss.getNumber();

				// add a small weight if the value changed
				if (bidList.get(lastBidIndex).getIssues().get(i) != bidList.get(lastBidIndex - 1).getIssues().get(i)) {
					partyIssueWeights.get(sender)[i] += n;
					totalWeight += partyIssueWeights.get(sender)[i];
				}

				int issueInd = 0;
				try {
					// find which of the values this is
					if (iss instanceof IssueDiscrete) {
						issueInd = ((IssueDiscrete) iss)
								.getValueIndex((ValueDiscrete) bidList.get(lastBidIndex).getValue(isn));
					} else if (iss instanceof IssueInteger) {
						issueInd = ((ValueInteger) bidList.get(lastBidIndex).getValue(isn)).getValue()
								- ((IssueInteger) iss).getLowerBound();
					} else if (iss instanceof IssueReal) {
						double value = ((ValueReal) bidList.get(lastBidIndex).getValue(isn)).getValue();
						double normalized = (((IssueReal) iss).getUpperBound() - value)
								/ (((IssueReal) iss).getUpperBound() - ((IssueReal) iss).getLowerBound());
						issueInd = (int) Math.floor(normalized * 5);
						if (issueInd >= 5)
							issueInd = 4;
					}
				} catch (Exception e) {
				}
				// add 1 to the issue chosen
				partyIssueCounter.get(sender).get(isn)[issueInd]++;
			}
			if (totalWeight == 0)
				return;

			// normalize
			for (int i = 0; i < issueSize; i++) {
				partyIssueWeights.get(sender)[i] /= totalWeight;
			}
		}
	}

	// given a bid, predict the utility for the opponent
	private double approximateUtility(Object party, Bid bid) {
		if (partyIssueCounter == null || party == null || bid == null)
			return 0.5;

		double totalUtility = 0;
		// go over each issue
		for (int i = 0; i < bid.getIssues().size(); i++) {
			Issue iss = bid.getIssues().get(i);
			if (iss == null)
				continue;

			int isn = iss.getNumber();

			int issueInd = 0;
			try {
				// get the correct issue type
				if (iss instanceof IssueDiscrete) {
					issueInd = ((IssueDiscrete) iss).getValueIndex((ValueDiscrete) bid.getValue(isn));
				} else if (iss instanceof IssueInteger) {
					issueInd = ((ValueInteger) bid.getValue(isn)).getValue() - ((IssueInteger) iss).getLowerBound();
				} else if (iss instanceof IssueReal) {
					double value = ((ValueReal) bid.getValue(isn)).getValue();
					double divider = ((IssueReal) iss).getUpperBound() - ((IssueReal) iss).getLowerBound();
					if (divider == 0)
						continue;
					double normalized = (((IssueReal) iss).getUpperBound() - value) / divider;
					issueInd = (int) Math.floor(normalized * 5);
					if (issueInd >= 5)
						issueInd = 4;
				}
			} catch (Exception e) {
			}

			if (!partyIssueCounter.containsKey(party))
				continue;
			if (!partyIssueCounter.get(party).containsKey(isn))
				continue;
			if (partyIssueCounter.get(party).get(isn) == null)
				continue;

			// get the max value
			int maxCount = 1;
			int numOptions = 0;
			for (int j = 0; j < partyIssueCounter.get(party).get(isn).length; j++) {
				maxCount = Math.max(maxCount, partyIssueCounter.get(party).get(isn)[j]);
				if (partyIssueCounter.get(party).get(isn)[j] != 0)
					numOptions++;
			}

			// get the weight and the value for this issue
			double value = partyIssueCounter.get(party).get(isn)[issueInd] / (double) maxCount;

			if (numOptions == 1 && value == 0.0) // for hardball players who
													// only select 1 option
				value = 0.5; // we dont know how you feel about this subject, so
								// we assume 0.5
			else if (value == 0.0)
				value = 0.1; // you probably dont hate it, even though you
								// didn't choose it

			float weight = partyIssueWeights.get(party)[i];
			totalUtility += value * weight;
		}

		if (totalUtility == 0) // something went wrong
			return 0.5;
		else
			return totalUtility;
	}

	protected AgentID partyId = new AgentID("Group 9");

	@Override
	public String getDescription() {
		return "ai2014 group9";
	}

}
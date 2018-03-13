package agents.ai2014.group4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;

public class Party {

	private String name;
	private ArrayList<IssueModel> issueModels = new ArrayList<IssueModel>();
	private double rateOfChange = 0.1;
	private HashMap<IssueModel, List<Integer>> accepted = new HashMap<IssueModel, List<Integer>>();
	private AgentID agentid;

	/**
	 * 
	 * @param name
	 * @param domain
	 * @param agentid
	 *            the agent id of the party represented
	 */
	public Party(String name, Domain domain, AgentID agentid) {
		this.name = name;
		createIssueModels(domain);
		this.agentid = agentid;
	}

	private void createIssueModels(Domain domain) {
		for (Issue issue : domain.getIssues()) {
			List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();
			IssueModel im = new IssueModel(issue.getName(), values);
			issueModels.add(im);
			accepted.put(im, new ArrayList<Integer>());
			for (int i = 0; i < values.size(); i++) {
				accepted.get(im).add(0);
			}
		}

	}

	public void updateWithBid(Bid bid, Action action) {
		// Update utility
		for (IssueModel issue : issueModels) {
			for (Issue iss : bid.getIssues()) {
				if (issue.getName().equals(iss.getName())) {
					ValueDiscrete value = new ValueDiscrete("");
					int i = iss.getNumber() - 1;
					try {
						value = (ValueDiscrete) bid.getValue(i + 1);
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (action instanceof Accept) {
						issue.updateUtility(value, rateOfChange);

						List<Integer> acc = accepted.get(issue);
						int j = 0;
						for (ValueDiscrete v : issue.getValues()) {
							try {
								if (v.getValue().equals(
										bid.getValue(i + 1).toString())) {
									acc.set(j, acc.get(j) + 1);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							j++;
						}
						accepted.put(issue, acc);
					} else if (action instanceof Offer) {
						issue.updateUtility(value, -rateOfChange);
						Bid newBid = DefaultAction.getBidFromAction(action);
						updateWithBid(newBid, new Accept(agentid, newBid));

						List<Integer> acc = accepted.get(issue);
						int j = 0;
						for (ValueDiscrete v : issue.getValues()) {
							try {
								if (v.getValue().equals(
										bid.getValue(i + 1).toString())) {
									acc.set(j, acc.get(j) - 1);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							j++;
						}
						accepted.put(issue, acc);
					}
					break;
				}
			}
		}

		// update weights
		double sumSpread = 0;
		double spread = 0;
		for (IssueModel issue : issueModels) {
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.POSITIVE_INFINITY;
			for (int p : accepted.get(issue)) {
				max = Math.max(max, p);
				min = Math.min(min, p);
			}
			spread = max - min;
			sumSpread = sumSpread + spread;
		}
		for (IssueModel issue : issueModels) {
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.POSITIVE_INFINITY;
			for (int p : accepted.get(issue)) {
				max = Math.max(max, p);
				min = Math.min(min, p);
			}
			spread = max - min;
			issue.setValue(spread / sumSpread);
		}
	}

	public Double estimateUtility(Bid bid) {
		double utility = 0;

		for (IssueModel issue : issueModels) {
			double weight = issue.getValue();
			ValueDiscrete value = new ValueDiscrete("");
			for (Issue iss : bid.getIssues()) {
				if (issue.getName() == iss.getName()) {
					int i = iss.getNumber();
					try {
						value = (ValueDiscrete) bid.getValue(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
			double util = issue.getUtility(value);
			utility = utility + util * weight;
		}

		return utility;
	}

	public ArrayList<IssueModel> getIssueModels() {
		return issueModels;
	}

	public void setIssueModels(ArrayList<IssueModel> issueModels) {
		this.issueModels = issueModels;
	}

}

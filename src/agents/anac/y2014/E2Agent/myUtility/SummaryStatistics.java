package  agents.anac.y2014.E2Agent.myUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import agents.anac.y2011.TheNegotiator.BidsCollection;
import agents.anac.y2012.MetaAgent.agents.WinnerAgent.opponentOffers;
import negotiator.Agent;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.timeline.Timeline;
import negotiator.utility.*;





public class SummaryStatistics {
    private double average = 0;
    private double variance = 0;

    public SummaryStatistics(double ave, double var) {
        average = ave;
        variance = var;
    }

    public double getAve() { return average; }
    public double getVar() { return variance; }
    public String toString() { return "Ave: " + average + " Var: " + variance; }
}

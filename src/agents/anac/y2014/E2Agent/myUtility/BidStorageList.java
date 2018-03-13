package agents.anac.y2014.E2Agent.myUtility;

import java.io.Serializable;
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



public class BidStorageList implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<BidStorage> bidStorageList = null;

    public BidStorageList() {
        bidStorageList = new ArrayList<BidStorage>();
    }

    public void addBidStorage(Bid bid, double util, double time) {
        bidStorageList.add(new BidStorage(bid, util, time));
    }
    public void addBidStorage(BidStorage bidStorage) {
        bidStorageList.add(bidStorage);
    }

    public ArrayList<BidStorage> getBidStorageList() { return bidStorageList; }

    /**
     * è¦�ç´„çµ±è¨ˆé‡�ã‚’å�–å¾—
     */
    public SummaryStatistics getSummaryStatistics() {
        double sum_utiliy = 0.0;
        double pow_sum_utiliy = 0.0;
        for (BidStorage bidStorage : bidStorageList) {
            double u = bidStorage.getUtility();;
            sum_utiliy += u; // å¹³å�‡ç”¨
            pow_sum_utiliy += Math.pow(u, 2); // åˆ†æ•£ç”¨
        }
        double size = bidStorageList.size();
        double ave_utility = sum_utiliy / size;
        double var_utility = (pow_sum_utiliy / size) - Math.pow(ave_utility, 2);

        return new SummaryStatistics(ave_utility, var_utility);
    }

    /**
     * æœ€ã‚‚è‰¯ã�„Bidã‚’å�–å¾—
     */
    public BidStorage getBestBidStorage() {
        BidStorage maximum = bidStorageList.get(0);
        for (BidStorage bidStorage : bidStorageList) {
            if (maximum.getUtility() < bidStorage.getUtility()) {
                maximum = bidStorage;
            }
        }
        return maximum;
    }
    
}

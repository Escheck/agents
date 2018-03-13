package agents.anac.y2016.caduceus.agents.Caduceus;

import java.util.ArrayList;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.UtilitySpace;
import agents.anac.y2016.caduceus.agents.Caduceus.sanity.SaneUtilitySpace;

/**
 * Created by tdgunes on 12/03/16.
 */
public class Opponent {
	private class BidRecord {
		int roundNumber;
		final Bid bid;

		BidRecord(int roundNumber, Bid bid) {
			this.bid = bid;
		}
	}

	public AgentID identifier;
	public ArrayList<Bid> history = new ArrayList<Bid>();
	public SaneUtilitySpace saneUtilitySpace = new SaneUtilitySpace();

	public Opponent(AgentID identifier, UtilitySpace space) {
		saneUtilitySpace.initZero((AdditiveUtilitySpace) space);
		this.identifier = identifier;
	}

	public void addToHistory(Bid receivedBid) {
		history.add(receivedBid);
	}

	public ArrayList<Bid> getBidHistory() {
		return history;
	}

}

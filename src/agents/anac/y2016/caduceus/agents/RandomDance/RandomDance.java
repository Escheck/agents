package agents.anac.y2016.caduceus.agents.RandomDance;

import java.util.List;

import negotiator.AgentID;
import negotiator.actions.Action;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

/**
 * Created by tdgunes on 30/03/16.
 */
public class RandomDance extends AbstractNegotiationParty {

	agents.anac.y2015.RandomDance.RandomDance realRandomDance = new agents.anac.y2015.RandomDance.RandomDance();

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		realRandomDance.init(info);

	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> list) {
		return realRandomDance.chooseAction(list);

	}

	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		realRandomDance.receiveMessage(sender, arguments);
	}

	@Override
	public String getDescription() {
		return "anac 2016 RandomDance";
	}
}

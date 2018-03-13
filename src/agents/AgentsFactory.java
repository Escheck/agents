package agents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import negotiator.Agent;

/**
 * Factory that can provide you with agent names, search for implementors of
 * some interface, and give you an instantiation of an agent of a given name.
 * 
 * <br>
 * 
 * This class is primarily intended to facilitate PocketNegotiator, but can be
 * used in Genius as well. Just notice we have no complete list of agents yet,
 * but this can be fixed easily.
 * 
 * @author W.Pasman 10jul14
 * 
 */
public class AgentsFactory {

	// * Singleton pattern, the static way.

	private static Map<String, Class<? extends Agent>> agents = new HashMap<String, Class<? extends Agent>>();

	static {
		add(SimpleTitForTatPN.class);
		add(SimpleAgentPN.class);
		add(TimeDependentAgentConcederPN.class);
		add(BayesianAgentPN.class);
		add(DenizPN.class);

		add(agents.anac.y2010.AgentFSEGA.AgentFSEGA.class);
		add(agents.anac.y2010.AgentK.Agent_K.class);
		add(agents.anac.y2010.Southampton.IAMhaggler.class);
		add(agents.anac.y2010.Southampton.IAMcrazyHaggler.class);
		add(agents.anac.y2010.AgentSmith.AgentSmith.class);
		add(agents.anac.y2010.Nozomi.Nozomi.class);
		add(agents.anac.y2010.Yushu.Yushu.class);
		add(agents.anac.y2011.TheNegotiator.TheNegotiator.class);
		add(agents.anac.y2011.ValueModelAgent.ValueModelAgent.class);
		add(agents.anac.y2011.Gahboninho.Gahboninho.class);
		add(agents.anac.y2011.BramAgent.BRAMAgent.class);

		// more to be added. Maybe add automatically?
	}

	private AgentsFactory() {
	}

	/**
	 * Add an agent to the factory. If agent fails to load/check, we just print
	 * error and return. This just facilitates internal handling, add is never
	 * used by others.
	 * 
	 * @param agentClass
	 */
	private static void add(Class<? extends Agent> agentClass) {
		Agent agent;
		try {
			agent = getInstanceOf(agentClass);
		} catch (Exception e) {
			System.out.println("failed to load agent " + agentClass);
			e.printStackTrace();
			return;
		}
		String name = agent.getName();
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("agent " + agentClass
					+ " has empty or null name");
		}
		Class<? extends Agent> existing = agents.get(name);
		if (existing != null) {
			throw new IllegalArgumentException("agent " + agent.getClass()
					+ " has same name as agent " + existing.getClass());
		}

		agents.put(agent.getName(), agentClass);
	}

	/**
	 * Create new instance of an Agent.
	 * 
	 * @param agentClass
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private static Agent getInstanceOf(Class<? extends Agent> agentClass)
			throws InstantiationException, IllegalAccessException {
		return agentClass.newInstance();
	}

	/**
	 * get all available agent names
	 * 
	 * @return all names.
	 */
	public static Set<String> getAgents() {
		return agents.keySet();
	}

	/**
	 * Get all agents of a particular type
	 * 
	 * @param classType
	 *            the type that is desired.
	 * @return
	 */
	public static Set<String> getAgents(Class<?> classType) {
		Set<String> names = new HashSet<String>();
		for (String name : agents.keySet()) {
			Class<? extends Agent> agtclass = agents.get(name);
			if (classType.isAssignableFrom(agtclass)) {
				names.add(name);
			}
		}
		return names;
	}

	/**
	 * Get a new instance of an agent with given name.
	 * 
	 * @param name
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Agent getAgentInstance(String name)
			throws InstantiationException, IllegalAccessException {
		return getInstanceOf(agents.get(name));
	}

}

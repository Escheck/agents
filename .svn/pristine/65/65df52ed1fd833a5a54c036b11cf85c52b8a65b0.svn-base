package parties.in4010.q12015.group2;

import static edu.berkeley.nlp.lm.NgramLanguageModel.StaticMethods.getDistributionOverNextWords;
import static edu.berkeley.nlp.lm.io.ArpaLmReader.END_SYMBOL;
import static edu.berkeley.nlp.lm.io.ArpaLmReader.START_SYMBOL;
import static edu.berkeley.nlp.lm.io.ArpaLmReader.UNK_SYMBOL;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
import agents.bayesianopponentmodel.OpponentModel;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.ConfigOptions;
import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.io.LmReaders;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.DefaultAction;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Group2 is a class representing a software agent for automatic negotiation in
 * Genius. The key technical insight in this agent is the application of
 * statistical language models. We address the problem of predicting the next
 * bid in the negotiation. Given a state of the negotiation, we predict the next
 * bid that could be offered from some of the parties -- in a more general
 * setting this could be the next n bids. Our main idea is to reduce the problem
 * of bid prediction to a natural-language processing problem of predicting
 * probabilities of sentences. We design a simple and scalable agent that stores
 * sequences of bids during a negotiation process, and index these into a
 * statistical language model. We then employ the language model to find the
 * highest ranked sentences, and use them to predicted bid(s) during bidding and
 * acceptance.
 * 
 * @author Group2
 * @since 1.0
 *
 */
public class Group2 extends AbstractNegotiationParty {
	private static final int TOP_K = 15;
	private static final int LM_ORDER = 5;
	private static final String LM_TXT = "lm.txt";
	private static final String LM_ARPA = "lm.arpa";
	private static final String LM_BINARY = "lm.binary";

	private static final double ACCEPTANCE_TIME = 0.9d;
	private static final double TRAINING_TIME = 0.5d;
	private static final double ALPHA_COEFICIENT = 0.8d;

	private static final int RANDOM_ITERATIONS = 100000;

	private Bid maxBid;
	private double alpha;
	private Bid opponentBid;
	private Bid upcommingBid;
	private Map<String, List<String>> trainingBids;
	private Map<String, OpponentModel> opponentsModels;
	private Map<String, ArrayEncodedProbBackoffLm<String>> languageModels;

	/**
	 * Initializes maxBid, alpha, opponentModels collections and trainingBids
	 * collection.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		try {
			this.maxBid = utilitySpace.getMaxUtilityBid();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.alpha = getUtility(maxBid) * ALPHA_COEFICIENT;
		this.opponentsModels = new HashMap<String, OpponentModel>();
		this.trainingBids = new HashMap<String, List<String>>();
		this.languageModels = new HashMap<String, ArrayEncodedProbBackoffLm<String>>();
	}

	/**
	 * Informs the agent that the opponent just performed the action. Stores the
	 * opponent's action in order to use it when choosing an action. Further, it
	 * performs an opponent modeling according to the strategy described in the
	 * report.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		super.receiveMessage(sender, arguments);
		this.opponentBid = DefaultAction.getBidFromAction(arguments);
		modelOpponent(sender, arguments);
	}

	/**
	 * The method asks to specify an action to send to the opponents. The method
	 * works as follows: if we are first to place a bid, we place a trade-off
	 * bid with a sufficient utility, otherwise, we determine whether to accept
	 * or not the bid, depending on the acceptance condition. Finally, it
	 * accepts or pose a new trade-off bid. It is essential that our bidding
	 * strategy takes a predicted opponents models into account.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> actions) {
		this.upcommingBid = getTradeOffBid();
		return isAcceptable() ? new Accept(getPartyId(), opponentBid) : new Offer(getPartyId(), this.upcommingBid);
	}

	/**
	 * The method implements the acceptance condition described in the report.
	 * Basically, AC_combi(T, alpha) = AC_next AND AC_time(T) OR AC_const(alpha)
	 * 
	 * @return
	 */
	private boolean isAcceptable() {
		boolean aConst = getUtility(this.opponentBid) >= this.alpha;
		boolean aNext = getUtility(this.opponentBid) >= getUtility(this.upcommingBid);
		boolean aTime = timeline.getTime() >= ACCEPTANCE_TIME;
		return aConst || aNext && aTime;
	}

	/**
	 * The method tries to build a statistical language model. Then it generates
	 * an upcoming bid based on the trade-off strategy described in the report.
	 * Here we employ the opponent modeling and bid prediction components. The
	 * isBetterThanOpponents(bid) method assures that the generated bid will
	 * have better utility than the estimated utilities of the opponents
	 * according to the opponent model. The method isSuitableForOpponents(bid)
	 * employs the statistical language model. The idea is that we offer bids
	 * which are suitable for our opponents -- our statistical language model
	 * predicts that the opponent will generate certain bid(s) with some
	 * probability.
	 * 
	 * @return
	 */
	private Bid getTradeOffBid() {
		buildStatisticalLanguageModel();
		Bid bestBid = null;
		double bestUtility = 0.0D;
		double bestDistance = 0.0D;
		boolean ignoreSuitabilityCheck = false;
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < RANDOM_ITERATIONS; i++) {
				Bid bid = utilitySpace.getDomain().getRandomBid(null);
				double oldUtility = getUtility(this.upcommingBid);
				double newUtility = getUtility(bid);
				double distance = getHammingDistance(bid, this.opponentBid);
				if (newUtility > oldUtility && distance <= bestDistance && newUtility > bestUtility
						&& isBetterThanOpponents(bid) && (ignoreSuitabilityCheck || isSuitableForOpponents(bid))) {
					bestDistance = distance;
					bestBid = bid;
					bestUtility = getUtility(bestBid);
				}
			}
			if (bestBid == null) {
				ignoreSuitabilityCheck = true;
			} else {
				break;
			}
		}
		return bestBid == null ? getRandomBid(this.alpha) : bestBid;
	}

	/**
	 * The method getRandomBid(this.alpha) generates a random bid with utility
	 * greater or equal than alpha. It is based on the well-known Random Walker
	 * strategy. It randomly jumps through the negotiation space, and it is run
	 * with break-off point, avoiding making offers below a certain utility. If
	 * it could not find a bid above a certain utility, the method returns the
	 * maximum utility bid.
	 * 
	 * @param target
	 * @return
	 */
	private Bid getRandomBid(double target) {
		for (int i = 0; i < RANDOM_ITERATIONS; i++) {
			Bid b = utilitySpace.getDomain().getRandomBid(null);
			if (getUtility(b) >= target && isBetterThanOpponents(b)) {
				return b;
			}
		}
		return this.maxBid;
	}

	private double getHammingDistance(Bid myBid, Bid opponentBid) {
		double dist = 0.0D;
		for (int i = 1; i <= myBid.getIssues().size(); i++) {
			try {
				if (myBid.getValue(i).equals(opponentBid.getValue(i))) {
					continue;
				}
			} catch (Exception e) {
				;
			}
			dist += 1.0D;
		}
		return dist;
	}

	private void modelOpponent(AgentID sender, Action arguments) {
		if (sender == null)
			return; // Wouter added
		if (!this.opponentsModels.containsKey(sender.toString())) {
			this.opponentsModels.put(sender.toString(),
					new BayesianOpponentModelScalable((AdditiveUtilitySpace) utilitySpace));
		}
		if ((arguments instanceof Offer)) {
			try {
				((OpponentModel) this.opponentsModels.get(sender.toString())).updateBeliefs(this.opponentBid);
			} catch (Exception e) {
				;
			}
		}

		if (!this.trainingBids.containsKey(sender.toString())) {
			this.trainingBids.put(sender.toString(), new ArrayList<String>());
		}
		if ((arguments instanceof Offer)) {
			this.trainingBids.get(sender.toString()).add(this.opponentBid.toString().replaceAll("\\s", ""));
		}
	}

	private boolean isBetterThanOpponents(Bid myBid) {
		for (OpponentModel model : this.opponentsModels.values()) {
			try {
				if (model.getExpectedUtility(myBid) >= getUtility(myBid)) {
					return false;
				}
			} catch (Exception e) {
				return true;
			}
		}
		return true;
	}

	/**
	 * Trains the statistical language model. First, we check whether it is time
	 * for training. Then, we generate training sequences and store them in a
	 * predefined $.txt$ file. Finally, we employ the statistical language model
	 * framework. The result is trained probabilistic models for each opponent
	 * stored in binary files.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildStatisticalLanguageModel() {
		if (timeline.getTime() > TRAINING_TIME && this.languageModels.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : trainingBids.entrySet()) {
				try {
					String opponentId = entry.getKey();
					List<String> opponentBids = entry.getValue();
					PrintWriter writer = new PrintWriter(opponentId + LM_TXT, "UTF-8");
					for (int i = 0; i < opponentBids.size(); i++) {
						for (int j = i; j < Math.min(i + LM_ORDER, opponentBids.size()); j++) {
							writer.print(opponentBids.get(j));
							writer.print(" ");
						}
						writer.println(".");
					}
					writer.close();
					List<String> files = new ArrayList<String>();
					files.add(opponentId + LM_TXT);
					StringWordIndexer wi = new StringWordIndexer();
					wi.setStartSymbol(START_SYMBOL);
					wi.setEndSymbol(END_SYMBOL);
					wi.setUnkSymbol(UNK_SYMBOL);
					LmReaders.createKneserNeyLmFromTextFiles(files, wi, LM_ORDER, new File(opponentId + LM_ARPA),
							new ConfigOptions());
					LmReaders.writeLmBinary(LmReaders.readArrayEncodedLmFromArpa(opponentId + LM_ARPA, true),
							opponentId + LM_BINARY);
					ArrayEncodedProbBackoffLm lm = (ArrayEncodedProbBackoffLm) LmReaders
							.readLmBinary(opponentId + LM_BINARY);
					this.languageModels.put(opponentId, lm);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	/**
	 * The prediction phase is encoded in the method predictNextBids(). It is a
	 * standard way of inference in statistical language model setting. For
	 * every opponent language model, we iterate over the distribution of the
	 * next words, sorted by decreasing count, and select the top k. Of course,
	 * we exclude the UNK, START, and END symbols from the prediction. As a
	 * result we return the intersection between the predictions for each
	 * opponent.
	 * 
	 * @return
	 */
	private List<String> predictNextBids() {
		List<String> result = new ArrayList<String>();
		if (!this.languageModels.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : trainingBids.entrySet()) {
				try {
					List<String> localResult = new ArrayList<String>();
					String opponentId = entry.getKey();
					List<String> opponentBids = entry.getValue();
					Collection<Entry<String, Double>> dist = getDistributionOverNextWords(
							this.languageModels.get(opponentId), opponentBids).getEntriesSortedByDecreasingCount();
					for (Entry<String, Double> e : dist) {
						if (localResult.size() == TOP_K) {
							break;
						} else if (UNK_SYMBOL.equals(e.getKey()) || START_SYMBOL.equals(e.getKey())
								|| END_SYMBOL.equals(e.getKey()) || ".".equals(e.getKey())) {
							continue;
						} else {
							localResult.add(e.getKey());
						}
					}
					if (result.isEmpty()) {
						result.addAll(localResult);
					} else {
						result.retainAll(localResult);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return result;
	}

	private boolean isSuitableForOpponents(Bid bid) {
		List<String> predictedBids = predictNextBids();
		return predictedBids.isEmpty() || predictedBids.contains(bid.toString().replaceAll("\\s", ""));
	}

	@Override
	public String getDescription() {
		return "in4010.q12015.group2";
	}
}

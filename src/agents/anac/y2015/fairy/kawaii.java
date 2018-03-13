package agents.anac.y2015.fairy;

import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * This is your negotiation party.
 */
public class kawaii extends AbstractNegotiationParty {
	private negotiatingInfo negotiatingInfo; // �����
	private bidSearch bidSearch; // Bid�T��
	private strategy strategy; // ���헪
	private Bid offeredBid = null; // ��Ă��ꂽ���ӈČ��

	// �f�o�b�O�p
	public static boolean isPrinting = false; // ���b�Z�[�W��\������

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

		if (isPrinting) {
			System.out.println("*** SAOPMN_SampleAgent ***");
		}

		negotiatingInfo = new negotiatingInfo((AdditiveUtilitySpace) utilitySpace);
		try {
			bidSearch = new bidSearch((AdditiveUtilitySpace) utilitySpace, negotiatingInfo);
		} catch (Exception e) {
			throw new RuntimeException("init failed:" + e, e);
		}
		strategy = new strategy((AdditiveUtilitySpace) utilitySpace, negotiatingInfo);
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
	@SuppressWarnings("rawtypes")
	@Override
	// Action�̑I��
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		double time = timeline.getTime(); // ���݂̌��������擾

		// System.out.println("�s���I��");
		// Accept
		if (validActions.contains(Accept.class) && strategy.selectAccept(offeredBid, time)) {
			return new Accept(getPartyId(), offeredBid);
		}

		// EndNegotiation
		// if(true){ return new EndNegotiation(); }

		if (strategy.selectEndNegotiation(time)) {
			return new EndNegotiation(getPartyId());
		}

		// Offer
		return OfferAction();
	}

	public Action OfferAction() {
		Bid offerBid = bidSearch.getBid(generateRandomBid(), strategy.getThreshold(timeline.getTime()));
		negotiatingInfo.updateMyBidHistory(offerBid);
		return new Offer(getPartyId(), offerBid);
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
	// ���g�ȊO�̌��Q���҂�Action����M
	public void receiveMessage(AgentID sender, Action action) {

		// System.out.println("��M");
		// System.out.println("Sender:"+sender+", Action:"+action);
		super.receiveMessage(sender, action);
		// Here you can listen to other parties' messages
		if (isPrinting) {
			System.out.println("Sender:" + sender + ", Action:" + action);
		}

		// System.out.println("��M");

		if (action != null) {
			if (action instanceof Inform && ((Inform) action).getName() == "NumberOfAgents"
					&& ((Inform) action).getValue() instanceof Integer) {
				Integer opponentsNum = (Integer) ((Inform) action).getValue();
				negotiatingInfo.updateOpponentsNum(opponentsNum);
				if (isPrinting) {
					System.out.println("NumberofNegotiator:" + negotiatingInfo.getNegotiatorNum());
				}
			} else if (action instanceof Accept) {
				if (!negotiatingInfo.getOpponents().contains(sender)) {
					negotiatingInfo.initOpponent(sender);
				} // ���o�̌��҂͏���
				negotiatingInfo.setOpponentsBool(sender, true);// ���ӂł��邱�Ƃ̃Z�b�g

			} else if (action instanceof Offer) {
				if (!negotiatingInfo.getOpponents().contains(sender)) {
					negotiatingInfo.initOpponent(sender);
				} // ���o�̌��҂͏���
				offeredBid = ((Offer) action).getBid(); // ��Ă��ꂽ���ӈČ��
				negotiatingInfo.setOpponentsBool(sender, false);// ���ӂłȂ����Ƃ̃Z�b�g
				try {
					negotiatingInfo.updateInfo(sender, offeredBid);
				} // �������X�V
				catch (Exception e) {
					System.out.println("�����̍X�V�Ɏ��s���܂���");
					e.printStackTrace();
				}
			} else if (action instanceof EndNegotiation) {
				// System.out.println("�������􂵂܂����B");
			}
		}

		// old ver 3/2 r
		/*
		 * if (action != null ) { if(action instanceof Accept){
		 * if(!negotiatingInfo.getOpponents().contains(sender)){
		 * negotiatingInfo.initOpponent(sender); } // ���o�̌��҂͏���
		 * negotiatingInfo.setOpponentsBool(sender,true);//���ӂł��邱�Ƃ̃Z�b�g }
		 * else if(action instanceof Offer) {
		 * if(!negotiatingInfo.getOpponents().contains(sender)){
		 * negotiatingInfo.initOpponent(sender); } // ���o�̌��҂͏���
		 * negotiatingInfo.setOpponentsBool(sender,false);//���ӂłȂ����Ƃ̃Z�b�g
		 * offeredBid = ((Offer) action).getBid(); // ��Ă��ꂽ���ӈČ�� try {
		 * negotiatingInfo.updateInfo(sender, offeredBid); } // �������X�V
		 * catch (Exception e) { System.out.println("�����̍X�V�Ɏ��s���܂���");
		 * e.printStackTrace(); } } else { Object obj = ((Object)action); int
		 * opponentsNum =
		 * Integer.parseInt(obj.toString().replaceAll("[^0-9]",""));
		 * negotiatingInfo.updateOpponentsNum(opponentsNum); if(isPrinting){
		 * System.out.println("NumberofNegotiator:" +
		 * negotiatingInfo.getNegotiatorNum());} } }
		 */
	}

	@Override
	public String getDescription() {
		return "ANAC2015-12-Kawaii";
	}

}

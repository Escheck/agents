package parties.AlternatingMultipleOffers;

import negotiator.protocol.AlternatingMajorityConsensusProtocol;
import negotiator.protocol.DefaultMultilateralProtocol;

public class RandomAmopPartyMajority extends RandomAmopParty {

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return AlternatingMajorityConsensusProtocol.class;
	}
}

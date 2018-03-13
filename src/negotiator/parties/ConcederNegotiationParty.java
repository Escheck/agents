package negotiator.parties;

public class ConcederNegotiationParty extends AbstractTimeDependentNegotiationParty {

	@Override
	public double getE() {
		return 2;
	}

	@Override
	public String getDescription() {
		return "Deadline-based conceder agent";
	}
}

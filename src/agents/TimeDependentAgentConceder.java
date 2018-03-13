package agents;

import negotiator.SupportedNegotiationSetting;

public class TimeDependentAgentConceder extends TimeDependentAgent {
	@Override
	public double getE() {
		return 2;
	}

	@Override
	public String getName() {
		return "Conceder";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}
}

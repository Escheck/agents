package agents;

import negotiator.SupportedNegotiationSetting;

public class TimeDependentAgentLinear extends TimeDependentAgent {
	@Override
	public double getE() {
		return 1;
	}

	@Override
	public String getName() {
		return "Conceder Linear";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}
}

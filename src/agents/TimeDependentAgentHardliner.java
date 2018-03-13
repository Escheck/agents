package agents;

import negotiator.SupportedNegotiationSetting;

public class TimeDependentAgentHardliner extends TimeDependentAgent {
	@Override
	public double getE() {
		return 0;
	}

	@Override
	public String getName() {
		return "Hardliner";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}
}

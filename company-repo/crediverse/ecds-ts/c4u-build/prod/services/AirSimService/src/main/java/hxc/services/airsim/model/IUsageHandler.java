package hxc.services.airsim.model;

import hxc.services.airsim.protocol.UsageTimer;

public interface IUsageHandler
{
	void onUsage(UsageTimer usageTimer);
}

package hxc.services.airsim;

import hxc.services.airsim.protocol.IAirSim;

public interface IAirSimProvider
{
	public abstract IAirSim getAirSim();
}
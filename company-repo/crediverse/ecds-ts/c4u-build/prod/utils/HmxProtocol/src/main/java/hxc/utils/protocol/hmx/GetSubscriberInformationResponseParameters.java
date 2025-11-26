package hxc.utils.protocol.hmx;

import hxc.utils.protocol.hsx.Number;

public class GetSubscriberInformationResponseParameters
{
	public static final int DOMAIN_CIRCUIT_SWITCHED = 0;
	public static final int DOMAIN_PACKET_SWITCHED = 1;
	public static final int DOMAIN_ANY = 3;

	// Mandatory
	public Number subscriberNumber;

	// This indicates the domain of the request.
	// 0 Circuit Switched
	// 1 Packet Switched
	// 3 Any
	// Mandatory
	public int domain;

	// Optional
	public State state;

	// Optional
	public BasicLocation basicLocation;

	// Optional
	public Mnp mnp;
	
	// Optional
	public Imsi imsi;
}

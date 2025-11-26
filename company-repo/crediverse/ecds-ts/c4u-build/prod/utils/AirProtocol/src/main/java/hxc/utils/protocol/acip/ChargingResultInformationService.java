package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * ChargingResultInformationService
 * 
 * The chargingResultInformationService parameter contains result information regarding a service for a charged end user communication event. It is enclosed in a <struct> of its own. Note: That
 * chargingResultInformationService is currently only used in updateFaFList Response.
 */
public class ChargingResultInformationService
{
	/*
	 * The cost1 and cost2 parameters contains the cost for an event. cost1 indicates a cost in the first currency to be announced and cost2 a cost in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long cost1;

	/*
	 * The cost1 and cost2 parameters contains the cost for an event. cost1 indicates a cost in the first currency to be announced and cost2 a cost in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long cost2;

}

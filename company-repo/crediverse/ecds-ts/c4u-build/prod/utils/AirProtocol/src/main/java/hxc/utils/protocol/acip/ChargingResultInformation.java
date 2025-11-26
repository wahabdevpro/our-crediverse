package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * ChargingResultInformation
 * 
 * The chargingResultInformation parameter contains result information for a charged end user communication event. It is enclosed in a <struct> of its own. Note: For GetBalanceAndDate Response the
 * currency1 and currency2 are sent separately and are not included in chargingResultInformation.
 */
public class ChargingResultInformation
{
	/*
	 * The cost1 and cost2 parameters contains the cost for an event. cost1 indicates a cost in the first currency to be announced and cost2 a cost in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long cost1;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency1;

	/*
	 * The cost1 and cost2 parameters contains the cost for an event. cost1 indicates a cost in the first currency to be announced and cost2 a cost in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long cost2;

	/*
	 * The currency1 and currency2 parameters contains the currencies to be presented to the end user. currency1 indicates the first currency to be announced and currency2 the second one.
	 */
	@Air(Format = "Currency")
	public String currency2;

	/*-
	 * The chargingResultCode parameter contains information related to a
	 * charged event.
	 *
	 * Possible Values:
	 * ----------------
	 * 0 or not present:	Successful
	 * 1:	No charge (free counter stepped)
	 * 2:	No charge (free counter not stepped)
	 */
	public Integer chargingResultCode;

	/*
	 * The reservationCorrelationID parameter contains the id needed to correlate a reservation.
	 */
	@Air(Range = "0:2147483647")
	public Integer reservationCorrelationID;

	public ChargingResultInformationService chargingResultInformationService;

}

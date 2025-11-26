package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * ChargingRequestInformation
 * 
 * The chargingRequestInformation parameter contains request information for a charged end user communication event. It is enclosed in a <struct> of its own.
 */
public class ChargingRequestInformation
{
	/*-
	 * The chargingType parameter contains information how the request is to be
	 * charged and which mechanism to use.
	 *
	 * Possible Values:
	 * ----------------
	 * 1:	Check order and make reservation
	 * 2:	Perform order and make deduction
	 * 3:	Perform order and commit reservation
	 * 4:	Rollback reservation
	 * 5:	Get allowed options
	 * 6:	Rate and check (reserved for future use)
	 */
	public Integer chargingType;

	/*
	 * The chargingIndicator parameter contains an indicator for rating differentiation.
	 */
	@Air(Range = "0:65535")
	public Integer chargingIndicator;

	/*
	 * The reservationCorrelationID parameter contains the id needed to correlate a reservation.
	 */
	@Air(Range = "0:2147483647")
	public Integer reservationCorrelationID;

}

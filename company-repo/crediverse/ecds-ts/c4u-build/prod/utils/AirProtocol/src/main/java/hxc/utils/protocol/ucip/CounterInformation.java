package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * CounterInformation
 * 
 * The struct counterInformation contains counter values and clearing date for account management. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class CounterInformation
{
	/*-
	 * The counterIDparameter contains an identifier of the counter used for a
	 * charged end user communication eventinformation related to a charged event.
	 *
	 * Possible Values:
	 * ----------------
	 * 201:	Charged Service Class change
	 * 202:	Charged FaF addition
	 * 203:	Charged balance enquiry
	 */
	@Air(Mandatory = true)
	public int counterID;

	/*
	 * The totalCounterValue parameter contains the value of the total counter used for charged end user communication events.
	 */
	@Air(Mandatory = true, Range = "0:127")
	public int totalCounterValue;

	/*
	 * The periodCounterValue parameter contains the value of period counter used for charged end user communication events.
	 */
	@Air(Mandatory = true, Range = "0:127")
	public int periodCounterValue;

	/*
	 * The counterClearingDate parameter contains a date when a period counter, for a charged end user communication event, was last reset.
	 */
	@Air(Mandatory = true, Range = "DateMin:DateToday")
	public Date counterClearingDate;

}

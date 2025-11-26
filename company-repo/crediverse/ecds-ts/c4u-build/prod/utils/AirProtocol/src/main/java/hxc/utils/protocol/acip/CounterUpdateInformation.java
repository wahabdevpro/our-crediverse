package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * CounterUpdateInformation
 * 
 * The struct counterUpdateInformation contains counter values and clearing date to be modified for account management. The two relative counter adjustment parameters must both be either positive or
 * negative. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class CounterUpdateInformation
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
	 * The totalCounterRelativeValue parameter contains the value to be added or subtracted to the total counter.
	 */
	@Air(Range = "-127:127")
	public Integer totalCounterRelativeValue;

	/*
	 * The periodCounterRelativeValue parameter contains the value to be added or subtracted to the period counter.
	 */
	@Air(Range = "-127:127")
	public Integer periodCounterRelativeValue;

	/*
	 * The counterClearingDate parameter contains a date when a period counter, for a charged end user communication event, was last reset.
	 */
	@Air(Range = "DateMin:DateToday")
	public Date counterClearingDate;

}

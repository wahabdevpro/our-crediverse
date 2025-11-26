package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * SubDedicatedAccountIdentifier
 * 
 * The struct subDedicatedAccountIdentifier contains information for identifying a unique sub dedicated account. DateInfinite is used to identify a sub dedicated account without a expiry date.
 * DateBeginingOfTime is used to identify a sub dedicated account without a start date It is enclosed in a <struct> of its own.
 */
public class SubDedicatedAccountIdentifier
{
	/*
	 * The startDateCurrent parameter contains the current start date for a dedicated account. The parameter may also be used to define start date for other entities depending on the context where it
	 * is used. Used for validation. No validation is performed if omitted.
	 */
	@Air(Mandatory = true, Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDateCurrent;

	/*
	 * The expiryDateCurrent parameter contains the current expiry date for a dedicated account. The parameter may also be used to define expiry date for other entities depending on the context where
	 * it is used. Used for validation. No validation is performed if omitted.
	 */
	@Air(Mandatory = true, Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDateCurrent;

}

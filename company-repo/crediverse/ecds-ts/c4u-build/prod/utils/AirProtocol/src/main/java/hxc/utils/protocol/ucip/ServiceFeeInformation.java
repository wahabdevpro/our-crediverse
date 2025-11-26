package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * ServiceFeeInformation
 * 
 * The serviceFeeInformation parameter contains the values of the service fee data information. It is enclosed in a <struct> of its own.
 */
@Air(PC = "PC:06214")
public class ServiceFeeInformation
{
	/*
	 * The serviceFeeID parameter contains the identity of the service fee definition in service class.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:255")
	public int serviceFeeID;

	/*
	 * The serviceFeeAmount1 and serviceFeeAmount2 parameter contains the periodic fee for the service. serviceFeeAmount1 indicates serviceFeeAmount in the first currency to be announced and
	 * serviceFeeAmount2 the amount in the second currency.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public long serviceFeeAmount1;

	/*
	 * The serviceFeeAmount1 and serviceFeeAmount2 parameter contains the periodic fee for the service. serviceFeeAmount1 indicates serviceFeeAmount in the first currency to be announced and
	 * serviceFeeAmount2 the amount in the second currency.
	 */
	@Air(PC = "PC:06214", Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long serviceFeeAmount2;

	/*
	 * The serviceFeeChargedAmount1 and serviceFeeChargedAm ount2serviceFeeChargedAmount1 and serviceFeeChargedAmount2 parameter contains the amount actually deducted. In case of pre-rate charging
	 * this could be different from the defined serviceFeeAmount. serviceFeeChargedAmount1 indicates serviceFeeChargedAmount in the first currency to be announced and serviceFeeChargedAmount2 in the
	 * second currency.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public long serviceFeeChargedAmount1;

	/*
	 * The serviceFeeChargedAmount1 and serviceFeeChargedAm ount2serviceFeeChargedAmount1 and serviceFeeChargedAmount2 parameter contains the amount actually deducted. In case of pre-rate charging
	 * this could be different from the defined serviceFeeAmount. serviceFeeChargedAmount1 indicates serviceFeeChargedAmount in the first currency to be announced and serviceFeeChargedAmount2 in the
	 * second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long serviceFeeChargedAmount2;

	/*
	 * The serviceFeeDeductionDate parameter contains the date when the last successful deduction was done. If no deduction date exists then the parameter will not be returned
	 */
	@Air(PC = "PC:06214", Range = "DateMin:DateToday")
	public Date serviceFeeDeductionDate;

	/*
	 * The serviceFeeDeductionPeriod parameter contains the number of days/months (see Section 7.199 on page 190) the service fee is valid after deduction.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "1:255")
	public int serviceFeeDeductionPeriod;

	/*-
	 * The serviceFeePeriodUnit parameter contains the unit of the
	 * serviceFeeDeductionPeriod.
	 *
	 * Possible Values:
	 * ----------------
	 * Days:	Period in days
	 * Months:	Period in months
	 */
	@Air(PC = "PC:06214", Mandatory = true)
	public String serviceFeePeriodUnit;

}

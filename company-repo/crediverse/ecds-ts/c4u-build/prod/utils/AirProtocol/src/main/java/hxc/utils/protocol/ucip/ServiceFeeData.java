package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * ServiceFeeData
 * 
 * The serviceFeeData parameter contains the values of the service fee data. It is enclosed in a <struct> of its own.
 */
@Air(PC = "PC:06214")
public class ServiceFeeData
{
	/*
	 * The serviceFeeID parameter contains the identity of the service fee definition in service class.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:255")
	public int serviceFeeID;

	/*
	 * The serviceFeeDeductionDate parameter contains the date when the last successful deduction was done. If no deduction date exists then the parameter will not be returned
	 */
	@Air(PC = "PC:06214", Range = "DateMin:DateToday")
	public Date serviceFeeDeductionDate;

	/*
	 * The serviceFeeDebtAmount1 and serviceFeeDebtAmount2 parameter contains the outstanding debt on the account for a fee. serviceFeeDe btAmount1 indicates a value in the first currency to be
	 * announced and serviceFeeDebtAmount2 a value in the second currency.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public long serviceFeeDebtAmount1;

	/*
	 * The serviceFeeDebtAmount1 and serviceFeeDebtAmount2 parameter contains the outstanding debt on the account for a fee. serviceFeeDe btAmount1 indicates a value in the first currency to be
	 * announced and serviceFeeDebtAmount2 a value in the second currency.
	 */
	@Air(PC = "PC:06214", Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long serviceFeeDebtAmount2;

	/*
	 * TheserviceFeeAccumulators parameter contains the number of accumulations which have been done since the last successful deduction.
	 */
	@Air(PC = "PC:06214", Mandatory = true, Range = "0:255")
	public int serviceFeeAccumulators;

	/*
	 * The chargedForIndicator parameter contains an indication whether fee is charged for or not.
	 */
	@Air(PC = "PC:06214", Mandatory = true)
	public boolean chargedForIndicator;

}

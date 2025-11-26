package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * ChargingInformation
 * 
 * The chargingInformation parameter contains request information for an charged Online Communication ID Change. It is enclosed in a <struct> of its own.
 */
public class ChargingInformation
{
	/*
	 * The chargingIndicator parameter contains an indicator for rating differentiation.
	 */
	@Air(Range = "0:65535")
	public Integer chargingIndicator;

	/*
	 * The specifiedPrice parameter contains a price that shall be used to charge for the operation, instead of using the the rating function normally used.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long specifiedPrice;

	/*
	 * The transactionCurrency parameter contains an ID to point out what currency is used for the transaction. A transaction parameter includes data regarding a requested change. transactionCurrency
	 * is mandatory if the account adjustment affects a dedicated account or an usageCounter with a monetary value, thus is of type Money. transactionCurrencyis also mandatory when a
	 * UpdateAccountDetails or InstallSubscriber request includes the parameter accountPrepaidEmptyLimit.
	 */
	@Air(Format = "Currency")
	public String transactionCurrency;

	/*
	 * The suppressDeduction parameter indicates if the calculated cost is to be withdrawn from the account or not.
	 */
	public Boolean suppressDeduction;

}

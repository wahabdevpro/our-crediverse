package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * UsageCounterUpdateInformation
 * 
 * The usageCounterUpdateInformation specifies usage counter data. Depending on the usage counted the counter value is represented either in the usageCounterValue parameter or the
 * usageCounterMonetaryValue1 parameter. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class UsageCounterUpdateInformation
{
	/*
	 * The usageCounterID parameter identifies the ID of a usage counter.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int usageCounterID;

	/*
	 * The usageCounterValueNew parameter contains the updated value of a non-monetary usage counter.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	public Long usageCounterValueNew;

	/*
	 * The adjustmentUsageCounterValueRelative parameter contains the adjustment value of a non-monetary usage counter.
	 */
	@Air(Range = "-9223372036854775807:9223372036854775807", Format = "SignedNumeric")
	@XmlRpcAsString
	public Long adjustmentUsageCounterValueRelative;

	/*
	 * The usageCounterMonetaryValueNew parameters contains the updated value of a monetary usage counter.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageCounterMonetaryValueNew;

	/*
	 * The adjustmentUsageCounterMonetaryValueRelative parameter indicates the adjustment value of a monetary usage counter.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long adjustmentUsageCounterMonetaryValueRelative;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String associatedPartyID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	public Integer productID;

}

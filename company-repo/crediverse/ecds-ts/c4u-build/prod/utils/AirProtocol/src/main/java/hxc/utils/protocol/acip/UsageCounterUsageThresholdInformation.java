package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * UsageCounterUsageThresholdInformation
 * 
 * The usageCounterUsageThresholdInformation element contains all active usage counters with their thresholds for a subscriber. The productID can be zero in a response indicating that a personal usage
 * threshold value exists for the usage counter and there may exist zero or more additional product local usage counters which have not been used yet. It is enclosed in a <struct> of its own. Structs
 * are placed in an <array>.
 */
public class UsageCounterUsageThresholdInformation
{
	/*
	 * The usageCounterID parameter identifies the ID of a usage counter.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int usageCounterID;

	/*
	 * The usageCounterValue parameter contains the value of a non-monetary usage counter.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	public Long usageCounterValue;

	/*
	 * The usageCounterMonetaryValue1 and usageCounterMonetaryValu e2 parameters contains the value of a monetary usage counter. The parameter usageCounterMonetaryValue1 indicates a usage counter
	 * amount in the first currency to be announced and usageCounterMonetaryValue2 a usage counter amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageCounterMonetaryValue1;

	/*
	 * The usageCounterMonetaryValue1 and usageCounterMonetaryValu e2 parameters contains the value of a monetary usage counter. The parameter usageCounterMonetaryValue1 indicates a usage counter
	 * amount in the first currency to be announced and usageCounterMonetaryValue2 a usage counter amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageCounterMonetaryValue2;

	public UsageThresholdInformation[] usageThresholdInformation;

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

package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * UsageThresholdUpdateInformation
 * 
 * The usageThresholdUpdateInformation element is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class UsageThresholdUpdateInformation
{
	/*
	 * The usageThresholdID parameter identifies the ID of a usage threshold.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int usageThresholdID;

	/*
	 * The usageThresholdValueNew parameter contains the new value of a non-monetary usage threshold.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	public Long usageThresholdValueNew;

	/*
	 * The usageThresholdMonetaryValueNew parameter contains the updated value of a monetary usage threshold.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageThresholdMonetaryValueNew;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String associatedPartyID;

	/*
	 * The productID parameter.
	 */
	@Air(Range = "0:2147483647")
	public int productID;
}

package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * UsageThresholdInformation
 * 
 * The usageThresholdInformation element carries all information about a usage threshold. Depending on the usage counted the threshold value is represented either in the usageThresholdValue parameter
 * or the usage ThresholdMonetaryValue1 or usageThresholdMonetaryValue2 parameter. The usageThresholdSource parameter can not be updated but is included in responses. The usageThresholdInformation
 * element is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class UsageThresholdInformation
{

	/*
	 * The usageThresholdID parameter identifies the ID of a usage threshold.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int usageThresholdID;

	/*
	 * The usageThresholdValue parameter contains the value of a non-monetary usage threshold.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	public Long usageThresholdValue;

	/*
	 * The usageThresholdMonetaryValue1 and usageThresholdMo netaryValue2 parameters contains the value of a monetary usage threshold. The parameter usageThresholdMonetaryValue1 indicates a usage
	 * threshold amount in the first currency to be announced and usageThresholdMonetaryValue2 a usage threshold amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageThresholdMonetaryValue1;

	/*
	 * The usageThresholdMonetaryValue1 and usageThresholdMo netaryValue2 parameters contains the value of a monetary usage threshold. The parameter usageThresholdMonetaryValue1 indicates a usage
	 * threshold amount in the first currency to be announced and usageThresholdMonetaryValue2 a usage threshold amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long usageThresholdMonetaryValue2;

	/*-
	 * The usageThreseholdSource parameter contains the source of a usage
	 * threshold.
	 *
	 * Possible Values:
	 * ----------------
	 * 1:	Personal - The threshold has been changed to a value other than the usage counter definition and is fetched from the subscriber.
	 * 2:	Common - The threshold has been changed to an individual value for a provider shared by all consumers.
	 * 3:	Default - The threshold value fetched from the usage counter definition.
	 */
	public Integer usageThresholdSource;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	public String associatedPartyID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UsageThresholdInformation()
	{

	}

	public UsageThresholdInformation(UsageThresholdInformation usageThresholdInformation)
	{
		this.usageThresholdID = usageThresholdInformation.usageThresholdID;
		this.usageThresholdValue = usageThresholdInformation.usageThresholdValue;
		this.usageThresholdMonetaryValue1 = usageThresholdInformation.usageThresholdMonetaryValue1;
		this.usageThresholdMonetaryValue2 = usageThresholdInformation.usageThresholdMonetaryValue2;
		this.usageThresholdSource = usageThresholdInformation.usageThresholdSource;
		this.associatedPartyID = usageThresholdInformation.associatedPartyID;
	}

}

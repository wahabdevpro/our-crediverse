package hxc.services.airsim.protocol;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

public class UsageThreshold
{
	/*
	 * The usageThresholdID parameter identifies the ID of a usage threshold.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	protected int usageThresholdID;

	/*
	 * The usageThresholdValue parameter contains the value of a non-monetary usage threshold.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	protected Long usageThresholdValue;

	/*
	 * The usageThresholdMonetaryValue1 and usageThresholdMo netaryValue2 parameters contains the value of a monetary usage threshold. The parameter usageThresholdMonetaryValue1 indicates a usage
	 * threshold amount in the first currency to be announced and usageThresholdMonetaryValue2 a usage threshold amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long usageThresholdMonetaryValue1;

	/*
	 * The usageThresholdMonetaryValue1 and usageThresholdMo netaryValue2 parameters contains the value of a monetary usage threshold. The parameter usageThresholdMonetaryValue1 indicates a usage
	 * threshold amount in the first currency to be announced and usageThresholdMonetaryValue2 a usage threshold amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long usageThresholdMonetaryValue2;

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
	protected Integer usageThresholdSource = 3;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	protected String associatedPartyID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getUsageThresholdID()
	{
		return usageThresholdID;
	}

	public void setUsageThresholdID(int usageThresholdID)
	{
		this.usageThresholdID = usageThresholdID;
	}

	public Long getUsageThresholdValue()
	{
		return usageThresholdValue;
	}

	public void setUsageThresholdValue(Long usageThresholdValue)
	{
		this.usageThresholdValue = usageThresholdValue;
	}

	public Long getUsageThresholdMonetaryValue1()
	{
		return usageThresholdMonetaryValue1;
	}

	public void setUsageThresholdMonetaryValue1(Long usageThresholdMonetaryValue1)
	{
		this.usageThresholdMonetaryValue1 = usageThresholdMonetaryValue1;
	}

	public Long getUsageThresholdMonetaryValue2()
	{
		return usageThresholdMonetaryValue2;
	}

	public void setUsageThresholdMonetaryValue2(Long usageThresholdMonetaryValue2)
	{
		this.usageThresholdMonetaryValue2 = usageThresholdMonetaryValue2;
	}

	public Integer getUsageThresholdSource()
	{
		return usageThresholdSource;
	}

	public void setUsageThresholdSource(Integer usageThresholdSource)
	{
		this.usageThresholdSource = usageThresholdSource;
	}

	public String getAssociatedPartyID()
	{
		return associatedPartyID;
	}

	public void setAssociatedPartyID(String associatedPartyID)
	{
		this.associatedPartyID = associatedPartyID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UsageThreshold()
	{
	}

	public UsageThreshold(UsageThreshold usageThreshold)
	{
		this.usageThresholdID = usageThreshold.usageThresholdID;
		this.usageThresholdValue = usageThreshold.usageThresholdValue;
		this.usageThresholdMonetaryValue1 = usageThreshold.usageThresholdMonetaryValue1;
		this.usageThresholdMonetaryValue2 = usageThreshold.usageThresholdMonetaryValue2;
		this.usageThresholdSource = usageThreshold.usageThresholdSource;
		this.associatedPartyID = usageThreshold.associatedPartyID;
	}

}

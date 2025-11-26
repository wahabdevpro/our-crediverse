package hxc.services.airsim.protocol;

import hxc.connectors.air.Air;
import hxc.utils.protocol.ucip.UsageThresholdInformation;
import hxc.utils.xmlrpc.XmlRpcAsString;

public class UsageCounter
{
	/*
	 * The usageCounterID parameter identifies the ID of a usage counter.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	protected int usageCounterID;

	/*
	 * The usageCounterValue parameter contains the value of a non-monetary usage counter.
	 */
	@Air(Range = "0:9223372036854775807", Format = "Numeric")
	@XmlRpcAsString
	protected Long usageCounterValue;

	/*
	 * The usageCounterMonetaryValue1 and usageCounterMonetaryValu e2 parameters contains the value of a monetary usage counter. The parameter usageCounterMonetaryValue1 indicates a usage counter
	 * amount in the first currency to be announced and usageCounterMonetaryValue2 a usage counter amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long usageCounterMonetaryValue1;

	/*
	 * The usageCounterMonetaryValue1 and usageCounterMonetaryValu e2 parameters contains the value of a monetary usage counter. The parameter usageCounterMonetaryValue1 indicates a usage counter
	 * amount in the first currency to be announced and usageCounterMonetaryValue2 a usage counter amount in the second one.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	protected Long usageCounterMonetaryValue2;

	protected UsageThresholdInformation[] usageThresholdInformation;

	/*
	 * The associatedPartyID parameter contains the subscriber identity of the consumer or provider. The default format of the parameter is the same numbering format as used by the account database,
	 * this also includes support of leading zeroes.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	protected String associatedPartyID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(Range = "0:2147483647")
	protected Integer productID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getUsageCounterID()
	{
		return usageCounterID;
	}

	public void setUsageCounterID(int usageCounterID)
	{
		this.usageCounterID = usageCounterID;
	}

	public Long getUsageCounterValue()
	{
		return usageCounterValue;
	}

	public void setUsageCounterValue(Long usageCounterValue)
	{
		this.usageCounterValue = usageCounterValue;
	}

	public Long getUsageCounterMonetaryValue1()
	{
		return usageCounterMonetaryValue1;
	}

	public void setUsageCounterMonetaryValue1(Long usageCounterMonetaryValue1)
	{
		this.usageCounterMonetaryValue1 = usageCounterMonetaryValue1;
	}

	public Long getUsageCounterMonetaryValue2()
	{
		return usageCounterMonetaryValue2;
	}

	public void setUsageCounterMonetaryValue2(Long usageCounterMonetaryValue2)
	{
		this.usageCounterMonetaryValue2 = usageCounterMonetaryValue2;
	}

	public UsageThresholdInformation[] getUsageThresholdInformation()
	{
		return usageThresholdInformation;
	}

	public void setUsageThresholdInformation(UsageThresholdInformation[] usageThresholdInformation)
	{
		this.usageThresholdInformation = usageThresholdInformation;
	}

	public String getAssociatedPartyID()
	{
		return associatedPartyID;
	}

	public void setAssociatedPartyID(String associatedPartyID)
	{
		this.associatedPartyID = associatedPartyID;
	}

	public Integer getProductID()
	{
		return productID;
	}

	public void setProductID(Integer productID)
	{
		this.productID = productID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UsageCounter()
	{

	}

	public UsageCounter(UsageCounter usageCounter)
	{
		this.usageCounterID = usageCounter.usageCounterID;
		this.usageCounterValue = usageCounter.usageCounterValue;
		this.usageCounterMonetaryValue1 = usageCounter.usageCounterMonetaryValue1;
		this.usageCounterMonetaryValue2 = usageCounter.usageCounterMonetaryValue2;

		if (usageCounter.usageThresholdInformation != null)
		{
			this.usageThresholdInformation = new UsageThresholdInformation[usageCounter.usageThresholdInformation.length];
			for (int index = 0; index < usageCounter.usageThresholdInformation.length; index++)
			{
				this.usageThresholdInformation[index] = new UsageThresholdInformation(usageCounter.usageThresholdInformation[index]);
			}
		}

		this.associatedPartyID = usageCounter.associatedPartyID;
		this.productID = usageCounter.productID;
	}

}

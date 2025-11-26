package hxc.services.airsim.model;

import hxc.connectors.air.Air;
import hxc.services.airsim.protocol.Offer;
import hxc.utils.protocol.ucip.AggregatedOfferInformation;
import hxc.utils.protocol.ucip.AttributeInformationList;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.UsageCounterUsageThresholdInformation;

public class OfferEx extends Offer
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private DedicatedAccountInformation[] dedicatedAccountInformation;

	private UsageCounterUsageThresholdInformation[] usageCounterUsageThresholdInformation;

	@Air(CAP = "CAP:1,CAP:16")
	private AttributeInformationList[] attributeInformationList;

	@Air(PC = "PC:10803", CAP = "CAP:15")
	private AggregatedOfferInformation aggregatedOfferInformation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public DedicatedAccountInformation[] getDedicatedAccountInformation()
	{
		return dedicatedAccountInformation;
	}

	public void setDedicatedAccountInformation(DedicatedAccountInformation[] dedicatedAccountInformation)
	{
		this.dedicatedAccountInformation = dedicatedAccountInformation;
	}

	public UsageCounterUsageThresholdInformation[] getUsageCounterUsageThresholdInformationx()
	{
		return usageCounterUsageThresholdInformation;
	}

	public void setUsageCounterUsageThresholdInformation(UsageCounterUsageThresholdInformation[] usageCounterUsageThresholdInformation)
	{
		this.usageCounterUsageThresholdInformation = usageCounterUsageThresholdInformation;
	}

	public AttributeInformationList[] getAttributeInformationList()
	{
		return attributeInformationList;
	}

	public void setAttributeInformationList(AttributeInformationList[] attributeInformationList)
	{
		this.attributeInformationList = attributeInformationList;
	}

	public AggregatedOfferInformation getAggregatedOfferInformation()
	{
		return aggregatedOfferInformation;
	}

	public void setAggregatedOfferInformation(AggregatedOfferInformation aggregatedOfferInformation)
	{
		this.aggregatedOfferInformation = aggregatedOfferInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public OfferEx()
	{

	}

	public OfferEx(OfferEx offer)
	{
		if (offer.dedicatedAccountInformation != null)
		{
			this.dedicatedAccountInformation = new DedicatedAccountInformation[offer.dedicatedAccountInformation.length];
			for (int index = 0; index < offer.dedicatedAccountInformation.length; index++)
			{
				this.dedicatedAccountInformation[index] = new DedicatedAccountInformation(offer.dedicatedAccountInformation[index]);
			}
		}

		if (offer.usageCounterUsageThresholdInformation != null)
		{
			this.usageCounterUsageThresholdInformation = new UsageCounterUsageThresholdInformation[offer.usageCounterUsageThresholdInformation.length];
			for (int index = 0; index < offer.usageCounterUsageThresholdInformation.length; index++)
			{
				this.usageCounterUsageThresholdInformation[index] = new UsageCounterUsageThresholdInformation(offer.usageCounterUsageThresholdInformation[index]);
			}
		}

		if (offer.attributeInformationList != null)
		{
			this.attributeInformationList = new AttributeInformationList[offer.attributeInformationList.length];
			for (int index = 0; index < offer.attributeInformationList.length; index++)
			{
				this.attributeInformationList[index] = new AttributeInformationList(offer.attributeInformationList[index]);
			}
		}

		if (offer.aggregatedOfferInformation != null)
		{
			this.aggregatedOfferInformation = new AggregatedOfferInformation(offer.aggregatedOfferInformation);
		}

	}

}

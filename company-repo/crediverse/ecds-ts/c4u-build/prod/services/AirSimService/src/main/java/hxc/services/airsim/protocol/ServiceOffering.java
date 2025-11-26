package hxc.services.airsim.protocol;

import hxc.utils.protocol.ucip.ServiceOfferings;

public class ServiceOffering
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private int serviceOfferingID;

	private boolean serviceOfferingActiveFlag;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getServiceOfferingID()
	{
		return serviceOfferingID;
	}

	public void setServiceOfferingID(int serviceOfferingID)
	{
		this.serviceOfferingID = serviceOfferingID;
	}

	public boolean isServiceOfferingActiveFlag()
	{
		return serviceOfferingActiveFlag;
	}

	public void setServiceOfferingActiveFlag(boolean serviceOfferingActiveFlag)
	{
		this.serviceOfferingActiveFlag = serviceOfferingActiveFlag;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ServiceOffering()
	{

	}

	public ServiceOffering(ServiceOfferings serviceOffering)
	{
		this.serviceOfferingActiveFlag = serviceOffering.serviceOfferingActiveFlag;
		this.serviceOfferingID = serviceOffering.serviceOfferingID;
	}

	public ServiceOfferings toServiceOfferings()
	{
		ServiceOfferings result = new ServiceOfferings();
		result.serviceOfferingActiveFlag = this.serviceOfferingActiveFlag;
		result.serviceOfferingID = this.serviceOfferingID;

		return result;
	}

}

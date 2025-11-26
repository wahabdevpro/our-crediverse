package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class VasServiceInfo implements IDeserialisable
{


	
	private String serviceID;
	private String serviceName;
	private String variantID;
	private String variantName;
	private SubscriptionState state;
	
	private static final long serialVersionUID = 5627921959965795532L;
	public static final int PROPERTY_COUNT = 5;

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(String name)
	{
		this.serviceName = name;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String getVariantName()
	{
		return variantName;
	}

	public void setVariantName(String variantName)
	{
		this.variantName = variantName;
	}

	public SubscriptionState getState()
	{
		return state;
	}

	public void setState(SubscriptionState state)
	{
		this.state = state;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDeserialisable Implementation
	//
	// /////////////////////////////////
	@Override
	public void deserialise(ISerialiser serialiser)
	{

		this.serviceID = serialiser.getString("serviceID", null);
		this.serviceName = serialiser.getString("serviceName", null);
		this.variantID = serialiser.getString("variantID", null);
		this.variantName = serialiser.getString("variantName", null);
		this.state = serialiser.getEnum("state", SubscriptionState.class,
				SubscriptionState.unknown);

	}

}

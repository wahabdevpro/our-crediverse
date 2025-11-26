package com.concurrent.hxc;

public class VasServiceInfo
{
	private String serviceID;
	private String serviceName;
	private String variantID;
	private String variantName;
	private SubscriptionState state;
	private ServiceQuota[] quotas;
	private Boolean customisable;

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

	public ServiceQuota[] getQuotas()
	{
		return quotas;
	}

	public void setQuotas(ServiceQuota[] quotas)
	{
		this.quotas = quotas;
	}

	public Boolean isCustomisable()
	{
		return customisable;
	}

	public void setCustomisable(Boolean customisable)
	{
		this.customisable = customisable;
	}

}

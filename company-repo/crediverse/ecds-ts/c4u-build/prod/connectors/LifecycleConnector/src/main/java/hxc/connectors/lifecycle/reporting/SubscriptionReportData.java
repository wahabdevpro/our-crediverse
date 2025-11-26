package hxc.connectors.lifecycle.reporting;

public class SubscriptionReportData
{
	private String serviceID;
	private String variantID;
	private int serviceClass;
	private long subscriptions;

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public int getServiceClass()
	{
		return serviceClass;
	}

	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	public long getSubscriptions()
	{
		return subscriptions;
	}

	public void setSubscriptions(long subscriptions)
	{
		this.subscriptions = subscriptions;
	}

	public SubscriptionReportData(String serviceID, String variantID, int serviceClass, long subscriptions)
	{
		super();
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.serviceClass = serviceClass;
		this.subscriptions = subscriptions;
	}

	public SubscriptionReportData()
	{
	}
}

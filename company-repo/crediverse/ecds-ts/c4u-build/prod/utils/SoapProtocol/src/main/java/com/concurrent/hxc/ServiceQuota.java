package com.concurrent.hxc;

public class ServiceQuota
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String quotaID;
	private String name;
	private String service;
	private String destination;
	private String timeOfDay;
	private String daysOfWeek;
	private Long quantity;
	private String units;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getQuotaID()
	{
		return quotaID;
	}

	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getTimeOfDay()
	{
		return timeOfDay;
	}

	public void setTimeOfDay(String timeOfDay)
	{
		this.timeOfDay = timeOfDay;
	}

	public String getDaysOfWeek()
	{
		return daysOfWeek;
	}

	public void setDaysOfWeek(String timeOfWeek)
	{
		this.daysOfWeek = timeOfWeek;
	}

	public Long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(Long quantity)
	{
		this.quantity = quantity;
	}

	public String getUnits()
	{
		return units;
	}

	public void setUnits(String units)
	{
		this.units = units;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ServiceQuota()
	{
	}

	public ServiceQuota(ServiceQuota quota)
	{
		this.quotaID = quota.quotaID;
		this.name = quota.name;
		this.service = quota.service;
		this.destination = quota.destination;
		this.timeOfDay = quota.timeOfDay;
		this.daysOfWeek = quota.daysOfWeek;
		this.quantity = quota.quantity;
		this.units = quota.units;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ServiceQuota))
			return false;

		ServiceQuota that = (ServiceQuota) obj;
		return false;
	}

	@Override
	public String toString()
	{
		return String.format("%s (%d %s)", name, quantity, units);
	}

}

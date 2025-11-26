package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialisable;
import com.concurrent.util.ISerialiser;

public class ServiceQuota implements IDeserialisable, ISerialisable
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
	
	public static final int PROPERTY_COUNTx = 7; //!! 

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public void deserialise(ISerialiser serialiser)
	{
		quotaID = serialiser.getString("quotaID", "");
		name = serialiser.getString("name", "");
		service = serialiser.getString("service", "");
		destination = serialiser.getString("destination", "");
		timeOfDay = serialiser.getString("timeOfDay", "");
		daysOfWeek = serialiser.getString("daysOfWeek", "");
		quantity = serialiser.getLong("quantity", 0L);
		units = serialiser.getString("units", "");
	}

	@Override
	public void serialise(ISerialiser serialiser)
	{
		serialiser.add("quotaID", quotaID);
//		serialiser.add("name", name);
//		serialiser.add("service", service);
//		serialiser.add("destination", destination);
//		serialiser.add("timeOfDay", timeOfDay);
//		serialiser.add("daysOfWeek", daysOfWeek);
		serialiser.add("quantity", quantity);
//		serialiser.add("units", units);
	}

}

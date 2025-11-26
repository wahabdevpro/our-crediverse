package hxc.userinterfaces.cc.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class QuotaFilter
{
	private String service;
	private String destination;
	private String dow;
	private String tod;
	private String units;

	public QuotaFilter()
	{
	}

	public QuotaFilter(String service, String destination, String dow, String tod, String units)
	{
		this.service = service;
		this.destination = destination;
		this.dow = dow;
		this.tod = tod;
		this.units = units;
	}

	/**
	 * @return the service
	 */
	public String getService()
	{
		return service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public void setService(String service)
	{
		this.service = service;
	}

	/**
	 * @return the destination
	 */
	public String getDestination()
	{
		return destination;
	}

	/**
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	/**
	 * @return the dow
	 */
	public String getDow()
	{
		return dow;
	}

	/**
	 * @param dow
	 *            the dow to set
	 */
	public void setDow(String dow)
	{
		this.dow = dow;
	}

	/**
	 * @return the tod
	 */
	public String getTod()
	{
		return tod;
	}

	/**
	 * @param tod
	 *            the tod to set
	 */
	public void setTod(String tod)
	{
		this.tod = tod;
	}

	public JsonObject getJsonObject()
	{
		JsonObject job = new JsonObject();
		job.add("service", new JsonPrimitive(service));
		job.add("dest", new JsonPrimitive(destination));
		job.add("dow", new JsonPrimitive(dow));
		job.add("tod", new JsonPrimitive(tod));
		job.add("units", new JsonPrimitive(units));
		return job;
	}

	/**
	 * @return the units
	 */
	public String getUnits()
	{
		return units;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(String units)
	{
		this.units = units;
	}

}

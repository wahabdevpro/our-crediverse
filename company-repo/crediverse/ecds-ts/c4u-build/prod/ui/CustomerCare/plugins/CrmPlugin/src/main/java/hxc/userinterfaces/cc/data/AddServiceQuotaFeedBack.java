package hxc.userinterfaces.cc.data;

import java.lang.reflect.Field;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class AddServiceQuotaFeedBack
{
	private String msisdn;
	private String serviceId;
	private String variantId;
	private String benMsisdn;

	private String service;
	private String destination;
	private String dow;
	private String tod;
	private String quantity;
	private String units;

	private String message; // Dialog title text

	private String cost;

	public AddServiceQuotaFeedBack()
	{
	}

	/**
	 * @return the msisdn
	 */
	public String getMsisdn()
	{
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId()
	{
		return serviceId;
	}

	/**
	 * @param serviceId
	 *            the serviceId to set
	 */
	public void setServiceId(String serviceId)
	{
		this.serviceId = serviceId;
	}

	/**
	 * @return the variantId
	 */
	public String getVariantId()
	{
		return variantId;
	}

	/**
	 * @param variantId
	 *            the variantId to set
	 */
	public void setVariantId(String variantId)
	{
		this.variantId = variantId;
	}

	/**
	 * @return the benMsisdn
	 */
	public String getBenMsisdn()
	{
		return benMsisdn;
	}

	/**
	 * @param benMsisdn
	 *            the benMsisdn to set
	 */
	public void setBenMsisdn(String benMsisdn)
	{
		this.benMsisdn = benMsisdn;
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

	/**
	 * @return the quantity
	 */
	public String getQuantity()
	{
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(String quantity)
	{
		this.quantity = quantity;
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

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getCost()
	{
		return cost;
	}

	public void setCost(String cost)
	{
		this.cost = cost;
	}

	@Override
	public String toString()
	{
		JsonObject job = new JsonObject();
		Field[] fields = AddServiceQuotaFeedBack.class.getDeclaredFields();
		for (Field f : fields)
		{
			f.setAccessible(true);
			String value = "";
			try
			{
				Object objValue = f.get(this);
				if (objValue != null)
				{
					value = objValue.toString();
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
			}
			job.add(f.getName(), new JsonPrimitive(value));
		}

		return job.toString();
	}
}

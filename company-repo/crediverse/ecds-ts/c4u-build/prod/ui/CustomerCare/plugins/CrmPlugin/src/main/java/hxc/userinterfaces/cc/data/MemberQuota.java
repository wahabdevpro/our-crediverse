package hxc.userinterfaces.cc.data;

/**
 * Combination of ServiceQuota and Balance information for template
 */
public class MemberQuota
{
	private String quotaID;
	private String service;
	private String destination;
	private String daysOfWeek;
	private String timeOfDay;
	private long quantity;
	private String units;
	private String balance = "-";
	private String expDate = "-";

	public MemberQuota()
	{
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
	 * @return the daysOfWeek
	 */
	public String getDaysOfWeek()
	{
		return daysOfWeek;
	}

	/**
	 * @param daysOfWeek
	 *            the daysOfWeek to set
	 */
	public void setDaysOfWeek(String daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
	}

	/**
	 * @return the timeOfDay
	 */
	public String getTimeOfDay()
	{
		return timeOfDay;
	}

	/**
	 * @param timeOfDay
	 *            the timeOfDay to set
	 */
	public void setTimeOfDay(String timeOfDay)
	{
		this.timeOfDay = timeOfDay;
	}

	/**
	 * @return the quantity
	 */
	public long getQuantity()
	{
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(long quantity)
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
	 * @return the balance
	 */
	public String getBalance()
	{
		return balance;
	}

	/**
	 * @param balance
	 *            the balance to set
	 */
	public void setBalance(String balance)
	{
		this.balance = balance;
	}

	/**
	 * @return the expDate
	 */
	public String getExpDate()
	{
		return expDate;
	}

	/**
	 * @param expDate
	 *            the expDate to set
	 */
	public void setExpDate(String expDate)
	{
		this.expDate = expDate;
	}

	public String getQuotaID()
	{
		return quotaID;
	}

	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;
	}

}

package hxc.services.cmbk;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "CMBK_Record")
public class CallMeBackServiceRecord
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String variantID;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String subscriberMsisdn;

	@Column(nullable = false)
	protected long totalCount = 0L;

	@Column(nullable = false)
	protected Date dayStartTime;

	@Column(nullable = false)
	protected Date weekStartTime;

	@Column(nullable = false)
	protected Date monthStartTime;

	@Column(nullable = false)
	protected int dayCount = 0;

	@Column(nullable = false)
	protected int weekCount = 0;

	@Column(nullable = false)
	protected int monthCount = 0;

	@Column(nullable = false)
	protected boolean isNew = true;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	public String getSubscriberMsisdn()
	{
		return subscriberMsisdn;

	}

	public void setSubscriberMsisdn(String subscriberMsisdn)
	{
		this.subscriberMsisdn = subscriberMsisdn;
	}

	public void setTotalCount(long totalCount)
	{
		this.totalCount = totalCount;
	}

	public Date getDayStartTime()
	{
		return dayStartTime;
	}

	public void setDayStartTime(Date dayStartTime)
	{
		this.dayStartTime = dayStartTime;
	}

	public Date getWeekStartTime()
	{
		return weekStartTime;
	}

	public void setWeekStartTime(Date weekStartTime)
	{
		this.weekStartTime = weekStartTime;
	}

	public Date getMonthStartTime()
	{
		return monthStartTime;
	}

	public void setMonthStartTime(Date monthStartTime)
	{
		this.monthStartTime = monthStartTime;
	}

	public long getTotalCount()
	{
		return totalCount;
	}

	public int getDayCount()
	{
		return dayCount;
	}

	public void setDayCount(int dayCount)
	{
		this.dayCount = dayCount;
	}

	public int getWeekCount()
	{
		return weekCount;
	}

	public void setWeekCount(int weekCount)
	{
		this.weekCount = weekCount;
	}

	public int getMonthCount()
	{
		return monthCount;
	}

	public void setMonthCount(int monthCount)
	{
		this.monthCount = monthCount;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public CallMeBackServiceRecord(String serviceID, String variantID, String subscriberMsisdn, long totalCount, //
			Date dayStartTime, int dayCount, Date weekStartTime, int weekCount, Date monthStartTime, int monthCount)
	{
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.subscriberMsisdn = subscriberMsisdn;
		this.totalCount = totalCount;
		this.dayStartTime = dayStartTime;
		this.dayCount = dayCount;
		this.weekStartTime = weekStartTime;
		this.weekCount = weekCount;
		this.monthStartTime = monthStartTime;
		this.monthCount = monthCount;
	}

	public CallMeBackServiceRecord()
	{
	}

}

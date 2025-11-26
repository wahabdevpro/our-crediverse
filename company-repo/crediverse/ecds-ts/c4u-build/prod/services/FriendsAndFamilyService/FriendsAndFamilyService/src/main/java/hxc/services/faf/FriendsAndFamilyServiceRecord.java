package hxc.services.faf;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "FAF_Record")
public class FriendsAndFamilyServiceRecord
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String variantAction;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String subscriberMsisdn;

	@Column(nullable = false)
	protected long totalCount = 0L;

	@Column(nullable = false)
	protected int periodCount = 0;

	@Column(nullable = false)
	protected Date periodStartTime;

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

	public String getVariantAction()
	{
		return variantAction;
	}

	public void setVariantAction(String variantAction)
	{
		this.variantAction = variantAction;
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

	public Date getPeriodStartTime()
	{
		return periodStartTime;
	}

	public void setPeriodStartTime(Date periodStartTime)
	{
		this.periodStartTime = periodStartTime;
	}

	public long getTotalCount()
	{
		return totalCount;
	}

	public int getPeriodCount()
	{
		return periodCount;
	}

	public void setPeriodCount(int periodCount)
	{
		this.periodCount = periodCount;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public FriendsAndFamilyServiceRecord(String serviceID, String variantAction, String subscriberMsisdn, long totalCount, //
			Date periodStartTime, int periodCount)
	{
		this.serviceID = serviceID;
		this.variantAction = variantAction;
		this.subscriberMsisdn = subscriberMsisdn;
		this.totalCount = totalCount;
		this.periodStartTime = periodStartTime;
		this.periodCount = periodCount;
	}

	public FriendsAndFamilyServiceRecord()
	{
	}

}

package hxc.services.sharedaccounts;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "sa_activity")
public class Activity
{
	// //////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////
	@Column(primaryKey = true, maxLength = 28, nullable = false)
	public Date startTime;

	@Column(primaryKey = true)
	public int sequenceNo;

	public String a_MSISDN;

	public String b_MSISDN;

	@Column(maxLength = 64)
	public String serviceID;

	@Column(maxLength = 64)
	public String variantID;

	@Column(maxLength = 64)
	public String processID;

	@Column(maxLength = 35)
	private String quotaName;

	@Column(maxLength = 15)
	private String serviceType;

	private long quantity = 0;

	private long amount;

	// //////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// ////////////////////////////
	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	public int getSequenceNo()
	{
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo)
	{
		this.sequenceNo = sequenceNo;
	}

	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	public void setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
	}

	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	public void setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
	}

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

	public String getProcessID()
	{
		return processID;
	}

	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	public String getQuotaName()
	{
		return quotaName;
	}

	public void setQuotaName(String quotaName)
	{
		this.quotaName = quotaName;
	}

	public String getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(String serviceType)
	{
		this.serviceType = serviceType;
	}

	public long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(long quantity)
	{
		this.quantity = quantity;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////
	public Activity()
	{

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////////////

}

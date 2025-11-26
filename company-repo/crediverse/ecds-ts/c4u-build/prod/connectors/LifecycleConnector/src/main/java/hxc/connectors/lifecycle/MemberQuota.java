package hxc.connectors.lifecycle;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "lc_memberquota")
public class MemberQuota implements IMemberQuota
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdn;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = true)
	private String variantID;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String memberMsisdn;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String quotaID;

	@Column(nullable = true)
	private Long quantity;

	@Column(nullable = false)
	private Date DateTime1;

	@Column(nullable = false)
	private int state;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getMsisdn()
	{
		return msisdn;
	}

	@Override
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	@Override
	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	@Override
	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getMemberMsisdn()
	{
		return memberMsisdn;
	}

	@Override
	public void setMemberMsisdn(String memberMsisdn)
	{
		this.memberMsisdn = memberMsisdn;
	}

	@Override
	public String getQuotaID()
	{
		return quotaID;
	}

	@Override
	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;
	}

	@Override
	public Long getQuantity()
	{
		return quantity;
	}

	@Override
	public void setQuantity(Long quantity)
	{
		this.quantity = quantity;
	}

	@Override
	public Date getDateTime1()
	{
		return DateTime1;
	}

	@Override
	public void setDateTime1(Date dateTime1)
	{
		DateTime1 = dateTime1;
	}

	@Override
	public int getState()
	{
		return state;
	}

	@Override
	public void setState(int state)
	{
		this.state = state;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MemberQuota()
	{

	}

	public MemberQuota(IMemberQuota quota)
	{
		this.msisdn = quota.getMsisdn();
		this.serviceID = quota.getServiceID();
		this.variantID = quota.getVariantID();
		this.memberMsisdn = quota.getMemberMsisdn();
		this.quotaID = quota.getQuotaID();
		this.quantity = quota.getQuantity();
		this.DateTime1 = quota.getDateTime1();
		this.state = quota.getState();
	}

}

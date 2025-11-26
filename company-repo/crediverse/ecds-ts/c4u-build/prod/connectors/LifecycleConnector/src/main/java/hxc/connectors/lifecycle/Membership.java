package hxc.connectors.lifecycle;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "lc_member")
public class Membership
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

	@Column(nullable = false)
	private int serviceClass;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String memberMsisdn;

	@Column(nullable = false)
	private int memberServiceClass;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
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

	public String getMemberMsisdn()
	{
		return memberMsisdn;
	}

	public int getServiceClass()
	{
		return serviceClass;
	}

	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	public int getMemberServiceClass()
	{
		return memberServiceClass;
	}

	public void setMemberServiceClass(int memberServiceClass)
	{
		this.memberServiceClass = memberServiceClass;
	}

	public void setMemberMsisdn(String memberMsisdn)
	{
		this.memberMsisdn = memberMsisdn;
	}
}

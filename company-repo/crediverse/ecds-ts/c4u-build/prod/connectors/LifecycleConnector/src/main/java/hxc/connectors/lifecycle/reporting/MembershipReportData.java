package hxc.connectors.lifecycle.reporting;

public class MembershipReportData
{
	private String serviceID;
	private String variantID;
	private int serviceClass;
	private int memberServiceClass;
	private long members;

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

	public long getMembers()
	{
		return members;
	}

	public void setMembers(long members)
	{
		this.members = members;
	}

	public MembershipReportData(String serviceID, String variantID, int serviceClass, int memberServiceClass, long members)
	{
		super();
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.serviceClass = serviceClass;
		this.memberServiceClass = memberServiceClass;
		this.members = members;
	}

	public MembershipReportData()
	{
	}
}

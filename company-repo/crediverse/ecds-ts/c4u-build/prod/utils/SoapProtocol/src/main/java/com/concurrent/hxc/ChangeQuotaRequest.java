package com.concurrent.hxc;

public class ChangeQuotaRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number subscriberNumber;
	private Number memberNumber;
	private ServiceQuota oldQuota;
	private ServiceQuota newQuota;

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

	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	public Number getMemberNumber()
	{
		return memberNumber;
	}

	public void setMemberNumber(Number memberNumber)
	{
		this.memberNumber = memberNumber;
	}

	public ServiceQuota getOldQuota()
	{
		return oldQuota;
	}

	public void setOldQuota(ServiceQuota oldQuota)
	{
		this.oldQuota = oldQuota;
	}

	public ServiceQuota getNewQuota()
	{
		return newQuota;
	}

	public void setNewQuota(ServiceQuota newServiceQuota)
	{
		this.newQuota = newServiceQuota;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public ChangeQuotaRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public ChangeQuotaRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(ChangeQuotaRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		if (request.serviceID == null || request.serviceID.length() == 0)
			return "No ServiceID";

		if (request.oldQuota == null || request.newQuota == null)
			return "No Quota";

		problem = Number.validate(request.subscriberNumber);
		if (problem != null)
			return problem;

		problem = Number.validate(request.memberNumber);
		if (problem != null)
			return problem;

		return null;
	}

}

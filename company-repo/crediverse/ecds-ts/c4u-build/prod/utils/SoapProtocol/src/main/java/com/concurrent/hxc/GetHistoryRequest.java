package com.concurrent.hxc;

public class GetHistoryRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private Number a_Number;
	private Number b_Number;
	private int rowLimit = 10;
	private boolean inReverse = false;

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

	public Number getA_Number()
	{
		return a_Number;
	}

	public void setA_Number(Number a_Number)
	{
		this.a_Number = a_Number;
	}

	public Number getB_Number()
	{
		return b_Number;
	}

	public void setB_Number(Number b_Number)
	{
		this.b_Number = b_Number;
	}

	public int getRowLimit()
	{
		return rowLimit;
	}

	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}

	public boolean isInReverse()
	{
		return inReverse;
	}

	public void setInReverse(boolean inReverse)
	{
		this.inReverse = inReverse;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	/**
	 * Default Constructor
	 */
	public GetHistoryRequest()
	{

	}

	/**
	 * Copy Constructor
	 */
	public GetHistoryRequest(RequestHeader request)
	{
		super(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public static String validate(GetHistoryRequest request)
	{
		// Validate Header
		String problem = RequestHeader.validate(request);
		if (problem != null)
			return problem;

		return null;
	}

}

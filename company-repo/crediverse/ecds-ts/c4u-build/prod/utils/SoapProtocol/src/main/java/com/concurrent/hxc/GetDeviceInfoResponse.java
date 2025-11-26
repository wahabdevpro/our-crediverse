package com.concurrent.hxc;

public class GetDeviceInfoResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String msisdn;
	private String registrationShortCode;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getMSISDN()
	{
		return msisdn;
	}

	public void setMSISDN(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public String getRegistrationShortCode()
	{
		return registrationShortCode;
	}

	public void setRegistrationShortCode(String registrationShortCode)
	{
		this.registrationShortCode = registrationShortCode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	// Constructor from Request
	public GetDeviceInfoResponse(GetDeviceInfoRequest request)
	{
		super(request);
	}

}

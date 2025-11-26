package com.concurrent.hxc;

public class GetServicesResponse extends ResponseHeader
{
	/**
	 * Constructor from Request
	 */
	public GetServicesResponse(GetServicesRequest request)
	{
		super(request);
	}

	private VasServiceInfo[] serviceInfo;

	public VasServiceInfo[] getServiceInfo()
	{
		return serviceInfo;
	}

	public void setServiceInfo(VasServiceInfo[] serviceInfo)
	{
		this.serviceInfo = serviceInfo;
	}

}
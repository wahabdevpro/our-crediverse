package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetServiceResponse extends ResponseHeader implements IDeserialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private VasServiceInfo[] serviceInfo;
	
	private static final long serialVersionUID = 7548306178368833561L;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public VasServiceInfo[] getServiceInfo()
	{
		return serviceInfo;
	}

	public void setServiceInfo(VasServiceInfo[] serviceInfo)
	{
		this.serviceInfo = serviceInfo;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetServiceResponse(GetServiceRequest request)
	{
		super(request);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDeserialisable
	//
	// /////////////////////////////////
	
	@Override
	public void deserialise(ISerialiser serialiser)
	{
		super.deserialise(serialiser);
		serviceInfo = serialiser.getArray("serviceInfo", VasServiceInfo.class);
	}

}
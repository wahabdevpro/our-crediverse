package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetServicesResponse extends ResponseHeader implements
		IDeserialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public GetServicesResponse(GetServicesRequest request)
	{
		super(request);
	}

	public GetServicesResponse()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private VasServiceInfo[] serviceInfo;
	
	private static final long serialVersionUID = -7261274606735653624L;

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
	// IDeserialisable Implementation
	//
	// /////////////////////////////////

	@Override
	public void deserialise(ISerialiser serialiser)
	{
		super.deserialise(serialiser); 
		
		serviceInfo = serialiser.getArray("serviceInfo", VasServiceInfo.class);
		
	}

}
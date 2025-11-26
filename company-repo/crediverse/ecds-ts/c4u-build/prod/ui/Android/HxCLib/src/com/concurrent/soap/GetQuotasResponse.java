package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetQuotasResponse extends ResponseHeader implements IDeserialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ServiceQuota[] serviceQuotas;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public ServiceQuota[] getServiceQuotas()
	{
		return serviceQuotas;
	}

	public void setServiceQuotas(ServiceQuota[] serviceQuotas)
	{
		this.serviceQuotas = serviceQuotas;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	/**
	 * Constructor from Request
	 */
	public GetQuotasResponse(GetQuotasRequest request)
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
		serviceQuotas = serialiser.getArray("serviceQuotas", ServiceQuota.class);
	}

}

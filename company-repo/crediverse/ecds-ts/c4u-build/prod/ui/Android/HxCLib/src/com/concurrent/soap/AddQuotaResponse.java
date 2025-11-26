package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class AddQuotaResponse extends ResponseHeader implements IDeserialisable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ServiceQuota quota;
	
	private static final long serialVersionUID = -1512735973953210683L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public ServiceQuota getQuota()
	{
		return quota;
	}

	public void setQuota(ServiceQuota quota)
	{
		this.quota = quota;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	/**
	 * Constructor from Request
	 */
	public AddQuotaResponse(AddQuotaRequest request)
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
		quota = serialiser.getDeserialisable("quota", null);
	}

}

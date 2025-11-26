package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetOwnersResponse extends ResponseHeader implements IDeserialisable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number[] owners;

	private static final long serialVersionUID = -8234315204532791706L;
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Number[] getOwners()
	{
		return owners;
	}

	public void setOwners(Number[] owners)
	{
		this.owners = owners;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public GetOwnersResponse(GetOwnersRequest request)
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
		owners = serialiser.getArray("owners", Number.class);
	}

}
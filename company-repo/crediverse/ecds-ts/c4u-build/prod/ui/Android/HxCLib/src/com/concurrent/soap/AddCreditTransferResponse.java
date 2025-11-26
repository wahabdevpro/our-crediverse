package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class AddCreditTransferResponse extends ResponseHeader implements IDeserialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private boolean requiresPIN;
	
	private static final long serialVersionUID = -2292823460517988967L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public boolean getRequiresPIN()
	{
		return requiresPIN;
	}

	public void setRequiresPIN(boolean requiresPIN)
	{
		this.requiresPIN = requiresPIN;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	// Constructor from Request
	public AddCreditTransferResponse(AddCreditTransferRequest request)
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
		requiresPIN = serialiser.getBoolean("requiresPIN", false);
	}

}
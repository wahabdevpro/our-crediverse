package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetCreditTransfersResponse extends ResponseHeader implements IDeserialisable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private CreditTransfer[] transfers;
	
	private static final long serialVersionUID = 699550292861805818L;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public CreditTransfer[] getTransfers()
	{
		return transfers;
	}

	public void setTransfers(CreditTransfer[] transfers)
	{
		this.transfers = transfers;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	// Constructor from Request
	public GetCreditTransfersResponse(GetCreditTransfersRequest request)
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
		transfers = serialiser.getArray("transfers", CreditTransfer.class);
	}
	

}

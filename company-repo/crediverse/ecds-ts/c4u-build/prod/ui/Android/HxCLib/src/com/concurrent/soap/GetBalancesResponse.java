package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetBalancesResponse extends ResponseHeader implements IDeserialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ServiceBalance[] balances;

	private static final long serialVersionUID = 2772985822616007283L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public ServiceBalance[] getBalances()
	{
		return balances;
	}

	public void setBalances(ServiceBalance[] balances)
	{
		this.balances = balances;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public GetBalancesResponse(GetBalancesRequest request)
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
		balances = serialiser.getArray("balances", ServiceBalance.class);
	}

}

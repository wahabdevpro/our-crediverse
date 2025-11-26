package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class PingResponse extends PingRequest implements IDeserialisable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDeserialisable implementation
	//
	// /////////////////////////////////
	@Override
	public void deserialise(ISerialiser serialiser)
	{
		super.deserialise(serialiser);		
	}
	

}

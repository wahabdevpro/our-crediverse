package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ICallable;
import com.concurrent.util.ISerialiser;

public class PingRequest implements ICallable<PingResponse>, IDeserialisable
{
	private int seq;

	public int getSeq()
	{
		return seq;
	}

	public void setSeq(int seq)
	{
		this.seq = seq;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISerialisable implementation
	//
	// /////////////////////////////////
	@Override
	public void serialise(ISerialiser serialiser)
	{
		serialiser.add("seq", seq);
	}

	@Override
	public PingResponse deserialiseResponse(ISerialiser serialiser)
	{
		PingResponse result = new PingResponse();
		result.deserialise(serialiser);
		return result;
	}

	@Override
	public String getMethodID()
	{
		return "ping";
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISerialisable implementation
	//
	// /////////////////////////////////
	@Override
	public void deserialise(ISerialiser serialiser)
	{
		seq = serialiser.getInteger("seq", 0);		
	}
	
	

}

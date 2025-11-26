package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetReturnCodeTextResponse extends ResponseHeader implements IDeserialisable
{


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String returnCodeText;
	
	private static final long serialVersionUID = 7156073302982606914L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getReturnCodeText()
	{
		return returnCodeText;
	}

	public void setReturnCodeText(String returnCodeText)
	{
		this.returnCodeText = returnCodeText;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public GetReturnCodeTextResponse(GetReturnCodeTextRequest request)
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
		returnCodeText = serialiser.getString("returnCodeText", "Unknown");
	}
	
	

}
package hxc.services.caisim.engine.cai;

import hxc.utils.tcp.TcpResponse;

public class ReturnValue
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private TcpResponse resp = null;
	boolean successful = false;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	
	public ReturnValue()
	{
		this.successful = true;
	}
	
	public ReturnValue(TcpResponse resp)
	{
		this.resp = resp;
	}
	
	public ReturnValue(TcpResponse resp, boolean successful)
	{
		this.resp = resp;
		this.successful = successful;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public TcpResponse getTcpResponse()
	{
		return resp;
	}
	
	public boolean hasTcpResponse()
	{
		return resp != null;
	}
	
	public boolean isSuccessful()
	{
		return successful;
	}
}
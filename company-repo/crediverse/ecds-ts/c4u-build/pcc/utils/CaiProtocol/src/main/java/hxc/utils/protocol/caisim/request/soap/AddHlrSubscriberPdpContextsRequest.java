package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.PdpContexts;

public class AddHlrSubscriberPdpContextsRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private PdpContexts pdps = new PdpContexts();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public PdpContexts getPdpContexts()
	{
		return pdps;
	}

	public void setPdpContexts(PdpContexts pdps)
	{
		this.pdps = pdps;
	}
}

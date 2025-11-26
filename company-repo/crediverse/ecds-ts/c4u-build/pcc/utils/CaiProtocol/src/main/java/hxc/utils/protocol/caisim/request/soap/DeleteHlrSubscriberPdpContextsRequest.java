package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.DeletedPdpContexts;

public class DeleteHlrSubscriberPdpContextsRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private DeletedPdpContexts deletedPdps;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public DeletedPdpContexts getPdpContexts()
	{
		return deletedPdps;
	}

	public void setPdpContexts(DeletedPdpContexts deletedPdps)
	{
		this.deletedPdps = deletedPdps;
	}
}

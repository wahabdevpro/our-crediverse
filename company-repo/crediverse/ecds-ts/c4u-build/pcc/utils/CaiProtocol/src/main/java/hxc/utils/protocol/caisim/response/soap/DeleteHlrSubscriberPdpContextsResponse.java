package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.DeleteHlrSubscriberPdpContextsRequest;

@XmlType(name = "DeletedHlrSubscriberPdpContexts")
public class DeleteHlrSubscriberPdpContextsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public DeleteHlrSubscriberPdpContextsResponse(DeleteHlrSubscriberPdpContextsRequest request)
	{
		super(request);
	}
	
	public DeleteHlrSubscriberPdpContextsResponse()
	{
		
	}
}

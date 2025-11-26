package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.UpdateHlrSubscriberPdpContextsRequest;

@XmlType(name = "UpdateHlrSubscriberResponse")
public class UpdateHlrSubscriberPdpContextsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public UpdateHlrSubscriberPdpContextsResponse(UpdateHlrSubscriberPdpContextsRequest request)
	{
		super(request);
	}
	
	public UpdateHlrSubscriberPdpContextsResponse()
	{
		
	}
}

package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberPdpContextsRequest;

@XmlType(name = "AddHlrSubscriberPdpContextsResponse")
public class AddHlrSubscriberPdpContextsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public AddHlrSubscriberPdpContextsResponse(AddHlrSubscriberPdpContextsRequest request)
	{
		super(request);
	}
	
	public AddHlrSubscriberPdpContextsResponse()
	{
		
	}
}

package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberObrRequest;

@XmlType(name = "SetHlrSubscriberObrResponse")
public class SetHlrSubscriberObrResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SetHlrSubscriberObrResponse(SetHlrSubscriberObrRequest request)
	{
		super(request);
	}
	
	public SetHlrSubscriberObrResponse()
	{
		
	}
}

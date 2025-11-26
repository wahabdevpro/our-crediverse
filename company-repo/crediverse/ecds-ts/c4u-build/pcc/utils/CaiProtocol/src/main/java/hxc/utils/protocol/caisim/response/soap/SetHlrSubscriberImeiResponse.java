package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberImeiRequest;

@XmlType(name = "SetHlrSubscriberImeiResponse")
public class SetHlrSubscriberImeiResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SetHlrSubscriberImeiResponse(SetHlrSubscriberImeiRequest request)
	{
		super(request);
	}
	
	public SetHlrSubscriberImeiResponse()
	{
		
	}
}

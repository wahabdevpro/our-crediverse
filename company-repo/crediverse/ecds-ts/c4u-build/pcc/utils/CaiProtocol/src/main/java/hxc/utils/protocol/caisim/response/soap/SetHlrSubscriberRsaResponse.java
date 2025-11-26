package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberRsaRequest;

@XmlType(name = "SetHlrSubscriberRsaResponse")
public class SetHlrSubscriberRsaResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SetHlrSubscriberRsaResponse(SetHlrSubscriberRsaRequest request)
	{
		super(request);
	}
	
	public SetHlrSubscriberRsaResponse()
	{
		
	}
}

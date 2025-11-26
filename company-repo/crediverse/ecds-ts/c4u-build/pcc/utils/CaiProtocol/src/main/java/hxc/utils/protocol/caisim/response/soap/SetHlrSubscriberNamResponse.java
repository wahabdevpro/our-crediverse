package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberNamRequest;

@XmlType(name = "SetHlrSubscriberNamResponse")
public class SetHlrSubscriberNamResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SetHlrSubscriberNamResponse(SetHlrSubscriberNamRequest request)
	{
		super(request);
	}
	
	public SetHlrSubscriberNamResponse()
	{
		
	}
}

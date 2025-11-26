package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberPdpCpRequest;

@XmlType(name = "SetHlrSubscriberPdpCpResponse")
public class SetHlrSubscriberPdpCpResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SetHlrSubscriberPdpCpResponse(SetHlrSubscriberPdpCpRequest request)
	{
		super(request);
	}
	
	public SetHlrSubscriberPdpCpResponse()
	{
		
	}
}

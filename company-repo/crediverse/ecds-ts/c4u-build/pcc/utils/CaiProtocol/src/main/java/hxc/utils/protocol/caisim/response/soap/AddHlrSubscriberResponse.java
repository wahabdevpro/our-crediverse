package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberRequest;

@XmlType(name = "AddHlrSubscriberResponse")
public class AddHlrSubscriberResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public AddHlrSubscriberResponse(AddHlrSubscriberRequest request)
	{
		super(request);
	}
	
	public AddHlrSubscriberResponse()
	{
		
	}
}

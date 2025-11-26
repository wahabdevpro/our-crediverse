package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.AddSapcSubscriberRequest;

@XmlType(name = "AddSapcSubscriberResponse")
public class AddSapcSubscriberResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////

	public AddSapcSubscriberResponse(AddSapcSubscriberRequest request)
	{
		super(request);
	}

	public AddSapcSubscriberResponse()
	{

	}
}

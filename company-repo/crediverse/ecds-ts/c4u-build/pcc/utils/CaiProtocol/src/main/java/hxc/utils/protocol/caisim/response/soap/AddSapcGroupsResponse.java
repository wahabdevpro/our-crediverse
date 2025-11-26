package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.AddSapcGroupsRequest;

@XmlType(name = "AddSapcGroupsResponse")
public class AddSapcGroupsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public AddSapcGroupsResponse(AddSapcGroupsRequest request)
	{
		super(request);
	}

	public AddSapcGroupsResponse()
	{

	}
}

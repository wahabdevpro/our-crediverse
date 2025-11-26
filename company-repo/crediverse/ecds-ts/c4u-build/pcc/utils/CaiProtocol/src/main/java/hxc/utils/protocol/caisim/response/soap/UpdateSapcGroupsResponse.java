package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.UpdateSapcGroupsRequest;

@XmlType(name = "UpdateSapcGroupsResponse")
public class UpdateSapcGroupsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public UpdateSapcGroupsResponse(UpdateSapcGroupsRequest request)
	{
		super(request);
	}

	public UpdateSapcGroupsResponse()
	{

	}
}

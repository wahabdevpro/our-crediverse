package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.DeleteSapcGroupsRequest;

@XmlType(name = "DeleteSapcGroupsResponse")
public class DeleteSapcGroupsResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public DeleteSapcGroupsResponse(DeleteSapcGroupsRequest request)
	{
		super(request);
	}

	public DeleteSapcGroupsResponse()
	{

	}

}

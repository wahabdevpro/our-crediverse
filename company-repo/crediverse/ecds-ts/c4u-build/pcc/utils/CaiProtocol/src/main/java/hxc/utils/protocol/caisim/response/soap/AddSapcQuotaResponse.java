package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.AddSapcQuotaRequest;

@XmlType(name = "AddSapcQuotaResponse")
public class AddSapcQuotaResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public AddSapcQuotaResponse(AddSapcQuotaRequest request)
	{
		super(request);
	}
	
	public AddSapcQuotaResponse()
	{
		
	}
}

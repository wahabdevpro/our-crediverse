package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetSapcQuotaRequest;

@XmlType(name = "SetSapcQuotaResponse")
public class SetSapcQuotaResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public SetSapcQuotaResponse(SetSapcQuotaRequest request)
	{
		super(request);
	}
	
	public SetSapcQuotaResponse()
	{
		
	}
}

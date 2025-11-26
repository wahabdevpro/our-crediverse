package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.request.soap.SetSapcZainLteRequest;

@XmlType(name = "SetSapcZainLteResponse")
public class SetSapcZainLteResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public SetSapcZainLteResponse(SetSapcZainLteRequest request)
	{
		super(request);
	}
	
	public SetSapcZainLteResponse()
	{
		
	}
}

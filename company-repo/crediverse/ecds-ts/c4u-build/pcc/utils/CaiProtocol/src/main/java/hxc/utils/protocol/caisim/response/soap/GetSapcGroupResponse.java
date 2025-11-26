package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.request.soap.GetSapcGroupRequest;

@XmlType(name = "GetSapcGroupResponse")
public class GetSapcGroupResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String msisdn = new String();
	private SapcGroup group;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetSapcGroupResponse(GetSapcGroupRequest request)
	{
		super(request);
	}

	public GetSapcGroupResponse()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}
	
	public SapcGroup getGroup()
	{
		return group;
	}

	public void setGroup(SapcGroup group)
	{
		this.group = group;
	}
}

package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.SapcGroups;
import hxc.utils.protocol.caisim.request.soap.GetSapcSubscriberRequest;

@XmlType(name = "GetSapcSubscriberResponse")
public class GetSapcSubscriberResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String msisdn = new String();
	private SapcGroups groups = new SapcGroups();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public GetSapcSubscriberResponse(GetSapcSubscriberRequest request)
	{
		super(request);
	}

	public GetSapcSubscriberResponse()
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

	public SapcGroups getGroups()
	{
		return groups;
	}

	public void setGroups(SapcGroups groups)
	{
		this.groups = groups;
	}
}

package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.SapcGroups;

public class AddSapcSubscriberRequest extends SoapRequest
{	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private SapcGroups groups = new SapcGroups();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public SapcGroups getGroups()
	{
		return groups;
	}

	public void setGroups(SapcGroups groups)
	{
		this.groups = groups;
	}
}

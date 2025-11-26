package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.SapcGroupId;

public class GetSapcGroupRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private SapcGroupId group = new SapcGroupId();
		
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public SapcGroupId getGroup()
	{
		return group;
	}
	
	public void setGroup(SapcGroupId group)
	{
		this.group = group;
	}
}

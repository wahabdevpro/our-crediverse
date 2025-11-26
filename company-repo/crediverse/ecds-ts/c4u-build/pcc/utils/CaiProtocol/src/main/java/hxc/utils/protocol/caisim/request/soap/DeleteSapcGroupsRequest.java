package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.DeletedSapcGroups;

public class DeleteSapcGroupsRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private DeletedSapcGroups groups = new DeletedSapcGroups();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////

	public void setGroups(DeletedSapcGroups groups)
	{
		this.groups = groups;
	}
	
	public DeletedSapcGroups getGroups()
	{
		return groups;
	}
}

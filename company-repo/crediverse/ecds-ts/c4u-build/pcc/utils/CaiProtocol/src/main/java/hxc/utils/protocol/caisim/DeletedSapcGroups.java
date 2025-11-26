package hxc.utils.protocol.caisim;

/**
 * Represents a list of requests for deletion of SAPC groups.
 * SAPC Groups can be deleted by unique GROUP ID.
 * 
 * @author petar
 *
 */
public class DeletedSapcGroups
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String[] groupsIds = new String[0];
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public DeletedSapcGroups()
	{
		
	}
	
	public DeletedSapcGroups(String[] groupsIds)
	{
		this.groupsIds = groupsIds;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	public String[] getId()
	{
		return groupsIds;
	}

	public void setId(String[] groupsIds)
	{
		this.groupsIds = groupsIds;
	}
	
	int length()
	{
		return groupsIds.length;
	}
	
	String at(int idx)
	{
		return groupsIds[idx];
	}
}

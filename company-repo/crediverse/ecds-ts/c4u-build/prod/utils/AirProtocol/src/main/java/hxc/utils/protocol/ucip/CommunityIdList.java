package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * CommunityIdList
 * 
 * communityIdList represents the community the subscriber belongs to. The communityIdList is a list of communityID placed in an <array> with maximum 3 entries.
 */
public class CommunityIdList
{
	/*
	 * The communityID parameter contains identity of the community the subscriber belong to.
	 */
	@Air(Mandatory = true, Range = "1:9999999")
	public int communityID;

}

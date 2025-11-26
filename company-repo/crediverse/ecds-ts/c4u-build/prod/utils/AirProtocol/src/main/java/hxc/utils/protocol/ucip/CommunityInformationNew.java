package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * CommunityInformationNew
 * 
 * communityInformationCurrent represents the community the subscriber currently belongs to. communityInformationNew represents the community which is to be assigned to the subscriber. The
 * communityInformationCurrent and communityInformationNew are lists of communityID placed in an <array> with maximum 3 entries.
 */
public class CommunityInformationNew
{
	/*
	 * The communityID parameter contains identity of the community the subscriber belong to.
	 */
	@Air(Mandatory = true, Range = "1:9999999")
	public int communityID;

}

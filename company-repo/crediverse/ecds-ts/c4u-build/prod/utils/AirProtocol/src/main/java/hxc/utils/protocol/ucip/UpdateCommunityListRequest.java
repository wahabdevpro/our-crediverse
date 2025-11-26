package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateCommunityListRequest
 * 
 * The message UpdateCommunityList set or updates the list of communities which the account belong to. The complete list of community numbers must be given when changing communities. Example: The
 * subscriber has communities 3,10,5. Now 10 is removed and 5 changed to 7. The array below would look like: communityInformationCurrent: 3,10,5; communityInformationNew: 3,7.
 */
@XmlRpcMethod(name = "UpdateCommunityList")
public class UpdateCommunityListRequest
{
	public UpdateCommunityListRequestMember member;

	public UpdateCommunityListRequest()
	{
		member = new UpdateCommunityListRequestMember();
	}
}

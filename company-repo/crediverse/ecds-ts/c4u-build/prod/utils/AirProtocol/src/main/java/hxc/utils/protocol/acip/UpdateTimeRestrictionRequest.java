package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateTimeRestrictionRequest
 * 
 * This message handles both creation and updates to time restrictions. If a restriction id is given that does not exist the restriction will be created. If the corresponding restriction exists it
 * will be updated instead.
 */
@XmlRpcMethod(name = "UpdateTimeRestriction")
public class UpdateTimeRestrictionRequest
{
	public UpdateTimeRestrictionRequestMember member;

	public UpdateTimeRestrictionRequest()
	{
		member = new UpdateTimeRestrictionRequestMember();
	}
}

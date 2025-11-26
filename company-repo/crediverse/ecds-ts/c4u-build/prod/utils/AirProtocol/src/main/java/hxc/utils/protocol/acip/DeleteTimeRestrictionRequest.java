package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeleteTimeRestrictionRequest
 * 
 * This message removes any number of time restrictions. If no identifier is given all existing restrictions will be deleted.
 */
@XmlRpcMethod(name = "DeleteTimeRestriction")
public class DeleteTimeRestrictionRequest
{
	public DeleteTimeRestrictionRequestMember member;

	public DeleteTimeRestrictionRequest()
	{
		member = new DeleteTimeRestrictionRequestMember();
	}
}

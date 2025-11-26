package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetTimeRestrictionRequest
 * 
 * This message retrieves time restrictions. Any number of time restriction IDs can be specified for retrieval. If no IDs are requested all the time restriction will be returned.
 */
@XmlRpcMethod(name = "GetTimeRestriction")
public class GetTimeRestrictionRequest
{
	public GetTimeRestrictionRequestMember member;

	public GetTimeRestrictionRequest()
	{
		member = new GetTimeRestrictionRequestMember();
	}
}

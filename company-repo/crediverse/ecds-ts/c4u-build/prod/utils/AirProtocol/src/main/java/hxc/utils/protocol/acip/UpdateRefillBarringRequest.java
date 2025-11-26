package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateRefillBarringRequest
 * 
 * The message UpdateRefillBarring either bar or clear the subscriber when attempting refills. It is done by either increasing the subscriber's barring counter (making it more likely the subscriber
 * will be barred) or moving the unbar-date forward, or clearing the counter and resetting the barring date. When a subscriber's barring counter exceeds a configured value, when it is increased, the
 * subscriber will be barred.
 */
@XmlRpcMethod(name = "UpdateRefillBarring")
public class UpdateRefillBarringRequest
{
	public UpdateRefillBarringRequestMember member;

	public UpdateRefillBarringRequest()
	{
		member = new UpdateRefillBarringRequestMember();
	}
}

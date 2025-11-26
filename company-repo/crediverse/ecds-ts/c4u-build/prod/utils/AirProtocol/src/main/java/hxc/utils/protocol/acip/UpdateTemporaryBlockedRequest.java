package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateTemporaryBlockedRequest
 * 
 * The message UpdateTemporaryBlocked set or clear the temporary blocked status on a subscriber. When temporary block status is set for a subscriber, all updates of the account through that particular
 * subscriber is prevented. This means that temporary blocking one subscriber (independent if it is the master or subordinate) does not prevent the other subscribers to access and update the account
 * if they belong to the same account.
 */
@XmlRpcMethod(name = "UpdateTemporaryBlocked")
public class UpdateTemporaryBlockedRequest
{
	public UpdateTemporaryBlockedRequestMember member;

	public UpdateTemporaryBlockedRequest()
	{
		member = new UpdateTemporaryBlockedRequestMember();
	}
}

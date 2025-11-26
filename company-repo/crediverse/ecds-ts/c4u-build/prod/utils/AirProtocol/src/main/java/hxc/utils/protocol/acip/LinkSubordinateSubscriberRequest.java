package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * LinkSubordinateSubscriberRequest
 * 
 * The message LinkSubordinateSubscriber will link a previously installed subscriber to another subscriber's account.
 */
@XmlRpcMethod(name = "LinkSubordinateSubscriber")
public class LinkSubordinateSubscriberRequest
{
	public LinkSubordinateSubscriberRequestMember member;

	public LinkSubordinateSubscriberRequest()
	{
		member = new LinkSubordinateSubscriberRequestMember();
	}
}

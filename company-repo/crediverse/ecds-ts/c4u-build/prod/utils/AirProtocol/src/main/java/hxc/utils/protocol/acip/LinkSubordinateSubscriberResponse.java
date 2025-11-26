package hxc.utils.protocol.acip;

/**
 * LinkSubordinateSubscriberResponse
 * 
 * The message LinkSubordinateSubscriber will link a previously installed subscriber to another subscriber's account.
 */
public class LinkSubordinateSubscriberResponse
{
	public LinkSubordinateSubscriberResponseMember member;

	public LinkSubordinateSubscriberResponse()
	{
		member = new LinkSubordinateSubscriberResponseMember();
	}
}

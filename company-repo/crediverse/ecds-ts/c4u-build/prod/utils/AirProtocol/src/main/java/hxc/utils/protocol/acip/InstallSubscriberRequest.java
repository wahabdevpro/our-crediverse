package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * InstallSubscriberRequest
 * 
 * The message InstallSubscriber performs an installation of a subscriber with relevant account and subscriber data. A master subscription is created in an account database predefined in the system.
 * The master subscription can be changed to a subordinate subscription by using the LinkSubordinateSubscriber message.
 */
@XmlRpcMethod(name = "InstallSubscriber")
public class InstallSubscriberRequest
{
	public InstallSubscriberRequestMember member;

	public InstallSubscriberRequest()
	{
		member = new InstallSubscriberRequestMember();
	}
}

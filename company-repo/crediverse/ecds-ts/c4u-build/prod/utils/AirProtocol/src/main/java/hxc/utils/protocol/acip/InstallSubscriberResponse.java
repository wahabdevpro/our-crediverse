package hxc.utils.protocol.acip;

/**
 * InstallSubscriberResponse
 * 
 * The message InstallSubscriber performs an installation of a subscriber with relevant account and subscriber data. A master subscription is created in an account database predefined in the system.
 * The master subscription can be changed to a subordinate subscription by using the LinkSubordinateSubscriber message.
 */
public class InstallSubscriberResponse
{
	public InstallSubscriberResponseMember member;

	public InstallSubscriberResponse()
	{
		member = new InstallSubscriberResponseMember();
	}
}

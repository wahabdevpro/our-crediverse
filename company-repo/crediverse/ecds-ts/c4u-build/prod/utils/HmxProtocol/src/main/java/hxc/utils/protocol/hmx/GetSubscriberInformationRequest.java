package hxc.utils.protocol.hmx;

import hxc.utils.xmlrpc.XmlRpcMethod;

@XmlRpcMethod(name = "HxcMapXml:getSubscriberInformation")
public class GetSubscriberInformationRequest
{
	public GetSubscriberInformationMembers member;
}

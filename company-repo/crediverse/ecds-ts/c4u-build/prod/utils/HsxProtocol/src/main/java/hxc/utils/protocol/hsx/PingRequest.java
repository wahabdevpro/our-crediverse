package hxc.utils.protocol.hsx;

import hxc.utils.xmlrpc.XmlRpcMethod;

@XmlRpcMethod(name = "pingRequest")
public class PingRequest
{
	public int seq;
}

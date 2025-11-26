package hxc.utils.protocol.hsx0;

import hxc.utils.xmlrpc.XmlRpcMethod;

@XmlRpcMethod(name = "pingRequest")
public class PingRequest
{
	public int seq;
}

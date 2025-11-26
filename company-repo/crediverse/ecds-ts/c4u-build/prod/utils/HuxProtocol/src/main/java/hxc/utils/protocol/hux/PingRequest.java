package hxc.utils.protocol.hux;

import hxc.utils.xmlrpc.XmlRpcMethod;

@XmlRpcMethod(name = "pingRequest")
public class PingRequest
{
	public int seq;
}

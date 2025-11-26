package hxc.utils.protocol.hsx0;

import java.util.Date;

import hxc.utils.xmlrpc.XmlRpcFormat;

public class RequestHeader
{
	public String originSystemType;
	public String originHostName;
	public String originServiceName;
	public String originTransactionId;
	@XmlRpcFormat(format = "yyyyMMdd'T'HH:mm:ss")
	public Date originTimeStamp;
	public String originOperatorId;
	public String version;
}

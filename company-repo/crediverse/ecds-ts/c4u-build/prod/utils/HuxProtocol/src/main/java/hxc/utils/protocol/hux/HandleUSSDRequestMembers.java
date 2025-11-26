package hxc.utils.protocol.hux;

import java.util.Date;

import hxc.utils.xmlrpc.XmlRpcAsString;
import hxc.utils.xmlrpc.XmlRpcFormat;

public class HandleUSSDRequestMembers
{
	public String TransactionId;
	@XmlRpcFormat(format = "yyyyMMdd'T'HH:mm:ss")
	public Date TransactionTime;
	public String MSISDN;
	public String IMSI;
	public String USSDServiceCode;
	public String USSDRequestString;
	public String requestOriginInterface;
	@XmlRpcAsString
	public Boolean response;
	public Integer SessionId;
	public Integer Sequence;
	public String VLR;
	public CellGlobalId cellGlobalId;
	public hxc.utils.protocol.hsx.Number mscNumber;
	public hxc.utils.protocol.hsx.Number vlrNumber;

}

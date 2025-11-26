package hxc.utils.protocol.hux;

import java.util.Date;

import hxc.utils.protocol.hsx.EncodingSelection;
import hxc.utils.xmlrpc.XmlRpcAsString;
import hxc.utils.xmlrpc.XmlRpcFormat;

public class HandleUSSDResponseMembers
{
	public enum Actions
	{
		// Initiate an interactive USSD session
		request,

		// Send a non-interactive USSD message
		notify,

		// Return a message and close the USSD Session
		end
	}

	public String TransactionId;
	@XmlRpcFormat(format = "yyyyMMdd'T'HH:mm:ss")
	public Date TransactionTime;
	public String USSDResponseString;
	public String USSDEncoding;
	public EncodingSelection[] encodingSelection;
	@XmlRpcAsString
	public Actions action;
	public Integer ResponseCode;
}

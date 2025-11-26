package hxc.utils.protocol.hux;

import java.util.Date;

import hxc.utils.xmlrpc.XmlRpcFormat;

public class SendUSSDResponseMembers
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// This parameter is used to identify a request / response pair between the Signalling
	// Gateway and a third party application.
	// ABNF: 1*64( ALPHA / DIGIT / ":" / "_" / "-" / "." )
	// Mandatory
	public String TransactionId;

	// This parameter is the ISO 8601 time stamp of when the request was sent.
	// Mandatory
	@XmlRpcFormat(format = "yyyyMMdd'T'HH:mm:ss")
	public Date TransactionTime;

	// The MSISDN of the subscriber who sent the request, or to whom the request will be
	// delivered.
	// Mandatory
	public String MSISDN;

	// This parameter is the short code of the service application. It is received from the HLR
	// in a subscriber initiated request and should be specified in an Application Originating
	// request.
	// ABNF: 3*4DIGIT
	// Mandatory
	public String USSDServiceCode;

	// For an Application Originating message, this parameter is the message entered on the
	// handset by the subscriber.
	// Mandatory
	public String USSDResponseString;

	// This parameter is sent back after a message has been processed and indicates
	// completion status of the message.
	// Optional
	public Integer ResponseCode;
}

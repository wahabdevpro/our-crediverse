package hxc.utils.protocol.hux;

import hxc.utils.protocol.hsx.EncodingSelection;

public class SendUSSDRequestMembers
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String ACTION_REQUEST = "request"; // Interactive
	public static final String ACTION_NOTIFY = "notify"; // Non-Interactive

	public static final String RESPONSE_MODE_IMMEDIATE = "immediate";
	public static final String RESPONSE_MODE_RESPONSE = "response";

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

	// For an Application Originating message, this is the request string that will be send to the
	// subscriber.
	// ABNF: 1*182( OCTET )
	// Mandatory
	public String USSDRequestString;

	// Optional
	public EncodingSelection[] encodingSelection;

	// This indicates the action to be performed on the MAP session.
	// request Initiate an interactive USSD session (default).
	// notify Send a non-interactive USSD message.
	// Optional
	public String action;

	// This selects the response mode to be used for the request. This only applies to USSD
	// Notify messages (requests that have the action field set to notify). If this field is not
	// included in the request, the behavior will be controlled by value set in the configuration
	// file.
	//
	// immediate Respond to the sendUSSD request
	// immediately after sending the USSD
	// Notify MAP request (or in case of
	// immediate error).
	// response Respond to the sendUSSD reqest after
	// after the subscriber has accepted /
	// dismissed the notify message.
	//
	// Optional
	public String notifyResponseMode;

}

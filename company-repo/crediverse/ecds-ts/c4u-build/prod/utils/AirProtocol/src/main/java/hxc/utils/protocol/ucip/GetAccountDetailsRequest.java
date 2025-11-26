package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetAccountDetailsRequest
 * 
 * The GetAccountDetails message is used to obtain account information in order to validate and tailor the user communication. Information on subscriber and account level is returned in the message.
 * Information is only returned in case it has previously been set on the account. Example, serviceFeeExpiryDate is only returned if the account has been activated (and thus has been assigned an end
 * date for service fee). Note: If pre-activation is wanted then messageCapabilityFlag.accountActivationFlag should be included set to 1. Note: If the locationNumber is not found, the Visitor Location
 * Register (VLR) is returned.
 */
@XmlRpcMethod(name = "GetAccountDetails")
public class GetAccountDetailsRequest
{
	public GetAccountDetailsRequestMember member;

	public GetAccountDetailsRequest()
	{
		member = new GetAccountDetailsRequestMember();
	}
}

package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateAccountDetailsRequest
 * 
 * The message UpdateAccountDetails is used to update the account information. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
@XmlRpcMethod(name = "UpdateAccountDetails")
public class UpdateAccountDetailsRequest
{
	public UpdateAccountDetailsRequestMember member;

	public UpdateAccountDetailsRequest()
	{
		member = new UpdateAccountDetailsRequestMember();
	}
}

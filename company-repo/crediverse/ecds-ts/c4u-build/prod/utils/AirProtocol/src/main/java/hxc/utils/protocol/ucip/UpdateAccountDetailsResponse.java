package hxc.utils.protocol.ucip;

/**
 * UpdateAccountDetailsResponse
 * 
 * The message UpdateAccountDetails is used to update the account information. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
public class UpdateAccountDetailsResponse
{
	public UpdateAccountDetailsResponseMember member;

	public UpdateAccountDetailsResponse()
	{
		member = new UpdateAccountDetailsResponseMember();
	}
}

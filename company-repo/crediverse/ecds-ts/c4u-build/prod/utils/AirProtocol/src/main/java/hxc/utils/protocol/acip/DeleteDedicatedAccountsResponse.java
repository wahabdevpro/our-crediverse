package hxc.utils.protocol.acip;

/**
 * DeleteDedicatedAccountsResponse
 * 
 * This message is intended to remove one or more dedicated accounts identified by their dedicatedAccountID. If additional conditions need to be processed, the message offers the possibility to use
 * optional input parameters to be verified with the dedicated account (expiryDate) configuration. Note that for product private (instantiated) DA:s, that the DeleteDedicatedAcc ounts response will
 * not contain any of the capabilities which the DA share with the Offer.
 */
public class DeleteDedicatedAccountsResponse
{
	public DeleteDedicatedAccountsResponseMember member;

	public DeleteDedicatedAccountsResponse()
	{
		member = new DeleteDedicatedAccountsResponseMember();
	}
}

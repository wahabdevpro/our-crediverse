package hxc.utils.protocol.acip;

/**
 * DeleteOfferResponse
 * 
 * The message DeleteOffer is used to disconnect an offer assigned to an account.
 */
public class DeleteOfferResponse
{
	public DeleteOfferResponseMember member;

	public DeleteOfferResponse()
	{
		member = new DeleteOfferResponseMember();
	}
}

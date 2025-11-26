package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeleteOfferRequest
 * 
 * The message DeleteOffer is used to disconnect an offer assigned to an account.
 */
@XmlRpcMethod(name = "DeleteOffer")
public class DeleteOfferRequest
{
	public DeleteOfferRequestMember member;

	public DeleteOfferRequest()
	{
		member = new DeleteOfferRequestMember();
	}
}

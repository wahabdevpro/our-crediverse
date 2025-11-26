package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateOfferRequest
 * 
 * The UpdateOffer message will assign a new offer or update an existing offer to an account. If the UpdateOffer request is sent and the offerID is not found for the account, then the update request
 * is considered to be an assignment request. If the offer is configured to allow multiple products a new product for the specified offer will be assigned. The following principles apply when
 * assigning a new offer: * It is not allowed to have a start date (and time) beyond the expiry date (and time). * It is not allowed to have an expiry date (and time) set to an earlier date (and time)
 * than the current date (and time). * If no absolute or relative start date (and time) is provided, then no date (and time) will be assigned as offer start date (and time). * If no expiry date (or
 * expiry date and time) is provided, then an infinite expiry date is used which means that the offer never expires. The following principles apply when updating an offer: * An offer (except type
 * Timer) will be active if the start date has been reached and the expiry date is still in the future. * An offer of type Timer will only become active through triggering by a traffic event. A Timer
 * offer is always installed in a disabled state. * An offer will expire if the expiry date (or expiry date and time) is before the current date (and time). * It is not allowed to modify the start
 * date (or start date and time) of an active or enabled (in the case of type Timer) offer. * It is not allowed to modify the start date and time of an offer of type Timer if the start date and time
 * has already passed. * It is not allowed to modify the expiry date (or expiry date and time) to an earlier date (or date and time) than the current date (or date and time). * It is not allowed to
 * modify the expiry date (or expiry date and time) of an expired offer * It is not allowed to modify the start date (or start date and time) beyond the expiry date (or expiry date and time). When
 * doing an update, if a date (or date and time) is given in relative days (or days and time expressed in seconds), then the new date (or date and time) will be the current defined date (or date and
 * time) plus the relative days (or days and time expressed in seconds). This applies to both start date (or date and time) and expiry date (or date and time). The parameter offerProviderID states the
 * needed provider ID when creating a provider account offer. The parameter offerProviderID states the new provider ID when updating a provider account offer. Note: OfferType it is mandatory for Timer
 * Offer
 */
@XmlRpcMethod(name = "UpdateOffer")
public class UpdateOfferRequest
{
	public UpdateOfferRequestMember member;

	public UpdateOfferRequest()
	{
		member = new UpdateOfferRequestMember();
	}
}

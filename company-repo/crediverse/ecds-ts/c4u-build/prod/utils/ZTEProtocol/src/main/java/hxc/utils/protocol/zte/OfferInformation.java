package hxc.utils.protocol.zte;

import java.util.Date;

/**
 * OfferInformation
 * 
 * The struct offerInformation contains dates (or dates and time) for offers, and optionally the dedicated accounts assigned to each offer. The dedicated accounts will only be included if specifically
 * indicated with the requestDedicatedAccountDetailsFlag. The fafInformationList will only be included specifically indicated with the requestFafDetailsFlag. In case of removal of offers it contains
 * the dates (or dates and time) before the offers were removed. It is enclosed in a <struct> of its own. The structs are placed in an <array>
 */
public class OfferInformation
{
	public static final int OFFERTYPE_ACCOUNT = 0;
	public static final int OFFERTYPE_MULTI_USER_IDENTIFICATION = 1;
	public static final int OFFERTYPE_TIMER = 2;
	public static final int OFFERTYPE_PROVIDER_ACCOUNT = 3;
	public static final int OFFERTYPE_SHARED_ACCOUNT = 4;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	public int offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	public Date startDate;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	public Date expiryDate;

	/*
	 * The startDateTime parameter contains the start date and time for an offer.
	 */
	public Date startDateTime;

	/*
	 * The expiryDateTime parameter contains the expiry date and time.
	 */
	public Date expiryDateTime;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	public Integer pamServiceID;


	/*-
	 * The offerType parameter identifies the offer type.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	Account Offer (default value for responses)
	 * 1:	Multi User Identification Offer
	 * 2:	Timer
	 * 3:	Provider Account Offer
	 * 4:	Shared Account Offer
	 */
	public Integer offerType;

	/*-
	 * The offerState parameter specifies the actual offer state to return in a
	 * GetOfferrequest.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	Enabled offer state
	 * 1:	Disabled offer state
	 */
	public Integer offerState;

	/*
	 * The offerProviderID parameter contains the subscriber number as represented in the SDP database for the provider. This parameter is connected to a provider account offer.
	 */
	public String offerProviderID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	public Integer productID;
}

package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * OfferUpdateInformationList
 * 
 * The struct offerUpdateInformationList contains dates (or dates and time) for offers. Note: The same principles as for UpdateOffer applies. It is enclosed in a <struct> of its own. The structs are
 * placed in an <array>
 */
@Air(PC = "PC:09854")
public class OfferUpdateInformationList
{
	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDate;

	/*
	 * The startDateRelative parameter is used to make a relative adjustment to the current start date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(Range = "-999:-1,1:999")
	public Integer startDateRelative;

	/*
	 * The startPamPeriodIndicator parameter indicates the Periodic Account Management period when the offer and DA becomes valid. (PC:09847 start) If the offer has the capability of date and time the
	 * time stamp will default to 00:00:00.(PC:09847 end) (PC:09847 start) Update of the PAM period indicator on an existing offer will keep the current time stamp unchanged. Time stamp can however be
	 * changed by including currentTimeOffset (see Section 7.64 on page 141) (PC:09847 end) Element Value Range: 0 to 99 where 0 means that the start date of an offer or a DA is the current date.
	 */
	public Integer startPamPeriodIndicator;

	/*
	 * The currentTimeOffset parameter indicates whether the default time or current time should be used in time stamp.
	 */
	@Air(PC = "PC:09847", CAP = "CAP:6")
	public Boolean currentTimeOffset;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

	/*
	 * The expiryDateRelative parameter is used to make a relative adjustment to an expiry date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(PC = "PC:05114", Range = "-999:-1,1:999")
	public Integer expiryDateRelative;

	/*
	 * The expiryPamPeriodIndicator parameter indicates the Periodic Account Management period when the offer or DA becomes invalid. (PC:09847 start) When the offer has the capability of storing date
	 * and time the time stamp will be set to 00:00:00 or 23:59:59 depending on system configuration. (PC:09847 end)
	 */
	@Air(Range = "1:100,2147483647")
	public Integer expiryPamPeriodIndicator;

	/*
	 * The startDateTime parameter contains the start date and time for an offer.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDateTime;

	/*
	 * The startDateTimeRelative parameter is used to make a relative adjustment to the current start date and time for an offer. The adjustment can be positive or negative. It is expressed in number
	 * of seconds. The parameter may also be used to define start date and time for other entities depending on the context where it is used.
	 */
	@Air(Range = "-99999999:99999999")
	public Integer startDateTimeRelative;

	/*
	 * The expiryDateTime parameter contains the expiry date and time.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDateTime;

	/*
	 * The expiryDateTimeRelative parameter is used to make a relative adjustment to the current expiry date and time. The adjustment can be positive or negative. It is expressed in number of seconds.
	 * The parameter may also be used to define expiry date and time for other entities depending on the context where it is used.
	 */
	@Air(Range = "-99999999:99999999")
	public Integer expiryDateTimeRelative;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Range = "0:99")
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
	@Air(Range = "0:7")
	public Integer offerType;

	@Air(PC = "PC:09847", CAP = "CAP:6")
	public DedicatedAccountUpdateInformation[] dedicatedAccountUpdateInformation;

	/*-
	 * The updateAction parameter is used to indicate which action to take on
	 * the resource.
	 *
	 * Possible Values:
	 * ----------------
	 * EXPIRE:	Expire the resource immediately.
	 */
	@Air(PC = "PC:10355", CAP = "CAP:2")
	public String updateAction;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(PC = "PC:10355", CAP = "CAP:2", Range = "0:2147483647")
	public Integer productID;

}

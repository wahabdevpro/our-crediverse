package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * FafInformationList
 * 
 * The fafInformationList is a list of fafInformation placed in an <array>.
 */
public class FafInformationList
{
	/*
	 * The fafNumber parameter contains a Family and Friends number.
	 */
	@Air(Mandatory = true, Length = "1:81", Format = "Numeric")
	public String fafNumber;

	/*-
	 * This owner parameter is used to indicate if the data is attached to the account or subscriber.
	 *
	 * Possible Values:
	 * ----------------
	 * Subscriber:	The data is attached to a subscriber
	 * Account:	The data is attached to an Account.
	 */
	@Air(Mandatory = true)
	public String owner;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(PC = "PC:05114", Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

	/*
	 * The expiryDateRelative parameter is used to make a relative adjustment to an expiry date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(PC = "PC:05114", Range = "-999:-1,1:999")
	public Integer expiryDateRelative;

	/*
	 * The fafIndicator parameter is used for differentiated rating for traffic events to and from numbers in the Family and Friends (FaF) number list.
	 */
	@Air(Range = "1:65535")
	public Integer fafIndicator;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(PC = "PC:05114", Range = "1:2147483647")
	public Integer offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(PC = "PC:05114", Range = "DateMin:DateMax,DateBeginningOfTime")
	public Date startDate;

	/*
	 * The startDateRelative parameter is used to make a relative adjustment to the current start date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	@Air(PC = "PC:05114", Range = "-999:-1,1:999")
	public Integer startDateRelative;

	/*
	 * The exactMatch parameter indicating if the faf number should be matched partial or exact. If the parameter is not present the default behavior is to match partial.
	 */
	public Boolean exactMatch;

}

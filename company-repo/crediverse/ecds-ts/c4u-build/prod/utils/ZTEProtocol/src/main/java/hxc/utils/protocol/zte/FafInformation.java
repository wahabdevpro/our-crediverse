package hxc.utils.protocol.zte;

import java.util.Date;

/**
 * FafInformation
 * 
 * The fafInformation is enclosed in a <struct> of its own and contains information about family and friends function - to match UCIP
 */
public class FafInformation
{

	public enum FaFOwners
	{
		Subscriber, Account
	}

	/*
	 * The fafNumber parameter contains a Family and Friends number.
	 */
	public String fafNumber;

	/*-
	 * This owner parameter is used to indicate if the data is attached to the account or subscriber.
	 *
	 * Possible Values:
	 * ----------------
	 * Subscriber:	The data is attached to a subscriber
	 * Account:	The data is attached to an Account.
	 */
	public FaFOwners owner;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	public Date expiryDate;

	/*
	 * The expiryDateRelative parameter is used to make a relative adjustment to an expiry date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	public Integer expiryDateRelative;

	/*
	 * The fafIndicator parameter is used for differentiated rating for traffic events to and from numbers in the Family and Friends (FaF) number list.
	 */
	public Integer fafIndicator;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	public Integer offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	public Date startDate;

	/*
	 * The startDateRelative parameter is used to make a relative adjustment to the current start date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	public Integer startDateRelative;

	/*
	 * The exactMatch parameter indicating if the faf number should be matched partial or exact. If the parameter is not present the default behavior is to match partial.
	 */
	public Boolean exactMatch;
	
//	public String msisdn;
//	public String userPwd;
//	public String fellowISDN;
//	public String fellowType;
//	public long effType;
//	public XMLGregorianCalendar effDate;
//	public XMLGregorianCalendar expDate;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public FafInformation()
	{
	}

	public FafInformation(FafInformation fafEntry)
	{
		this.fafNumber = fafEntry.fafNumber;
		this.owner = fafEntry.owner;
		this.expiryDate = fafEntry.expiryDate;
		this.expiryDateRelative = fafEntry.expiryDateRelative;
		this.fafIndicator = fafEntry.fafIndicator;
		this.offerID = fafEntry.offerID;
		this.startDate = fafEntry.startDate;
		this.startDateRelative = fafEntry.startDateRelative;
		this.exactMatch = fafEntry.exactMatch;

//		this.msisdn = fafEntry.msisdn;
//		this.userPwd = fafEntry.userPwd;
//		this.fellowISDN = fafEntry.fellowISDN;
//		this.fellowType = fafEntry.fellowISDN;
//		this.effType = fafEntry.effType;
//		this.effDate = fafEntry.effDate;
//		this.expDate = fafEntry.expDate;
	}

}

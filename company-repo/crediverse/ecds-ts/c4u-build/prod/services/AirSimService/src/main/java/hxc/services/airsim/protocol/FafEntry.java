package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.xmlrpc.XmlRpcAsString;

public class FafEntry
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private enum FaFOwners
	{
		Subscriber, Account
	}

	/*
	 * The fafNumber parameter contains a Family and Friends number.
	 */
	private String fafNumber;

	/*-
	 * This owner parameter is used to indicate if the data is attached to the account or subscriber.
	 *
	 * Possible Values:
	 * ----------------
	 * Subscriber:	The data is attached to a subscriber
	 * Account:	The data is attached to an Account.
	 */
	@XmlRpcAsString
	private FaFOwners owner;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	private Date expiryDate;

	/*
	 * The expiryDateRelative parameter is used to make a relative adjustment to an expiry date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	private Integer expiryDateRelative;

	/*
	 * The fafIndicator parameter is used for differentiated rating for traffic events to and from numbers in the Family and Friends (FaF) number list.
	 */
	private Integer fafIndicator;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	private Integer offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	private Date startDate;

	/*
	 * The startDateRelative parameter is used to make a relative adjustment to the current start date. The adjustment can be positive or negative. It is expressed in number of days.
	 */
	private Integer startDateRelative;

	/*
	 * The exactMatch parameter indicating if the faf number should be matched partial or exact. If the parameter is not present the default behavior is to match partial.
	 */
	private Boolean exactMatch;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getFafNumber()
	{
		return fafNumber;
	}

	public void setFafNumber(String fafNumber)
	{
		this.fafNumber = fafNumber;
	}

	public FaFOwners getOwner()
	{
		return owner;
	}

	public void setOwner(FaFOwners owner)
	{
		this.owner = owner;
	}

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	public Integer getExpiryDateRelative()
	{
		return expiryDateRelative;
	}

	public void setExpiryDateRelative(Integer expiryDateRelative)
	{
		this.expiryDateRelative = expiryDateRelative;
	}

	public Integer getFafIndicator()
	{
		return fafIndicator;
	}

	public void setFafIndicator(Integer fafIndicator)
	{
		this.fafIndicator = fafIndicator;
	}

	public Integer getOfferID()
	{
		return offerID;
	}

	public void setOfferID(Integer offerID)
	{
		this.offerID = offerID;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Integer getStartDateRelative()
	{
		return startDateRelative;
	}

	public void setStartDateRelative(Integer startDateRelative)
	{
		this.startDateRelative = startDateRelative;
	}

	public Boolean getExactMatch()
	{
		return exactMatch;
	}

	public void setExactMatch(Boolean exactMatch)
	{
		this.exactMatch = exactMatch;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public FafEntry()
	{

	}

	public FafEntry(FafInformation fafInfo)
	{
		this.fafNumber = fafInfo.fafNumber;
		this.owner = Enum.valueOf(FaFOwners.class, fafInfo.owner.toString());
		this.expiryDate = fafInfo.expiryDate;
		this.expiryDateRelative = fafInfo.expiryDateRelative;
		this.fafIndicator = fafInfo.fafIndicator;
		this.offerID = fafInfo.offerID;
		this.startDate = fafInfo.startDate;
		this.startDateRelative = fafInfo.startDateRelative;
		this.exactMatch = fafInfo.exactMatch;
	}

	public void clone(FafInformation fafInfo)
	{
		fafInfo.owner = Enum.valueOf(FafInformation.FaFOwners.class, this.owner.toString());
		fafInfo.expiryDate = this.expiryDate;
		fafInfo.expiryDateRelative = this.expiryDateRelative;
		fafInfo.fafIndicator = this.fafIndicator;
		fafInfo.offerID = this.offerID;
		fafInfo.startDate = this.startDate;
		fafInfo.startDateRelative = this.startDateRelative;
		fafInfo.exactMatch = this.exactMatch;

	}

}

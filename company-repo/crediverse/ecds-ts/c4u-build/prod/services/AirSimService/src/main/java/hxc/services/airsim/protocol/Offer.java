package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.air.Air;

public class Offer
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	protected int offerID;

	/*
	 * The startDate parameter contains the date when a dedicated account, FaF entry or offer will be considered as active. The parameter may also be used to define start date for other entities
	 * depending on the context where it is used.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	protected Date startDate;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	protected Date expiryDate;

	/*
	 * The startDateTime parameter contains the start date and time for an offer.
	 */
	@Air(Range = "DateMin:DateMax,DateBeginningOfTime")
	protected Date startDateTime;

	/*
	 * The expiryDateTime parameter contains the expiry date and time.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	protected Date expiryDateTime;

	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Range = "0:99")
	protected Integer pamServiceID;

	// @Air(PC = "PC:05114")
	// public FafInformationList[] fafInformationList;

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
	protected Integer offerType;

	/*-
	 * The offerState parameter specifies the actual offer state to return in a
	 * GetOfferrequest.
	 *
	 * Possible Values:
	 * ----------------
	 * 0:	Enabled offer state
	 * 1:	Disabled offer state
	 */
	@Air(Range = "0:99")
	protected Integer offerState;

	/*
	 * The offerProviderID parameter contains the subscriber number as represented in the SDP database for the provider. This parameter is connected to a provider account offer.
	 */
	@Air(Length = "1:28", Format = "Numeric")
	protected String offerProviderID;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(Range = "0:2147483647")
	protected Integer productID;

	// public UsageCounterUsageThresholdInformation[] usageCounterUsageThresholdInformation;
	//
	// @Air(CAP = "CAP:1,CAP:16")
	// public AttributeInformationList[] attributeInformationList;
	//
	// @Air(PC = "PC:10803", CAP = "CAP:15")
	// public AggregatedOfferInformation aggregatedOfferInformation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getOfferID()
	{
		return offerID;
	}

	public void setOfferID(int offerID)
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

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	public Date getStartDateTime()
	{
		return startDateTime;
	}

	public void setStartDateTime(Date startDateTime)
	{
		this.startDateTime = startDateTime;
	}

	public Date getExpiryDateTime()
	{
		return expiryDateTime;
	}

	public void setExpiryDateTime(Date expiryDateTime)
	{
		this.expiryDateTime = expiryDateTime;
	}

	public Integer getPamServiceID()
	{
		return pamServiceID;
	}

	public void setPamServiceID(Integer pamServiceID)
	{
		this.pamServiceID = pamServiceID;
	}

	public Integer getOfferType()
	{
		return offerType;
	}

	public void setOfferType(Integer offerType)
	{
		this.offerType = offerType;
	}

	public Integer getOfferState()
	{
		return offerState;
	}

	public void setOfferState(Integer offerState)
	{
		this.offerState = offerState;
	}

	public String getOfferProviderID()
	{
		return offerProviderID;
	}

	public void setOfferProviderID(String offerProviderID)
	{
		this.offerProviderID = offerProviderID;
	}

	public Integer getProductID()
	{
		return productID;
	}

	public void setProductID(Integer productID)
	{
		this.productID = productID;
	}

}

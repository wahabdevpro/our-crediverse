package hxc.services.sharedaccounts;

import java.io.Serializable;

import hxc.configuration.Configurable;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;

@SuppressWarnings("serial")
@Configurable
public class Variant implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	private static final int FOREVER = 32000; // Days

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// ID
	private String variantID;

	// Name of the Shared Accounts Service Variant
	private Phrase names;

	// Validity Period of the Shared Accounts Service Variant in Days
	private int validityPeriodDays;

	// Hours before expiry to send a renewal warning notification, or 0 if not to be sent.
	private int firstRenewalWarningHoursBefore;
	private int secondRenewalWarningHoursBefore;

	// Subscription charge in USD
	private int subscriptionCharge;

	// Renewal charge in USD
	private int renewalCharge;

	// Subscription (Shared) OfferID which indicates that a subscriber is subscribed as a Provider to this variant of Shared Services. This is an Account OfferID on CS.
	private int subscriptionOfferID = 9999;

	// Consumer (Provider) OfferID which indicates that a subscriber is a registered Consumer of this variant of Shared Services. This is an Account OfferID on CS.
	private int consumerOfferID = 999900;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Phrase getNames()
	{
		return names;
	}

	public void setNames(Phrase name)
	{
		this.names = name;
	}

	public Integer getValidityPeriodDays()
	{
		return validityPeriodDays == FOREVER ? null : validityPeriodDays;
	}

	public int getSafeValidityPeriodDays()
	{
		return validityPeriodDays;
	}

	public void setValidityPeriodDays(Integer validityPeriodDays)
	{
		this.validityPeriodDays = validityPeriodDays == null ? FOREVER : validityPeriodDays;
	}

	public int getFirstRenewalWarningHoursBefore()
	{
		return firstRenewalWarningHoursBefore;
	}

	public void setFirstRenewalWarningHoursBefore(int firstRenewalWarningHoursBefore)
	{
		this.firstRenewalWarningHoursBefore = firstRenewalWarningHoursBefore;
	}

	public int getSecondRenewalWarningHoursBefore()
	{
		return secondRenewalWarningHoursBefore;
	}

	public void setSecondRenewalWarningHoursBefore(int secondRenewalWarningHoursBefore)
	{
		this.secondRenewalWarningHoursBefore = secondRenewalWarningHoursBefore;
	}

	public int getSubscriptionCharge()
	{
		return subscriptionCharge;
	}

	public void setSubscriptionCharge(int subscriptionCharge)
	{
		this.subscriptionCharge = subscriptionCharge;
	}

	public int getRenewalCharge()
	{
		return renewalCharge;
	}

	public void setRenewalCharge(int renewalCharge)
	{
		this.renewalCharge = renewalCharge;
	}

	public int getSubscriptionOfferID()
	{
		return subscriptionOfferID;
	}

	public void setSubscriptionOfferID(int subscriptionOfferID)
	{
		this.subscriptionOfferID = subscriptionOfferID;
	}

	public int getConsumerOfferID()
	{
		return consumerOfferID;
	}

	public void setConsumerOfferID(int consumerOfferID)
	{
		this.consumerOfferID = consumerOfferID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Variant()
	{
	}

	public Variant(String variantID, IPhrase names, Integer validityPeriodDays, int firstRenewalWarningHoursBefore, int secondRenewalWarningHoursBefore, int subscriptionCharge, int renewalCharge, //
			int subscriptionOfferID, int consumerOfferID)
	{
		super();
		this.variantID = variantID;
		this.names = new Phrase(names);
		this.setValidityPeriodDays(validityPeriodDays);
		this.firstRenewalWarningHoursBefore = firstRenewalWarningHoursBefore;
		this.secondRenewalWarningHoursBefore = secondRenewalWarningHoursBefore;
		this.subscriptionCharge = subscriptionCharge;
		this.renewalCharge = renewalCharge;
		this.subscriptionOfferID = subscriptionOfferID;
		this.consumerOfferID = consumerOfferID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return getVariantID();
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String toString(String languageCode)
	{
		return names.get(languageCode);
	}

	public boolean equals(String variantID)
	{
		if (variantID == null || variantID.length() == 0)
			return false;

		if (variantID.equals(this.variantID))
			return true;

		return names.matches(variantID);
	}

}

package hxc.services.advancedtransfer;

import java.io.Serializable;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.servicebus.ILocale;
import hxc.services.notification.Phrase;
import hxc.utils.calendar.TimeUnits;

@Configurable
@SuppressWarnings("serial")
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

	// ID of this Variant
	private String variantID;

	// Name of the Periodic Transfer Service Variant
	private Phrase name;

	// Validity Period of the Periodic Transfer Service Variant
	private int validityPeriod;

	// Validity Period Units of the Periodic Transfer Service Variant
	private TimeUnits validityPeriodUnit;

	// Hours before expiry to send a renewal warning notification, or 0 if not to be sent.
	private int firstRenewalWarningHoursBefore;
	private int secondRenewalWarningHoursBefore;

	// Subscription charge
	private long subscriptionCharge;

	// Renewal charge
	private long renewalCharge;

	// Is Recurring
	private boolean recurring;

	// Eligible Service Classes
	private Integer[] eligibleServiceClasses;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getVariantID()
	{
		return this.variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public Phrase getName()
	{
		return name;
	}

	public void setName(Phrase name)
	{
		this.name = name;
	}

	public Integer getValidityPeriod()
	{
		return validityPeriod == FOREVER ? null : validityPeriod;
	}

	public int getSafeValidityPeriod()
	{
		return validityPeriod;
	}

	public void setValidityPeriod(Integer validityPeriod)
	{
		this.validityPeriod = validityPeriod == null ? FOREVER : validityPeriod;
	}

	public TimeUnits getValidityPeriodUnit()
	{
		return validityPeriodUnit;
	}

	public void setValidityPeriodUnit(TimeUnits validityPeriodUnit)
	{
		this.validityPeriodUnit = validityPeriodUnit;
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

	@Config(description = "Subscription Charge", renderAs = Rendering.CURRENCY)
	public long getSubscriptionCharge()
	{
		return subscriptionCharge;
	}

	public void setSubscriptionCharge(long subscriptionCharge)
	{
		this.subscriptionCharge = subscriptionCharge;
	}

	@Config(description = "Renewal Charge", renderAs = Rendering.CURRENCY)
	public long getRenewalCharge()
	{
		return renewalCharge;
	}

	public void setRenewalCharge(long renewalCharge)
	{
		this.renewalCharge = renewalCharge;
	}

	public boolean isRecurring()
	{
		return recurring;
	}

	public void setRecurring(boolean recurring)
	{
		this.recurring = recurring;
	}

	public Integer[] getEligibleServiceClasses()
	{
		return eligibleServiceClasses;
	}

	public void setEligibleServiceClasses(Integer[] eligibleServiceClasses)
	{
		this.eligibleServiceClasses = eligibleServiceClasses;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Variant()
	{

	}

	public Variant(String variantID, Phrase name, //
			Integer validityPeriod, TimeUnits validityPeriodUnit, //
			int firstRenewalWarningHoursBefore, int secondRenewalWarningHoursBefore, //
			long subscriptionCharge, long renewalCharge, boolean recurring, Integer[] eligibleServiceClasses)
	{
		super();
		this.variantID = variantID;
		this.name = name;
		this.setValidityPeriod(validityPeriod);
		this.validityPeriodUnit = validityPeriodUnit;
		this.firstRenewalWarningHoursBefore = firstRenewalWarningHoursBefore;
		this.secondRenewalWarningHoursBefore = secondRenewalWarningHoursBefore;
		this.subscriptionCharge = subscriptionCharge;
		this.renewalCharge = renewalCharge;
		this.recurring = recurring;
		this.eligibleServiceClasses = eligibleServiceClasses;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public String toString(String languageCode)
	{
		return name.get(languageCode);
	}

	public boolean equals(String variantID)
	{
		if (variantID == null || variantID.length() == 0)
			return false;

		if (this.variantID.equalsIgnoreCase(variantID))
			return true;

		if (name.matches(variantID))
			return true;

		return false;
	}

	public boolean isEligibleFor(int serviceClassID)
	{
		if (eligibleServiceClasses == null)
			return false;
		for (int index = 0; index < eligibleServiceClasses.length; index++)
		{
			if (eligibleServiceClasses[index].equals(serviceClassID))
				return true;
		}
		return false;
	}

	public void validate(Variant[] variants, int[] serviceClasses, ILocale locale) throws ValidationException
	{
		ValidationException.notEmpty(this.variantID, "VariantID is empty");

		// Must be unique
		for (Variant variant : variants)
		{
			if (variant != this && variant.variantID.equalsIgnoreCase(this.variantID))
			{
				throw ValidationException.createFieldValidationException("variantID", String.format("VariantID %s is not unique", this.variantID));
			}
		}

		// Service class must exist
		for (Integer sc : eligibleServiceClasses)
		{
			ValidationException.doesContain(sc, serviceClasses, "eligibleServiceClasses", "Invalid variant service Class");
		}

		// Name of the Periodic Transfer Service Variant
		ValidationException.validate(name, locale, "name", "Invalid Name");

		// Validity Period of the Periodic Transfer Service Variant
		ValidationException.inRange(1, validityPeriod, FOREVER, "validityPeriod", "Invalid Validity Period");

		// Validity Period Units of the Periodic Transfer Service Variant
		ValidationException.isOneOff(validityPeriodUnit, "validityPeriodUnit", "Invalid Validity Period Units", TimeUnits.Days, TimeUnits.Weeks, TimeUnits.Months);

		// Hours before expiry to send a renewal warning notification, or 0 if not to be sent.
		ValidationException.inRange(0, firstRenewalWarningHoursBefore, 200, "firstRenewalWarningHoursBefore", "Invalid First Renewal Hours");
		ValidationException.inRange(0, secondRenewalWarningHoursBefore, 200, "secondRenewalWarningHoursBefore", "Invalid Second Renewal Hours");

		// Subscription charge
		if (subscriptionCharge < 0)
			throw ValidationException.createFieldValidationException("subscriptionCharge", "Invalid Subscription Charge");

		// Renewal charge
		if (renewalCharge < 0)
			throw ValidationException.createFieldValidationException("renewalCharge", "Invalid Renewal Charge");

		// Is Recurring
		if (!recurring && renewalCharge > 0)
			throw ValidationException.createFieldValidationException("renewalCharge", "Cannot have Renewal Charge");
	}

	public static String[] getVariantIDs(Variant[] variants)
	{
		String[] result = new String[variants.length];
		int index = 0;
		for (Variant variant : variants)
		{
			result[index++] = variant.variantID;
		}
		return result;
	}

}

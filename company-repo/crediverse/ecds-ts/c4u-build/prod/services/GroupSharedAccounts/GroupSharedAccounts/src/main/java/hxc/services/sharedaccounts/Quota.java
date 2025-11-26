package hxc.services.sharedaccounts;

import java.io.Serializable;

import com.concurrent.hxc.ServiceQuota;

import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;
import hxc.services.ServiceType;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;

@SuppressWarnings("serial")
@Configurable
public class Quota implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// ID
	private String quotaID;

	// Name.
	private Phrase name;

	// Service Type Name.
	private Phrase service;
	private ServiceType serviceType;

	// Destination Name.
	private Phrase destination;

	// Days of Week Name.
	private Phrase daysOfWeek;

	// Times of Day Name.
	private Phrase timeOfDay;

	// Service/Monetary Units Name.
	private Phrase unitName;

	// Quota Price per Unit in USD * 100 (i.e. 100th of a base unit)
	// When (priceCents == 100) => (cost == 0.01 USD) 
	private int priceCents = 100;

	// Sponsors's Quota OfferID which will be set for a Provider to be able to use his own shared bundle.
	// This is a Shared OfferID on CS.
	private int sponsorOfferID;

	private int sponsorUsageCounterID;

	// Beneficiary's Quota OfferID which will be set for a Consumer when he receives the quota. This is a Provider OfferID on CS.
	private int beneficiaryOfferID;

	// Quota Usage Counter which will accumulate the units of the Quota which have been consumed by the Consumer.
	private int beneficiaryUsageCounterID;

	// The Usage Threshold which contains the level at which a depletion notification will be sent by E///
	private int beneficiaryWarningUsageThresholdID;

	// Quota Usage Threshold which will contain the allotted units the Consumer is allowed to use.
	private int beneficiaryTotalThresholdID;

	// Level at which warning SM messages will be sent
	private int warningMargin;

	// Minimum Units which can be bought
	private int minUnits;

	// Maximum Units which can be bought
	private int maxUnits;

	// Unit Conversion Factor for CS
	private int csUnitConversionFactor;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase getName()
	{
		return name;
	}

	public void setName(Phrase name)
	{
		this.name = name;
	}

	public Phrase getService()
	{
		return service;
	}

	public void setService(Phrase service)
	{
		this.service = service;
	}

	public ServiceType getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType)
	{
		this.serviceType = serviceType;
	}

	public Phrase getDestination()
	{
		return destination;
	}

	public void setDestination(Phrase destination)
	{
		this.destination = destination;
	}

	public Phrase getDaysOfWeek()
	{
		return daysOfWeek;
	}

	public void setDaysOfWeek(Phrase daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
	}

	public Phrase getTimeOfDay()
	{
		return timeOfDay;
	}

	public void setTimeOfDay(Phrase timeOfDay)
	{
		this.timeOfDay = timeOfDay;
	}

	public Phrase getUnitName()
	{
		return unitName;
	}

	public void setUnitName(Phrase unitName)
	{
		this.unitName = unitName;
	}

	public int getPriceCents()
	{
		return priceCents;
	}

	public void setPriceCents(int priceCents)
	{
		this.priceCents = priceCents;
	}

	public int getSponsorOfferID()
	{
		return sponsorOfferID;
	}

	public void setSponsorOfferID(int sponsorOfferID)
	{
		this.sponsorOfferID = sponsorOfferID;
	}

	public int getSponsorUsageCounterID()
	{
		return sponsorUsageCounterID;
	}

	public void setSponsorUsageCounterID(int sponsorUsageCounterID)
	{
		this.sponsorUsageCounterID = sponsorUsageCounterID;
	}

	public int getBeneficiaryOfferID()
	{
		return beneficiaryOfferID;
	}

	public void setBeneficiaryOfferID(int beneficiaryOfferID)
	{
		this.beneficiaryOfferID = beneficiaryOfferID;
	}

	public int getBeneficiaryUsageCounterID()
	{
		return beneficiaryUsageCounterID;
	}

	public void setBeneficiaryUsageCounterID(int beneficiaryUsageCounterID)
	{
		this.beneficiaryUsageCounterID = beneficiaryUsageCounterID;
	}

	public int getBeneficiaryWarningUsageThresholdID()
	{
		return beneficiaryWarningUsageThresholdID;
	}

	public void setBeneficiaryWarningUsageThresholdID(int beneficiaryWarningUsageThresholdID)
	{
		this.beneficiaryWarningUsageThresholdID = beneficiaryWarningUsageThresholdID;
	}

	public int getBeneficiaryTotalThresholdID()
	{
		return beneficiaryTotalThresholdID;
	}

	public void setBeneficiaryTotalThresholdID(int beneficiaryTotalThresholdID)
	{
		this.beneficiaryTotalThresholdID = beneficiaryTotalThresholdID;
	}

	public int getWarningMargin()
	{
		return warningMargin;
	}

	public void setWarningMargin(int warningMargin)
	{
		this.warningMargin = warningMargin;
	}

	public int getMinUnits()
	{
		return minUnits;
	}

	public void setMinUnits(int minUnits)
	{
		this.minUnits = minUnits;
	}

	public int getMaxUnits()
	{
		return maxUnits;
	}

	public void setMaxUnits(int maxUnits)
	{
		this.maxUnits = maxUnits;
	}

	public int getUnitConversionFactor()
	{
		return csUnitConversionFactor;
	}

	public void setUnitConversionFactor(int csUnitConversionFactor)
	{
		this.csUnitConversionFactor = csUnitConversionFactor;
	}

	public String getQuotaID()
	{
		return quotaID;
	}

	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public Quota()
	{

	}

	public Quota(String quotaID, IPhrase name, IPhrase service, ServiceType serviceType, //
			IPhrase destination, IPhrase daysOfWeek, //
			IPhrase timeOfDay, IPhrase unitName, //
			int priceCents, //
			int sponsorOfferID, int sponsorUsageCounterID, //
			int beneficiaryOfferID, int beneficiaryUsageCounterID, int beneficiaryWarningUsageThresholdID, int beneficiaryTotalThresholdID, //
			int warningMargin, int minUnits, int maxUnits, int csUnitConversionFactor)
	{
		// 4500, 1001, 1000, 100100, 1001, 100101, 100102

		this.quotaID = quotaID;
		this.name = new Phrase(name);
		this.service = new Phrase(service);
		this.serviceType = serviceType;
		this.destination = new Phrase(destination);
		this.daysOfWeek = new Phrase(daysOfWeek);
		this.timeOfDay = new Phrase(timeOfDay);
		this.unitName = new Phrase(unitName);
		this.priceCents = priceCents;
		this.sponsorOfferID = sponsorOfferID;
		this.sponsorUsageCounterID = sponsorUsageCounterID;
		this.beneficiaryOfferID = beneficiaryOfferID;
		this.beneficiaryUsageCounterID = beneficiaryUsageCounterID;
		this.beneficiaryWarningUsageThresholdID = beneficiaryWarningUsageThresholdID;
		this.beneficiaryTotalThresholdID = beneficiaryTotalThresholdID;
		this.warningMargin = warningMargin;
		this.minUnits = minUnits;
		this.maxUnits = maxUnits;
		this.csUnitConversionFactor = csUnitConversionFactor;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public ServiceQuota toServiceQuota(String lanuageCode)
	{
		ServiceQuota result = new ServiceQuota();
		result.setQuotaID(getQuotaID());
		result.setName(name.getSafe(lanuageCode, ""));
		result.setService(service.getSafe(lanuageCode, ""));
		result.setDestination(destination.getSafe(lanuageCode, ""));
		result.setTimeOfDay(timeOfDay.getSafe(lanuageCode, ""));
		result.setDaysOfWeek(daysOfWeek.getSafe(lanuageCode, ""));
		result.setUnits(unitName.getSafe(lanuageCode, ""));
		return result;
	}

	public static Quota find(Quota[] quotas, ServiceQuota serviceQuota)
	{
		for (Quota quota : quotas)
		{
			// Match on ID
			if (notEmpty(serviceQuota.getQuotaID()) && serviceQuota.getQuotaID().equalsIgnoreCase(quota.getQuotaID()))
				return quota;

			// Match on Name
			if (notEmpty(serviceQuota.getName()) && quota.name.matches(serviceQuota.getName()))
				return quota;

			// Match on Fields
			if ((quota.service == null || quota.service.matches(serviceQuota.getService())) //
					&& (quota.destination == null || quota.destination.matches(serviceQuota.getDestination())) //
					&& (quota.timeOfDay == null || quota.timeOfDay.matches(serviceQuota.getTimeOfDay())) //
					&& (quota.daysOfWeek == null || quota.daysOfWeek.matches(serviceQuota.getDaysOfWeek())) //
			)
			{
				return quota;
			}

		}

		return null;
	}

	private static boolean notEmpty(String text)
	{
		return text != null && text.length() > 0;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public void validate(Quota[] quotas) throws ValidationException
	{
		if (serviceType.equals(ServiceType.AIRTIME) && priceCents != 100)
			throw new ValidationException("For AIRTIME Quotas, the Price must be 0.01");
		
		for (Quota other : quotas)
		{
			if (this == other)
				continue;

			// Test for Unique QuotaID
			if (this.quotaID.equalsIgnoreCase(other.quotaID))
				throw new ValidationException("Duplicate QuotaID");

			// Test for Unique Name
			if (this.name.overlaps(other.name))
				throw new ValidationException("Duplicate Name");

			// Test for Unique Sponsor Offer ID
			if (this.sponsorOfferID == other.sponsorOfferID)
				throw new ValidationException("Duplicate Sponsor Offer ID");

			// Test for Unique Sponsor Usage Counter ID
			if (this.sponsorUsageCounterID == other.sponsorUsageCounterID)
				throw new ValidationException("Duplicate Sponsor Usage Counter ID");

			// Test for Unique Beneficiary Offer ID
			if (this.beneficiaryOfferID == other.beneficiaryOfferID)
				throw new ValidationException("Duplicate Beneficiary Offer ID");

			// Test for Unique Beneficiary Usage Counter ID
			if (this.beneficiaryUsageCounterID == other.beneficiaryUsageCounterID)
				throw new ValidationException("Duplicate Beneficiary Usage Counter ID");

			// Test for Unique Beneficiary Warning Usage Threshold ID
			if (this.beneficiaryWarningUsageThresholdID == other.beneficiaryWarningUsageThresholdID)
				throw new ValidationException("Duplicate Beneficiary Warning Usage Threshold ID");

			// Test for Unique Beneficiary Total Threshold ID
			if (this.beneficiaryTotalThresholdID == other.beneficiaryTotalThresholdID)
				throw new ValidationException("Duplicate Unique Beneficiary Total Threshold ID");

		}

	}

}

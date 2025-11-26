package hxc.services.cmbk;

import hxc.configuration.Configurable;
import hxc.services.notification.Phrase;

@Configurable
public class Variant
{
	public static final String LOCAL_ONNET_VARIANT = "LOCAL_ONNET";
	public static final String LOCAL_OFFNET_VARIANT = "LOCAL_OFFNET";
	public static final String LOCAL_ALLNET_VARIANT = "LOCAL_ALLNET";
	public static final String INTERNATIONAL_VARIANT = "INTERNATIONAL";
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// ID of this Variant
	private String variantID;

	// Name of the CMBK Service Variant
	private Phrase name;

	// variant Type
	private String variantType = LOCAL_ALLNET_VARIANT;

	// Free Daily Requests
	private int freeDailyRequests = 5;

	// Maximum Daily Requests
	private int maxDailyRequests = 5;

	// Free Weekly Requests
	private int freeWeeklyRequests = -1;

	// Maximum Weekly Requests
	private int maxWeeklyRequests = -1;

	// Free Monthly Requests
	private int freeMonthlyRequests = -1;

	// Maximum Daily Requests
	private int maxMonthlyRequests = -1;

	// Local Charge
	private long charge = 0;

	// NumberPlans
	private String numberPlanString;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getVariantID()
	{
		return variantID;
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

	public String getVariantType()
	{
		return variantType;
	}

	public void setVariantType(String variantType)
	{
		if (variantType.compareTo(LOCAL_ONNET_VARIANT) == 0)
			this.variantType = variantType;
		else if (variantType.compareTo(LOCAL_OFFNET_VARIANT) == 0)
			this.variantType = variantType;
		else if (variantType.compareTo(LOCAL_ALLNET_VARIANT) == 0)
			this.variantType = variantType;
		else if (variantType.compareTo(INTERNATIONAL_VARIANT) == 0)
			this.variantType = variantType;
	}

	// Daily setters and getters
	public int getFreeDailyRequests()
	{
		return freeDailyRequests;
	}

	public void setFreeDailyRequests(int freeDailyRequests)
	{
		this.freeDailyRequests = freeDailyRequests;
	}

	public int getMaxDailyRequests()
	{
		return maxDailyRequests;
	}

	public void setMaxDailyRequests(int maxDailyRequests)
	{
		this.maxDailyRequests = maxDailyRequests;
	}

	// Weekly setters and getters
	public int getFreeWeeklyRequests()
	{
		return freeWeeklyRequests;
	}

	public void setWeeklyDailyRequests(int freeWeeklyRequests)
	{
		this.freeWeeklyRequests = freeWeeklyRequests;
	}

	public int getMaxWeeklyRequests()
	{
		return maxWeeklyRequests;
	}

	public void setMaxWeeklyRequests(int maxWeeklyRequests)
	{
		this.maxWeeklyRequests = maxWeeklyRequests;
	}

	// Monthly setters and getters
	public int getFreeMonthlyRequests()
	{
		return freeMonthlyRequests;
	}

	public void setFreeMonthlyRequests(int freeMonthlyRequests)
	{
		this.freeMonthlyRequests = freeMonthlyRequests;
	}

	public int getMaxMonthlyRequests()
	{
		return maxMonthlyRequests;
	}

	public void setMaxMonthlyRequests(int maxMonthlyRequests)
	{
		this.maxMonthlyRequests = maxMonthlyRequests;
	}

	public long getCharge()
	{
		return charge;
	}

	public void setCharge(long charge)
	{
		this.charge = charge;
	}

	public String getNumberPlanString()
	{
		return numberPlanString;
	}

	public void setNumberPlanString(String numberPlanString)
	{
		this.numberPlanString = numberPlanString;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Variant()
	{

	}

	public Variant(String variantID, Phrase name, String variantType, int freeDailyRequests, int maxDailyRequests, //
			int freeWeeklyRequests, int maxWeeklyRequests, int freeMonthlyRequests, int maxMonthlyRequests, //
			long charge, String defaultNumberPlan)
	{
		super();
		this.variantID = variantID;
		this.name = name;
		this.setVariantType(variantType);
		this.freeDailyRequests = freeDailyRequests;
		this.maxDailyRequests = maxDailyRequests;
		this.freeWeeklyRequests = freeWeeklyRequests;
		this.maxWeeklyRequests = maxWeeklyRequests;
		this.freeMonthlyRequests = freeMonthlyRequests;
		this.maxMonthlyRequests = maxMonthlyRequests;
		this.charge = charge;
		this.numberPlanString = defaultNumberPlan;
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

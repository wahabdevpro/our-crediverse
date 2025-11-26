package hxc.services.faf;

import hxc.configuration.Configurable;
import hxc.services.notification.Phrase;

@Configurable
public class Variant
{
	public static final String LOCAL_ONNET_VARIANT = "LOCAL_ONNET";
	public static final String LOCAL_OFFNET_VARIANT = "LOCAL_OFFNET";
	public static final String LOCAL_ALLNET_VARIANT = "LOCAL_ALLNET";
	public static final String INTERNATIONAL_VARIANT = "INTERNATIONAL";
	public static final String LIST_VARIANT = "LIST";
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// ID of this Variant
	private String variantID;

	// Name of the FAF Service Variant
	private Phrase name;

	// variant Type
	private String variantType = LOCAL_ALLNET_VARIANT;

	// Free Add Requests
	private int freeMonthlyAdd = 5;

	// Maximum Add Requests
	private int maxMonthlyAdd = 5;

	// Free Monthly Delete  Requests
	private int freeMonthlyDelete = 3;

	// Maximum Monthly Delete Requests
	private int maxMonthlyDelete = 3;

	// Change Charge
	private long chargeAdd = 0;
	private long chargeDel = 0;

	// Maximum Monthly Delete Requests
	private int maxVariantCount = 5;

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
		else if (variantType.compareTo(LIST_VARIANT) == 0)
			this.variantType = variantType;
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

	public Variant(String variantID, Phrase name, String variantType, //
			int freeMonthlyAdd, int maxMonthlyAdd, //
			int freeMonthlyDelete, int maxMonthlyDelete, //
			long chargeAdd, long chargeDel, int maxVariantCount, String defaultNumberPlan)
	{
		super();
		this.variantID = variantID;
		this.name = name;
		this.setVariantType(variantType);
		this.freeMonthlyAdd = freeMonthlyAdd;
		this.maxMonthlyAdd = maxMonthlyAdd;
		this.freeMonthlyDelete = freeMonthlyDelete;
		this.maxMonthlyDelete = maxMonthlyDelete;
		this.chargeAdd = chargeAdd;
		this.chargeDel = chargeDel;
		this.setMaxVariantCount(maxVariantCount);
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

	public int getFreeMonthlyAdd()
	{
		return freeMonthlyAdd;
	}

	public void setFreeMonthlyAdd(int freeMonthlyAdd)
	{
		this.freeMonthlyAdd = freeMonthlyAdd;
	}

	public int getMaxMonthlyAdd()
	{
		return maxMonthlyAdd;
	}

	public void setMaxMonthlyAdd(int maxMonthlyAdd)
	{
		this.maxMonthlyAdd = maxMonthlyAdd;
	}

	public int getFreeMonthlyDelete()
	{
		return freeMonthlyDelete;
	}

	public void setFreeMonthlyDelete(int freeMonthlyDelete)
	{
		this.freeMonthlyDelete = freeMonthlyDelete;
	}

	public int getMaxMonthlyDelete()
	{
		return maxMonthlyDelete;
	}

	public void setMaxMonthlyDelete(int maxMonthlyDelete)
	{
		this.maxMonthlyDelete = maxMonthlyDelete;
	}

	public long getChargeAdd()
	{
		return chargeAdd;
	}

	public void setChargeAdd(long chargeAdd)
	{
		this.chargeAdd = chargeAdd;
	}

	public long getChargeDel()
	{
		return chargeDel;
	}

	public void setChargeDel(long chargeDel)
	{
		this.chargeDel = chargeDel;
	}

	public int getMaxVariantCount()
	{
		return maxVariantCount;
	}

	public void setMaxVariantCount(int maxVariantCount)
	{
		this.maxVariantCount = maxVariantCount;
	}

}

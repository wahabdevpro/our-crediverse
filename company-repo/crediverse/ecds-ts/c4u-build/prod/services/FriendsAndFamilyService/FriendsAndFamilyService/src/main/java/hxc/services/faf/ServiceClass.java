package hxc.services.faf;

import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;
import hxc.servicebus.ILocale;
import hxc.services.notification.Phrase;

@Configurable
public class ServiceClass
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private int serviceClassID;

	// Names
	private Phrase name;

	// Post-Paid Flag
	private boolean postPaid;

	// Variants
	private String variantString;
	
	// Minimum Balance
	private long minBal = 0;
	// Maximum Balance
	private long maxBal = 0;

	// Free Daily List Requests
	private int freeDailyLists = 5;
	// Maximum Daily List Requests
	private int maxDailyLists = 5;
	// Maximum Daily List Requests

	private int maxListSize = 5;
	
	private long chargeList = 0;

	// subscription cost
	private long fafSubcriptionCost = 0;

	// Days of subscription length
	private int fafSubcriptionExpiry = 36500;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getServiceClassID()
	{
		return serviceClassID;
	}

	public void setServiceClassID(int serviceClassID)
	{
		this.serviceClassID = serviceClassID;
	}

	public Phrase getName()
	{
		return name;
	}

	public void setName(Phrase name)
	{
		this.name = name;
	}

	public boolean isPostPaid()
	{
		return postPaid;
	}

	public void setPostPaid(boolean postPaid)
	{
		this.postPaid = postPaid;
	}

	public String getVariantString()
	{
		return variantString;
	}

	public void setVariantString(String variantString)
	{
		this.variantString = variantString;
	}

	public long getMinBal()
	{
		return minBal;
	}

	public void setMinBal(long minBal)
	{
		this.minBal = minBal;
	}
	
	public long getMaxBal()
	{
		return maxBal;
	}

	public void setMaxBal(long maxBal)
	{
		this.maxBal = maxBal;
	}
	
	public int getFreeDailyLists()
	{
		return freeDailyLists;
	}

	public void setFreeDailyLists(int freeDailyLists)
	{
		this.freeDailyLists = freeDailyLists;
	}
	
	public int getMaxDailyLists()
	{
		return maxDailyLists;
	}

	public void setMaxDailyLists(int maxDailyLists)
	{
		this.maxDailyLists = maxDailyLists;
	}

	public int getMaxListSize()
	{
		return maxListSize;
	}

	public void setMaxListSize(int maxListSize)
	{
		this.maxListSize = maxListSize;
	}

	public long getChargeList()
	{
		return chargeList;
	}
	public void setChargeList(long chargeList)
	{
		this.chargeList = chargeList;
	}

	public long getFafSubcriptionCost()
	{
		return fafSubcriptionCost;
	}

	public void setFafSubcriptionCost(long fafSubcriptionCost)
	{
		this.fafSubcriptionCost = fafSubcriptionCost;
	}

	public int getFafSubcriptionExpiry()
	{
		return fafSubcriptionExpiry;
	}

	public void setFafSubcriptionExpiry(int fafSubcriptionExpiry)
	{
		this.fafSubcriptionExpiry = fafSubcriptionExpiry;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ServiceClass()
	{
	}

	public ServiceClass(int serviceClassID, Phrase name, boolean postPaid, String defaultVariants, Long minBal, Long maxBal, //
			int freeDailyLists, int maxDailyLists, int maxListSize, long chargeList, long fafSubcriptionCost)
	{
		super();
		this.serviceClassID = serviceClassID;
		this.name = name;
		this.postPaid = postPaid;
		this.setVariantString(defaultVariants);
		this.minBal = minBal;
		this.maxBal = maxBal;
		this.freeDailyLists = freeDailyLists;
		this.maxDailyLists = maxDailyLists;
		this.maxListSize = maxListSize;
		this.chargeList = chargeList;
		this.fafSubcriptionCost = fafSubcriptionCost;
	}

	public void validate(ServiceClass[] serviceClasses, ILocale locale) throws ValidationException
	{
		// Must be unique
		if (serviceClassID <= 0)
			throw ValidationException.createFieldValidationException("serviceClassID", "Invalid Service Class ID");

		for (ServiceClass serviceClass : serviceClasses)
		{
			if (serviceClass != this && serviceClass.serviceClassID == this.serviceClassID)
			{
				throw new ValidationException("ServiceClassID %s is not unique", this.serviceClassID);
			}
		}

		// Names
		ValidationException.validate(name, locale, "name", "Invalid Service Class Name");

		// Post-Paid Flag : true / false can't go wrong : nothing to test : move along

		// TODO Check Variants - can only be done by FAF parent
	}

	public static int[] getServiceClassIDs(ServiceClass[] serviceClasses)
	{
		int[] result = new int[serviceClasses.length];
		int index = 0;
		for (ServiceClass serviceClass : serviceClasses)
		{
			result[index++] = serviceClass.serviceClassID;
		}
		return result;
	}

	public String toString(String languageCode)
	{
		return name.get(languageCode);
	}

}

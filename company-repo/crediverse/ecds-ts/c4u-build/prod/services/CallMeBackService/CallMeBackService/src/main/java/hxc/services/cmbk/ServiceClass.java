package hxc.services.cmbk;

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ServiceClass()
	{
	}

	public ServiceClass(int serviceClassID, Phrase name, //
			boolean postPaid, String defaultVariants, Long minBal, Long maxBal)
	{
		super();
		this.serviceClassID = serviceClassID;
		this.name = name;
		this.postPaid = postPaid;
		this.setVariantString(defaultVariants);
		this.minBal = minBal;
		this.maxBal = maxBal;
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

		// TODO Check Variants - can only be done by CMBK parent
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

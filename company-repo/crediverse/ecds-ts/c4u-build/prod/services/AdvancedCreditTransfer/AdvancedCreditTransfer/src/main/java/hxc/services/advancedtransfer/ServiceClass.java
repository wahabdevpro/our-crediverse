package hxc.services.advancedtransfer;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.Rendering;
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

	// Maximum Recipients
	private int maxRecipients = 10;

	// Recipient Addition Charge
	private long addRecipientCharge;

	// Recipient Removal Charge
	private long removeRecipientCharge;

	// Transfer Addition Charge
	private long addTransferCharge;

	// Transfer Change Charge
	private long changeTransferCharge;

	// Transfer Removal Charge
	private long removeTransferCharge;

	// Transfer Suspension Charge
	private long suspendTransferCharge;

	// Transfer Resumption Charge
	private long resumeTransferCharge;

	// Un-Subscription Fee
	private long unsubscribeCharge;

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

	public int getMaxRecipients()
	{
		return maxRecipients;
	}

	public void setMaxRecipients(int maxRecipients)
	{
		this.maxRecipients = maxRecipients;
	}

	@Config(description = "Add Recipient Charge", renderAs = Rendering.CURRENCY)
	public long getAddRecipientCharge()
	{
		return addRecipientCharge;
	}

	public void setAddRecipientCharge(long addRecipientCharge)
	{
		this.addRecipientCharge = addRecipientCharge;
	}

	@Config(description = "Remove Recipient Charge", renderAs = Rendering.CURRENCY)
	public long getRemoveRecipientCharge()
	{
		return removeRecipientCharge;
	}

	public void setRemoveRecipientCharge(long removeRecipientCharge)
	{
		this.removeRecipientCharge = removeRecipientCharge;
	}

	@Config(description = "Add Transfer Charge", renderAs = Rendering.CURRENCY)
	public long getAddTransferCharge()
	{
		return addTransferCharge;
	}

	public void setAddTransferCharge(long addTransferCharge)
	{
		this.addTransferCharge = addTransferCharge;
	}

	@Config(description = "Add Transfer Charge", renderAs = Rendering.CURRENCY)
	public long getChangeTransferCharge()
	{
		return changeTransferCharge;
	}

	public void setChangeTransferCharge(long changeTransferCharge)
	{
		this.changeTransferCharge = changeTransferCharge;
	}

	@Config(description = "Remove Transfer Charge", renderAs = Rendering.CURRENCY)
	public long getRemoveTransferCharge()
	{
		return removeTransferCharge;
	}

	public void setRemoveTransferCharge(long removeTransferCharge)
	{
		this.removeTransferCharge = removeTransferCharge;
	}

	@Config(description = "Suspend Charge", renderAs = Rendering.CURRENCY)
	public long getSuspendTransferCharge()
	{
		return suspendTransferCharge;
	}

	public void setSuspendTransferCharge(long suspendTransferCharge)
	{
		this.suspendTransferCharge = suspendTransferCharge;
	}

	@Config(description = "Resume Charge", renderAs = Rendering.CURRENCY)
	public long getResumeTransferCharge()
	{
		return resumeTransferCharge;
	}

	public void setResumeTransferCharge(long resumeTransferCharge)
	{
		this.resumeTransferCharge = resumeTransferCharge;
	}

	@Config(description = "Unsubscribe Charge", renderAs = Rendering.CURRENCY)
	public long getUnsubscribeCharge()
	{
		return unsubscribeCharge;
	}

	public void setUnsubscribeCharge(long unsubscribeCharge)
	{
		this.unsubscribeCharge = unsubscribeCharge;
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
			boolean postPaid, int maxRecipients, //
			int addRecipientCharge, int removeRecipientCharge, int addTransferCharge, int removeTransferCharge, //
			int suspendTransferCharge, int resumeTransferCharge, int unsubscribeCharge)
	{
		super();
		this.serviceClassID = serviceClassID;
		this.name = name;
		this.postPaid = postPaid;
		this.maxRecipients = maxRecipients;
		this.addRecipientCharge = addRecipientCharge;
		this.removeRecipientCharge = removeRecipientCharge;
		this.removeTransferCharge = removeTransferCharge;
		this.suspendTransferCharge = suspendTransferCharge;
		this.resumeTransferCharge = resumeTransferCharge;
		this.unsubscribeCharge = unsubscribeCharge;
		this.addTransferCharge = addTransferCharge;
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

		// Post-Paid Flag

		// Maximum Recipients
		ValidationException.inRange(1, maxRecipients, 100, "maxRecipients", "Invalid Maximum Recipients");

		// Recipient Addition Charge
		if (addRecipientCharge < 0)
			throw ValidationException.createFieldValidationException("addRecipientCharge", "Invalid Add Recipient Charge");

		// Recipient Removal Charge
		if (removeRecipientCharge < 0)
			throw ValidationException.createFieldValidationException("removeRecipientCharge", "Invalid Remove Recipient Charge");

		// Transfer Addition Charge
		if (addTransferCharge < 0)
			throw ValidationException.createFieldValidationException("addTransferCharge", "Invalid Add Transfer Charge");

		// Transfer Change Charge
		if (changeTransferCharge < 0)
			throw ValidationException.createFieldValidationException("changeTransferCharge", "Invalid Change Transfer Charge");

		// Transfer Removal Charge
		if (removeTransferCharge < 0)
			throw ValidationException.createFieldValidationException("removeTransferCharge", "Invalid Remove Transfer Charge");

		// Transfer Suspension Charge
		if (suspendTransferCharge < 0)
			throw ValidationException.createFieldValidationException("suspendTransferCharge", "Invalid Suspend Transfer Charge");

		// Transfer Resumption Charge
		if (resumeTransferCharge < 0)
			throw ValidationException.createFieldValidationException("resumeTransferCharge", "Invalid Resume Transfer Charge");

		// Un-Subscription Fee
		if (unsubscribeCharge < 0)
			throw ValidationException.createFieldValidationException("unsubscribeCharge", "Invalid Unsubscribe Charge");
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

}

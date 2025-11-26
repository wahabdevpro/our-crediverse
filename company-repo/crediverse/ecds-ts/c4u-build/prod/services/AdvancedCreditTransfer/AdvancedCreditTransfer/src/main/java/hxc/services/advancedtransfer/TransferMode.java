package hxc.services.advancedtransfer;

import com.concurrent.hxc.CreditTransferType;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.servicebus.ILocale;
import hxc.services.notification.Phrase;
import hxc.utils.calendar.TimeUnits;
import hxc.utils.string.StringUtils;

@Configurable
public class TransferMode
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int MAX_TRANSFERMODEID_LEN = 20;


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private String transferModeID;
	private long donorUnitsDisplayConversion; // Scaled SCALE_DENOMINATOR
	private long recipientUnitsDisplayConversion; // Scaled SCALE_DENOMINATOR
	private long conversionRate = SCALE_DENOMINATOR; // Scaled SCALE_DENOMINATOR, e.g. SCALE_DENOMINATOR => 1:1
	private Phrase name;
	private CreditTransferType transferType;
	private boolean topUpOnly;
	private int donorAccountID;
	private int donorAccountType;
	private long donorMinBalance;
	private long donorMaxBalance;
	private int recipientAccountID;
	private int recipientAccountType;
	private long recipientMinBalance;
	private long recipientMaxBalance;
	private Integer recipientExpiryDays;
	private int interval = 1;
	private TimeUnits intervalType;
	private Phrase units;
	private int thresholdID;
	private long minAmount;
	private long maxAmount;
	private long commissionAmount;
	private long commissionPercentage; // Scaled 10^4 = 100%
	private boolean requiresPIN;
	private int retryIntervalMinutes = 60;
	private int maxRetries = 1;
	private boolean autoSuspend = false;
	private Integer[] validDonorServiceClasses;
	private Integer[] validRecipientServiceClasses;
	private String[] requiredSubscriptionVariants;
	private String blackListedPSOBits = "";
	private Long maxAmountPerPeriod = null;
	private Integer maxCountPerPeriod = null;

	public static final long SCALE_DENOMINATOR = 640000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getTransferModeID()
	{
		return transferModeID;
	}

	public void setTransferModeID(String transferModeID)
	{
		this.transferModeID = transferModeID;
	}

	public Phrase getName()
	{
		return name;
	}

	public void setName(Phrase name)
	{
		this.name = name;
	}

	public CreditTransferType getTransferType()
	{
		return transferType;
	}

	public void setTransferType(CreditTransferType transferType)
	{
		this.transferType = transferType;
	}

	public boolean isTopUpOnly()
	{
		return topUpOnly;
	}

	public void setTopUpOnly(boolean topUpOnly)
	{
		this.topUpOnly = topUpOnly;
	}

	public int getDonorAccountID()
	{
		return donorAccountID;
	}

	public void setDonorAccountID(int donorAccountID)
	{
		this.donorAccountID = donorAccountID;
	}

	public int getDonorAccountType()
	{
		return donorAccountType;
	}

	public void setDonorAccountType(int donorAccountType)
	{
		this.donorAccountType = donorAccountType;
	}

	@Config(description = "", hidden = true)
	public long getDonorMinBalance()
	{
		return donorMinBalance;
	}

	public void setDonorMinBalance(long donorMinBalance)
	{
		this.donorMinBalance = donorMinBalance;
	}

	@Config(description = "", hidden = true)
	public long getDonorMaxBalance()
	{
		return donorMaxBalance;
	}

	public void setDonorMaxBalance(long donorMaxBalance)
	{
		this.donorMaxBalance = donorMaxBalance;
	}

	public int getRecipientAccountID()
	{
		return recipientAccountID;
	}

	public void setRecipientAccountID(int recipientAccountID)
	{
		this.recipientAccountID = recipientAccountID;
	}

	public int getRecipientAccountType()
	{
		return recipientAccountType;
	}

	public void setRecipientAccountType(int recipientAccountType)
	{
		this.recipientAccountType = recipientAccountType;
	}

	@Config(description = "", hidden = true)
	public long getRecipientMinBalance()
	{
		return recipientMinBalance;
	}

	public void setRecipientMinBalance(long recipientMinBalance)
	{
		this.recipientMinBalance = recipientMinBalance;
	}

	@Config(description = "", hidden = true)
	public long getRecipientMaxBalance()
	{
		return recipientMaxBalance;
	}

	public void setRecipientMaxBalance(long recipientMaxBalance)
	{
		this.recipientMaxBalance = recipientMaxBalance;
	}

	@Config(description = "Benefit Validity Period")
	public Integer getRecipientExpiryDays()
	{
		return recipientExpiryDays;
	}

	public void setRecipientExpiryDays(Integer recipientExpiryDays)
	{
		this.recipientExpiryDays = recipientExpiryDays;
	}

	public Phrase getUnits()
	{
		return units;
	}

	public void setUnits(Phrase units)
	{
		this.units = units;
	}

	public long getDonorUnitsDisplayConversion()
	{
		return donorUnitsDisplayConversion;
	}

	public void setDonorUnitsDisplayConversion(long donorUnitsDisplayConversion)
	{
		this.donorUnitsDisplayConversion = donorUnitsDisplayConversion;
	}

	public long getRecipientUnitsDisplayConversion()
	{
		return recipientUnitsDisplayConversion;
	}

	public void setRecipientUnitsDisplayConversion(long recipientUnitsDisplayConversion)
	{
		this.recipientUnitsDisplayConversion = recipientUnitsDisplayConversion;
	}

	public long getConversionRate()
	{
		return conversionRate;
	}

	public void setConversionRate(long conversionRate)
	{
		this.conversionRate = conversionRate;
	}

	@Config(description = "", hidden = true)
	public long getMinAmount()
	{
		return minAmount;
	}

	public void setMinAmount(long minAmount)
	{
		this.minAmount = minAmount;
	}

	@Config(description = "", hidden = true)
	public long getMaxAmount()
	{
		return maxAmount;
	}

	public void setMaxAmount(long maxAmount)
	{
		this.maxAmount = maxAmount;
	}

	public int getInterval()
	{
		return interval;
	}

	public void setInterval(int interval)
	{
		this.interval = interval;
	}

	public TimeUnits getIntervalType()
	{
		return intervalType;
	}

	public void setIntervalType(TimeUnits intervalType)
	{
		this.intervalType = intervalType;
	}

	@Config(description = "Commission Amount", renderAs = Rendering.CURRENCY)
	public long getCommissionAmount()
	{
		return commissionAmount;
	}

	public void setCommissionAmount(long commissionAmount)
	{
		this.commissionAmount = commissionAmount;
	}

	@Config(description = "Commission Percentage", scaleFactor = 100, decimalDigitsToDisplay = 2)
	public long getCommissionPercentage()
	{
		return commissionPercentage;
	}

	public void setCommissionPercentage(long commissionPercentage)
	{
		this.commissionPercentage = commissionPercentage;
	}

	public Integer[] getValidDonorServiceClasses()
	{
		return validDonorServiceClasses;
	}

	public void setValidDonorServiceClasses(Integer[] validDonorServiceClasses)
	{
		this.validDonorServiceClasses = validDonorServiceClasses;
	}

	public Integer[] getValidRecipientServiceClasses()
	{
		return validRecipientServiceClasses;
	}

	public void setValidRecipientServiceClasses(Integer[] validRecipientServiceClasses)
	{
		this.validRecipientServiceClasses = validRecipientServiceClasses;
	}

	public String[] getRequiredSubscriptionVariants()
	{
		return requiredSubscriptionVariants;
	}

	public void setRequiredSubscriptionVariants(String[] requiredSubscriptionVariants)
	{
		this.requiredSubscriptionVariants = requiredSubscriptionVariants;
	}

	public int getThresholdID()
	{
		return thresholdID;
	}

	public void setThresholdID(int thresholdID)
	{
		this.thresholdID = thresholdID;
	}

	public boolean getRequiresPIN()
	{
		return requiresPIN;
	}

	public void setRequiresPIN(boolean requiresPIN)
	{
		this.requiresPIN = requiresPIN;
	}

	public int getRetryIntervalMinutes()
	{
		return retryIntervalMinutes;
	}

	public void setRetryIntervalMinutes(int retryIntervalMinutes)
	{
		this.retryIntervalMinutes = retryIntervalMinutes;
	}

	public int getMaxRetries()
	{
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries)
	{
		this.maxRetries = maxRetries;
	}

	public boolean isAutoSuspend()
	{
		return autoSuspend;
	}

	public void setAutoSuspend(boolean autoSuspend)
	{
		this.autoSuspend = autoSuspend;
	}

	public Long getMaxAmountPerPeriod()
	{
		return maxAmountPerPeriod;
	}

	public void setMaxAmountPerPeriod(Long maxAmountPerPeriod)
	{
		this.maxAmountPerPeriod = maxAmountPerPeriod;
	}

	public Integer getMaxCountPerPeriod()
	{
		return maxCountPerPeriod;
	}

	public void setMaxCountPerPeriod(Integer maxCountPerPeriod)
	{
		this.maxCountPerPeriod = maxCountPerPeriod;
	}

	@Config(description = "", hidden = true)
	public boolean isUponDepletion()
	{
		return transferType == CreditTransferType.UponDepletion;
	}

	@Config(description = "", hidden = true)
	public boolean isPeriodic()
	{
		return transferType == CreditTransferType.Periodic;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI Properties (Helper properties for pre-scaling values for the GUI)
	//
	// /////////////////////////////////
	public String getDonorMinBalanceUI()
	{
		return StringUtils.formatScaled(getDonorMinBalance(), donorUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setDonorMinBalanceUI(String quantity)
	{
		setDonorMinBalance(StringUtils.parseScaled(quantity, donorUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getDonorMaxBalanceUI()
	{
		return StringUtils.formatScaled(getDonorMaxBalance(), donorUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setDonorMaxBalanceUI(String quantity)
	{
		setDonorMaxBalance(StringUtils.parseScaled(quantity, donorUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getRecipientMinBalanceUI()
	{
		return StringUtils.formatScaled(getRecipientMinBalance(), recipientUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setRecipientMinBalanceUI(String quantity)
	{
		setRecipientMinBalance(StringUtils.parseScaled(quantity, recipientUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getRecipientMaxBalanceUI()
	{
		return StringUtils.formatScaled(getRecipientMaxBalance(), recipientUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setRecipientMaxBalanceUI(String quantity)
	{
		setRecipientMaxBalance(StringUtils.parseScaled(quantity, recipientUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getMinAmountUI()
	{
		return StringUtils.formatScaled(getMinAmount(), recipientUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setMinAmountUI(String quantity)
	{
		setMinAmount(StringUtils.parseScaled(quantity, recipientUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getMaxAmountUI()
	{
		return StringUtils.formatScaled(getMaxAmount(), recipientUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public void setMaxAmountUI(String quantity)
	{
		setMaxAmount(StringUtils.parseScaled(quantity, recipientUnitsDisplayConversion, SCALE_DENOMINATOR));
	}

	public String getBlackListedPSOBits()
	{
		return blackListedPSOBits;
	}

	public void setBlackListedPSOBits(String blackListedPSOBits)
	{
		this.blackListedPSOBits = blackListedPSOBits;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransferMode()
	{

	}

	public TransferMode(String transferModeID, CreditTransferType transferType, boolean topUpOnly, boolean requiresPIN, //
			Phrase name, Phrase units, int donorAccountID, int donorAccountType, long donorMinBalance, long donorMaxBalance, //
			int recipientAccountID, int recipientAccountType, long recipientMinBalance, long recipientMaxBalance, Integer recipientExpiryDays, //
			int interval, TimeUnits intervalType, //
			int donorUnitsDisplayConversion, long recipientUnitsDisplayConversion, long conversionRate, //
			long minAmount, long maxAmount, long commissionAmount, long commissionPercentage, int thresholdID, //
			Integer[] validDonorServiceClasses, Integer[] validRecipientServiceClasses, String[] requiredSubscriptionVariants, //
			String blackListedPSOBits, Long maxAmountPerPeriod, Integer maxCountPerPeriod)
	{
		this.name = name;
		this.units = units;
		this.donorUnitsDisplayConversion = donorUnitsDisplayConversion;
		this.recipientUnitsDisplayConversion = recipientUnitsDisplayConversion;
		this.transferModeID = transferModeID;
		this.transferType = transferType;
		this.topUpOnly = topUpOnly;
		this.donorAccountID = donorAccountID;
		this.donorAccountType = donorAccountType;
		this.donorMinBalance = donorMinBalance;
		this.donorMaxBalance = donorMaxBalance;
		this.recipientAccountID = recipientAccountID;
		this.recipientAccountType = recipientAccountType;
		this.recipientMinBalance = recipientMinBalance;
		this.recipientMaxBalance = recipientMaxBalance;
		this.recipientExpiryDays = recipientExpiryDays;
		this.conversionRate = conversionRate;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.recipientExpiryDays = recipientExpiryDays;
		this.intervalType = intervalType;
		this.interval = interval;
		this.commissionAmount = commissionAmount;
		this.commissionPercentage = commissionPercentage;
		this.thresholdID = thresholdID;
		this.validDonorServiceClasses = validDonorServiceClasses;
		this.validRecipientServiceClasses = validRecipientServiceClasses;
		this.requiredSubscriptionVariants = requiredSubscriptionVariants;
		this.requiresPIN = requiresPIN;
		this.blackListedPSOBits = blackListedPSOBits;
		this.maxAmountPerPeriod = maxAmountPerPeriod;
		this.maxCountPerPeriod = maxCountPerPeriod;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unused")
	private static boolean notEmpty(String text)
	{
		return text != null && text.length() > 0;
	}

	public boolean isValidDonorServiceClass(int serviceClass)
	{
		for (int sc : validDonorServiceClasses)
		{
			if (sc == serviceClass)
				return true;
		}
		return false;
	}

	public boolean isValidRecipientServiceClass(int serviceClass)
	{
		for (int sc : validRecipientServiceClasses)
		{
			if (sc == serviceClass)
				return true;
		}
		return false;
	}

	public boolean equals(String transferMode)
	{
		if (transferMode == null || transferMode.length() == 0)
			return false;

		// Remove () if present
		int p = transferMode.indexOf(" (");
		if (p > 0)
			transferMode = transferMode.substring(0, p);

		if (transferMode.equalsIgnoreCase(transferModeID))
			return true;

		if (name.matches(transferMode))
			return true;

		return false;
	}

	public static TransferMode findByID(TransferMode[] transferModes, String transferModeID)
	{
		for (TransferMode transferMode : transferModes)
		{
			if (transferMode.getTransferModeID().equalsIgnoreCase(transferModeID))
				return transferMode;
		}

		return null;
	}

	public void validate(TransferMode[] transferModes, int[] serviceClassIDs, String[] variantIDs, ILocale locale) throws ValidationException
	{
		// Must be unique
		ValidationException.notEmpty(transferModeID, "Inavlid Transfer Mode ID");
		if (transferModeID.length() < 1 || transferModeID.length() > MAX_TRANSFERMODEID_LEN)
			throw new ValidationException("Invalid Transaction Mode ID");

		for (TransferMode transferMode : transferModes)
		{
			if (transferMode != this && transferMode.transferModeID.equalsIgnoreCase(this.transferModeID))
			{
				throw ValidationException.createFieldValidationException("transferModeID", String.format("TransferModeID %s is not unique", this.transferModeID));
			}
		}

		ValidationException.isOneOff(transferType, "transferType", "Invalid Transfer Mode Type", //
				CreditTransferType.OnceOff, CreditTransferType.Periodic, CreditTransferType.UponDepletion);

		ValidationException.inRange(0, donorAccountID, 99999, "donorAccountID", "Invalid Donor Account ID. Valid Range between 0 and 99999");
		if (donorAccountID == 0)
			ValidationException.isOneOff(donorAccountType, "donorAccountType", "Invalid Donor Account Type", 1);
		else
			ValidationException.isOneOff(donorAccountType, "donorAccountType", "Invalid Donor Account Type", 0, 1, 5, 6);

		if (donorMinBalance >= donorMaxBalance)
			throw ValidationException.createFieldValidationException("donorMinBalance", "Invalid Donor Min/Max Balance. Donor Max Balance must be Greater than Donor Min Balance");

		ValidationException.inRange(0, recipientAccountID, 99999, "recipientAccountID", "Invalid Recipient Account ID. Valid Range between 0 and 99999");

		if (recipientAccountID == 0)
			ValidationException.isOneOff(recipientAccountType, "recipientAccountType", "Invalid Recipient Account Type", 1);
		else
			ValidationException.isOneOff(recipientAccountType, "recipientAccountType", "Invalid Recipient Account Type", 0, 1, 5, 6);

		if (recipientMinBalance >= recipientMaxBalance)
			throw ValidationException.createFieldValidationException("recipientMinBalance", "Invalid Recipient Min/Max Balance. Recipient Max Balance must be Greater than Recipient Min Balance");

		if (recipientExpiryDays != null && recipientExpiryDays <= 0)
			throw ValidationException.createFieldValidationException("recipientExpiryDays", "Invalid Recipient Benefit Expiry Days. Must be greater than 0.");

		ValidationException.inRange(0, retryIntervalMinutes, 60 * 24 * 7, "retryIntervalMinutes", "Invalid Retry Interval. Valid Range between 0 and 10 080.");

		ValidationException.inRange(0, maxRetries, 24, "maxRetries", "Invalid Maximum Retries. Valid Range between 0 and 24.");

		if (maxRetries == 0 && retryIntervalMinutes > 0)
			throw ValidationException.createFieldValidationException("retryIntervalMinutes", "Retry Interval must be set to 0 if no retries are required");

		ValidationException.inRange(1, interval, 600, "interval", "Invalid Interval");
		ValidationException.isOneOff(intervalType, "Invalid Interval Type", "intervalType", TimeUnits.Days, TimeUnits.Weeks, TimeUnits.Months);

		ValidationException.inRange(1L, donorUnitsDisplayConversion, Long.MAX_VALUE, "donorUnitsDisplayConversion", "Invalid Donor Units Display Conversion");
		ValidationException.inRange(1L, recipientUnitsDisplayConversion, Long.MAX_VALUE, "recipientUnitsDisplayConversion", "Invalid Recipient Units Display Conversion");

		if (conversionRate <= 0)
			throw ValidationException.createFieldValidationException("conversionRate", "Invalid Conversion Rate");

		ValidationException.inRange(0, thresholdID, 16, "thresholdID", "Invalid Threshold ID");

		if (thresholdID > 0 && transferType != CreditTransferType.UponDepletion || //
				thresholdID <= 0 && transferType == CreditTransferType.UponDepletion)
		{
			throw ValidationException.createFieldValidationException("thresholdID", "Invalid Threshold ID for Transfer Type");
		}

		if (minAmount < 1)
			throw ValidationException.createFieldValidationException("minAmount", "Min Transfer Amount needs to be Greater than 0 after it has been Converted");

		if (maxAmount <= minAmount)
			throw ValidationException.createFieldValidationException("maxAmount", "Max Transfer Amount needs to be Greater than Min Transfer Amount");

		long sourceAmount = TransferMode.unScale(minAmount * conversionRate);
		if (sourceAmount <= 0)
			throw ValidationException.createFieldValidationException("minAmount", "Minimum Amount too small");

		long commission = commissionAmount + (sourceAmount * commissionPercentage + 5000) / 10000;
		long charge = commission;
		boolean isFromMain = donorAccountID == 0;
		if (isFromMain)
			sourceAmount += charge;
		if (sourceAmount > donorMaxBalance)
			throw ValidationException.createFieldValidationException("donorMaxBalance", "Minimum Amount too large or Donor Max Balance too small");

		if (commissionAmount < 0 || commissionPercentage < 0 || commissionPercentage >= 20000)
			throw new ValidationException("Invalid Commission");

		for (int sc : validDonorServiceClasses)
		{
			ValidationException.doesContain(sc, serviceClassIDs, "validDonorServiceClasses", "Invalid Donor Service Class");
		}

		for (int sc : validRecipientServiceClasses)
		{
			ValidationException.doesContain(sc, serviceClassIDs, "validRecipientServiceClasses", "Invalid Recipient Service Class");
		}

		for (String variantID : requiredSubscriptionVariants)
		{
			ValidationException.doesContain(variantID, variantIDs, "requiredSubscriptionVariants", "Transfer Mode [" + this.transferModeID + "] Requires Variant");
		}

		if (blackListedPSOBits != null && blackListedPSOBits.length() != 0)
		{
			String[] psoStrings = blackListedPSOBits.split("\\,");
			for (String pso : psoStrings)
			{
				if (!pso.matches("\\d+"))
					throw new ValidationException("Invalid Blacklisted PSO: %s", pso);
			}
		}

	}

	public String formatRecipientQuantity(long quantity)
	{
		return StringUtils.formatScaled(quantity, recipientUnitsDisplayConversion, SCALE_DENOMINATOR);
	}

	public static long unScale(long amount)
	{
		return (amount + (SCALE_DENOMINATOR / 2)) / SCALE_DENOMINATOR;
	}

}

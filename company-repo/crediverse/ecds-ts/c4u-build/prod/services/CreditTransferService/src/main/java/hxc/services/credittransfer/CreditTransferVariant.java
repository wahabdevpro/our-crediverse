package hxc.services.credittransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;
import hxc.services.notification.Texts;

@Configurable
public class CreditTransferVariant
{
	// /////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ///////////////////////////

	private String variantID = "mainToMain";
	private int ussdID;
	private Texts name;
	private int donorAccountID;
	private DedicatedAccountUnitType donorAccountType;
	private DedicatedAccountUnitType recipientAccountType;
	private long donorMinBalance;
	private long donorMaxBalance;
	private int recipientAccountID;
	private long recipientMinBalance;
	private long recipientMaxBalance;
	private Integer recipientExpiryDays;
	private Texts donorUnits;
	private Texts recipientUnits;
	private long unitCostPerDonation;
	private long unitCostPerBenefit;
	private long minAmount;
	private long maxAmount;
	private int[] donorServiceClassIds;
	private int[] recipientServiceClassIds;
	private String[] donorQuotas;
	private TransactionCharge[] transactionChargeBands;
	private CumulativeLimits cumulativeDonorLimits;
	private CumulativeLimits cumulativeRecipientLimits;

	// association of SCs and chargingBands
	// private ChargingProfile[] chargingProfiles;

	// /////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ///////////////////////////

	public CreditTransferVariant(String variantID, int ussdID, Texts name, int donorAccountID, DedicatedAccountUnitType donorAccountType, long donorMinBalance, long donorMaxBalance,
			int recipientAccountID, DedicatedAccountUnitType recipientAccountType, long recipientMinBalance, long recipientMaxBalance, Integer recipientExpiryDays, Texts donorUnits,
			Texts recipientUnits, long unitCostPerDonation, long unitCostPerBenefit, long minAmount, long maxAmount, int[] donorServiceClassIds, int[] recipientServiceClassIds, String[] donorQuotas,
			TransactionCharge[] transactionChargeBands, CumulativeLimits donorLimits, CumulativeLimits recipientLimits)

	{
		this.variantID = variantID;
		this.ussdID = ussdID;
		this.name = name;
		this.donorAccountID = donorAccountID;
		this.donorAccountType = donorAccountType;
		this.donorMinBalance = donorMinBalance;
		this.donorMaxBalance = donorMaxBalance;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.recipientAccountID = recipientAccountID;
		this.recipientAccountType = recipientAccountType;
		this.recipientMinBalance = recipientMinBalance;
		this.recipientMaxBalance = recipientMaxBalance;
		this.recipientExpiryDays = recipientExpiryDays;
		this.donorUnits = donorUnits;
		this.recipientUnits = recipientUnits;
		this.unitCostPerDonation = unitCostPerDonation;
		this.unitCostPerBenefit = unitCostPerBenefit;
		this.donorServiceClassIds = donorServiceClassIds;
		this.recipientServiceClassIds = recipientServiceClassIds;
		this.donorQuotas = donorQuotas;
		this.transactionChargeBands = transactionChargeBands;
		this.cumulativeDonorLimits = donorLimits;
		this.cumulativeRecipientLimits = recipientLimits;
	}// CreditTransferVariant()

	// Default
	public CreditTransferVariant()
	{
	}

	// /////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// ///////////////////////////

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public Texts getName()
	{
		return name;
	}

	public void setName(Texts name)
	{
		this.name = name;
	}

	public int getDonorAccountID()
	{
		return donorAccountID;
	}

	public void setDonorAccountID(int donorAccountID) throws ValidationException
	{
		if (donorAccountID < 0)
			throw new ValidationException("%s: Donor account ID can't be negative", this.getVariantID());

		this.donorAccountID = donorAccountID;
	}

	public DedicatedAccountUnitType getDonorAccountType()
	{
		return donorAccountType;
	}

	public void setDonorAccountType(DedicatedAccountUnitType donorAccountType)
	{
		this.donorAccountType = donorAccountType;
	}

	@Config(description = "Donor Minimum Balance", hidden = true)
	public long getDonorMinBalance()
	{
		return donorMinBalance;
	}

	public void setDonorMinBalance(long donorMinBalance) throws ValidationException
	{
		if (donorMinBalance < 0)
			throw new ValidationException("%s: Min donor balance can't be negative", this.getVariantID());

		this.donorMinBalance = donorMinBalance;
	}

	@Config(description = "Donor Maximum Balance", hidden = true)
	public long getDonorMaxBalance()
	{
		return donorMaxBalance;
	}

	public void setDonorMaxBalance(long donorMaxBalance) throws ValidationException
	{
		if (donorMaxBalance < 0)
			throw new ValidationException("%s: Max donor balance can't be negative", this.getVariantID());

		this.donorMaxBalance = donorMaxBalance;
	}

	public int getRecipientAccountID()
	{
		return recipientAccountID;
	}

	public void setRecipientAccountID(int recipientAccountID) throws ValidationException
	{
		if (recipientAccountID < 0)
			throw new ValidationException("%s: Recipient account ID can't be negative", this.getVariantID());

		this.recipientAccountID = recipientAccountID;
	}

	public DedicatedAccountUnitType getRecipientAccountType()
	{
		return recipientAccountType;
	}

	public void setRecipientAccountType(DedicatedAccountUnitType recipientAccountType)
	{
		this.recipientAccountType = recipientAccountType;
	}

	@Config(description = "Recipient Minimum Balance", hidden = true)
	public long getRecipientMinBalance()
	{
		return recipientMinBalance;
	}

	public void setRecipientMinBalance(long recipientMinBalance) throws ValidationException
	{
		if (recipientMinBalance < 0)
			throw new ValidationException("%s: Min recipient balance can't be negative", this.getVariantID());

		this.recipientMinBalance = recipientMinBalance;
	}

	@Config(description = "Recipient Maximum Balance", hidden = true)
	public long getRecipientMaxBalance()
	{
		return recipientMaxBalance;
	}

	public void setRecipientMaxBalance(long recipientMaxBalance) throws ValidationException
	{
		if (recipientMaxBalance < 0)
			throw new ValidationException("%s: Max recipient balance can't be negative", this.getVariantID());

		this.recipientMaxBalance = recipientMaxBalance;
	}

	public Integer getRecipientExpiryDays()
	{
		return recipientExpiryDays;
	}

	public void setRecipientExpiryDays(Integer recipientExpiryDays) throws ValidationException
	{
		if (recipientExpiryDays < 0)
			throw new ValidationException("%s: Number of expiry days can't be negative", this.getVariantID());

		this.recipientExpiryDays = recipientExpiryDays;
	}

	public Texts getRecipientUnits()
	{
		return recipientUnits;
	}

	public void setRecipientUnits(Texts units)
	{
		this.recipientUnits = units;
	}

	@Config(description = "Minimum Transfer Amount", hidden = true)
	public long getMinAmount()
	{
		return minAmount;
	}

	public void setMinAmount(long minAmount) throws ValidationException
	{
		if (minAmount < 0)
			throw new ValidationException("%s: Minimum transferrable amount can't be negative", this.getVariantID());

		this.minAmount = minAmount;
	}

	@Config(description = "Maximum Transfer Amount", hidden = true)
	public long getMaxAmount()
	{
		return maxAmount;
	}

	public void setMaxAmount(long maxAmount) throws ValidationException
	{
		if (maxAmount < 0)
			throw new ValidationException("%s: Maximum transferrable amount can't be negative", this.getVariantID());

		this.maxAmount = maxAmount;
	}

	public int[] getDonorServiceClassIds()
	{
		return donorServiceClassIds;
	}

	public void setDonorServiceClassIds(int[] donorServiceClassIds) throws ValidationException
	{
		for (int sc : donorServiceClassIds)
		{
			if (sc < 0)
				throw new ValidationException("%s: Negative service class not allowed", this.getVariantID());
		}

		this.donorServiceClassIds = donorServiceClassIds;
	}

	public int[] getRecipientServiceClassIds()
	{
		return recipientServiceClassIds;
	}

	public void setRecipientServiceClassIds(int[] recipientServiceClassIds) throws ValidationException
	{
		for (int sc : recipientServiceClassIds)
		{
			if (sc < 0)
				throw new ValidationException("%s: Negative service class not allowed", this.getVariantID());
		}

		this.recipientServiceClassIds = recipientServiceClassIds;
	}

	public int getUssdID()
	{
		return ussdID;
	}

	public void setUssdID(int ussdID) throws ValidationException
	{
		if (ussdID < 0)
			throw new ValidationException("%s: USSD ID can't be negative", this.getVariantID());

		this.ussdID = ussdID;
	}

	@Config(description = "Cost Per Donor Unit", hidden = true)
	public long getUnitCostPerDonation()
	{
		return unitCostPerDonation;
	}

	public void setUnitCostPerDonation(long unitCostPerDonor) throws ValidationException
	{
		if (unitCostPerDonor < 0)
			throw new ValidationException("%s: UnitCostPerDonor can't be negative", this.getVariantID());

		this.unitCostPerDonation = unitCostPerDonor;
	}

	@Config(description = "Cost Per Recipient Unit", hidden = true)
	public long getUnitCostPerBenefit()
	{
		return unitCostPerBenefit;
	}

	public void setUnitCostPerBenefit(long unitCostPerBenefit) throws ValidationException
	{
		if (unitCostPerBenefit < 0)
			throw new ValidationException("%s: UnitCostPerBenefit can't be negative", this.getVariantID());

		this.unitCostPerBenefit = unitCostPerBenefit;
	}

	public Texts getDonorUnits()
	{
		return donorUnits;
	}

	public void setDonorUnits(Texts donorUnits)
	{
		this.donorUnits = donorUnits;
	}

	// public CreditTransferQuota getDonorQuota()
	@Config(description = "Donor quota", hidden = true)
	public String[] getDonorQuotas()
	{
		return donorQuotas;
	}

	// public void setDonorQuota(CreditTransferQuota donorQuota)
	public void setDonorQuotas(String[] donorQuotas)
	{
		// if (donorQuotas.length > 3)
		// {
		// throw new ValidationException("Invalid number of quotas specified");
		// }

		// Ensure all specified quota IDs are available in the pool
		this.donorQuotas = donorQuotas;
	}

	public TransactionCharge[] getTransactionChargeBands()
	{
		return transactionChargeBands;
	}

	public void setTransactionChargeBands(TransactionCharge[] transactionChargeBands) throws ValidationException
	{
		validateTransactionCharges(transactionChargeBands);

		this.transactionChargeBands = new TransactionCharge[transactionChargeBands.length];
		System.arraycopy(transactionChargeBands, 0, this.transactionChargeBands, 0, transactionChargeBands.length);

	}

	/**
	 * Given a transferAmount and service class, returns an applicable transaction charge THROWS: Exception with string ReturnCode message to be caught parsed by caller
	 */
	public long getTransactionCharge(long transferAmount, int serviceClass) throws Exception
	{
		long transactionCharge = -1L;
		long lowerBound = 0L;
		long upperBound = 0L;

		// Search for the TransactionCharge object whose range contains 'transferAmount'
		try
		{
			for (TransactionCharge currentBand : transactionChargeBands)
			{
				// Find applicable band first based on transferAmount,
				// then look for the SC within that band
				lowerBound = currentBand.getAmountRange().getMinValue();
				upperBound = currentBand.getAmountRange().getMaxValue();

				if ((lowerBound <= transferAmount * 10000) && (transferAmount * 10000 < upperBound))
				{
					// service class must be eligible for this charging profile
					int[] applicableServiceClasses = currentBand.getApplicableServiceClasses();
					Arrays.sort(applicableServiceClasses);

					int scFound = Arrays.binarySearch(applicableServiceClasses, serviceClass);
					if (scFound < 0)
					{
						throw new Exception("Specified service class not in list of allowed service classes");
					}

					// long variableCharge = transferAmount * ( (long)(charge.getPercentageCharge() * chargeScalingFactor) )/100;
					long variableCharge = transferAmount * currentBand.getPercentageCharge() / 100;
					transactionCharge = currentBand.getFixedCharge() + variableCharge;
					break;
				}
			}// for()
		}
		catch (Exception e)
		{
			throw e;
		}

		return transactionCharge;
	}

	@Config(description = "Cumulative Donor Limits", hidden = true)
	public CumulativeLimits getCumulativeDonorLimits()
	{
		return cumulativeDonorLimits;
	}

	// Ensure incoming object is in a consistent state before assigning it to target variable
	public void setCumulativeDonorLimits(CumulativeLimits cumulativeDonorLimits) throws ValidationException
	{
		try
		{
			cumulativeDonorLimits.validate();
			this.cumulativeDonorLimits = cumulativeDonorLimits;
		}
		catch (ValidationException e)
		{
			throw e;
		}
	}

	@Config(description = "Cumulative Recipient Limits", hidden = true)
	public CumulativeLimits getCumulativeRecipientLimits()
	{
		return cumulativeRecipientLimits;
	}

	// Ensure incoming object is in a consistent state before assigning it to target variable
	public void setCumulativeRecipientLimits(CumulativeLimits cumulativeRecipientLimits) throws ValidationException
	{
		try
		{
			cumulativeRecipientLimits.validate();
			this.cumulativeRecipientLimits = cumulativeRecipientLimits;
		}
		catch (ValidationException e)
		{
			throw e;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI Properties (Helper properties for pre-scaling values for the GUI)
	//
	// /////////////////////////////////

	// BALANCES

	@Config(description = "Donor Minimum Balance")
	public String getDonorMinBalanceUI()
	{
		return TransactionCharge.longToString(donorMinBalance, 2);
	}

	public void setDonorMinBalanceUI(String donorMinBalance) throws ValidationException
	{
		// this.donorMinBalance = TransactionCharge.stringToLong(donorMinBalance, 2);

		// delegation
		this.setDonorMinBalance(TransactionCharge.stringToLong(donorMinBalance, 2));
	}

	@Config(description = "Donor Maximum Balance")
	public String getDonorMaxBalanceUI()
	{
		return TransactionCharge.longToString(donorMaxBalance, 2);
	}

	public void setDonorMaxBalanceUI(String donorMaxBalance) throws ValidationException
	{
		// this.donorMaxBalance = TransactionCharge.stringToLong(donorMaxBalance, 2);

		// delegation
		this.setDonorMaxBalance(TransactionCharge.stringToLong(donorMaxBalance, 2));
	}

	@Config(description = "Recipient Minimum Balance")
	public String getRecipientMinBalanceUI()
	{
		return TransactionCharge.longToString(recipientMinBalance, 2);
	}

	public void setRecipientMinBalanceUI(String recipientMinBalance) throws ValidationException
	{
		// this.recipientMinBalance = TransactionCharge.stringToLong(recipientMinBalance, 2);

		// delegation
		this.setRecipientMinBalance(TransactionCharge.stringToLong(recipientMinBalance, 2));
	}

	@Config(description = "Recipient Maximum Balance")
	public String getRecipientMaxBalanceUI()
	{
		return TransactionCharge.longToString(recipientMaxBalance, 2);
	}

	public void setRecipientMaxBalanceUI(String recipientMaxBalance) throws ValidationException
	{
		// this.recipientMaxBalance = TransactionCharge.stringToLong(recipientMaxBalance, 2);

		// delegation
		this.setRecipientMaxBalance(TransactionCharge.stringToLong(recipientMaxBalance, 2));
	}

	// TRANSFER AMOUNTS

	@Config(description = "Minimum Transfer Amount")
	public String getMinAmountUI()
	{
		return TransactionCharge.longToString(minAmount, 2);
	}

	public void setMinAmountUI(String minAmount) throws ValidationException
	{
		// this.minAmount = TransactionCharge.stringToLong(minAmount, 2);

		// delegation
		this.setMinAmount(TransactionCharge.stringToLong(minAmount, 2));
	}

	@Config(description = "Maximum Transfer Amount")
	public String getMaxAmountUI()
	{
		return TransactionCharge.longToString(maxAmount, 2);
	}

	public void setMaxAmountUI(String maxAmount) throws ValidationException
	{
		// this.maxAmount = TransactionCharge.stringToLong(maxAmount, 2);

		// delegation
		this.setMaxAmount(TransactionCharge.stringToLong(maxAmount, 2));
	}

	// UNIT COSTS

	@Config(description = "Cost Per Donor Unit")
	public String getUnitCostPerDonorUI()
	{
		return TransactionCharge.longToString(unitCostPerDonation, 2);
	}

	public void setUnitCostPerDonationUI(String unitCostPerDonor) throws ValidationException
	{
		// this.unitCostPerDonation = TransactionCharge.stringToLong(unitCostPerDonor, 2);

		// delegation
		this.setUnitCostPerDonation(TransactionCharge.stringToLong(unitCostPerDonor, 2));
	}

	@Config(description = "Cost Per Recipient Unit")
	public String getUnitCostPerBenefitUI()
	{
		return TransactionCharge.longToString(unitCostPerBenefit, 2);
	}

	public void setUnitCostPerBenefitUI(String unitCostPerBenefit) throws ValidationException
	{
		// this.unitCostPerBenefit = TransactionCharge.stringToLong(unitCostPerBenefit, 2);

		// delegation
		this.setUnitCostPerBenefit(TransactionCharge.stringToLong(unitCostPerBenefit, 2));
	}

	// LIMITS

	// / Donor Limits

	public void setCumulativeDonorLimitDailyUI(String limit) throws ValidationException
	{
		if (cumulativeDonorLimits == null)
		{
			cumulativeDonorLimits = new CumulativeLimits();
		}

		try
		{
			cumulativeDonorLimits.setTotalDailyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	@Config(description = "Donor Cumulative Daily Limit")
	public String getCumulativeDonorLimitDailyUI()
	{
		return TransactionCharge.longToString(cumulativeDonorLimits.getTotalDailyLimit(), 2);
	}

	public void setCumulativeDonorLimitWeeklyUI(String limit) throws ValidationException
	{
		if (cumulativeDonorLimits == null)
		{
			cumulativeDonorLimits = new CumulativeLimits();
		}

		try
		{
			cumulativeDonorLimits.setTotalWeeklyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	@Config(description = "Donor Cumulative Weekly Limit")
	public String getCumulativeDonorLimitWeeklyUI()
	{
		// delegation
		return TransactionCharge.longToString(cumulativeDonorLimits.getTotalWeeklyLimit(), 2);
	}

	public void setCumulativeDonorLimitMonthlyUI(String limit) throws ValidationException
	{
		if (cumulativeDonorLimits == null)
		{
			cumulativeDonorLimits = new CumulativeLimits();
		}

		try
		{
			// delegation
			cumulativeDonorLimits.setTotalMonthlyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	@Config(description = "Donor Cumulative Monthly Limits")
	public String getCumulativeDonorLimitMonthlyUI()
	{
		// delegation
		return TransactionCharge.longToString(cumulativeDonorLimits.getTotalMonthlyLimit(), 2);
	}

	public void setCumulativeRecipientLimitDailyUI(String limit) throws ValidationException
	{
		if (cumulativeRecipientLimits == null)
		{
			cumulativeRecipientLimits = new CumulativeLimits();
		}

		try
		{
			// delegation
			cumulativeRecipientLimits.setTotalDailyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	// / Recipient Limits

	@Config(description = "Recipient Cumulative Daily Limit")
	public String getCumulativeRecipientLimitDailyUI()
	{
		return TransactionCharge.longToString(cumulativeRecipientLimits.getTotalDailyLimit(), 2);
	}

	public void setCumulativeRecipientLimitWeeklyUI(String limit) throws ValidationException
	{
		if (cumulativeRecipientLimits == null)
		{
			cumulativeRecipientLimits = new CumulativeLimits();
		}

		try
		{
			cumulativeRecipientLimits.setTotalWeeklyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	@Config(description = "Recipient Cumulative Weekly Limit")
	public String getCumulativeRecipientLimitWeeklyUI()
	{
		return TransactionCharge.longToString(cumulativeRecipientLimits.getTotalWeeklyLimit(), 2);
	}

	public void setCumulativeRecipientLimitMonthlyUI(String limit) throws ValidationException
	{
		if (cumulativeRecipientLimits == null)
		{
			cumulativeRecipientLimits = new CumulativeLimits();
		}

		try
		{
			cumulativeRecipientLimits.setTotalMonthlyLimit(TransactionCharge.stringToLong(limit, 2));
		}
		catch (ValidationException e)
		{
			throw new ValidationException(this.getVariantID() + ": " + e.getMessage());
		}
	}

	@Config(description = "Recipient Cumulative Monthly Limit")
	public String getCumulativeRecipientLimitMonthlyUI()
	{
		return TransactionCharge.longToString(cumulativeRecipientLimits.getTotalMonthlyLimit(), 2);
	}

	// Quota

	@Config(description = "Donor Transfer Quotas", referencesKey = "DonorQuotas.QuotaID")
	public String[] getDonorQuotasUI()
	{
		return this.donorQuotas;
	}

	public void setDonorQuotasUI(String[] donorQuotas) throws ValidationException
	{
		if (donorQuotas.length > 3)
			throw new ValidationException("Quota list cannot be longer than 3 items.");

		// this.donorQuotas = donorQuotas;
		this.setDonorQuotas(donorQuotas);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VALIDATION METHODS
	//
	// /////////////////////////////////

	// Basic sanity checks
	public void validate() throws ValidationException
	{
		if (donorMinBalance > donorMaxBalance)
			throw new ValidationException("Donor minimum balance cannot be greater than maximum balance");

		if (recipientMinBalance > recipientMaxBalance)
			throw new ValidationException("Recipient minimum balance cannot be greater than maximum balance");

		if (minAmount > maxAmount)
			throw new ValidationException("Minimum transferrable amount cannot be greater than maximum transferrable amount");

		if (ussdID < 0)
			throw new ValidationException("USSD ID must not be less than 0");

		if (donorQuotas.length > 3)
			throw new ValidationException("Quota list cannot be longer than 3 items");

		// BIG ONE: validate src and dest account IDs are consistent with ussdID/variantID
		/*
		 * Not necessary... switch (ussdID) { case 0: if (donorAccountID != pairs[ussdID].donorAccountID) { throw new ValidationException("For ussdID %s, variant specifies unexpected donorAccountID",
		 * ussdID); } if (recipientAccountID != pairs[ussdID].recipientAccountID) { throw new ValidationException("For ussdID %s, variant specifies unexpected recipientAccountID", ussdID); } break;
		 * case 1: break; case 2: break; case 3: break; case 4: break; case 5: break; case 6: break; case 7: break; case 8: break; case 9: break; case 10: break; case 11: break; case 12: break; case
		 * 13: break; case 14: break; case 15: break; }
		 */
	}

	public void validateTransactionCharges(TransactionCharge[] transactionCharges) throws ValidationException
	{
		List<TransactionCharge> list = new ArrayList<TransactionCharge>();

		int counter = 0;
		// Check for duplicates
		for (TransactionCharge tc : transactionCharges)
		{
			counter++;
			if (!list.contains(tc))
				list.add(tc);
			else
				throw new ValidationException("Duplicate Transaction Charge Bands: %s and %s in Variant: %s, band %s", tc.getFixedChargeUI(), tc.getPercentageChargeUI(), this.variantID, counter);
		}

		validateTransactionChargeServiceClasses(transactionCharges);

		validateTransactionChargeRange(transactionCharges);

	}

	private static void validateTransactionChargeServiceClasses(TransactionCharge[] transactionCharges) throws ValidationException
	{
		ListIterator<TransactionCharge> it = Arrays.asList(transactionCharges).listIterator();

		TransactionCharge currentCharge = null;
		TransactionCharge nextCharge = null;

		int[] currentSCList;
		int[] nextSCList;

		while (it.hasNext())
		{
			try
			{
				currentCharge = it.next();
				nextCharge = it.next();

				currentSCList = currentCharge.getApplicableServiceClasses();
				nextSCList = nextCharge.getApplicableServiceClasses();

				if (!Arrays.equals(currentSCList, nextSCList))
					throw new ValidationException("Service class list must be identical within a band.");

				// Move cursor back
				it.previous();
			}
			catch (NoSuchElementException nse)
			{
				break;
			}
			catch (Throwable t)
			{
				throw t;
			}
		}
	}

	private void validateTransactionChargeRange(TransactionCharge[] transactionCharges) throws ValidationException
	{
		ListIterator<TransactionCharge> it = Arrays.asList(transactionCharges).listIterator();

		TransactionCharge currentCharge = null;
		TransactionCharge nextCharge = null;

		long currentMaxRange;
		long nextMinRange;

		int counter = 0;

		while (it.hasNext())
		{
			counter++;
			try
			{
				currentCharge = it.next();
				nextCharge = it.next();

				currentMaxRange = currentCharge.getMaxRange();
				nextMinRange = nextCharge.getMinRange();

				if (currentMaxRange != nextMinRange)
					throw new ValidationException("Charge ranges cannot have gaps: %s and %s in Variant: %s, band %s", TransactionCharge.longToString(currentMaxRange, 2),
							TransactionCharge.longToString(nextMinRange, 2), this.variantID, counter);

				// Move cursor back
				it.previous();
			}
			catch (NoSuchElementException nse)
			{
				break;
			}
			catch (Throwable t)
			{
				throw t;
			}
		}
	}

}

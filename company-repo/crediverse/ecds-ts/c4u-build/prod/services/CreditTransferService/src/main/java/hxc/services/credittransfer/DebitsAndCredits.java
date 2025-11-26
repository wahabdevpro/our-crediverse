package hxc.services.credittransfer;

public class DebitsAndCredits
{
	// //////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////////
	long donorDADebit = 0L;
	long recipientDACredit = 0L;

	// //////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// ////////////////////////////////

	// DebitsAndCredits(long donorDADebit, long recipientDACredit)
	DebitsAndCredits()
	{
		// this.donorDADebit = donorDADebit;
		// this.recipientDACredit = recipientDACredit;
	}

	// //////////////////////////////////////////////////////////////////////////////////
	//
	// Getters and setters
	//
	// ////////////////////////////////
	public long getDonorDADebit()
	{
		return donorDADebit;
	}

	public void setDonorDADebit(long donorDADebit)
	{
		this.donorDADebit = donorDADebit;
	}

	public long getRecipientDACredit()
	{
		return recipientDACredit;
	}

	public void setRecipientDACredit(long recipientDACredit)
	{
		this.recipientDACredit = recipientDACredit;
	}

	// Worker method
	// NB! we truncate as opposed to rounding!!!
	public DebitsAndCreditsResponse calculateDebitsAndCredits(long transferAmount, long transactionCharge, CreditTransferVariant variant)
	{
		long donorDADebit = 0;
		long recipientDACredit = 0;

		// Get number of Donor Units to be debited
		long donorCostPerUnit = variant.getUnitCostPerDonation();
		// long numberOfDonorUnitsToDebit = (transferAmount*10000 + transactionCharge) / donorCostPerUnit;
		// recipientDACredit = (transferAmount*10000 + transactionCharge) / donorCostPerUnit;

		// Get transfer amount from number of Donor Units
		// long actualTransferAmount = numberOfDonorUnitsToDebit*donorCostPerUnit;
		// long actualTransferAmount = ( transferAmount*10000 / donorCostPerUnit ) * donorCostPerUnit;

		// Get transaction charge in terms of Donor Units
		// long transactionChargeInDonorUnits = transactionCharge / donorCostPerUnit;

		// Calculate total number of Donor Units to be debited
		// donorDADebit = transactionChargeInDonorUnits + numberOfDonorUnitsToDebit;
		donorDADebit = (transferAmount * 10000 + transactionCharge) / donorCostPerUnit;

		// Calculate number of Recipient Units to be credited
		long recipientCostPerUnit = variant.getUnitCostPerBenefit();
		recipientDACredit = ((transferAmount * CreditTransferBase.getChargeScalingfactor() / donorCostPerUnit) * donorCostPerUnit) / recipientCostPerUnit;

		return new DebitsAndCreditsResponse(donorDADebit, recipientDACredit);
	}
}

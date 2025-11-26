package hxc.services.credittransfer;

public class DebitsAndCreditsResponse
{
	// //////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////////
	private long donorDADebit = 0L;
	private long recipientDACredit = 0L;

	// //////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// ////////////////////////////////
	public DebitsAndCreditsResponse(long donorDADebit, long recipientDACredit)
	{
		this.setDonorDADebit(donorDADebit);
		this.setRecipientDACredit(recipientDACredit);
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

}

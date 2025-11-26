package hxc.services.advancedtransfer;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;
import hxc.connectors.lifecycle.ITemporalTrigger;

@Table(name = "ac_transfer")
public class TransferRecord
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String variantID;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String donorMsisdn;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String recipientMsisdn;

	@Column(primaryKey = true, maxLength = TransferMode.MAX_TRANSFERMODEID_LEN, nullable = false)
	protected String transferModeID;

	@Column(nullable = false)
	protected Date nextTransferDate;

	@Column(nullable = false)
	protected int attemptNo = 1;

	@Column(nullable = true)
	protected Long amount;

	@Column(nullable = true)
	protected Long transferLimit;

	@Column(nullable = false)
	protected long totalTransferred = 0L;

	@Column(nullable = true)
	protected Long transferThreshold = null;

	@Column(nullable = true)
	protected Date lastTransferDate;

	@Column(nullable = false)
	protected boolean suspended = false;

	@Column(nullable = false)
	protected boolean beingRetried = false;

	@Column(nullable = true)
	protected int transferCount = 0;

	protected boolean changed = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
		this.changed = true;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
		this.changed = true;

	}

	public String getDonorMsisdn()
	{
		return donorMsisdn;

	}

	public void setDonorMsisdn(String donorMsisdn)
	{
		this.donorMsisdn = donorMsisdn;
		this.changed = true;
	}

	public String getRecipientMsisdn()
	{
		return recipientMsisdn;
	}

	public void setRecipientMsisdn(String recipientMsisdn)
	{
		this.recipientMsisdn = recipientMsisdn;
		this.changed = true;
	}

	public String getTransferModeID()
	{
		return transferModeID;
	}

	public void setTransferModeID(String transferModeID)
	{
		this.transferModeID = transferModeID;
		this.changed = true;
	}

	public Date getNextTransferDate()
	{
		return nextTransferDate;
	}

	public void setNextTransferDate(Date nextTransferDate)
	{
		this.nextTransferDate = nextTransferDate;
		this.changed = true;
	}

	public int getAttemptNo()
	{
		return attemptNo;
	}

	public void setAttemptNo(int attemptNo)
	{
		this.attemptNo = attemptNo;
		this.changed = true;
	}

	public Long getAmount()
	{
		return amount;
	}

	public void setAmount(Long amount)
	{
		this.amount = amount;
		this.changed = true;
	}

	public Long getTransferLimit()
	{
		return transferLimit;
	}

	public void setTransferLimit(Long transferLimit)
	{
		this.transferLimit = transferLimit;
		this.changed = true;
	}

	public long getTotalTransferred()
	{
		return totalTransferred;
	}

	public void setTotalTransferred(long totalTransferred)
	{
		this.totalTransferred = totalTransferred;
		this.changed = true;
	}

	public Long getTransferThreshold()
	{
		return transferThreshold;
	}

	public void setTransferThreshold(Long transferThreshold)
	{
		this.transferThreshold = transferThreshold;
		this.changed = true;
	}

	public Date getLastTransferDate()
	{
		return lastTransferDate;
	}

	public void setLastTransferDate(Date lastTransferDate)
	{
		this.lastTransferDate = lastTransferDate;
		this.changed = true;
	}

	public boolean isSuspended()
	{
		return suspended;
	}

	public void setSuspended(boolean suspended)
	{
		this.suspended = suspended;
		this.changed = true;
	}

	public boolean isBeingRetried()
	{
		return beingRetried;
	}

	public void setBeingRetried(boolean beingRetried)
	{
		this.beingRetried = beingRetried;
		this.changed = true;
	}

	public int getTransferCount()
	{
		return transferCount;
	}

	public void setTransferCount(int transferCount)
	{
		this.transferCount = transferCount;
		this.changed = true;
	}

	public boolean isChanged()
	{
		return changed;
	}

	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransferRecord()
	{

	}

	public TransferRecord(String serviceID, String variantID, String transferModeID, //
			String donorMsisdn, String recipientMsisdn, Date nextTransferDate, long amount, Long transferLimit, Long transferThreshold)
	{
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.transferModeID = transferModeID;
		this.donorMsisdn = donorMsisdn;
		this.recipientMsisdn = recipientMsisdn;
		this.nextTransferDate = nextTransferDate;
		this.amount = amount;
		this.transferLimit = transferLimit;
		this.transferThreshold = transferThreshold;

	}

	public ITemporalTrigger toTrigger()
	{
		return new ITemporalTrigger()
		{

			@Override
			public String getServiceID()
			{
				return serviceID;
			}

			@Override
			public void setServiceID(String serviceID)
			{
			}

			@Override
			public String getVariantID()
			{
				return variantID;
			}

			@Override
			public void setVariantID(String variantID)
			{
			}

			@Override
			public String getMsisdnA()
			{
				return donorMsisdn;
			}

			@Override
			public void setMsisdnA(String msisdnA)
			{
			}

			@Override
			public String getMsisdnB()
			{
				return recipientMsisdn;
			}

			@Override
			public void setMsisdnB(String msisdnB)
			{
			}

			@Override
			public String getKeyValue()
			{
				return transferModeID;
			}

			@Override
			public void setKeyValue(String key)
			{
			}

			@Override
			public Date getNextDateTime()
			{
				return nextTransferDate;
			}

			@Override
			public void setNextDateTime(Date nextDateTime)
			{
			}

			@Override
			public boolean isBeingProcessed()
			{
				return false;
			}

			@Override
			public void setBeingProcessed(boolean beingProcessed)
			{
			}

			@Override
			public int getState()
			{
				return 0;
			}

			@Override
			public void setState(int state)
			{

			}

			@Override
			public Date getDateTime1()
			{
				return null;
			}

			@Override
			public void setDateTime1(Date dateTime1)
			{
			}

			@Override
			public Date getDateTime2()
			{
				return null;
			}

			@Override
			public void setDateTime2(Date dateTime2)
			{
			}

			@Override
			public Date getDateTime3()
			{
				return null;
			}

			@Override
			public void setDateTime3(Date dateTime3)
			{
			}

			@Override
			public Date getDateTime4()
			{
				return null;
			}

			@Override
			public void setDateTime4(Date dateTime4)
			{
			}

		};
	}

}

package com.concurrent.hxc;

import java.util.Date;

public class CreditTransfer
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields from Transfer Mode
	//
	// /////////////////////////////////

	private String transferModeID;
	private String name;
	private CreditTransferType transferType;
	private int donorAccountID;
	private int donorAccountType;
	private int recipientAccountID;
	private int recipientAccountType;
	private Integer recipientExpiryDays;
	private String units;
	private long conversionRate; // Scaled scaleDenominator
	private long scaleNumerator; // Applies to conversionRate/amount
	private long scaleDenominator; // Applies to conversionRate/amount
	private boolean requiresPIN;
	private boolean requiresSubscription;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields from TemporalTrigger
	//
	// /////////////////////////////////
	private boolean active = false;
	private Number donorNumber;
	private Number recipientNumber;
	private long amount;
	private int interval;
	private String intervalType;
	private Date nextTransfer;

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
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

	public Integer getRecipientExpiryDays()
	{
		return recipientExpiryDays;
	}

	public void setRecipientExpiryDays(Integer recipientExpiryDays)
	{
		this.recipientExpiryDays = recipientExpiryDays;
	}

	public String getUnits()
	{
		return units;
	}

	public void setUnits(String units)
	{
		this.units = units;
	}

	public long getConversionRate()
	{
		return conversionRate;
	}

	public void setConversionRate(long conversionRate)
	{
		this.conversionRate = conversionRate;
	}

	public long getScaleNumerator()
	{
		return scaleNumerator;
	}

	public void setScaleNumerator(long scaleNumerator)
	{
		this.scaleNumerator = scaleNumerator;
	}

	public long getScaleDenominator()
	{
		return scaleDenominator;
	}

	public void setScaleDenominator(long scaleDenominator)
	{
		this.scaleDenominator = scaleDenominator;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public Number getDonorNumber()
	{
		return donorNumber;
	}

	public void setDonorNumber(Number donorNumber)
	{
		this.donorNumber = donorNumber;
	}

	public Number getRecipientNumber()
	{
		return recipientNumber;
	}

	public void setRecipientNumber(Number recipientNumber)
	{
		this.recipientNumber = recipientNumber;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public int getInterval()
	{
		return interval;
	}

	public void setInterval(int interval)
	{
		this.interval = interval;
	}

	public String getIntervalType()
	{
		return intervalType;
	}

	public void setIntervalType(String intervalType)
	{
		this.intervalType = intervalType;
	}

	public Date getNextTransfer()
	{
		return nextTransfer;
	}

	public void setNextTransfer(Date nextTransfer)
	{
		this.nextTransfer = nextTransfer;
	}

	public boolean getRequiresPIN()
	{
		return requiresPIN;
	}

	public void setRequiresPIN(boolean requiresPIN)
	{
		this.requiresPIN = requiresPIN;
	}

	public boolean getRequiresSubscription()
	{
		return requiresSubscription;
	}

	public void setRequiresSubscription(boolean requiresSubscription)
	{
		this.requiresSubscription = requiresSubscription;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return String.format("%s %d %s", name, amount, units);
	}

}

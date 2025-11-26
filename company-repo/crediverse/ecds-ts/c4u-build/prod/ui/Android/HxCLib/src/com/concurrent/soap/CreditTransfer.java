package com.concurrent.soap;

import java.util.Date;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class CreditTransfer implements IDeserialisable
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDeserialisable Implementation
	//
	// /////////////////////////////////
	@Override
	public void deserialise(ISerialiser serialiser)
	{
		this.transferModeID = serialiser.getString("transferModeID", null);
		this.name = serialiser.getString("name", null);
		this.transferType = serialiser.getEnum("transferType", CreditTransferType.class, CreditTransferType.OnceOff);
		this.donorAccountID = serialiser.getInteger("donorAccountID", 0);
		this.donorAccountType = serialiser.getInteger("donorAccountType", 0);
		this.recipientAccountID = serialiser.getInteger("recipientAccountID", 0);
		this.recipientExpiryDays = serialiser.getInteger("recipientExpiryDays", null);
		this.units = serialiser.getString("units", "");
		this.conversionRate = serialiser.getLong("conversionRate", 0L);
		this.scaleNumerator = serialiser.getLong("scaleNumerator", 0L);
		this.scaleDenominator = serialiser.getLong("scaleDenominator", 0L);
		this.requiresPIN = serialiser.getBoolean("requiresPIN", false);
		this.active = serialiser.getBoolean("active", false);
		this.donorNumber = serialiser.getDeserialisable("donorNumber", null);
		this.recipientNumber = serialiser.getDeserialisable("recipientNumber", null);
		this.amount = serialiser.getLong("amount", null);
		this.interval = serialiser.getInteger("interval", 0);
		this.intervalType = serialiser.getString("intervalType", null);
		this.nextTransfer = serialiser.getDate("nextTransfer", null);

	}

}

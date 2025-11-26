package hxc.utils.protocol.uiconnector.airsim;

import java.io.Serializable;

public class AirSimMSISDNUsage implements Serializable
{
	
	private static final long serialVersionUID = 8070082199739856176L;
	public enum TimeUnits
	{
		Seconds, Minutes, Hours, Days, Weeks, Months, Years
	}
	
	public AirSimMSISDNUsage(){}
	
	public AirSimMSISDNUsage(String msisdn, int account, long amount, long interval, TimeUnits timeUnit,
			int standardDeviation, Long topupValue)
	{
		super();
		this.msisdn = msisdn;
		this.account = account;
		this.amount = amount;
		this.interval = interval;
		this.timeUnit = timeUnit;
		this.standardDeviation = standardDeviation;
		this.topupValue = topupValue;
	}

	private String msisdn;	// NOt for setting up this can be a range
	private int account;
	private long amount;
	private long interval;
	private TimeUnits timeUnit;
	private int standardDeviation;
	private Long topupValue;
	public String getMsisdn()
	{
		return msisdn;
	}
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}
	public int getAccount()
	{
		return account;
	}
	public void setAccount(int account)
	{
		this.account = account;
	}
	public long getAmount()
	{
		return amount;
	}
	public void setAmount(long amount)
	{
		this.amount = amount;
	}
	public long getInterval()
	{
		return interval;
	}
	public void setInterval(long interval)
	{
		this.interval = interval;
	}
	public TimeUnits getTimeUnit()
	{
		return timeUnit;
	}
	public void setTimeUnit(TimeUnits timeUnit)
	{
		this.timeUnit = timeUnit;
	}
	public int getStandardDeviation()
	{
		return standardDeviation;
	}
	public void setStandardDeviation(int standardDeviation)
	{
		this.standardDeviation = standardDeviation;
	}
	public Long getTopupValue()
	{
		return topupValue;
	}
	public void setTopupValue(Long topupValue)
	{
		this.topupValue = topupValue;
	}
	
}

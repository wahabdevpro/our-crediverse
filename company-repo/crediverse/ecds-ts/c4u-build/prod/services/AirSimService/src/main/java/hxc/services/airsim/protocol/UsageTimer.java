package hxc.services.airsim.protocol;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;

import hxc.services.airsim.model.IUsageHandler;
import hxc.utils.calendar.TimeUnits;

public class UsageTimer implements Runnable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String msisdn;
	private int account;
	private long amount;
	private long interval;
	private TimeUnits timeUnit;
	private int standardDeviation;
	private Long topupValue;
	private transient IUsageHandler handler;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@WebMethod(exclude = true)
	public void schedule(ScheduledThreadPoolExecutor scheduledThreadPool, IUsageHandler handler)
	{
		this.handler = handler;

		long millis = 1000;
		switch (timeUnit)
		{
			case Seconds:
				millis = TimeUnit.SECONDS.toMillis(interval);
				break;

			case Minutes:
				millis = TimeUnit.MINUTES.toMillis(interval);
				break;

			case Hours:
				millis = TimeUnit.HOURS.toMillis(interval);
				break;

			case Days:
				millis = TimeUnit.DAYS.toMillis(interval);
				break;

			case Weeks:
				millis = TimeUnit.DAYS.toMillis(interval) * 7L;
				break;

			case Months:
				millis = TimeUnit.DAYS.toMillis(interval) * 30L;
				break;

			case Years:
				millis = TimeUnit.DAYS.toMillis(interval) * 365L;
				break;
		}

		scheduledThreadPool.schedule(this, millis, TimeUnit.MILLISECONDS);
	}

	@WebMethod(exclude = true)
	@Override
	public void run()
	{
		if (handler != null)
			handler.onUsage(this);
	}

}

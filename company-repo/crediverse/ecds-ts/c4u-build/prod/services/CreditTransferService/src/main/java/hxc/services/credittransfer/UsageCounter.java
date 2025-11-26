package hxc.services.credittransfer;

import java.io.Serializable;
import java.util.Date;

import hxc.configuration.Config;
import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "ct_counter")
public class UsageCounter implements Serializable
{
	private static final long serialVersionUID = 706887331582395895L;

	// //////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////
	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdn = "";

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID = "";

	@Column(nullable = false)
	private long dailyCounter = 0;

	@Column(nullable = false)
	private long weeklyCounter = 0;

	@Column(nullable = false)
	private long monthlyCounter = 0;

	@Column(nullable = false)
	private long dailySentAccumulator;

	@Column(nullable = false)
	private long weeklySentAccumulator;

	@Column(nullable = false)
	private long monthlySentAccumulator;

	@Column(nullable = false)
	private long dailyReceivedAccumulator;

	@Column(nullable = false)
	private long weeklyReceivedAccumulator;

	@Column(nullable = false)
	private long monthlyReceivedAccumulator;

	@Column(nullable = false)
	private Date weekBaseDate = new Date();

	@Column(nullable = false)
	private Date monthBaseDate = new Date();

	// //////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////

	// Default
	public UsageCounter()
	{
		// Donor
		this.dailySentAccumulator = 0;
		this.weeklySentAccumulator = 0;
		this.monthlySentAccumulator = 0;
		// Recipient
		this.dailyReceivedAccumulator = 0;
		this.weeklyReceivedAccumulator = 0;
		this.monthlyReceivedAccumulator = 0;
	}

	public UsageCounter(String msisdn, String serviceID, long dailyCounter, long weeklyCounter, long monthlyCounter, long dailyCount, long weeklyCount, long monthlyCount)
	{
		setMsisdn(msisdn);
		setServiceID(serviceID);

		setDailyCounter(dailyCounter);
		setWeeklyCounter(weeklyCounter);
		setMonthlyCounter(monthlyCounter);

		this.setDailySentAccumulator(dailyCount);
		this.setWeeklySentAccumulator(weeklyCount);
		this.setDailySentAccumulator(monthlyCount);

		this.setDailyReceivedAccumulator(dailyCount);
		this.setWeeklyReceivedAccumulator(weeklyCount);
		this.setMonthlyReceivedAccumulator(monthlyCount);
	}

	public UsageCounter(String msisdn, String serviceID, long dailyCounter, long weeklyCounter, long monthlyCounter, long dailyCountD, long weeklyCountD, long monthlyCountD, long dailyCountR,
			long weeklyCountR, long monthlyCountR)
	{
		setMsisdn(msisdn);
		setServiceID(serviceID);

		// Usage
		setDailyCounter(dailyCounter);
		setWeeklyCounter(weeklyCounter);
		setMonthlyCounter(monthlyCounter);

		// Donor
		this.setDailySentAccumulator(dailyCountD);
		this.setWeeklySentAccumulator(weeklyCountD);
		this.setDailySentAccumulator(monthlyCountD);

		// Recipient
		this.setDailyReceivedAccumulator(dailyCountR);
		this.setWeeklyReceivedAccumulator(weeklyCountR);
		this.setMonthlyReceivedAccumulator(monthlyCountR);
	}

	public UsageCounter(String msisdn, String serviceID, long dailyCounter, long weeklyCounter, long monthlyCounter)
	{
		setMsisdn(msisdn);
		setServiceID(serviceID);
		setDailyCounter(dailyCounter);
		setWeeklyCounter(weeklyCounter);
		setMonthlyCounter(monthlyCounter);
	}

	/**
	 * @return the firstInserted
	 */
	@Config(description = "", hidden = true)
	public Date getFirstInserted()
	{
		return weekBaseDate;
	}

	/**
	 * @param firstInserted
	 *            the firstInserted to set
	 */
	public void setFirstInserted(Date firstInserted)
	{
		this.weekBaseDate = firstInserted;
	}

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public long getDailyCounter()
	{
		return this.dailyCounter;
	}

	public void setDailyCounter(long dailyCounter2)
	{
		this.dailyCounter = dailyCounter2;
	}

	public long getWeeklyCounter()
	{
		return weeklyCounter;
	}

	public void setWeeklyCounter(long weeklyCounter2)
	{
		this.weeklyCounter = weeklyCounter2;
	}

	public long getMonthlyCounter()
	{
		return monthlyCounter;
	}

	public void setMonthlyCounter(long monthlyCounter2)
	{
		this.monthlyCounter = monthlyCounter2;
	}

	public long getSerialversionuid()
	{
		return serialVersionUID;
	}

	// thread-safety
	void incrementCounters()
	{
		dailyCounter += 1;
		weeklyCounter += 1;
		monthlyCounter += 1;
	}

	// set all counters to the same value
	public void setCounters(int newValue)
	{
		setDailyCounter(newValue);
		setWeeklyCounter(newValue);
		setMonthlyCounter(newValue);
	}

	public long getDailySentAccumulator()
	{
		return dailySentAccumulator;
	}

	public void setDailySentAccumulator(long dailySentAccumulator)
	{
		this.dailySentAccumulator = dailySentAccumulator;
	}

	public long getWeeklySentAccumulator()
	{
		return weeklySentAccumulator;
	}

	public void setWeeklySentAccumulator(long weeklySentAccumulator)
	{
		this.weeklySentAccumulator = weeklySentAccumulator;
	}

	public long getMonthlySentAccumulator()
	{
		return monthlySentAccumulator;
	}

	public void setMonthlySentAccumulator(long monthlySentAccumulator)
	{
		this.monthlySentAccumulator = monthlySentAccumulator;
	}

	public long getDailyReceivedAccumulator()
	{
		return dailyReceivedAccumulator;
	}

	public void setDailyReceivedAccumulator(long dailyReceivedAccumulator)
	{
		this.dailyReceivedAccumulator = dailyReceivedAccumulator;
	}

	public long getWeeklyReceivedAccumulator()
	{
		return weeklyReceivedAccumulator;
	}

	public void setWeeklyReceivedAccumulator(long weeklyReceivedAccumulator)
	{
		this.weeklyReceivedAccumulator = weeklyReceivedAccumulator;
	}

	public long getMonthlyReceivedAccumulator()
	{
		return monthlyReceivedAccumulator;
	}

	public void setMonthlyReceivedAccumulator(long monthlyReceivedAccumulator)
	{
		this.monthlyReceivedAccumulator = monthlyReceivedAccumulator;
	}

	// Pushes up all cumulative (donor and recipient) counters by the given value
	void incrementSentCumulativeTotals(long value)
	{
		// Update donor totals
		this.setDailySentAccumulator(this.getDailySentAccumulator() + value);
		this.setWeeklySentAccumulator(this.getWeeklySentAccumulator() + value);
		this.setMonthlySentAccumulator(this.getMonthlySentAccumulator() + value);
	}

	void incrementReceivedCumulativeTotals(long value)
	{
		// Update donor totals
		this.setDailyReceivedAccumulator(this.getDailyReceivedAccumulator() + value);
		this.setWeeklyReceivedAccumulator(this.getWeeklyReceivedAccumulator() + value);
		this.setMonthlyReceivedAccumulator(this.getMonthlyReceivedAccumulator() + value);
	}

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public Date getWeekBaseDate()
	{
		return weekBaseDate;
	}

	public void setWeekBaseDate(Date weekBaseDate)
	{
		this.weekBaseDate = weekBaseDate;
	}

	public Date getMonthBaseDate()
	{
		return monthBaseDate;
	}

	public void setMonthBaseDate(Date monthBaseDate)
	{
		this.monthBaseDate = monthBaseDate;
	}
}

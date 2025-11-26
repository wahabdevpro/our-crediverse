package hxc.services.credittransfer;

import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;

@Configurable
public class CumulativeLimits
{
	// //////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////////

	private long totalDailyLimit;
	private long totalWeeklyLimit;
	private long totalMonthlyLimit;

	// //////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// ////////////////////////////////

	// Default
	public CumulativeLimits()
	{
		totalDailyLimit = 10000;
		totalWeeklyLimit = 100000;
		totalMonthlyLimit = 1000000;
	}

	public CumulativeLimits(long totalDailyLimit, long totalWeeklyLimit, long totalMonthlyLimit)
	{
		super();
		this.totalDailyLimit = totalDailyLimit;
		this.totalWeeklyLimit = totalWeeklyLimit;
		this.totalMonthlyLimit = totalMonthlyLimit;
	}

	// //////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// ////////////////////////////////

	public long getTotalDailyLimit()
	{
		return totalDailyLimit;
	}

	public void setTotalDailyLimit(long totalDailyLimit) throws ValidationException
	{

		if (totalDailyLimit < 0)
			throw new ValidationException("Negative daily limit not allowed");

		this.totalDailyLimit = totalDailyLimit;
	}

	public long getTotalWeeklyLimit()
	{
		return totalWeeklyLimit;
	}

	public void setTotalWeeklyLimit(long totalWeeklyLimit) throws ValidationException
	{
		if (totalWeeklyLimit < 0)
			throw new ValidationException("Negative weekly limit not allowed");

		this.totalWeeklyLimit = totalWeeklyLimit;
	}

	public long getTotalMonthlyLimit()
	{
		return totalMonthlyLimit;
	}

	public void setTotalMonthlyLimit(long totalMonthlyLimit) throws ValidationException
	{
		if (totalMonthlyLimit < 0)
			throw new ValidationException("Negative monthly limit not allowed");

		this.totalMonthlyLimit = totalMonthlyLimit;
	}

	// Sanity checks
	public void validate() throws ValidationException
	{
		if (!(totalDailyLimit < totalWeeklyLimit && totalWeeklyLimit < totalMonthlyLimit))
			throw new ValidationException("CumulativeLimits: [totalDailyLimit < totalWeeklyLimit < totalMonthlyLimit] violated");
	}

}

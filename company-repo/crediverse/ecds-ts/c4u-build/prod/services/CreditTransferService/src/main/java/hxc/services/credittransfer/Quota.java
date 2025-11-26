package hxc.services.credittransfer;

import hxc.configuration.Config;
import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;

@Configurable
public class Quota implements Comparable<Quota>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// //////////////////////////////////

	private String quotaID = "";
	private int limit = 5;
	private int numberOfLimitPeriods = 1;
	private QuotaPeriodUnits limitPeriodUnits = QuotaPeriodUnits.DAY;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// //////////////////////////////////

	public Quota()
	{
	}

	public Quota(String quotaID, int quota, int numQuotaPeriods, QuotaPeriodUnits quotaPeriodUnits)
	{
		this.quotaID = quotaID;
		this.limit = quota;
		this.numberOfLimitPeriods = numQuotaPeriods;
		this.limitPeriodUnits = quotaPeriodUnits;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods ( getters/setters/others )
	//
	// //////////////////////////////////

	@Config(description = "Quota ID", unique = true)
	public String getQuotaID()
	{
		return quotaID;

	}

	public void setQuotaID(String quotaID)
	{
		this.quotaID = quotaID;

	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int quota) throws ValidationException
	{
		if (quota < 0)
		{
			throw new ValidationException("setLimit(): Negative value not allowed");
		}

		this.limit = quota;
	}

	public int getNumberOfLimitPeriods()
	{
		return numberOfLimitPeriods;
	}

	public void setNumberOfLimitPeriods(int numberOfLimitPeriods) throws ValidationException
	{
		if (numberOfLimitPeriods < 0)
		{
			throw new ValidationException("setNumberOfLimitPeriods(): Negative value not allowed");
		}

		this.numberOfLimitPeriods = numberOfLimitPeriods;
	}

	public QuotaPeriodUnits getLimitPeriodUnits()
	{
		return limitPeriodUnits;
	}

	public void setLimitPeriodUnits(QuotaPeriodUnits quotaPeriodUnits)
	{
		this.limitPeriodUnits = quotaPeriodUnits;
	}

	@Override
	public int compareTo(Quota other)
	{
		return (this.getLimitPeriodUnits().ordinal() > other.getLimitPeriodUnits().ordinal() ? 1 : 0);
	}
}

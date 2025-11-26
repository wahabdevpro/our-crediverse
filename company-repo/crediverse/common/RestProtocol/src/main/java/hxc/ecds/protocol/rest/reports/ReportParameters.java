package hxc.ecds.protocol.rest.reports;

import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.util.TimeInterval;

/*
*/

public class ReportParameters<FilterType extends Report.IFilter, SortType extends Report.ISort> implements IReportParameters, IValidatable
{
	protected FilterType filter;
	protected SortType sort;
	protected Report.RelativeTimeRange relativeTimeRange;
	protected TimeInterval timeInterval;

	public IReportParameters amend(IReportParameters other)
	{
		if (!(other instanceof ReportParameters))
		{
			throw new IllegalArgumentException(String.format("other is not an instance of ReportParameters (%s)", other));
		}
		return this.amend((ReportParameters)other);
	}

	public IReportParameters amend(ReportParameters<FilterType, SortType> other)
	{
		this.filter = other.filter;
		this.sort = other.sort;
		this.relativeTimeRange = other.relativeTimeRange;
		this.timeInterval = other.timeInterval;
		return this;
	}

	public FilterType getFilter()
	{
		return this.filter;
	}

	public ReportParameters setFilter(FilterType filter)
	{
		this.filter = filter;
		return this;
	}

	public SortType getSort()
	{
		return this.sort;
	}

	public ReportParameters setSort(SortType sort)
	{
		this.sort = sort;
		return this;
	}

	public Report.RelativeTimeRange getRelativeTimeRange()
	{
		return this.relativeTimeRange;
	}

	public ReportParameters setRelativeTimeRange(Report.RelativeTimeRange relativeTimeRange)
	{
		this.relativeTimeRange = relativeTimeRange;
		return this;
	}

	public TimeInterval getTimeInterval()
	{
		return this.timeInterval;
	}

	public ReportParameters setTimeInterval(TimeInterval timeInterval)
	{
		this.timeInterval = timeInterval;
		return this;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(filter = %s, sort = %s, relativeTimeRange = %s, timeInterval = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			filter, sort, relativeTimeRange, timeInterval,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}


	@Override
	public List<Violation> validate()
	{
		return new ArrayList<Violation>();
	}
}

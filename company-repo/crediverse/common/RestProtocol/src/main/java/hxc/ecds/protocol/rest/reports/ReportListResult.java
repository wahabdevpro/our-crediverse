package hxc.ecds.protocol.rest.reports;

import java.util.List;

public class ReportListResult<ReportSpecificationType extends IReportSpecification>
{
	private List<? extends ReportSpecificationType> entries;
	private Long found;

	public List<? extends ReportSpecificationType> getEntries()
	{
		return this.entries;
	}

	public ReportListResult<? extends ReportSpecificationType> setEntries(List<? extends ReportSpecificationType> entries)
	{
		this.entries = entries;
		return this;
	}

	public Long getFound()
	{
		return this.found;
	}

	public ReportListResult<? extends ReportSpecificationType> setFound(Long found)
	{
		this.found = found;
		return this;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(found = %s, entries = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			found, entries,
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

}

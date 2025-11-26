package hxc.ecds.protocol.rest.reports;

import java.util.List;

public class ReportResult<ReportResultEntry>
{
	private List<? extends ReportResultEntry> entries;
	private Integer found;

	public List<? extends ReportResultEntry> getEntries()
	{
		return this.entries;
	}

	public ReportResult setEntries(List<? extends ReportResultEntry> entries)
	{
		this.entries = entries;
		return this;
	}

	public Integer getFound()
	{
		return this.found;
	}

	public ReportResult setFound(Integer found)
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

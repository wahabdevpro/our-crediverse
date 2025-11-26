package hxc.ecds.protocol.rest.reports;

import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.Violation;

/*
*/

public class ReportParametersWithNothing implements IReportParameters, IValidatable
{
	public IReportParameters amend(IReportParameters other)
	{
		if (!(other instanceof ReportParameters))
		{
			throw new IllegalArgumentException(String.format("other is not an instance of ReportParameters (%s)", other));
		}
		return this.amend((ReportParameters)other);
	}

	public IReportParameters amend(ReportParametersWithNothing other)
	{
		return this;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
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

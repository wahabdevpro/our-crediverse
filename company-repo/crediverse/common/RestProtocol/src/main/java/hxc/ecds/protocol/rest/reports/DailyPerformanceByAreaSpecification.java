package hxc.ecds.protocol.rest.reports;

import java.util.List;

import hxc.ecds.protocol.rest.Violation;

public class DailyPerformanceByAreaSpecification extends ReportSpecificationWithParameters< DailyPerformanceByAreaReportParameters >
{
	public DailyPerformanceByAreaSpecification()
	{
		super();
	}

	public List<Violation> validate()
	{
		return super.validate();
	}
}

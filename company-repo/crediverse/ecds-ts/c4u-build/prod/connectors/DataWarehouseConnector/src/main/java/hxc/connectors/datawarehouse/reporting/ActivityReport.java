package hxc.connectors.datawarehouse.reporting;

import hxc.services.reporting.IReportDefinition;
import hxc.services.reporting.ReportParameters;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;

public abstract class ActivityReport implements IReportDefinition<ActivityReportData>
{

	@Override
	public String getName()
	{
		return "Activity Report";
	}

	@Override
	public String getTemplate()
	{
		return "ActivityReportTemplate.jasper";
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return new ActivityReportParameters(DateRange.GetRange(Periods.PastMonth));
	}

}

package hxc.services.sharedaccounts.reporting;

import hxc.services.reporting.IReportDefinition;
import hxc.services.reporting.ReportParameters;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;

public abstract class SharedAccountsActivityReport implements IReportDefinition<SharedAccountsActivityReportData>
{

	@Override
	public String getName()
	{
		return "Shared Accounts Activity Report";
	}

	@Override
	public String getTemplate()
	{
		return "SharedAccountsActivityReportTemplate.jasper";
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return new SharedAccountsActivityReportParameters(DateRange.GetRange(Periods.ThisMonth));
	}

}

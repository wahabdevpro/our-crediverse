package hxc.services.sharedaccounts.reporting;

import hxc.services.reporting.IReportDefinition;
import hxc.services.reporting.ReportParameters;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;

public abstract class SharedAccountsDetailedRevenueReport implements IReportDefinition<SharedAccountsDetailedRevenueReportData>
{

	@Override
	public String getName()
	{
		return "Shared Account: Detailed Revenue Per Service Type Report";
	}

	@Override
	public String getTemplate()
	{
		return "SharedAccountDetailedRevenueTemplate.jasper";
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return new SharedAccountsDetailedRevenueReportParameters(DateRange.GetRange(Periods.ThisMonth));
	}

}

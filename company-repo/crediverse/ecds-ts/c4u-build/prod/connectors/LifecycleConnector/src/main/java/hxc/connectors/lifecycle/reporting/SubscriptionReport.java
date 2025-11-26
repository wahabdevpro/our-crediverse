package hxc.connectors.lifecycle.reporting;

import hxc.services.reporting.IReportDefinition;
import hxc.services.reporting.ReportParameters;

public abstract class SubscriptionReport implements IReportDefinition<SubscriptionReportData>
{

	@Override
	public String getName()
	{
		return "Subscriptions Report";
	}

	@Override
	public String getTemplate()
	{
		return "SubscriptionReportTemplate.jasper";
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return null;
	}

}

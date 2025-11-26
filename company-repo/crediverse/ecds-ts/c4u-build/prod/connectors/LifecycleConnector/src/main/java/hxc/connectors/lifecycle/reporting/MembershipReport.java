package hxc.connectors.lifecycle.reporting;

import hxc.services.reporting.IReportDefinition;
import hxc.services.reporting.ReportParameters;

public abstract class MembershipReport implements IReportDefinition<MembershipReportData>
{

	@Override
	public String getName()
	{
		return "Membership Report";
	}

	@Override
	public String getTemplate()
	{
		return "MembershipReportTemplate.jasper";
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return null;
	}

}

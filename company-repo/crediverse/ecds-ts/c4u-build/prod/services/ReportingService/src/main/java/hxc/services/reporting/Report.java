package hxc.services.reporting;

public abstract class Report implements IReport
{

	private String name;
	private ReportParameters defaultParameters;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public ReportParameters getDefaultParameters()
	{
		return defaultParameters;
	}

	public Report(IReportDefinition<?> report)
	{
		this.name = report.getName();
		this.defaultParameters = report.getDefaultParameters();
	}

	public Report(String name, ReportParameters defaultParameters)
	{
		this.name = name;
		this.defaultParameters = defaultParameters;
	}

}

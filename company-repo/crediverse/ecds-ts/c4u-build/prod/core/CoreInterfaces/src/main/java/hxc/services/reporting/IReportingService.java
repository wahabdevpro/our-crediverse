package hxc.services.reporting;

public interface IReportingService
{
	public abstract IReport[] getReports();

	public abstract void addReport(IReportDefinition<?> report);
}

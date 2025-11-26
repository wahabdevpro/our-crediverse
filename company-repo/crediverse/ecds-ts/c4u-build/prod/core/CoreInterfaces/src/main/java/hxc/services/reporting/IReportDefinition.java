package hxc.services.reporting;

import java.util.Collection;

public interface IReportDefinition<T>
{
	public abstract String getName();

	public abstract String getTemplate();

	public abstract ReportParameters getDefaultParameters();

	public abstract Collection<T> getReportData(ReportParameters parameters);
}

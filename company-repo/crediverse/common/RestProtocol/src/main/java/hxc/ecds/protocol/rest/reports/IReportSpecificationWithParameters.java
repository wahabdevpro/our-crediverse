package hxc.ecds.protocol.rest.reports;

public interface IReportSpecificationWithParameters<ReportParametersType extends IReportParameters> extends IReportSpecification
{
	public IReportSpecification amend(IReportSpecificationWithParameters<? extends ReportParametersType> other);

	public ReportParametersType getParameters();
	public IReportSpecificationWithParameters<? extends ReportParametersType> setParameters(ReportParametersType reportParameters);
}

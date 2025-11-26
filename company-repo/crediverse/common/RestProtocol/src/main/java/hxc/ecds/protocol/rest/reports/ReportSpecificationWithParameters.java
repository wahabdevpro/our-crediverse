package hxc.ecds.protocol.rest.reports;

public class ReportSpecificationWithParameters<ReportParametersType extends IReportParameters> extends ReportSpecification implements IReportSpecificationWithParameters<ReportParametersType>
{
	protected ReportParametersType parameters;

	@Override
	public ReportSpecificationWithParameters<? extends ReportParametersType> amend(IReportSpecificationWithParameters<? extends ReportParametersType> other)
	{
		super.amend(other);
		this.parameters.amend(other.getParameters());
		return this;
	}

	@Override
	public ReportParametersType getParameters()
	{
		return this.parameters;
	}

	@Override
	public ReportSpecificationWithParameters<? extends ReportParametersType> setParameters(ReportParametersType parameters)
	{
		this.parameters = parameters;
		return this;
	}

	public String describe(String extra)
	{
		return super.describe(String.format("parameters = %s%s%s",
			parameters,
			(extra.isEmpty() ? "" : ", "), extra));
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}
}

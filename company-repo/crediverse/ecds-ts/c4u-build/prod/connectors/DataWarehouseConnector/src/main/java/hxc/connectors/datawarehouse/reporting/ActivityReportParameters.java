package hxc.connectors.datawarehouse.reporting;

import hxc.configuration.ValidationException;
import hxc.services.reporting.ReportParameters;
import hxc.utils.calendar.DateRange;

public class ActivityReportParameters extends ReportParameters
{
	private DateRange period;

	public DateRange getPeriod()
	{
		return period;
	}

	public void setPeriod(DateRange period)
	{
		this.period = period;
	}

	public ActivityReportParameters(DateRange period)
	{
		this.period = period;
	}

	@Override
	public void validate() throws ValidationException
	{
		if (period == null)
			throw new ValidationException("Period was not Supplied");
	}
}

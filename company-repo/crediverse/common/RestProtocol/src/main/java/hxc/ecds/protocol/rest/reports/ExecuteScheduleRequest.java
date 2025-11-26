package hxc.ecds.protocol.rest.reports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hxc.ecds.protocol.rest.RequestHeader;
import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

// REST End-Point: ~/{report_type}/{specification_id}/schedule/{schedule_id}/execute
public class ExecuteScheduleRequest extends RequestHeader
{
	private Date referenceDate;

	public Date getReferenceDate()
	{
		return this.referenceDate;
	}
	public ExecuteScheduleRequest setReferenceDate( Date referenceDate )
	{
		this.referenceDate = referenceDate;
		return this;
	}

	@Override
	public ExecuteScheduleResponse createResponse()
	{
		return new ExecuteScheduleResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator();
		return validator.toList();

	}

	public String describe(String extra)
	{
		return super.describe(String.format("referenceDate = %s%s%s",
			( referenceDate != null ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(referenceDate) : referenceDate ),
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

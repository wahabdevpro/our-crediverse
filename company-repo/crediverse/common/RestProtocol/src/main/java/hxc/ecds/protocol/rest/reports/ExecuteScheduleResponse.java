package hxc.ecds.protocol.rest.reports;

import hxc.ecds.protocol.rest.ResponseHeader;

public class ExecuteScheduleResponse extends ResponseHeader
{

	public static enum NotExecutedReason
	{
		BEFORE_TIME_OF_DAY,
		BEFORE_START_TIME_OF_DAY,
		AFTER_END_TIME_OF_DAY,
		BELOW_MINIMUM_SECONDS_AFTER_PERIOD
	}

	private boolean executed;
	public boolean getExecuted()
	{
		return this.executed;
	}
	public ExecuteScheduleResponse setExecuted( boolean executed )
	{
		this.executed = executed;
		return this;
	}

	private NotExecutedReason notExecutedReason;
	public NotExecutedReason getNotExecutedReason()
	{
		return this.notExecutedReason;
	}
	public ExecuteScheduleResponse setNotExecutedReason( NotExecutedReason notExecutedReason )
	{
		this.notExecutedReason = notExecutedReason;
		return this;
	}


	public ExecuteScheduleResponse()
	{
	}

	public ExecuteScheduleResponse(ExecuteScheduleRequest request)
	{
		super(request);
	}

	public String describe(String extra)
	{
		return super.describe(String.format("executed = %s, notExecutedReason = %s%s%s",
			executed, notExecutedReason,
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

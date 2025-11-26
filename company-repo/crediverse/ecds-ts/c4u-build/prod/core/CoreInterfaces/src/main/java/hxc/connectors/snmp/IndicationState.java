package hxc.connectors.snmp;

public enum IndicationState
{
	// ElementServiceStatus
	IN_SERVICE(0, "Slice resumed/ceased"), OUT_OF_SERVICE(1, "Slice resumed/ceased"),
	// ElementManagementStatus
	MANAGED(0, "Computer managed/impaired"), IMPAIRED(1, "Computer managed/impaired"),
	// ElementDisposition
	STABLE(0, "Slice stable/ceased/impaired"), FAILED_AWAY(1, "Slice stable/ceased/impaired"), FAILED_OVER(2, "Slice stable/ceased/impaired"), FAIL_OVER_FAILED(3, "Slice stable/ceased/impaired"), RESTORE_FAILED(
			4, "Slice stable/ceased/impaired"), FAILED_THROUGH(5, "Slice stable/ceased/impaired"),
	// ElementBoundaryStatus
	UP(0, "SS#7 interface local SPC {value}"), DOWN(1, "SS#7 interface local SPC {value}"),
	// TaskExecutionStatus
	STARTED(0, "Task starting/stopping"), STARTING(1, "Task starting/stopping"), STOPPING(2, "Task starting/stopping"), STOPPED(3, "Task starting/stopping"), FAILED(4, "Task starting/stopping"),
	// ThresholdStatus
	WITHIN_LIMITS(0, "Unknown"), SOFT_LIMIT_BREACHED(1, "Unknown"), HARD_LIMIT_BREACHED(2, "Unknown");

	private int value;
	private String gist;

	IndicationState(int value, String gist)
	{
		this.value = value;
		this.gist = gist;
	}

	public int value()
	{
		return this.value;
	}

	public String gist()
	{
		return this.gist;
	}
}

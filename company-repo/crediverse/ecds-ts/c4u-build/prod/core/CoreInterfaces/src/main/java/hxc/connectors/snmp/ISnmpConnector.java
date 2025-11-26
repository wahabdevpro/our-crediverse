package hxc.connectors.snmp;

public interface ISnmpConnector
{

	// Incidents

	public abstract void jobFailed(String element, IncidentSeverity severity, String description);

	public abstract void transactionFailed(String element, IncidentSeverity severity, String description);

	public abstract void deadlineExceeded(String element, IncidentSeverity severity, String description);

	// Indications

	public abstract void elementServiceStatus(String element, IndicationState state, IncidentSeverity severity, String description);

	public abstract void elementManagementStatus(String element, IndicationState state, IncidentSeverity severity, String description);

	public abstract void elementDisposition(String element, IndicationState state, IncidentSeverity severity, String description);

	public abstract void elementBoundaryStatus(String element, IndicationState state, IncidentSeverity severity, String description);

	public abstract void taskExecutionStatus(String element, IndicationState state, IncidentSeverity severity, String description);

	public abstract void thresholdStatus(String element, IndicationState state, IncidentSeverity severity, String description);
}

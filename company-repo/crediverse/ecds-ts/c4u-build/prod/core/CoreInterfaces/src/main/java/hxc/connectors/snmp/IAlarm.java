package hxc.connectors.snmp;

public interface IAlarm
{

	public abstract String getName();

	public abstract String getDescription();

	public abstract AlarmType getType();

	public abstract IncidentSeverity getSeverity();

	public abstract IndicationState getState();

	public abstract java.util.Date getTimestamp();

}

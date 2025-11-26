package hxc.connectors.snmp.components;

import java.util.Date;

import hxc.connectors.snmp.AlarmType;
import hxc.connectors.snmp.IAlarm;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;

public class Alarm implements IAlarm
{

	// Name of the alarm
	private String name;

	// Description of the alarm
	private String description;

	// Type of the alarm, either Incident or Indication
	private AlarmType type;

	// Time the alarm was created
	private Date timestamp;

	// The severity of this alarm
	private IncidentSeverity severity;

	// The current state of the alarm
	private IndicationState state;

	public Alarm(AlarmType type, String name, String description, IncidentSeverity severity, IndicationState state)
	{
		this(type, name, description, severity);
		this.state = state;
	}

	public Alarm(AlarmType type, String name, String description, IncidentSeverity severity)
	{
		this.type = type;
		this.name = name;
		this.description = description;
		this.severity = severity;
		this.timestamp = new Date();
	}

	@Override
	public AlarmType getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public Date getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public IncidentSeverity getSeverity()
	{
		return severity;
	}

	@Override
	public IndicationState getState()
	{
		return state;
	}

}

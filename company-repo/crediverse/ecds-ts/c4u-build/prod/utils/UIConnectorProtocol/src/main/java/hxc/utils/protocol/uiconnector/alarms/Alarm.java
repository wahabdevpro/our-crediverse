package hxc.utils.protocol.uiconnector.alarms;

import java.io.Serializable;
import java.util.Date;

import hxc.connectors.snmp.AlarmType;
import hxc.connectors.snmp.IAlarm;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;

public class Alarm implements IAlarm, Serializable
{

	private static final long serialVersionUID = 8550789075864812871L;
	private String name;
	private String description;
	private AlarmType type;
	private IncidentSeverity severity;
	private IndicationState state;
	private Date timestamp;

	public Alarm(IAlarm alarm)
	{
		this.name = alarm.getName();
		this.description = alarm.getDescription();
		this.type = alarm.getType();
		this.severity = alarm.getSeverity();
		this.state = alarm.getState();
		this.timestamp = alarm.getTimestamp();
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public AlarmType getType()
	{
		return type;
	}

	public void setType(AlarmType type)
	{
		this.type = type;
	}

	@Override
	public IncidentSeverity getSeverity()
	{
		return severity;
	}

	public void setSeverity(IncidentSeverity severity)
	{
		this.severity = severity;
	}

	@Override
	public IndicationState getState()
	{
		return state;
	}

	public void setState(IndicationState state)
	{
		this.state = state;
	}

	@Override
	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

}

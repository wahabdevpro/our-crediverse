package hxc.connectors.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import hxc.connectors.IConnection;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.ctrl.IServerInfo;
import hxc.connectors.snmp.AlarmType;
import hxc.connectors.snmp.IAlarm;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.Trigger;
import hxc.utils.protocol.uiconnector.alarms.Alarm;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataRequest;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataResponse;

public class UiAlarmController
{

	private IServiceBus esb;
	// private ILogger logger;
	private ICtrlConnector control;

	private List<Alarm> indicationAlarms = new LinkedList<Alarm>();

	// A trigger for capturing alarms
	private Trigger<IAlarm> alarmTrigger = new Trigger<IAlarm>(IAlarm.class)
	{

		@Override
		public boolean testCondition(IAlarm message)
		{
			return true;
		}

		@Override
		public void action(IAlarm message, IConnection connection)
		{
			// Check if it is a indication
			if (message.getType() == AlarmType.Indication)
			{
				// Check if it already exists in the list, if so, then update it
				boolean found = false;
				Stack<Alarm>todoStack = new Stack<Alarm>();
				synchronized(indicationAlarms)
				{
					for (Alarm alarm : indicationAlarms)
					{
						// Compare the names
						if (alarm.getName().equalsIgnoreCase(message.getName()))
						{
							found = true;
							todoStack.push(alarm);
						}
					}
					
					while (!todoStack.empty())
					{
						Alarm alarm = todoStack.pop();
						if (alarm != null)
						{
							// Set the information
							alarm.setDescription(message.getDescription());
							alarm.setSeverity(message.getSeverity());
							alarm.setState(message.getState());
							alarm.setTimestamp(message.getTimestamp());
						}
					}

					// Else if it is not found, then add the alarm to the list
					if (!found)
						indicationAlarms.add(new Alarm(message));
				}
			}
		}

		@Override
		public boolean isTransaction()
		{
			return false;
		}

	};

	public UiAlarmController(IServiceBus esb, ICtrlConnector control)
	{
		this.esb = esb;
		this.control = control;

		this.esb.addTrigger(alarmTrigger);
	}

	// Gets the information for the alarms
	public GetAlarmDataResponse pollAlarmData(GetAlarmDataRequest request)
	{
		// Create the response
		GetAlarmDataResponse response = new GetAlarmDataResponse(request.getUserId(), request.getSessionId());

		// Set the indication alarms
		response.setIndicationAlarms(indicationAlarms);

		// Get all the available hosts
		List<String> hosts = new ArrayList<String>();
		for (IServerInfo info : control.getServerList())
			hosts.add(info.getServerHost());

		// Set the hosts
		response.setHosts(hosts);

		// Return the response
		return response;
	}

}

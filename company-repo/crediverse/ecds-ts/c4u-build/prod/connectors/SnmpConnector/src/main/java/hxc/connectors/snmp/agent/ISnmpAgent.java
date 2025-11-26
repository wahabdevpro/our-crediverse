package hxc.connectors.snmp.agent;

import org.snmp4j.CommandResponder;

import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;
import hxc.connectors.snmp.TrapCodes;
import hxc.connectors.snmp.components.SnmpStatusException;

public interface ISnmpAgent
{

	public static final int PORT = 16100;
	public static final String DEFAULT_ADDRESS = "0.0.0.0/" + PORT;

	// Starts the agent
	public abstract void start() throws Exception;

	// Stops the agent
	public abstract void stop();

	// Properties
	public abstract void setTargetAddress(int target, String targetAddress);

	public abstract void setCommunity(String community);

	public abstract void setRetries(int retries);

	public abstract void setTimeout(int timeout);

	public abstract void setResponder(CommandResponder responder);

	public abstract SnmpMibDatbase getDatabase();

	public abstract boolean isWorking();

	// Snmp Methods

	// Sends an incident - Once off
	public abstract void incident(TrapCodes code, String element, IncidentSeverity severity, String description) throws SnmpStatusException;

	// Sends an indication - State
	public abstract void indication(TrapCodes code, String element, IndicationState state, IncidentSeverity severity, String description) throws SnmpStatusException;

}

package hxc.connectors.snmp.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.TransportMappings;

import hxc.connectors.snmp.AlarmType;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.connectors.snmp.IndicationState;
import hxc.connectors.snmp.TrapCodes;
import hxc.connectors.snmp.components.Alarm;
import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpStatusException;
import hxc.connectors.snmp.components.SnmpTable;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import mibs.DISMAN_EVENT_MIBOidTable;
import mibs.HOST_RESOURCES_MIBOidTable;
import mibs.IF_MIBOidTable;
import mibs.IP_MIBOidTable;
import mibs.RFC1213_MIBOidTable;
import mibs.SNMPv2_MIBOidTable;
import mibs.TCP_MIBOidTable;
import mibs.UDP_MIBOidTable;

public class SnmpAgent extends BaseAgent implements ISnmpAgent, CommandResponder
{
	final static Logger logger = LoggerFactory.getLogger(SnmpAgent.class);
	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static String bootCounterFile = "bootCounterFile";
	private static String configFile = "configFile";
	public static final int PORT = 16100;
	public static final String DEFAULT_ADDRESS = "0.0.0.0/" + PORT;

	// Configurables
	private String address;
	private String community = "public";
	private String communityEntry = "public2public";
	private String security = "security";
	private String group = "public";
	private String context = "public";
	private int retries = 5;
	private int timeout = 1000;
	private String defaultHostName = "hxc";

	private Date uptime;

	private SnmpMibDatbase mibDatabase;
	private ArrayList<String> targetAddresses;
	private IServiceBus esb;
	public CommandResponder responder = this;
	
	public SnmpAgent(String address, IServiceBus esb)
	{
		this(address);
		this.esb = esb;
	}

	public SnmpAgent(String address)
	{
		super(new File(bootCounterFile), new File(configFile), new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
		this.address = address;
		mibDatabase = new SnmpMibDatbase();
		defaultHostName = HostInfo.getNameOrElseHxC();
		targetAddresses = new ArrayList<String>();
		uptime = new Date();
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// BaseAgent implementation
	//
	// /////////////////////////////////

	// http://www.snmp4j.org/html/documentation.html

	@SuppressWarnings("unchecked")
	@Override
	protected void addCommunities(SnmpCommunityMIB snmpCommunityMIB)
	{
		// Adds the community
		Variable comV2[] = new Variable[] { new OctetString(community), new OctetString(security), getAgent().getContextEngineID(), new OctetString(context), new OctetString(),
				new Integer32(StorageType.nonVolatile), new Integer32(RowStatus.active) };
		MOTableRow<?> row = snmpCommunityMIB.getSnmpCommunityEntry().createRow(new OctetString(communityEntry).toSubIndex(true), comV2);
		snmpCommunityMIB.getSnmpCommunityEntry().addRow(row);
	}

	@Override
	protected void addNotificationTargets(SnmpTargetMIB arg0, SnmpNotificationMIB arg1)
	{
	}

	@Override
	protected void addUsmUser(USM arg0)
	{
		// Involves v3 Traps, which is not implemented at this moment.
	}

	@Override
	protected void addViews(VacmMIB vacmMIB)
	{
		vacmMIB.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(security), new OctetString(group), StorageType.nonVolatile);
		vacmMIB.addAccess(new OctetString(group), new OctetString(community), SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT,
				new OctetString("fullReadView"), new OctetString("fullWriteView"), new OctetString("fullNotifyView"), StorageType.nonVolatile);
		vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
	}

	// Registers the MIB tables
	@Override
	protected void registerManagedObjects()
	{
		unregisterManagedObject(getSnmpv2MIB());
		mibDatabase.add(new DISMAN_EVENT_MIBOidTable());
		mibDatabase.add(new HOST_RESOURCES_MIBOidTable());
		mibDatabase.add(new IF_MIBOidTable());
		mibDatabase.add(new IP_MIBOidTable());
		mibDatabase.add(new RFC1213_MIBOidTable());
		mibDatabase.add(new SNMPv2_MIBOidTable());
		mibDatabase.add(new TCP_MIBOidTable());
		mibDatabase.add(new UDP_MIBOidTable());
	}

	public void unregisterManagedObject(MOGroup managedObjectGroup)
	{
		managedObjectGroup.unregisterMOs(server, getContext(managedObjectGroup));
	}

	@Override
	protected void unregisterManagedObjects()
	{
	}

	@Override
	protected void initTransportMappings() throws IOException
	{
		transportMappings = new TransportMapping<?>[1];
		Address address = GenericAddress.parse(this.address);
		TransportMapping<?> transportMapping = TransportMappings.getInstance().createTransportMapping(address);
		transportMappings[0] = transportMapping;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// CommandResponder implementation
	//
	// /////////////////////////////////

	@Override
	public void processPdu(CommandResponderEvent event)
	{
		// Handles the get snmp message type
		switch (event.getPDU().getType())
		{
			// Someone is querying us
			case PDU.GET:

				// Set the response target
				CommunityTarget target = getCommunityTarget();
				target.setAddress(event.getPeerAddress());

				// Create the PDU
				PDU pdu = event.getPDU();

				// Set type of PDU
				pdu.setType(PDU.RESPONSE);

				try
				{
					// Set the value for the query
					pdu.get(0).setVariable(new OctetString(resolveValue(pdu)));
				}
				catch (Exception exc)
				{
					logger.error("Could not resolve the OID: {}", exc.getLocalizedMessage());
				}
				try
				{
					// Send the response back to the address
					session.send(pdu, target);
				}
				catch (IOException exc)
				{
					logger.error("Could not send the response: {}", exc.getLocalizedMessage());
				}

				break;
			case PDU.NOTIFICATION:
				break;
		}

		// Must set the event being processed
		event.setProcessed(true);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// ISnmpAgent implementation
	//
	// /////////////////////////////////

	@Override
	public void start() throws Exception
	{
		// Initialise the agent.
		init();

		addShutdownHook();
		getServer().addContext(new OctetString(community));
		
		// Add the command responder where processing of the PDU will take place.
		session.addCommandResponder(responder);
		loadPresetOIDS();

		finishInit();

		run();
		sendColdStartNotification();
	}

	@Override
	public void stop()
	{
		// Close the session if no null
		if (session == null)
		{
			return;
		}
		try
		{
			session.close();
		}
		catch (IOException exc)
		{
			logger.error("Could not close the session: {}", exc.getLocalizedMessage());
		}
	}

	// Sets the target address
	@Override
	public void setTargetAddress(int target, String targetAddress)
	{
		logger.trace("SnmpAgent: target = {}, targetAddress = {}", target, targetAddress);
		if (targetAddresses.size() <= target)
		{
			logger.trace("SnmpAgent: target = {}, targetAddress = {} -> add", target, targetAddress);
			targetAddresses.add(target, targetAddress);
			return;
		}
		logger.trace("SnmpAgent: target = {}, targetAddress = {} -> set", target, targetAddress);
		targetAddresses.set(target, targetAddress);
	}

	@Override
	public void setCommunity(String community)
	{
		this.community = community;
	}

	@Override
	public void setRetries(int retries)
	{
		this.retries = retries;
	}

	@Override
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public void setResponder(CommandResponder responder)
	{
		this.responder = responder;
	}

	@Override
	public SnmpMibDatbase getDatabase()
	{
		return this.mibDatabase;
	}

	@Override
	public boolean isWorking()
	{
		// Check that the agent is running
		if (agentState != SnmpAgent.STATE_RUNNING)
		{
			return false;
		}

		// Check for null elements
		if (session == null || server == null || dispatcher == null || transportMappings == null)
		{
			return false;
		}

		// Ensure that the message dispatcher is checking whether outgoing messages are checked
		if (!dispatcher.isCheckOutgoingMsg())
		{
			return false;
		}

		// Check whether the snmp agent is listening for messages
		if (transportMappings.length <= 0 || !transportMappings[0].isListening())
		{
			return false;
		}

		return true;
	}

	@Override
	public void incident(TrapCodes code, String element, IncidentSeverity severity, String description) throws SnmpStatusException
	{
		// De-Duplicate Clear
		if (deDuplicate(element, severity))
			return;

		logger.trace("SnmpAgent.incident: code = {}, element = {}, severity = {}, description = {}", code, element, severity, description);

		// Create the community target
		CommunityTarget target = getCommunityTarget();

		// Get the trap record
		SnmpOidRecord record = (SnmpOidRecord) mibDatabase.getRecordWithName(code.toString());

		// Create a basic Trap v2 PDU
		PDU pdu = createPDU(record);

		// Add the incident to the PDU
		createIncident(pdu, element, severity, description);

		try
		{
			// Send the trap
			sendPdu(pdu, target);
		}
		catch (IOException exc)
		{
			logger.error("Could not send the incident: {}", exc.getLocalizedMessage());
		}
	}

	@Override
	public void indication(TrapCodes code, String element, IndicationState state, IncidentSeverity severity, String description) throws SnmpStatusException
	{
		// De-Duplicate Clear
		if (deDuplicate(element, severity))
			return;

		logger.trace("SnmpAgent.indication: code = {}, element = {}, state = {}, severity = {}, description = {}", code, element, state, severity, description);

		// Create the community target.
		CommunityTarget target = getCommunityTarget();

		// Get the trap record.
		SnmpOidRecord record = (SnmpOidRecord) mibDatabase.getRecordWithName(code.toString());

		// Create the trap PDU with the record.
		PDU pdu = createPDU(record);

		// Add the indication and the incident to the trap.
		createIndication(pdu, state);
		createIncident(pdu, element, severity, description);

		try
		{
			// Send the finished trap.
			sendPdu(pdu, target);
		}
		catch (IOException exc)
		{
			logger.error("Could not send the indication: {}", exc.getLocalizedMessage());
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Private implementation
	//
	// /////////////////////////////////


	// Load certain OID's to the database
	private int serialNumber = 0;

	private void loadPresetOIDS()
	{
		SnmpTable table = new SnmpTable("Version 2 OID's");
		table.loadMib(new SnmpOidRecord[] { new SnmpOidRecord("System UpTime", uptime.toString(), "S"), new SnmpOidRecord("snmpTrapv2OID", SnmpConstants.snmpTrapOID.toString(), "S") });
		mibDatabase.add(table);
	}

	// Get the serial number.
	private int getSerialNumber()
	{
		if (serialNumber == Integer.MAX_VALUE)
		{
			serialNumber = 0;
		}
		return serialNumber++;
	}

	// Creates a basic community target
	private CommunityTarget getCommunityTarget()
	{
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(SnmpConstants.version2c);
		target.setRetries(retries);
		target.setTimeout(timeout);
		return target;
	}

	// Create the base for the PDU
	private PDU createPDU(SnmpOidRecord record)
	{
		PDU pdu = new PDU();
		
		// Set the PDU to a notification for traps
		pdu.setType(PDU.NOTIFICATION);
		
		// Add the appropriate Variable bindings for a trap v2
		// new TimeTicks(()uptime.getTime()/10))
		TimeTicks sysUpTime = new TimeTicks();
		sysUpTime.fromMilliseconds((new Date().getTime()) - uptime.getTime());
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime, sysUpTime));
		pdu.add(new VariableBinding(new OID(SnmpConstants.snmpTrapOID), new OID(record.getOID())));
		return pdu;
	}

	// Creates the indication which contains the Gist and the State
	private void createIndication(PDU pdu, IndicationState state) throws SnmpStatusException
	{
		// Add the gist to the PDU, gist is based on the indication state
		SnmpOidRecord record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIndicationGist.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(state.gist())));
		
		// Add the state to the PDU
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIndicationState.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new Integer32(state.ordinal())));
	}

	// Create the incident that would be sent with every trap.
	private void createIncident(PDU pdu, String element, IncidentSeverity severity, String description) throws SnmpStatusException
	{
		// Add the incident element to the PDU.
		SnmpOidRecord record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentElement.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(element)));
		
		// Add the incident origin to the PDU, the origin at which the trap originated, usually the hostname of the computer.
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentOrigin.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(defaultHostName)));
		
		// Add the incident timestamp to the PDU, the time at which the trap was sent.
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentTimestamp.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(new Date().toString())));
		
		// Add the incident severity to the PDU, how severe the alarm is. CRITICAL, MAJOR, MINOR, etc.
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentSeverity.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(severity.toString().toLowerCase())));
		
		// Add the incident description to the PDU, the description of the alarm.
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentDescription.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new OctetString(description)));
		
		// Add the incident serial number to the PDU.
		record = (SnmpOidRecord) mibDatabase.getRecordWithName(TrapCodes.csIncidentSerialNumber.toString());
		pdu.add(new VariableBinding(new OID(record.getOID()), new Integer32(getSerialNumber())));
		record = null;
	}

	// Helper method for sending the PDU to the different specified addresses.
	private void sendPdu(PDU pdu, Target target) throws IOException
	{
		logger.trace("SnmpAgent.sendPdu: pdu = {}, target = {}", pdu, target);
		dispatchAlarm(pdu);
		// Iterate through all the valid addresses
		int index = -1;
		logger.trace("SnmpAgent.sendPdu: targetAddresses.size() = {}", targetAddresses.size());
		for (String targetAddress : targetAddresses)
		{
			index++;
			if (targetAddress.length() < 1)
			{
				logger.trace("SnmpAgent.sendPdu: skipping targetAddress[{}] = {}", index, targetAddress);
				continue;
			}
			try
			{
				Address address = TransportIpAddress.parse(targetAddress);
				logger.trace("SnmpAgent.sendPdu: pdu = {}, target = {}, targetAddress = {}, address = {}", pdu, target, targetAddress, address);
				target.setAddress(address);
			}
			catch (Exception exc)
			{
				logger.error("SnmpAgent.sendPdu failed", exc);
				continue;
			}
			
			// Send via the session, which is the snmp.
			logger.trace("SnmpAgent.sendPdu: session.send(): pdu = {}, target = {}", pdu, target);
			ResponseEvent response = session.send(pdu, target);
			logger.trace("SnmpAgent.sendPdu: response = {}", response);
		}
	}

	private void dispatchAlarm(PDU pdu)
	{
		logger.trace("SnmpAgent.dispatchAlarm: pdu = {}", pdu);
		Alarm alarm = null;
		try
		{
			alarm = extractAlarmFromPDU(pdu);
		}
		catch (SnmpStatusException exc)
		{
			logger.error("Could not extract the alarm from the trap: {}", exc.getLocalizedMessage());
		}

		if (esb != null)
		{
			esb.dispatch(alarm, null);
		}
	}

	// Get the value for a particular OID, NOT the name of the OID.
	private String resolveValue(PDU pdu) throws SnmpStatusException
	{
		// Get the record
		SnmpOidRecord record = resolvePDU(pdu);
		return record.getValue().toString();
	}

	// Get the record via the PDU.
	private SnmpOidRecord resolvePDU(PDU pdu) throws SnmpStatusException
	{
		return resolve(pdu.get(0).getOid().toString());
	}

	// Get the record via String.
	private SnmpOidRecord resolve(String value) throws SnmpStatusException
	{
		SnmpOidRecord record = null;
		
		// Check whether you are using the OID OR the name of the OID.
		if (value.contains("."))
		{
			record = (SnmpOidRecord) mibDatabase.getRecordWithOID(value);
		}
		else
		{
			record = (SnmpOidRecord) mibDatabase.getRecordWithName(value);
		}
		return record;
	}

	private Alarm extractAlarmFromPDU(PDU pdu) throws SnmpStatusException
	{
		AlarmType type = (pdu.size() == 8) ? AlarmType.Incident : AlarmType.Indication;
		String name = pdu.getVariable(new OID((resolve(TrapCodes.csIncidentElement.toString())).getOID())).toString();
		String description = pdu.getVariable(new OID((resolve(TrapCodes.csIncidentDescription.toString())).getOID())).toString();
		IncidentSeverity severity = IncidentSeverity.valueOf(pdu.getVariable(new OID((resolve(TrapCodes.csIncidentSeverity.toString())).getOID())).toString().toUpperCase());
		IndicationState state = (pdu.size() == 8) ? null : IndicationState.values()[Integer.parseInt(pdu.getVariable(new OID((resolve(TrapCodes.csIndicationState.toString())).getOID())).toString())];
		return new Alarm(type, name, description, severity, state);
	}
	
	
	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Private implementation
	//
	// /////////////////////////////////

	private boolean deDuplicate(String element, IncidentSeverity severity)
	{
		return false;  // For Now
	}
}

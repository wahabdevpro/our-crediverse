package co.za.concurrent.mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

/**
 * The class contains metadata definitions for "CONCURRENT-SYSTEMS-C4U-MIB". Call SnmpOid.setSnmpOidTable(new CONCURRENT_SYSTEMS_C4U_MIBOidTable()) to load the metadata in the SnmpOidTable.
 */
public class CONCURRENT_SYSTEMS_C4U_MIBOidTable extends SnmpTable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8935507876060509904L;

	/**
	 * Default constructor. Initialize the Mib tree.
	 */
	public CONCURRENT_SYSTEMS_C4U_MIBOidTable()
	{
		super("CONCURRENT_SYSTEMS_C4U_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = {
			// Gist of indication.
			new SnmpOidRecord("csIndicationGist", "1.3.6.1.4.1.28272.16.7.2.1.2", "S"),
			// State of indication (0 => clear, 1+ => set).
			new SnmpOidRecord("csIndicationState", "1.3.6.1.4.1.28272.16.7.2.1.1", "I"),
			// Serial number of incident.
			new SnmpOidRecord("csIncidentSerialNumber", "1.3.6.1.4.1.28272.16.7.1.1.6", "I"),
			// Description of incident.
			new SnmpOidRecord("csIncidentDescription", "1.3.6.1.4.1.28272.16.7.1.1.5", "S"),
			// Severity of incident. Possible values include: clear, unknown, minor, major, critical
			new SnmpOidRecord("csIncidentSeverity", "1.3.6.1.4.1.28272.16.7.1.1.4", "S"),
			// Timestamp of incident.
			new SnmpOidRecord("csIncidentTimestamp", "1.3.6.1.4.1.28272.16.7.1.1.3", "S"),
			// Short name of cluster reporting this incident.
			new SnmpOidRecord("csIncidentOrigin", "1.3.6.1.4.1.28272.16.7.1.1.2", "S"),
			// Full name of element on which incident was observed.
			new SnmpOidRecord("csIncidentElement", "1.3.6.1.4.1.28272.16.7.1.1.1", "S"),
			// The list of possible indications.
			new SnmpOidRecord("csIndicationsGroup", "1.3.6.1.4.1.28272.16.7.0.4", "OBG"),
			// A boundary between a managed element and an external system is
			// 0 => up
			// 1 => down
			new SnmpOidRecord("csElementBoundaryStatus", "1.3.6.1.4.1.28272.16.7.2.0.4", "NT"),
			// A transaction has failed.
			new SnmpOidRecord("csTransactionFailed", "1.3.6.1.4.1.28272.16.7.1.0.2", "NT"),
			// A deadline (e.g. file arrival, batch window) has been exceeded.
			new SnmpOidRecord("csDeadlineExceeded", "1.3.6.1.4.1.28272.16.7.1.0.3", "NT"),
			// A managed element is: 0 => stable
			// 1 => failed away
			// 2 => failed over
			// 3 => fail over failed
			// 4 => restore failed
			// 5 => failed through
			new SnmpOidRecord("csElementDisposition", "1.3.6.1.4.1.28272.16.7.2.0.3", "NT"),
			// A threshold is: 0 => within limits
			// 1 => soft limit breached
			// 2 => hard limit breached
			new SnmpOidRecord("csThresholdStatus", "1.3.6.1.4.1.28272.16.7.2.0.6", "NT"),
			// A managed element is 0 => managed
			// 1 => impaired
			new SnmpOidRecord("csElementManagementStatus", "1.3.6.1.4.1.28272.16.7.2.0.2", "NT"),
			// The list of fields required in each indication trap.
			new SnmpOidRecord("csIndicationFieldsGroup", "1.3.6.1.4.1.28272.16.7.0.2", "OBG"),
			// The list of fields required in each incident trap.
			new SnmpOidRecord("csIncidentFieldsGroup", "1.3.6.1.4.1.28272.16.7.0.1", "OBG"),
			// The list of possible incidents.
			new SnmpOidRecord("csIncidentsGroup", "1.3.6.1.4.1.28272.16.7.0.3", "OBG"),
			// A managed element is 0 => in-service
			// 1 => out-of-service
			new SnmpOidRecord("csElementServiceStatus", "1.3.6.1.4.1.28272.16.7.2.0.1", "NT"),
			// A job has failed.
			new SnmpOidRecord("csJobFailed", "1.3.6.1.4.1.28272.16.7.1.0.1", "NT"),
			// A managed element is: 0 => started
			// 1 => starting
			// 2 => stopping
			// 3 => stopped
			// 4 => failed
			new SnmpOidRecord("csTaskExecutionStatus", "1.3.6.1.4.1.28272.16.7.2.0.5", "NT") };
}

package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;
import hxc.servicebus.HostInfo;

public class SNMPv2_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = -3901556585905238892L;

	private static String hostname;

	public SNMPv2_MIBOidTable()
	{
		super("SNMPv2_MIB");
		hostname = HostInfo.getNameOrElseHxC();

		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("snmpSetSerialNo", "1.3.6.1.6.3.1.1.6.1", "I"), new SnmpOidRecord("snmpTrapEnterprise", "1.3.6.1.6.3.1.1.4.3", "OI"),
			new SnmpOidRecord("snmpTrapOID", "1.3.6.1.6.3.1.1.4.1", "OI"), new SnmpOidRecord("snmpProxyDrops", "1.3.6.1.2.1.11.32", "C"),
			new SnmpOidRecord("snmpSilentDrops", "1.3.6.1.2.1.11.31", "C"), new SnmpOidRecord("snmpEnableAuthenTraps", "1.3.6.1.2.1.11.30", "I"),
			new SnmpOidRecord("snmpInTraps", "1.3.6.1.2.1.11.19", "C"), new SnmpOidRecord("snmpInGetResponses", "1.3.6.1.2.1.11.18", "C"),
			new SnmpOidRecord("snmpInSetRequests", "1.3.6.1.2.1.11.17", "C"), new SnmpOidRecord("snmpInGetNexts", "1.3.6.1.2.1.11.16", "C"),
			new SnmpOidRecord("snmpInGetRequests", "1.3.6.1.2.1.11.15", "C"), new SnmpOidRecord("snmpInTotalSetVars", "1.3.6.1.2.1.11.14", "C"),
			new SnmpOidRecord("snmpInTotalReqVars", "1.3.6.1.2.1.11.13", "C"), new SnmpOidRecord("snmpInGenErrs", "1.3.6.1.2.1.11.12", "C"),
			new SnmpOidRecord("snmpInReadOnlys", "1.3.6.1.2.1.11.11", "C"), new SnmpOidRecord("snmpInBadValues", "1.3.6.1.2.1.11.10", "C"),
			new SnmpOidRecord("snmpInNoSuchNames", "1.3.6.1.2.1.11.9", "C"), new SnmpOidRecord("snmpInTooBigs", "1.3.6.1.2.1.11.8", "C"), new SnmpOidRecord("snmpOutTraps", "1.3.6.1.2.1.11.29", "C"),
			new SnmpOidRecord("snmpOutGetResponses", "1.3.6.1.2.1.11.28", "C"), new SnmpOidRecord("snmpInASNParseErrs", "1.3.6.1.2.1.11.6", "C"),
			new SnmpOidRecord("snmpOutSetRequests", "1.3.6.1.2.1.11.27", "C"), new SnmpOidRecord("snmpInBadCommunityUses", "1.3.6.1.2.1.11.5", "C"),
			new SnmpOidRecord("snmpOutGetNexts", "1.3.6.1.2.1.11.26", "C"), new SnmpOidRecord("snmpOutGetRequests", "1.3.6.1.2.1.11.25", "C"),
			new SnmpOidRecord("snmpInBadCommunityNames", "1.3.6.1.2.1.11.4", "C"), new SnmpOidRecord("snmpOutGenErrs", "1.3.6.1.2.1.11.24", "C"),
			new SnmpOidRecord("snmpInBadVersions", "1.3.6.1.2.1.11.3", "C"), new SnmpOidRecord("snmpOutPkts", "1.3.6.1.2.1.11.2", "C"),
			new SnmpOidRecord("snmpOutBadValues", "1.3.6.1.2.1.11.22", "C"), new SnmpOidRecord("snmpInPkts", "1.3.6.1.2.1.11.1", "C"),
			new SnmpOidRecord("snmpOutNoSuchNames", "1.3.6.1.2.1.11.21", "C"), new SnmpOidRecord("snmpOutTooBigs", "1.3.6.1.2.1.11.20", "C"), new SnmpOidRecord("sysORTable", "1.3.6.1.2.1.1.9", "TA"),
			new SnmpOidRecord("sysOREntry", "1.3.6.1.2.1.1.9.1", "EN"), new SnmpOidRecord("sysORUpTime", "1.3.6.1.2.1.1.9.1.4", "T"), new SnmpOidRecord("sysORDescr", "1.3.6.1.2.1.1.9.1.3", "S"),
			new SnmpOidRecord("sysORID", "1.3.6.1.2.1.1.9.1.2", "OI"), new SnmpOidRecord("sysORIndex", "1.3.6.1.2.1.1.9.1.1", "I"), new SnmpOidRecord("sysORLastChange", "1.3.6.1.2.1.1.8", "T"),
			new SnmpOidRecord("sysServices", "1.3.6.1.2.1.1.7", "I"), new SnmpOidRecord("sysLocation", "1.3.6.1.2.1.1.6", "S"), new SnmpOidRecord("sysName", "1.3.6.1.2.1.1.5", "S", hostname),
			new SnmpOidRecord("sysContact", "1.3.6.1.2.1.1.4", "S", "Unknown"), new SnmpOidRecord("sysUpTime", "1.3.6.1.2.1.1.3", "T", new java.util.Date().toString()),
			new SnmpOidRecord("sysObjectID", "1.3.6.1.2.1.1.2", "OI"), new SnmpOidRecord("sysDescr", "1.3.6.1.2.1.1.1", "S", "Description of the System"),
			new SnmpOidRecord("snmpWarmStartNotificationGroup", "1.3.6.1.6.3.1.2.2.11", "OBG"), new SnmpOidRecord("systemGroup", "1.3.6.1.6.3.1.2.2.6", "OBG"),
			new SnmpOidRecord("warmStart", "1.3.6.1.6.3.1.1.5.2", "NT"), new SnmpOidRecord("snmpSetGroup", "1.3.6.1.6.3.1.2.2.5", "OBG"),
			new SnmpOidRecord("snmpCommunityGroup", "1.3.6.1.6.3.1.2.2.9", "OBG"), new SnmpOidRecord("snmpObsoleteGroup", "1.3.6.1.6.3.1.2.2.10", "OBG"),
			new SnmpOidRecord("authenticationFailure", "1.3.6.1.6.3.1.1.5.5", "NT"), new SnmpOidRecord("snmpBasicNotificationsGroup", "1.3.6.1.6.3.1.2.2.7", "OBG"),
			new SnmpOidRecord("snmpGroup", "1.3.6.1.6.3.1.2.2.8", "OBG"), new SnmpOidRecord("coldStart", "1.3.6.1.6.3.1.1.5.1", "NT"),
			new SnmpOidRecord("snmpNotificationGroup", "1.3.6.1.6.3.1.2.2.12", "OBG") };
}

package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class IF_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = 8163731298646384993L;

	public IF_MIBOidTable()
	{
		super("IF_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("ifTable", "1.3.6.1.2.1.2.2", "TA"), new SnmpOidRecord("ifEntry", "1.3.6.1.2.1.2.2.1", "EN"),
			new SnmpOidRecord("ifOutDiscards", "1.3.6.1.2.1.2.2.1.19", "C"), new SnmpOidRecord("ifOutNUcastPkts", "1.3.6.1.2.1.2.2.1.18", "C"),
			new SnmpOidRecord("ifOutUcastPkts", "1.3.6.1.2.1.2.2.1.17", "C"), new SnmpOidRecord("ifOutOctets", "1.3.6.1.2.1.2.2.1.16", "C"),
			new SnmpOidRecord("ifInUnknownProtos", "1.3.6.1.2.1.2.2.1.15", "C"), new SnmpOidRecord("ifInErrors", "1.3.6.1.2.1.2.2.1.14", "C"),
			new SnmpOidRecord("ifInDiscards", "1.3.6.1.2.1.2.2.1.13", "C"), new SnmpOidRecord("ifInNUcastPkts", "1.3.6.1.2.1.2.2.1.12", "C"),
			new SnmpOidRecord("ifInUcastPkts", "1.3.6.1.2.1.2.2.1.11", "C"), new SnmpOidRecord("ifInOctets", "1.3.6.1.2.1.2.2.1.10", "C"),
			new SnmpOidRecord("ifLastChange", "1.3.6.1.2.1.2.2.1.9", "T"), new SnmpOidRecord("ifOperStatus", "1.3.6.1.2.1.2.2.1.8", "I"),
			new SnmpOidRecord("ifAdminStatus", "1.3.6.1.2.1.2.2.1.7", "I"), new SnmpOidRecord("ifPhysAddress", "1.3.6.1.2.1.2.2.1.6", "S"), new SnmpOidRecord("ifSpeed", "1.3.6.1.2.1.2.2.1.5", "G"),
			new SnmpOidRecord("ifMtu", "1.3.6.1.2.1.2.2.1.4", "I"), new SnmpOidRecord("ifType", "1.3.6.1.2.1.2.2.1.3", "I"), new SnmpOidRecord("ifDescr", "1.3.6.1.2.1.2.2.1.2", "S"),
			new SnmpOidRecord("ifIndex", "1.3.6.1.2.1.2.2.1.1", "I"), new SnmpOidRecord("ifSpecific", "1.3.6.1.2.1.2.2.1.22", "OI"), new SnmpOidRecord("ifOutQLen", "1.3.6.1.2.1.2.2.1.21", "G"),
			new SnmpOidRecord("ifOutErrors", "1.3.6.1.2.1.2.2.1.20", "C"), new SnmpOidRecord("ifNumber", "1.3.6.1.2.1.2.1", "I"), new SnmpOidRecord("ifStackLastChange", "1.3.6.1.2.1.31.1.6", "T"),
			new SnmpOidRecord("ifTableLastChange", "1.3.6.1.2.1.31.1.5", "T"), new SnmpOidRecord("ifRcvAddressTable", "1.3.6.1.2.1.31.1.4", "TA"),
			new SnmpOidRecord("ifRcvAddressEntry", "1.3.6.1.2.1.31.1.4.1", "EN"), new SnmpOidRecord("ifRcvAddressType", "1.3.6.1.2.1.31.1.4.1.3", "I"),
			new SnmpOidRecord("ifRcvAddressStatus", "1.3.6.1.2.1.31.1.4.1.2", "I"), new SnmpOidRecord("ifRcvAddressAddress", "1.3.6.1.2.1.31.1.4.1.1", "S"),
			new SnmpOidRecord("ifTestTable", "1.3.6.1.2.1.31.1.3", "TA"), new SnmpOidRecord("ifTestEntry", "1.3.6.1.2.1.31.1.3.1", "EN"),
			new SnmpOidRecord("ifTestOwner", "1.3.6.1.2.1.31.1.3.1.6", "S"), new SnmpOidRecord("ifTestCode", "1.3.6.1.2.1.31.1.3.1.5", "OI"),
			new SnmpOidRecord("ifTestResult", "1.3.6.1.2.1.31.1.3.1.4", "I"), new SnmpOidRecord("ifTestType", "1.3.6.1.2.1.31.1.3.1.3", "OI"),
			new SnmpOidRecord("ifTestStatus", "1.3.6.1.2.1.31.1.3.1.2", "I"), new SnmpOidRecord("ifTestId", "1.3.6.1.2.1.31.1.3.1.1", "I"),
			new SnmpOidRecord("ifStackTable", "1.3.6.1.2.1.31.1.2", "TA"), new SnmpOidRecord("ifStackEntry", "1.3.6.1.2.1.31.1.2.1", "EN"),
			new SnmpOidRecord("ifStackStatus", "1.3.6.1.2.1.31.1.2.1.3", "I"), new SnmpOidRecord("ifStackLowerLayer", "1.3.6.1.2.1.31.1.2.1.2", "I"),
			new SnmpOidRecord("ifStackHigherLayer", "1.3.6.1.2.1.31.1.2.1.1", "I"), new SnmpOidRecord("ifXTable", "1.3.6.1.2.1.31.1.1", "TA"),
			new SnmpOidRecord("ifXEntry", "1.3.6.1.2.1.31.1.1.1", "EN"), new SnmpOidRecord("ifCounterDiscontinuityTime", "1.3.6.1.2.1.31.1.1.1.19", "T"),
			new SnmpOidRecord("ifAlias", "1.3.6.1.2.1.31.1.1.1.18", "S"), new SnmpOidRecord("ifConnectorPresent", "1.3.6.1.2.1.31.1.1.1.17", "I"),
			new SnmpOidRecord("ifPromiscuousMode", "1.3.6.1.2.1.31.1.1.1.16", "I"), new SnmpOidRecord("ifHighSpeed", "1.3.6.1.2.1.31.1.1.1.15", "G"),
			new SnmpOidRecord("ifLinkUpDownTrapEnable", "1.3.6.1.2.1.31.1.1.1.14", "I"), new SnmpOidRecord("ifHCOutBroadcastPkts", "1.3.6.1.2.1.31.1.1.1.13", "C64"),
			new SnmpOidRecord("ifHCOutMulticastPkts", "1.3.6.1.2.1.31.1.1.1.12", "C64"), new SnmpOidRecord("ifHCOutUcastPkts", "1.3.6.1.2.1.31.1.1.1.11", "C64"),
			new SnmpOidRecord("ifHCOutOctets", "1.3.6.1.2.1.31.1.1.1.10", "C64"), new SnmpOidRecord("ifHCInBroadcastPkts", "1.3.6.1.2.1.31.1.1.1.9", "C64"),
			new SnmpOidRecord("ifHCInMulticastPkts", "1.3.6.1.2.1.31.1.1.1.8", "C64"), new SnmpOidRecord("ifHCInUcastPkts", "1.3.6.1.2.1.31.1.1.1.7", "C64"),
			new SnmpOidRecord("ifHCInOctets", "1.3.6.1.2.1.31.1.1.1.6", "C64"), new SnmpOidRecord("ifOutBroadcastPkts", "1.3.6.1.2.1.31.1.1.1.5", "C"),
			new SnmpOidRecord("ifOutMulticastPkts", "1.3.6.1.2.1.31.1.1.1.4", "C"), new SnmpOidRecord("ifInBroadcastPkts", "1.3.6.1.2.1.31.1.1.1.3", "C"),
			new SnmpOidRecord("ifInMulticastPkts", "1.3.6.1.2.1.31.1.1.1.2", "C"), new SnmpOidRecord("ifName", "1.3.6.1.2.1.31.1.1.1.1", "S"),
			new SnmpOidRecord("ifGeneralInformationGroup", "1.3.6.1.2.1.31.2.1.10", "OBG"), new SnmpOidRecord("ifVHCPacketGroup", "1.3.6.1.2.1.31.2.1.6", "OBG"),
			new SnmpOidRecord("linkDown", "1.3.6.1.6.3.1.1.5.3", "NT"), new SnmpOidRecord("ifOldObjectsGroup", "1.3.6.1.2.1.31.2.1.12", "OBG"),
			new SnmpOidRecord("ifRcvAddressGroup", "1.3.6.1.2.1.31.2.1.7", "OBG"), new SnmpOidRecord("linkUp", "1.3.6.1.6.3.1.1.5.4", "NT"),
			new SnmpOidRecord("ifGeneralGroup", "1.3.6.1.2.1.31.2.1.1", "OBG"), new SnmpOidRecord("ifHCPacketGroup", "1.3.6.1.2.1.31.2.1.5", "OBG"),
			new SnmpOidRecord("linkUpDownNotificationsGroup", "1.3.6.1.2.1.31.2.1.14", "OBG"), new SnmpOidRecord("ifCounterDiscontinuityGroup", "1.3.6.1.2.1.31.2.1.13", "OBG"),
			new SnmpOidRecord("ifStackGroup", "1.3.6.1.2.1.31.2.1.9", "OBG"), new SnmpOidRecord("ifFixedLengthGroup", "1.3.6.1.2.1.31.2.1.2", "OBG"),
			new SnmpOidRecord("ifPacketGroup", "1.3.6.1.2.1.31.2.1.4", "OBG"), new SnmpOidRecord("ifHCFixedLengthGroup", "1.3.6.1.2.1.31.2.1.3", "OBG"),
			new SnmpOidRecord("ifStackGroup2", "1.3.6.1.2.1.31.2.1.11", "OBG"), new SnmpOidRecord("ifTestGroup", "1.3.6.1.2.1.31.2.1.8", "OBG") };
}

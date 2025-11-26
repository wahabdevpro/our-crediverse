package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class UDP_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = 6016270458992154593L;

	public UDP_MIBOidTable()
	{
		super("UDP_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("udpTable", "1.3.6.1.2.1.7.5", "TA"), new SnmpOidRecord("udpEntry", "1.3.6.1.2.1.7.5.1", "EN"),
			new SnmpOidRecord("udpLocalPort", "1.3.6.1.2.1.7.5.1.2", "I"), new SnmpOidRecord("udpLocalAddress", "1.3.6.1.2.1.7.5.1.1", "IP"),
			new SnmpOidRecord("udpOutDatagrams", "1.3.6.1.2.1.7.4", "C"), new SnmpOidRecord("udpInErrors", "1.3.6.1.2.1.7.3", "C"), new SnmpOidRecord("udpNoPorts", "1.3.6.1.2.1.7.2", "C"),
			new SnmpOidRecord("udpInDatagrams", "1.3.6.1.2.1.7.1", "C"), new SnmpOidRecord("udpHCOutDatagrams", "1.3.6.1.2.1.7.9", "C64"),
			new SnmpOidRecord("udpHCInDatagrams", "1.3.6.1.2.1.7.8", "C64"), new SnmpOidRecord("udpEndpointTable", "1.3.6.1.2.1.7.7", "TA"),
			new SnmpOidRecord("udpEndpointEntry", "1.3.6.1.2.1.7.7.1", "EN"), new SnmpOidRecord("udpEndpointRemotePort", "1.3.6.1.2.1.7.7.1.6", "G"),
			new SnmpOidRecord("udpEndpointRemoteAddress", "1.3.6.1.2.1.7.7.1.5", "S"), new SnmpOidRecord("udpEndpointRemoteAddressType", "1.3.6.1.2.1.7.7.1.4", "I"),
			new SnmpOidRecord("udpEndpointLocalPort", "1.3.6.1.2.1.7.7.1.3", "G"), new SnmpOidRecord("udpEndpointLocalAddress", "1.3.6.1.2.1.7.7.1.2", "S"),
			new SnmpOidRecord("udpEndpointLocalAddressType", "1.3.6.1.2.1.7.7.1.1", "I"), new SnmpOidRecord("udpEndpointProcess", "1.3.6.1.2.1.7.7.1.8", "G"),
			new SnmpOidRecord("udpEndpointInstance", "1.3.6.1.2.1.7.7.1.7", "G"), new SnmpOidRecord("udpEndpointGroup", "1.3.6.1.2.1.50.2.2.4", "OBG"),
			new SnmpOidRecord("udpHCGroup", "1.3.6.1.2.1.50.2.2.3", "OBG"), new SnmpOidRecord("udpBaseGroup", "1.3.6.1.2.1.50.2.2.2", "OBG"),
			new SnmpOidRecord("udpGroup", "1.3.6.1.2.1.50.2.2.1", "OBG") };
}

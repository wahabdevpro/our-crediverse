package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class TCP_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = -1255525292475386777L;

	public TCP_MIBOidTable()
	{
		super("TCP_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("tcpConnectionTable", "1.3.6.1.2.1.6.19", "TA"), new SnmpOidRecord("tcpConnectionEntry", "1.3.6.1.2.1.6.19.1", "EN"),
			new SnmpOidRecord("tcpConnectionRemPort", "1.3.6.1.2.1.6.19.1.6", "G"), new SnmpOidRecord("tcpConnectionRemAddress", "1.3.6.1.2.1.6.19.1.5", "S"),
			new SnmpOidRecord("tcpConnectionRemAddressType", "1.3.6.1.2.1.6.19.1.4", "I"), new SnmpOidRecord("tcpConnectionLocalPort", "1.3.6.1.2.1.6.19.1.3", "G"),
			new SnmpOidRecord("tcpConnectionLocalAddress", "1.3.6.1.2.1.6.19.1.2", "S"), new SnmpOidRecord("tcpConnectionLocalAddressType", "1.3.6.1.2.1.6.19.1.1", "I"),
			new SnmpOidRecord("tcpConnectionProcess", "1.3.6.1.2.1.6.19.1.8", "G"), new SnmpOidRecord("tcpConnectionState", "1.3.6.1.2.1.6.19.1.7", "I"),
			new SnmpOidRecord("tcpHCOutSegs", "1.3.6.1.2.1.6.18", "C64"), new SnmpOidRecord("tcpHCInSegs", "1.3.6.1.2.1.6.17", "C64"), new SnmpOidRecord("tcpOutRsts", "1.3.6.1.2.1.6.15", "C"),
			new SnmpOidRecord("tcpInErrs", "1.3.6.1.2.1.6.14", "C"), new SnmpOidRecord("tcpConnTable", "1.3.6.1.2.1.6.13", "TA"), new SnmpOidRecord("tcpConnEntry", "1.3.6.1.2.1.6.13.1", "EN"),
			new SnmpOidRecord("tcpConnRemPort", "1.3.6.1.2.1.6.13.1.5", "I"), new SnmpOidRecord("tcpConnRemAddress", "1.3.6.1.2.1.6.13.1.4", "IP"),
			new SnmpOidRecord("tcpConnLocalPort", "1.3.6.1.2.1.6.13.1.3", "I"), new SnmpOidRecord("tcpConnLocalAddress", "1.3.6.1.2.1.6.13.1.2", "IP"),
			new SnmpOidRecord("tcpConnState", "1.3.6.1.2.1.6.13.1.1", "I"), new SnmpOidRecord("tcpRetransSegs", "1.3.6.1.2.1.6.12", "C"), new SnmpOidRecord("tcpOutSegs", "1.3.6.1.2.1.6.11", "C"),
			new SnmpOidRecord("tcpInSegs", "1.3.6.1.2.1.6.10", "C"), new SnmpOidRecord("tcpCurrEstab", "1.3.6.1.2.1.6.9", "G"), new SnmpOidRecord("tcpEstabResets", "1.3.6.1.2.1.6.8", "C"),
			new SnmpOidRecord("tcpAttemptFails", "1.3.6.1.2.1.6.7", "C"), new SnmpOidRecord("tcpPassiveOpens", "1.3.6.1.2.1.6.6", "C"), new SnmpOidRecord("tcpActiveOpens", "1.3.6.1.2.1.6.5", "C"),
			new SnmpOidRecord("tcpMaxConn", "1.3.6.1.2.1.6.4", "I"), new SnmpOidRecord("tcpRtoMax", "1.3.6.1.2.1.6.3", "I"), new SnmpOidRecord("tcpRtoMin", "1.3.6.1.2.1.6.2", "I"),
			new SnmpOidRecord("tcpRtoAlgorithm", "1.3.6.1.2.1.6.1", "I"), new SnmpOidRecord("tcpListenerTable", "1.3.6.1.2.1.6.20", "TA"),
			new SnmpOidRecord("tcpListenerEntry", "1.3.6.1.2.1.6.20.1", "EN"), new SnmpOidRecord("tcpListenerProcess", "1.3.6.1.2.1.6.20.1.4", "G"),
			new SnmpOidRecord("tcpListenerLocalPort", "1.3.6.1.2.1.6.20.1.3", "G"), new SnmpOidRecord("tcpListenerLocalAddress", "1.3.6.1.2.1.6.20.1.2", "S"),
			new SnmpOidRecord("tcpListenerLocalAddressType", "1.3.6.1.2.1.6.20.1.1", "I"), new SnmpOidRecord("tcpBaseGroup", "1.3.6.1.2.1.49.2.2.2", "OBG"),
			new SnmpOidRecord("tcpConnectionGroup", "1.3.6.1.2.1.49.2.2.3", "OBG"), new SnmpOidRecord("tcpGroup", "1.3.6.1.2.1.49.2.2.1", "OBG"),
			new SnmpOidRecord("tcpListenerGroup", "1.3.6.1.2.1.49.2.2.4", "OBG"), new SnmpOidRecord("tcpHCGroup", "1.3.6.1.2.1.49.2.2.5", "OBG") };
}

package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class IP_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = -7925800187660692209L;

	public IP_MIBOidTable()
	{
		super("IP_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("icmpOutSrcQuenchs", "1.3.6.1.2.1.5.19", "C"), new SnmpOidRecord("icmpOutParmProbs", "1.3.6.1.2.1.5.18", "C"),
			new SnmpOidRecord("icmpOutTimeExcds", "1.3.6.1.2.1.5.17", "C"), new SnmpOidRecord("icmpOutDestUnreachs", "1.3.6.1.2.1.5.16", "C"),
			new SnmpOidRecord("icmpOutErrors", "1.3.6.1.2.1.5.15", "C"), new SnmpOidRecord("icmpOutMsgs", "1.3.6.1.2.1.5.14", "C"), new SnmpOidRecord("icmpInAddrMaskReps", "1.3.6.1.2.1.5.13", "C"),
			new SnmpOidRecord("icmpInAddrMasks", "1.3.6.1.2.1.5.12", "C"), new SnmpOidRecord("icmpInTimestampReps", "1.3.6.1.2.1.5.11", "C"),
			new SnmpOidRecord("icmpInTimestamps", "1.3.6.1.2.1.5.10", "C"), new SnmpOidRecord("icmpInEchoReps", "1.3.6.1.2.1.5.9", "C"), new SnmpOidRecord("icmpInEchos", "1.3.6.1.2.1.5.8", "C"),
			new SnmpOidRecord("icmpInRedirects", "1.3.6.1.2.1.5.7", "C"), new SnmpOidRecord("icmpInSrcQuenchs", "1.3.6.1.2.1.5.6", "C"), new SnmpOidRecord("icmpInParmProbs", "1.3.6.1.2.1.5.5", "C"),
			new SnmpOidRecord("icmpOutAddrMaskReps", "1.3.6.1.2.1.5.26", "C"), new SnmpOidRecord("icmpInTimeExcds", "1.3.6.1.2.1.5.4", "C"),
			new SnmpOidRecord("icmpOutAddrMasks", "1.3.6.1.2.1.5.25", "C"), new SnmpOidRecord("icmpInDestUnreachs", "1.3.6.1.2.1.5.3", "C"),
			new SnmpOidRecord("icmpOutTimestampReps", "1.3.6.1.2.1.5.24", "C"), new SnmpOidRecord("icmpInErrors", "1.3.6.1.2.1.5.2", "C"),
			new SnmpOidRecord("icmpOutTimestamps", "1.3.6.1.2.1.5.23", "C"), new SnmpOidRecord("icmpInMsgs", "1.3.6.1.2.1.5.1", "C"), new SnmpOidRecord("icmpOutEchoReps", "1.3.6.1.2.1.5.22", "C"),
			new SnmpOidRecord("icmpOutEchos", "1.3.6.1.2.1.5.21", "C"), new SnmpOidRecord("icmpOutRedirects", "1.3.6.1.2.1.5.20", "C"), new SnmpOidRecord("ipFragCreates", "1.3.6.1.2.1.4.19", "C"),
			new SnmpOidRecord("ipFragFails", "1.3.6.1.2.1.4.18", "C"), new SnmpOidRecord("ipFragOKs", "1.3.6.1.2.1.4.17", "C"), new SnmpOidRecord("ipReasmFails", "1.3.6.1.2.1.4.16", "C"),
			new SnmpOidRecord("ipReasmOKs", "1.3.6.1.2.1.4.15", "C"), new SnmpOidRecord("ipReasmReqds", "1.3.6.1.2.1.4.14", "C"), new SnmpOidRecord("ipReasmTimeout", "1.3.6.1.2.1.4.13", "I"),
			new SnmpOidRecord("ipOutNoRoutes", "1.3.6.1.2.1.4.12", "C"), new SnmpOidRecord("ipOutDiscards", "1.3.6.1.2.1.4.11", "C"), new SnmpOidRecord("ipOutRequests", "1.3.6.1.2.1.4.10", "C"),
			new SnmpOidRecord("ipInDelivers", "1.3.6.1.2.1.4.9", "C"), new SnmpOidRecord("ipInDiscards", "1.3.6.1.2.1.4.8", "C"), new SnmpOidRecord("ipInUnknownProtos", "1.3.6.1.2.1.4.7", "C"),
			new SnmpOidRecord("ipForwDatagrams", "1.3.6.1.2.1.4.6", "C"), new SnmpOidRecord("ipInAddrErrors", "1.3.6.1.2.1.4.5", "C"), new SnmpOidRecord("ipInHdrErrors", "1.3.6.1.2.1.4.4", "C"),
			new SnmpOidRecord("ipInReceives", "1.3.6.1.2.1.4.3", "C"), new SnmpOidRecord("ipDefaultTTL", "1.3.6.1.2.1.4.2", "I"), new SnmpOidRecord("ipRoutingDiscards", "1.3.6.1.2.1.4.23", "C"),
			new SnmpOidRecord("ipForwarding", "1.3.6.1.2.1.4.1", "I"), new SnmpOidRecord("ipNetToMediaTable", "1.3.6.1.2.1.4.22", "TA"),
			new SnmpOidRecord("ipNetToMediaEntry", "1.3.6.1.2.1.4.22.1", "EN"), new SnmpOidRecord("ipNetToMediaType", "1.3.6.1.2.1.4.22.1.4", "I"),
			new SnmpOidRecord("ipNetToMediaNetAddress", "1.3.6.1.2.1.4.22.1.3", "IP"), new SnmpOidRecord("ipNetToMediaPhysAddress", "1.3.6.1.2.1.4.22.1.2", "S"),
			new SnmpOidRecord("ipNetToMediaIfIndex", "1.3.6.1.2.1.4.22.1.1", "I"), new SnmpOidRecord("ipAddrTable", "1.3.6.1.2.1.4.20", "TA"),
			new SnmpOidRecord("ipAddrEntry", "1.3.6.1.2.1.4.20.1", "EN"), new SnmpOidRecord("ipAdEntReasmMaxSize", "1.3.6.1.2.1.4.20.1.5", "I"),
			new SnmpOidRecord("ipAdEntBcastAddr", "1.3.6.1.2.1.4.20.1.4", "I"), new SnmpOidRecord("ipAdEntNetMask", "1.3.6.1.2.1.4.20.1.3", "IP"),
			new SnmpOidRecord("ipAdEntIfIndex", "1.3.6.1.2.1.4.20.1.2", "I"), new SnmpOidRecord("ipAdEntAddr", "1.3.6.1.2.1.4.20.1.1", "IP"),
			new SnmpOidRecord("ipGroup", "1.3.6.1.2.1.48.2.2.1", "OBG"), new SnmpOidRecord("icmpGroup", "1.3.6.1.2.1.48.2.2.2", "OBG") };
}

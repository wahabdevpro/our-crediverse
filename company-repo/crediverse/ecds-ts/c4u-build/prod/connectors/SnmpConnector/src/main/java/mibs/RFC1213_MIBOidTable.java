package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class RFC1213_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = -4177408135613252260L;

	public RFC1213_MIBOidTable()
	{
		super("RFC1213_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("egpAs", "1.3.6.1.2.1.8.6", "I"), new SnmpOidRecord("egpNeighTable", "1.3.6.1.2.1.8.5", "TA"),
			new SnmpOidRecord("egpNeighEntry", "1.3.6.1.2.1.8.5.1", "EN"), new SnmpOidRecord("egpNeighOutErrMsgs", "1.3.6.1.2.1.8.5.1.9", "C"),
			new SnmpOidRecord("egpNeighInErrMsgs", "1.3.6.1.2.1.8.5.1.8", "C"), new SnmpOidRecord("egpNeighOutErrs", "1.3.6.1.2.1.8.5.1.7", "C"),
			new SnmpOidRecord("egpNeighEventTrigger", "1.3.6.1.2.1.8.5.1.15", "I"), new SnmpOidRecord("egpNeighOutMsgs", "1.3.6.1.2.1.8.5.1.6", "C"),
			new SnmpOidRecord("egpNeighMode", "1.3.6.1.2.1.8.5.1.14", "I"), new SnmpOidRecord("egpNeighInErrs", "1.3.6.1.2.1.8.5.1.5", "C"),
			new SnmpOidRecord("egpNeighIntervalPoll", "1.3.6.1.2.1.8.5.1.13", "I"), new SnmpOidRecord("egpNeighInMsgs", "1.3.6.1.2.1.8.5.1.4", "C"),
			new SnmpOidRecord("egpNeighAs", "1.3.6.1.2.1.8.5.1.3", "I"), new SnmpOidRecord("egpNeighIntervalHello", "1.3.6.1.2.1.8.5.1.12", "I"),
			new SnmpOidRecord("egpNeighAddr", "1.3.6.1.2.1.8.5.1.2", "IP"), new SnmpOidRecord("egpNeighStateDowns", "1.3.6.1.2.1.8.5.1.11", "C"),
			new SnmpOidRecord("egpNeighStateUps", "1.3.6.1.2.1.8.5.1.10", "C"), new SnmpOidRecord("egpNeighState", "1.3.6.1.2.1.8.5.1.1", "I"),
			new SnmpOidRecord("egpOutErrors", "1.3.6.1.2.1.8.4", "C"), new SnmpOidRecord("egpOutMsgs", "1.3.6.1.2.1.8.3", "C"), new SnmpOidRecord("egpInErrors", "1.3.6.1.2.1.8.2", "C"),
			new SnmpOidRecord("egpInMsgs", "1.3.6.1.2.1.8.1", "C") };
}

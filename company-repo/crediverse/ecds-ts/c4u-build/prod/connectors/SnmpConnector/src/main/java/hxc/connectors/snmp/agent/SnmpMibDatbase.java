package hxc.connectors.snmp.agent;

import co.za.concurrent.mibs.CONCURRENT_SYSTEMS_C4U_MIBOidTable;
import hxc.connectors.snmp.components.SnmpDatabase;

public class SnmpMibDatbase extends SnmpDatabase
{

	public SnmpMibDatbase()
	{
		super();
		// Add the tables to database
		add(new CONCURRENT_SYSTEMS_C4U_MIBOidTable());
	}

}

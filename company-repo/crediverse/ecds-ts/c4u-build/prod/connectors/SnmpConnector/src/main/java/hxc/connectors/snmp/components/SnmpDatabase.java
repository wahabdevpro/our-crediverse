package hxc.connectors.snmp.components;

import java.util.HashMap;

import hxc.connectors.snmp.components.SnmpStatusException.State;

// Contains tables for SNMP OID Records
public class SnmpDatabase
{

	private HashMap<String, SnmpTable> tables;
	private SnmpOidRecord lastRecord;

	public SnmpDatabase()
	{
		tables = new HashMap<String, SnmpTable>();
	}

	public void add(SnmpTable table)
	{
		tables.put(table.getName(), table);
	}

	// Helper method to get the record using the unique identifier
	public SnmpOidRecord getRecordWithOID(String oid) throws SnmpStatusException
	{
		// Iterate through the tables
		for (String tableName : tables.keySet())
		{
			// Compare the records
			SnmpTable table = tables.get(tableName);
			if ((lastRecord = table.getRecordWithOID(oid)) != null)
			{
				return lastRecord;
			}
		}
		throw new SnmpStatusException(State.notFound);
	}

	// Helper method to get the record using the name of the record
	public SnmpOidRecord getRecordWithName(String name) throws SnmpStatusException
	{
		// Iterate through the tables
		for (String tableName : tables.keySet())
		{
			// Compare the records
			SnmpTable table = tables.get(tableName);
			if ((lastRecord = table.getRecordWithName(name)) != null)
			{
				return lastRecord;
			}
		}
		throw new SnmpStatusException(State.notFound);
	}
}

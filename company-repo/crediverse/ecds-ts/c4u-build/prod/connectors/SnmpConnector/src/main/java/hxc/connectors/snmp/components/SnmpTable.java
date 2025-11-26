package hxc.connectors.snmp.components;

import java.io.Serializable;
import java.util.HashMap;

public class SnmpTable implements Serializable
{
	private static final long serialVersionUID = -1536863213699642081L;
	private String name;
	private transient HashMap<String, SnmpOidRecord> records;

	public SnmpTable(String name)
	{
		this.name = name;
		records = new HashMap<String, SnmpOidRecord>();
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void loadMib(SnmpOidRecord list[])
	{
		for (SnmpOidRecord record : list)
		{
			records.put(record.getOID(), record);
		}
	}

	public SnmpOidRecord getRecordWithOID(String oid)
	{
		return records.get(oid);
	}

	public SnmpOidRecord getRecordWithName(String name)
	{
		for (String oid : records.keySet())
		{
			SnmpOidRecord record = records.get(oid);
			if (record.getName().equalsIgnoreCase(name))
			{
				return record;
			}
		}
		return null;
	}
}

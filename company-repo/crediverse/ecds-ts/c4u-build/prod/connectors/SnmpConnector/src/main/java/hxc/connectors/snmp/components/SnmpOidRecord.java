package hxc.connectors.snmp.components;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UnsignedInteger32;

// A single record for the SNMP tables
public class SnmpOidRecord
{

	private String name;
	private String oid;
	private String type;
	private Object value;

	public SnmpOidRecord(String name, String oid, String type, Object value)
	{
		this.name = name;
		this.oid = oid;
		this.type = type;
		this.value = value;
	}

	public SnmpOidRecord(String name, String oid, String type)
	{
		this.name = name;
		this.oid = oid;
		this.type = type;
		this.value = getType(type);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setOID(String oid)
	{
		this.oid = oid;
	}

	public String getOID()
	{
		return oid;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public Object getValue()
	{
		return this.value;
	}

	// Convert the object to the correct type
	private Object getType(String type)
	{
		Object value = null;
		switch (type)
		{
			case "C":
				value = new Counter32(0);
				break;
			case "C64":
				value = new Counter64(0);
				break;
			case "EN":
				value = new String("[Table Entry]");
				break;
			case "G":
				value = new Gauge32(0);
				break;
			case "I":
				value = Integer.valueOf(0);
				break;
			case "ID":
				value = new String("ID");
				break;
			case "IP":
				value = new IpAddress("127.0.0.1");
				break;
			case "NT":
			case "NU":
				value = null;
				break;
			case "O":
				value = new Opaque();
				break;
			case "OBG":
				value = null;
				break;
			case "OI":
				try
				{
					value = new Oid("1.1.1.1.1.1.1");
				}
				catch (GSSException e)
				{
					value = null;
				}
				break;
			case "S":
				value = new String("No Value Set");
				break;
			case "T":
				value = new TimeTicks(0);
				break;
			case "TA":
				value = new String("[Table]");
				break;
			case "U":
				value = new UnsignedInteger32(0);
				break;
		}
		return value;
	}
}

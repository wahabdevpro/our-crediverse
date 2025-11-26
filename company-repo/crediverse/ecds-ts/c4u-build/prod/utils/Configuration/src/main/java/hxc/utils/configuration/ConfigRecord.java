package hxc.utils.configuration;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Persistence Class
//
// /////////////////////////////////

@Table(name = "CF_Config")
public class ConfigRecord
{
	@Column(primaryKey = true)
	long SerialVersionUID;

	@Column(primaryKey = true, maxLength = 80)
	String name;

	int sequence;

	@Column(maxLength = 512, nullable = true)
	byte[] value;

	@Override
	public String toString()
	{
		return name;
	}

}

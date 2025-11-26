package hxc.utils.processmodel;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Persistence Class
//
// /////////////////////////////////
@Table(name = "CF_Process")
public class ProcessRecord
{
	@Column(primaryKey = true)
	long SerialVersionUID;

	@Column(primaryKey = true, maxLength = 80)
	String Name;

	@Column(maxLength = 65535, nullable = true)
	String Process;

}

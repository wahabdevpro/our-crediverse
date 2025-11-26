package hxc.connectors.file;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

////////////////////////////////////////////////////////////////////////////////////////
//
// Persistence Class
//
///////////////////////////////////

@Table(name = "FC_Progress")
public class ProgressRecord
{
	@Column(primaryKey = true, maxLength = 64)
	public String filename;

	public long recordNo;
}

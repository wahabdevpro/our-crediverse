package hxc.connectors.file;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

////////////////////////////////////////////////////////////////////////////////////////
//
// Persistence Class
//
///////////////////////////////////

@Table(name = "FC_File")
public class FileRecord
{
	@Column(primaryKey = true, maxLength = 64)
	public String filename;

	@Column(maxLength = 255, nullable = false)
	public String inputDirectory;

	@Column(nullable = false, defaultValue = "20140101T000000")
	public Date firstReceived;

	public boolean distributed;

	public boolean completed;
}

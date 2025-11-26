package hxc.connectors.file;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////
//
// Base Class for all File Processing DTOs
//
///////////////////////////////////
public abstract class FileDTO
{
	public String filename;
	public Date fileTime;
	public long recordNo; // 0 Based
}

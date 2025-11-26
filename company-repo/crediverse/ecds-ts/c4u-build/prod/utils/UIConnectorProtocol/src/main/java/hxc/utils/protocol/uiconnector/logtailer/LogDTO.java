package hxc.utils.protocol.uiconnector.logtailer;

import java.io.Serializable;

import hxc.connectors.file.CsvField;

@SuppressWarnings("serial")
public class LogDTO implements Serializable
{
	public LogDTO()
	{
	}

	@CsvField(column = 0, format = "yyyyMMdd'T'HHmmss")
	// public Date recordTime;
	public String recordTime;

	@CsvField(column = 1)
	public String severity;

	@CsvField(column = 2)
	public String transactionID;

	@CsvField(column = 3)
	public String component;

	@CsvField(column = 4)
	public String operation;

	@CsvField(column = 5)
	public int returnCode;

	@CsvField(column = 6)
	public String text;

	public String host;
}

package hxc.connectors.datawarehouse;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

public class Cdr extends FileDTO
{
	@CsvField(column = 0)
	public String hostName;

	@CsvField(column = 1)
	public String callerID;

	@CsvField(column = 2)
	public String a_MSISDN;

	@CsvField(column = 3)
	public String b_MSISDN;

	@CsvField(column = 4, format = "yyyyMMdd'T'HHmmss")
	public Date startTime;

	@CsvField(column = 5)
	public String inboundTransactionID;

	@CsvField(column = 6)
	public String inboundSessionID;

	@CsvField(column = 7)
	public String channel;

	@CsvField(column = 8)
	public String requestMode;

	@CsvField(column = 9)
	public String transactionID;

	@CsvField(column = 10)
	public String serviceID;

	@CsvField(column = 11)
	public String variantID;

	@CsvField(column = 12)
	public String processID;

	@CsvField(column = 13)
	public String lastActionID;

	@CsvField(column = 14)
	public int lastExternalResultCode;

	@CsvField(column = 15)
	public int chargeLevied;

	@CsvField(column = 16)
	public String returnCode;

	@CsvField(column = 17)
	public boolean rolledBack;

	@CsvField(column = 18)
	public boolean followUp;

	@CsvField(column = 19)
	public String param1;

	@CsvField(column = 20)
	public String param2;

	@CsvField(column = 21)
	public String additionalInformation;
}

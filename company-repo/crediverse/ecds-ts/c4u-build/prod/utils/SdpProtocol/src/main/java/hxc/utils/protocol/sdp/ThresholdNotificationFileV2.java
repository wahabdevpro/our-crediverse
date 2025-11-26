package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 0701235489,1,u,0,3,-12.0,020124,163254,23
//
// - Subscriber ID = 0701235489
// - Service Class ID = 1
// - Threshold Direction = u
// - Trigger = TRAFFIC
// - Threshold ID = 3
// - Threshold Limit = -12.0
// - Date = 020124 (24 Jan 2002)
// - Time = 163254 (16:32:54)
// - Account Group ID = 23
//
///////////////////////////////////

public class ThresholdNotificationFileV2 extends FileDTO
{

	@CsvField(column = 0)
	public String subscriberID;

	@CsvField(column = 1)
	public int serviceClassID;

	@CsvField(column = 2)
	public char thresholdDirection;

	@CsvField(column = 3)
	public String trigger;

	@CsvField(column = 4)
	public int thresholdID;

	@CsvField(column = 5)
	public double thresholdLimit;

	@CsvField(column = 6, format = "yyMMdd")
	public Date date;

	@CsvField(column = 7, format = "HHmmss")
	public Date time;

	@CsvField(column = 8)
	public Integer accountGroupID;

}

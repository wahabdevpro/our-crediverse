package hxc.utils.protocol.sdp;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 510088916,2,25,14,1
//
// - Subscriber ID = 5100888916
// - Usage threshold ID = 2
// - Usage counter ID = 25
// - Usage threshold value = 14
// - Value type = 1
//
///////////////////////////////////

public class UsageThresholdFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String subscriberID;

	@CsvField(column = 1)
	public String usageThresholdID;

	@CsvField(column = 2)
	public String usageCounterID;

	@CsvField(column = 3)
	public double usageThresholdValue;

	@CsvField(column = 4)
	public byte valueType;

}

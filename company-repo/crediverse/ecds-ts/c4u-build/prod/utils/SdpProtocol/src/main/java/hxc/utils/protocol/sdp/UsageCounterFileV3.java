package hxc.utils.protocol.sdp;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 510088916,1,1,164.5,2
//
// - Subscriber ID = 5100888916
// - Usage counter ID = 1
// - Usage counter type = 1
// - Usage counter value = 164.5
// - Value type = 2
//
///////////////////////////////////

public class UsageCounterFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String subscriberID;

	@CsvField(column = 1)
	public String usageCounterID;

	@CsvField(column = 2)
	public byte usageCounterType;

	@CsvField(column = 3)
	public double usageCounterValue;

	@CsvField(column = 4)
	public byte valueType;

}

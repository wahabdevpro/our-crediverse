package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 5369527977,1,400,2008-01-01,560
//
// - Account ID = 5369527977
// - Accumulator ID = 1
// - Accumulator Counter = 400
// - Clearing Date = 2008-01-01
// - Service Class ID = 560
//
///////////////////////////////////

public class UsageAccumulatorsFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String accountID;

	@CsvField(column = 1)
	public int accumulatorID;

	@CsvField(column = 2)
	public int accumulatorCounter;

	@CsvField(column = 3, format = "yyyy-MM-dd")
	public Date clearingDate;

	@CsvField(column = 4)
	public String serviceClassID;

}

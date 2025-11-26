package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
//
// Version: 2.0
// 1,0701234568,14,20090127,105632,3,5,2,u,4,N,20.000000,0,23
// 1,0701234568,14,20090127,105632,3,5,2,u,5,Y,30.000000,0,23
// ==========
// 2,0721234555,14,20090127,105706,3,5,2,d,1,N,50.000000,0,23,32,20090101,20090531,27
// 2,0721234555,14,20090127,105706,3,5,2,d,2,N,40.000000,0,23,32,20090101,20090531,27
// 2,0721234555,14,20090127,105706,3,5,2,d,3,Y,30.000000,0,23,32,20090101,20090531,27
// ==========
//
// record_type
// sub_id
// sc
// date
// time
// sub_language
// reason
// operation
// threshold_direction
// threshold_id
// threshold_passing_last
// threshold_limit
// tele_service_code
// account_group_id
//
// da_id
// da_start_date
// da_expiry_date
// offer_id
//
///////////////////////////////////

public class ThresholdNotificationFileV3 extends FileDTO
{
	// Both MA and DA

	@CsvField(column = 0)
	public int recordType; // record_type

	@CsvField(column = 1)
	public String subscriberID; // sub_id

	@CsvField(column = 2)
	public int serviceClassID; // sc

	@CsvField(column = 3, format = "yyyyMMdd")
	public Date date;

	@CsvField(column = 4, format = "HHmmss")
	public Date time;

	@CsvField(column = 5)
	public int languageID; // sub_language

	@CsvField(column = 6)
	public int reason;

	@CsvField(column = 7)
	public int operation;

	@CsvField(column = 8)
	public char thresholdDirection; // threshold_direction

	@CsvField(column = 9)
	public int thresholdID; // threshold_id

	@CsvField(column = 10)
	public char passingLast; // threshold_passing_last

	@CsvField(column = 11)
	public double thresholdLimit; // threshold_limit

	@CsvField(column = 12)
	public int teleServiceCode; // tele_service_code

	@CsvField(column = 13)
	public int accountGroupID; // account_group_id

	// Dedicated Accounts Only
	@CsvField(column = 14, optional = true)
	public Integer daID; // da_id

	@CsvField(column = 15, optional = true)
	public Date daStateDate; // da_start_date

	@CsvField(column = 16, optional = true)
	public Date daExpiryDate; // da_expiry_date

	@CsvField(column = 17, optional = true)
	public Integer offerID; // offer_id

}

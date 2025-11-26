package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 5100788004,1,2009-01-26,2009-02-24,,,0,1,1,1
//
// - Account ID = 5100788004
// - OfferID=1
// - Start Date = 2009-01-26
// - Expiry Date = 2009-02-24
// - Start Time = Empty string
// - Expiry Time = Empty string
// - Offer Type = 0
// - PAM Service ID = 1
// - Product ID = 1
// - Fee Applied = 1
//
///////////////////////////////////

public class OfferFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String accountID;

	@CsvField(column = 1, optional = true)
	public String offerID;

	@CsvField(column = 2, format = "yyyy-MM-dd", optional = true)
	public Date startDate;

	@CsvField(column = 3, format = "yyyy-MM-dd", optional = true)
	public Date expiryDate;

	@CsvField(column = 4, format = "HH:mm:ssZ", optional = true)
	public Date startTime;

	@CsvField(column = 5, format = "HH:mm:ssZ", optional = true)
	public Date expiryTime;

	@CsvField(column = 6)
	public byte offerType;

}

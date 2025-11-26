package hxc.utils.protocol.sdp;

import hxc.connectors.file.CsvField;

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

public class OfferFileV3_3 extends OfferFileV3
{

	@CsvField(column = 7, optional = true)
	public String pamServiceID;

	@CsvField(column = 8, optional = true)
	public String productID;

	@CsvField(column = 9, optional = true)
	public Boolean feeApplied;

}

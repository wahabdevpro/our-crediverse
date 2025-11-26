package hxc.utils.protocol.sdp;

import hxc.connectors.file.CsvField;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 5100788004,1,,2009-02-05,0,,2009-01-05,0,2,,457890,1,1 (PC:09847)
//
// - Account ID = 5100788004
// - Dedicated Account ID = 1
// - Dedicated Account Balance = Empty string
// - Expiry Date = 2009-02-05
// - Account in Euro flag = 0
// - Offer ID = Empty string
// - Start Date = 2009-01-05
// - Dedicated Account Unit Type = 0
// - Dedicated Account Category = 2
// - Money Unit Sub-Type = Empty string
// - Dedicated Account Unit Balance = 457890
// - PAM Service Id = 1
// - Product ID = 1 (PC:09847)
//
///////////////////////////////////

public class DedicatedAccountsFileV3_3 extends DedicatedAccountsFileV3
{

	@CsvField(column = 11, optional = true)
	public Integer pamServiceId;

	@CsvField(column = 12, optional = true)
	public Integer productID;

}

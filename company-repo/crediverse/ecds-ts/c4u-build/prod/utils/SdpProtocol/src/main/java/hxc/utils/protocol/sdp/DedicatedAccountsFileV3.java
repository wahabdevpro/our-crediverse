package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 5100788004,1,,2009-02-05,0,,2009-01-05,0,2,,457890
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
//
///////////////////////////////////

public class DedicatedAccountsFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String accountID;

	@CsvField(column = 1)
	public int dedicatedAccountID;

	@CsvField(column = 2, optional = true)
	public Double dedicatedAccountBalance;

	@CsvField(column = 3, format = "yyyy-MM-dd", optional = true)
	public Date expiryDate;

	@CsvField(column = 4)
	public boolean accountinEuroflag;

	@CsvField(column = 5, optional = true)
	public Integer offerID;

	@CsvField(column = 6, format = "yyyy-MM-dd", optional = true)
	public Date startDate;

	@CsvField(column = 7)
	public int dedicatedAccountUnitType;

	@CsvField(column = 8)
	public int dedicatedAccountCategory;

	@CsvField(column = 9)
	public boolean moneyUnitSubtype;

	@CsvField(column = 10)
	public double dedicatedAccountUnitBalance;

}

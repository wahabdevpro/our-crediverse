package hxc.utils.protocol.sdp;

import hxc.connectors.file.CsvField;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 56245000,56245000,0,0,,0,1,1,1,0,0,0,1,1,1,1,1,524,524,,
// -439.000000,1,2009- 07-30,2009-07-30,,2009-08-29,0,0,2009-08-09,
// 0,0,2008-10-19,0,0,0,0,0,1022 ,0,0,0,0,0,2008-01-02,010812131415,25
//
// - Subscriber ID = 56245000
// - Account ID = 56245000
// - Temporary Block Flag = 0
// - Refill Failed Counter = 0
// - Refill Bar End Date and Time = Empty string
// - First IVR Call Done Flag = 0
// - First Call Done Flag = 1
// - Language = 1
// - Special Announcement Played Flag = 1
// - Service Fee Period Warning Played Flag = 0
// - Supervision Period Warning Played Flag = 0
// - Low Level Warning Played Flag = 0
// - Originating Voice Block Status = 1
// - Terminating Voice Block Status = 1
// - Originating SMS Block Status = 1
// - Terminating SMS Block Status = 1
// - GPRS Block Status = 1
// - Service Class ID = 524
// - Original Service Class ID = 524
// - Temporary Service Class Expiry Date = Empty string
// - Account Balance = -439.000000
// - Account Activated Flag = 1
// - Service Fee Expiry Date = 2009-07-30
// - Supervision Period Expiry Date = 2009-07-30
// - Last Service Fee Deduction Date = Empty string
// - Account Disconnection Date = 2009-08-29
// - Service Fee Expiry Flag = 0
// - Service Fee Expiry Warning Flag = 0
// - Credit Clearance Date = 2009-08-09
// - Supervision Expiry Flag = 0
// - Supervision Expiry Warning Flag = 0
// - Negative Balance Barring Start Date = 2008-10-19
// - Negative Balance Barred Flag = 0
// - Account In Euro Flag = 0
// - Active Service Disabled Flag = 0
// - Passive Service Disabled Flag = 0
// - Converged Flag = 0
// - Life Cycle Notification Report = 1022
// - Service Offerings = 0
// - Account Group ID = 0
// - Community ID 1 = 0
// - Community ID 2 = 0
// - Community ID 3 = 0
// - Account Activated Date = 2008-01-02
// - Global ID = 010812131415
// - Account Prepaid Empty Limit = 25
//
///////////////////////////////////

public class SubscriberFileV3_3 extends SubscriberFileV3
{

	@CsvField(column = 44)
	public String globalID;

	@CsvField(column = 45)
	public double accountPrepaidEmptyLimit;

}

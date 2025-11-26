package hxc.utils.protocol.sdp;

import java.util.Date;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;

////////////////////////////////////////////////////////////////////////////////////////
//
// Example:
// 56245000,56245000,0,0,,0,1,1,1,0,0,0,1,1,1,1,1,524,524,,
// -439.000000,1,2009- 07-30,2009-07-30,,2009-08-29,0,0,2009-08-09,
// 0,0,2008-10-19,0,0,0,0,0,1022 ,0,0,0,0,0,2008-01-02
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
//
///////////////////////////////////

public class SubscriberFileV3 extends FileDTO
{

	@CsvField(column = 0)
	public String subscriberID;

	@CsvField(column = 1)
	public String accountID;

	@CsvField(column = 2)
	public boolean temporaryBlockFlag;

	@CsvField(column = 3)
	public short refillFailedCounter;

	@CsvField(column = 4, format = "yyyy-MM-dd'T'HH:mm:ssZ", optional = true)
	public Date refillBarEndDateAndTime;

	@CsvField(column = 5)
	public boolean firstIVRCallDoneFlag;

	@CsvField(column = 6)
	public boolean firstCallDoneFlag;

	@CsvField(column = 7)
	public byte language;

	@CsvField(column = 8)
	public boolean specialAnnouncementPlayedFlag;

	@CsvField(column = 9)
	public boolean serviceFeePeriodWarningPlayedFlag;

	@CsvField(column = 10)
	public boolean supervisionPeriodWarningPlayedFlag;

	@CsvField(column = 11)
	public boolean lowLevelWarningPlayedFlag;

	@CsvField(column = 12)
	public boolean originatingVoiceBlockStatus;

	@CsvField(column = 13)
	public boolean terminatingVoiceBlockStatus;

	@CsvField(column = 14)
	public boolean originatingSMSBlockStatus;

	@CsvField(column = 15)
	public boolean terminatingSMSBlockStatus;

	@CsvField(column = 16)
	public boolean gprsBlockStatus;

	@CsvField(column = 17)
	public int serviceClassID;

	@CsvField(column = 18)
	public int originalServiceClassID;

	@CsvField(column = 19, format = "yyyy-MM-dd", optional = true)
	public Date temporaryServiceClassExpiryDate;

	@CsvField(column = 20)
	public double accountBalance;

	@CsvField(column = 21)
	public boolean accountActivatedFlag;

	@CsvField(column = 22, format = "yyyy-MM-dd", optional = true)
	public Date serviceFeeExpiryDate;

	@CsvField(column = 23, format = "yyyy-MM-dd", optional = true)
	public Date supervisionPeriodExpiryDate;

	@CsvField(column = 24, format = "yyyy-MM-dd", optional = true)
	public Date lastServiceFeeDeductionDate;

	@CsvField(column = 25, format = "yyyy-MM-dd", optional = true)
	public Date accountDisconnectionDate;

	@CsvField(column = 26)
	public boolean serviceFeeExpiryFlag;

	@CsvField(column = 27)
	public boolean serviceFeeExpiryWarningFlag;

	@CsvField(column = 28, format = "yyyy-MM-dd", optional = true)
	public Date creditClearanceDate;

	@CsvField(column = 29)
	public boolean supervisionExpiryFlag;

	@CsvField(column = 30)
	public boolean supervisionExpiryWarningFlag;

	@CsvField(column = 31, format = "yyyy-MM-dd", optional = true)
	public Date negativeBalanceBarringStartDate;

	@CsvField(column = 32)
	public boolean negativeBalanceBarredFlag;

	@CsvField(column = 33)
	public boolean accountInEuroFlag;

	@CsvField(column = 34)
	public boolean activeServiceDisabledFlag;

	@CsvField(column = 35)
	public boolean passiveServiceDisabledFlag;

	@CsvField(column = 36)
	public boolean convergedFlag;

	@CsvField(column = 37)
	public String lifeCycleNotificationReport;

	@CsvField(column = 38)
	public int serviceOfferings;

	@CsvField(column = 39)
	public int accountGroupID;

	@CsvField(column = 40)
	public String communityID1;

	@CsvField(column = 41)
	public String communityID2;

	@CsvField(column = 42)
	public String communityID3;

	@CsvField(column = 43, format = "yyyy-MM-dd", optional = true)
	public Date accountActivatedDate;

}

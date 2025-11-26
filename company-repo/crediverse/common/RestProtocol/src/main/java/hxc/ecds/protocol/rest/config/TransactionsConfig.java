package hxc.ecds.protocol.rest.config;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class TransactionsConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 2453858303713402228L;

	public static final String ERR_REFILL_FAILED = "REFILL_FAILED"; // Technical
	public static final String ERR_TECHNICAL_PROBLEM = "TECHNICAL_PROBLEM";
	public static final String ERR_INVALID_CHANNEL = "INVALID_CHANNEL";
	public static final String ERR_FORBIDDEN = "FORBIDDEN";
	public static final String ERR_NO_TRANSFER_RULE = "NO_TRANSFER_RULE";
	public static final String ERR_INTRATIER_TRANSFER = "INTRATIER_TRANSFER";
	public static final String ERR_NO_LOCATION = "NO_LOCATION";
	public static final String ERR_WRONG_LOCATION = "WRONG_LOCATION";
	public static final String ERR_CO_AUTHORIZE = "CO_AUTHORIZE";
	public static final String ERR_INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
	public static final String ERR_INSUFFICIENT_PROVISION = "INSUFFICIENT_PROVISN";
	public static final String ERR_DAY_COUNT_LIMIT = "DAY_COUNT_LIMIT";
	public static final String ERR_DAY_AMOUNT_LIMIT = "DAY_AMOUNT_LIMIT";
	public static final String ERR_MONTH_COUNT_LIMIT = "MONTH_COUNT_LIMIT";
	public static final String ERR_MONTH_AMOUNT_LIMIT = "MONTH_AMOUNT_LIMIT";
	public static final String ERR_MAX_AMOUNT_LIMIT = "MAX_AMOUNT_LIMIT";
	public static final String ERR_ALREADY_REGISTERED = "ALREADY_REGISTERED";
	public static final String ERR_TRANSACTION_ALREADY_ADJUDICATED = "ALREADY_ADJUDICATED ";
	public static final String ERR_NOT_REGISTERED = "NOT_REGISTERED";
	public static final String ERR_INVALID_STATE = "INVALID_STATE";
	public static final String ERR_INVALID_PIN = "INVALID_PIN";
	public static final String ERR_CONFIRM_PIN_DIFF = "CONFIRM_PIN_DIFF";
	public static final String ERR_INVALID_PASSWORD = "INVALID_PASSWORD";
	public static final String ERR_HISTORIC_PASSWORD = "HISTORIC_PASSWORD";
	public static final String ERR_NOT_ELIGIBLE = "NOT_ELIGIBLE";
	public static final String ERR_PIN_LOCKOUT = "PIN_LOCKOUT";
	public static final String ERR_PASSWORD_LOCKOUT = "PASSWORD_LOCKOUT";
	public static final String ERR_NOT_SELF = "NOT_SELF";
	public static final String ERR_TRANSACTION_NOT_FOUND = "TX_NOT_FOUND";
	public static final String ERR_IMSI_LOCKOUT = "IMSI_LOCKOUT";
	public static final String ERR_INVALID_AGENT = "INVALID_AGENT";
	public static final String ERR_INVALID_AMOUNT = "INVALID_AMOUNT";
	public static final String ERR_INVALID_TRANSACTION_TYPE = "INVALID_TRAN_TYPE";
	public static final String ERR_INVALID_BUNDLE = "INVALID_BUNDLE";
	public static final String ERR_TRANSACTION_ALREADY_REVERSED = "ALREADY_REVERSED";
	public static final String ERR_NOT_WEBUSER_SESSION = "NOT_WEBUSER_SESSION";
	public static final String ERR_CO_SIGN_ONLY_SESSION = "CO_SIGN_ONLY_SESSION";
	public static final String ERR_SESSION_EXPIRED = "SESSION_EXPIRED";
	public static final String ERR_TIMED_OUT = "TIMED_OUT";
	public static final String ERR_INVALID_RECIPIENT = "INVALID_RECIPIENT";
	public static final String ERR_ACC_NOT_FOUND = "ACC_NOT_FOUND";
	public static final String ERR_MSISDNS_MISMATCH = "MSISDN_RE_MISMATCH";
	public static final String ERR_WRONG_B_NUMBER_FORMAT = "ERR_WRONG_B_NUMBER_FORMAT";

	public static final String ERR_OTHER_ERROR = "OTHER_ERROR";
	public static final String ERR_REFILL_BARRED = "REFILL_BARRED";
	public static final String ERR_TEMPORARY_BLOCKED = "TEMPORARY_BLOCKED";
	public static final String ERR_REFILL_NOT_ACCEPTED = "REFILL_NOT_ACCEPTED";
	public static final String ERR_REFILL_DENIED = "REFILL_DENIED";
	public static final String ERR_NO_IMSI = "NO_IMSI";
	public static final String ERR_BUNDLE_SALE_FAILED = "BUNDLE_SALE_FAILED";

	// From Violations
	public static final String ERR_INVALID_VALUE = Violation.INVALID_VALUE;
	public static final String ERR_TOO_SMALL = Violation.TOO_SMALL;
	public static final String ERR_TOO_LARGE = Violation.TOO_LARGE;
	public static final String ERR_TOO_LONG = Violation.TOO_LONG;
	public static final String ERR_TOO_SHORT = Violation.TOO_SHORT;

	public static final String TRANSACTION_NO = "{TransactionNo}";
	public static final String SENDER_MSISDN = "{SenderMSISDN}";
	public static final String RECIPIENT_MSISDN = "{RecipientMSISDN}";
	public static final String AMOUNT = "{Amount}";
	public static final String FR_TRANSACTION_NO = "{NoTransaction}";
	public static final Phrase PHRASE_TRANSACTION_NO = Phrase.en(TRANSACTION_NO).fre(FR_TRANSACTION_NO);

	public static final String COMPANY_ID = "{CompanyID}";
	public static final String TIME_STAMP = "{TimeStamp}";
	public static final String SERVER_NAME = "{ServerName}";
	public static final String FILENAME = "{Filename}";
	public static final String TDR_STRUCTURE_VERSION_1 = "1.0.0";
	public static final String TDR_STRUCTURE_VERSION_2 = "1.13.0";



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private Map<String, Phrase> errorMessages = new HashMap<String, Phrase>();
	private int imsiChangeLockoutHours = 24;
	private int imeiRefreshInterval = 24 * 60; // 1440 minutes = 1 day
	private Integer channelRequestTimeoutSeconds = 2000; // Seconds, null means no time-out COOP REMOVE ME

	// TDR Related
	private int tdrRotationIntervalSeconds = 60 * 60;
	private int tdrMaxFileLengthBytes = 10 * 1024 * 1024;
	private String tdrDirectory = "/var/opt/cs/ecds/tdr/" + COMPANY_ID;
	private String tdrFilenameFormat = COMPANY_ID + "_ecds_tdr_" + TIME_STAMP + ".csv"; 
	private String tdrCopyCommand = "";
	private String zipFilenameFormat = COMPANY_ID + "_ecds_tdr_" + TIME_STAMP + ".zip";
	private String tdrStructureVersion = TDR_STRUCTURE_VERSION_1;
	private String zipCopyCommand = "";
	private int zipTdrAfterDays = 30;
	private int deleteZipAfterDays = 90;
	private int oltpTransactionRetentionDays = 365;
	private LocalTime oltpTransactionCleanupTimeOfDay = LocalTime.of(2,0,0,0);
	private int olapTransactionRetentionDays = 365;
	private int olapTransactionCleanupTimeOfDay = 7200;
	private int olapSyncTimeOfDay = 10800;
	private int maxUssdLength = 160;

	private static Phrase[] tdrDirectoryFields = new Phrase[] { Phrase.en(COMPANY_ID) };
	private static Phrase[] tdrFilenameFields = new Phrase[] { Phrase.en(COMPANY_ID), Phrase.en(TIME_STAMP), Phrase.en(SERVER_NAME) };
	private static Phrase[] zipFilenameFields = new Phrase[] { Phrase.en(COMPANY_ID), Phrase.en(TIME_STAMP), Phrase.en(SERVER_NAME) };
	private static Phrase[] tdrCopyCommandFields = new Phrase[] { Phrase.en(COMPANY_ID), Phrase.en(FILENAME) };
	private static Phrase[] zipCopyCommandFields = new Phrase[] { Phrase.en(COMPANY_ID), Phrase.en(FILENAME) };

	protected int version;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Map<String, Phrase> getErrorMessages()
	{
		return errorMessages;
	}

	public TransactionsConfig setErrorMessages(Map<String, Phrase> errorMessages)
	{
		this.errorMessages = errorMessages;
		return this;
	}

	public int getImsiChangeLockoutHours()
	{
		return imsiChangeLockoutHours;
	}

	public TransactionsConfig setImsiChangeLockoutHours(int imsiChangeLockoutHours)
	{
		this.imsiChangeLockoutHours = imsiChangeLockoutHours;
		return this;
	}

	public int getImeiRefreshInterval()
	{
		return imeiRefreshInterval;
	}

	public TransactionsConfig setImeiRefreshInterval(int imeiRefreshInterval)
	{
		this.imeiRefreshInterval = imeiRefreshInterval;
		return this;
	}

	public Integer getChannelRequestTimeoutSeconds()
	{
		return channelRequestTimeoutSeconds;
	}

	public TransactionsConfig setChannelRequestTimeoutSeconds(Integer channelRequestTimeoutSeconds)
	{
		this.channelRequestTimeoutSeconds = channelRequestTimeoutSeconds;
		return this;
	}

	public int getTdrRotationIntervalSeconds()
	{
		return tdrRotationIntervalSeconds;
	}

	public TransactionsConfig setTdrRotationIntervalSeconds(int tdrRotationIntervalSeconds)
	{
		this.tdrRotationIntervalSeconds = tdrRotationIntervalSeconds;
		return this;
	}

	public int getTdrMaxFileLengthBytes()
	{
		return tdrMaxFileLengthBytes;
	}

	public TransactionsConfig setTdrMaxFileLengthBytes(int tdrMaxFileLengthBytes)
	{
		this.tdrMaxFileLengthBytes = tdrMaxFileLengthBytes;
		return this;
	}

	public Phrase[] listTdrDirectoryFields()
	{
		return tdrDirectoryFields;
	}

	public String getTdrDirectory()
	{
		return tdrDirectory;
	}

	public TransactionsConfig setTdrDirectory(String tdrDirectory)
	{
		this.tdrDirectory = tdrDirectory;
		return this;
	}

	public Phrase[] listTdrFilenameFormatFields()
	{
		return tdrFilenameFields;
	}

	public String getTdrFilenameFormat()
	{
		return tdrFilenameFormat;
	}

	public TransactionsConfig setTdrFilenameFormat(String tdrFilenameFormat)
	{
		this.tdrFilenameFormat = tdrFilenameFormat;
		return this;
	}

	public Phrase[] listTdrCopyCommandFields()
	{
		return tdrCopyCommandFields;
	}

	public String getTdrCopyCommand()
	{
		return tdrCopyCommand;
	}

	public TransactionsConfig setTdrCopyCommand(String tdrCopyCommand)
	{
		this.tdrCopyCommand = tdrCopyCommand;
		return this;
	}

	public Phrase[] listZipFilenameFormatFields()
	{
		return zipFilenameFields;
	}

	public String getZipFilenameFormat()
	{
		return zipFilenameFormat;
	}

	public TransactionsConfig setZipFilenameFormat(String zipFilenameFormat)
	{
		this.zipFilenameFormat = zipFilenameFormat;
		return this;
	}

	public String getTdrStructureVersion() {
		return tdrStructureVersion;
	}

	public TransactionsConfig setTdrStructureVersion(String tdrStructureVersion) {
		this.tdrStructureVersion = tdrStructureVersion;
		return this;
	}

	public Phrase[] listZipCopyCommandFields()
	{
		return zipCopyCommandFields;
	}

	public String getZipCopyCommand()
	{
		return zipCopyCommand;
	}

	public TransactionsConfig setZipCopyCommand(String zipCopyCommand)
	{
		this.zipCopyCommand = zipCopyCommand;
		return this;
	}

	public int getZipTdrAfterDays()
	{
		return zipTdrAfterDays;
	}

	public TransactionsConfig setZipTdrAfterDays(int zipTdrAfterDays)
	{
		this.zipTdrAfterDays = zipTdrAfterDays;
		return this;
	}

	public int getDeleteZipAfterDays()
	{
		return deleteZipAfterDays;
	}

	public TransactionsConfig setDeleteZipAfterDays(int deleteZipAfterDays)
	{
		this.deleteZipAfterDays = deleteZipAfterDays;
		return this;
	}

	public int getOltpTransactionRetentionDays()
	{
		return oltpTransactionRetentionDays;
	}

	public TransactionsConfig setOltpTransactionRetentionDays(int oltpTransactionRetentionDays)
	{
		this.oltpTransactionRetentionDays = oltpTransactionRetentionDays;
		return this;
	}

	public LocalTime getOltpTransactionCleanupTimeOfDay()
	{
		return oltpTransactionCleanupTimeOfDay;
	}

	public TransactionsConfig setOltpTransactionCleanupTimeOfDay(LocalTime oltpTransactionCleanupTimeOfDay)
	{
		this.oltpTransactionCleanupTimeOfDay = oltpTransactionCleanupTimeOfDay;
		return this;
	}

	public int getOlapTransactionRetentionDays()
	{
		return olapTransactionRetentionDays;
	}

	public TransactionsConfig setOlapTransactionRetentionDays(int olapTransactionRetentionDays)
	{
		this.olapTransactionRetentionDays = olapTransactionRetentionDays;
		return this;
	}

	public int getOlapTransactionCleanupTimeOfDay()
	{
		return olapTransactionCleanupTimeOfDay;
	}

	public TransactionsConfig setOlapTransactionCleanupTimeOfDay(int olapTransactionCleanupTimeOfDay)
	{
		this.olapTransactionCleanupTimeOfDay = olapTransactionCleanupTimeOfDay;
		return this;
	}

	public int getOlapSyncTimeOfDay()
	{
		return olapSyncTimeOfDay;
	}

	public TransactionsConfig setOlapSyncTimeOfDay(int olapSyncTimeOfDay)
	{
		this.olapSyncTimeOfDay = olapSyncTimeOfDay;
		return this;
	}
	
	public int getMaxUssdLength()
	{
		return maxUssdLength;
	}
	
	public TransactionsConfig setMaxUssdLength(int maxUssdLength)
	{
		this.maxUssdLength = maxUssdLength;
		return this;
	}

	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public TransactionsConfig setVersion(int version)
	{
		this.version = version;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransactionsConfig()
	{
		initialize();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public String findErrorText(String languageID, String returnCode)
	{
		Phrase phrase = lookup(returnCode);
		if (phrase == null)
			phrase = lookup(ERR_TECHNICAL_PROBLEM);
		if (phrase == null)
			return returnCode;

		return phrase.safe(languageID, returnCode);
	}

	private Phrase lookup(String returnCode)
	{
		return errorMessages.get(returnCode);
	}

	private synchronized void initialize()
	{
		if (errorMessages == null)
			errorMessages = new HashMap<String, Phrase>();

		en(ERR_TECHNICAL_PROBLEM, "A technical error has occurred. Please try again later."); // Don't remove
		en(ResponseHeader.RETURN_CODE_SUCCESS, "Success."); // Don't remove
		en(ERR_INVALID_CHANNEL, "You are not allowed to transact using this channel.");
		en(ERR_FORBIDDEN, "You are not allowed perform this action.");
		en(ERR_NO_TRANSFER_RULE, "You cannot transfer to this Agent.");
		en(ERR_INTRATIER_TRANSFER, "Intra-tier transfers are not allowed on this tier.");
		en(ERR_NO_LOCATION, "Your location cannot be verified.");
		en(ERR_WRONG_LOCATION, "You are not allowed to trade at this location.");
		en(ERR_CO_AUTHORIZE, "Invalid co-signatory.");
		en(ERR_INSUFFICIENT_FUNDS, "There are insufficient funds to perform this transaction.");
		en(ERR_INSUFFICIENT_PROVISION, "There is insufficient bonus provision to perform this transaction.");
		en(ERR_DAY_COUNT_LIMIT, "You have reached the limit of transactions you can perform in one day.");
		en(ERR_DAY_AMOUNT_LIMIT, "You have reached you maximum daily limit for total transaction amounts.");
		en(ERR_MONTH_COUNT_LIMIT, "You have reached the limit of transactions you can perform in one month.");
		en(ERR_MONTH_AMOUNT_LIMIT, "You have reached you maximum monthly limit for total transaction amounts.");
		en(ERR_MAX_AMOUNT_LIMIT, "You have exceeded the maximum allowed Amount.");
		en(ERR_ALREADY_REGISTERED, "Your PIN has already been registered.");
		en(ERR_NOT_REGISTERED, "You must register your PIN first.");
		en(ERR_INVALID_STATE, "Your account is not active.");
		en(ERR_INVALID_PIN, "Invalid PIN.");
		en(ERR_CONFIRM_PIN_DIFF, "The repeated PIN does not match.");
		en(ERR_INVALID_PASSWORD, "Invalid Password.");
		//Errorcode only, translated in UI server and configurable in TS.
		//en(ERR_HISTORIC_PASSWORD, "You have set this password before, please choose a unique password.");
		en(ERR_NOT_ELIGIBLE, "Subscriber not Eligible");
		en(ERR_PIN_LOCKOUT, "Locked out. Too many PIN attempts.");
		en(ERR_PASSWORD_LOCKOUT, "Locked out. Too many Password attempts.");
		en(ERR_NOT_SELF, "You cannot transact with yourself.");
		en(ERR_TRANSACTION_NOT_FOUND, "Transaction not found.");
		en(ERR_IMSI_LOCKOUT, "Agent locked pending an IMSI change.");
		en(ERR_INVALID_AGENT, "Invalid Agent.");
		en(ERR_INVALID_AMOUNT, "Invalid Amount.");
		en(ERR_INVALID_TRANSACTION_TYPE, "Invalid Transaction Type.");
		en(ERR_INVALID_BUNDLE, "Invalid Bundle.");
		en(ERR_TRANSACTION_ALREADY_REVERSED, "Transaction already Reversed.");
		en(ERR_TRANSACTION_ALREADY_ADJUDICATED, "Transaction already Adjudicated.");
		en(ERR_SESSION_EXPIRED, "Your session has expired.");
		en(ERR_TIMED_OUT, "Your request timed out.");
		en(ERR_INVALID_RECIPIENT, "Invalid Recipient.");
		en(ERR_ACC_NOT_FOUND, "Account(s) not found.");
		en(ERR_INVALID_VALUE, "Invalid Value.");
		en(ERR_TOO_SMALL, "Amount too Small.");
		en(ERR_TOO_LARGE, "Amount too Large.");
		en(ERR_TOO_LONG, "Too Long.");
		en(ERR_TOO_SHORT, "Too Short.");

		en(ERR_OTHER_ERROR, "Other Error, please contact your call centre.");
		en(ERR_REFILL_BARRED, "Account barred from refill, please contact your call centre.");
		en(ERR_TEMPORARY_BLOCKED, "Temporary blocked, please contact your call centre.");
		en(ERR_REFILL_NOT_ACCEPTED, "Refill not accepted, please contact your call centre.");
		en(ERR_REFILL_DENIED, "Refill denied, please contact your call centre.");
		en(ERR_BUNDLE_SALE_FAILED, "Bundle sale failed.");
		en(ERR_NO_IMSI, "Technical error - unable to subscribe to bundle.");

	}

	private void en(String returnCode, String text)
	{
		add(Phrase.ENG, returnCode, text);
	}

	private void add(String languageID, String returnCode, String text)
	{
		int length = returnCode.length();
		if (length > Transaction.RETURN_CODE_MAX_LENGTH)
			return;
		Phrase phrase = errorMessages.get(returnCode);
		if (phrase == null)
			errorMessages.put(returnCode, new Phrase().set(languageID, text));
		else if (!phrase.has(languageID))
			phrase.set(languageID, text);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		initialize();

		TransactionsConfig template = new TransactionsConfig();

		if (channelRequestTimeoutSeconds == null)
			channelRequestTimeoutSeconds = template.channelRequestTimeoutSeconds;

		if (tdrDirectory == null || tdrDirectory.isEmpty())
			tdrDirectory = template.tdrDirectory;

		if (tdrFilenameFormat == null || tdrFilenameFormat.isEmpty())
			tdrFilenameFormat = template.tdrFilenameFormat;

		if (tdrCopyCommand == null || tdrCopyCommand.isEmpty())
			tdrCopyCommand = template.tdrCopyCommand;

		if (zipFilenameFormat == null || zipFilenameFormat.isEmpty())
			zipFilenameFormat = template.zipFilenameFormat;

		if (tdrStructureVersion == null || tdrStructureVersion.isEmpty())
			tdrStructureVersion = template.tdrStructureVersion;

		if (zipCopyCommand == null || zipCopyCommand.isEmpty())
			zipCopyCommand = template.zipCopyCommand;

		if (zipTdrAfterDays == 0)
			zipTdrAfterDays = template.zipTdrAfterDays;

		if (deleteZipAfterDays == 0)
			deleteZipAfterDays = template.deleteZipAfterDays;

		if (oltpTransactionRetentionDays == 0)
			oltpTransactionRetentionDays = template.oltpTransactionRetentionDays;

		if (oltpTransactionCleanupTimeOfDay == null)
			oltpTransactionCleanupTimeOfDay = template.oltpTransactionCleanupTimeOfDay;

		if (olapTransactionRetentionDays == 0)
			olapTransactionRetentionDays = template.olapTransactionRetentionDays;

		if (olapTransactionCleanupTimeOfDay == 0)
			olapTransactionCleanupTimeOfDay = template.olapTransactionCleanupTimeOfDay;

		if (tdrRotationIntervalSeconds == 0)
			tdrRotationIntervalSeconds = template.tdrRotationIntervalSeconds;

		if (tdrMaxFileLengthBytes == 0)
			tdrMaxFileLengthBytes = template.tdrMaxFileLengthBytes;
		
		if(maxUssdLength == 0)
			maxUssdLength = template.maxUssdLength;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validations = new Validator() //
				.notLess("imsiChangeLockoutHours", imsiChangeLockoutHours, 0) //
				.notLess("imeiRefreshInterval", imeiRefreshInterval, 0) // 0 Days
				.notLess("channelRequestTimeoutSeconds", channelRequestTimeoutSeconds, 5) // 5 Minutes
				.notLess("tdrRotationIntervalSeconds", tdrRotationIntervalSeconds, 60) // 1 Min
				.notMore("tdrRotationIntervalSeconds", tdrRotationIntervalSeconds, 24 * 60 * 26) // 1 Day
				.notLess("tdrMaxFileLengthBytes", tdrMaxFileLengthBytes, 1024 * 1024) // 1 Meg
				.notMore("tdrMaxFileLengthBytes", tdrMaxFileLengthBytes, 1024 * 1024 * 1024) // 1 Gig
				.validExpandablePath("tdrDirectory", Phrase.en(tdrDirectory), tdrDirectoryFields) //
				.validExpandableFilename("tdrFilenameFormat", Phrase.en(tdrFilenameFormat), tdrFilenameFields) //
				.validExpandableText("tdrCopyCommand", Phrase.en(tdrCopyCommand), tdrCopyCommandFields) //
				.validExpandableFilename("zipFilenameFormat", Phrase.en(zipFilenameFormat), zipFilenameFields) //
				.validExpandableText("zipCopyCommand", Phrase.en(zipCopyCommand), zipCopyCommandFields) //
				.notLess("zipTdrAfterDays", zipTdrAfterDays, 1) //
				.notLess("deleteZipAfterDays", deleteZipAfterDays, zipTdrAfterDays + 1) //
				.notLess("oltpTransactionRetentionDays", oltpTransactionRetentionDays, deleteZipAfterDays + 1) //
				.notNull("oltpTransactionCleanupTimeOfDay", oltpTransactionCleanupTimeOfDay) //
				.notLess("olapTransactionRetentionDays", olapTransactionRetentionDays, 1) //
				.notLess("olapTransactionCleanupTimeOfDay", olapTransactionCleanupTimeOfDay, 0) //
				.notMore("olapTransactionCleanupTimeOfDay", olapTransactionCleanupTimeOfDay, (24 * 60 * 60) - 1)
				.notMore("maxUssdLength", maxUssdLength, 182)
				.notLess("maxUssdLength", maxUssdLength, 1)
				;

		for (String key : errorMessages.keySet())
		{
			Phrase message = errorMessages.get(key);
			if (Phrase.someNullOrEmpty(message))
				validations.append(Violation.CANNOT_BE_EMPTY, String.format("errorMessages.%s", key), null, "%s Error message is empty", key);
		}

		return validations.toList();
	}

}

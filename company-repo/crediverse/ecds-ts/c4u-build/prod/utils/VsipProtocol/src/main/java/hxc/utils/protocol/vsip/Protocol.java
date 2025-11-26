package hxc.utils.protocol.vsip;

import java.util.Date;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Protocol
{
	final static Logger logger = LoggerFactory.getLogger(Protocol.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////

	// This flag is used on completion of the refill transaction. It indicates to the
	// Voucher Server if the transaction should be committed or rolled back.
	// commit: Commit Transaction
	// rollback: Rollback Transaction

	public static final String ACTION_COMMIT = "commit";
	public static final String ACTION_ROLLBACK = "rollback";

	public static boolean validateAction(IValidationContext context, boolean required, final String action)
	{
		if (isEmpty(action))
			return wasRequired(context, required, "Action");
		return test(context, "Action", action.equals(ACTION_COMMIT) || action.equals(ACTION_ROLLBACK));
	}

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	// Size: 8-20 Allowed: 0-9

	private static final Pattern patternActivationCode = Pattern.compile("^[0-9]{8,20}$");

	public static boolean validateActivationCode(IValidationContext context, boolean required, String activationCode)
	{
		if (isEmpty(activationCode))
			return wasRequired(context, required, "ActivationCode");
		return test(context, "ActivationCode", activationCode, patternActivationCode);
	}

	// This flag can be used on reservation to automatically commit the voucher or
	// to request automatic rollback of the voucher in case of no commit within a
	// specified time.
	// Range: 8-20
	public static boolean validateActivationCodeLength(IValidationContext context, boolean required, Integer activationCodeLength)
	{
		if (isEmpty(activationCodeLength))
			return wasRequired(context, required, "ActivationCodeLength");
		return test(context, "ActivationCodeLength", activationCodeLength >= 8 && activationCodeLength <= 20);
	}

	// commit: Commit Transaction
	// autoRollback: Automatic Rollback of Transaction in case of no commit within specified time

	public static final String ADDITIONALACTION_COMMIT = "commit";
	public static final String ADDITIONALACTION_AUTO_ROLLBACK = "autoRollback";

	public static boolean validateAdditionalAction(IValidationContext context, boolean required, String additionalAction)
	{
		if (isEmpty(additionalAction))
			return wasRequired(context, required, "AdditionalAction");
		return test(context, "AdditionalAction", additionalAction.equals(ADDITIONALACTION_COMMIT) || additionalAction.equals(ADDITIONALACTION_AUTO_ROLLBACK));
	}

	// The additionalInfo parameter is used to hold additional information such
	// as how many vouchers of a voucher batch file that was successfully loaded.
	// Size: 1-255
	public static boolean validateAdditionalInfo(IValidationContext context, boolean required, String additionalInfo)
	{
		if (isEmpty(additionalInfo))
			return wasRequired(context, required, "AdditionalInfo");
		return test(context, "AdditionalInfo", additionalInfo, 1, 255);
	}

	// The agent parameter is used to indicate the name of the dealer who has
	// received the card from the service provider.
	// Size: 1-8 Allowed: A-Z,a-z,0-9
	private static final Pattern patternAgent = Pattern.compile("^[0-9,a-z,A-Z]{1,8}$");

	public static boolean validateAgent(IValidationContext context, boolean required, String agent)
	{
		if (isEmpty(agent))
			return wasRequired(context, required, "Agent");
		return test(context, "Agent", agent, patternAgent);
	}

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	// Size: 1-30 Allowed: A-Z,a-z,0-9,_
	private static final Pattern patternBatchId = Pattern.compile("^[0-9,a-z,A-Z,_]{1,30}$");

	public static boolean validateBatchId(IValidationContext context, boolean required, String batchId)
	{
		if (isEmpty(batchId))
			return wasRequired(context, required, "BatchId");
		return test(context, "BatchId", batchId, patternBatchId);
	}

	// The currency parameter is used to indicate the currency of the voucher value.
	// The currency is expressed as a three letter string according to the ISO 4217
	// standard, see Codes for the representation of currencies and funds, Reference
	// [6]. Examples are "EUR" for Euro and "SEK" for Swedish Kronor.
	// Size: 3 Range: ISO 4217
	private static final Pattern patternCurrency = Pattern.compile("^[A-Z]{3}$");

	public static boolean validateCurrency(IValidationContext context, boolean required, String currency)
	{
		if (isEmpty(currency))
			return wasRequired(context, required, "Currency");
		return test(context, "Currency", currency, patternCurrency);
	}

	// The executionTime parameter is used to define the time when a task was
	// run or should be run.
	// Allowed: yyyyMMddThh:mm:ssTZ
	public static boolean validateExecutionTime(IValidationContext context, boolean required, Date executionTime)
	{
		if (isEmpty(executionTime))
			return wasRequired(context, required, "ExecutionTime");
		return test(context, "ExecutionTime", true);
	}

	// The expiryDate parameter is used to identify the last date when the voucher
	// will be usable in the system. Only the date information will be considered by
	// this parameter. The time and timezone should be set to all zeroes, and will
	// be ignored.
	// TZ is the deviation in hours from UTC. This field is optional. This date format
	// does not strictly follow the XML-RPC specification on date format. It does
	// however follow the ISO 8601 specification. Parsers for this protocol must be
	// prepared to parse dates containing timezone.
	// Allowed: yyyyMMddThh:mm:ssTZ
	public static boolean validateExpiryDate(IValidationContext context, boolean required, Date expiryDate)
	{
		if (isEmpty(expiryDate))
			return wasRequired(context, required, "ExpiryDate");
		return test(context, "ExpiryDate", true);
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	// Size: 1-30 Allowed: -A-Za-z0-9_=:?.()
	private static final Pattern patternExtensionText = Pattern.compile("^[\\-,A-Z,a-z,0-9,_,\\=,\\:,\\?,\\.,\\(,\\,\\s)]{1,30}$");

	public static boolean validateExtensionText1(IValidationContext context, boolean required, String extensionText1)
	{
		if (isEmpty(extensionText1))
			return wasRequired(context, required, "ExtensionText1");
		return test(context, "ExtensionText1", extensionText1, patternExtensionText);
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	// Size: 1-30 Allowed: -A-Za-z0-9_=:?.()
	public static boolean validateExtensionText2(IValidationContext context, boolean required, String extensionText2)
	{
		if (isEmpty(extensionText2))
			return wasRequired(context, required, "extensionText2");
		return test(context, "extensionText2", extensionText2, patternExtensionText);
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	// Size: 1-30 Allowed: -A-Za-z0-9_=:?.()
	public static boolean validateExtensionText3(IValidationContext context, boolean required, String extensionText3)
	{
		if (isEmpty(extensionText3))
			return wasRequired(context, required, "ExtensionText3");
		return test(context, "ExtensionText3", extensionText3, patternExtensionText);
	}

	// The failReason parameter is used to describe the cause of a failed task
	// in execution.
	// Size: 1-255
	public static boolean validateFailReason(IValidationContext context, boolean required, String failReason)
	{
		if (isEmpty(failReason))
			return wasRequired(context, required, "FailReason");
		return test(context, "FailReason", failReason, 1, 255);
	}

	// The faultCode parameter is used to signify that a request failed due to illegal
	// request data or internal processing errors. This does not cover business level
	// logic faults, which instead are covered in the responseCode parameter, see
	// Section 9.29 on page 73.
	// 1000: Illegal request message
	// 1001: Mandatory field missing
	// 1002: Illegal data type
	// 1003: Data out of bounds
	// 1004: Unknown operation
	// 1005: Internal server error
	// 1006: This fault code is reserved for future use
	// 1007: Overload rejection

	public static final int FAULT_ILLEGAL_REQUEST_MESSAGE = 1000;
	public static final int FAULT_MANDATORY_FIELD_MISSING = 1001;
	public static final int FAULT_ILLEGAL_DATA_TYPE = 1002;
	public static final int FAULT_DATA_OUT_OF_BOUNDS = 1003;
	public static final int FAULT_UNKNOWN_OPERATION = 1004;
	public static final int FAULT_INTERNAL_SERVER_ERROR = 1005;
	public static final int FAULT_RESERVED = 1006;
	public static final int FAULT_OVERLAY_REJECTION = 1007;

	public static boolean validateFaultCode(IValidationContext context, boolean required, Integer faultCode)
	{
		if (isEmpty(faultCode))
			return wasRequired(context, required, "FaultCode");

		switch (faultCode)
		{
			case FAULT_ILLEGAL_REQUEST_MESSAGE:
			case FAULT_MANDATORY_FIELD_MISSING:
			case FAULT_ILLEGAL_DATA_TYPE:
			case FAULT_DATA_OUT_OF_BOUNDS:
			case FAULT_UNKNOWN_OPERATION:
			case FAULT_INTERNAL_SERVER_ERROR:
			case FAULT_RESERVED:
			case FAULT_OVERLAY_REJECTION:
				return true;
		}

		return test(context, "FaultCode", false);

	}

	// The faultString parameter is used to give a descriptive message as a
	// complement to the faultCode parameter.
	// Size: 1-255
	public static boolean validateFaultString(IValidationContext context, boolean required, String faultString)
	{
		if (isEmpty(faultString))
			return wasRequired(context, required, "FaultString");
		return test(context, "FaultString", faultString, 1, 255);
	}

	// The filename parameter is the filename generated as output from the specific
	// operation. Note that the filename is not a complete filename. The full path of
	// the file is not included and the suffix of the file may be excluded (for report
	// files for example).
	// Size: 1-255 Allowed: A-Z,a-z,0-9,_,.
	private static final Pattern patternFilename = Pattern.compile("^[A-Z,a-z,0-9,_,\\.]{1,255}$");

	public static boolean validateFilename(IValidationContext context, boolean required, String filename)
	{
		if (isEmpty(filename))
			return wasRequired(context, required, "Filename");
		return test(context, "Filename", filename, patternFilename);
	}

	// The fromTime parameter together with toTime parameter is used to create
	// a time frame, which can be used to define, for example a matching criteria.
	// The fromTime parameter is used to determine which vouchers was used in
	// a specific time frame.
	// Allowed: yyyyMMddThh:mm:ssTZ
	public static boolean validateFromTime(IValidationContext context, boolean required, Date fromTime)
	{
		if (isEmpty(fromTime))
			return wasRequired(context, required, "FromTime");
		return test(context, "FromTime", true);
	}

	// The newState parameter is used to represent the state of the voucher as it is,
	// or will be, after a specific event. Such an event could be a request to change
	// a voucher to the new state, or it could be a historical record that represents a
	// state change in the past.

	public static final String STATE_UNAVAILABLE = "unavailable";
	public static final String STATE_AVAILABLE = "available";
	public static final String STATE_PENDING = "pending";
	public static final String STATE_RESERVED = "reserved";
	public static final String STATE_USED = "used";
	public static final String STATE_DAMAGED = "damaged";
	public static final String STATE_STOLEN = "stolen";

	public static boolean validateNewState(IValidationContext context, boolean required, String newState)
	{
		if (isEmpty(newState))
			return wasRequired(context, required, "NewState");
		return test(context, "NewState", //
				newState.equals(STATE_UNAVAILABLE) || //
						newState.equals(STATE_AVAILABLE) || //
						newState.equals(STATE_PENDING) || //
						newState.equals(STATE_USED) || //
						newState.equals(STATE_DAMAGED) || //
						newState.equals(STATE_STOLEN) || //
						newState.equals(STATE_RESERVED));
	}

	// The initialVoucherState parameter is used to set which state the voucher
	// will be in when a voucher is loaded (Voucher Load). A voucher can be set
	// to the state available or unavailable.
	// When performing the GetLoadVoucherBatchFileTaskInfo operation the name
	// and which state the voucher is in will be presented. See Section 9.18.1 on
	// page 67.
	// unavailable: Voucher is loaded, but still unavailable for usage.
	// available: Voucher is loaded and available for usage.
	// pending: Voucher was reserved, but never commited.
	// used: Voucher is used.
	// damaged: Voucher is marked as damaged.
	// stolen: Voucher is marked as stolen/missing.
	public static boolean validateInitialVoucherState(IValidationContext context, boolean required, String initialVoucherState)
	{
		if (isEmpty(initialVoucherState))
			return wasRequired(context, required, "InitialVoucherState");
		return test(context, "InitialVoucherState", //
				initialVoucherState.equals(STATE_UNAVAILABLE) || //
						initialVoucherState.equals(STATE_AVAILABLE) || //
						initialVoucherState.equals(STATE_PENDING) || //
						initialVoucherState.equals(STATE_USED) || //
						initialVoucherState.equals(STATE_DAMAGED) || //
						initialVoucherState.equals(STATE_STOLEN));
	}

	// The networkOperatorId parameter is used to reference a Mobile Virtual
	// Network Operator. The VS system is capable of administering and managing
	// multiple operators simultaneously. Each Mobile Virtual Network Operator has
	// its own database schema, in which this operator's own vouchers are stored.
	// The parameter is bound to the Mobile Virtual Network Operator functionality,
	// which must be explicitly configured. If not activated, the parameter is not
	// mandatory, in which case all requests are targeted to the default database
	// schema of the VS system.
	// Size: 0-20 Allowed: A-Z,a-z,0-9
	private static final Pattern patternNetworkOperatorId = Pattern.compile("^[A-Z,a-z,0-9]{0,20}$");

	public static boolean validateNetworkOperatorId(IValidationContext context, String networkOperatorId)
	{
		if (isEmpty(networkOperatorId))
			return wasRequired(context, context.getIsMultiOperator(), "NetworkOperatorId");
		return test(context, "NetworkOperatorId", networkOperatorId, patternNetworkOperatorId);
	}

	// The numberOfVouchers parameter is used to define the number of vouchers
	// in a batch or a serial number range.
	// Range: 1:1000000
	public static boolean validateNumberOfVouchers(IValidationContext context, boolean required, Integer numberOfVouchers)
	{
		if (isEmpty(numberOfVouchers))
			return wasRequired(context, required, "numberOfVouchers");
		return test(context, "numberOfVouchers", numberOfVouchers, 1, 1000000);
	}

	// The offset parameter is used to indicate a date in the past, by specifying the
	// offset, in days, from the current date. If this parameter is used in a scheduled
	// request the offset will based on the time of execution rather than the current
	// date.
	// Range: 0:999

	public static boolean validateOffset(IValidationContext context, boolean required, Integer offset)
	{
		if (isEmpty(offset))
			return wasRequired(context, required, "Offset");
		return test(context, "Offset", offset, 0, 999);
	}

	// The oldState parameter is used to represent the state of the voucher as it is,
	// or was, prior to a specific event. Such an event could be a request to change a
	// vouchers state from the specified state, or it could be a historical record that
	// represents a state change in the past.
	// unavailable: Voucher is loaded, but still unavailable for usage.
	// available: Voucher is loaded and available for usage.
	// pending: Voucher was reserved, but never commited.
	// used: Voucher is used.
	// damaged: Voucher is marked as damaged.
	// stolen: Voucher is marked as stolen/missing.
	public static boolean validateOldState(IValidationContext context, boolean required, String oldState)
	{
		if (isEmpty(oldState))
			return wasRequired(context, required, "OldState");
		return test(context, "OldState", //
				oldState.equals(STATE_UNAVAILABLE) || //
						oldState.equals(STATE_AVAILABLE) || //
						oldState.equals(STATE_PENDING) || //
						oldState.equals(STATE_USED) || //
						oldState.equals(STATE_DAMAGED) || //
						oldState.equals(STATE_STOLEN));
	}

	// The operatorId parameter is used to define the name of the operator who
	// carried out the operation.
	// When used in a response message it represents the operator that did the
	// change on the voucher.
	// Size: 1-255 Allowed: A-Z,a-z,0-9,–,,,/,.
	private static final Pattern patternOperatorId = Pattern.compile("^[A-Z,a-z,0-9,\\–,\\,,\\/, \\.]{1,255}$");

	public static boolean validateOperatorId(IValidationContext context, boolean required, String operatorId)
	{
		if (isEmpty(operatorId))
			return wasRequired(context, required, "OperatorId");
		return test(context, "OperatorId", operatorId, patternOperatorId);
	}

	// The outputVAC parameter is used to specify whether the Voucher Activation
	// Code should be included in the report generated by a scheduled task.
	// 0: The voucher activation code will not be included in the scheduled report.
	// 1: The voucher activation code will be included in the scheduled report.

	public static boolean validateOutputVAC(IValidationContext context, boolean required, Boolean outputVAC)
	{
		if (isEmpty(outputVAC))
			return wasRequired(context, required, "OutputVAC");
		return test(context, "OutputVAC", true);
	}

	// The purgeVouchers parameter is used to specify whether the vouchers
	// should actually be purged by the PurgeVouchers scheduled task. The
	// PurgeVouchers task will still generate a report even when the vouchers are not
	// actually purged.
	// 0: Vouchers will not be purged.
	// 1: Vouchers will be purged.
	public static boolean validatePurgeVouchers(IValidationContext context, boolean required, Boolean purgeVouchers)
	{
		if (isEmpty(purgeVouchers))
			return wasRequired(context, required, "PurgeVouchers");
		return test(context, "PurgeVouchers", true);
	}

	// The recurrence parameter is, in combination with the recurrenceValue
	// parameter, used to define how often a scheduled task should be executed. This
	// parameter indicates whether the recurrence is described in terms of days,
	// weeks or months.
	// Range: daily,weekly,monthly

	// public static final String RECURRENCE_DAILY = "daily";
	// public static final String RECURRENCE_WEEKLY = "weekly";
	// public static final String RECURRENCE_MONTHLY = "monthly";

	public static boolean validateRecurrence(IValidationContext context, boolean required, Recurrence recurrence)
	{
		if (isEmpty(recurrence))
			return wasRequired(context, required, "Recurrence");
		return test(context, "Recurrence", recurrence.equals(Recurrence.daily) || recurrence.equals(Recurrence.weekly) || recurrence.equals(Recurrence.monthly));
	}

	// The recurrenceValue parameter is, in combination with the
	// recurrence parameter, used to define how often a scheduled task should be
	// executed. This parameter defines the interval of the recurrence.
	// Range: 1:99999
	public static boolean validateRecurrenceValue(IValidationContext context, boolean required, Integer recurrenceValue)
	{
		if (isEmpty(recurrenceValue))
			return wasRequired(context, required, "RecurrenceValue");
		return test(context, "RecurrenceValue", recurrenceValue, 1, 99999);
	}

	// The reportFormat parameter is used to determine what output format the
	// result report file will have.
	// 0: Only failed is written
	// 1: Both failed and successful changes are written
	public static final int REPORTFORMAT_ONLY_FAILED = 0;
	public static final int REPORTFORMAT_BOTH = 1;

	public static boolean validateReportFormat(IValidationContext context, boolean required, Integer reportFormat)
	{
		if (isEmpty(reportFormat))
			return wasRequired(context, required, "ReportFormat");
		return test(context, "ReportFormat", reportFormat == REPORTFORMAT_ONLY_FAILED || reportFormat == REPORTFORMAT_BOTH);
	}

	// The responseCode parameter is sent back after a message has been
	// processed and indicates success or failure of the message.
	// 0: Successful
	// 10: Voucher does not exist
	// 11: Voucher already used by other subscriber
	// 12: Voucher missing/stolen
	// 13: Voucher unavailable
	// 100: Voucher already used by same subscriber
	// 101: Voucher reserved by same subscriber (pending)
	// 102: Voucher expired
	// 103: This responseCode is reserved for future use
	// 104: Subscriber Id mismatch between the reservation and the end of reservation
	// 105: Voucher not reserved
	// 106: Transaction Id mismatch between messages between reservation and the end of reservation
	// 107: Voucher damaged
	// 108: Voucher reserved by other subscriber
	// 109: Database error
	// 110: Bad state transition requested
	// 111: Voucher state change limits exceeded
	// 200: Task does not exist
	// 201: Can not delete a running task

	public static final int RESPONSECODE_SUCCESS = 0;
	public static final int RESPONSECODE_VOUCHER_DOESNT_EXIST = 10;
	public static final int RESPONSECODE_VOUCHER_ALREADY_USED = 11;
	public static final int RESPONSECODE_VOUCHER_MISSING_STOLEN = 12;
	public static final int RESPONSECODE_VOUCHER_UNAVAILABLE = 13;
	public static final int RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER = 100;
	public static final int RESPONSECODE_VOUCHER_RESERVED_SAME_SUBSCRIBER = 101;
	public static final int RESPONSECODE_VOUCHER_EXPIRED = 102;
	public static final int RESPONSECODE_RESERVED = 103;
	public static final int RESPONSECODE_SUBSCRIBER_ID_MISMATCH = 104;
	public static final int RESPONSECODE_VOUCHER_NOT_RESERVED = 105;
	public static final int RESPONSECODE_TRANSACTION_ID_MISMATCH = 106;
	public static final int RESPONSECODE_VOUCHER_DAMAGED = 107;
	public static final int RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER = 108;
	public static final int RESPONSECODE_DATABASE_ERROR = 109;
	public static final int RESPONSECODE_BAD_STATE_TRANSITION = 110;
	public static final int RESPONSECODE_STATE_CHANGE_LIMITS_EXCEEDED = 111;
	public static final int RESPONSECODE_TASK_DOESNT_EXIST = 200;
	public static final int RESPONSECODE_CANNOT_DELETE_RUNNING_TASK = 201;

	// Concurrent Additions for SOAP:
	public static final int RESPONSECODE_MALFORMED_REQUEST = 991;
	public static final int RESPONSECODE_MALFORMED_RESPONSE = 992;
	public static final int RESPONSECODE_NOT_IMPLEMENTED_YET = 993;

	public static boolean validateResponseCode(IValidationContext context, boolean required, Integer responseCode)
	{
		if (isEmpty(responseCode))
			return wasRequired(context, required, "ResponseCode");

		switch (responseCode)
		{
			case RESPONSECODE_SUCCESS:
			case RESPONSECODE_VOUCHER_DOESNT_EXIST:
			case RESPONSECODE_VOUCHER_ALREADY_USED:
			case RESPONSECODE_VOUCHER_MISSING_STOLEN:
			case RESPONSECODE_VOUCHER_UNAVAILABLE:
			case RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER:
			case RESPONSECODE_VOUCHER_RESERVED_SAME_SUBSCRIBER:
			case RESPONSECODE_VOUCHER_EXPIRED:
			case RESPONSECODE_RESERVED:
			case RESPONSECODE_SUBSCRIBER_ID_MISMATCH:
			case RESPONSECODE_VOUCHER_NOT_RESERVED:
			case RESPONSECODE_TRANSACTION_ID_MISMATCH:
			case RESPONSECODE_VOUCHER_DAMAGED:
			case RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER:
			case RESPONSECODE_DATABASE_ERROR:
			case RESPONSECODE_BAD_STATE_TRANSITION:
			case RESPONSECODE_STATE_CHANGE_LIMITS_EXCEEDED:
			case RESPONSECODE_TASK_DOESNT_EXIST:
			case RESPONSECODE_CANNOT_DELETE_RUNNING_TASK:
			case RESPONSECODE_MALFORMED_REQUEST:
			case RESPONSECODE_MALFORMED_RESPONSE:
			case RESPONSECODE_NOT_IMPLEMENTED_YET:
				return true;
		}

		return test(context, "ResponseCode", false);
	}

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	// Size: 8-20 Range: 0-9,a-z,A-Z
	private static final Pattern patternSerialNumber = Pattern.compile("^[A-Z,a-z,0-9]{8,20}$");
	private static final Pattern patternSerialNumeric = Pattern.compile("^[0-9]{8,20}$");

	public static boolean validateSerialNumber(IValidationContext context, boolean required, String serialNumber)
	{
		if (isEmpty(serialNumber))
			return wasRequired(context, required, "SerialNumber");

		if (patternSerialNumeric.matcher(serialNumber).matches())
		{
			try
			{
				Long.parseLong(serialNumber);
			}
			catch (Exception e)
			{
				return test(context, "SerialNumber", false);
			}
		}

		return test(context, "SerialNumber", serialNumber, patternSerialNumber);
	}

	// The serialNumberFirst parameter is used to state the first voucher in the
	// serial number range to be checked. Leading zeros are allowed. The element
	// size defined bellow defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	// Size: 8-20 Allowed: 0-9,a-z,A-Z
	public static boolean validateSerialNumberFirst(IValidationContext context, boolean required, String serialNumberFirst)
	{
		if (isEmpty(serialNumberFirst))
			return wasRequired(context, required, "SerialNumberFirst");
		return test(context, "SerialNumberFirst", serialNumberFirst, patternSerialNumber);
	}

	// The serialNumberLast parameter states the last voucher in the serial
	// number range to be checked. Leading zeros are allowed. The element size
	// defined below defines the limit at protocol level, and may be further restricted at
	// application level by the server side.
	// Size: 8-20 Allowed: 0-9,a-z,A-Z
	public static boolean validateSerialNumberLast(IValidationContext context, boolean required, String serialNumberLast)
	{
		if (isEmpty(serialNumberLast))
			return wasRequired(context, required, "SerialNumberLast");
		return test(context, "SerialNumberLast", serialNumberLast, patternSerialNumber);
	}

	// The state parameter is used to represent the state of a voucher, as it currently
	// is.
	// unavailable: Voucher is loaded, but still unavailable for usage.
	// available: Voucher is loaded and available for usage.
	// pending: Voucher was reserved, but never committed.
	// used: Voucher is used.
	// damaged: Voucher is marked as damaged.
	// stolen: Voucher is marked as stolen/missing.
	// reserved: A voucher is in state reserved when it has been reserved for usage.
	public static boolean validateState(IValidationContext context, boolean required, String state)
	{
		if (isEmpty(state))
			return wasRequired(context, required, "State");
		return test(context, "State", state.equals(STATE_UNAVAILABLE) || //
				state.equals(STATE_AVAILABLE) || //
				state.equals(STATE_PENDING) || //
				state.equals(STATE_USED) || //
				state.equals(STATE_DAMAGED) || //
				state.equals(STATE_STOLEN) || //
				state.equals(STATE_RESERVED));
	}

	// The subscriberId parameter is used to identify a subscriber in the system.
	// This field will hold the phone number of the subscriber in the same format
	// as held in the account database. The number is usually in national format.
	// Leading zeroes are allowed.
	// Size: 1-15 Allowed: 0-9
	private static final Pattern patternSubscriberId = Pattern.compile("^[0-9]{1,15}$");

	public static boolean validateSubscriberId(IValidationContext context, boolean required, String subscriberId)
	{
		if (isEmpty(subscriberId))
			return wasRequired(context, required, "SubscriberId");
		return test(context, "SubscriberId", subscriberId, patternSubscriberId);
	}

	// The taskId parameter is used to state the unique Id that identifies a task in
	// the VS Task Manager.
	// Range: 0:99999999
	public static boolean validateTaskId(IValidationContext context, boolean required, Integer taskId)
	{
		if (isEmpty(taskId))
			return wasRequired(context, required, "taskId");
		return test(context, "taskId", taskId, 0, 99999999);
	}

	// The taskStatus parameter is used to indicate in what state a task is in.
	// ordered: The task is scheduled for execution
	// completed: The task have finished executing and was successful
	// failed: The task have finished executing and was unsuccessful
	// running: The task is currently executing

	public static final String TASKSTATUS_ORDERED = "ordered";
	public static final String TASKSTATUS_COMPLETED = "completed";
	public static final String TASKSTATUS_FAILED = "failed";
	public static final String TASKSTATUS_RUNNING = "running";

	public static boolean validateTaskStatus(IValidationContext context, boolean required, String taskStatus)
	{
		if (isEmpty(taskStatus))
			return wasRequired(context, required, "TaskStatus");
		return test(context, "TaskStatus",
				taskStatus.equals(TASKSTATUS_ORDERED) || taskStatus.equals(TASKSTATUS_COMPLETED) || taskStatus.equals(TASKSTATUS_FAILED) || taskStatus.equals(TASKSTATUS_RUNNING));
	}

	// The timestamp parameter is detailing the time a voucher state change was
	// done.
	// Range: yyyyMMddThh:mm:ssTZ
	public static boolean validateTimestamp(IValidationContext context, boolean required, Date timestamp)
	{
		if (isEmpty(timestamp))
			return wasRequired(context, required, "timestamp");
		return test(context, "timestamp", true);
	}

	// The toTime parameter together with the fromTime parameter is used to
	// create an interval and within this time frame is the matching criteria. The
	// toTime parameter is used to determine which vouchers was used in a specific
	// time frame.
	// Range: yyyyMMddThh:mm:ssTZ
	public static boolean validateToTime(IValidationContext context, boolean required, Date toTime)
	{
		if (isEmpty(toTime))
			return wasRequired(context, required, "ToTime");
		return test(context, "ToTime", true);
	}

	// The transactionId parameter should be unique among transactions, and it
	// must be common among different requests within the same transaction.
	// Size: 1-20 Allowed: 0-9
	private static final Pattern patternTransactionId = Pattern.compile("^[0-9]{1,20}$");

	public static boolean validateTransactionId(IValidationContext context, boolean required, String transactionId)
	{
		if (isEmpty(transactionId))
			return wasRequired(context, required, "TransactionId");
		return test(context, "TransactionId", transactionId, patternTransactionId);
	}

	// The value parameter is used to specify the actual value of the voucher
	// in currency units. The value is formatted as a numeric string. No decimal
	// separator is included. The amount is expressed in the lowest denomination of
	// the specified currency. For example a USD 100 value is represented as 10000.
	// Size: 1-12 Allowed: 0-9
	public static boolean validateValue(IValidationContext context, boolean required, Long value)
	{
		if (isEmpty(value))
			return wasRequired(context, required, "Value");
		return test(context, "Value", value, 1L, 999999999999L);
	}

	// The voucherGroup parameter is used to define a set of properties that are
	// associated with a voucher. Each voucher is assigned to a voucher group and
	// many vouchers can be assigned the same voucher group.
	// Size: 1-4 Allowed: A-Z,a-z,0-9
	private static final Pattern patternVoucherGroup = Pattern.compile("^[0-9,A-Z,a-z]{1,4}$");

	public static boolean validateVoucherGroup(IValidationContext context, boolean required, String voucherGroup)
	{
		if (isEmpty(voucherGroup))
			return wasRequired(context, required, "VoucherGroup");
		return test(context, "VoucherGroup", voucherGroup, patternVoucherGroup);
	}

	// The voucherExpired parameter is used to indicate if the voucher has passed
	// the expiration date. When this parameter is set the voucher can no longer be
	// used. If the voucher is not expired this parameter will not be included.
	// 1: Voucher is expired.
	public static boolean validateVoucherExpired(IValidationContext context, boolean required, Boolean voucherExpired)
	{
		if (isEmpty(voucherExpired))
			return wasRequired(context, required, "VoucherExpired");
		return test(context, "VoucherExpired", true);
	}

	// The supplierId parameter is used to indicate the supplier (print shop) for
	// which voucher batch files with separate encryption keys per supplier will be
	// generated.
	// Size: 1-8 Allowed: A-Z,a-z,0-9
	private static final Pattern patternSupplierId = Pattern.compile("^[0-9,A-Z,a-z]{1,8}$");

	public static boolean validateSupplierId(IValidationContext context, boolean required, String supplierId)
	{
		if (isEmpty(supplierId))
			return wasRequired(context, required, "SupplierId");
		return test(context, "SupplierId", supplierId, patternSupplierId);
	}

	// Validates exclusivity between two fields
	public static boolean validateExclusivity(IValidationContext context, String field1, String field2, Object exclusive1, Object exclusive2)
	{
		if (!(isEmpty(exclusive1) ^ isEmpty(exclusive2)))
		{
			logger.error("Either '{}' or '{}' need to be set. Not both.", field1, field2);
			return false;
		}

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validator
	//
	// /////////////////////////////////

	private static boolean isEmpty(Object value)
	{
		return value == null || (value instanceof String && ((String) value).length() == 0);
	}

	private static boolean wasRequired(IValidationContext context, boolean required, String field)
	{
		if (required)
			logger.error("Required field '{}' is empty", field);
		return !required;
	}

	private static boolean test(IValidationContext context, String field, boolean isValid)
	{
		if (!isValid)
		{
			logger.error("Invalid '{}' value", field);
			return false;
		}

		return true;
	}

	private static boolean test(IValidationContext context, String field, String value, Pattern pattern)
	{
		return test(context, field, value != null && pattern.matcher(value).matches());
	}

	private static boolean test(IValidationContext context, String field, String text, int minLength, int maxLength)
	{
		if (text == null || text.length() < minLength)
		{
			logger.error("'{}' text too short", field);
			return false;
		}
		else if (text.length() > maxLength)
		{
			logger.error("'{}' text too long", field);
			return false;
		}

		return true;

	}

	private static boolean test(IValidationContext context, String field, Integer value, int minValue, int maxValue)
	{
		return test(context, field, value != null && value >= minValue && value <= maxValue);
	}

	private static boolean test(IValidationContext context, String field, Long value, long minValue, long maxValue)
	{
		return test(context, field, value != null && value >= minValue && value <= maxValue);
	}

}

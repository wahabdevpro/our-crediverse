package cs.constants;

public class MissingApiConstants
{
	public static final String	ACC_NOT_FOUND = "ACC_NOT_FOUND";
	public static final String  CANNOT_BE_EMPTY = "CANNOT_BE_EMPTY"; // Status.NOT_ACCEPTABLE
	public static final String  RECURSIVE = "RECURSIVE"; // Status.NOT_ACCEPTABLE
	public static final String  TOO_LONG = "TOO_LONG"; // Status.NOT_ACCEPTABLE
	public static final String  INVALID_VALUE = "INVALID_VALUE"; // Status.NOT_ACCEPTABLE
	public static final String  CANNOT_HAVE_VALUE = "CANNOT_HAVE_VALUE"; // Status.NOT_ACCEPTABLE
	public static final String  CANT_BE_CHANGED = "CANT_BE_CHANGED"; // Status.NOT_ACCEPTABLE
	public static final String  NOT_SAME = "NOT_SAME"; // Status.NOT_ACCEPTABLE
	public static final String  TOO_SMALL = "TOO_SMALL"; // Status.NOT_ACCEPTABLE
	public static final String  TOO_LARGE = "TOO_LARGE"; // Status.NOT_ACCEPTABLE
	public static final String  FAILED_TO_SAVE = "FAILED_TO_SAVE"; // Status.NOT_ACCEPTABLE
	public static final String  FAILED_TO_DELETE = "FAILED_TO_DELETE"; // Status.NOT_ACCEPTABLE
	public static final String  NOT_FOUND = "NOT_FOUND"; // Status.NOT_ACCEPTABLE
	public static final String  FORBIDDEN = "FORBIDDEN"; // Status.FORBIDDEN
	public static final String  AMBIGUOUS = "AMBIGUOUS"; // Status.NOT_ACCEPTABLE
	public static final String  CANNOT_ADD = "CANNOT_ADD"; // Status.NOT_ACCEPTABLE
	public static final String  CANNOT_DELETE = "CANNOT_DELETE"; // Status.NOT_ACCEPTABLE
	public static final String  CANNOT_DELETE_SELF = "CANNOT_DELETE_SELF"; // Status.NOT_ACCEPTABLE
	public static final String  UNAUTHORIZED = "UNAUTHORIZED"; // Status.UNAUTHORIZED
	public static final String  TAMPERED  = "TAMPERED"; // Status.CONFLICT
	public static final String  TECHNICAL_PROBLEM = "TECHNICAL_PROBLEM"; // Status.INTERNAL_SERVER_ERROR
	public static final String  DAY_COUNT_LIMIT = "DAY_COUNT_LIMIT"; // Status.NOT_ACCEPTABLE
	public static final String  DAY_AMOUNT_LIMIT = "DAY_AMOUNT_LIMIT"; // Status.NOT_ACCEPTABLE
	public static final String  MONTH_COUNT_LIMIT = "MONTH_COUNT_LIMIT"; // Status.NOT_ACCEPTABLE
	public static final String  MONTH_AMOUNT_LIMIT = "MONTH_AMOUNT_LIMIT"; // Status.NOT_ACCEPTABLE
	public static final String  INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS"; // Status.NOT_ACCEPTABLE
	public static final String  INSUFFICIENT_PROVISION = "INSUFFICIENT_PROVISION"; // Status.NOT_ACCEPTABLE
	public static final String  LIMIT_REACHED = "LIMIT_REACHED"; // Status.NOT_ACCEPTABLE

	public static final String  ALREADY_REGISTERED = "ALREADY_REGISTERED"; // Status.FORBIDDEN
	public static final String  NOT_REGISTERED = "NOT_REGISTERED"; // Status.FORBIDDEN
	public static final String  INVALID_PIN = "INVALID_PIN"; // Status.NOT_ACCEPTABLE
	public static final String  CONFIRM_PIN_DIFF = "CONFIRM_PIN_DIFF"; 
	public static final String  INVALID_CHANNEL = "INVALID_CHANNEL"; // Status.FORBIDDEN
	public static final String  INVALID_STATE  = "INVALID_STATE"; // Status.FORBIDDEN
	public static final String  TOO_SHORT = "TOO_SHORT";	// Validation Constant

	public static final String  RESOURCE_IN_USE = "RESOURCE_IN_USE";	// Status.RESOURCE_IN_USE
	public static final String  DUPLICATE_VALUE = "DUPLICATE_VALUE";	// Status.DUPLICATE_VALUE
}

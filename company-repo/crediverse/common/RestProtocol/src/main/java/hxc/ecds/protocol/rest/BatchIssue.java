package hxc.ecds.protocol.rest;

public class BatchIssue extends Violation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// Additional Responses specific to Batch Processing
	public static final String INVALID_VALUE = "INVALID_VALUE";
	public static final String DOESNT_EXIST = "DOESNT_EXIST";
	public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
	public static final String VALUE_DIFFERS = "VALUE_DIFFERS";
	public static final String INVALID_HEADING = "INVALID_HEADING";
	public static final String MISSING_HEADING = "MISSING_HEADING";
	public static final String CANNOT_ADD = "CANNOT_ADD";
	public static final String CANNOT_UPDATE = "CANNOT_UPDATE";
	public static final String CANNOT_DELETE = "CANNOT_DELETE";
	public static final String CANNOT_ADJUST = "CANNOT_ADJUST";
	public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
	public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
	public static final String ALREADY_PROCESSED = "ALREADY_PROCESSED";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public BatchIssue(Integer lineNumber, String returnCode, String property, Object criterium, String additionalInformation)
	{
		super(returnCode, property, criterium, additionalInformation);
		this.lineNumber = lineNumber;
	}

	public BatchIssue()
	{
		super(null, null, null, null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Integer lineNumber;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Integer getLineNumber()
	{
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber)
	{
		this.lineNumber = lineNumber;
	}

}

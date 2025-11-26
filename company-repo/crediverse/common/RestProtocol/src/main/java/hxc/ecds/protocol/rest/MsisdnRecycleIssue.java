package hxc.ecds.protocol.rest;

@Deprecated
/*
 * Functionality on hold
 */
public class MsisdnRecycleIssue extends Violation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// Additional Responses specific to Batch Processing
	public static final String INVALID_VALUE = "INVALID_VALUE";
	public static final String AGENT_ID_NOT_FOUND = "AGENT_ID_NOT_FOUND";
	public static final String NON_ZERO_BALANCE = "NON_ZERO_BALANCE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MsisdnRecycleIssue( String returnCode, String property, String additionalInformation)
	{
		super(returnCode, property, null, additionalInformation);
	}

	public MsisdnRecycleIssue()
	{
		super(null, null, null, null);
	}



}

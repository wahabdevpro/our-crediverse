package hxc.ecds.protocol.rest;

public class Violation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final String CANNOT_BE_EMPTY = "CANNOT_BE_EMPTY";
	public static final String CANNOT_HAVE_VALUE = "CANNOT_HAVE_VALUE";
	public static final String INVALID_VALUE = "INVALID_VALUE";
	public static final String CANT_BE_CHANGED = "CANT_BE_CHANGED";
	public static final String NOT_SAME = "NOT_SAME";
	public static final String SAME = "SAME";
	public static final String TOO_SMALL = "TOO_SMALL";
	public static final String TOO_LARGE = "TOO_LARGE";
	public static final String TOO_LONG = "TOO_LONG";
	public static final String TOO_SHORT = "TOO_SHORT";
	public static final String RECURSIVE = "RECURSIVE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String returnCode;
	private String property;
	private Object criterium;
	private String additionalInformation;

    public Violation() {
    }

    // //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getReturnCode()
	{
		return returnCode;
	}

	public String getProperty()
	{
		return property;
	}

	public Object getCriterium()
	{
		return criterium;
	}

	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Violation(String returnCode, String property, Object criterium, String additionalInformation)
	{
		this.returnCode = returnCode;
		this.property = property;
		this.criterium = criterium;
		this.additionalInformation = additionalInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// NOTE: additionalInformation not for public viewing!
	@Override
	public String toString()
	{
		return additionalInformation == null || additionalInformation.isEmpty() ? returnCode : additionalInformation;
	}
}

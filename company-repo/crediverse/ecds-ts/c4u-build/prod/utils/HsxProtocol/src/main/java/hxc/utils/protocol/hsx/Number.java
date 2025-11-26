package hxc.utils.protocol.hsx;

public class Number
{
	public enum NumberType
	{
		UNKNOWN, // = 0
		INTERNATIONAL, // = 1
		NATIONAL, // = 2
		NETWORKSPECIFIC, // = 3
		SUBSCRIBER, // = 4
		ALPHANUMERIC, // = 5
		ABBREVIATED // = 6
	}

	public enum NumberPlan
	{
		UNKNOWN, // = 0
		ISDN, // = 1
		UNUSED2, // = 2
		DATA, // = 3
		TELEX, // = 4
		UNUSED5, // = 5
		UNUSED6, // = 6
		UNUSED7, // = 7
		NATIONAL, // = 8
		PRIVATE, // = 9
		ERMES, // = 10

	}

	public String addressDigits;
	public NumberType numberType;
	public NumberPlan numberPlan;
}

package com.concurrent.hxc;

public class Number
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enumerations
	//
	// /////////////////////////////////

	public enum NumberType
	{
		UNKNOWN, // = 0
		INTERNATIONAL, // = 1
		NATIONAL, // = 2
		NETWORKSPECIFIC, // = 3
		SUBSCRIBER, // = 4
		ALPHANUMERIC, // = 5
		ABBREVIATED, // = 6

		DEVICE, IMSI, IMEI,
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String addressDigits;
	protected NumberType numberType;
	protected NumberPlan numberPlan;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getAddressDigits()
	{
		return addressDigits;
	}

	public void setAddressDigits(String addressDigits)
	{
		this.addressDigits = addressDigits;
	}

	public NumberType getNumberType()
	{
		return numberType;
	}

	public void setNumberType(NumberType numberType)
	{
		this.numberType = numberType;
	}

	public NumberPlan getNumberPlan()
	{
		return numberPlan;
	}

	public void setNumberPlan(NumberPlan numberPlan)
	{
		this.numberPlan = numberPlan;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Number()
	{
	}

	public Number(String msisdn)
	{
		this.addressDigits = msisdn;
		this.numberType = NumberType.UNKNOWN;
		this.numberPlan = NumberPlan.UNKNOWN;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return addressDigits;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Number))
			return false;
		Number that = (Number) obj;
		return this.addressDigits.equalsIgnoreCase(that.addressDigits);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	// /////////////////////////////////
	public static String validate(Number number)
	{
		if (number == null)
			return "No Number";

		if (number.addressDigits == null || number.addressDigits.length() == 0)
			return "No Address Digits";

		if (number.numberType == null)
			return "No NumberType";

		if (number.numberPlan == null)
			return "No NumberPlan";

		return null;
	}

	public static Number[] fromString(String[] numbers, NumberType numberType, NumberPlan numberPlan)
	{
		if (numbers == null)
			return new Number[0];

		Number[] result = new Number[numbers.length];
		for (int index = 0; index < numbers.length; index++)
		{
			Number number = new Number();
			number.addressDigits = numbers[index];
			number.numberType = numberType;
			number.numberPlan = numberPlan;
			result[index] = number;
		}

		return result;
	}

	public String toMSISDN()
	{
		return addressDigits;
	}

}
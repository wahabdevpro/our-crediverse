package com.concurrent.hxc;

public class GetLocaleSettingsResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String languageCode;
	private String name;
	private String alphabet;
	private String dateFormat;
	private String encodingScheme;
	private int currencyDecimalDigits;
	private String currencyCode;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getLanguageCode()
	{
		return languageCode;
	}

	public void setLanguageCode(String languageCode)
	{
		this.languageCode = languageCode;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAlphabet()
	{
		return alphabet;
	}

	public void setAlphabet(String alphabet)
	{
		this.alphabet = alphabet;
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public String getEncodingScheme()
	{
		return encodingScheme;
	}

	public void setEncodingScheme(String encodingScheme)
	{
		this.encodingScheme = encodingScheme;
	}

	public int getCurrencyDecimalDigits()
	{
		return currencyDecimalDigits;
	}

	public void setCurrencyDecimalDigits(int currencyDecimalDigits)
	{
		this.currencyDecimalDigits = currencyDecimalDigits;
	}
	
	public String getCurrencyCode()
	{
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode)
	{
		this.currencyCode = currencyCode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public GetLocaleSettingsResponse(GetLocaleSettingsRequest request)
	{
		super(request);
	}

}
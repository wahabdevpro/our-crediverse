package hxc.utils.protocol.uiconnector.response;

import java.util.ArrayList;

public class GetLocaleInformationResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 1111035892193052080L;

	private ArrayList<String> languages;
	private ArrayList<String> alphabet;
	private String currencyCode;
	private int currencyDecimalDigits;

	public GetLocaleInformationResponse()
	{
		languages = new ArrayList<>();
		alphabet = new ArrayList<>();
	}

	public void setLanguage(int id, String language)
	{
		languages.add(id, language);
	}

	public void setLanguages(ArrayList<String> languages)
	{
		this.languages = languages;
	}

	public String getLanguage(int id)
	{
		return this.languages.get(id);
	}

	public ArrayList<String> getLanguages()
	{
		return this.languages;
	}

	public void setAlphabet(int id, String alphabet)
	{
		this.alphabet.add(id, alphabet);
	}

	public void setAlphabet(ArrayList<String> alphabet)
	{
		this.alphabet = alphabet;
	}

	public String getAlphabet(int id)
	{
		return this.alphabet.get(id);
	}

	public ArrayList<String> getAlphabet()
	{
		return this.alphabet;
	}

	public String getCurrencyCode()
	{
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode)
	{
		this.currencyCode = currencyCode;
	}

	public int getCurrencyDecimalDigits()
	{
		return currencyDecimalDigits;
	}

	public void setCurrencyDecimalDigits(int currencyDecimalDigits)
	{
		this.currencyDecimalDigits = currencyDecimalDigits;
	}

}

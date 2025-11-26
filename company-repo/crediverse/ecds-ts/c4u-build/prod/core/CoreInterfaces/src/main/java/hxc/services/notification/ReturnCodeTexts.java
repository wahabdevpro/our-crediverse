package hxc.services.notification;

import java.io.Serializable;

import hxc.configuration.Configurable;
import hxc.servicebus.ReturnCodes;

@SuppressWarnings("serial")
@Configurable
public class ReturnCodeTexts implements Serializable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ReturnCodes returnCode;
	private Phrase phrase;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	public IPhrase getPhrase()
	{
		return phrase;
	}

	public void setPhrase(IPhrase phrase)
	{
		this.phrase = (Phrase) phrase;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ReturnCodeTexts()
	{
	}

	public ReturnCodeTexts(ReturnCodes returnCode, Phrase phrase)
	{
		this.returnCode = returnCode;
		this.phrase = phrase;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(456);
		sb.append(this.returnCode.toString());
		sb.append('|');
		sb.append(this.phrase.toString());
		return sb.toString();
	}
}

package hxc.services.advancedtransfer;

import hxc.connectors.soap.ISubscriber;

//Subscriber Stub
public class Party implements ISubscriber
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String internationalNumber;
	private int languageID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getInternationalNumber()
	{
		return internationalNumber;
	}

	@Override
	public String getNationalNumber()
	{
		return internationalNumber;
	}

	@Override
	public int getLanguageID()
	{
		return languageID;
	}

	@Override
	public boolean isSameNumber(String msisdn)
	{
		return internationalNumber.equals(msisdn);
	}

	@Override
	public int getServiceClass()
	{
		return 1000;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Party(String internationalNumber, int languageID)
	{
		this.internationalNumber = internationalNumber;
		this.languageID = languageID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

}

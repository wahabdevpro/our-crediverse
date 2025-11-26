package hxc.ecds.protocol.rest;

import java.util.List;

public class BundleLanguage implements IValidatable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int LANGUAGE_MAX_LENGTH = 2;
	public static final int SMS_KEYWORD_MAX_LENGTH = Bundle.SMS_KEYWORD_MAX_LENGTH;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int bundleID;
	protected int version;
	protected String language;
	protected String name;
	protected String description;
	protected String type;
	protected String smsKeyword;
		
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getVersion()
	{
		return version;
	}

	public BundleLanguage setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public int getBundleID()
	{
		return bundleID;
	}

	public BundleLanguage setBundleID(int bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	public String getLanguage()
	{
		return language;
	}

	public BundleLanguage setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public BundleLanguage setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public BundleLanguage setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public BundleLanguage setType(String type)
	{
		this.type = type;
		return this;
	}
	
	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	public BundleLanguage setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
		return this;
	}

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		return new Validator() //
				.notEmpty("name", name, Bundle.NAME_MAX_LENGTH) //
				.notEmpty("description", description, Bundle.DESCRIPTION_MAX_LENGTH) //
				.notEmpty("type", type, Bundle.TYPE_MAX_LENGTH) //
				.notEmpty("language", language, LANGUAGE_MAX_LENGTH) //
				.notLonger("smsKeyword", smsKeyword, SMS_KEYWORD_MAX_LENGTH) //
				.toList();
	}
}

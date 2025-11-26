package hxc.ecds.protocol.rest;

import java.util.List;

public class ClientState implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int KEY_MAX_LENGTH = 20;


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String key;
	protected String value;
	protected int version;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getKey()
	{
		return key;
	}

	public ClientState setKey(String key)
	{
		this.key = key;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public ClientState setValue(String value)
	{
		this.value = value;

		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public ClientState setVersion(int version)
	{
		this.version = version;

		return this;
	}
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notEmpty("key", key, KEY_MAX_LENGTH) //
		;

		return validator.toList();
	}



}

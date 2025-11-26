package hxc.ecds.protocol.rest;

import hxc.ecds.protocol.rest.config.Phrase;

public class UssdCommand
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected Phrase name;
	protected Phrase[] commandFields;
	protected Phrase[] informationFields;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public UssdCommand setId(int id)
	{
		this.id = id;
		return this;
	}

	public Phrase getName()
	{
		return name;
	}

	public UssdCommand setName(Phrase name)
	{
		this.name = name;
		return this;
	}

	public Phrase[] getCommandFields()
	{
		return commandFields;
	}

	public UssdCommand setCommandFields(Phrase[] commandFields)
	{
		this.commandFields = commandFields;
		return this;
	}

	public Phrase[] getInformationFields()
	{
		return informationFields;
	}

	public UssdCommand setInformationFields(Phrase[] informationFields)
	{
		this.informationFields = informationFields;
		return this;
	}
	
	
	
}

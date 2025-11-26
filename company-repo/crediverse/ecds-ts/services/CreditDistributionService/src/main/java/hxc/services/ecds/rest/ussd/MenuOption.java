package hxc.services.ecds.rest.ussd;

import hxc.ecds.protocol.rest.config.Phrase;

public class MenuOption
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected Phrase name;
	protected String value;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase getName()
	{
		return name;
	}
	
	public MenuOption setName(Phrase name)
	{
		this.name = name;
		return this;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public MenuOption setValue(String value)
	{
		this.value = value;
		return this;
	}
	
	
	
	
}

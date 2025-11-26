package hxc.ecds.protocol.rest;

import java.util.List;

public class Department implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int NAME_MAX_LENGTH = 20;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	public Department setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Department setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Department setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Department setName(String name)
	{
		this.name = name;
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
				.notEmpty("name", name, NAME_MAX_LENGTH) //
		;

		return validator.toList();
	}

}

package hxc.ecds.protocol.rest;

import java.util.List;

public class CellGroup implements IValidatable
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
	protected String code;
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

	public CellGroup setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public CellGroup setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public CellGroup setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getCode()
	{
		return code;
	}

	public CellGroup setCode(String code)
	{
		this.code = code;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public CellGroup setName(String name)
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
				.notEmpty("code", code, 5) //
				.notEmpty("name", name, NAME_MAX_LENGTH) //
		;

		return validator.toList();
	}

}

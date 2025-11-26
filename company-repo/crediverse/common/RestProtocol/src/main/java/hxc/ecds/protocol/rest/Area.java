package hxc.ecds.protocol.rest;

import java.util.List;

public class Area implements IValidatable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int NAME_MAX_LENGTH = 30;
	public static final int TYPE_MAX_LENGTH = 30;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected String type;
	protected Integer parentAreaID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Area setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Area setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Area setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Area setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Area setType(String type)
	{
		this.type = type;
		return this;
	}

	public Integer getParentAreaID()
	{
		return parentAreaID;
	}

	public Area setParentAreaID(Integer parentAreaID)
	{
		this.parentAreaID = parentAreaID;
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
				.notEmpty("name", name, NAME_MAX_LENGTH) //
				.notLess("companyID", companyID, 1) //
				.notEmpty("type", type, TYPE_MAX_LENGTH) //
				.toList();
	}
}

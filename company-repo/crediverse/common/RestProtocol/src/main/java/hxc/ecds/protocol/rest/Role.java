package hxc.ecds.protocol.rest;

import java.util.ArrayList;
import java.util.List;

public class Role implements IValidatable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int NAME_MAX_LENGTH = 50;
	public static final int DESCRIPTION_MAX_LENGTH = 80;

	public static final String TYPE_AGENT = "A";
	public static final String TYPE_WEB_USER = "W";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected String description;
	protected boolean permanent;
	protected String type = TYPE_WEB_USER;

	protected List<? extends Permission> permissions = new ArrayList<Permission>();

	public Role() {}

	public Role(Role role) {
		this.id = role.id;
		this.companyID = role.companyID;
		this.version = role.version;
		this.name = role.name;
		this.description = role.description;
		this.permanent = role.permanent;
		this.type = role.type;
		this.permissions = role.permissions;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Role setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public void setCompanyID(int companyID)
	{
		this.companyID = companyID;
	}

	public int getVersion()
	{
		return version;
	}

	public Role setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Role setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Role setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public boolean isPermanent()
	{
		return permanent;
	}

	public Role setPermanent(boolean permanent)
	{
		this.permanent = permanent;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Role setType(String type)
	{
		this.type = type;
		return this;
	}

	public List<? extends Permission> getPermissions()
	{
		return permissions;
	}

	public void setPermissions(List<? extends Permission> permissions)
	{
		this.permissions = permissions;
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
				.notEmpty("description", description, DESCRIPTION_MAX_LENGTH) //
				.notNull("type", type) //
				.oneOf("type", type, TYPE_WEB_USER, TYPE_AGENT) //
				.toList();
	}
}

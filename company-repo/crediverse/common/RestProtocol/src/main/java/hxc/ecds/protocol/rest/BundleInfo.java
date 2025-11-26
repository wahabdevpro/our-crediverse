package hxc.ecds.protocol.rest;

public class BundleInfo 
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String tag;
	protected String name;
	protected String type;
	protected String description;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	public String getTag()
	{
		return tag;
	}

	public BundleInfo setTag(String tag)
	{
		this.tag = tag;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public BundleInfo setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public BundleInfo setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public BundleInfo setDescription(String description)
	{
		this.description = description;
		return this;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

}

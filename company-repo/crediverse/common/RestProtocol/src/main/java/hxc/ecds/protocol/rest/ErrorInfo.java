package hxc.ecds.protocol.rest;

public class ErrorInfo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int code;
	private String description;
	private String type;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getCode()
	{
		return code;
	}

	public ErrorInfo setCode(int code)
	{
		this.code = code;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public ErrorInfo setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public ErrorInfo setDescription(String description)
	{
		this.description = description;
		return this;
	}

}

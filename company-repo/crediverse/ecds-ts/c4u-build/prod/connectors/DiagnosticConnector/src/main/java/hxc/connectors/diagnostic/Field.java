package hxc.connectors.diagnostic;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("Field")
public class Field
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enumerations
	//
	// /////////////////////////////////
	public enum FieldType
	{
		Int, Date, Flag, Text
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XStreamAsAttribute
	private String scope;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
	private FieldType type;

	@XStreamAsAttribute
	private int offset;

	@XStreamAsAttribute
	private int length;

	@XStreamOmitField
	private byte[] currentValue;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public void setScope(String scope)
	{
		this.scope = scope;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String getScope()
	{
		return scope;
	}

	public FieldType getType()
	{
		return type;
	}

	public void setType(FieldType type)
	{
		this.type = type;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public byte[] getCurrentValue()
	{
		return currentValue;
	}

	public void setCurrentValue(byte[] currentValue)
	{
		this.currentValue = currentValue;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

}

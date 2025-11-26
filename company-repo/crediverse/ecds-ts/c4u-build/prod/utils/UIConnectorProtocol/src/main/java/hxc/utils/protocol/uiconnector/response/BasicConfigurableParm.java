package hxc.utils.protocol.uiconnector.response;

import java.io.Serializable;

import hxc.utils.protocol.uiconnector.common.IConfigurableParam;

public class BasicConfigurableParm implements IConfigurableParam, Serializable
{

	private static final long serialVersionUID = -6786473779339961196L;
	private String fieldName;
	private Object value; // each value will need to be serializable

	public BasicConfigurableParm()
	{
	}

	public BasicConfigurableParm(String fieldName)
	{
		this.fieldName = fieldName;
	}

	public BasicConfigurableParm(String fieldName, Object o)
	{
		this.fieldName = fieldName;
		this.value = o;
	}

	@Override
	public int compareTo(IConfigurableParam other)
	{
		return this.fieldName.compareTo(other.getFieldName());
	}

	@Override
	public String getFieldName()
	{
		return fieldName;
	}

	@Override
	public void setFieldName(String name)
	{
		this.fieldName = name;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	@Override
	public void setValue(Object value)
	{
		this.value = value;
	}

}

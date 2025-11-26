package hxc.utils.protocol.uiconnector.bam;

import java.io.Serializable;

import hxc.utils.instrumentation.IDimension;
import hxc.utils.instrumentation.ValueType;

public class Dimension implements IDimension, Serializable
{
	private static final long serialVersionUID = -4564009923923963063L;
	private String name;
	private String units;
	private ValueType valueType;
	private Object value;

	public Dimension(IDimension dimension)
	{
		this.name = dimension.getName();
		this.units = dimension.getUnits();
		this.valueType = dimension.getValueType();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getUnits()
	{
		return units;
	}

	@Override
	public ValueType getValueType()
	{
		return valueType;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

}

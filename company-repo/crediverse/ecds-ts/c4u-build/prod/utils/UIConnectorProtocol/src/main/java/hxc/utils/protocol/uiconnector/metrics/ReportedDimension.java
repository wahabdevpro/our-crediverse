package hxc.utils.protocol.uiconnector.metrics;

import java.io.Serializable;

import hxc.utils.instrumentation.IDimension;
import hxc.utils.instrumentation.ValueType;

@SuppressWarnings("serial")
public class ReportedDimension implements IDimension, Serializable
{

	private String name;
	private String units;
	private ValueType valueType;

	public ReportedDimension()
	{
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getUnits()
	{
		return units;
	}

	public void setUnits(String units)
	{
		this.units = units;
	}

	@Override
	public ValueType getValueType()
	{
		return valueType;
	}

	public void setValueType(ValueType valueType)
	{
		this.valueType = valueType;
	}

}

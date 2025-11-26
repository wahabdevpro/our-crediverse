package hxc.utils.instrumentation;

public class Dimension implements IDimension
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String name;
	private String units;
	private ValueType valueType;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Dimension(String name, String units, ValueType valueType)
	{
		this.name = name;
		this.units = units;
		this.valueType = valueType;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDimension implementation
	//
	// /////////////////////////////////
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

	@Override
	public String toString()
	{
		return name;
	}

}

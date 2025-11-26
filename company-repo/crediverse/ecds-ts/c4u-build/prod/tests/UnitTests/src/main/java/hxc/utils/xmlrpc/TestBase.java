package hxc.utils.xmlrpc;

import hxc.utils.reflection.NonReflective;

public class TestBase
{
	// i4
	private Integer nullInt;
	private Integer negativeInt;
	private int positiveInt;
	@NonReflective
	private int ignoreInt;

	public Integer getNullInt()
	{
		return nullInt;
	}

	public void setNullInt(Integer nullInt)
	{
		this.nullInt = nullInt;
	}

	public Integer getNegativeInt()
	{
		return negativeInt;
	}

	public void setNegativeInt(Integer negativeInt)
	{
		this.negativeInt = negativeInt;
	}

	public int getPositiveInt()
	{
		return positiveInt;
	}

	public void setPositiveInt(int positiveInt)
	{
		this.positiveInt = positiveInt;
	}

	public int getIgnoreInt()
	{
		return ignoreInt;
	}

	public void setIgnoreInt(int ignoreInt)
	{
		this.ignoreInt = ignoreInt;
	}

	// double
	private Double nullDouble;
	private Double negativeDouble;
	private double positiveDouble;

	public Double getNullDouble()
	{
		return nullDouble;
	}

	public void setNullDouble(Double nullDouble)
	{
		this.nullDouble = nullDouble;
	}

	public Double getNegativeDouble()
	{
		return negativeDouble;
	}

	public void setNegativeDouble(Double negativeDouble)
	{
		this.negativeDouble = negativeDouble;
	}

	public double getPositiveDouble()
	{
		return positiveDouble;
	}

	public void setPositiveDouble(double positiveDouble)
	{
		this.positiveDouble = positiveDouble;
	}
}

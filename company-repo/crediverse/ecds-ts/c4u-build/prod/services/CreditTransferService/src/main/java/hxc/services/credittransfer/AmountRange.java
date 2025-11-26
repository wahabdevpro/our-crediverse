package hxc.services.credittransfer;

import java.io.Serializable;

import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;

@SuppressWarnings("serial")
@Configurable
public class AmountRange implements Serializable
{
	private long minValue = 0;
	private long maxValue = 10;

	public AmountRange()
	{
	}

	public AmountRange(long floor, long ceil)
	{
		this.minValue = floor;
		this.maxValue = ceil;
	}

	public long getMinValue()
	{
		return this.minValue;
	}

	public void setMinValue(long floor) throws ValidationException
	{
		if (floor < 0)
			throw new ValidationException("Negative values not allowed for charging range definitions");
		this.minValue = floor;
	}

	public long getMaxValue()
	{
		return this.maxValue;
	}

	public void setMaxValue(long ceil) throws ValidationException
	{
		if (ceil < 0)
			throw new ValidationException("Negative values not allowed for charging range definitions");
		this.maxValue = ceil;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof AmountRange))
			return false;

		AmountRange ar = (AmountRange) obj;
		return minValue == ar.minValue && maxValue == ar.maxValue;
	}

	@Override
	public String toString()
	{
		return String.format("[%s, %s)", minValue, maxValue);
	}

}
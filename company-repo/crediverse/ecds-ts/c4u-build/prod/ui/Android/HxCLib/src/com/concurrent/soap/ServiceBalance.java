package com.concurrent.soap;

import java.util.Date;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class ServiceBalance implements IDeserialisable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String name;
	private long value;
	private String unit;
	private Date expiryDate;
	
	private static final long serialVersionUID = -8517315454374148039L;
	public static final int PROPERTY_COUNT = 4;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	public String getUnit()
	{
		return unit;
	}

	public void setUnit(String unit)
	{
		this.unit = unit;
	}

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public ServiceBalance()
	{
	}

	public ServiceBalance(String name, long value, String unit, Date expiryDate)
	{
		this.name = name;
		this.value = value;
		this.unit = unit;
		this.expiryDate = expiryDate;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public void deserialise(ISerialiser serialiser)
	{
		name = serialiser.getString("name", "");
		value = serialiser.getLong("value", 0L);
		unit = serialiser.getString("unit", "");
		expiryDate = serialiser.getDate("expiryDate", new Date(0));
		
	}

}

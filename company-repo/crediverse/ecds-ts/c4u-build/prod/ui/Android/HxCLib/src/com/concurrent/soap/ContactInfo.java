package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialisable;
import com.concurrent.util.ISerialiser;

public class ContactInfo implements IDeserialisable, ISerialisable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String name;
	
	private static final long serialVersionUID = -5281696263601479587L;

	public static final int PROPERTY_COUNT = 1;

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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ContactInfo()
	{
	}

	public ContactInfo(ContactInfo contactInfo)
	{
		this.name = contactInfo.name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ServiceQuota))
			return false;

		ServiceQuota that = (ServiceQuota) obj;
		return false;
	}

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public void deserialise(ISerialiser serialiser)
	{
		name = serialiser.getString("name", "");
	}

	@Override
	public void serialise(ISerialiser serialiser)
	{
		serialiser.add("name", name);
	}

}

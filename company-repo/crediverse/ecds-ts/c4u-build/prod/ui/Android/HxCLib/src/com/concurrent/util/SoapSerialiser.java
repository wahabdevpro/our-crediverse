package com.concurrent.util;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.kobjects.util.Util;
import org.ksoap2.serialization.SoapObject;

import com.concurrent.hxc.Program;

import android.util.Log;

public class SoapSerialiser implements ISerialiser
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private SoapObject object;
	
	private static final String SOAP_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SoapSerialiser(SoapObject object)
	{
		this.object = object;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Serialisation
	//
	// /////////////////////////////////
	@Override
	public void add(String name, Integer value)
	{
		if (value != null)
			object.addProperty(name, value);
	}

	@Override
	public void add(String name, Long value)
	{
		if (value != null)
			object.addProperty(name, value);
	}

	@Override
	public void add(String name, String value)
	{
		if (value != null)
			object.addProperty(name, value);
	}

	@Override
	public void add(String name, Boolean value)
	{
		if (value != null)
			object.addProperty(name, value);
	}
	
	@Override
	public void add(String name, Date value)
	{
		if (value != null)
		{
			SimpleDateFormat formatter = new SimpleDateFormat(SOAP_DATE_FORMAT);
			object.addProperty(name, formatter.format(value));
		}
	}

	@Override
	public <T extends Enum<?>> void add(String name, T value)
	{
		if (value != null)
			object.addProperty(name, value.toString());
	}

	@Override
	public void add(String name, ISerialisable value)
	{
		if (value == null)
			return;
		
		SoapObject child = new SoapObject("", name);
		SoapSerialiser childSerialiser = new SoapSerialiser(child);
		value.serialise(childSerialiser);
		object.addSoapObject(child);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Deserialisation
	//
	// /////////////////////////////////
	@Override
	public Integer getInteger(String name, Integer defaultValue)
	{
		String value = getString(name, null);
		if (value == null)
			return defaultValue;
		else
			return Integer.parseInt(value);
	}

	@Override
	public Long getLong(String name, Long defaultValue)
	{
		String value = getString(name, null);
		return value != null ? Long.parseLong(value) : defaultValue;
	}

	@Override
	public String getString(String name, String defaultValue)
	{
		if (object.hasProperty(name))
			return object.getPropertyAsString(name);
		else
			return defaultValue;
	}

	@Override
	public Boolean getBoolean(String name, Boolean defaultValue)
	{
		String value = getString(name, null);
		return value != null ? Boolean.parseBoolean(value) : defaultValue;
	}
	
	@Override
	public Date getDate(String name, Date defaultValue)
	{
		String value = getString(name, null);
		// 2015-06-25T00:00:00+02:00
		if (value == null || value.length() == 0)
			return defaultValue;
		
		SimpleDateFormat formatter = new SimpleDateFormat(SOAP_DATE_FORMAT);
		try
		{
			Date result = formatter.parse(value);
			return result;
		}
		catch (ParseException e)
		{
			return defaultValue;
		}

	}

	@Override
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumType, T defaultValue)
	{
		String value = getString(name, null);
		return value != null ? (T) Enum.valueOf(enumType, value) : defaultValue;
	}
	
	@Override
	public <T extends IDeserialisable> T getDeserialisable(String name, T defaultValue)
	{
		return null;
	}
	
	@Override
	public <T extends IDeserialisable> T[] getArray(String name, Class<T> cls)
	{
		return getArray(name, cls, 0);
	}

	@Override
	public <T extends IDeserialisable> T[] getArray(String name, Class<T> cls, int propertyCountx)
	{
		if (!object.hasProperty(name))
			return null;
	
	    @SuppressWarnings("unchecked")
	    T[] results = (T[])(Object[]) Array.newInstance(cls, object.getPropertyCount());
		int count = 0;
		
	    for (int index = 0; index < results.length; index++)
	    {
	    	Object property = object.getProperty(index);
	      	if (!(property instanceof SoapObject))
	    		continue;
	      	
	    	SoapObject element = (SoapObject)property;
	    	if (propertyCountx != 0 && element.getPropertyCount() != propertyCountx)
	    	{
	    		continue;    
	    	}
    		    	
	    	try
			{
				T result = (T)cls.newInstance();
				SoapSerialiser elementSerialiser = new SoapSerialiser(element);
				result.deserialise(elementSerialiser);
				results[count++] = result;
			}
			catch (InstantiationException e)
			{
				Log.wtf(Program.Tag, e);
				continue;
			}
			catch (IllegalAccessException e)
			{
				Log.wtf(Program.Tag, e);
				continue;
			}
	    	
	    }
		
		return java.util.Arrays.copyOf(results, count);
	}


	
	

}

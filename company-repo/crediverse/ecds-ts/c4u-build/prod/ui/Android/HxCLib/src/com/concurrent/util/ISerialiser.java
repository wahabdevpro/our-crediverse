package com.concurrent.util;

import java.util.Date;

public interface ISerialiser
{
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Serialisation
	//
	// /////////////////////////////////
	public abstract void add(String name, Integer value);
	
	public abstract void add(String name, Long value);

	public abstract void add(String name, String value);

	public abstract void add(String name, Boolean value);
	
	public abstract void add(String name, Date value);

	public abstract <T extends Enum<?>> void add(String name, T value);
	
	public abstract void add(String name, ISerialisable value);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Deserialisation
	//
	// /////////////////////////////////
	public abstract Integer getInteger(String name, Integer defaultValue);
	
	public abstract Long getLong(String name, Long defaultValue);

	public abstract String getString(String name, String defaultValue);

	public abstract Boolean getBoolean(String name, Boolean defaultValue);
	
	public abstract Date getDate(String name, Date defaultValue);

	public <T extends Enum<T>> T getEnum(String name, Class<T> enumType, T defaultValue);
	
	//public abstract ISerialisable getSerialisable(String name, ISerialisable defaultValue);
	public abstract <T extends IDeserialisable> T getDeserialisable(String name, T defaultValue);
	
	public abstract <T extends IDeserialisable> T[] getArray(String name, Class<T> cls);
	
	public abstract <T extends IDeserialisable> T[] getArray(String name, Class<T> cls, int propertyCount);
	
}

package com.concurrent.util;

public interface ICallable<T extends IDeserialisable> extends ISerialisable
{
	public abstract T deserialiseResponse(ISerialiser serialiser);
	public abstract String getMethodID();	
}

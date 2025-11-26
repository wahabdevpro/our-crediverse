package com.concurrent.util;

import java.io.Serializable;

public interface IDeserialisable extends Serializable
{
	public abstract void deserialise(ISerialiser serialiser);
}

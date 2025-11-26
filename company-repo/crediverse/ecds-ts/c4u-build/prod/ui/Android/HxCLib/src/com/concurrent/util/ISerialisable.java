package com.concurrent.util;

import java.io.Serializable;

public interface ISerialisable extends Serializable
{
	public abstract void serialise(ISerialiser serialiser);
}

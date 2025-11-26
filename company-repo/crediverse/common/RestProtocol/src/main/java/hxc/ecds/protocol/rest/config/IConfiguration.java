package hxc.ecds.protocol.rest.config;

import java.io.Serializable;

import hxc.ecds.protocol.rest.IValidatable;

public interface IConfiguration extends Serializable, IValidatable
{
	public abstract long uid();

	public abstract int getVersion();

	public abstract void onPostLoad();
}

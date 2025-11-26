package hxc.processmodel;

import hxc.configuration.IConfigurable;

public interface IProcess extends IConfigurable
{
	public String serialize();

	public IProcess getStart();
}
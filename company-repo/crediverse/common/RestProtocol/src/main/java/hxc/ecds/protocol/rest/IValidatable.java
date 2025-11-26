package hxc.ecds.protocol.rest;

import java.util.List;

public interface IValidatable
{
	public abstract List<Violation> validate();
}

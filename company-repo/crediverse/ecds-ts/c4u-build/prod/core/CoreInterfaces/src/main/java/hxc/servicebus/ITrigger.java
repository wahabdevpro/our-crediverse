package hxc.servicebus;

import hxc.connectors.IConnection;

public interface ITrigger
{
	public boolean matches(Object message, IConnection connection);

	public void run(Object message, IConnection connection);

	public boolean getIsLowPriority(Object message);

	public boolean getIsTransaction();

}
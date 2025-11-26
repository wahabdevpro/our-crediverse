package hxc.servicebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IConnection;

public abstract class Trigger<T> implements ITrigger
{
	final static Logger logger = LoggerFactory.getLogger(Trigger.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private properties
	//
	// /////////////////////////////////

	private Class<T> cls;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ESB Side
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public final boolean matches(Object message, IConnection connection)
	{
		return cls.isInstance(message) && testCondition((T) message);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void run(Object message, IConnection connection)
	{
		try
		{
			action((T) message, connection);
		}
		catch (Throwable e)
		{
			logger.error("Failed to execute action", e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service Side
	//
	// /////////////////////////////////

	public Trigger(Class<T> cls)
	{
		this.cls = cls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean getIsLowPriority(Object message)
	{
		return isLowPriority((T) message);
	}

	public boolean isLowPriority(T message)
	{
		return false;
	}

	@Override
	public final boolean getIsTransaction()
	{
		return isTransaction();
	}

	public boolean isTransaction()
	{
		return true;
	}

	public abstract boolean testCondition(T message);

	public abstract void action(T message, IConnection connection);

}

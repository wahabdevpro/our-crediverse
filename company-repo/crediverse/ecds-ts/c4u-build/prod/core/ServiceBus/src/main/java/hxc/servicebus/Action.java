package hxc.servicebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IConnection;

@SuppressWarnings("rawtypes")
public class Action implements Runnable, Comparable
{
	final static Logger logger = LoggerFactory.getLogger(Action.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private ITrigger trigger;
	private Object message;
	private IConnection connection;
	private long priority;

	public boolean isTransaction()
	{
		return trigger.getIsTransaction();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public Action(ITrigger trigger, Object message, IConnection connection, long priority)
	{
		this.trigger = trigger;
		this.message = message;
		this.connection = connection;
		this.priority = priority;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable Implementation
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		trigger.run(message, connection);
	}

	@Override
	public int compareTo(Object o)
	{
		//logger.error(String.format("%s.compareTo(%s)", this, o));
		if (o instanceof Action)
			return Long.compare(priority, ((Action) o).priority);
		else
			return 1;
	}

}

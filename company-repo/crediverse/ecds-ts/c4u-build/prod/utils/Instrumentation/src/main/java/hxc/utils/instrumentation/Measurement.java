package hxc.utils.instrumentation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Measurement implements IMeasurement
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private List<IListener> listeners;
	private IMetric metric;
	private Date timeStamp;
	private Object[] values;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public IMetric getMetric()
	{
		return metric;
	}

	@Override
	public Date getTimeStamp()
	{
		return timeStamp;
	}

	@Override
	public Object[] getValues()
	{
		return values;
	}

	public String describe(String extra)
	{
		return String.format(
			"%s@%s("
			+ "timeStamp = %s, listeners = %s, metric = %s, values = %s"
			+ "%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(timeStamp), listeners, metric, values,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Measurement(List<IListener> listeners, IMetric metric, Date timeStamp, Object... values)
	{
		this.listeners = listeners;
		this.metric = metric;
		this.timeStamp = timeStamp;
		this.values = values;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable Implementation
	//
	// /////////////////////////////////

	@Override
	public void run()
	{
		IListener[] targets = listeners.toArray(new IListener[listeners.size()]);
		for (IListener target : targets)
		{
			try
			{
				target.receive(metric, timeStamp, values);
			}
			catch (Throwable e)
			{

			}
		}

	}

	@Override
	public int compareTo(Object o)
	{
		//logger.error(String.format("%s.compareTo(%s)", this, o));
		if (o instanceof IMeasurement)
			return timeStamp.compareTo(((IMeasurement) o).getTimeStamp());
		else
			return -1;
	}

}

package hxc.utils.instrumentation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.servicebus.IServiceBus;

public class Metric implements IMetric
{
	final static Logger logger = LoggerFactory.getLogger(Metric.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String name;
	private List<IDimension> dimensions = new ArrayList<IDimension>();
	private long minInterval_ms = 0;
	private long nextReportTime = System.currentTimeMillis();
	private List<IListener> listeners = new ArrayList<IListener>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IMetric Implementation
	//
	// /////////////////////////////////

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public IDimension[] getDimensions()
	{
		return dimensions.toArray(new IDimension[dimensions.size()]);
	}

	@Override
	public synchronized void subscribe(IListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public synchronized void unsubscribe(IListener listener)
	{
		listeners.remove(listener);
	}

	public String describe(String extra)
	{
		return String.format(
			"%s@%s("
			+ "name = %s, minInterval_ms = %s, nextReportTime = %s, dimensions = %s, listeners = %s"
			+ "%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			this.name, this.minInterval_ms, this.nextReportTime, this.dimensions, this.listeners,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	/*
	public String toString()
	{
		return this.describe();
	}
	*/

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	private Metric(String name, long minInterval_ms)
	{
		this.name = name;
		this.minInterval_ms = minInterval_ms;
	}

	public static Metric CreateSimple(String name, String units, ValueType type, long minInterval_ms)
	{
		Metric metric = new Metric(name, minInterval_ms);
		metric.dimensions.add(new Dimension(name, units, type));
		return metric;
	}

	public static Metric CreateMultipleDimension(String name, long minInterval_ms, IDimension... dimensions)
	{
		Metric metric = new Metric(name, minInterval_ms);
		for (IDimension dimension : dimensions)
		{
			metric.dimensions.add(dimension);
		}
		return metric;
	}

	public static Metric CreateGraph(String name, long minInterval_ms, String units, Object... columns)
	{
		LinkedList<IDimension> dimensions = new LinkedList<IDimension>();
		for (Object column : columns)
		{
			dimensions.add(new Dimension(column.toString(), units, ValueType.CumulativeCount));
		}
		return CreateMultipleDimension(name, minInterval_ms, dimensions.toArray(new IDimension[dimensions.size()]));
	}

	public static Metric CreateBasedOnProtocol(String name, long minInterval_ms, String units, Class<?> interfaceProtocol, String... excludes)
	{
		LinkedList<IDimension> dimensions = new LinkedList<IDimension>();
		for (Method method : interfaceProtocol.getMethods())
		{
			boolean skip = false;
			String methodName = method.getName();

			if (excludes != null && excludes.length > 0)
			{
				for (String exclude : excludes)
				{
					if (exclude == null)
						continue;

					if (methodName.equalsIgnoreCase(exclude))
						skip = true;
				}

				if (skip)
					continue;
			}

			methodName = methodName.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
			methodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);

			for (IDimension d : dimensions)
			{
				if (methodName.equalsIgnoreCase(d.getName()))
				{
					skip = true;
				}
			}

			if (skip)
				continue;

			dimensions.add(new Dimension(methodName, units, ValueType.CumulativeCount));
		}
		return CreateMultipleDimension(name, minInterval_ms, dimensions.toArray(new IDimension[dimensions.size()]));
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Static Methods
	//
	// /////////////////////////////////

	public Metric prioritise(String... dimensions)
	{

		List<IDimension> newDimensions = new LinkedList<IDimension>();

		for (String dimension : dimensions)
		{
			if (dimension == null)
				continue;

			for (IDimension dime : this.dimensions)
			{
				if (dime == null)
					continue;

				if (dime.getName().replaceAll(" ", "").equalsIgnoreCase(dimension))
				{
					newDimensions.add(dime);
					break;
				}
			}
		}

		for (IDimension dimension : this.dimensions)
		{
			if (dimension == null)
				continue;

			if (!newDimensions.contains(dimension))
				newDimensions.add(dimension);
		}

		this.dimensions = newDimensions;

		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void report(IServiceBus esb, final Object... values)
	{
		long now = System.currentTimeMillis();
		//if ( logger != null ) logger.trace(this, "(%s).report: listeners.size = %s, (now < nextReportTime) = %s, values = %s",
		//	this.describe(), listeners.size(), (now < nextReportTime), values);
		// No Listeners
		if (listeners.size() == 0)
			return;

		// Too soon
		if (now < nextReportTime)
			return;
		nextReportTime += minInterval_ms;

		// Transmit
		try
		{
			esb.sendMeasurement(new Measurement(listeners, this, new Date(), values));
		}
		catch (Throwable throwable)
		{
			logger.error(String.format("(%s).report: caught exception %s", this.describe(), throwable.getMessage()), throwable);
		}

	}

	@Override
	public String toString()
	{
		return name;
	}

}

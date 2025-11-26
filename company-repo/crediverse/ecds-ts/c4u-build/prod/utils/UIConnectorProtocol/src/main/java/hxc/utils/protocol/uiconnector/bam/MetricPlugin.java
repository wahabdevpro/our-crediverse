package hxc.utils.protocol.uiconnector.bam;

import java.io.Serializable;

import hxc.utils.instrumentation.IMetric;

public class MetricPlugin implements Serializable
{
	private static final long serialVersionUID = 2267248954810492968L;
	private String uid;
	private String name;
	private String metrics[];

	public MetricPlugin(Class<?> klass, IMetric metrics[])
	{
		this.uid = klass.getCanonicalName();
		this.name = klass.getSimpleName();
		this.metrics = new String[metrics.length];
		for (int i = 0; i < metrics.length; i++)
		{
			this.metrics[i] = metrics[i].getName();
		}
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getUid()
	{
		return uid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setMetrics(String metrics[])
	{
		this.metrics = metrics;
	}

	public String[] getMetrics()
	{
		return metrics;
	}
}

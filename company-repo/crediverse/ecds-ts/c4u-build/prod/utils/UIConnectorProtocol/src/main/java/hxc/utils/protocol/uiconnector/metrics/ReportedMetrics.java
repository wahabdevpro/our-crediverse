package hxc.utils.protocol.uiconnector.metrics;

import java.io.Serializable;

import hxc.utils.instrumentation.IDimension;
import hxc.utils.instrumentation.IListener;
import hxc.utils.instrumentation.IMetric;

public class ReportedMetrics implements IMetric, Serializable
{

	private static final long serialVersionUID = 1837510780342096899L;
	private String name;
	private IDimension[] dimensions;

	public ReportedMetrics()
	{
	}

	public ReportedMetrics(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public IDimension[] getDimensions()
	{
		return dimensions;
	}

	public void setDimensions(IDimension[] dimensions)
	{
		this.dimensions = dimensions;
	}

	@Override
	public void subscribe(IListener listener)
	{
	}

	@Override
	public void unsubscribe(IListener listener)
	{
	}

}

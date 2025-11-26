package hxc.utils.protocol.uiconnector.bam;

import java.io.Serializable;
import java.util.Date;

import hxc.utils.instrumentation.IMetric;

public class Metric implements Serializable
{
	private static final long serialVersionUID = 4001047010500915818L;

	private String uid;
	private String name;
	private Dimension dimensions[];
	private Date timestamp;
	private boolean updated;

	private Metric(String uid, IMetric metric, Object... values)
	{
		this.uid = uid;
		this.name = metric.getName();
		this.dimensions = new Dimension[metric.getDimensions().length];
		for (int i = 0; i < metric.getDimensions().length; i++)
		{
			this.dimensions[i] = new Dimension(metric.getDimensions()[i]);
		}
		this.updated = true;
	}

	public static Metric CreateMetric(String uid, IMetric metric)
	{
		return new Metric(uid, metric);
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Dimension[] getDimensions()
	{
		return dimensions;
	}

	public void setDimenstions(Dimension dimensions[])
	{
		this.dimensions = dimensions;
	}

	public void setValues(Date timestamp, Object... values)
	{
		if (values == null || values.length == 0)
			return;

		this.timestamp = timestamp;

		for (int i = 0; i < values.length; i++)
		{
			if (dimensions.length >= i)
				dimensions[i].setValue(values[i]);
		}
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setUpdated(boolean updated)
	{
		this.updated = updated;
	}

	public boolean isUpdated()
	{
		return updated;
	}
}

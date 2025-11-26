package hxc.userinterfaces.gui.bam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.utils.instrumentation.IDimension;

public class BAMService
{
	private long uid;
	private String metricName;
	private String serviceName; // Plugin used
	private IDimension[] dimensions;

	public BAMService()
	{
	}

	public BAMService(String serviceName, String metricName, long uid)
	{
		this.serviceName = serviceName;
		this.metricName = metricName;
		this.uid = uid;
	}

	public BAMService(long uid, String metricName)
	{
		this.uid = uid;
		this.metricName = metricName;
	}

	public long getUid()
	{
		return uid;
	}

	public void setUid(long uid)
	{
		this.uid = uid;
	}

	public String getMetricName()
	{
		return metricName;
	}

	public void setMetricName(String metricName)
	{
		this.metricName = metricName;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public IDimension[] getDimensions()
	{
		return dimensions;
	}

	public void setDimensions(IDimension[] dimensions)
	{
		this.dimensions = dimensions;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		job.add("metric", new JsonPrimitive(this.metricName));
		job.add("serviceName", new JsonPrimitive(this.serviceName));
		job.add("uid", new JsonPrimitive(String.valueOf(this.uid)));

		JsonArray jarr = new JsonArray();
		for (IDimension dim : dimensions)
		{
			JsonObject jdim = new JsonObject();
			jdim.add("name", new JsonPrimitive(dim.getName()));
			jdim.add("units", new JsonPrimitive(dim.getUnits()));
			jdim.add("vtype", new JsonPrimitive(String.valueOf(dim.getValueType())));
			jarr.add(jdim);
		}
		job.add("dims", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}

}

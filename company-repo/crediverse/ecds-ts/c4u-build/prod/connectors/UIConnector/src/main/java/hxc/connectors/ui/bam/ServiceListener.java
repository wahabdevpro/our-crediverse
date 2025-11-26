package hxc.connectors.ui.bam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import hxc.utils.instrumentation.IListener;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.metrics.MetricDataRecord;

public class ServiceListener implements IListener
{
	private long pluginUid;
	private ConcurrentLinkedDeque<MetricDataRecord> data;
	private List<String> registeredMetricNames;

	private static final int MAX_QUEUE = 20;

	public ServiceListener(long pluginUid)
	{
		this.pluginUid = pluginUid;
		this.data = new ConcurrentLinkedDeque<>();
		this.registeredMetricNames = new ArrayList<>();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	/**
	 * @return the pluginUid
	 */
	public long getPluginUid()
	{
		return pluginUid;
	}

	/**
	 * @param pluginUid
	 *            the pluginUid to set
	 */
	public void setPluginUid(long pluginUid)
	{
		this.pluginUid = pluginUid;
	}

	/**
	 * @return the data
	 */
	public ConcurrentLinkedDeque<MetricDataRecord> getData()
	{
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(ConcurrentLinkedDeque<MetricDataRecord> data)
	{
		this.data = data;
	}

	/**
	 * @return the registeredMetricNames
	 */
	public List<String> getRegisteredMetricNames()
	{
		return registeredMetricNames;
	}

	/**
	 * @param registeredMetricNames
	 *            the registeredMetricNames to set
	 */
	public void setRegisteredMetricNames(List<String> registeredMetricNames)
	{
		this.registeredMetricNames = registeredMetricNames;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public void registerMetric(IMetric metric)
	{
		if (!registeredMetricNames.contains(metric.getName()))
		{
			metric.subscribe(this);
			registeredMetricNames.add(metric.getName());
		}
	}

	public void removeMetric(String metricName)
	{
		// Removes name from list coming in, next time data is received, the metric is ubsubscribed
		int index = 0;
		for (index = 0; index < registeredMetricNames.size(); index++)
		{
			if (registeredMetricNames.get(index).equalsIgnoreCase(metricName))
			{
				registeredMetricNames.remove(index);
			}
		}
	}

	@Override
	public void receive(IMetric metric, Date timeStamp, Object... values)
	{
		// First check that the metric is still in request
		if (registeredMetricNames.contains(metric.getName()))
		{
			MetricDataRecord md = new MetricDataRecord();
			md.setTimeInMilliSecconds(timeStamp.getTime());
			md.setValues(values);
			data.add(md);

			// Remove older (prevent queue from growing)
			while (data.size() > MAX_QUEUE)
			{
				data.poll();
			}
		}
		else
		{
			metric.unsubscribe(this);
		}

	}

}

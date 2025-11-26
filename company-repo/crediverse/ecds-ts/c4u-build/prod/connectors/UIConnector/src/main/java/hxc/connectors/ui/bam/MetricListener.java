package hxc.connectors.ui.bam;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import hxc.servicebus.IPlugin;
import hxc.utils.instrumentation.IListener;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.bam.Metric;

public class MetricListener implements IListener
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private String uid;
	private List<Metric> metrics;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public MetricListener(String uid)
	{
		this.uid = uid;
		metrics = new LinkedList<Metric>();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Metric Listener Methods
	//
	// /////////////////////////////////

	// Registers a metric into this listener
	public boolean register(IMetric metric)
	{
		// Ensure the metric is valid
		if (metric == null)
			return false;

		// Check if it already contains the metric
		if (contains(metric))
		{
			// Set the value to being updated
			getMetric(metric).setUpdated(true);
			return true;
		}

		// Subscribe the metric to this listener
		metric.subscribe(this);

		// Add the metric to the list of metrics for this listener
		metrics.add(Metric.CreateMetric(uid, metric));

		return true;
	}

	// Unregisters the metric from this listener
	public boolean unregister(IMetric metric)
	{
		// Ensure the metric is valid
		if (metric == null)
			return false;

		// Check if the metric is in the list
		if (!contains(metric))
			return true;

		// Unsubscribe the metric from this listener
		metric.unsubscribe(this);

		// Iterate through the metrics
		for (Metric m : metrics)
		{
			// Check if the metric is found in the list
			if (m.getName().equals(metric.getName()))
			{
				// Remove it from the list
				return metrics.remove(m);
			}
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IListener Implementation
	//
	// /////////////////////////////////

	// When it receives a metric data
	@Override
	public void receive(IMetric imetric, Date timeStamp, Object... values)
	{
		// Ensure the metric is valid
		if (imetric == null)
			return;

		// Get the metric
		Metric metric = getMetric(imetric);

		// Set the new value
		metric.setValues(timeStamp, values);

		// Set the metric updated flag
		metric.setUpdated(true);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Checks whether this listener is listening for that plugin
	public boolean isListener(String uid)
	{
		return this.uid.equals(uid);
	}

	// Checks whether this listener is listening for that plugin
	public boolean isListener(IPlugin plugin)
	{
		return isListener(plugin.getClass().getCanonicalName());
	}

	// Checks if this listener contains the metric name
	public boolean contains(String name)
	{
		for (Metric metric : metrics)
		{
			if (metric != null && metric.getName().equals(name))
				return true;
		}
		return false;
	}

	// Checks if this listener contains the metric
	public boolean contains(IMetric metric)
	{
		return contains(metric.getName());
	}

	// Gets the metric from the metric name
	public Metric getMetric(String metricName)
	{
		for (Metric metric : metrics)
		{
			if (metric == null)
				continue;

			if (metric.getName().equals(metricName))
			{
				metric.setUpdated(false);
				return metric;
			}
		}
		return null;
	}

	// Gets the metric from the imetric
	public Metric getMetric(IMetric metric)
	{
		return getMetric(metric.getName());
	}

	// Checks if the metric has been updated
	public boolean metricUpdated(String metricName)
	{
		for (Metric metric : metrics)
		{
			if (metric == null)
				continue;

			if (metric.getName().equals(metricName))
			{
				return metric.isUpdated();
			}
		}
		return false;
	}
}

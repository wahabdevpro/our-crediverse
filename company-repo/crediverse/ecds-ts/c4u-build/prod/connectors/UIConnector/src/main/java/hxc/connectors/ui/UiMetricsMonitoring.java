package hxc.connectors.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.connectors.ui.bam.MetricInfo;
import hxc.connectors.ui.bam.ServiceListener;
import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.connectors.ui.utils.ConfigurablesIterator;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.Phrase;
import hxc.services.security.IUser;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.metrics.AvailableMetricsResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricData;
import hxc.utils.protocol.uiconnector.metrics.MetricDataRecord;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest;
import hxc.utils.protocol.uiconnector.metrics.PluginMetrics;
import hxc.utils.protocol.uiconnector.metrics.ReportedDimension;
import hxc.utils.protocol.uiconnector.metrics.ReportedMetrics;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse.ErrorCode;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class UiMetricsMonitoring
{
	final static Logger logger = LoggerFactory.getLogger(UiMetricsMonitoring.class);

	private IServiceBus esb;

	// ???
	// session > metric + last timestamp

	private Map<String, List<MetricInfo>> metricRegistrations; // User, [UID, MetricInfo]
	private List<ServiceListener> serviceListeners; // List of call back mechanisms
	private UiSessionManager sessionManager; // To Ensure user still active

	// private Map<String, List<String>> userSessions;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UiMetricsMonitoring(IServiceBus esb, UiSessionManager sessionManager)
	{
		this.esb = esb;
		this.sessionManager = sessionManager;
		this.serviceListeners = new ArrayList<>();
		this.metricRegistrations = new HashMap<String, List<MetricInfo>>();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public AvailableMetricsResponse discoverAvailableMetrics(IUser user, String sessionId)
	{
		List<PluginMetrics> pluginMetrics = extractPluginMetrics(user);
		AvailableMetricsResponse response = new AvailableMetricsResponse(user.getUserId(), sessionId);
		response.setPluginMetrics(pluginMetrics);
		return response;
	}

	/**
	 * Extract metrics for reporting
	 * 
	 * @param user
	 * @return
	 */
	private synchronized List<PluginMetrics> extractPluginMetrics(IUser user)
	{
		List<PluginMetrics> pluginMetrics = new ArrayList<>();
		ConfigurablesIterator configIterator = new ConfigurablesIterator(this.esb, pluginMetrics)
		{
			@Override
			@SuppressWarnings("unchecked")
			public void processConfiguration(IPlugin iplugin, IConfiguration iconfig, String parentPath, Object... refs)
			{
				try
				{
					if (iplugin.getMetrics() != null && iplugin.getMetrics().length > 0)
					{
						// Check if there is no metric with that uid
						PluginMetrics plugMetric = new PluginMetrics();
						plugMetric.setPluginName(iconfig.getName(Phrase.ENG));
						plugMetric.setPluginUID(iconfig.getSerialVersionUID());
						plugMetric.setMetrics(new IMetric[iplugin.getMetrics().length]);

						for (int i = 0; i < iplugin.getMetrics().length; i++)
						{
							IMetric metric = iplugin.getMetrics()[i];
							ReportedDimension[] dimensions = new ReportedDimension[metric.getDimensions().length];
							for (int j = 0; j < metric.getDimensions().length; j++)
							{
								dimensions[j] = new ReportedDimension();
								dimensions[j].setName(metric.getDimensions()[j].getName());
								dimensions[j].setUnits(metric.getDimensions()[j].getUnits());
								dimensions[j].setValueType(metric.getDimensions()[j].getValueType());
							}
							ReportedMetrics reportedMetric = new ReportedMetrics(metric.getName());
							reportedMetric.setDimensions(dimensions);
							plugMetric.getMetrics()[i] = reportedMetric;
						}
						((List<PluginMetrics>) refs[0]).add(plugMetric);
					}
				}
				catch (Exception e)
				{
				}
			}

		};

		configIterator.iterateThroughPlughinConfigurables(false);

		return pluginMetrics;
	}

	// go through all metrics and remove those that do not be long to to any active users
	private void metricCleanUpDeadSessions()
	{
		// Step 1: Remove invalid user registration
		List<String> usersToRemove = new ArrayList<>();
		List<MetricInfo> posibleMetricsToDeregister = new ArrayList<MetricInfo>();
		for (String userId : metricRegistrations.keySet())
		{
			if (!sessionManager.hasUserValidSession(userId))
			{
				posibleMetricsToDeregister.addAll(metricRegistrations.get(userId));
				usersToRemove.add(userId);
			}
		}

		for (String userId : usersToRemove)
		{
			metricRegistrations.remove(userId);
		}

		Iterator<MetricInfo> i = posibleMetricsToDeregister.iterator();
		while (i.hasNext())
		{
			MetricInfo miq = i.next();
			for (List<MetricInfo> mil : metricRegistrations.values())
			{
				for (MetricInfo mi : mil)
				{
					if (miq.getName().equals(mi.getName()))
					{
						i.remove();
						break;
					}
				}
			}
		}

		Iterator<ServiceListener> si = serviceListeners.iterator();
		while (si.hasNext())
		{
			ServiceListener sl = si.next();
			for (MetricInfo mi : posibleMetricsToDeregister)
			{
				if (mi.getUid() == sl.getPluginUid())
				{
					sl.removeMetric(mi.getName());
				}
			}
		}
	}

	private boolean subscribeUserToMonitorMetric(long pluginConfigUID, IMetric metric, String userId)
	{
		logger.trace("UiMetricsMonitoring.subscribeUserToMonitorMetric: pluginConfigUID = {}, metric = {}, userId = {}", pluginConfigUID, metric, userId);
		boolean result = true;
		try
		{
			metricCleanUpDeadSessions();

			// Metric Event registrations
			ServiceListener serviceListener = null;
			for (ServiceListener sl : serviceListeners)
			{
				if (sl.getPluginUid() == pluginConfigUID)
				{
					serviceListener = sl;
					break;
				}
			}

			if (serviceListener == null)
			{
				serviceListener = new ServiceListener(pluginConfigUID);
				serviceListeners.add(serviceListener);
			}

			serviceListener.registerMetric(metric);

			if (!metricRegistrations.containsKey(userId))
				metricRegistrations.put(userId, new ArrayList<MetricInfo>());

			boolean containsMetric = false;
			for (MetricInfo mi : metricRegistrations.get(userId))
			{
				if ((mi.getUid() == pluginConfigUID) && (mi.getName().equals(metric.getName())))
				{
					containsMetric = true;
					break;
				}
			}

			if (!containsMetric)
			{
				MetricInfo mi = new MetricInfo(pluginConfigUID, metric.getName());
				metricRegistrations.get(userId).add(mi);
			}
		}
		catch (Exception e)
		{
			throw (e);
		}
		return result;
	}

	public UiBaseResponse registerMetric(MetricsRequest request)
	{
		logger.trace("UiMetricsMonitoring.registerMetric: request = {}", request);
		UiBaseResponse result = null;
		try
		{
			if (registerMetric(request.getUserId(), request.getPluginUID(), request.getMetricName()))
			{
				result = new ConfirmationResponse(request.getUserId(), request.getSessionId());
			}
			else
			{
				result = new ErrorResponse(request.getUserId(), ErrorCode.GENERAL);
			}
		}
		catch (Exception e)
		{
			result = new ErrorResponse(request.getUserId(), ErrorCode.GENERAL);
			((ErrorResponse) result).setError(String.format("registration failed: %s", e.getMessage()));
		}

		return result;
	}

	private synchronized boolean registerMetric(final String userId, final long pluginUID, final String metricName)
	{
		logger.trace("UiMetricsMonitoring.registerMetric: userId = {}, pluginUID = {}, metricName = {}", userId, pluginUID, metricName);
		List<IMetric> registeredMetrics = new ArrayList<>();
		ConfigurablesIterator configIterator = new ConfigurablesIterator(this.esb, registeredMetrics)
		{
			@Override
			@SuppressWarnings("unchecked")
			public void processConfiguration(IPlugin iplugin, IConfiguration iconfig, String parentPath, Object... refs)
			{
				try
				{
					List<IMetric> metrics = (List<IMetric>) refs[0];
					if (iconfig.getSerialVersionUID() == pluginUID)
					{
						if (iplugin.getMetrics() != null && iplugin.getMetrics().length > 0)
						{
							for (int i = 0; i < iplugin.getMetrics().length; i++)
							{
								if (iplugin.getMetrics()[i].getName().equals(metricName))
								{
									if (subscribeUserToMonitorMetric(pluginUID, iplugin.getMetrics()[i], userId))
									{
										metrics.add(iplugin.getMetrics()[i]);
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
				}
			}

		};
		configIterator.iterateThroughPlughinConfigurables(false);

		return (registeredMetrics.size() > 0);
	}

	public MetricsDataResponse getLatestMetricData(MetricsDataRequest request)
	{
		String userId = request.getUserId();
		MetricsDataResponse result = new MetricsDataResponse(request.getUserId(), request.getSessionId());

		if (metricRegistrations.containsKey(userId))
		{
			for (MetricInfo mi : metricRegistrations.get(userId))
			{
				for (ServiceListener sl : serviceListeners)
				{
					if (sl.getPluginUid() == mi.getUid() && sl.getRegisteredMetricNames().contains(mi.getName()))
					{
						MetricData md = new MetricData();
						md.setUid(sl.getPluginUid());
						md.setName(mi.getName());

						for (MetricDataRecord mr : sl.getData())
						{
							if (request.getLastMillisecondResponse() < mr.getTimeInMilliSecconds())
							{
								md.getRecords().add(mr);
							}
						}
						result.getMetricData().add(md);
					}
				}
			}
		}

		return result;
	}

}

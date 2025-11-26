package hxc.connectors.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.ui.bam.MetricListener;
import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutResponse;
import hxc.utils.protocol.uiconnector.bam.LayoutRecord;
import hxc.utils.protocol.uiconnector.bam.Metric;
import hxc.utils.protocol.uiconnector.bam.MetricPlugin;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricRequest;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricResponse;
import hxc.utils.protocol.uiconnector.bam.SetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.SetUserLayoutResponse;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricRequest;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricResponse;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse.ErrorCode;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class UiBamController
{
	final static Logger logger = LoggerFactory.getLogger(UiBamController.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private IServiceBus esb;
	private UiSessionManager session;

	private List<MetricListener> listeners;

	private String defaultLayout = "H4sIAAAAAAAAAI2SQUvEMBCF/0rIuZQV9dKb7q6CIB66B0U8zCaz" + "NphOSjKRLqX/3emquOAqvYSZx/d4mUkGfX6pq0FnZ3Wlm96UJhCh4RBTmajtylqO5bemC03Qo"
			+ "qBXHmKrliET40HO7cPuNjr7qKuzi6P+SfpFoXnf4RTkyGIv0lhIKdZ38J+ABYaErKvnQbNjP4" + "Xcha26AefRSgISbKeq4phxLH6oTQRKYNgFmkGvEKx3hGrdG0T7P7z22CKxquWizqCqGTinOY5"
			+ "7IHj9Ms82rVzqQnLTJMf4Dnw6yV/L+i3E/YmIX54NpDeZGU0+LGqOo4mYmuDtn/CLvCI3OH2I" + "xfgByBfrhEsCAAA=";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public UiBamController(IServiceBus esb, UiSessionManager session)
	{
		this.esb = esb;
		this.session = session;

		this.listeners = new LinkedList<MetricListener>();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// UiBamController Methods
	//
	// /////////////////////////////////

	// Gets the available plugins that have metrics
	public UiBaseResponse GetAvailablePluginMetrics(GetAvailablePluginMetricsRequest request)
	{
		// Check if the request is valid
		if (!isValid(request))
		{
			// Return invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		GetAvailablePluginMetricsResponse response = new GetAvailablePluginMetricsResponse(request.getUserId(), request.getSessionId());

		// Set the plugins
		response.setMetricPlugins(getPluginsWithAvailableMetrics());

		// Return the response
		return response;
	}

	// Registers a metric
	public UiBaseResponse RegisterMetric(RegisterMetricRequest request)
	{
		logger.trace("UiBamController.RegisterMetric: request = {}", request);
		// Check if the request is valid
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		RegisterMetricResponse response = new RegisterMetricResponse(request.getUserId(), request.getSessionId());

		// Set whether the registration was successful
		response.setRegistered(registerMetric(request.getUid(), getIMetricForString(request.getUid(), request.getMetricName())));

		// Return the response
		return response;
	}

	// Unregisters the metric
	public UiBaseResponse UnregisterMetric(UnregisterMetricRequest request)
	{
		// Check if the request is valid
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		UnregisterMetricResponse response = new UnregisterMetricResponse(request.getUserId(), request.getSessionId());

		// Set whether the unregistration was successful
		response.setUnregistered(unregisterMetric(request.getUid(), getIMetricForString(request.getUid(), request.getMetricName())));

		// Return the response
		return response;
	}

	// Gets the metric data
	public UiBaseResponse GetMetric(GetMetricRequest request)
	{
		// Check if the request is valid
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		GetMetricResponse response = new GetMetricResponse(request.getUserId(), request.getSessionId());

		// Get the metric and set it in the response
		response.setMetric(getMetric(request.getUid(), request.getMetricName(), request.isForce()));

		// Return the response
		return response;
	}

	// Gets multiple metric data
	public UiBaseResponse GetMetrics(GetMetricsRequest request)
	{
		// Checks if the request is valid
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Check if the parameters are valid
		if (request.getUids() == null || request.getMetricNames() == null || request.getUids().length != request.getMetricNames().length)
		{
			return Error(request.getUserId(), ErrorCode.GENERAL, "Invalid Arguments.");
		}

		// Create the response
		GetMetricsResponse response = new GetMetricsResponse(request.getUserId(), request.getSessionId());

		// Iterate through the metrics
		List<Metric> metrics = new LinkedList<Metric>();
		for (int i = 0; i < request.getUids().length; i++)
		{
			// Get the plugin identifier
			String uid = request.getUids()[i];

			// Get the metric name
			String metricName = request.getMetricNames()[i];

			// Add the metric data to the list
			metrics.add(getMetric(uid, metricName, false));
		}

		// Set the metrics in the response
		response.setMetrics(metrics.toArray(new Metric[metrics.size()]));

		// Return the response
		return response;
	}

	// Gets the user layout of metrics
	public UiBaseResponse GetUserLayout(GetUserLayoutRequest request)
	{
		// Check if it is a valid request
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		GetUserLayoutResponse response = new GetUserLayoutResponse(request.getUserId(), request.getSessionId());

		// Get the database connector
		IDatabase database = esb.getFirstConnector(IDatabase.class);

		// Ensure it is not null
		if (database != null)
		{
			// Get the database connection
			try (IDatabaseConnection connection = database.getConnection(null))
			{
				// Check if the table exists
				if (connection.tableExists(LayoutRecord.class))
				{
					// Get the layout
					LayoutRecord layout = connection.select(LayoutRecord.class, " where userId = %s", request.getUserId());

					// If the layout is not null, decompress it and set it in the response
					if (layout != null)
					{
						response.setLayout(decompress(layout.layout));
					}
					// Else the layout is not set, load the default layout
					else
					{
						response.setLayout(decompress(defaultLayout));
					}
				}
				// Else load the default layout
				else
				{
					response.setLayout(decompress(defaultLayout));
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		// Return the response
		return response;
	}

	// Set the user layout
	public UiBaseResponse SetUserLayout(SetUserLayoutRequest request)
	{
		// Check if the request is valid
		if (!isValid(request))
		{
			// Return an invalid request
			return Invalid(request.getUserId());
		}

		// Create the response
		SetUserLayoutResponse response = new SetUserLayoutResponse(request.getUserId(), request.getSessionId());

		// Get the database connector
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		if (database != null)
		{
			// Get the database connection
			try (IDatabaseConnection connection = database.getConnection(null))
			{
				// Check if the table exists, if not, then create the table
				if (!connection.tableExists(LayoutRecord.class))
				{
					connection.createTable(LayoutRecord.class);
				}

				// Create the layout record
				LayoutRecord layout = new LayoutRecord();

				// Fill in the values
				layout.userId = request.getUserId();
				layout.layout = compress(request.getLayout());

				// Update or insert the record
				connection.upsert(layout);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		// Return the response
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Checks if the current session is valid
	private boolean isValid(UiBaseRequest request)
	{
		return session.isValidSession(request.getUserId(), request.getSessionId());
	}

	// Creates an error response with the message and error code
	private ErrorResponse Error(String userId, ErrorCode code, String message)
	{
		ErrorResponse response = new ErrorResponse(userId, code);
		response.setError(message);
		return response;
	}

	// Returns an invalid error response
	private ErrorResponse Invalid(String userId)
	{
		return Error(userId, ErrorCode.SESSION_EXPIRED, "Session is expired/invalid");
	}

	// Gets the plugins that contain metric
	private MetricPlugin[] getPluginsWithAvailableMetrics()
	{
		// List of the plugins
		List<MetricPlugin> plugins = new LinkedList<MetricPlugin>();

		// Iterate through the plugins from the esb
		for (IPlugin plugin : esb.getRegisteredPlugins())
		{
			// Check if the metrics from the plugin is valid
			if (plugin != null && plugin.getMetrics() != null && plugin.getMetrics().length > 0)
				// Add the plugin to the list
				plugins.add(new MetricPlugin( //
						plugin.getClass(), //
						plugin.getMetrics()));
		}

		// Return the array of the plugins
		return plugins.toArray(new MetricPlugin[plugins.size()]);
	}

	// Registers the metric
	private boolean registerMetric(String uid, IMetric metric)
	{
		logger.trace("UiBamController.registerMetric: uid = {}, metric = {}", uid, metric);
		// Ensure the metric is valid
		if (metric == null)
			return false;

		// Iterate through the metric listeners
		for (MetricListener listener : listeners)
		{
			// Ensure the listener is valid
			if (listener == null)
				continue;

			// Check if the listener listens on that plugin
			if (listener.isListener(uid))
			{
				// Register the metric
				return listener.register(metric);
			}
		}

		// If it gets here, then the listener needs to be created for that plugin
		MetricListener listener = new MetricListener(uid);
		// Add the listener to the global listeners
		listeners.add(listener);

		// Call method again
		return registerMetric(uid, metric);
	}

	// Unregisters the metric
	private boolean unregisterMetric(String uid, IMetric metric)
	{
		// Ensure the metric is valid
		if (metric == null)
			return false;

		// Iterate through the listeners
		for (MetricListener listener : listeners)
		{
			// Ensure the listener is valid
			if (listener == null)
				continue;

			// Check if the listener contains the metric
			if (listener.contains(metric))
			{
				// Unregister the metric
				return listener.unregister(metric);
			}
		}

		// If it reaches here, then the metric was not found
		return false;
	}

	// Gets the metric data
	private Metric getMetric(String uid, String metricName, boolean force)
	{
		// Ensure the metric is valid
		if (metricName == null)
			return null;

		// Iterate through the listeners
		for (MetricListener listener : listeners)
		{
			// Ensure the listener is valid
			if (listener == null)
				continue;

			// Check that the listener is a listener of the plugin and that it contains the metric
			if (listener.isListener(uid) && listener.contains(metricName))
			{

				// Wait for the metric data to come in or force
				while (!force && !listener.metricUpdated(metricName))
				{
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{

					}
				}

				// Return the metric data
				return listener.getMetric(metricName);

			}
		}

		return null;
	}

	// Gets an IMetric from the metric name
	private IMetric getIMetricForString(String uid, String metricName)
	{
		// Reference to the metric
		IMetric metric = null;

		// Iterate through the esb plugins
		for (IPlugin plugin : esb.getRegisteredPlugins())
		{
			// Check if the plugins name equals the identifier
			if (plugin != null && plugin.getClass().getCanonicalName().equals(uid))
			{
				// Check that the metrics are valid
				if (plugin.getMetrics() == null || plugin.getMetrics().length == 0)
					continue;

				// Iterate through the metrics
				for (IMetric imetric : plugin.getMetrics())
				{
					// Compare the metric names
					if (imetric.getName().equalsIgnoreCase((String) metricName))
						metric = imetric;
				}
			}
		}

		// Return the metric
		return metric;
	}

	// Compress the string into a compressed base64 bit string
	private String compress(String string) throws SQLException
	{
		// Create the output stream
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		// Create the zip output stream
		GZIPOutputStream zip;
		try
		{
			// Initialise the zip
			zip = new GZIPOutputStream(outStream);

			// Write the string to the stream
			zip.write(string.getBytes("UTF8"));
			zip.close();

			// Flush the contents
			outStream.flush();

			// Get the base64 bit string
			return DatatypeConverter.printBase64Binary(outStream.toByteArray());
		}
		catch (IOException e)
		{
			throw new SQLException(e.getMessage());
		}
		finally
		{
			if (outStream != null)
				try
				{
					outStream.close();
				}
				catch (IOException e)
				{
				}
		}
	}

	// Decompresses a base64 bit string into a json string
	private static String decompress(String string)
	{
		try
		{
			// Read the converted base64 bit string into the zip input stream
			try (GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(string))))
			{
				// Create the byte array output stream
				try (ByteArrayOutputStream output = new ByteArrayOutputStream())
				{
					// Create a buffer
					byte buffer[] = new byte[10000];

					// While there is available bytes in the input stream
					while (zip.available() != 0)
					{
						// Read the contents into the byte buffer
						int count = zip.read(buffer, 0, buffer.length);

						// Ensure there was content read
						if (count < 0)
							break;

						// Write to the output stream
						output.write(buffer, 0, count);
					}

					// Flush the stream
					output.flush();

					// Get the bytes
					byte data[] = output.toByteArray();

					// Create the string from the byte array
					return new String(data, "UTF8");
				}
			}
		}
		catch (IOException e)
		{
		}

		// Else return null
		return null;
	}
}

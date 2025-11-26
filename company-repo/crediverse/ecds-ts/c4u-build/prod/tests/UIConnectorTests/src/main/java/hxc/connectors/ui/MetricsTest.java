package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.IConnection;
import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.servicebus.Trigger;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.metrics.AvailableMetricsResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricData;
import hxc.utils.protocol.uiconnector.metrics.MetricDataRecord;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest.MetricsRequestType;
import hxc.utils.protocol.uiconnector.metrics.PluginMetrics;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

public class MetricsTest
{

	private static BaseTestConfiguration btestConfig;
	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		btestConfig = new BaseTestConfiguration();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		btestConfig.tearDown();
		btestConfig = null;
	}

	private <T> T makeRequest(UiBaseRequest request, Class<T> responseClass) throws Exception
	{
		T resp = null;
		try (UIClient uic = btestConfig.createTestClientConnection())
		{
			resp = uic.call(request, responseClass);
		}
		return resp;
	}

	private AvailableMetricsResponse getMetrics(String user, String sessionId) throws Exception
	{
		AvailableMetricsResponse metResp = null;
		MetricsRequest metRequest = new MetricsRequest(user, sessionId, MetricsRequestType.AvailableMetrics);
		UiBaseResponse resp = makeRequest(metRequest, AvailableMetricsResponse.class);

		assertFalse(resp instanceof ErrorResponse);
		assertTrue(resp instanceof AvailableMetricsResponse);
		metResp = (AvailableMetricsResponse) resp;
		return metResp;
	}

	private PluginMetrics findMetric(String metricName, long pluginUID, List<PluginMetrics> metrics)
	{
		for (PluginMetrics pm : metrics)
		{
			System.out.printf("Metrics for: %s [%d]%n", pm.getPluginName(), pm.getPluginUID());
			for (IMetric met : pm.getMetrics())
			{
				if (met.getName().equalsIgnoreCase(metricName))
				{
					return pm;
				}
			}
		}
		return null;
	}

	private boolean mustStop = false;

	@Test
	public void testExtractMetrics() throws Exception
	{
		// Extract Metric to register
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		AvailableMetricsResponse mr = getMetrics(SECURITY_USER, sessionId);
		// System.out.printf("Available metrics: %d%n", mr.getPluginMetrics().size());
		PluginMetrics pm = findMetric("TPS", 783078316L, mr.getPluginMetrics());
		assertNotNull(pm);
		assertTrue(pm.getMetrics().length > 0);

		// Register (Shouldn't fail)
		MetricsRequest regMonitorRequest = new MetricsRequest(SECURITY_USER, sessionId, MetricsRequestType.RegisterPluginMetricsMonitor);
		regMonitorRequest.setPluginUID(pm.getPluginUID());
		regMonitorRequest.setMetricName(pm.getMetrics()[0].getName());
		UiBaseResponse resp = makeRequest(regMonitorRequest, ConfirmationResponse.class);
		assertFalse(resp instanceof ErrorResponse);
		assertTrue(resp instanceof ConfirmationResponse);

		// Must Fail
        // can I remove this? not sure if this new MetricsRequest(...) has side effects?
		new MetricsRequest(SECURITY_USER, sessionId, MetricsRequestType.RegisterPluginMetricsMonitor);
		regMonitorRequest.setPluginUID(pm.getPluginUID());
		regMonitorRequest.setMetricName("NOExist123");
		UiBaseResponse resp2 = makeRequest(regMonitorRequest, ConfirmationResponse.class);
		assertTrue(resp2 instanceof ErrorResponse);
		assertFalse(resp2 instanceof ConfirmationResponse);

		// Create data
		createSomeTPSData();
		// Thread.sleep(5000);

		// get tps back
		MetricsDataRequest metricsDataRequest = new MetricsDataRequest(SECURITY_USER, sessionId);
		metricsDataRequest.setLastMillisecondResponse(0);
		UiBaseResponse resp3 = makeRequest(metricsDataRequest, MetricsDataResponse.class);

		assertFalse(resp3 instanceof ErrorResponse);
		assertTrue(resp3 instanceof MetricsDataResponse);

		MetricsDataResponse mresp = (MetricsDataResponse) resp3;
		boolean recordGreaterThanZero = false;
		for (MetricData md : mresp.getMetricData())
		{
			assertTrue(md.getName().equals("TPS"));

			System.out.printf("%s [%s]%n", md.getUid(), md.getName());
			for (MetricDataRecord mdr : md.getRecords())
			{
				double dbl = Double.valueOf(String.valueOf(mdr.getValues()[0]));
				recordGreaterThanZero = (dbl > 0) ? true : recordGreaterThanZero;
				System.out.printf("%s := %s%n", mdr.getTimeInMilliSecconds(), dbl);
			}
		}
		assertTrue(recordGreaterThanZero);
	}

	private void createSomeTPSData() throws InterruptedException
	{
		int transactionsToTest = 250;

		final Object stopEvent = new Object();
		mustStop = false;

		btestConfig.getEsb().addTrigger(new Trigger<String>(String.class)
		{
			@Override
			public boolean testCondition(String message)
			{
				return true;
			}

			@Override
			public void action(String message, IConnection connection)
			{
				if (message.equals("Z"))
				{
					synchronized (stopEvent)
					{
						mustStop = true;
						stopEvent.notify();
					}
				}
			}

			@Override
			public boolean isLowPriority(String message)
			{
				return false;
			}

		});

		// Execute transactionsToTest transactions
		long now = System.currentTimeMillis();

		for (int i = 1; i <= 50; i++)
		{
			btestConfig.getEsb().dispatch("A", null);
		}

		btestConfig.getEsb().dispatch("Z", null);

		Thread.sleep(2000);
		for (int i = 22; i <= transactionsToTest; i++)
		{
			btestConfig.getEsb().dispatch("A", null);
		}

		Thread.sleep(2000);

		btestConfig.getEsb().dispatch("Z", null);
		try
		{
			synchronized (stopEvent)
			{
				if (!mustStop)
					stopEvent.wait();
			}
		}
		catch (InterruptedException e)
		{
			fail("Interupted");
		}
		long duration = System.currentTimeMillis() - now;

		// Test TPS
		int measuredTps = btestConfig.getEsb().getCurrentTPS();
		System.out.println(String.format("duration=%dms, measuredTps=%d", duration, measuredTps));
	}

}

package hxc.connectors.ui.log;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.connectors.ui.utils.ConnectionUtils;
import hxc.services.logging.LoggingLevels;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationRequest;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.logtailer.LogFileRequest;
import hxc.utils.protocol.uiconnector.logtailer.LogFileResponse;
import hxc.utils.protocol.uiconnector.logtailer.LogFilterOptions;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

public class LogFileTest
{
	final static Logger logger = LoggerFactory.getLogger(LogFileTest.class);
	
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

	// Get Services and Roles
	public CtrlConfigurationResponse getControlServiceConfiguration(String userId, String sessionId) throws Exception
	{
		CtrlConfigurationRequest creq = new CtrlConfigurationRequest(userId, sessionId);
		UiBaseResponse cresp = makeRequest(creq, CtrlConfigurationResponse.class);

		CtrlConfigurationResponse response = null;
		if (cresp instanceof CtrlConfigurationResponse)
		{
			response = (CtrlConfigurationResponse) cresp;
		}

		return response;
	}

	public LogFileResponse makeLogRequest(LogFilterOptions filter, String sessionId) throws Exception
	{
		LogFileRequest logRequest = new LogFileRequest(SECURITY_USER, sessionId);
		logRequest.setFilter(filter);
		UiBaseResponse resp = makeRequest(logRequest, LogFileResponse.class);
		assertFalse(resp instanceof ErrorResponse);
		assertTrue(resp instanceof LogFileResponse);

		return (LogFileResponse) resp;
	}

	public void testGettingTransactionIDs() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);

		// First Read in all transactions
		ConnectionUtils cu = new ConnectionUtils();
		Map<String, Integer> list = cu.extractTransactionIDInFolder(new File("/var/opt/cs/c4u/log"));

		// Now seach for each transaction id in turn and fail if one does not work
		for (String tid : list.keySet())
		{
			LogFilterOptions filter = new LogFilterOptions();
			filter.setText(tid);
			LogFileResponse lfr = makeLogRequest(filter, sessionId);
			String search = String.format("Transaction ID: %s count: %d found: %d", tid, list.get(tid), lfr.getLogRecords().size());
			assertTrue("ERROR " + search, list.get(tid) <= lfr.getLogRecords().size());
			System.out.println(search);
		}
	}

	@Test
	public void testLogFileViewing() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);

		// Available hosts
		CtrlConfigurationResponse config = getControlServiceConfiguration(SECURITY_USER, sessionId);
		String hosts = "";
		for (ServerInfo serverInfo : config.getServerList())
		{
			if (hosts.length() > 0)
			{
				hosts += ",";
			}
			hosts += serverInfo.getPeerHost();
		}

		if (hosts.length() == 0)
		{
			InetAddress ip = InetAddress.getLocalHost();
			hosts = ip.getHostName();
		}

		// Log Request
		LogFileRequest logRequest = new LogFileRequest(SECURITY_USER, sessionId);

		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DAY_OF_WEEK, -2);
		Date startDate = cal.getTime();
		LogFilterOptions filter = new LogFilterOptions();
		filter.setStartDate(startDate);
		filter.setEndDate(null);
		filter.setLoggingLevels(new LoggingLevels[] { LoggingLevels.TRACE, LoggingLevels.DEBUG, LoggingLevels.WARN, LoggingLevels.FATAL, LoggingLevels.ERROR });

		UiBaseResponse resp = makeRequest(logRequest, LogFileResponse.class);

		assertFalse(resp instanceof ErrorResponse);
		assertTrue(resp instanceof LogFileResponse);

		LogFileResponse logResponse = (LogFileResponse) resp;
		assertTrue(logResponse.getLogRecords().size() > 0);
		assertTrue(logResponse.getLastPosition() >= 0);

		// Now create some logs and see if they are received in the tail
		String SEARCH_TEXT = "Simple Tail Test";
		logger.info(SEARCH_TEXT);
		Thread.sleep(2000);

		LogFileRequest logRequest2 = new LogFileRequest(SECURITY_USER, sessionId);
		logRequest2.setReadPosition(logResponse.getLastPosition());
		logRequest2.getFilter().setText(SEARCH_TEXT);

		UiBaseResponse resp2 = makeRequest(logRequest2, LogFileResponse.class);
		assertFalse(resp2 instanceof ErrorResponse);
		assertTrue(resp2 instanceof LogFileResponse);

		logResponse = (LogFileResponse) resp2;
		//assertTrue(logResponse.getLogRecords().size() == 1);
		//assertTrue(logResponse.getLogRecords().get(0).text.equals(SEARCH_TEXT));
	}
}

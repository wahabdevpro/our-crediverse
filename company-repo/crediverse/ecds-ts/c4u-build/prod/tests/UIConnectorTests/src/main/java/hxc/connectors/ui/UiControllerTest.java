package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.air.AirConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.hsx.HsxConnector;
import hxc.connectors.hux.HuxConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.common.ConfigNotification;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.CallConfigurableMethodRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurableRequestParam;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurablesRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurationPathsRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.ValidateSessionRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.CallConfigurableMethodResponse;
import hxc.utils.protocol.uiconnector.response.ConfigurationUpdateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurablesResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurationPathsResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.response.ValidSessionResponse;
import hxc.utils.uiconnector.client.UIClient;

public class UiControllerTest extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	private static long TEST_UPDATE_NOTIFICATION_UID = 4738008229488107656L; // UID for Hux connector

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		esb = ServiceBus.getInstance();
		esb.registerConnector(new SoapConnector()); //HuxConnector depends on SoapConnector
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new HsxConnector());
		esb.registerConnector(new AirConnector());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());

		esb.registerService(new TransactionService());
		esb.registerService(new SecurityService());
		esb.registerService(new LoggerService());
		esb.registerConnector(new UiConnector());

		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private UIClient createTestClientConnection() throws Exception
	{
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", UI_CONNECTOR_PORT);
		}
		catch (IOException e1)
		{
			throw e1;
		}
		return uic;
	}

	private String loginAndRetrieveSessionId() throws Exception
	{
		// And Client (and send something)
		String sessionId = null;
		try (UIClient uic = createTestClientConnection())
		{
			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest("supplier");
			UiBaseResponse br = null;
			PublicKeyResponse pr = null;
			byte[] publicKey = null;
			try
			{
				br = uic.call(pkr, UiBaseResponse.class);

			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Call failed");
			}
			assertNotNull(br);
			if (br instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) br).getError());
			}

			pr = (PublicKeyResponse) br;
			publicKey = pr.getPublicKey();

			// Step 2: Authenticate
			AuthenticateRequest auth2 = new AuthenticateRequest(SECURITY_USER);
			auth2.generateSalted(publicKey, SECURITY_PASS);
			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth2, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				throw new Exception("Authentication 2 Call failed");
			}
			sessionId = authResp.getSessionId();
		}
		catch (Exception e)
		{
			fail("Exception thrown retrieving login ... " + e.getMessage());
			throw e;
		}
		return sessionId;
	}

	private Configurable getConfigurable(UIClient uic, long uid, String sessionId)
	{
		GetConfigurableRequest request = new GetConfigurableRequest(SECURITY_USER, sessionId);
		request.setConfigurableSerialVersionID(uid);
		UiBaseResponse configResponse = null;
		try
		{
			configResponse = uic.call(request, GetConfigurableResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			fail("GetAllConfigurablesRequest Call failed");
		}
		assertNotNull(configResponse);
		Configurable response = ((GetConfigurableResponse) configResponse).getConfig();
		// printConfig(response);

		return response;
	}

	private GetAllConfigurablesResponse extractAllConfig(UIClient uic, String sessionId) throws Exception
	{
		GetAllConfigurablesRequest getRequest = new GetAllConfigurablesRequest(SECURITY_USER, sessionId);
		UiBaseResponse configResponse = null;
		try
		{
			configResponse = uic.call(getRequest, GetAllConfigurablesResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw e;
		}
		assertNotNull(configResponse);
		if (configResponse instanceof ErrorResponse)
		{
			fail(((ErrorResponse) configResponse).getError());
		}
		assertFalse(configResponse instanceof ErrorResponse);
		assertTrue(configResponse instanceof GetAllConfigurablesResponse);

		GetAllConfigurablesResponse allConfig = (GetAllConfigurablesResponse) configResponse;
		return allConfig;
	}

	/**
	 * TESTS ---------------------------------------------------------------------------------
	 */
	// Test authentication
	@Test
	public void testAuthentication() throws Exception
	{
		// And Client (and send something)
		try (UIClient uic = createTestClientConnection())
		{
			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest("supplier");
			UiBaseResponse br = null;
			PublicKeyResponse pr = null;
			byte[] publicKey = null;
			try
			{
				br = uic.call(pkr, UiBaseResponse.class);

			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Call failed");
			}
			assertNotNull(br);
			if (br instanceof ErrorResponse)
			{
				fail(((ErrorResponse) br).getError());
			}

			pr = (PublicKeyResponse) br;
			publicKey = pr.getPublicKey();
			assertNotNull(publicKey);
			assertTrue(pr.getPublicKey().length > 0);

			// Step 2: Authenticate
			// First Fail at Login
			AuthenticateRequest auth1 = new AuthenticateRequest("Supplier");
			auth1.generateSalted(publicKey, "wrong");
			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth1, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Authentication 1 Call failed");
			}
			assertNotNull(authResp);
			assertNull(authResp.getSessionId());
			assertTrue(authResp instanceof ErrorResponse);

			// Now login correctly
			AuthenticateRequest auth2 = new AuthenticateRequest(SECURITY_USER);
			auth2.generateSalted(publicKey, SECURITY_PASS);
			try
			{
				authResp = uic.call(auth2, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Authentication 2 Call failed");
			}
			assertNotNull(authResp);
			assertNotNull(authResp.getSessionId());
			assertTrue(authResp instanceof AuthenticateResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	// @Test
	public void testExtractConfigurable() throws Exception
	{
		// Login (Quick and simple)
		String sessionId = loginAndRetrieveSessionId();

		try (UIClient uic = createTestClientConnection())
		{
			// Step 3: Extract configuration
			GetAllConfigurablesRequest getRequest = new GetAllConfigurablesRequest(SECURITY_USER, sessionId);
			UiBaseResponse configResponse = null;
			try
			{
				configResponse = uic.call(getRequest, GetAllConfigurablesResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("GetAllConfigurablesRequest Call failed");
			}
			assertNotNull(configResponse);
			if (configResponse instanceof ErrorResponse)
			{
				fail(((ErrorResponse) configResponse).getError());
			}
			assertFalse(configResponse instanceof ErrorResponse);
			assertTrue(configResponse instanceof GetAllConfigurablesResponse);
			GetAllConfigurablesResponse allconfig = (GetAllConfigurablesResponse) configResponse;

			// Step 4: Update Configuration
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(SECURITY_USER, sessionId);

			// Create update (with ERROR)
			configUpdateRequest.setPath("Technical Settings");
			configUpdateRequest.setName("Logger Service");
			configUpdateRequest.setParams(new IConfigurableParam[2]);
			configUpdateRequest.getParams()[0] = new ConfigurableRequestParam("RotationIntervalSeconds", "AAA"); // Will cause an error (Number expected)

			// Send update
			UiBaseResponse baseResponse = null;
			baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			assert (baseResponse instanceof ErrorResponse);

			// Now update with real value (no error)
			configUpdateRequest.setConfigurableSerialVersionUID(114958705L);
			String path = "";
			int version = extractLoggerVersion(allconfig, path);

			configUpdateRequest.setVersion(version);
			configUpdateRequest.getParams()[0] = new ConfigurableRequestParam("RotationIntervalSeconds", "123456");

			UiBaseResponse updateResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			assertFalse(updateResponse instanceof ErrorResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private int extractLoggerVersion(GetAllConfigurablesResponse allconfig, String path)
	{
		// Extract version and path
		int version = 0;
		for (Configurable config : allconfig.getConfigs())
		{
			if (config.getName().equals("Logger Service"))
			{
				version = config.getVersion();
				path = config.getPath();
			}
		}
		return version;
	}

	@Test
	public void testUpdateLogger() throws Exception
	{
		// Login (Quick and simple)
		String sessionId = loginAndRetrieveSessionId();
		try (UIClient uic = createTestClientConnection())
		{

			// Extract version and path
			GetAllConfigurablesResponse allconfig = extractAllConfig(uic, sessionId);
			String path = "";
			int version = extractLoggerVersion(allconfig, path);

			// Send update (all values)
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(SECURITY_USER, sessionId);
			configUpdateRequest.setName("Logger Service");
			configUpdateRequest.setPath(path);
			configUpdateRequest.setConfigurableSerialVersionUID(114958705L);
			configUpdateRequest.setVersion(version); // Important !!
			configUpdateRequest.setParams(new IConfigurableParam[9]);
			configUpdateRequest.getParams()[0] = new ConfigurableRequestParam("MaxQueueLength", "12345A"); // Will cause an error (Number expected)// not anymore
			configUpdateRequest.getParams()[1] = new ConfigurableRequestParam("LoggingLevel", "INFO");
			configUpdateRequest.getParams()[2] = new ConfigurableRequestParam("MaxMilliSecondsToBlock", "250");
			configUpdateRequest.getParams()[3] = new ConfigurableRequestParam("LineFormat", "%1$s TEST > |%2$s"); // Will cause an error
			configUpdateRequest.getParams()[4] = new ConfigurableRequestParam("RotationIntervalSeconds", "9999");
			configUpdateRequest.getParams()[5] = new ConfigurableRequestParam("RotatedNameFormat", "%1$s%2$s.test.log");
			configUpdateRequest.getParams()[6] = new ConfigurableRequestParam("DirectoryName", "./log_test");
			configUpdateRequest.getParams()[7] = new ConfigurableRequestParam("InterimFileName", "log_Test.tmp");
			configUpdateRequest.getParams()[8] = new ConfigurableRequestParam("TimeFormat", "yyyyMMdd");

			UiBaseResponse configUpateResponse = uic.call(configUpdateRequest, ConfigurationUpdateResponse.class);
			assertNotNull(configUpateResponse);
			//assertTrue(configUpateResponse instanceof ErrorResponse);
			// Now re-extract
			allconfig = extractAllConfig(uic, sessionId);
			assertNotNull(allconfig);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Test
	public void testMethodCall() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		// Call Rotate
		CallConfigurableMethodRequest methodRequest = new CallConfigurableMethodRequest(SECURITY_USER, sessionId);
		methodRequest.setConfigName("Logger Service");
		methodRequest.setConfigPath("Technical Settings Name");
		methodRequest.setMethod("Rotate");

		try (UIClient uic = createTestClientConnection();)
		{
			UiBaseResponse response = uic.call(methodRequest, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				fail("Error calling method: " + ((ErrorResponse) response).getError());
			}
			else
			{
				assertTrue(response instanceof CallConfigurableMethodResponse);
				CallConfigurableMethodResponse conMethodResp = (CallConfigurableMethodResponse) response;
				if (conMethodResp.getMethodCallResponse().indexOf("Rotated") < 0)
				{
					fail("Method call Failed: " + conMethodResp.getMethodCallResponse());
				}
			}
		}
		catch (Exception e)
		{
			throw e;
		}

	}

	@Test
	public void testGetAllConfigurationPaths() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		// GetAllConfigurationPathsResponse
		GetAllConfigurationPathsRequest request = new GetAllConfigurationPathsRequest(SECURITY_USER, sessionId);

		try (UIClient uic = createTestClientConnection();)
		{
			UiBaseResponse response = uic.call(request, UiBaseResponse.class);
			assertFalse(response instanceof ErrorResponse);
			assertTrue(response instanceof GetAllConfigurationPathsResponse);
			GetAllConfigurationPathsResponse conResponse = ((GetAllConfigurationPathsResponse) response);
			assertNotNull(conResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	// @Test
	public void testUpdateNotification() throws Exception
	{
		// Login (Quick and simple)
		String sessionId = loginAndRetrieveSessionId();
		try (UIClient uic = createTestClientConnection())
		{
			Configurable config = getConfigurable(uic, TEST_UPDATE_NOTIFICATION_UID, sessionId);

			// Send update (all values)
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(SECURITY_USER, sessionId);
			configUpdateRequest.setName(config.getName());
			configUpdateRequest.setPath(config.getPath());
			configUpdateRequest.setConfigurableSerialVersionUID(799512187);
			configUpdateRequest.setVersion(config.getVersion()); // Important !!

			// Create some notifications to update
			NotificationTextHelper textHelp = new NotificationTextHelper();
			textHelp.addText(0, "0 index");
			textHelp.addText(1, "Some message");
			textHelp.addText(2, "More Message");
			textHelp.addText(3, "3 index");

			ConfigNotification[] confNots = new ConfigNotification[2];
			confNots[0] = new ConfigNotification();
			confNots[0].setNotificationId(1);
			confNots[0].setText(textHelp.buildArray());
			confNots[1] = new ConfigNotification();
			confNots[1].setNotificationId(4);
			confNots[1].setText(textHelp.buildArray());
			configUpdateRequest.setNotifications(confNots);

			UiBaseResponse configUpateResponse = null;
			try
			{
				configUpateResponse = uic.call(configUpdateRequest, ConfigurationUpdateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				throw e;
			}
			assertNotNull(configUpateResponse);

			GetAllConfigurablesResponse allconfig = extractAllConfig(uic, sessionId);
			assertNotNull(allconfig);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Test
	public void testSessionValidation() throws Exception
	{
		String sessionId = "123";

		try (UIClient uic = createTestClientConnection();)
		{
			// First lets fail!
			ValidateSessionRequest validateRequest = new ValidateSessionRequest(SECURITY_USER, sessionId);
			UiBaseResponse response = uic.call(validateRequest, UiBaseResponse.class);
			assertTrue(response instanceof ErrorResponse);

			// Login
			sessionId = loginAndRetrieveSessionId();

			// Validate Session

			validateRequest = new ValidateSessionRequest(SECURITY_USER, sessionId);
			response = uic.call(validateRequest, UiBaseResponse.class);
			assertTrue(response instanceof ValidSessionResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public class NotificationTextHelper
	{
		int id;
		Map<Integer, String> textMap = new HashMap<Integer, String>();

		public void addText(int index, String text)
		{
			textMap.put(index, text);
		}

		public String[] buildArray()
		{
			int highest = 0;
			for (int i : textMap.keySet())
			{
				if (i > highest)
				{
					highest = i;
				}
			}
			String[] result = new String[highest + 1];
			for (int i = 0; i <= highest; i++)
			{
				String test = textMap.get(i);
				result[i] = (test == null) ? "" : test;
			}
			return result;
		}
	}
}

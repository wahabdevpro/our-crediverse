package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

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
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.GetLocaleInformationRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.SystemInfoRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.SystemInfoResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

/**
 * @author johne
 * 
 */
public class UiControllerConfigurationTest extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

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

		esb.registerService(new TransactionService());
		esb.registerService(new SecurityService());
		esb.registerService(new LoggerService());
		esb.registerConnector(new UiConnector());
		esb.registerConnector(new CtrlConnector());

		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

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

	// @Test
	public void testExtractSingleConfigurable() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		try (UIClient uic = createTestClientConnection())
		{
			GetConfigurableRequest request = new GetConfigurableRequest(SECURITY_USER, sessionId);

			// TEST 1: SerialVersionID Doesn't Exist
			request.setConfigurableSerialVersionID(-1L);

			UiBaseResponse response = null;
			try
			{
				response = uic.call(request, GetConfigurableResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("GetConfigurablesRequest Call failed");
			}
			assertNotNull(response);
			assertTrue(response instanceof ErrorResponse);
			assertFalse(response instanceof GetConfigurableResponse);

			// TEST 2: SerialVersionID Does Exist
			request = new GetConfigurableRequest(SECURITY_USER, sessionId);
			request.setConfigurableSerialVersionID(783078316L);

			try
			{
				response = uic.call(request, GetConfigurableResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("GetConfigurablesRequest Call failed");
			}
			assertNotNull(response);
			if (response instanceof ErrorResponse)
			{
				fail(((ErrorResponse) response).getError());
			}
			assertFalse(response instanceof ErrorResponse);
			assertTrue(response instanceof GetConfigurableResponse);

			Configurable config = ((GetConfigurableResponse) response).getConfig();

			assertNotNull(config);
			assertNull(config.getConfigurable());
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Test
	public void testLocaleGathering() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		try (UIClient uic = createTestClientConnection())
		{
			GetLocaleInformationRequest request = new GetLocaleInformationRequest(SECURITY_USER, sessionId);

			UiBaseResponse response = null;
			try
			{
				response = uic.call(request, GetLocaleInformationResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("GetLocaleInformationRequest Call failed");
			}
			if (response instanceof ErrorResponse)
			{
				fail(((ErrorResponse) response).getError());
			}
			assertFalse(response instanceof ErrorResponse);
			assertTrue(response instanceof GetLocaleInformationResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Test
	public void testVersion() throws Exception
	{
		try (UIClient uic = createTestClientConnection())
		{
			SystemInfoRequest vreq = new SystemInfoRequest();
			UiBaseResponse vresp = uic.call(vreq, SystemInfoResponse.class);
			assertFalse(vresp instanceof ErrorResponse);
			assertTrue(vresp instanceof SystemInfoResponse);
			String version = ((SystemInfoResponse) vresp).getVersion();
			assert (version != null);
		}
	}

}

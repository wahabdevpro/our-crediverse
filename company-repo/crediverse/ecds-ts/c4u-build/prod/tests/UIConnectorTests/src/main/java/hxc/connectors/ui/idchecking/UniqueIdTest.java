package hxc.connectors.ui.idchecking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import hxc.connectors.ui.UiConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurablesRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurablesResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

public class UniqueIdTest extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";
	private static int UI_CONNECTOR_PORT = 10101;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		esb = ServiceBus.getInstance();

		esb.registerService(new LoggerService());
		esb.registerConnector(new SoapConnector()); //HuxConnector depends on SoapConnector
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new HsxConnector());
		esb.registerConnector(new AirConnector());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());

		esb.registerService(new TransactionService());
		esb.registerService(new SecurityService());
		esb.registerConnector(new UiConnector());
		esb.registerConnector(new UiConnector());

		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
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

	@Test
	public void testUniqueId() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		try (UIClient uic = createTestClientConnection())
		{
			Map<Long, String> configSerials = new HashMap<>();
			GetAllConfigurablesResponse allconfig = extractAllConfig(uic, sessionId);
			List<String> problems = new ArrayList<String>();
			for (Configurable config : allconfig.getConfigs())
			{
				long serialID = config.getConfigSerialVersionUID();
				if (configSerials.containsKey(serialID))
					problems.add(String.format("SerialID %d found to clash between Configurations %s and %s%n", serialID, configSerials.get(serialID), config.getName()));
				configSerials.put(serialID, config.getName());
			}
			assertTrue(String.format("Duplicate ID registered: %s%n", problems.toString()), (problems.toString().length() > 0));
		}

	}
}

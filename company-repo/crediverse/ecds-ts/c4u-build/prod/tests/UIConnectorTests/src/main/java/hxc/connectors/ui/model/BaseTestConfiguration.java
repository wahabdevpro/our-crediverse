package hxc.connectors.ui.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;

import hxc.configuration.IConfiguration;
import hxc.connectors.air.AirConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.file.FileConnector;
import hxc.connectors.hsx.HsxConnector;
import hxc.connectors.hux.HuxConnector;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.connectors.ui.UiConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.advancedtransfer.AdvancedTransfer;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.pin.PinService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.reflection.IPropertyInfo;
import hxc.utils.reflection.ReflectionHelper;
import hxc.utils.uiconnector.client.UIClient;

public class BaseTestConfiguration extends RunAllTestsBase
{
	private IServiceBus esb;
	private int UI_CONNECTOR_PORT = 10101;

	public BaseTestConfiguration()
	{
		setupTestSystem();
	}

	private void setupTestSystem()
	{
		esb = ServiceBus.getInstance();

		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new HsxConnector());
		esb.registerConnector(new AirConnector());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new LifecycleConnector());
		esb.registerConnector(new SoapConnector());

		esb.registerService(new LoggerService());
		esb.registerService(new TransactionService());
		esb.registerService(new SecurityService());
		esb.registerService(new PinService());
		esb.registerService(new NumberPlanService());
		esb.registerService(new AdvancedTransfer());
		esb.registerService(new ReportingService());

		esb.registerConnector(new UiConnector());
		CtrlConnector ctrl = new CtrlConnector();
		ctrl.setMaxNodes(100);
		esb.registerConnector(ctrl);

		esb.start(null);

	}

	public void tearDown()
	{
		esb.stop();
		esb = null;
	}

	public UIClient createTestClientConnection() throws Exception
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

	public <T> T makeRequest(UiBaseRequest request, Class<T> responseClass) throws Exception
	{
		T resp = null;
		try (UIClient uic = this.createTestClientConnection())
		{
			resp = uic.call(request, responseClass);
		}
		return resp;
	}

	public String loginAndRetrieveSessionId(String userName, String password) throws Exception
	{
		// And Client (and send something)
		String sessionId = null;
		try (UIClient uic = createTestClientConnection())
		{
			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest(userName);
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
			AuthenticateRequest auth2 = new AuthenticateRequest(userName);
			auth2.generateSalted(publicKey, password);
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

	public IServiceBus getEsb()
	{
		return esb;
	}

	public void updatePortProperty(IConfiguration config, String propertyName, String newValue) throws Exception
	{

		IPropertyInfo[] pis = config.getProperties();

		for (int i = 0; i < pis.length; i++)
		{
			if (pis[i].getName().equalsIgnoreCase(propertyName))
			{
				Method setterMethod = pis[i].getSetterMethod();
				Class<?> parms[] = setterMethod.getParameterTypes();
				Object value = ReflectionHelper.valueOf(parms[0], newValue);
				setterMethod.invoke(config, value);
				break;
			}
		}
	}
}

package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.file.FileConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.reflection.ReflectionHelper;
import hxc.utils.uiconnector.client.UIClient;

public class ExtractFileConfigTest extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	private static long FILE_CONNECTOR_ID = -2276295713134351271L; // UID for File connector

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.registerConnector(new CtrlConnector());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new UiConnector());

		esb.registerService(new SecurityService());
		esb.registerService(new LoggerService());
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

	@Test
	public void testExtractSingleConfigurable() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId();

		try (UIClient uic = createTestClientConnection())
		{
			GetConfigurableRequest request = new GetConfigurableRequest(SECURITY_USER, sessionId);
			request.setConfigurableSerialVersionID(FILE_CONNECTOR_ID);

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
			assertFalse(response instanceof ErrorResponse);
			assertTrue(response instanceof GetConfigurableResponse);

			Configurable config = ((GetConfigurableResponse) response).getConfig();
			printConfig(config);

			// Update config (Create a new Object and embed)
			ConfigurationUpdateRequest configUpdateRequest = updateConfig(config, sessionId);
			UiBaseResponse updateResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			assertFalse(updateResponse instanceof ErrorResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

	private ConfigurationUpdateRequest updateConfig(Configurable config, String sessionId)
	{
		ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(SECURITY_USER, sessionId);
		configUpdateRequest.setPath(config.getPath());
		configUpdateRequest.setName(config.getName());
		configUpdateRequest.setConfigurableSerialVersionUID(FILE_CONNECTOR_ID);

		// Step 1: Get info to build Object
		ConfigurableResponseParam conParm = null;
		configUpdateRequest.setParams(new IConfigurableParam[config.getParams().length]);

		int index = -1;
		for (int i = 0; i < config.getParams().length; i++)
		{
			IConfigurableParam param = config.getParams()[i];
			configUpdateRequest.getParams()[i] = config.getParams()[i];
			conParm = (ConfigurableResponseParam) param;
			if (conParm.getStructure() != null)
			{
				index = i;
			}
		}

		// Add Data
		Map<String, String> map = createTemplateMap();
		IConfigurableParam[] template = new IConfigurableParam[map.keySet().size()];
		for (int i = 0; i < conParm.getStructure().length; i++)
		{
			ConfigurableResponseParam p = conParm.getStructure()[i];
			String field = p.getFieldName();
			template[i] = new BasicConfigurableParm(field);

			try
			{
				Class<?> clsType = GuiUtils.stringToClass(p.getValueType());

				if (clsType == null)
				{
					System.err.println("DAMN??: " + p.getValueType());
				}
				if (clsType.getName().equals("java.util.Date"))
					template[i].setValue(defaultDateFormat.parse(map.get(field)));
				else if (clsType.getName().equals("java.lang.String"))
					template[i].setValue(map.get(field));
				else
					template[i].setValue(ReflectionHelper.valueOf(clsType, map.get(field)));
			}
			catch (Exception e)
			{
				template[i].setValue(map.get(field));
			}
		}

		List<IConfigurableParam[]> list = (List<IConfigurableParam[]>) conParm.getValue();
		list.add(template);
		configUpdateRequest.getParams()[index].setValue(template);

		return configUpdateRequest;
	}

	private Map<String, String> createTemplateMap()
	{
		Map<String, String> map = new HashMap<>();
		map.put("Sequence", "1");
		map.put("ServerRole", "Some Other Role");
		map.put("CopyCommand", "some Other command");
		map.put("FilenameFilter", "[a-zA-Z0-9]+");
		map.put("StartTimeOfDay", "20140210T203015");
		map.put("EndTimeOfDay", "20140210T203107");
		map.put("FileProcessorType", "CSV");
		map.put("InputDirectory", "./");
		map.put("OutputDirectory", "./");
		map.put("StrictlySequential", "true");
		map.put("FileType", "DedicatedAccountsFileV3");

		return map;
	}

	//

	private void printConfig(Configurable config)
	{
		System.out.println("PATH: " + config.getPath() + " ||| Name: " + config.getName());
		System.out.println("SerialVersionUID: " + config.getConfigSerialVersionUID());

		System.out.println("Properties:");
		for (IConfigurableParam param : config.getParams())
		{
			ConfigurableResponseParam conParm = (ConfigurableResponseParam) param;

			StringBuilder sb = new StringBuilder();

			if (conParm.getStructure() != null)
			{
				System.out.println("Structure:");
				for (ConfigurableResponseParam p : conParm.getStructure())
				{
					sb.append(String.format("\tFIELD %s [%s]%n", p.getFieldName(), p.getValueType()));
				}

				sb.append(conParm.getFieldName()).append(" := ").append(conParm.getValue()).append("\n");
				if (conParm.getValue() instanceof List<?>)
				{
					List<IConfigurableParam[]> list = (List<IConfigurableParam[]>) conParm.getValue();
					int index = 0;
					for (IConfigurableParam[] cp : list)
					{
						sb.append(String.format("[%d] >>%n", index));
						for (IConfigurableParam p : cp)
						{
							sb.append(String.format("\t parm[%s] = %s%n", p.getFieldName(), p.getValue()));
						}
					}
				}

			}
			else
			{
				sb.append(String.format("%s %n", conParm.getValueType()));
				sb.append(conParm.getFieldName()).append(" := ").append(conParm.getValue());

				sb.append(conParm.isReadOnly() ? "[Readonly]" : "");
				sb.append(conParm.getMaxLength() >= 0 ? "[Max length:" + conParm.getMaxLength() + "]" : "");
				sb.append(conParm.getMinValue() != null ? "[Min value:" + conParm.getMinValue() + "]" : "");
				sb.append(conParm.getMaxValue() != null ? "[Max value:" + conParm.getMaxValue() + "]" : "");
				sb.append(conParm.getPossibleValues() != null ? "[Possible value:" + conParm.getPossibleValues() + "]" : "");
				sb.append(conParm.getRenderAs() != null ? "[Render As:" + conParm.getRenderAs() + "]" : "");
			}

			System.out.println(sb.toString());
		}
	}

}

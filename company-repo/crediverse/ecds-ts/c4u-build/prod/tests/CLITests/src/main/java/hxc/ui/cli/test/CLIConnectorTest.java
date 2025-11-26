package hxc.ui.cli.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.ui.UiConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.ui.cli.CLIClient;
import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.controller.CLIController;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.system.CLISystem;
import hxc.utils.protocol.uiconnector.common.Configurable;

public class CLIConnectorTest extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;
	private static CLIConnector connector;
	private static CLISystem system;
	private static CLIClient client;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// Create ESB
		esb = ServiceBus.getInstance();
		esb.registerService(new LoggerService());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new UiConnector());
		esb.registerService(new SecurityService());

		// Start the ESB
		System.out.println("ESB Started: " + esb.start(null));

		new CLIController("localhost", UI_CONNECTOR_PORT);
		connector = new CLIConnector();
		client = new CLIClient();
		CLIOutput.setOutputStream(System.out);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}

	public void login()
	{
		String username = "supplier";
		String password = " $$4u";
		assertTrue(connector.connectUIClient("localhost", UI_CONNECTOR_PORT));
		byte[] publicKey = null;
		publicKey = connector.retrievePublicKey(username);
		assertNotNull(publicKey);
		String session = connector.checkUserDetails(username, password, publicKey);
		assertNotNull(session);
		client.setUsername(username);
		client.setSessionID(session);
	}

	public void loadConfiguration()
	{
		List<Configurable> configurations = connector.getAllConfigurables(client.getUsername(), client.getSessionID());
		assertNotNull(configurations);

		system = new CLISystem(configurations, connector);
		system.setClient(client);
	}

	@Test
	public void testReadonly()
	{
		login();
		loadConfiguration();

		assertTrue(system.updateCurrentPath("technical settings"));
		assertTrue(system.updateCurrentPath("mysql database"));
		assertTrue(system.updateCurrentPath("connectionstring =  changed"));
	}

}

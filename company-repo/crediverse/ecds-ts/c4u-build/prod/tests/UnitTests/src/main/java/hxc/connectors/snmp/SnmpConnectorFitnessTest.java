package hxc.connectors.snmp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;

import hxc.connectors.IConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.diagnostic.DiagnosticConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.snmp.agent.ISnmpAgent;
import hxc.connectors.snmp.agent.SnmpAgent;
import hxc.connectors.snmp.components.SnmpStatusException;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;

public class SnmpConnectorFitnessTest extends RunAllTestsBase
{

	private static ISnmpAgent agent;
	private static IServiceBus esb;
	private static IConnector snmp;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
		agent = null;
	}

	// Test to send a simple trap and then check whether agent is still fit
	@Test
	public void testFitnessWithAgent()
	{
		// Create the SnmpAgent
		agent = new SnmpAgent(SnmpAgent.DEFAULT_ADDRESS);
		// Adjust the agent to send a simple trap.
		agent.setResponder(new CommandResponder()
		{

			@Override
			public void processPdu(CommandResponderEvent event)
			{
				// Check that a trap was sent
				assertNotNull(event);
			}
		});
		agent.setTargetAddress(0, "127.0.0.1/" + SnmpAgent.PORT);
		// Start the agent
		try
		{
			agent.start();
		}
		catch (Exception e)
		{
			fail("Failed to start the agent.");
		}
		// Check fitness
		assertNotNull(agent);
		assertTrue(agent.isWorking());
		try
		{
			agent.incident(TrapCodes.csJobFailed, "Example", IncidentSeverity.CLEAR, "Example Description");
		}
		catch (SnmpStatusException e)
		{
			fail("Failed to send the trap.");
		}
		// Check fitness
		assertNotNull(agent);
		assertTrue(agent.isWorking());
		agent.stop();

		// Setup for failure with weird address
		agent = new SnmpAgent("255.0.1.1/" + SnmpAgent.PORT);
		agent.setTargetAddress(0, "127.0.0.1/" + SnmpAgent.PORT);
		// Start the agent which will fail because of the address
		try
		{
			agent.start();
		}
		catch (Exception e)
		{
			assertNotNull(e);
		}
		// Check that it is unfit
		assertNotNull(agent);
		assertFalse(agent.isWorking());
		agent.stop();
	}

	@Test
	public void testFitnessWithSnmpConnector()
	{
		// Start the esb
		esb.stop();

		esb.registerService(new LoggerService());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new SnmpConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerConnector(new DiagnosticConnector());

		assertTrue(esb.start(null));

		// Get snmp
		snmp = esb.getFirstConnector(SnmpConnector.class);
		assertNotNull(snmp);

		// Check for fitness
		assertTrue(snmp.isFit());
	}

}

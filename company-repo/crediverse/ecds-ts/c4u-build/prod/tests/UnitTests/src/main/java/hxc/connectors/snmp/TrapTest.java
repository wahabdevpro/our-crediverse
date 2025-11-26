package hxc.connectors.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;

import hxc.connectors.snmp.agent.ISnmpAgent;
import hxc.connectors.snmp.agent.SnmpAgent;
import hxc.connectors.snmp.components.SnmpStatusException;

public class TrapTest
{

	private static ISnmpAgent agent;
	private static boolean mustStop = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testIncidentTrap()
	{

		agent = new SnmpAgent("0.0.0.0/" + SnmpAgent.PORT);

		// Set the necessary target details
		agent.setCommunity("public");
		agent.setTimeout(1000);
		agent.setRetries(5);
		// Set the Command Responder
		agent.setResponder(new CommandResponder()
		{

			@Override
			public void processPdu(CommandResponderEvent event)
			{
				// Check that it is not null
				assertNotNull(event);
				// Get the PDU
				PDU pdu = event.getPDU();
				// Ensure that the sysuptime oid is the first binding as it is required for v2 traps
				assertEquals(pdu.get(0).getOid(), SnmpConstants.sysUpTime);
				try
				{
					// Check that the trap is csJobFailed
					assertEquals(pdu.get(1).getVariable().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csJobFailed.toString()).getOID());
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csJobFailed OID");
				}
				try
				{
					// Check the Incident Element
					assertEquals(pdu.get(2).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentElement.toString()).getOID());
					assertEquals(pdu.get(2).getVariable().toString(), "jobFailedTest");
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentElement OID");
				}
				try
				{
					// Check the Incident Severity
					assertEquals(pdu.get(5).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentSeverity.toString()).getOID());
					assertEquals(pdu.get(5).getVariable().toString(), IncidentSeverity.CRITICAL.toString().toLowerCase());
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentSeverity OID");
				}
				try
				{
					// Check the Incident Description
					assertEquals(pdu.get(6).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentDescription.toString()).getOID());
					assertEquals(pdu.get(6).getVariable().toString(), "Testing the trap.");
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentDescription OID");
				}
				mustStop = true;
				event.setProcessed(true);
			}
		});
		agent.setTargetAddress(0, "127.0.0.1/" + SnmpAgent.PORT);
		try
		{
			agent.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Failed to start the agent.");
		}
		try
		{
			agent.incident(TrapCodes.csJobFailed, "jobFailedTest", IncidentSeverity.CRITICAL, "Testing the trap.");
		}
		catch (SnmpStatusException e)
		{
			fail("Failed to send the trap.");
		}

		while (!mustStop)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
		mustStop = false;
		agent.stop();
	}

	@Test
	public void testIndicationTrap()
	{

		agent = new SnmpAgent("0.0.0.0/36200");

		// Set the necessary target details
		agent.setCommunity("public");
		agent.setTimeout(1000);
		agent.setRetries(5);
		// Set the Command Responder
		agent.setResponder(new CommandResponder()
		{

			@Override
			public void processPdu(CommandResponderEvent event)
			{
				// Check that it is not null
				assertNotNull(event);
				// Get the PDU
				PDU pdu = event.getPDU();
				// Ensure that the sysuptime oid is the first binding as it is required for v2 traps
				assertEquals(pdu.get(0).getOid(), SnmpConstants.sysUpTime);
				try
				{
					// Check that the trap is csJobFailed
					assertEquals(pdu.get(1).getVariable().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csThresholdStatus.toString()).getOID());
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csJobFailed OID");
				}
				try
				{
					// Check the Indication State
					assertEquals(pdu.get(3).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIndicationState.toString()).getOID());
					assertEquals(pdu.get(3).getVariable().toString(), IndicationState.WITHIN_LIMITS.ordinal() + "");
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIndicationState OID");
				}
				try
				{
					// Check the Incident Element
					assertEquals(pdu.get(4).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentElement.toString()).getOID());
					assertEquals(pdu.get(4).getVariable().toString(), "thresholdStatusTest");
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentElement OID");
				}
				try
				{
					// Check the Incident Severity
					assertEquals(pdu.get(7).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentSeverity.toString()).getOID());
					assertEquals(pdu.get(7).getVariable().toString(), IncidentSeverity.MAJOR.toString().toLowerCase());
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentSeverity OID");
				}
				try
				{
					// Check the Incident Description
					assertEquals(pdu.get(8).getOid().toString(), agent.getDatabase().getRecordWithName(TrapCodes.csIncidentDescription.toString()).getOID());
					assertEquals(pdu.get(8).getVariable().toString(), "Testing indication trap.");
				}
				catch (SnmpStatusException e)
				{
					fail("Failed to get the csIncidentDescription OID");
				}
				mustStop = true;
				event.setProcessed(true);
			}
		});
		agent.setTargetAddress(0, "127.0.0.1/36200");
		try
		{
			agent.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Failed to start the agent.");
		}
		try
		{
			agent.indication(TrapCodes.csThresholdStatus, "thresholdStatusTest", IndicationState.WITHIN_LIMITS, IncidentSeverity.MAJOR, "Testing indication trap.");
		}
		catch (SnmpStatusException e)
		{
			fail("Failed to send the trap.");
		}
		while (!mustStop)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
		agent.stop();
	}

}

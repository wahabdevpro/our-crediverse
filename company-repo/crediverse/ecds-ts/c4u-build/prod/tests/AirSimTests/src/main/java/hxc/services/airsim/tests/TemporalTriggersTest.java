package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.lifecycle.ILifecycle;
import hxc.connectors.lifecycle.ITemporalTrigger;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.sut.C4UTestConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TemporalTrigger;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.reporting.ReportingService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.calendar.DateTime;

public class TemporalTriggersTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(TemporalTriggersTest.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static IAirSim airSimulator = null;
	private static AirConnector air;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new LoggerService());
		esb.registerConnector(new CtrlConnector());

		air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		NumberPlanService numberPlan = new NumberPlanService();
		esb.registerService(numberPlan);

		esb.registerConnector(air);
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");

		esb.registerConnector(new LifecycleConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerService(new ReportingService());
		esb.registerConnector(new C4UTestConnector());
		esb.registerService(new TransactionService());

		boolean started = esb.start(null);
		assert (started);
		airSimulator.start();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tear Down
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		airSimulator.stop();
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testTemporalTriggers() throws Exception
	{

		// Create
		airSimulator.reset();
		String msisdn = "07245566612";
		String id = "tmp";
		airSimulator.addSubscriber(msisdn, 1, 12, 5000, SubscriberState.active);
		ILifecycle lifecycle = esb.getFirstConnector(ILifecycle.class);
		assertNotNull(lifecycle);
		IDatabase database = esb.getFirstConnector(IDatabase.class);

		try (IDatabaseConnection db = database.getConnection(null))
		{
			ITemporalTrigger trg = getTrigger(id, id, msisdn, "", DateTime.getNow().addDays(5), false, 1, "TST");
			boolean ok = lifecycle.addTemporalTrigger(db, trg);
			assert (ok);

			TemporalTrigger[] triggers = airSimulator.getTemporalTriggers(id, id, msisdn, null);
			assert (triggers.length == 1);
			TemporalTrigger trigger = triggers[0];
			assertEquals(trigger.getServiceID(), id);
			assertEquals(trigger.getVariantID(), id);
			assertEquals(trigger.getMsisdnA(), msisdn);

			Date christmas = new DateTime(2014, 12, 25);
			trigger.setBeingProcessed(true);
			trigger.setNextDateTime(christmas);
			airSimulator.updateTemporalTrigger(trigger);

			triggers = airSimulator.getTemporalTriggers(id, id, msisdn, null);
			assert (triggers.length == 1);
			trigger = triggers[0];
			assertEquals(trigger.getServiceID(), id);
			assertEquals(trigger.getVariantID(), id);
			assertEquals(trigger.getMsisdnA(), msisdn);
			assertEquals(christmas, trigger.getNextDateTime());
			assertTrue(trigger.isBeingProcessed());

			airSimulator.deleteTemporalTrigger(trigger);
			triggers = airSimulator.getTemporalTriggers(id, id, msisdn, null);
			assert (triggers.length == 0);

		}
		catch (SQLException ex)
		{
			throw ex;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private hxc.connectors.lifecycle.ITemporalTrigger getTrigger(final String serviceID, final String variantID, final String msisdnA, final String msisdnB, final Date nextDateTime, final boolean beingProcessed, final int state, final String keyValue)
	{
		return new hxc.connectors.lifecycle.ITemporalTrigger()
		{

			@Override
			public String getServiceID()
			{
				return serviceID;
			}

			@Override
			public String getVariantID()
			{
				return variantID;
			}

			@Override
			public String getMsisdnA()
			{
				return msisdnA;
			}

			@Override
			public String getMsisdnB()
			{
				return msisdnB;
			}

			@Override
			public Date getNextDateTime()
			{
				return nextDateTime;
			}

			@Override
			public boolean isBeingProcessed()
			{
				return beingProcessed;
			}

			@Override
			public int getState()
			{
				return state;
			}

			@Override
			public void setServiceID(String serviceID)
			{
			}

			@Override
			public void setVariantID(String variantID)
			{
			}

			@Override
			public void setMsisdnA(String msisdnA)
			{
			}

			@Override
			public void setMsisdnB(String msisdnB)
			{
			}

			@Override
			public String getKeyValue()
			{
				return keyValue;
			}

			@Override
			public void setKeyValue(String key)
			{

			}

			@Override
			public void setNextDateTime(Date nextDateTime)
			{
			}

			@Override
			public void setBeingProcessed(boolean beingProcessed)
			{
			}

			@Override
			public void setState(int state)
			{
			}

			@Override
			public Date getDateTime1()
			{
				return null;
			}

			@Override
			public void setDateTime1(Date dateTime1)
			{
			}

			@Override
			public Date getDateTime2()
			{
				return null;
			}

			@Override
			public void setDateTime2(Date dateTime2)
			{
			}

			@Override
			public Date getDateTime3()
			{
				return null;
			}

			@Override
			public void setDateTime3(Date dateTime3)
			{
			}

			@Override
			public Date getDateTime4()
			{
				return null;
			}

			@Override
			public void setDateTime4(Date dateTime4)
			{

			}

		};
	}

}

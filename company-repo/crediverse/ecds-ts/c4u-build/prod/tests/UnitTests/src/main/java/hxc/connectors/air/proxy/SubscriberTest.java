package hxc.connectors.air.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.Offer;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ITransactionService;
import hxc.services.transactions.Transaction;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.ucip.OfferInformation;

public class SubscriberTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(SubscriberTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private String A_NUMBER = "08244526545";
	private String B_NUMBER = "08244526546";
	private static IAirSim airSimulator = null;

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
		configureLogging(esb);
		esb.registerService(new TransactionService());
		esb.registerService(new NumberPlanService());
		AirConnector air = new AirConnector();

		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new LifecycleConnector());
		boolean started = esb.start(null);
		assert (started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		if (airSimulator != null)
			airSimulator.stop();
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Rollback
	//
	// /////////////////////////////////
	@Test
	public void testRollback()
	{
		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");
		assertTrue(airSimulator.start());
		SubscriberEx subscriberA = (SubscriberEx) airSimulator.addSubscriber(A_NUMBER, 1, 76, 1400, SubscriberState.active);
		Offer offer = new Offer();
		offer.setOfferID(12);
		offer.setExpiryDate(new DateTime(2014, 12, 25));
		offer.setOfferProviderID(B_NUMBER);
		offer.setOfferType(4);
		offer.setPamServiceID(112);
		offer.setProductID(113);
		airSimulator.updateOffer(A_NUMBER, offer);

		IAirConnector airConn = esb.getFirstConnector(IAirConnector.class);
		// IAirConnection air = airCon.getConnection(null);
		// UpdateOfferRequest request;
		// air.updateOffer(request);

		// Create a CDR
		CdrBase cdr = new CsvCdr();

		// Transaction Reversal Scope
		ITransactionService transactions = esb.getFirstService(ITransactionService.class);
		try (Transaction<?> transaction = transactions.create(cdr, null))
		{
			Subscriber subscriber = new Subscriber(A_NUMBER, airConn, transaction);
			assertTrue(subscriberA.hasOffer(12));
			subscriber.deleteSharedOffer(12);
			assertFalse(subscriberA.hasOffer(12));
		}
		catch (IOException | AirException e)
		{
			fail(e.getMessage());
		}

		assertTrue(subscriberA.hasOffer(12));
		OfferInformation[] offers = subscriberA.getOfferInformation(subscriberA);
		assertEquals(1, offers.length);
		OfferInformation offer2 = offers[0];

		assertEquals(offer.getOfferID(), offer2.offerID);
		assertEquals(offer.getOfferProviderID(), offer2.offerProviderID);
		assertEquals(offer.getOfferType(), offer2.offerType);
		assertEquals(offer.getPamServiceID(), offer2.pamServiceID);
		assertEquals(offer.getProductID(), offer2.productID);
		assertEquals(offer.getExpiryDate(), offer2.expiryDate);

	}

}

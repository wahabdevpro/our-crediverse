package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import hxc.connectors.air.IAirConnection;
import hxc.connectors.air.IRequestHeader;
import hxc.connectors.air.IResponseHeader;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.ucip.GetAccountDetailsRequest;
import hxc.utils.protocol.ucip.GetAccountDetailsRequestMember;
import hxc.utils.protocol.ucip.GetAccountDetailsResponse;
import hxc.utils.protocol.ucip.GetAccountDetailsResponseMember;
import hxc.utils.protocol.ucip.GetOffersRequest;
import hxc.utils.protocol.ucip.GetOffersRequestMember;
import hxc.utils.protocol.ucip.GetOffersResponse;
import hxc.utils.protocol.ucip.GetOffersResponseMember;
import hxc.utils.protocol.ucip.OfferInformation;
import hxc.utils.protocol.ucip.OfferInformationList;
import hxc.utils.protocol.ucip.ServiceOfferings;
import hxc.utils.protocol.ucip.UpdateOfferRequest;
import hxc.utils.protocol.ucip.UpdateOfferRequestMember;
import hxc.utils.protocol.ucip.UpdateOfferResponse;
import hxc.utils.protocol.ucip.UpdateOfferResponseMember;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationRequest;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationRequestMember;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationResponse;
import hxc.utils.protocol.ucip.UpdateSubscriberSegmentationResponseMember;

public class PsoTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(PsoTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static IAirSim airSimulator = null;
	private static AirConnector air;

	private static final int LANGUAGE_ID = 1;
	private static final String MSISDN_A = "0824452655";

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
	public void testPSO() throws AirException
	{
		airSimulator.reset();
		airSimulator.addSubscribers("07245566612", 2, 1, 12, 5000, SubscriberState.active);

		DateTime dt1 = new DateTime(2014, 9, 11, 15, 16, 17);
		DateTime dt2 = new DateTime(2014, 9, 11, 15, 16, 27);
		boolean isClose = airSimulator.isCloseTo(dt1, dt2, 2);
		assertFalse(isClose);
		isClose = airSimulator.isCloseTo(dt2, dt1, 2);
		assertFalse(isClose);
		isClose = airSimulator.isCloseTo(dt1, dt2, 10);
		assertTrue(isClose);
		isClose = airSimulator.isCloseTo(dt2, dt1, 10);
		assertTrue(isClose);

		IAirConnection airConn = air.getConnection(null);
		Date now = DateTime.getToday();

		airSimulator.addSubscriber(MSISDN_A, LANGUAGE_ID, 1, 1000, SubscriberState.active);

		{
			UpdateSubscriberSegmentationRequest request = new UpdateSubscriberSegmentationRequest();
			UpdateSubscriberSegmentationRequestMember member = request.member;
			initialize(member);
			ServiceOfferings pso = new ServiceOfferings();
			pso.serviceOfferingActiveFlag = true;
			pso.serviceOfferingID = 3;
			member.setServiceOfferings(new ServiceOfferings[] { pso });
			UpdateSubscriberSegmentationResponse response = airConn.updateSubscriberSegmentation(request);
			validate(request.member, response.member);
			UpdateSubscriberSegmentationResponseMember result = response.member;
			assertNotNull(result.serviceOfferingsResult);
			assertEquals(1, result.serviceOfferingsResult.length);
			assertEquals(3, result.serviceOfferingsResult[0].serviceOfferingID);
		}

		{
			UpdateOfferRequest request = new UpdateOfferRequest();
			UpdateOfferRequestMember member = request.member;
			initialize(member);
			member.setOfferType(3);
			member.setOfferID(12);
			member.setExpiryDate(now);
			UpdateOfferResponse response = airConn.updateOffer(request);
			UpdateOfferResponseMember result = response.member;
			validate(request.member, result);
		}

		{
			GetOffersRequest request = new GetOffersRequest();
			GetOffersRequestMember member = request.member;
			initialize(member);
			GetOffersResponse response = airConn.getOffers(request);
			GetOffersResponseMember result = response.member;
			validate(request.member, result);
			assertNotNull(result.offerInformation);
			assertEquals(1, result.offerInformation.length);
			OfferInformation offer = result.offerInformation[0];
			assertEquals(12, offer.offerID);
			assertEquals(3, (int) offer.offerType);
			assertEquals(now.getTime(), offer.expiryDate.getTime());
			assertNull(offer.startDate);
		}

		{
			GetAccountDetailsRequest request = new GetAccountDetailsRequest();
			GetAccountDetailsRequestMember member = request.member;
			initialize(member);
			GetAccountDetailsResponse response = airConn.getAccountDetails(request);
			GetAccountDetailsResponseMember result = response.member;
			validate(request.member, result);

			assertNotNull(result.serviceOfferings);
			assertEquals(1, result.serviceOfferings.length);
			assertEquals(3, result.serviceOfferings[0].serviceOfferingID);

			assertNotNull(result.offerInformationList);
			assertEquals(1, result.offerInformationList.length);
			OfferInformationList offer = result.offerInformationList[0];
			assertEquals(12, offer.offerID);
			assertEquals(3, (int) offer.offerType);
			assertEquals(now.getTime(), offer.expiryDate.getTime());
			assertNull(offer.startDate);

		}

	}

	@Test
	public void testGetNextMSISDN() throws AirException, InterruptedException
	{
		BlockingQueue<Runnable> requestQueue = new ArrayBlockingQueue<Runnable>(100000);
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1000, 1, TimeUnit.HOURS, requestQueue);

		long now = System.currentTimeMillis();

		for (int index = 0; index < 100000; index++)
		{
			Runnable command = new Runnable()
			{
				@Override
				public void run()
				{
					airSimulator.getNextMSISDN("00824452655");
				}
			};

			threadPool.execute(command);
		}

		while (threadPool.getActiveCount() > 0)
			Thread.sleep(10);

		now = System.currentTimeMillis() - now;

		System.out.print(now);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private void initialize(IRequestHeader request)
	{
		request.setOriginHostName("AndriesHP");
		request.setOriginNodeType("HxC");
		request.setOriginTimeStamp(new Date());
		request.setOriginTransactionID("123");
		request.setSubscriberNumber(MSISDN_A);
		request.setSubscriberNumberNAI(2);
	}

	private void validate(IRequestHeader request, IResponseHeader response)
	{
		assertEquals(0, response.getResponseCode());

	}

}

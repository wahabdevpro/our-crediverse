package hxc.services.subscription;

import static org.junit.Assert.*;
import hxc.configuration.ValidationException;
import hxc.connectors.Channels;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.datawarehouse.DataWarehouseConnector;
import hxc.connectors.lifecycle.ISubscription;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.subscription.SubscriptionService.SubscriptionConfig;
import hxc.services.transactions.TransactionService;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

public class SubscriptionServiceTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;

	private String GOLD = "Au";
	private String SILVER = "Ag";

	private String NUMBER = "0824452655";
	private String SERVICE_ID = "SUBS";
	private String SERVICE_NAME_EN = "Subscription";

	private static IAirSim airSimulator = null;
	private int languageID = 1;

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
		logger = new LoggerService();
		esb.registerService(logger);
		esb.registerService(new TransactionService());
		esb.registerService(new SubscriptionService());
		esb.registerService(new NumberPlanService());
		AirConnector air = new AirConnector();

		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		esb.registerConnector(air);

		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new SoapConnector());

		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new LifecycleConnector());
		esb.registerService(new ReportingService());
		esb.registerConnector(new DataWarehouseConnector());

		boolean started = esb.start(null);
		assert (started);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Subscription Service
	//
	// /////////////////////////////////
	@Test
	public void testSubscriptionService() throws ValidationException, IOException, SQLException, InterruptedException
	{
		// Get the VAS Soap Interface
		ISoapConnector soapConnector = esb.getFirstConnector(ISoapConnector.class);
		assertNotNull("SOAP Connector is NULL!! Connector not starting !", soapConnector);
		IHxC vasConnector = soapConnector.getVasInterface();

		// Setup Simulator
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		airSimulator = new AirSim(esb, 10011, "/Air", logger, numberPlan, "CFR");
		assertTrue(airSimulator.start());

		// Setup Config
		SubscriptionService service = esb.getFirstService(SubscriptionService.class);
		SubscriptionConfig config = (SubscriptionConfig) service.getConfiguration();
		Variant[] variants = config.getVariants();
		Variant gold = variants[0];
		assertEquals(GOLD, gold.getVariantID());

		// Get All Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(false);
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(2, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(GOLD, info[0].getVariantID());
			assertEquals(SERVICE_NAME_EN, info[0].getServiceName());
			assertEquals(SubscriptionState.unknown, info[0].getState());
			assertEquals(SERVICE_ID, info[1].getServiceID());
			assertEquals(SILVER, info[1].getVariantID());
			assertEquals(SERVICE_NAME_EN, info[1].getServiceName());
			assertEquals(SubscriptionState.unknown, info[1].getState());
		}

		// Get Active Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(0, info.length);
		}

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(0, response.getServiceInfo().length);

		}

		// Subscribe
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(GOLD);
			subscribeRequest.setSubscriberNumber(new Number(NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			validate(subscribeRequest, subscribeResponse);
		}

		// Attempt to Subscribe Twice
		{
			SubscribeRequest subscribeRequest = new SubscribeRequest();
			initialize(subscribeRequest);
			subscribeRequest.setServiceID(SERVICE_ID);
			subscribeRequest.setVariantID(GOLD);
			subscribeRequest.setSubscriberNumber(new Number(NUMBER));
			SubscribeResponse subscribeResponse = vasConnector.subscribe(subscribeRequest);
			assertEquals(ReturnCodes.alreadySubscribed, subscribeResponse.getReturnCode());
		}

		// Get Active Services
		{
			GetServicesRequest request = new GetServicesRequest();
			initialize(request);
			request.setActiveOnly(true);
			request.setSubscriberNumber(new Number(NUMBER));
			GetServicesResponse response = vasConnector.getServices(request);
			validate(request, response);
			VasServiceInfo[] info = response.getServiceInfo();
			assertEquals(1, info.length);
			assertEquals(SERVICE_ID, info[0].getServiceID());
			assertEquals(GOLD, info[0].getVariantID());
			assertEquals(SERVICE_NAME_EN, info[0].getServiceName());
			assertEquals(SubscriptionState.active, info[0].getState());
		}

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setSubscriberNumber(new Number(NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(GOLD, response.getServiceInfo()[0].getVariantID());
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}

		// Get Service
		{
			GetServiceRequest request = new GetServiceRequest();
			initialize(request);
			request.setServiceID(SERVICE_ID);
			request.setVariantID(GOLD);
			request.setSubscriberNumber(new Number(NUMBER));
			request.setActiveOnly(true);
			GetServiceResponse response = vasConnector.getService(request);
			validate(request, response);
			assertEquals(SubscriptionState.active, response.getServiceInfo()[0].getState());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private ProcessLifecycleEventRequest createRequest(ISubscription subscription)
	{
		ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(subscription);
		initialize(request);

		return request;
	}

	private void initialize(RequestHeader request)
	{
		request.setCallerID(NUMBER);
		request.setChannel(Channels.INTERNAL);
		request.setHostName("local");
		request.setTransactionID("00012");
		request.setSessionID("001");
		request.setVersion("1");
		request.setMode(RequestModes.normal);
		request.setLanguageID(languageID);
	}

	private void validate(RequestHeader request, ResponseHeader response)
	{
		assertEquals(ReturnCodes.success, response.getReturnCode());
		assertEquals(request.getTransactionID(), response.getTransactionId());
		assertEquals(request.getSessionID(), response.getSessionId());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void teardown()
	{
		if (airSimulator != null)
			airSimulator.stop();
		esb.stop();
	}

}

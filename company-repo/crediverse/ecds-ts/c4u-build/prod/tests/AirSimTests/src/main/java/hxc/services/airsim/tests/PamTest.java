package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataRequestMember;
import hxc.utils.protocol.acip.AddPeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataRequestMember;
import hxc.utils.protocol.acip.DeletePeriodicAccountManagementDataResponse;
import hxc.utils.protocol.acip.PamInformation;
import hxc.utils.protocol.acip.PamInformationList;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementRequest;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementRequestMember;
import hxc.utils.protocol.acip.RunPeriodicAccountManagementResponse;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataRequest;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataRequestMember;
import hxc.utils.protocol.acip.UpdatePeriodicAccountManagementDataResponse;
import hxc.utils.protocol.ucip.GetAccountDetailsRequest;
import hxc.utils.protocol.ucip.GetAccountDetailsRequestMember;
import hxc.utils.protocol.ucip.GetAccountDetailsResponse;

public class PamTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(PamTest.class);

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
	private static final String MSISDN_B = "0824452656";

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
	public void testAddPeriodicAccountManagementData()
	{
		airSimulator.reset();
		airSimulator.addSubscribers("07245566612", 2, 1, 12, 5000, SubscriberState.active);

		IAirConnection airConn = air.getConnection(null);

		airSimulator.addSubscriber(MSISDN_A, LANGUAGE_ID, 1, 1000, SubscriberState.active);

		airSimulator.cloneSubscriber(MSISDN_A, MSISDN_B);

		try
		{
			PamInformationList pam;

			// Add
			{
				AddPeriodicAccountManagementDataRequest request = new AddPeriodicAccountManagementDataRequest();
				AddPeriodicAccountManagementDataRequestMember member = request.member = new AddPeriodicAccountManagementDataRequestMember();
				initialize(member);

				pam = new PamInformationList();
				pam.pamServiceID = 12;
				pam.pamClassID = 2;
				pam.scheduleID = 3;
				pam.currentPamPeriod = "Feb";
				pam.deferredToDate = new DateTime(2015, 2, 21, 12, 0, 0);
				pam.pamServicePriority = 5;

				member.setPamInformationList(new PamInformationList[] { pam });

				AddPeriodicAccountManagementDataResponse response = airConn.addPeriodicAccountManagementData(request);
				validate(request.member, response.member);

				PamInformationList[] pamList = response.member.pamInformationList;
				assertNotNull(pamList);
				assertEquals(1, pamList.length);
				assertEquals(pam.pamServiceID, pamList[0].pamServiceID);
				assertEquals(pam.deferredToDate, pamList[0].deferredToDate);
			}

			// GAD
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(true);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(1, pamList.length);
				assertEquals(pam.pamServiceID, pamList[0].pamServiceID);
				assertEquals(pam.deferredToDate, pamList[0].deferredToDate);
			}

			// Update
			{
				UpdatePeriodicAccountManagementDataRequest request = new UpdatePeriodicAccountManagementDataRequest();
				request.member = new UpdatePeriodicAccountManagementDataRequestMember();
				initialize(request.member);

				hxc.utils.protocol.acip.PamUpdateInformationList apam = new hxc.utils.protocol.acip.PamUpdateInformationList();
				apam.pamServiceID = pam.pamServiceID;
				apam.pamClassIDNew = pam.pamClassID + 1;
				apam.scheduleIDNew = pam.scheduleID + 1;
				apam.currentPamPeriod = "Dec";
				apam.deferredToDate = pam.deferredToDate;
				apam.pamServicePriorityNew = pam.pamServicePriority + 1;

				request.member.pamUpdateInformationList = new hxc.utils.protocol.acip.PamUpdateInformationList[] { apam };
				UpdatePeriodicAccountManagementDataResponse response = airConn.updatePeriodicAccountManagementData(request);
				validate(request.member, response.member);
				hxc.utils.protocol.acip.PamInformationList[] pamList = response.member.pamInformationList;
				assertNotNull(pamList);
				assertEquals(1, pamList.length);
			}

			// GAD
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(true);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(1, pamList.length);
				assertEquals(pam.pamServiceID, pamList[0].pamServiceID);

				hxc.utils.protocol.ucip.PamInformationList apam = pamList[0];
				assertEquals(apam.pamServiceID, pam.pamServiceID);
				assertEquals(apam.pamClassID, pam.pamClassID + 1);
				assertEquals(apam.scheduleID, pam.scheduleID + 1);
				assertEquals(apam.currentPamPeriod, "Dec");
				assertEquals(apam.deferredToDate.getTime(), pam.deferredToDate.getTime());
				assertEquals((int) apam.pamServicePriority, pam.pamServicePriority + 1);

			}

			// Execute
			{
				RunPeriodicAccountManagementRequest request = new RunPeriodicAccountManagementRequest();
				request.member = new RunPeriodicAccountManagementRequestMember();
				initialize(request.member);

				hxc.utils.protocol.acip.PamInformationList apam = new hxc.utils.protocol.acip.PamInformationList();
				apam.pamServiceID = pam.pamServiceID;
				request.member.pamServiceID = pam.pamServiceID;
				RunPeriodicAccountManagementResponse response = airConn.runPeriodicAccountManagement(request);
				validate(request.member, response.member);
				PamInformation pam2 = response.member.pamInformation;
				assertNotNull(pam2);
			}

			// Delete
			{
				DeletePeriodicAccountManagementDataRequest request = new DeletePeriodicAccountManagementDataRequest();
				request.member = new DeletePeriodicAccountManagementDataRequestMember();
				initialize(request.member);

				hxc.utils.protocol.acip.PamInformationList apam = new hxc.utils.protocol.acip.PamInformationList();
				apam.pamServiceID = pam.pamServiceID;
				request.member.pamInformationList = new hxc.utils.protocol.acip.PamInformationList[] { apam };
				DeletePeriodicAccountManagementDataResponse response = airConn.deletePeriodicAccountManagementData(request);
				validate(request.member, response.member);
				hxc.utils.protocol.acip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(0, pamList.length);
			}

			// GAD
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(true);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(0, pamList.length);
			}

			// Add
			{
				boolean result = airSimulator.updatePAM(MSISDN_A, 12, 2, 3, "Feb", new DateTime(2015, 2, 21, 12, 0, 0), null, 5);
				assertTrue(result);
			}

			// GAD
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(true);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(1, pamList.length);
				assertEquals(pam.pamServiceID, pamList[0].pamServiceID);
				assertEquals(pam.deferredToDate, pamList[0].deferredToDate);
			}

			// GAD w/o requestPamInformationFlag
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(false);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNull(pamList);
			}

			// Delete
			{
				boolean result = airSimulator.deletePAM(MSISDN_A, 12);
				assertTrue(result);
			}

			// GAD
			{
				GetAccountDetailsRequest request = new GetAccountDetailsRequest();
				request.member = new GetAccountDetailsRequestMember();
				initialize(request.member);
				request.member.setRequestPamInformationFlag(true);
				GetAccountDetailsResponse response = airConn.getAccountDetails(request);
				validate(request.member, response.member);
				hxc.utils.protocol.ucip.PamInformationList[] pamList = response.member.getPamInformationList();
				assertNotNull(pamList);
				assertEquals(0, pamList.length);
			}

			logger.info("OK");

		}
		catch (AirException e)
		{
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}

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

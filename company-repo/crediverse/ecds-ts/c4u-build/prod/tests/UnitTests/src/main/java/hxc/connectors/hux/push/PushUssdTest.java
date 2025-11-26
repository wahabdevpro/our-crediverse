package hxc.connectors.hux.push;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.hux.HuxConnector;
import hxc.connectors.hux.HuxConnector.HuxConfiguration;
import hxc.connectors.soap.SoapConnector;
import hxc.connectors.ussd.IPushUSSD;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.hux.SendUSSDRequest;
import hxc.utils.protocol.hux.SendUSSDResponse;
import hxc.utils.protocol.hux.SendUSSDResponseMembers;
import hxc.utils.xmlrpc.XmlRpcRequest;
import hxc.utils.xmlrpc.XmlRpcServer;

public class PushUssdTest extends RunAllTestsBase
{

	private static IServiceBus esb;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// ////////////////////////////////
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);

		HuxConnector huxConnector = new HuxConnector();

		HuxConfiguration config = (HuxConfiguration) huxConnector.getConfiguration();
		config.setHuxServerAddress("http://localhost:4010/Test");

		esb.registerConnector(huxConnector);
		esb.registerConnector(new SoapConnector());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());

		esb.registerService(new SecurityService());

		boolean started = esb.start(null);
		assert (started);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// testPushUSSD
	//
	// /////////////////////////////////
	@Test
	public void testPushUSSD() throws Exception
	{
		// Start a Server
		XmlRpcServer server = new XmlRpcServer(SendUSSDRequest.class)
		{
			@Override
			protected void uponXmlRpcRequest(XmlRpcRequest request)
			{
				SendUSSDRequest pushRequest = request.getMethodCall();

				SendUSSDResponse response = new SendUSSDResponse();
				response.members = new SendUSSDResponseMembers();
				response.members.TransactionId = pushRequest.members.TransactionId;
				response.members.TransactionTime = new Date();
				response.members.MSISDN = pushRequest.members.MSISDN;
				response.members.USSDServiceCode = pushRequest.members.USSDServiceCode;
				response.members.USSDResponseString = String.format("%s,%s,%s,%s,%s,%s,%s", //
						pushRequest.members.TransactionId, //
						pushRequest.members.MSISDN, //
						pushRequest.members.USSDServiceCode, //
						pushRequest.members.USSDRequestString, //
						pushRequest.members.encodingSelection[0].language, //
						pushRequest.members.encodingSelection[0].alphabet, //
						pushRequest.members.action //
						);
				response.members.ResponseCode = 0;

				try
				{
					request.respond(response);
				}
				catch (IOException e)
				{
					assert (false);
				}
			}
		};
		server.start(4010, "/Test");

		IPushUSSD push = esb.getFirstConnector(IPushUSSD.class);
		assertNotNull(push);

		String response = push.pushUSSD("1234", "0824452655", "321", "eng", "Testing", false);

		assertEquals("1234,0824452655,321,Testing,eng,latn,request", response);

		server.stop();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tear Down
	//
	// ////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}

}

package hxc.utils.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;

public class XmlRpcClientServerTest
{
	@BeforeClass
	public static void setup()
	{
		// Make sure that the service bus (invariably using port 1400 is down)
		IServiceBus esb = ServiceBus.getInstance();
		if (esb != null)
		{
			esb.stop();
		}
	}

	@AfterClass
	public static void teardown()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup / tear-down
	//
	// /////////////////////////////////

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testXmlRpcClientServer() throws IOException, XmlRpcException
	{
		// Test Request
		TestRequest testRequest = TestRequest.Create();
		// Create a server
		XmlRpcServer server = new XmlRpcServer(TestRequest.class)
		{
			@Override
			protected void uponXmlRpcRequest(XmlRpcRequest request)
			{
				// Test if request is correct
				TestRequest testRequest = request.getMethodCall();
				testRequest.assertSame(testRequest);

				// Reply
				TestResponse testResponse = new TestResponse();
				testResponse.members = new TestResponseMembers();
				testResponse.members.resultCode = 999;
				try
				{
					request.respond(testResponse);
				}
				catch (IOException e)
				{
					fail(e.getMessage());
				}

			}

		};
		server.start(14000, "/RPC2");
		// Create Client
		XmlRpcClient client = new XmlRpcClient("http://127.0.0.1:14000/RPC2"); // Added the http://

		// Test
		for (int index = 0; index < 5; index++)
		{
			XmlRpcConnection connection = client.getConnection();
			TestResponse testResponse = connection.call(testRequest, TestResponse.class);
			assertEquals(999, testResponse.members.resultCode);
		}

		server.stop();
	}

}

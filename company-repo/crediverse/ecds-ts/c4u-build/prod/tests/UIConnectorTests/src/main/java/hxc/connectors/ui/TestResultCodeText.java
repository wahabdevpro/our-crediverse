package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsRequest;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class TestResultCodeText
{

	private static BaseTestConfiguration btestConfig;
	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		btestConfig = new BaseTestConfiguration();
	}

	@Test
	public void testExtract() throws Exception
	{
		try
		{
			String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);

			ReturnCodeTextDefaultsRequest metRequest = new ReturnCodeTextDefaultsRequest(SECURITY_USER, sessionId);
			UiBaseResponse resp = btestConfig.makeRequest(metRequest, ReturnCodeTextDefaultsResponse.class);

			assertFalse(resp instanceof ErrorResponse);
			assertTrue(resp instanceof ReturnCodeTextDefaultsResponse);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

	}

}

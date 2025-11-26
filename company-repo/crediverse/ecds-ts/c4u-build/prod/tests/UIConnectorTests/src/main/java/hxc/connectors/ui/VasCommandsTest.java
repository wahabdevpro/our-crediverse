package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.vas.VasCommandsRequest;
import hxc.utils.protocol.uiconnector.vas.VasCommandsResponse;
import hxc.utils.uiconnector.client.UIClient;

public class VasCommandsTest
{

	private static BaseTestConfiguration btestConfig;
	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";
	private static final long AUTO_CREDIT_SHARING_ID = -1103158008836733663L;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		btestConfig = new BaseTestConfiguration();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		btestConfig.tearDown();
		btestConfig = null;
	}

	private <T> T makeRequest(UiBaseRequest request, Class<T> responseClass) throws Exception
	{
		T resp = null;
		try (UIClient uic = btestConfig.createTestClientConnection())
		{
			resp = uic.call(request, responseClass);
		}
		return resp;
	}

	@Test
	public void testVasdCommandExtraction() throws Exception
	{
		String sessionId = null;
		try
		{
			sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		}
		catch (Exception e)
		{
			System.err.println("Error retrieving session ID in 'testVasdCommandExtraction'");
			throw e;
		}
		VasCommandsRequest request = new VasCommandsRequest(SECURITY_USER, sessionId);
		request.setConfigurationUID(AUTO_CREDIT_SHARING_ID);

		UiBaseResponse resp = null;
		try
		{
			resp = makeRequest(request, VasCommandsResponse.class);
		}
		catch (Exception e)
		{
			System.err.println("Error making request in 'testVasdCommandExtraction'");
			e.printStackTrace();
		}
		if (resp == null)
		{
			throw new Exception("Response to VasCommandsRequest NULL");
		}
		assertFalse(resp instanceof ErrorResponse);
		assertTrue(resp instanceof VasCommandsResponse);
		assertTrue("Assumption of VasCommands amount failed (assumed at least 5)", ((VasCommandsResponse) resp).getCommandVariables().length >= 5);

	}

}

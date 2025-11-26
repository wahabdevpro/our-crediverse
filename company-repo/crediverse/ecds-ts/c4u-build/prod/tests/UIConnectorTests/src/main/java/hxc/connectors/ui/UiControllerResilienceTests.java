package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerInfoUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.FitnessRequest;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

public class UiControllerResilienceTests
{
	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	private static BaseTestConfiguration btestConfig;

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

	@Test
	public void testUpdateControlConfigTest() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);

		// Valid Configuration
		ServerInfo[] serverInfo = generateServerInto(5);
		ServerRole[] serverRoles = generateServerRole(5);

		try (UIClient uic = btestConfig.createTestClientConnection())
		{
			CtrlConfigurationRequest creq = new CtrlConfigurationRequest(SECURITY_USER, sessionId);
			UiBaseResponse resp = uic.call(creq, CtrlConfigurationResponse.class);
			assertFalse(String.format("resp supposed NOT to be ErrorResponse, is %s", resp), resp instanceof ErrorResponse);
			int version = ((CtrlConfigurationResponse) resp).getVersionNumber();

			CtrlConfigServerInfoUpdateRequest infoUpdate = new CtrlConfigServerInfoUpdateRequest(SECURITY_USER, sessionId);
			infoUpdate.setServerInfoList(serverInfo);
			infoUpdate.setPersistToDatabase(false);
			infoUpdate.setVersionNumber(version);
			UiBaseResponse iresp = uic.call(infoUpdate, UiBaseResponse.class);
			assertFalse(String.format("resp supposed NOT to be ErrorResponse, is %s", iresp), iresp instanceof ErrorResponse);
			assertTrue(String.format("resp supposed to be ConfirmationResponse, is %s", iresp), iresp instanceof ConfirmationResponse);

			CtrlConfigServerRoleUpdateRequest roleUpdate = new CtrlConfigServerRoleUpdateRequest(SECURITY_USER, sessionId);
			roleUpdate.setPersistToDatabase(true);
			roleUpdate.setVersionNumber(version);
			roleUpdate.setServerRoleList(serverRoles);
			UiBaseResponse rresp = uic.call(roleUpdate, UiBaseResponse.class);
			assertFalse(String.format("resp supposed NOT to be ErrorResponse, is %s", rresp), rresp instanceof ErrorResponse);
			assertTrue(String.format("resp supposed to be ConfirmationResponse, is %s", rresp), rresp instanceof ConfirmationResponse);

			// Update will fail due to version
			CtrlConfigServerRoleUpdateRequest roleUpdate2 = new CtrlConfigServerRoleUpdateRequest(SECURITY_USER, sessionId);
			roleUpdate2.setPersistToDatabase(true);
			roleUpdate2.setServerRoleList(serverRoles);
			UiBaseResponse rresp2 = uic.call(roleUpdate, UiBaseResponse.class);
			assertTrue(String.format("resp supposed NOT to be ErrorResponse, is %s", rresp2), rresp2 instanceof ErrorResponse);
			assertFalse(String.format("resp supposed to be ConfirmationResponse, is %s", rresp2), rresp2 instanceof ConfirmationResponse);

			// // Update
			// CtrlConfigurationUpdateRequest updateRequest = new CtrlConfigurationUpdateRequest(SECURITY_USER, sessionId);
			// updateRequest.setServerInfoList(serverInfo);
			// updateRequest.setServerRoleList(serverRoles);
			// UiBaseResponse cresp = uic.call(updateRequest, UiBaseResponse.class);
			// assertFalse(cresp instanceof ErrorResponse);
			// assertTrue(cresp instanceof ConfirmationResponse);

			// Validate
			CtrlConfigurationRequest creq2 = new CtrlConfigurationRequest(SECURITY_USER, sessionId);
			UiBaseResponse resp2 = uic.call(creq2, CtrlConfigurationResponse.class);
			assertFalse(resp2 instanceof ErrorResponse);
			assert (resp2 instanceof CtrlConfigurationResponse);
			CtrlConfigurationResponse cc = (CtrlConfigurationResponse) resp2;

			assert (cc.getServerList().length == serverInfo.length);
			for (int i = 0; i < cc.getServerList().length; i++)
			{
				assert (cc.getServerList()[i].getServerHost().equals(serverInfo[i].getServerHost()));
				assert (cc.getServerList()[i].getPeerHost().equals(serverInfo[i].getPeerHost()));
			}

			assert (cc.getServerRoleList().length == serverRoles.length);
			for (int i = 0; i < cc.getServerRoleList().length; i++)
			{
				assert (cc.getServerRoleList()[i].getServerRoleName().equals(serverRoles[i].getServerRoleName()));
				assert (cc.getServerRoleList()[i].isExclusive() == (serverRoles[i].isExclusive()));
				assert (cc.getServerRoleList()[i].getOwner().equals(serverRoles[i].getOwner()));
				assert (cc.getServerRoleList()[i].getAttachCommand().equals(serverRoles[i].getAttachCommand()));
				assert (cc.getServerRoleList()[i].getDetachCommand().equals(serverRoles[i].getDetachCommand()));
			}
		}
	}

	// @Test
	public void testDuplicatesNotAllowed() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);

		// Valid Configuration
		ServerInfo[] serverInfo = generateServerInto(6);
		ServerRole[] serverRoles = generateServerRole(6);

		try (UIClient uic = btestConfig.createTestClientConnection())
		{
			// Open path?
			serverInfo[5].setServerHost("serverhostend");
			serverInfo[5].setPeerHost("peerhostend");
			UiBaseResponse resp1 = performCtlUpdateRequest(uic, serverInfo, serverRoles, SECURITY_USER, sessionId);
			assertTrue(resp1 instanceof ErrorResponse);

			// Repeated path?
			serverInfo[5].setServerHost(serverInfo[0].getServerHost());
			serverInfo[5].setPeerHost(serverInfo[0].getPeerHost());
			UiBaseResponse resp2 = performCtlUpdateRequest(uic, serverInfo, serverRoles, SECURITY_USER, sessionId);
			assertTrue(resp2 instanceof ErrorResponse);

			// Make things right
			serverInfo = generateServerInto(6);
			CtrlConfigServerInfoUpdateRequest infoUpdate = new CtrlConfigServerInfoUpdateRequest(SECURITY_USER, sessionId);
			infoUpdate.setServerInfoList(serverInfo);
			UiBaseResponse iresp = uic.call(infoUpdate, UiBaseResponse.class);
			assertFalse(iresp instanceof ErrorResponse);
			assertTrue(iresp instanceof ConfirmationResponse);

			// Repeating Role
			ServerInfo[] serverInfo2 = generateServerInto(10);
			ServerRole[] serverRoles2 = generateServerRole(10);
			serverRoles2[0] = serverRoles2[1];
			UiBaseResponse resp4 = performCtlUpdateRequest(uic, serverInfo2, serverRoles2, SECURITY_USER, sessionId);
			assertTrue(resp4 instanceof ErrorResponse);
		}
	}

	@Test
	public void testExtractControlConfigTest() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		try (UIClient uic = btestConfig.createTestClientConnection())
		{

			CtrlConfigurationRequest creq = new CtrlConfigurationRequest(SECURITY_USER, sessionId);
			UiBaseResponse cresp = uic.call(creq, CtrlConfigurationResponse.class);
			assertFalse(cresp instanceof ErrorResponse);
			assert (cresp instanceof CtrlConfigurationResponse);
		}
	}

	@Test
	public void testFitnessRequest() throws Exception
	{
		String sessionId = btestConfig.loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		try (UIClient uic = btestConfig.createTestClientConnection())
		{
			FitnessRequest freq = new FitnessRequest(SECURITY_USER, sessionId);
			UiBaseResponse fresp = uic.call(freq, FitnessResponse.class);
			assertFalse(fresp instanceof ErrorResponse);
			assertTrue(fresp instanceof FitnessResponse);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private ServerInfo[] generateServerInto(int hostCount)
	{
		String letters = "ABCDEFGHIJKLMNOP";
		ServerInfo[] serverInfo = new ServerInfo[hostCount];
		for (int i = 0; i < hostCount; i++)
		{
			if (i < (hostCount - 1))
			{
				serverInfo[i] = new ServerInfo("host" + letters.charAt(i), "host" + letters.charAt(i + 1), "0" + i);
			}
			else
			{
				serverInfo[i] = new ServerInfo("host" + letters.charAt(i), "host" + letters.charAt(0), "0" + i);
			}
		}
		return serverInfo;
	}

	private ServerRole[] generateServerRole(int roleCount)
	{
		ServerRole[] serverRoles = new ServerRole[roleCount];
		for (int i = 0; i < roleCount; i++)
		{
			serverRoles[i] = new ServerRole("role_" + i, false, "owner_" + i, "attach_" + i, "detach_" + i);
		}
		return serverRoles;
	}

	private UiBaseResponse performCtlUpdateRequest(UIClient uic, ServerInfo[] serverInfo, ServerRole[] serverRoles, String user, String sessionId) throws ClassNotFoundException, IOException
	{
		CtrlConfigurationUpdateRequest request = new CtrlConfigurationUpdateRequest(user, sessionId);
		request.setServerInfoList(serverInfo);
		request.setServerRoleList(serverRoles);
		UiBaseResponse response = uic.call(request, UiBaseResponse.class);
		return response;
	}

}

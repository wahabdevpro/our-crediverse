package hxc.connectors.ui.roles;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.ValidationException;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.file.FileConnector;
import hxc.connectors.ui.UiConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;
import hxc.utils.protocol.uiconnector.userman.request.DeleteUserRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateSecurityRoleRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserPasswordRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserRequest;
import hxc.utils.protocol.uiconnector.userman.response.DeleteUserResponse;
import hxc.utils.uiconnector.client.UIClient;

public class TestConfigurationRetrivalWithRoles extends RunAllTestsBase
{

	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;

	public static String TEST_USER = "testUser";
	public static String TEST_USER_PASSWORD = "testUser";

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	private static long LOGGER_ID = -2276295713134351271L; // UID for File connector

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.registerConnector(new CtrlConnector());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new UiConnector());

		esb.registerService(new SecurityService());
		esb.registerService(new LoggerService());
		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}

	private UIClient createTestClientConnection() throws Exception
	{
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", UI_CONNECTOR_PORT);
		}
		catch (IOException e1)
		{
			throw e1;
		}
		return uic;
	}

	private String loginAndRetrieveSessionId(String userName, String password) throws Exception
	{
		// And Client (and send something)
		String sessionId = null;
		try (UIClient uic = createTestClientConnection())
		{
			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest(userName);
			UiBaseResponse br = null;
			PublicKeyResponse pr = null;
			byte[] publicKey = null;
			try
			{
				br = uic.call(pkr, UiBaseResponse.class);

			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Call failed");
			}
			assertNotNull(br);
			if (br instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) br).getError());
			}

			pr = (PublicKeyResponse) br;
			publicKey = pr.getPublicKey();

			// Step 2: Authenticate
			AuthenticateRequest auth2 = new AuthenticateRequest(userName);
			auth2.generateSalted(publicKey, password);
			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth2, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				throw new Exception("Authentication 2 Call failed");
			}
			sessionId = authResp.getSessionId();
		}
		catch (Exception e)
		{
			fail("Exception thrown retrieving login ... " + e.getMessage());
			throw e;
		}
		return sessionId;
	}

	public void createUser(String superUserName, String sessionId, String userName, String password) throws Exception
	{
		try (UIClient uic = createTestClientConnection())
		{
			UserDetails userDetails = new UserDetails();
			userDetails.setMobileNumber("123456789");
			userDetails.setName(userName);
			userDetails.setUserId(userName);
			userDetails.setEnabled(true);
			userDetails.setNewUser(true);

			UpdateUserRequest updateRequest = new UpdateUserRequest(superUserName, sessionId);
			updateRequest.addSingleUserDetail(userDetails);

			UiBaseResponse baseResponse1 = uic.call(updateRequest, UiBaseResponse.class);
			if (baseResponse1 instanceof ErrorResponse)
			{
				System.err.println(((ErrorResponse) baseResponse1).getError());
			}
			// assertFalse(baseResponse1 instanceof ErrorResponse);
			// assertTrue(baseResponse1 instanceof ConfirmationResponse);

			// Update Password
			// Get the user password key
			PublicKeyRequest pkr = new PublicKeyRequest(userName);
			UiBaseResponse passResp = uic.call(pkr, UiBaseResponse.class);
			assertFalse(passResp instanceof ErrorResponse);

			// Use key to create password credentials
			PublicKeyResponse keyResponse = (PublicKeyResponse) passResp;
			UpdateUserPasswordRequest passUpdateRequest = new UpdateUserPasswordRequest(superUserName, sessionId);
			passUpdateRequest.setUserToUpdateId(userName);
			passUpdateRequest.generateSalted(keyResponse.getPublicKey(), password);

			UiBaseResponse passUpdateResp = uic.call(passUpdateRequest, UiBaseResponse.class);
			if (passUpdateResp instanceof ErrorResponse)
			{
				System.err.println(((ErrorResponse) passUpdateResp).getError());
			}
			assertFalse(passUpdateResp instanceof ErrorResponse);
		}
	}

	private void removeUser(String superUserName, String sessionId, String userToRemove) throws Exception
	{
		try (UIClient uic = createTestClientConnection())
		{
			DeleteUserRequest delRequest = new DeleteUserRequest(superUserName, sessionId);
			delRequest.setUsersToDeleteIds(new String[] { userToRemove });
			UiBaseResponse baseResponse3 = uic.call(delRequest, UiBaseResponse.class);
			assertFalse(baseResponse3 instanceof ErrorResponse);
			assertTrue(baseResponse3 instanceof DeleteUserResponse);
		}
	}

	public void createRole(String superUserName, String sessionId, SecurityRole role) throws Exception
	{
		try (UIClient uic = createTestClientConnection())
		{
			UpdateSecurityRoleRequest updateRequest = new UpdateSecurityRoleRequest(superUserName, sessionId);
			updateRequest.addSingleSecurityRole(role);
			UiBaseResponse baseResponse = uic.call(updateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw new ValidationException(err.getError());
			}
		}
	}

	public void extractConfiguration(String userName, String sessionId, long configId) throws Exception
	{
		try (UIClient uic = createTestClientConnection())
		{
			GetConfigurableRequest request = new GetConfigurableRequest(userName, sessionId);
			request.setConfigurableSerialVersionID(configId);

			UiBaseResponse response = null;
			try
			{
				response = uic.call(request, GetConfigurableResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("GetConfigurablesRequest Call failed");
			}
			assertNotNull(response);
			if (response instanceof ErrorResponse)
				System.err.println(((ErrorResponse) response).getError());
			assertFalse(response instanceof ErrorResponse);
			assertTrue(response instanceof GetConfigurableResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Test
	public void testGetLoggerServiceNoPermissions()
	{
		String sessionId = null;
		try
		{
			sessionId = loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		}
		catch (Exception e)
		{
			fail("Error: " + e.getMessage());
		}

		// Create New User
		try
		{
			createUser(SECURITY_USER, sessionId, TEST_USER, TEST_USER_PASSWORD);
		}
		catch (Exception e)
		{
			assertTrue(e instanceof SecurityException);
		}

		// Login as new User and view all permissions
		try
		{
			sessionId = loginAndRetrieveSessionId(TEST_USER, TEST_USER_PASSWORD);
		}
		catch (Exception e)
		{
			fail("Error: " + e.getMessage());
		}

		try
		{
			extractConfiguration(TEST_USER, sessionId, LOGGER_ID);
		}
		catch (Exception e)
		{
			assertTrue(e instanceof SecurityException);
		}

		try
		{
			sessionId = loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		}
		catch (Exception e)
		{
			fail("Error: " + e.getMessage());
		}

		try
		{
			removeUser(SECURITY_USER, sessionId, TEST_USER);
		}
		catch (Exception e)
		{
			assertTrue(e instanceof SecurityException);
		}
	}

}

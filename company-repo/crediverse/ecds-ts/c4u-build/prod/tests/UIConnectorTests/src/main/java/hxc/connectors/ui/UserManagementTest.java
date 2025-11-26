package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.IPermission;
import hxc.services.security.IRole;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;
import hxc.utils.protocol.uiconnector.userman.request.DeleteUserRequest;
import hxc.utils.protocol.uiconnector.userman.request.ReadUserDetailsRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateMyDetailsRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserPasswordRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserRequest;
import hxc.utils.protocol.uiconnector.userman.response.DeleteUserResponse;
import hxc.utils.protocol.uiconnector.userman.response.ReadUserDetailsResponse;
import hxc.utils.uiconnector.client.UIClient;

public class UserManagementTest extends RunAllTestsBase
{
	private static IServiceBus esb;
	private static int UI_CONNECTOR_PORT = 10101;

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	private static String TEST_USER = "TEST_USER";
	private static String TEST_PASSWORD = "test@Password";
	private static int USERS_TO_CREATE = 10;

	// Delete me:
	private static ISecurity security;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// Create ESB
		esb = ServiceBus.getInstance();
		esb.registerService(new LoggerService());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new UiConnector());
		esb.registerService(new SecurityService());
		esb.registerConnector(new CtrlConnector());

		esb.start(null);

		security = esb.getFirstService(ISecurity.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

	private IUser securityAuthenticate(String username, String password)
	{
		byte[] publicKey = security.getPublicKey(username);
		assertNotNull(publicKey);
		byte[] credentials = encrypt(password, publicKey);
		IUser user = security.authenticate(username, credentials);
		return user;
	}

	private IUser createTestUser()
	{
		// Use supplier details
		securityAuthenticate(SECURITY_USER, SECURITY_PASS);

		IUser testUser = security.createUser(TEST_USER);
		assertNotNull(testUser);
		byte[] publicKey = testUser.getPublicKey();
		testUser.setPassword(encrypt(TEST_PASSWORD, publicKey));
		assertTrue(testUser.update());

		return testUser;
	}

	private void removeTestUser()
	{
		IUser userUnit = security.getUser(TEST_USER);
		if (userUnit != null)
			userUnit.delete();
	}

	/**
	 * Utiltiy Methods ---------------------------------------------------------------------------------
	 */

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
		UIClient uic = createTestClientConnection();

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
		String sessionId = authResp.getSessionId();

		uic.close();
		return sessionId;
	}

	// --------------------------- TESTS ---------------------------------------------------------------

	@Test
	public void testRetrieveUsers() throws Exception
	{
		String sessionId = loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		try (UIClient uic = createTestClientConnection())
		{
			ReadUserDetailsRequest ur = new ReadUserDetailsRequest(SECURITY_USER, sessionId);

			UiBaseResponse baseResponse = uic.call(ur, UiBaseResponse.class);
			assertTrue(baseResponse instanceof ReadUserDetailsResponse);
			assertFalse(baseResponse instanceof ErrorResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public void testAddUsers() throws Exception
	{

		String sessionId = loginAndRetrieveSessionId(SECURITY_USER, SECURITY_PASS);
		try (UIClient uic = createTestClientConnection())
		{
			// Retrieve all users
			ReadUserDetailsRequest ur = new ReadUserDetailsRequest(SECURITY_USER, sessionId);
			UiBaseResponse baseResponse = uic.call(ur, UiBaseResponse.class);
			assertTrue(baseResponse instanceof ReadUserDetailsResponse);
			ReadUserDetailsResponse readDetails = (ReadUserDetailsResponse) baseResponse;
			int currentCount = readDetails.getUserDetails().length;

			// Create User + save
			List<UserDetails> userDetailsList = new ArrayList<UserDetails>();
			for (int i = 0; i < USERS_TO_CREATE; i++)
			{
				UserDetails userDetails = new UserDetails();
				userDetails.setMobileNumber("123456789");
				userDetails.setName("testName_" + i);
				userDetails.setUserId("test_" + i);
				userDetailsList.add(userDetails);
			}
			UpdateUserRequest updateRequest = new UpdateUserRequest(SECURITY_USER, sessionId);
			updateRequest.setUserDetails((UserDetails[]) userDetailsList.toArray(new UserDetails[userDetailsList.size()]));
			UiBaseResponse baseResponse1 = uic.call(updateRequest, UiBaseResponse.class);
			assertFalse(baseResponse1 instanceof ErrorResponse);
			assertTrue(baseResponse1 instanceof ConfirmationResponse);

			// Retrieve all users
			ur = new ReadUserDetailsRequest(SECURITY_USER, sessionId);
			baseResponse = uic.call(ur, UiBaseResponse.class);
			assertTrue(baseResponse instanceof ReadUserDetailsResponse);
			readDetails = (ReadUserDetailsResponse) baseResponse;
			assertTrue(readDetails.getUserDetails().length == (currentCount + USERS_TO_CREATE));

			// Retrieve User public keys
			PublicKeyRequest pkr = new PublicKeyRequest("test_0");
			UiBaseResponse br = null;

			br = uic.call(pkr, UiBaseResponse.class);
			assertNotNull(br);
			byte[] publicKey = ((PublicKeyResponse) br).getPublicKey();

			// Update user password
			UpdateUserPasswordRequest passwordRequest = new UpdateUserPasswordRequest(SECURITY_USER, sessionId);
			passwordRequest.generateSalted(publicKey, TEST_PASSWORD);
			UiBaseResponse baseResponse2 = uic.call(passwordRequest, UiBaseResponse.class);
			assertFalse(baseResponse2 instanceof ErrorResponse);
			assertTrue(baseResponse2 instanceof ConfirmationResponse);

			// Remove user
			List<String> deleteMe = new ArrayList<String>();
			for (int i = 0; i < USERS_TO_CREATE; i++)
			{
				deleteMe.add("test_" + i);
			}
			DeleteUserRequest delRequest = new DeleteUserRequest(SECURITY_USER, sessionId);
			delRequest.setUsersToDeleteIds((String[]) deleteMe.toArray(new String[deleteMe.size()]));
			UiBaseResponse baseResponse3 = uic.call(delRequest, UiBaseResponse.class);
			assertFalse(baseResponse3 instanceof ErrorResponse);
			assertTrue(baseResponse3 instanceof DeleteUserResponse);

			DeleteUserResponse delResponse = (DeleteUserResponse) baseResponse3;
			assertTrue(delResponse.getDeletedUserIds().length == USERS_TO_CREATE);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private IUser authenticate(String username, String password)
	{
		byte[] publicKey = security.getPublicKey(username);
		assertNotNull(publicKey);
		byte[] credentials = encrypt(password, publicKey);
		IUser user = security.authenticate(username, credentials);
		return user;
	}

	private byte[] encrypt(String password, byte[] publicKey)
	{
		try
		{
			byte[] passwordBytes = password.getBytes("utf-8");
			byte[] credentials = new byte[passwordBytes.length + publicKey.length];
			System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
			System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(credentials);
		}
		catch (UnsupportedEncodingException | NoSuchAlgorithmException e)
		{
			fail(e.getMessage());
		}
		return null;
	}

	@Test
	public void testRoleUpdating() throws Exception
	{
		security = esb.getFirstService(ISecurity.class);
		assertNotNull(authenticate(SECURITY_USER, SECURITY_PASS));

		// This is a simple test (nothing to do with UIConnector
		try
		{
			// Create Role
			int TEST_ROLE = 15;
			IRole newRole = security.createRole(TEST_ROLE);
			if (newRole == null)
			{
				newRole = security.getRole(TEST_ROLE);
			}
			newRole.setName("ServiceDeliverer");
			newRole.setDescription("Can Configure Services");
			newRole.update();

			// Get Available permissions
			List<IPermission> perms = security.getPermissions();

			// Choose some random role to assign
			Random rnd = new Random(System.currentTimeMillis());
			int rndOne = rnd.nextInt(perms.size());

			// Assign permissions (first remove all)
			for (int i = 0; i < perms.size(); i++)
			{
				perms.get(i).removeRole(newRole.getRoleId());
				perms.get(i).update();
			}

			// Add some random permision to role
			rndOne = rnd.nextInt(perms.size());
			perms.get(rndOne).addRole(newRole.getRoleId());
			perms.get(rndOne).update();

			// Verify role added to permission
			newRole = security.getRole(TEST_ROLE);
			perms = security.getPermissions();
			long mask = (newRole.getRoleId() == 0) ? 0L : (1L << (newRole.getRoleId() - 1));

			boolean wasSet = false;
			for (int i = 0; i < perms.size(); i++)
			{
				long j = ((long) perms.get(i).getRoles() & mask);
				if (j > 0)
				{
					// This is set
					if (i != rndOne)
					{
						// fail("Not corrent permission set!");
					}
					else
					{
						wasSet = true;
					}
				}
			}
			assert (wasSet);

			// Remove Role
			newRole = security.getRole(TEST_ROLE);
			boolean remove = newRole.delete();
			assert (remove);

			// Checked that removed
			wasSet = false;
			for (int i = 0; i < perms.size(); i++)
			{
				long j = ((long) perms.get(i).getRoles() & mask);
				if (j > 0)
				{
					// This is set
					if (i != rndOne)
					{
						fail("Not corrent permission set!");
					}
					else
					{
						wasSet = true;
					}
				}
			}
			assert (!wasSet);

		}
		catch (Exception e)
		{
			throw e;
		}

	}

	@Test
	public void testUpdateMyDetails() throws Exception
	{
		IUser user = createTestUser();
		assertNotNull(user);

		try (UIClient uic = createTestClientConnection())
		{
			// Logon
			String sessionId = loginAndRetrieveSessionId(TEST_USER, TEST_PASSWORD);
			assertNotNull(sessionId);

			// Update Details
			UpdateMyDetailsRequest updateMyDetailsRequest = new UpdateMyDetailsRequest(TEST_USER, sessionId);
			updateMyDetailsRequest.setMobile("012456789");
			updateMyDetailsRequest.setName("Human");
			UiBaseResponse baseResponse1 = uic.call(updateMyDetailsRequest, UiBaseResponse.class);
			assertFalse(baseResponse1 instanceof ErrorResponse);
			assertTrue(baseResponse1 instanceof ConfirmationResponse);
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			removeTestUser();
		}

	}

}

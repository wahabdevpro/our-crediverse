package hxc.connectors.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.ui.server.UIServer;
import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.security.IRole;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.SecurityService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIServerTest extends RunAllTestsBase
{
	private static IServiceBus esb;
	// private static LoggerService logger;
	// private static IService security;
	private static int SERVER_PORT = 9999;

	private static byte[] publicKey;

	private static String SECURITY_USER = "supplier";
	private static String SECURITY_PASS = " $$4u";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// Create ESB
		esb = ServiceBus.getInstance();
		esb.registerService(new LoggerService());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerService(new SecurityService());
		esb.start(null);

		ISecurity secure = esb.getFirstService(ISecurity.class);
		if (secure == null)
		{
			fail("No security service found");
		}

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{
	}

	// Test Can get key
	@Test
	public void test1()
	{
		UIServer server = new UIServer(esb)
		{

			@Override
			protected UiBaseResponse handleUiRequest(UiBaseRequest request) throws IOException
			{

				assertTrue(request instanceof PublicKeyRequest);
				PublicKeyResponse pkr = new PublicKeyResponse();
				ISecurity security = esb.getFirstService(ISecurity.class);

				byte[] publicKey = security.getPublicKey(request.getUserId());
				pkr.setPublicKey(publicKey);

				return pkr;
			}
		};

		// Start server
		try
		{
			server.start(SERVER_PORT);
		}
		catch (IOException e1)
		{
			fail("Server not started");
		} catch (InterruptedException e) {
			fail("Server not started due interrupt");
		}

		// And Client (and send something)
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", SERVER_PORT);
		}
		catch (IOException e1)
		{
			fail("Client could not connect");
		}

		PublicKeyRequest pkr = new PublicKeyRequest(SECURITY_USER);
		PublicKeyResponse pr = null;
		try
		{
			pr = uic.call(pkr, PublicKeyResponse.class);
			publicKey = pr.getPublicKey();
		}
		catch (ClassNotFoundException | IOException e)
		{
			fail("Call failed");
		}
		assertNotNull(pr);
		assertTrue(pr.getPublicKey().length > 0);

		// Clean up
		uic.close();
		server.stop();
	}

	// Test authenticate using key
	@Test
	public void test2() throws NoSuchAlgorithmException
	{
		assertNotNull(publicKey);

		UIServer server = new UIServer(esb)
		{

			@Override
			protected UiBaseResponse handleUiRequest(UiBaseRequest request) throws IOException
			{

				assertTrue(request instanceof AuthenticateRequest);
				AuthenticateRequest authReq = (AuthenticateRequest) request;

				AuthenticateResponse authResp = new AuthenticateResponse(request.getUserId());
				ISecurity securityService = esb.getFirstService(ISecurity.class);
				IUser user = securityService.authenticate(authReq.getUserId(), authReq.getCredentials());
				if (user != null)
				{
					authResp.setSessionId("123");
				}
				return authResp;
			}
		};

		// Start server
		try
		{
			server.start(SERVER_PORT);
		}
		catch (IOException e1)
		{
			fail("Server not started");
		} catch (InterruptedException e) {
			fail("Server not started due to interrupt");
		}

		// And Client (and send something)
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", SERVER_PORT);
		}
		catch (IOException e1)
		{
			fail("Client could not connect");
		}

		AuthenticateRequest auth1 = new AuthenticateRequest(SECURITY_USER);

		// First Fail at Login
		auth1.generateSalted(publicKey, "password");
		AuthenticateResponse authResp = null;
		try
		{
			authResp = uic.call(auth1, AuthenticateResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			fail("Authentication 1 Call failed");
		}
		assertNotNull(authResp);
		assertNull(authResp.getSessionId());

		// Now login correctly
		AuthenticateRequest auth2 = new AuthenticateRequest(SECURITY_USER);
		auth2.generateSalted(publicKey, SECURITY_PASS);
		try
		{
			authResp = uic.call(auth2, AuthenticateResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			fail("Authentication 2 Call failed");
		}
		assertNotNull(authResp);
		assertNotNull(authResp.getSessionId());

		// Clean up
		uic.close();
		server.stop();
	}

	@Test
	// Validate Session Timeout
	public void test3()
	{
		UiSessionManager sm = new UiSessionManager(1000);
		IUser user = new IUser()
		{

			@Override
			public boolean update()
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setUserId(String userId)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setPassword(byte[] password)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setName(String name)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setMobileNumber(String mobileNumber)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void setEnabled(boolean enabled)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isEnabled()
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isBuiltIn()
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean hasPermission(Object object, String permissionId)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean hasPermission(Class<?> type, String permissionId)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getUserId()
			{
				// TODO Auto-generated method stub
				return "123";
			}

			@Override
			public long getRoles()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public byte[] getPublicKey()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getName()
			{
				return "hello";
			}

			@Override
			public String getMobileNumber()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean delete()
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean addRole(int roleId)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean removeRole(int roleId)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public List<String> getPermissionIdList()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getUserRolesForLogin()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasRole(IRole role)
			{
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isSupplier()
			{
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public String getInternalMobileNumber()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setInternalMobileNumber(String mobileNumber)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getInternalName()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setInternalName(String name)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setInternalPassword(byte[] password)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getInternalUserId()
			{
				// TODO Auto-generated method stub
				return null;
			}
		};

		// Check for valid session
		String sessionId = null;
		byte[] dummyCredentials = { 0, 1, 2, 3, 4, 5 };

		for (int i = 0; i < 1000; i++)
		{
			sessionId = sm.addSession(user.getUserId(), dummyCredentials);
		}

		// Check last session relavant
		assertTrue(sm.isValidSession("123", sessionId));

		// Check for session refresh
		for (int i = 0; i < 10; i++)
		{
			assertTrue(sm.isValidSession("123", sessionId));
			try
			{
				Thread.sleep(700);
			}
			catch (InterruptedException e)
			{
			}
		}
		sm.revalidateAll();

		// All but one should have timed out
		assertEquals(sm.sessionSize(), 1);

		// Check userId validity
		assertTrue(!sm.isValidSession("456", sessionId));

		// Check for session timeout
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
		}
		assertTrue(!sm.isValidSession("123", sessionId));
	}

	public static String bytArrayToHex(byte[] a)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
}

package hxc.services.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;

public class SecurityTest extends RunAllTestsBase
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static ISecurity security;

	private static final String SUPPLIER_USER = "Supplier";
	private static final String SUPPLIER_PWD = " $$4u";
	private static final int SUPPLIER_ROLE = 1;
	private static final String SUPPLIER_ROLE_NAME = "Supplier";

	private static final int ADMINISTRATOR_ROLE = 2;
	private static final String ADMINISTRATOR_ROLE_NAME = "Administrator";

	private static final int CRM_ROLE = 3;
	private static final String CRM_ROLE_NAME = "CRM";

	private static final String TEST_USER = "UnitTest";
	private static final int TEST_ROLE = 49;
	private static final int DUPLICATE_ROLE = 48;

	private static final int ROLE_COUNT = 3;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup and tear down
	//
	// /////////////////////////////////

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new LoggerService());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerService(new SecurityService());
		esb.start(null);
		security = esb.getFirstService(ISecurity.class);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		// Delete UnitTest User if it exists
		IUser userUnit = security.getUser(TEST_USER);
		if (userUnit != null)
			userUnit.delete();

		// Delete Role TEST_ROLE
		IRole roleUnit = security.getRole(TEST_ROLE);
		if (roleUnit != null)
			roleUnit.delete();

		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testSecurity() throws Exception
	{

		// Authenticate as Supplier
		assertNotNull(authenticate(SUPPLIER_USER, SUPPLIER_PWD));

		// Get Supplier
		IUser userSupplier = security.getUser("supplier");
		assertNotNull(userSupplier);
		assertEquals("Supplier", userSupplier.getUserId());
		assertEquals("Supplier", userSupplier.getName());
		assertEquals("+27738175876", userSupplier.getMobileNumber());
		byte[] publicKey = userSupplier.getPublicKey();
		assertNotNull(publicKey);
		assertTrue(publicKey.length > 10);
		assertEquals(true, userSupplier.isBuiltIn());
		assertEquals(true, userSupplier.isEnabled());
		assertEquals(1, userSupplier.getRoles() & SUPPLIER_ROLE);

		// Check that the supplier is the supplier
		assertTrue(security.isSupplier(userSupplier));

		// Get Administrator
		IUser userAdmin = security.getUser("administrator");
		assertNotNull(userAdmin);
		assertEquals("Administrator", userAdmin.getUserId());
		assertEquals("Administrator", userAdmin.getName());
		assertEquals("", userAdmin.getMobileNumber());
		publicKey = userAdmin.getPublicKey();
		assertNotNull(publicKey);
		assertTrue(publicKey.length > 10);
		assertEquals(true, userAdmin.isBuiltIn());
		assertEquals(true, userAdmin.isEnabled());

		assertEquals(2, userAdmin.getRoles() & ADMINISTRATOR_ROLE);

		// Check NOT supplier
		assertFalse(security.isSupplier(userAdmin));

		// Delete UnitTest User if it exists
		IUser userUnit = security.getUser(TEST_USER);
		if (userUnit != null)
			userUnit.delete();

		// Get all Users
		List<IUser> users = security.getUsers();
		assertEquals(2, users.size());
		assertEquals("Administrator", users.get(0).getUserId());
		assertEquals("Supplier", users.get(1).getUserId());

		// Create User UnitTest
		userUnit = security.createUser("AdMiNistrator");
		assertNull(userUnit);
		userUnit = security.createUser("SuppLier");
		assertNull(userUnit);
		userUnit = security.createUser(TEST_USER);
		assertNotNull(userUnit);
		userUnit.setName("Unit Testing");
		userUnit.addRole(TEST_ROLE);
		userUnit.setMobileNumber("555");
		publicKey = userUnit.getPublicKey();
		assertNotNull(publicKey);
		userUnit.setPassword(encrypt("Java", publicKey));
		assertTrue(userUnit.update());

		// Verify User UnitTest
		userUnit = security.getUser(TEST_USER);
		assertNotNull(userUnit);
		assertEquals("UnitTest", userUnit.getUserId());
		assertEquals("Unit Testing", userUnit.getName());
		assertEquals("555", userUnit.getMobileNumber());
		publicKey = userUnit.getPublicKey();
		assertNotNull(publicKey);
		assertTrue(publicKey.length > 10);
		assertEquals(false, userUnit.isBuiltIn());
		assertEquals(true, userUnit.isEnabled());
		long testRoleMask = 1L << (TEST_ROLE - 1);
		assertEquals(testRoleMask, userUnit.getRoles());

		// Update Test
		userUnit.setName("Unit Testing2");
		assertTrue(userUnit.update());
		userUnit = null;
		userUnit = security.getUser(TEST_USER);
		assertEquals("Unit Testing2", userUnit.getName());

		// Authentication Test
		publicKey = security.getPublicKey(TEST_USER);
		assertNotNull(publicKey);
		byte[] credentials = encrypt("java", publicKey);
		userUnit = security.authenticate(TEST_USER, credentials);
		assertNull(userUnit);
		credentials = encrypt("Java", publicKey);
		userUnit = security.authenticate(TEST_USER, credentials);
		assertNotNull(userUnit);

		// Authenticate as Supplier
		assertNotNull(authenticate(SUPPLIER_USER, SUPPLIER_PWD));

		// Get Roles
		List<IRole> roles = security.getRoles();
		assertEquals(ROLE_COUNT, roles.size()); //
		for (IRole role : roles)
		{
			if (role.getRoleId() == SUPPLIER_ROLE)
				assertEquals(SUPPLIER_ROLE_NAME, role.getName());
			else if (role.getRoleId() == ADMINISTRATOR_ROLE)
				assertEquals(ADMINISTRATOR_ROLE_NAME, role.getName());
			else if (role.getRoleId() == CRM_ROLE)
				assertEquals(CRM_ROLE_NAME, role.getName());
		}

		// Get Supplier Role
		IRole supplierRole = security.getRole(1);
		assertNotNull(supplierRole);
		assertEquals(1, supplierRole.getRoleId());
		assertEquals("Supplier", supplierRole.getName());
		assertEquals("Built-in Supplier Role", supplierRole.getDescription());
		assertTrue(supplierRole.isBuiltIn());

		// Get Administrator Role
		IRole administratorRole = security.getRole(2);
		assertNotNull(administratorRole);
		assertEquals(2, administratorRole.getRoleId());
		assertEquals("Administrator", administratorRole.getName());
		assertEquals("Built-in Administrator Role", administratorRole.getDescription());
		assertTrue(administratorRole.isBuiltIn());

		// Delete Role TEST_ROLE
		IRole roleUnit = security.getRole(TEST_ROLE);
		if (roleUnit != null)
			roleUnit.delete();

		// Create Role
		roleUnit = security.createRole(0);
		assertNull(roleUnit);
		roleUnit = security.createRole(100);
		assertNull(roleUnit);
		roleUnit = security.createRole(1);
		assertNull(roleUnit);
		roleUnit = security.createRole(2);
		assertNull(roleUnit);
		roleUnit = security.createRole(TEST_ROLE);
		assertNotNull(roleUnit);
		roleUnit.setDescription("Unit Testing");
		roleUnit.setName("UnitTest");
		roleUnit.update();

		// Attempt to make a duplicate role
		IRole roleDuplicate = security.createRole(DUPLICATE_ROLE);
		assertNotNull(roleDuplicate);
		roleDuplicate.setDescription("Unit Testing");
		roleDuplicate.setName("UnitTest");
		assertFalse(roleDuplicate.update());

		// Attempt to modify Built-in Supplier
		supplierRole = null;
		supplierRole = security.getRole(1);
		supplierRole.setName("??");
		assertFalse(supplierRole.update());
		assertFalse(supplierRole.delete());
		supplierRole = security.getRole(1);
		assertEquals("Supplier", supplierRole.getName());

		// Attempt to modify Built-in Administrator
		administratorRole = null;
		administratorRole = security.getRole(2);
		administratorRole.setName("??");
		assertFalse(administratorRole.update());
		assertFalse(administratorRole.delete());
		administratorRole = security.getRole(2);
		assertEquals("Administrator", administratorRole.getName());

		// Modify Unit Test Role
		roleUnit = null;
		roleUnit = security.getRole(TEST_ROLE);
		roleUnit.setName("TestUnit");
		assertTrue(roleUnit.update());
		roleUnit = security.getRole(TEST_ROLE);
		assertEquals("TestUnit", roleUnit.getName());

		// Delete Test Role
		assertTrue(roleUnit.delete());
		roleUnit = security.getRole(TEST_ROLE);
		assertNull(roleUnit);

		// Delete User UnitTest
		assertFalse(userSupplier.delete());
		userSupplier = security.getUser("Supplier");
		assertNotNull(userSupplier);
		assertFalse(userAdmin.delete());
		userAdmin = security.getUser("Administrator");
		assertNotNull(userAdmin);
		assertTrue(userUnit.delete());
		userUnit = security.getUser(TEST_USER);
		assertNull(userUnit);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
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

	private IUser authenticate(String username, String password)
	{
		byte[] publicKey = security.getPublicKey(username);
		assertNotNull(publicKey);
		byte[] credentials = encrypt(password, publicKey);
		IUser user = security.authenticate(username, credentials);
		return user;
	}

}

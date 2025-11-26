package hxc.services.security;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.utils.instrumentation.IMetric;

public class SecurityService implements IService, ISecurity
{
	final static Logger logger = LoggerFactory.getLogger(SecurityService.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private IDatabase database;
	private boolean permissionsReflected = false;

	private static final String SUPPLIER = "Supplier";
	protected static final int SUPPLIER_ROLE = 1;
	private static final String ADMINISTRATOR = "Administrator";
	private static final int ADMINISTRATOR_ROLE = 2;
	private static final int CUSTOMER_RELATIONS_OFFICIAL_ROLE = 3;

	private static final Map<String, Permission> permissionMap = new HashMap<String, Permission>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Per Thread Properties
	//
	// /////////////////////////////////
	private static final ThreadLocal<Long> currentRoles = new ThreadLocal<Long>()
	{
		@Override
		protected Long initialValue()
		{
			return 0x7FFFFFFFL;
		}
	};

	private static void setCurrentRoles(long roles)
	{
		currentRoles.set(roles);
	}

	private static long getCurrentRoles()
	{
		return currentRoles.get();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////
	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		// Get Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		// Log
		logger.info("Security Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log
		logger.info("Security Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{

	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Authentication
	//
	// /////////////////////////////////

	@Override
	public byte[] getPublicKey(String userId)
	{
		IUser user = getInternalUser(userId);
		if (user == null)
			return getRandomKey();
		else
			return user.getPublicKey();
	}

	@Override
	public IUser authenticate(String userId, byte[] credentials)
	{
		User user = (User) getInternalUser(userId);
		if (user == null)
			return null;
		byte[] password = user.getInternalPassword();
		int count = password.length;
		if (credentials == null || credentials.length != count)
			return null;
		for (int index = 0; index < count; index++)
		{
			if (password[index] != credentials[index])
				return null;
		}

		setCurrentRoles(user.getInternalRoles());

		return user;
	}

	@Override
	public void check(Object object, String permissionId) throws SecurityException
	{
		// Defensive
		if (object == null)
			throw new SecurityException("Null object");

		check(object.getClass(), permissionId);
	}

	@Override
	public boolean hasPermission(Object object, String permissionId)
	{
		// Defensive
		if (object == null)
			throw new SecurityException("Null object");

		return hasPermission(object.getClass(), permissionId);
	}

	@Override
	public void check(Class<?> type, String permissionId) throws SecurityException
	{
		if (!hasPermission(type, permissionId))
			throw new SecurityException(permissionId);
	}

	@Override
	public boolean hasPermission(Class<?> type, String permissionId)
	{
		return hasPermission(type, permissionId, getCurrentRoles());
	}

	@Override
	public boolean isSupplier(IUser user)
	{
		return user.isSupplier();
	}

	public boolean hasPermission(Class<?> type, String permissionId, long roles)
	{
		// Defensive
		if (type == null || permissionId == null || permissionId.length() == 0)
			throw new SecurityException("Null parameter");

		Permission permission = (Permission) getPermission(type, permissionId);
		if (permission == null)
			return false;
		return (permission.getInternalRoles() & roles) != 0L;
	}

	private IPermission getPermission(Class<?> type, String permissionId)
	{
		String path = type.getName();
		int pathPos = path.lastIndexOf('.');
		if (pathPos > 0)
			path = path.substring(0, pathPos);

		// Get From Cache
		Permission permission = permissionMap.get(getPermissionKey(permissionId, path));
		if (permission != null)
			return permission;

		// Get from Database
		permission = (Permission) getInternalPermission(getPermissionKey(permissionId, path));
		if (permission != null)
			return permission;

		// Reflect to see if it really exists
		Perm perm = (Perm) type.getAnnotation(Perm.class);
		if (perm == null || !perm.name().equalsIgnoreCase(permissionId))
		{
			perm = null;
			Perms perms = (Perms) type.getAnnotation(Perms.class);
			if (perms == null)
				return null;
			for (Perm p : perms.perms())
			{
				if (p.name().equalsIgnoreCase(permissionId))
				{
					perm = p;
					break;
				}
			}
		}
		if (perm == null)
			return null;

		// Create it
		String description = perm.description().length() == 0 ? perm.name() : perm.description();
		long roles = perm.supplier() ? SUPPLIER_ROLE : SUPPLIER_ROLE | ADMINISTRATOR_ROLE;
		permission = new Permission(this, perm.name(), description, perm.category(), perm.implies(), path, roles);

		internalUpdatePermission(permission);

		return permission;
	}

	@Override
	public List<IPermission> getPermissions()
	{
		check(Permission.class, "ViewPermissions");

		// Reflect All Permissions
		reflectAllPermissions();

		return getInternalPermissions();
	}

	private List<IPermission> getInternalPermissions()
	{
		List<IPermission> result = new ArrayList<IPermission>();
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Retrieve from Database
			List<Permission> permissions = connection.selectList(Permission.class, "");
			for (Permission permission : permissions)
			{
				permission.setParent(this);
				permissionMap.put(getPermissionKey(permission), permission);
				result.add(permission);
			}
			return result;
		}
		catch (Exception e)
		{
			logger.error("getInternalPermissions failed", e);
			return null;
		}
	}

	@Override
	public List<String> getUserPermissionIds(IUser user)
	{
		List<String> result = null;
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Retrieve from Database
			result = new ArrayList<String>();

			List<Permission> permissions = connection.selectList(Permission.class, "");
			long roleMask = user.getUserRolesForLogin();

			for (Permission permission : permissions)
			{
				if ((roleMask & permission.getRoleForValidation()) > 0)
				{
					result.add(permission.getPermissionId());
				}
			}
		}
		catch (Exception e)
		{
			logger.error("getUserPermissionIds failed", e);
			return null;
		}

		return result;
	}

	/**
	 * Return full list of user permissions
	 */
	@Override
	public List<IPermission> getUserPermissionDetails(IUser user)
	{
		List<IPermission> result = null;
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Retrieve from Database
			result = new ArrayList<>();

			List<Permission> permissions = connection.selectList(Permission.class, "");
			long roleMask = user.getUserRolesForLogin();

			for (Permission permission : permissions)
			{
				if ((roleMask & permission.getRoleForValidation()) > 0)
				{
					result.add(permission);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("getUserPermissionDetails failed", e);
			return null;
		}

		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// User Management
	//
	// /////////////////////////////////

	@Override
	public List<IUser> getUsers()
	{
		check(User.class, "ViewUsers");

		// Ensure Supplier and Admin users are loaded
		getUser(SUPPLIER);
		getUser(ADMINISTRATOR);

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			List<User> users = connection.selectList(User.class, "order by Name");
			List<IUser> result = new ArrayList<IUser>();
			for (User user : users)
			{
				result.add(user);
				user.setParent(this);
			}
			return result;
		}
		catch (Exception e)
		{
			logger.error("getUsers connection failed", e);
			return null;
		}
	}

	@Override
	public IUser getUser(String userId)
	{
		check(User.class, "ViewUsers");
		return getInternalUser(userId);
	}

	private IUser getInternalUser(String userId)
	{
		if (userId == null)
			return null;

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Select from database
			User user = connection.select(User.class, "where UserID = %s", userId);
			if (user != null)
			{
				user.setParent(this);
				return user;
			}

			// Create Supplier
			if (userId.equalsIgnoreCase(SUPPLIER))
			{
				user = new User();
				user.setParent(this);
				setCurrentRoles(SUPPLIER_ROLE);
				user.setBuiltIn(true);
				user.setEnabled(true);
				user.setMobileNumber("+27738175876");
				user.setName(SUPPLIER);
				user.setPublicKey(getRandomKey());
				user.setPassword(encrypt(" $$4u", user.getPublicKey()));
				user.addRole(SUPPLIER_ROLE);
				user.setUserId(SUPPLIER);
				connection.insert(user);
				return user;
			}

			// Create Administrator
			if (userId.equalsIgnoreCase(ADMINISTRATOR))
			{
				user = new User();
				user.setParent(this);
				user.setBuiltIn(true);
				user.setEnabled(true);
				user.setMobileNumber("");
				user.setName(ADMINISTRATOR);
				user.setPublicKey(getRandomKey());
				user.setPassword(encrypt("C8Rr>q;B&g5&9pF\"5~RS", user.getPublicKey()));
				user.addRole(ADMINISTRATOR_ROLE);
				user.setUserId(ADMINISTRATOR);
				connection.insert(user);
				return user;
			}

			return user;
		}
		catch (Exception ex)
		{
			logger.error("getInternalUser", ex);
		}

		return null;
	}

	@Override
	public IUser createUser(String userId)
	{
		check(User.class, "ChangeUsers");

		if (userId == null || userId.equalsIgnoreCase(SUPPLIER) || userId.equalsIgnoreCase(ADMINISTRATOR))
			return null;

		User user = new User();
		user.setParent(this);
		user.setUserId(userId);
		user.setName(userId);
		user.setPublicKey(getRandomKey());
		user.setBuiltIn(false);
		user.setEnabled(true);
		user.setMobileNumber("?");
		return user;
	}

	public boolean updateUser(User user)
	{
		check(User.class, "ChangeUsers");

		// Supplier Checks
		if (user.isBuiltIn() && user.getUserId().equalsIgnoreCase(SUPPLIER))
		{
			user.addRole(SUPPLIER_ROLE);
		}
		else
		{
			user.removeRole(~SUPPLIER_ROLE);
		}

		// Administrator Checks
		if (user.isBuiltIn() && user.getUserId().equalsIgnoreCase(ADMINISTRATOR))
		{
			user.addRole(ADMINISTRATOR_ROLE);
		}

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.upsert(user);
			return true;
		}
		catch (Exception e)
		{
			logger.error("updateUser", e);
			return false;
		}
	}

	/**
	 * Relaxed to save non-role details
	 * 
	 * @param user
	 * @return
	 */
	public boolean updateInternalUserDetails(IUser user)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.upsert(user);
			return true;
		}
		catch (Exception e)
		{
			logger.error("updateInternalUserDetails", e);
			return false;
		}
	}

	public boolean deleteUser(User user)
	{
		check(User.class, "ChangeUsers");

		if (user.isBuiltIn())
			return false;
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.delete(user);
			return true;
		}
		catch (Exception e)
		{
			logger.error("deleteUser", e);
			return false;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Role Management
	//
	// /////////////////////////////////

	@Override
	public List<IRole> getRoles()
	{
		check(Role.class, "ViewRoles");

		// Ensure Supplier and Admin roles are loaded
		getRole(SUPPLIER_ROLE);
		getRole(ADMINISTRATOR_ROLE);
		getRole(CUSTOMER_RELATIONS_OFFICIAL_ROLE);

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			List<Role> roles = connection.selectList(Role.class, "order by Name");
			List<IRole> result = new ArrayList<IRole>();
			for (Role role : roles)
			{
				result.add(role);
				role.setParent(this);
			}
			return result;
		}
		catch (Exception e)
		{
			logger.error("getRoles", e);
			return null;
		}
	}

	@Override
	public IRole getRole(int roleId)
	{
		check(Role.class, "ViewRoles");
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			Role role = connection.select(Role.class, "where RoleID = %s", roleId);
			if (role != null)
			{
				role.setParent(this);
				return role;
			}

			if (roleId == SUPPLIER_ROLE)
			{
				role = new Role();
				role.setParent(this);
				role.setRoleId(SUPPLIER_ROLE);
				role.setBuiltIn(true);
				role.setDescription("Built-in Supplier Role");
				role.setName("Supplier");
				connection.insert(role);
				return role;
			}

			if (roleId == ADMINISTRATOR_ROLE)
			{
				role = new Role();
				role.setParent(this);
				role.setRoleId(ADMINISTRATOR_ROLE);
				role.setBuiltIn(true);
				role.setDescription("Built-in Administrator Role");
				role.setName("Administrator");
				connection.insert(role);
				return role;
			}

			if (roleId == CUSTOMER_RELATIONS_OFFICIAL_ROLE)
			{
				role = new Role();
				role.setParent(this);
				role.setRoleId(CUSTOMER_RELATIONS_OFFICIAL_ROLE);
				role.setBuiltIn(true);
				role.setDescription("Built-in Customer Relations Management Role");
				role.setName("CRM");
				connection.insert(role);
				return role;
			}

			return null;
		}
		catch (Exception e)
		{
			logger.error("getRole", e);
			return null;
		}
	}

	@Override
	public IRole createRole(int roleId)
	{
		check(Role.class, "ChangeRoles");

		// Range Quota
		if (roleId <= 0 || roleId > 50)
			return null;

		// Musn't exist yet
		Role role = (Role) getRole(roleId);
		if (role != null)
			return null;

		// Create role
		role = new Role();
		role.setParent(this);
		role.setName(String.format("Role %d", roleId));
		role.setDescription(role.getName());
		role.setBuiltIn(false);
		role.setRoleId(roleId);
		return role;
	}

	@Override
	public List<String> getRolePermissionIds(IRole role)
	{
		List<IPermission> permissions = getInternalPermissions();
		long mask = (role.getRoleId() == 0) ? 0L : (1L << (role.getRoleId() - 1));

		List<String> result = new ArrayList<>();
		for (IPermission perm : permissions)
		{
			long rolesMask = ((Permission) perm).getInternalRoles();
			if ((rolesMask & mask) > 0)
			{
				result.add(perm.getPermissionId());
			}
		}
		return result;
	}

	@Override
	/**
	 * Extract Permissions pertaining to Security Role
	 * @param role Security Role to interrogate
	 * @return List of UPermissions using this role
	 */
	public List<IPermission> getRolePermissionDetails(IRole role)
	{
		List<IPermission> result = new ArrayList<>();

		// Extract permission mask
		long mask = (role.getRoleId() == 0) ? 0L : (1L << (role.getRoleId() - 1));

		for (IPermission perm : getInternalPermissions())
		{
			// Extract Permissions relative to mask
			long rolesMask = ((Permission) perm).getInternalRoles();
			if ((rolesMask & mask) > 0)
			{
				result.add(perm);
			}
		}
		return result;
	}

	public boolean updateRole(Role role)
	{
		check(Role.class, "ChangeRoles");
		if (role == null || role.isBuiltIn())
			return false;

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Test for duplication
			Role duplicate = connection.select(Role.class, "where RoleId != %s and Name = %s", role.getRoleId(), role.getName());
			if (duplicate != null)
				return false;

			connection.upsert(role);
			return true;
		}
		catch (Exception e)
		{
			logger.error("updateRole", e);
			return false;
		}
	}

	public boolean deleteRole(Role role)
	{
		check(Role.class, "ChangeRoles");
		if (role == null || role.isBuiltIn())
			return false;

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.delete(role);
			return true;
		}
		catch (Exception e)
		{
			logger.error("deleteRole", e);
			return false;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Permission Management
	//
	// /////////////////////////////////

	@Override
	public IPermission getPermission(String permissionId)
	{
		check(Permission.class, "ViewPermissions");
		return getInternalPermission(permissionId);
	}

	/**
	 * Provides a finer grained way of retrieving permissions, since permissionId / category is unique per permission
	 */
	@Override
	public IPermission getPermission(String permissionId, String category)
	{
		check(Permission.class, "ViewPermissions");
		return getInternalPermission(permissionId, category);
	}

	private IPermission getInternalPermission(String permissionId)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Retrieve from Database
			Permission permission = null;
			if (permissionId.lastIndexOf('.') > 0)
				permission = connection.select(Permission.class, "where permissionId = %s and path = %s", permissionId.substring(permissionId.lastIndexOf('.') + 1),
						permissionId.substring(0, permissionId.lastIndexOf('.')));
			else
				permission = connection.select(Permission.class, "where permissionId = %s", permissionId);
			if (permission != null)
			{
				permission.setParent(this);
				permissionMap.put(getPermissionKey(permission), permission);
			}
			return permission;
		}
		catch (Exception e)
		{
			logger.error("getInternalPermission", e);
			return null;
		}
	}

	private IPermission getInternalPermission(String permissionId, String category)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			// Sanetize permissionId
			if (permissionId.lastIndexOf('.') > 0)
				permissionId = permissionId.substring(permissionId.lastIndexOf('.') + 1);

			// Retrieve from Database
			Permission permission = connection.select(Permission.class, "where permissionId = %s and category = %s", permissionId, category);

			if (permission != null)
			{
				permission.setParent(this);
				permissionMap.put(getPermissionKey(permission), permission);
			}
			return permission;
		}
		catch (Exception e)
		{
			logger.error("getInternalPermission", e);
			return null;
		}
	}

	public boolean updatePermission(Permission permission)
	{
		check(Permission.class, "ChangePermissions");
		return internalUpdatePermission(permission);
	}

	private boolean internalUpdatePermission(Permission permission)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.upsert(permission);
		}
		catch (Exception e)
		{
			logger.error("internalUpdatePermission", e);
			return false;
		}

		permissionMap.put(getPermissionKey(permission), permission);

		return true;
	}

	public boolean deletePermission(Permission permission)
	{
		check(Permission.class, "ChangePermissions");

		try (IDatabaseConnection connection = database.getConnection(null))
		{
			connection.delete(permission);
		}
		catch (Exception e)
		{
			logger.error("deletePermission", e);
			return false;
		}

		permissionMap.remove(getPermissionKey(permission));

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private byte[] getRandomKey()
	{
		final int count = 20;
		byte[] result = new byte[count];
		for (int index = 0; index < count; index++)
		{
			result[index] = (byte) ((Math.random() * 256) - 128);
		}
		return result;
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
			logger.error("encrypt", e);
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Permission Discovery
	//
	// /////////////////////////////////

	// Reflect All Permissions
	private void reflectAllPermissions()
	{
		if (permissionsReflected)
			return;

		reflectPermission(Permission.class);
		reflectPermission(Role.class);
		reflectPermission(User.class);

		List<IPlugin> plugins = esb.getRegisteredPlugins();
		for (IPlugin plugin : plugins)
		{
			IConfiguration config = plugin.getConfiguration();
			reflectPermission(config);
		}
		permissionsReflected = true;
	}

	// Reflect Configuration Permissions
	private void reflectPermission(IConfiguration config)
	{
		if (config == null)
			return;
		reflectPermission(config.getClass());

		Collection<IConfiguration> children = config.getConfigurations();
		if (children == null)
			return;
		for (IConfiguration child : children)
		{
			reflectPermission(child);
		}

	}

	private void reflectPermission(Class<?> cls)
	{
		Annotation[] annotations = cls.getAnnotations();

		for (Annotation annotation : annotations)
		{
			if (annotation instanceof Perm)
			{
				reflectPermission(cls, (Perm) annotation);
			}
			else if (annotation instanceof Perms)
			{
				for (Perm perm : ((Perms) annotation).perms())
				{
					reflectPermission(cls, perm);
				}
			}
		}
	}

	private void reflectPermission(Class<?> cls, Perm annotation)
	{
		if (hasPermission(cls, annotation.name()))
		{
			return;
		}
	}

	private String getPermissionKey(Permission permission)
	{
		return getPermissionKey(permission.getPermissionId(), permission.getPath());
	}

	private String getPermissionKey(String permissionId, String path)
	{
		return String.format("%s.%s", path.toLowerCase(), permissionId.toLowerCase());
	}

}

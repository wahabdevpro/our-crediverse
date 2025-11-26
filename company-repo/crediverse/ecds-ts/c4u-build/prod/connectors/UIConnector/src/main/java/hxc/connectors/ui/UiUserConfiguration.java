package hxc.connectors.ui;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.services.security.IPermission;
import hxc.services.security.IRole;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.utils.protocol.uiconnector.userman.common.SecurityPermissionInfo;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;
import hxc.utils.protocol.uiconnector.userman.request.ReadSecurityPermissionRequest;
import hxc.utils.protocol.uiconnector.userman.request.ReadSecurityRoleRequest;
import hxc.utils.protocol.uiconnector.userman.response.ReadSecurityPermissionesponse;
import hxc.utils.protocol.uiconnector.userman.response.ReadSecurityRolesResponse;

public class UiUserConfiguration
{
	final static Logger logger = LoggerFactory.getLogger(UiUserConfiguration.class);

	private final int MIN_ROLE_ID = 3;
	private final int MAX_ROLE_ID = 10;

	public void discoverSecurity(ISecurity security)
	{
		try
		{
			security.getUsers();
			security.getRoles();
			security.getPermissions();
		}
		catch (Exception e)
		{
		}
	}

	public UserDetails getUserDetails(ISecurity security, IUser usr)
	{
		UserDetails result = new UserDetails(usr.getInternalUserId(), usr.getInternalName(), usr.getInternalMobileNumber());

		// This part could throw a security Exception
		try
		{
			List<IRole> availRoles = security.getRoles();
			for (IRole role : availRoles)
			{
				long roleMask = 1L << (role.getRoleId() - 1); // Role Id is a position in the usr.getRoles()
				if ((usr.getRoles() & roleMask) > 0)
				{
					result.addSecurityRole(role.getRoleId());
				}
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

	private SecurityRole extractRoleDetails(List<IPermission> currentUserPermissions, ISecurity security, int roleId) throws Exception
	{
		SecurityRole result = null;
		try
		{
			IRole role = security.getRole(roleId);
			if (role != null)
			{
				result = new SecurityRole(role.getRoleId(), role.getName(), role.getDescription());

				// Go through all permissions, who belongs to this role?
				try
				{

					List<IPermission> permissions = security.getRolePermissionDetails(role);
					result.addPermissions(permissions);
					boolean isAssignable = true;
					for (IPermission rPerm : permissions)
					{
						boolean canAssignPerm = false;
						for (IPermission uPerm : currentUserPermissions)
						{
							if (rPerm.getPermissionId().equalsIgnoreCase(uPerm.getPermissionId()) && rPerm.getPath().equalsIgnoreCase(uPerm.getPath()))
							{
								canAssignPerm = true;
								break;
							}
						}
						if (!canAssignPerm)
						{
							isAssignable = false;
							break;
						}
					}
					result.setAssignable(isAssignable);

				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(String.format("Problem extracting roles for ROLE ID {}", roleId), e);
			throw new Exception("Problem extracting roles for ROLE ID: " + roleId);
		}
		return result;
	}

	public boolean createUpdateUserDetails(ISecurity security, UserDetails userDetails) throws ValidationException
	{
		IUser user = security.getUser(userDetails.getUserId());
		if (userDetails.isNewUser())
		{
			for (IUser usr : security.getUsers())
			{
				if (usr.getUserId().equalsIgnoreCase(userDetails.getUserId()))
				{
					throw new ValidationException("UserName not unique");
				}
			}
			user = security.createUser(userDetails.getUserId());
		}

		if (user == null)
		{
			throw new ValidationException("User not found to be updated (for new try newUser parameter)");
		}

		// Default data
		user.setMobileNumber(userDetails.getMobileNumber());
		user.setName(userDetails.getName());
		user.setEnabled(userDetails.isEnabled());

		// Update roles
		try
		{
			for (IRole role : security.getRoles())
			{
				user.removeRole(role.getRoleId());
			}
			for (int roleId : userDetails.getRoleIds())
			{
				user.addRole(roleId);
			}
		}
		catch (Exception e)
		{
		}

		// There needs to be password (else will not save)
		if (userDetails.isNewUser())
		{
			byte[] publicKey = user.getPublicKey();
			user.setPassword(publicKey);
		}

		return user.update();
	}

	public boolean deleteUsers(ISecurity security, String userToDeleteId)
	{
		IUser user = security.getUser(userToDeleteId);

		if (user != null)
		{
			try
			{
				return user.delete();
			}
			catch (SecurityException se)
			{
				return false;
			}

		}
		return false;
	}

	public boolean createUpdateSecurityRole(ISecurity security, SecurityRole role) throws ValidationException
	{

		if (role.getRoleId() < 0)
		{
			// First Check that the role name does not exist
			for (IRole ir : security.getRoles())
			{
				if (ir.getName().equalsIgnoreCase(role.getName()))
				{
					throw new ValidationException("RoleName exists");
				}
			}

			for (int i = MIN_ROLE_ID; i <= MAX_ROLE_ID; i++)
			{
				IRole ir = security.getRole(i);
				if (ir == null)
				{
					role.setRoleId(i);
					break;
				}
			}
		}

		if (role.getRoleId() < 0)
		{
			// Run out of slots
			throw new SecurityException("No new roles can be created");
		}
		else
		{
			// Does it exist then update else create new
			IRole irole = security.getRole(role.getRoleId());
			if (irole == null)
			{
				irole = security.createRole(role.getRoleId());
			}
			irole.setName(role.getName());
			irole.setDescription(role.getDescription());

			// Clean out role permissions (Done from the perspective of the permissions
			try
			{
				long mask = (role.getRoleId() == 0) ? 0L : (1L << (role.getRoleId() - 1));
				List<IPermission> perms = security.getPermissions();
				for (int i = 0; i < perms.size(); i++)
				{
					// Does this permission exist?
					boolean hasPemission = false;
					for (String permPathAndId : role.getPermissionIds())
					{
						String permPath = permPathAndId.substring(0, permPathAndId.lastIndexOf("."));
						String permId = permPathAndId.substring(permPathAndId.lastIndexOf(".") + 1);

						if (permPath.equalsIgnoreCase(perms.get(i).getPath()) && permId.equalsIgnoreCase(perms.get(i).getPermissionId()))
						{
							hasPemission = true;
							break;
						}
					}

					long check = ((long) perms.get(i).getRoles() & mask);
					if ((check > 0) && (!hasPemission))
					{
						// Remove role from permission
						perms.get(i).removeRole(role.getRoleId());
					}
					else if ((check == 0) && (hasPemission))
					{
						// Add role to permission
						perms.get(i).addRole(role.getRoleId());
					}

					perms.get(i).update();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}

			return irole.update();
		}
	}

	public ReadSecurityPermissionesponse readSecurityPermissions(ReadSecurityPermissionRequest request, ISecurity security, IUser user)
	{
		// Extract Permissions for current user
		List<String> currentUserPermissions = security.getUserPermissionIds(user);

		// Read in Roles
		List<SecurityPermissionInfo> availPerms = new ArrayList<>();

		if (request.getRoleId() < 0)
		{
			// Get Available permissions
			List<IPermission> perms = security.getPermissions();
			for (IPermission perm : perms)
			{
				availPerms.add(new SecurityPermissionInfo(perm.getPermissionId(), perm.getPath(), perm.getDescription(), perm.getCategory(), perm.getImplies(), currentUserPermissions.contains(perm
						.getPermissionId())));
			}
		}
		else
		{
			IRole role = security.getRole(request.getRoleId());
			if (role != null)
			{
				List<IPermission> perms = security.getPermissions();
				long mask = (role.getRoleId() == 0) ? 0L : (1L << (role.getRoleId() - 1));
				for (IPermission perm : perms)
				{
					long check = ((long) perm.getRoles() & mask);
					if (check > 0)
					{
						availPerms.add(new SecurityPermissionInfo(perm.getPermissionId(), perm.getPath(), perm.getDescription(), perm.getCategory(), perm.getImplies(), currentUserPermissions
								.contains(perm.getPermissionId())));
					}
				}
			}
		}
		ReadSecurityPermissionesponse response = new ReadSecurityPermissionesponse(request.getUserId(), request.getSessionId());
		response.setSecurityPermissionInfo(availPerms.toArray(new SecurityPermissionInfo[availPerms.size()]));
		return response;
	}

	public ReadSecurityRolesResponse readSecurityRoleRequest(ReadSecurityRoleRequest request, ISecurity security, IUser user) throws Exception
	{
		// Extract Permissions for current user
		List<IPermission> currentUserPermissions = security.getUserPermissionDetails(user);

		// Read in Roles
		ReadSecurityRoleRequest req = (ReadSecurityRoleRequest) request;
		List<SecurityRole> roles = new ArrayList<>();

		if (req.getRoleId() < 0)
		{
			// Read all
			List<IRole> iroles = security.getRoles();
			if (iroles != null)
			{
				for (IRole rol : iroles)
				{
					if (rol.getRoleId() != 1)
					{
						roles.add(extractRoleDetails(currentUserPermissions, security, rol.getRoleId()));
					}
				}
			}
		}
		else
		{
			// Read specific user
			SecurityRole srole = extractRoleDetails(currentUserPermissions, security, req.getRoleId());
			if (srole != null)
			{
				roles.add(srole);
			}
		}

		// Set Role list
		ReadSecurityRolesResponse response = new ReadSecurityRolesResponse(req.getUserId(), req.getSessionId());
		response.setSecurityRole(roles.toArray(new SecurityRole[roles.size()]));

		return response;
	}

	public boolean deleteSecurityRole(ISecurity security, int roleToDeleteId)
	{
		IRole role = security.getRole(roleToDeleteId);

		if (role != null)
		{
			try
			{
				return role.delete();
			}
			catch (SecurityException se)
			{
				return false;
			}

		}
		return false;
	}
}

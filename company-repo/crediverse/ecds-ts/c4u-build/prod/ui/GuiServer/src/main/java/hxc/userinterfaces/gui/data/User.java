package hxc.userinterfaces.gui.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User
{
	private String userId;
	private String sessionId;
	private String lastLoginError;
	private byte[] credentials;
	private String credentialsString;
	private String name;

	// Menu structure rights
	private boolean canViewUsers;
	private boolean canChangeUsers;
	private boolean canViewRoles;
	private boolean canChangeRoles;
	private boolean canViewServices;
	private boolean canChangeServices;
	private boolean canViewCustomerCare;
	private boolean canViewPermissions;
	private boolean canChangePermissions;

	private List<String> permissionIds;
	private Map<String, String> sessionIds; // host / sessionId

	public User()
	{
	}

	public User(String userId, String sessionId, List<String> permissionIds)
	{
		this.userId = userId;
		this.sessionId = sessionId;
		this.permissionIds = new ArrayList<>();
		this.permissionIds.addAll(permissionIds);
		validateUserPermisssionsForGUI();
	}

	private void validateUserPermisssionsForGUI()
	{
		for (String permission : permissionIds)
		{
			if (permission.equalsIgnoreCase("ViewUsers"))
			{
				canViewUsers = true;
			}
			else if (permission.equalsIgnoreCase("ChangeUsers"))
			{
				canChangeUsers = true;
			}
			else if (permission.equalsIgnoreCase("ViewRoles"))
			{
				canViewRoles = true;
			}
			else if (permission.equalsIgnoreCase("ChangeRoles"))
			{
				canChangeRoles = true;
			}
			else if (permission.equalsIgnoreCase("ViewTuning"))
			{
				canViewServices = true;
			}
			else if (permission.equalsIgnoreCase("ViewPermissions"))
			{
				canViewPermissions = true;
			}
			else if (permission.equalsIgnoreCase("ChangePermissions"))
			{
				canChangePermissions = true;
			}
		}
	}

	/**
	 * @return the userId
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	/**
	 * @return the lastLoginError
	 */
	public String getLastLoginError()
	{
		return lastLoginError;
	}

	/**
	 * @param lastLoginError
	 *            the lastLoginError to set
	 */
	public void setLastLoginError(String lastLoginError)
	{
		this.lastLoginError = lastLoginError;
	}

	/**
	 * @return the canViewUsers
	 */
	public boolean isCanViewUsers()
	{
		return canViewUsers;
	}

	/**
	 * @param canViewUsers
	 *            the canViewUsers to set
	 */
	public void setCanViewUsers(boolean canViewUsers)
	{
		this.canViewUsers = canViewUsers;
	}

	/**
	 * @return the canViewRoles
	 */
	public boolean isCanViewRoles()
	{
		return canViewRoles;
	}

	/**
	 * @param canViewRoles
	 *            the canViewRoles to set
	 */
	public void setCanViewRoles(boolean canViewRoles)
	{
		this.canViewRoles = canViewRoles;
	}

	/**
	 * @return the canViewServices
	 */
	public boolean isCanViewServices()
	{
		return canViewServices;
	}

	/**
	 * @param canViewServices
	 *            the canViewServices to set
	 */
	public void setCanViewServices(boolean canViewServices)
	{
		this.canViewServices = canViewServices;
	}

	/**
	 * @return the canViewCustomerCare
	 */
	public boolean isCanViewCustomerCare()
	{
		return canViewCustomerCare;
	}

	/**
	 * @param canViewCustomerCare
	 *            the canViewCustomerCare to set
	 */
	public void setCanViewCustomerCare(boolean canViewCustomerCare)
	{
		this.canViewCustomerCare = canViewCustomerCare;
	}

	/**
	 * @return the permissionIds
	 */
	public List<String> getPermissionIds()
	{
		return permissionIds;
	}

	/**
	 * @param permissionIds
	 *            the permissionIds to set
	 */
	public void setPermissionIds(List<String> permissionIds)
	{
		this.permissionIds = permissionIds;
	}

	/**
	 * @return the canChangeUsers
	 */
	public boolean isCanChangeUsers()
	{
		return canChangeUsers;
	}

	/**
	 * @param canChangeUsers
	 *            the canChangeUsers to set
	 */
	public void setCanChangeUsers(boolean canChangeUsers)
	{
		this.canChangeUsers = canChangeUsers;
	}

	/**
	 * @return the canChangeRoles
	 */
	public boolean isCanChangeRoles()
	{
		return canChangeRoles;
	}

	/**
	 * @param canChangeRoles
	 *            the canChangeRoles to set
	 */
	public void setCanChangeRoles(boolean canChangeRoles)
	{
		this.canChangeRoles = canChangeRoles;
	}

	/**
	 * @return the canChangeServices
	 */
	public boolean isCanChangeServices()
	{
		return canChangeServices;
	}

	/**
	 * @param canChangeServices
	 *            the canChangeServices to set
	 */
	public void setCanChangeServices(boolean canChangeServices)
	{
		this.canChangeServices = canChangeServices;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the canViewPermissions
	 */
	public boolean isCanViewPermissions()
	{
		return canViewPermissions;
	}

	/**
	 * @param canViewPermissions
	 *            the canViewPermissions to set
	 */
	public void setCanViewPermissions(boolean canViewPermissions)
	{
		this.canViewPermissions = canViewPermissions;
	}

	/**
	 * @return the canChangePermissions
	 */
	public boolean isCanChangePermissions()
	{
		return canChangePermissions;
	}

	/**
	 * @param canChangePermissions
	 *            the canChangePermissions to set
	 */
	public void setCanChangePermissions(boolean canChangePermissions)
	{
		this.canChangePermissions = canChangePermissions;
	}

	public byte[] getCredentials()
	{
		return credentials;
	}

	public void setCredentials(byte[] credentials)
	{
		this.credentialsString = new String(credentials);
		this.credentials = new byte[credentials.length];
		System.arraycopy(credentials, 0, this.credentials, 0, credentials.length);
	}

	public String getCredentialsString()
	{
		return credentialsString;
	}

	public Map<String, String> getSessionIds()
	{
		return sessionIds;
	}

	public void setSessionIds(Map<String, String> sessionIds)
	{
		this.sessionIds = sessionIds;
	}
	
	public boolean hasPermission(String permissionName) 
	{
		for(String perm : permissionIds)
		{
			if (perm.equalsIgnoreCase(permissionName))
				return true;
		}
		return false;
	}

}

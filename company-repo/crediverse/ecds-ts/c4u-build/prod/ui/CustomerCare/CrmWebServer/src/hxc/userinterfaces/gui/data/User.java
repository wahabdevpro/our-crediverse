package hxc.userinterfaces.gui.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class User
{

	private String userId;
	private String password;
	private Locale language = Locale.ENGLISH;
	private int languageId = 1;

	private String sessionId;
	private String lastLoginError;
	private String name;

	// Menu structure rights

	private List<String> permissionIds;

	public User()
	{
	}

	public User(String userId, String password)
	{
		this.userId = userId;
		this.password = password;
	}

	public User(String userId, String sessionId, List<String> permissionIds)
	{
		this.userId = userId;
		this.sessionId = sessionId;
		this.permissionIds = new ArrayList<>();
		this.permissionIds.addAll(permissionIds);
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
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the language
	 */
	public Locale getLanguage()
	{
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(Locale language)
	{
		this.language = language;
	}

	/**
	 * @return the languageId
	 */
	public int getLanguageId()
	{
		return languageId;
	}

	/**
	 * @param languageId
	 *            the languageId to set
	 */
	public void setLanguageId(int languageId)
	{
		this.languageId = languageId;
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

}
package hxc.connectors.ui.sessionman;

import java.util.Date;

public class UiSession
{

	// userId and credentials stored for system user validation
	private String userId;
	private byte[] credentials;

	private long lastUpdate; // Last update in millisessons

	public UiSession(String userId, byte[] credentials)
	{
		lastUpdate = (new Date()).getTime();
		this.userId = userId;
		this.credentials = new byte[credentials.length];
		System.arraycopy(credentials, 0, this.credentials, 0, credentials.length);
	}

	/**
	 * @return the lastUpdate
	 */
	public long getLastUpdate()
	{
		return lastUpdate;
	}

	/**
	 * @param lastUpdate
	 *            the lastUpdate to set
	 */
	public void setLastUpdate(long lastUpdate)
	{
		this.lastUpdate = lastUpdate;
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
	 * @return the credentials
	 */
	public byte[] getCredentials()
	{
		return credentials;
	}

	/**
	 * @param credentials
	 *            the credentials to set
	 */
	public void setCredentials(byte[] credentials)
	{
		this.credentials = credentials;
	}

}

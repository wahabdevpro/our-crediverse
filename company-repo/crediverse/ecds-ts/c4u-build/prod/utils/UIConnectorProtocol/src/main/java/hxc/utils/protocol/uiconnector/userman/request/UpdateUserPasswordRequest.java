package hxc.utils.protocol.uiconnector.userman.request;

import java.security.NoSuchAlgorithmException;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.uiconnector.client.UiConnectorUtils;

public class UpdateUserPasswordRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -4434064167303281472L;
	private String userToUpdateId;
	private byte[] credentials;

	public UpdateUserPasswordRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the userToUpdateId
	 */
	public String getUserToUpdateId()
	{
		return userToUpdateId;
	}

	/**
	 * @param userToUpdateId
	 *            the userToUpdateId to set
	 */
	public void setUserToUpdateId(String userToUpdateId)
	{
		this.userToUpdateId = userToUpdateId;
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

	public void generateSalted(byte[] publicKey, String password) throws NoSuchAlgorithmException
	{
		credentials = UiConnectorUtils.generateSalted(publicKey, password);
	}

}

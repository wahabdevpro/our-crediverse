package hxc.utils.protocol.uiconnector.request;

import java.security.NoSuchAlgorithmException;

import hxc.utils.uiconnector.client.UiConnectorUtils;

public class AuthenticateRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -5927299731033959100L;
	private byte[] credentials;

	public AuthenticateRequest(String userId)
	{
		super(userId);
		setRequestCode(UiRequestCode.AUTHENTICATE);
	}

	/**
	 * Generate SALTED password
	 * 
	 * @param publicKey
	 *            public key (SALT)
	 * @param password
	 *            plain text password
	 * @throws NoSuchAlgorithmException
	 */
	public void generateSalted(byte[] publicKey, String password) throws NoSuchAlgorithmException
	{
		credentials = UiConnectorUtils.generateSalted(publicKey, password);
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

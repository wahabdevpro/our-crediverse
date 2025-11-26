package hxc.utils.uiconnector.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UiConnectorUtils
{
	public static byte[] generateSalted(byte[] publicKey, String password) throws NoSuchAlgorithmException
	{
		byte[] passwordBytes = password.getBytes();
		byte[] credentials = new byte[passwordBytes.length + publicKey.length];
		System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
		System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return md.digest(credentials);
	}
}

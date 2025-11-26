package hxc.ecds.utils.encrypt;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {
	private static final String ALGO = "AES";
	private static final byte[] keyValue = new byte[]{'I','Z','Z','7','y','^','R','D','E','9','@','a','u',',','H','z'};

	/**
	 * Encrypt a string with AES algorithm.
	 *
	 * @param data is a string
	 * @return the encrypted string
	 */
	public static String encrypt(String data) throws Exception
	{
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(data.getBytes());
		return Base64.getEncoder().encodeToString(encVal);
	}

	/**
	 * Decrypt a string with AES algorithm.
	 *
	 * @param encryptedData is a string
	 * @return the decrypted string
	 */
	public static String decrypt(String encryptedData) throws Exception
	{
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
		byte[] decValue = c.doFinal(decordedValue);
		return new String(decValue);
	}

	/**
	 * Generate a new encryption key.
	 */
	private static Key generateKey() throws Exception
	{
		return new SecretKeySpec(keyValue, ALGO);
	}
}

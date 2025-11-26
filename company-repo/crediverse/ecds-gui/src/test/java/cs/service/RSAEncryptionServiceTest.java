package cs.service;

import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import cs.config.UnitTestConstants;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class RSAEncryptionServiceTest
{

	@Autowired
	private RSAEncryptionService rsaSvc;

	private Key publicKey;
	private Key privateKey;

	@Before
	public void before() throws Exception
	{
		try
		{
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);

			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEncryption()
	{
		String toEncrypt = UnitTestConstants.CONST_PASSWORD_1;
		String encrypted = rsaSvc.encrypt(toEncrypt.getBytes());
		String decrypted = rsaSvc.decrypt(encrypted);
		assertTrue(toEncrypt.equals(decrypted));
	}

	@Test
	public void testEncryption2()
	{
		String toEncrypt = UnitTestConstants.CONST_PASSWORD_2;
		String encrypted = rsaSvc.encrypt(toEncrypt.getBytes());
		String decrypted = rsaSvc.decrypt(encrypted);
		assertTrue(toEncrypt.equals(decrypted));
	}

	public byte[] myencrypt(byte[] key, String data)
	{
		byte[] result = null;
		Cipher encryptor;
		try
		{
			encryptor = Cipher.getInstance("RSA");

			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(key);
			PublicKey pubkey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);

			encryptor.init(Cipher.ENCRYPT_MODE, pubkey);
			result = encryptor.doFinal(data.getBytes());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public String mydecrypt(byte[] key, byte[] data)
	{
		String decrypted = null;
		Cipher decryptor;
		try
		{
			decryptor = Cipher.getInstance("RSA");

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(this.privateKey.getEncoded());
			PrivateKey privkey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

			decryptor.init(Cipher.DECRYPT_MODE, privkey);
			decrypted = new String(decryptor.doFinal(data));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new String(decrypted);
	}

	@Test
	public void testEncryptionWithPublicKey()
	{
		String toEncrypt = UnitTestConstants.CONST_PASSWORD_1;
		try
		{

			byte[] encrypted = rsaSvc.encrypt(this.publicKey.getEncoded(), toEncrypt.getBytes());
			// byte[] encrypted = rsaSvc.encrypt(this.publicKey.getEncoded(),
			// toEncrypt.getBytes());

			System.out.println(rsaSvc.decrypt(this.privateKey.getEncoded(), encrypted));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * byte[] byteData = DatatypeConverter.parseHexBinary(new String(data));
		 *
		 *
		 *
		 *
		 *
		 *
		 * byte[] publicKey = this.publicKey.getEncoded(); byte[] privateKey = this.privateKey.getEncoded(); byte[] bintoEncrypt = toEncrypt.getBytes(); byte[] encrypted = rsaSvc.encrypt(publicKey,
		 * bintoEncrypt);
		 *
		 * String decrypted = rsaSvc.decrypt(privateKey, encrypted);
		 */
		// assertTrue(toEncrypt.equals(decrypted));
	}

	@Test
	public void testEncryptionWithPublicKey2()
	{
		String toEncrypt = UnitTestConstants.CONST_PASSWORD_2;
		byte[] publicKey = this.publicKey.getEncoded();
		byte[] privateKey = this.privateKey.getEncoded();
		byte[] encrypted = rsaSvc.encrypt(publicKey, toEncrypt.getBytes());
		String decrypted = rsaSvc.decrypt(privateKey, encrypted);
		assertTrue(toEncrypt.equals(decrypted));
	}
}

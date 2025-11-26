package cs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.io.BaseEncoding;

import cs.dto.security.EncryptionKey;

@Service
public class RSAEncryptionService
{
	private static final Logger logger = LoggerFactory.getLogger(RSAEncryptionService.class);

	private EncryptionKey privateKey;
	private EncryptionKey publicKey;

	private Cipher encryptor;
	private Cipher decryptor;

	public RSAEncryptionService()
	{
		Security.addProvider(new BouncyCastleProvider());
		createKey(1024, "RSA");
	}

	private String pemToString(PemObject pemObject)
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter oswriter = new OutputStreamWriter(byteArrayOutputStream);
		// PEMWriter writer=new PEMWriter(oswriter);
		JcaPEMWriter writer = new JcaPEMWriter(oswriter);

		try
		{
			writer.writeObject(pemObject);
			writer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}

		return new String(byteArrayOutputStream.toByteArray());
	}

	// http://www.txedo.me/blog/java-generate-rsa-keys-write-pem-file/
	private void createKey(int length, String algorithm)
	{
		KeyPairGenerator kpg = null;
		try
		{
			if (privateKey == null && privateKey == null)
				try
				{
					kpg = KeyPairGenerator.getInstance(algorithm, "BC");
				}
				catch (NoSuchProviderException e)
				{
					// TODO Auto-generated catch block
					logger.error("", e);
				}
			kpg.initialize(length);
			KeyPair kp = kpg.genKeyPair();
			RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
			RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

			Key publicKey = kp.getPublic();
			Key privateKey = kp.getPrivate();
			this.privateKey = new EncryptionKey();
			this.publicKey = new EncryptionKey();

			this.privateKey.setRawKey(privateKey);
			this.publicKey.setRawKey(publicKey);

			this.privateKey.setAlgorithm(privateKey.getAlgorithm());
			this.publicKey.setAlgorithm(publicKey.getAlgorithm());

			Encoder encoder = Base64.getEncoder();
			this.privateKey.setEncodedKey(encoder.encodeToString(privateKey.getEncoded()));
			this.publicKey.setEncodedKey(pemToString(new PemObject("Public key", publicKey.getEncoded())));
			// this.publicKey.setEncodedKey(encoder.encodeToString(publicKey.getEncoded()));

			this.privateKey.setEncodingFormat(privateKey.getFormat());
			this.publicKey.setEncodingFormat(publicKey.getFormat());

			encryptor = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
			decryptor = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");

			encryptor.init(Cipher.ENCRYPT_MODE, publicKey);
			decryptor.init(Cipher.DECRYPT_MODE, privateKey);
			this.publicKey.setExponent(new String(pub.getPublicExponent().toString(16)));
			this.publicKey.setModulus(new String(pub.getModulus().toString(16)));
			this.privateKey.setExponent(new String(priv.getPrivateExponent().toString(16)));
			this.privateKey.setModulus(new String(priv.getModulus().toString(16)));

		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (NoSuchPaddingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (InvalidKeyException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (NoSuchProviderException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
	}

	public EncryptionKey getPublicKey()
	{
		return publicKey;
	}

	public String encrypt(byte[] data)
	{
		byte[] cipherText = null;
		try
		{
			cipherText = encryptor.doFinal(data);
		}
		catch (IllegalBlockSizeException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (BadPaddingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return BaseEncoding.base16().lowerCase().encode(cipherText);
	}

	public byte[] encrypt(byte[] key, byte[] data)
	{
		byte[] encodedData = null;
		try
		{
			Cipher encryptor = Cipher.getInstance("RSA");

			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(key);
			PublicKey pubkey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);

			encryptor.init(Cipher.ENCRYPT_MODE, pubkey);
			encodedData = encryptor.doFinal(data);
		}
		catch (InvalidKeySpecException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (NoSuchAlgorithmException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (NoSuchPaddingException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (InvalidKeyException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (IllegalBlockSizeException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (BadPaddingException e1)
		{
			// TODO Auto-generated catch block
			logger.error("", e1);
		}
		catch (Exception ex)
		{
			logger.error("", ex);
		}

		// return
		// BaseEncoding.base16().lowerCase().encode(encodedData).getBytes();
		return encodedData;
	}

	public String decrypt(String data)
	{
		String result = null;
		try
		{
			byte[] byteData = DatatypeConverter.parseHexBinary(data);
			result = new String(decryptor.doFinal(byteData));
		}
		catch (IllegalBlockSizeException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (BadPaddingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return result;
	}

	public String decrypt(byte[] key, byte[] data)
	{
		String result = null;
		try
		{
			Cipher decryptor = Cipher.getInstance("RSA");

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
			PrivateKey privkey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

			decryptor.init(Cipher.DECRYPT_MODE, privkey);
			// byte[] byteData = DatatypeConverter.parseHexBinary(new
			// String(data));
			result = new String(decryptor.doFinal(data));
		}
		catch (IllegalBlockSizeException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (BadPaddingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (InvalidKeySpecException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (InvalidKeyException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		catch (NoSuchPaddingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return result;
	}
}

package hxc.utils.registration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.google.gson.Gson;

public class Registration implements IRegistration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String issuer;
	private Date issueDate;
	private int maxTPS;
	private int maxPeakTPS;
	private int maxNodes;
	private String supplierKey;
	private FacilityRegistration[] facilities;

	private static final String FILENAME = "registration.lic";

	private static final byte[] publicKeyBytes = new byte[] { //
	48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, //
			-127, 0, -87, 95, 114, 49, 119, 39, -65, 50, 112, -117, -22, 29, 12, 41, -28, -37, -22, 41, 87, -105, 106, -79, -103, //
			1, 55, 92, -85, -109, 123, -28, -27, 33, 83, -24, -97, -53, 60, 97, -10, -29, 11, -45, 115, 54, -14, 62, -123, -32, //
			45, -41, -84, 52, -100, 59, 59, 124, -101, 118, 83, 110, -46, 21, 41, -15, 96, 34, -49, -77, -24, 47, -6, 117, -50, //
			-127, -113, 108, 96, -50, 108, -126, -94, 85, -77, -92, 51, 7, -60, 119, -63, 7, 105, -4, -68, 48, 36, 11, 65, -47, //
			123, 78, 52, 25, -89, 68, 78, 109, 22, -71, -73, 80, 87, -5, 78, 99, 96, 39, -12, 5, 19, -37, 48, 1, 55, 101, -78, //
			-90, -106, -99, 2, 3, 1, 0, 1 };

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getIssuer()
	{
		return issuer;
	}

	public void setIssuer(String issuer)
	{
		this.issuer = issuer;
	}

	@Override
	public Date getIssueDate()
	{
		return issueDate;
	}

	public void setIssueDate(Date issueDate)
	{
		this.issueDate = issueDate;
	}

	@Override
	public int getMaxTPS()
	{
		return maxTPS;
	}

	public void setMaxTPS(int maxTPS)
	{
		this.maxTPS = maxTPS;
	}

	@Override
	public int getMaxPeakTPS()
	{
		return maxPeakTPS;
	}

	public void setMaxPeakTPS(int maxPeakTPS)
	{
		this.maxPeakTPS = maxPeakTPS;
	}

	@Override
	public int getMaxNodes()
	{
		return maxNodes;
	}

	public void setMaxNodes(int maxNodes)
	{
		this.maxNodes = maxNodes;
	}

	@Override
	public String getSupplierKey()
	{
		return supplierKey;
	}

	public void setSupplierKey(String supplierKey)
	{
		this.supplierKey = supplierKey;
	}

	@Override
	public IFacilityRegistration[] getFacilities()
	{
		return facilities;
	}

	public void setFacilities(FacilityRegistration[] facilities)
	{
		this.facilities = facilities;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean serialize(byte[] privateKeyBytes, String directory)
	{
		try
		{
			// Create private key from it's bytes
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

			// Serialize this object to Json
			Gson gson = new Gson();
			String json = gson.toJson(this);
			byte[] jsonBytes = json.getBytes("UTF-8");

			// Encrypt the Json
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			byte[] cipherData = blockCipher(jsonBytes, cipher, Cipher.ENCRYPT_MODE);

			// Save to Licence File
			File file = new File(directory, FILENAME);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(cipherData);
			fos.close();

		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e)
		{
			return false;
		}

		return true;
	}

	public static Registration deserialize(String directory)
	{
		try
		{
			// Read Licence File
			File file = new File(directory, FILENAME);
			if (!file.exists())
				return null;

			FileInputStream fis = new FileInputStream(file);
			byte[] cipherData = new byte[(int) file.length()];
			fis.read(cipherData, 0, cipherData.length);
			fis.close();

			// Create public key from its bytes
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			// Decrypt the Data
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			byte[] jsonBytes = blockCipher(cipherData, cipher, Cipher.DECRYPT_MODE);

			// Deserialize the result
			String json = new String(jsonBytes, "UTF-8");
			Gson gson = new Gson();
			Registration result = gson.fromJson(json, Registration.class);

			return result;

		}
		catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
		{
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private static byte[] blockCipher(byte[] bytes, Cipher cipher, int mode) throws IllegalBlockSizeException, BadPaddingException
	{
		byte[] scrambled = new byte[0];
		byte[] toReturn = new byte[0];

		// Encryption = 100, Decryption = 128 (RSA)
		int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;

		byte[] buffer = new byte[length];
		for (int i = 0; i < bytes.length; i++)
		{
			if ((i > 0) && (i % length == 0))
			{
				scrambled = cipher.doFinal(buffer);
				toReturn = append(toReturn, scrambled);

				int newlength = length;

				if (i + length > bytes.length)
				{
					newlength = bytes.length - i;
				}
				buffer = new byte[newlength];
			}

			buffer[i % length] = bytes[i];
		}

		scrambled = cipher.doFinal(buffer);
		toReturn = append(toReturn, scrambled);

		return toReturn;
	}

	private static byte[] append(byte[] prefix, byte[] suffix)
	{
		byte[] toReturn = new byte[prefix.length + suffix.length];
		for (int i = 0; i < prefix.length; i++)
		{
			toReturn[i] = prefix[i];
		}
		for (int i = 0; i < suffix.length; i++)
		{
			toReturn[i + prefix.length] = suffix[i];
		}
		return toReturn;
	}
}

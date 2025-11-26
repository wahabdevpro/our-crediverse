package hxc.services.pin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "se_pin")
public class Pin
{
	final static Logger logger = LoggerFactory.getLogger(Pin.class);
	// //////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// ////////////////////////////
	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdn;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String variantID;

	// Don't persist this guy
	@Column(persistent = false)
	private String pin;

	@Column(nullable = false)
	private String encryptedPin;

	@Column(nullable = false)
	private int failedCount = 0;

	@Column(nullable = false)
	private boolean blocked = false;

	// //////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////
	public Pin()
	{

	}

	public Pin(String msisdn, String serviceID, String variantID, String pin, int failedCount, boolean blocked, Date lastAccessed)
	{
		super();
		setMsisdn(msisdn);
		setServiceID(serviceID);
		setVariantID(variantID);
		setPin(pin);
		setFailedCount(failedCount);
		setBlocked(blocked);
		setEncryptedPin(Pin.encrypt(pin));
	}

	public Pin(Pin pin)
	{
		setMsisdn(pin.getMsisdn());
		setServiceID(pin.getServiceID());
		setVariantID(pin.getVariantID());
		setPin(pin.getPin());
		setFailedCount(pin.getFailedCount());
		setBlocked(pin.isBlocked());
		setEncryptedPin(Pin.encrypt(pin.getPin()));
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Getters and setters
	//
	// ////////////////////////////

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String getPin()
	{
		return pin;
	}

	public void setPin(String pin)
	{
		this.pin = pin;
	}

	public int getFailedCount()
	{
		return failedCount;
	}

	public void setFailedCount(int failedCount)
	{
		this.failedCount = failedCount;
	}

	public boolean isBlocked()
	{
		return blocked;
	}

	public void setBlocked(boolean blocked)
	{
		this.blocked = blocked;
	}

	public String getEncryptedPin()
	{
		return encryptedPin;
	}

	public void setEncryptedPin(String encryptedPin)
	{
		this.encryptedPin = encryptedPin;
	}

	public static String encrypt(String pin)
	{
		String algorithm = "SHA1";
		String encryptedPin = null;

		try
		{
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(pin.getBytes());
			byte[] bytes = md.digest();
			encryptedPin = bytesToHex(bytes);
		}
		catch (NoSuchAlgorithmException ae)
		{
			logger.error(ae.getLocalizedMessage(), ae);
		}
		catch (Exception e)
		{
			logger.error(e.getLocalizedMessage(), e);
		}

		return encryptedPin;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////////////
	private static String bytesToHex(byte[] b)
	{
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuffer buf = new StringBuffer();

		for (int j = 0; j < b.length; j++)
		{
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}

		return buf.toString();
	}

}

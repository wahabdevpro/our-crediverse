package hxc.services.ecds.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

public class SignCheck
{
	final static Logger logger = LoggerFactory.getLogger(SignCheck.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long POLY64REV = 0xd800000000000000L;
	private static final long[] LOOKUPTABLE;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ByteArrayOutputStream baos = null;
	private String debug = "";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Static Initialisation
	//
	// /////////////////////////////////

	static
	{
		LOOKUPTABLE = new long[0x100];
		for (int i = 0; i < 0x100; i++)
		{
			long v = i;
			for (int j = 0; j < 8; j++)
			{
				if ((v & 1) == 1)
				{
					v = (v >>> 1) ^ POLY64REV;
				}
				else
				{
					v = (v >>> 1);
				}
			}
			LOOKUPTABLE[i] = v;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SignCheck()
	{
		baos = new ByteArrayOutputStream();
		add("Constructor()[Concurrent], ", "Concurrent");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public SignCheck add(String name, String value)
	{
		try
		{
			if (value == null)
			{
				debug += name + "(NullString)[";
				SignCheck signCheck = add("", -1);
				debug += "], ";
				return signCheck;
			}
			else
			{
				debug += name + "(String)[" + value + "], ";
				baos.write(value.trim().getBytes("UTF-8"));
			}
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}

		return this;
	}

	public SignCheck add(String name, byte[] value)
	{
		try
		{
			if (value == null)
			{
				debug += name + "(nullByte[])[";
				SignCheck signCheck = add("", -1);
				debug += "], ";
				return signCheck;
			} else {
				
				debug += name + "(byte[])[" + Arrays.toString(value) + "], ";
				baos.write(value);
			}
		}
		catch (Throwable e)
		{
			logger.error("", e);
		}
		return this;
	}

	public SignCheck add(String name, Integer value)
	{
		if (value == null)
			value = -1;
		try
		{
			debug += name + "(Integer)[" + Integer.toString(value) + "], ";
			baos.write(ByteBuffer.allocate(4).putInt(value).array());
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
		return this;
	}

	public SignCheck add(String name, Long value)
	{
		if (value == null)
			value = -1L;
		try
		{
			debug += name + "(Long)[" + Long.toString(value) + "], ";
			baos.write(ByteBuffer.allocate(8).putLong(value).array());
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
		return this;
	}

	public SignCheck add(String name, Date value)
	{
		if (value == null)
		{
			debug += name + "(NullDate)[";
			SignCheck signCheck = add("", -1L);
			debug += "], ";
			return signCheck;
		} else
		{
			// XXX TODO FIXME ... this is here in case the database backend does not support milliseconds.
			debug += name + "(Date)[";
			SignCheck signCheck = add("", value.getTime() / 1000);
			debug += "], ";
			return signCheck;
		}
	}

	public SignCheck add(String name, BigDecimal value)
	{
		if (value == null)
		{
			debug += name + "(NullBigDecimal)[";
			SignCheck signCheck = add("", -1L);
			debug += "], ";
			return signCheck;
		}
		else if (value.signum() == 0)
		{
			debug += name+"(BigDecimalSigNumZero)[";
			SignCheck signCheck = add("", "0");
			debug += "], ";
			return signCheck;
		} else
		{
			debug += name + "(BigDecimal)[";
			SignCheck signCheck = add("",value.stripTrailingZeros().toString());
			debug += "], ";
			return signCheck;
		}
	}

	private SignCheck add(String name, BigInteger value)
	{
		if (value == null)
		{
			debug += name + "(NullBigInteger)[";
			SignCheck signCheck = add("", -1L);
			debug += "], ";
			return signCheck;
		} else
		{
			debug += "BigInteger[";
			SignCheck signCheck = add("", value.toByteArray());
			debug += "], ";
			return signCheck;
		}
	}

	public SignCheck add(String name, Boolean value)
	{
		debug += name + "(Boolean)[";
		if (value == null)
		{
			add("", -1);
		} else if (value) {
			add("", 1);
		} else {
			add("", 0);
		}
		debug += "], ";
		return this;
	}
	
	public String getByteArray()
	{
		byte[] byteArray = baos.toByteArray();
		String bytesArrayString = Arrays.toString(byteArray);
		return bytesArrayString;
	}

	public long signature()
	{
		try
		{
			byte[] byteArray = baos.toByteArray();			
			long result = hashByAlgo1(byteArray);			
			return result;
		}
		finally
		{
			try
			{
				baos.close();
			}
			catch (IOException e)
			{
				logger.error("", e);
			}
		}
	}
	
	public String getDebug()
	{
		return debug;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Static Methods
	//
	// /////////////////////////////////

	/**
	 * * Calculates the CRC64 checksum for the given data array.
	 * 
	 * @param data
	 *            data to calculate checksum for
	 * @return checksum value
	 */
	public static long hashByAlgo1(final byte[] data)
	{
		long sum = 0;
		for (final byte b : data)
		{
			final int lookupidx = ((int) sum ^ b) & 0xff;
			sum = (sum >>> 8) ^ LOOKUPTABLE[lookupidx];
		}
		return sum;
	}

}

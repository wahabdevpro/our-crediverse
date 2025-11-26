package hxc.connectors.diagnostic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.diagnostic.Field.FieldType;
import hxc.connectors.sms.ISmsConnector;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;

public class DiagnosticConnector implements IConnector, IDiagnosticsTransmitter
{
	final static Logger logger = LoggerFactory.getLogger(DiagnosticConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ISmsConnector sms;
	private TimedThread diagnosticWorkerThread;
	private static final String filename = "./diagnostics.xml";
	private static Diagnostics diagnostics;
	private static byte[] dataBuffer;
	private static boolean rebooted = true;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnector implementation
	//
	// /////////////////////////////////
	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{

		// Get SMS
		sms = esb.getFirstConnector(ISmsConnector.class);
		if (sms == null)
			return false;

		// Load Meta Data
		loadMetaData();

		// Start Diagnostic Worker Thread
		diagnosticWorkerThread = new TimedThread("Diagnostic Worker Thread", config.sendIntervalHours * 3600_000L, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				if(config.isEnabled())
				{
					try
					{
						doDiagnosicWork();
					}
					catch (Exception e)
					{
						logger.error("doDiagnosicWork failed", e);
					}
				} else {
					logger.debug("Diagnostic Connector is disabled. Skipping...");
				}
			}
		};
		diagnosticWorkerThread.start();

		// Log Information
		logger.info("Diagnostic Connector Started");

		// Send First SMS after 60 seconds
		TimedThread diagnosticFirstThread = new TimedThread("Diagnostic First Thread", 60_000L, TimedThreadType.EXECUTE_ONCE)
		{
			@Override
			public void action()
			{
				if(config.isEnabled())
				{
					try
					{
						doDiagnosicWork();
					}
					catch (Exception e)
					{
						logger.error("doDiagnosticWork failed", e);
					}
				} else {
					logger.debug("Diagnostic Connector is disabled. Skipping...");
				}
			}
		};
		diagnosticFirstThread.start();

		return true;
	}

	@Override
	public void stop()
	{

		if (diagnosticWorkerThread != null)
		{
			try
			{
				// Stop the thread
				diagnosticWorkerThread.kill();

				// Wait for it to die
				diagnosticWorkerThread.join(200000);

			}
			catch (InterruptedException ex)
			{
				logger.error("diagnosticWorkerThread interupted", ex);
			}
			finally
			{
				diagnosticWorkerThread = null;
			}

		}

		// Log Information
		logger.info("Diagnostic Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (DiagnosticConfiguration) config;

		if (diagnosticWorkerThread != null)
			diagnosticWorkerThread.setWaitTime(this.config.sendIntervalHours * 3600_000L);

	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ChangeDiagnosticParameters", implies = "ViewDiagnosticParameters", description = "Change Diagnostic Parameters", category = "Diagnostic", supplier = true),
			@Perm(name = "ViewDiagnosticParameters", description = "View Diagnostic Parameters", category = "Diagnostic", supplier = true),
			@Perm(name = "PerformDiagnosticMigration", description = "Perform NPC Migration", category = "Diagnostic", supplier = true), })
	public class DiagnosticConfiguration extends ConfigurationBase
	{
		private boolean enabled = true;
		private int sendIntervalHours = 6;
		private String serverNumbers = "";
		private String sourceNumber = "27123";

		@SupplierOnly
		public int getSendIntervalHours()
		{
			check(esb, "ViewDiagnosticParameters");
			return sendIntervalHours;
		}

		@SupplierOnly
		public void setSendIntervalHours(int sendIntervalHours) throws ValidationException
		{
			check(esb, "ChangeDiagnosticParameters");
			ValidationException.inRange(1, sendIntervalHours, 730);
			this.sendIntervalHours = sendIntervalHours;
		}

		@SupplierOnly
		@Config(description = "Destination MSISDNs", comment = "Comma separated list of MSISDNs that the diagnostic information shall be sent to.")
		public String getServerNumbers()
		{
			check(esb, "ViewDiagnosticParameters");
			return serverNumbers;
		}

		@SupplierOnly
		public void setServerNumbers(String serverNumbers)
		{
			check(esb, "ChangeDiagnosticParameters");
			this.serverNumbers = serverNumbers;
		}

		@SupplierOnly
		public String getSourceNumber()
		{
			check(esb, "ViewDiagnosticParameters");
			return sourceNumber;
		}

		@SupplierOnly
		public void setSourceNumber(String sourceNumber)
		{
			check(esb, "ChangeDiagnosticParameters");
			this.sourceNumber = sourceNumber;
		}

		@SupplierOnly
		public void setEnabled(boolean enabled)
		{
			check(esb, "ChangeDiagnosticParameters");
			this.enabled = enabled;
		}

		@SupplierOnly
		public boolean isEnabled()
		{
			check(esb, "ViewDiagnosticParameters");
			return enabled;
		}


		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 4165791750478587280L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Diagnostic Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{

		}

		@SupplierOnly
		public String SendNow()
		{
			check(esb, "PerformDiagnosticMigration");
			String result = DiagnosticConnector.this.sendSMS();
			return result;
		}

	};

	DiagnosticConfiguration config = new DiagnosticConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Worker Threads
	//
	// /////////////////////////////////
	private void doDiagnosicWork()
	{
		sendSMS();
	}

	private String sendSMS()
	{
		String result = null;

		set("Meta", "Version", diagnostics.getVersion(), 6);
		set("Meta", "TStamp", new Date());
		set("Meta", "Reboot", rebooted);

		byte[] buffer = dataBuffer;
		if (buffer == null)
		{
			result = "No SMS Diagnostics Buffer to send";
			logger.debug(result);
			return result;
		}

		if (config.serverNumbers.isEmpty())
		{
			result = "No Server Numbers configured fror SMS Diagnostics";
			logger.debug(result);
			return result;
		}
		String[] numbers = config.serverNumbers.split("\\,");

		final String base64 = DatatypeConverter.printBase64Binary(buffer);
		for (String number : numbers)
		{
			result = result == null ? "Sent to " + number : result + " and " + number;
			logger.debug("SMS Diagnostic '{}' to '{}'", base64, number);
			sms.send(config.sourceNumber, number, new INotificationText()
			{

				@Override
				public String getText()
				{
					return base64;
				}

				@Override
				public String getLanguageCode()
				{
					return IPhrase.ENG;
				}
			});

			rebooted = false;
		}

		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private void loadMetaData()
	{
		File file = new File(filename);
		if (!file.exists())
		{
			diagnostics = new Diagnostics();
			diagnostics.setVersion(1);
			saveMetaData();

		}

		else
		{
			try
			{
				XStream xstream = new XStream(new StaxDriver());
				xstream.processAnnotations(Diagnostics.class);
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				diagnostics = (Diagnostics) xstream.fromXML(reader, new Diagnostics());
				reader.close();
			}
			catch (Exception e)
			{
				logger.error("Failed to load diagnostics XML", e);
				diagnostics = new Diagnostics();
			}
		}

		set("Meta", "Version", diagnostics.getVersion(), 6);
		set("Meta", "TStamp", new Date());
		set("Meta", "Host", HostInfo.getName(), 8);

	}

	private synchronized void saveMetaData()
	{
		try
		{
			XStream xstream = new XStream(new StaxDriver());
			xstream.processAnnotations(Diagnostics.class);
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			xstream.toXML(diagnostics, writer);
			writer.close();
		}
		catch (IOException ex)
		{
			logger.error("Failed to write daignostics XML", ex);
			return;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDiagnosticsTransmitter Implementation
	//
	// /////////////////////////////////

	@Override
	public void set(String scope, String name, boolean value)
	{
		set(scope, name, FieldType.Flag, value ? new byte[] { 1 } : new byte[] { 0 }, 1);
	}

	@Override
	public void set(String scope, String name, String value, int maxLength)
	{
		if (value == null)
			value = "";

		if (value.length() > maxLength)
		{
			value = value.substring(0, maxLength);
		}
		else if (value.length() < maxLength)
		{
			value += String.format("%1$-" + (maxLength - value.length()) + "s", " ");
		}

		byte[] bytes;
		try
		{
			bytes = value.getBytes("ASCII");
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("Invalid encoding, only ASCII is supported", e);
			bytes = new byte[maxLength];
		}

		set(scope, name, FieldType.Text, bytes, 8 * maxLength);

	}

	@Override
	public void set(String scope, String name, int value, int bitLength)
	{
		byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
		set(scope, name, FieldType.Int, bytes, bitLength);
	}

	@Override
	public void set(String scope, String name, Date value)
	{
		long date = value.getTime();
		byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(date).array();
		set(scope, name, FieldType.Date, bytes, 6 * 8);
	}

	public void set(String scope, String name, FieldType type, byte[] value, int bitLength)
	{
		// Get field and create if it doesn't exist
		Field field = diagnostics.getField(scope, name);
		if (field == null)
		{
			field = diagnostics.addField(scope, name, type, bitLength);
			diagnostics.setVersion(diagnostics.getVersion() + 1);
			saveMetaData();
		}

		// Check if value has changed
		byte[] currentValue = field.getCurrentValue();
		if (currentValue != null && java.util.Arrays.equals(value, currentValue))
			return;
		field.setCurrentValue(value);

		// Grow Buffer if Required
		int minSize = (field.getOffset() + field.getLength() + 7) / 8;
		byte[] buffer;
		if (dataBuffer == null)
			buffer = new byte[minSize];
		else if (dataBuffer.length < minSize)
			buffer = java.util.Arrays.copyOf(dataBuffer, minSize);
		else
			buffer = dataBuffer.clone();

		for (int index = 0; bitLength > 0; index++)
		{
			int size = bitLength > 8 ? 8 : bitLength;
			set(buffer, field.getOffset() + 8 * index, value[index], size);
			bitLength -= size;
		}

		dataBuffer = buffer;

	}

	private void set(byte[] buffer, int offset, byte value, int bitCount)
	{
		int index = offset / 8;
		int startBit = offset % 8;
		int bitsToSet = bitCount;
		if (bitsToSet + startBit > 8)
			bitsToSet = 8 - startBit;

		byte current = buffer[index];
		byte mask = (byte) ((1 << bitsToSet) - 1);
		current &= ~(mask << startBit);
		current |= (value & mask) << startBit;
		buffer[index] = current;

		bitCount -= bitsToSet;
		if (bitCount > 0)
			set(buffer, offset + bitsToSet, (byte) (value >> bitsToSet), bitCount);
	}
}

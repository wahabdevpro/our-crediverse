package hxc.services.transactions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.database.IDatabaseConnection;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class TransactionService implements IService, ITransactionService, ITransactionInfoConfig
{
	final static Logger logger = LoggerFactory.getLogger(TransactionService.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Properties
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewCdrParameters", description = "View CDR Parameters", category = "CDR", supplier = true),
			@Perm(name = "ChangeCdrParameters", implies = "ViewCdrParameters", description = "Change CDR Parameters", category = "CDR", supplier = true) })
	public class TransactionServiceConfig extends ConfigurationBase
	{
		private String timeFormat = "yyyyMMdd'T'HHmmss";
		private String cdrDirectory = "/var/opt/cs/c4u/cdr";
		private int rotationIntervalSeconds = 7200;
		private String interimFileName = "cdr.tmp";
		private String rotatedNameFormat = "%1$s%2$s.cdr";

		@SupplierOnly
		public String getTimeFormat()
		{
			check(esb, "ViewCdrParameters");
			return timeFormat;
		}

		@SupplierOnly
		public void setTimeFormat(String timeFormat) throws ValidationException
		{
			check(esb, "ChangeCdrParameters");

			ValidationException.validateTimeFormat(timeFormat);
			this.timeFormat = timeFormat;
		}

		public String getCdrDirectory()
		{
			check(esb, "ViewCdrParameters");
			return cdrDirectory;
		}

		public void setCdrDirectory(String cdrDirectory)
		{
			check(esb, "ChangeCdrParameters");
			this.cdrDirectory = cdrDirectory;
		}

		public int getRotationIntervalSeconds()
		{
			check(esb, "ViewCdrParameters");
			return rotationIntervalSeconds;
		}

		public void setRotationIntervalSeconds(int rotationIntervalMinutes) throws ValidationException
		{
			check(esb, "ChangeCdrParameters");

			ValidationException.min(1, rotationIntervalMinutes);
			this.rotationIntervalSeconds = rotationIntervalMinutes;
		}

		public String getInterimFileName()
		{
			check(esb, "ViewCdrParameters");
			return interimFileName;
		}

		public void setInterimFileName(String interimFileName)
		{
			check(esb, "ChangeCdrParameters");
			this.interimFileName = interimFileName;
		}

		@SupplierOnly
		public String getRotatedNameFormat()
		{
			check(esb, "ViewCdrParameters");
			return rotatedNameFormat;
		}

		@SupplierOnly
		public void setRotatedNameFormat(String rotatedNameFormat) throws ValidationException
		{
			check(esb, "ChangeCdrParameters");

			ValidationException.validateFormat(rotatedNameFormat);
			this.rotatedNameFormat = rotatedNameFormat;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public String getName(String languageCode)
		{
			return "Transaction Service";
		}

		@Override
		public long getSerialVersionUID()
		{
			return 398665892L;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

	}

	// Configuration
	private TransactionServiceConfig config = new TransactionServiceConfig();

	private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(config.timeFormat);

	private String getInterimFilePath()
	{
		return config.cdrDirectory + "/" + config.interimFileName;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private Thread rotationThread;
	private boolean mustStop = false;
	@SuppressWarnings("unused")
	private boolean isRunning = false;
	private Object eventFlag = new Object();
	private FileOutputStream fileOutputStream;
	private Date nextRotationTime = new Date();
	private int rotationCounter = 0;
	private String hostName = "unknown";
	private Date lastWriteTime = new Date();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	public TransactionService()
	{
		hostName = HostInfo.getNameOrElseHxC();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Diagnostics
	//
	// /////////////////////////////////
	private ICdr lastCdr = null;

	@Override
	public ICdr getLastCdr()
	{
		return lastCdr;
	}

	@Override
	public void setLastCdr(ICdr lastCdr)
	{
		this.lastCdr = lastCdr;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
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
		rotationThread = new Thread()
		{
			@Override
			public void run()
			{
				rotationThread();
			}

		};
		mustStop = false;
		isRunning = false;

		rotationThread.start();
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}

		return true;
	}

	@Override
	public void stop()
	{
		mustStop = true;
		synchronized (eventFlag)
		{
			eventFlag.notifyAll();
		}
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) throws ValidationException
	{
		this.config = (TransactionServiceConfig) configuration;
		simpleTimeFormat = new SimpleDateFormat(config.timeFormat);
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return rotationThread != null && rotationThread.isAlive();
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ITransactionConfig Implementation
	//
	// /////////////////////////////////

	@Override
	public String getDirectory()
	{
		return config.cdrDirectory;
	}

	@Override
	public String getTimeFormat()
	{
		return config.timeFormat;
	}

	@Override
	public String getRotatedFilename()
	{
		return config.rotatedNameFormat;
	}

	@Override
	public String getInterimFilename()
	{
		return config.interimFileName;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// CDR File Rotation
	//
	// /////////////////////////////////
	public void rotationThread()
	{
		isRunning = false;

		try
		{
			// Create Directory
			File dictionary = new File(config.cdrDirectory).getCanonicalFile();
			if (!dictionary.exists() || !dictionary.isDirectory())
				dictionary.mkdirs();

			// Rotate file if it exists
			Rotate();

			// Open output stream
			fileOutputStream = new FileOutputStream(getInterimFilePath());

		}
		catch (IOException e1)
		{
			logger.error(e1.getMessage(), e1);
			return;
		}

		try
		{
			isRunning = true;

			// Loop
			while (true)
			{
				// Wait for a signal
				synchronized (eventFlag)
				{
					if (config.rotationIntervalSeconds <= 60)
						eventFlag.wait(1000);
					else
						eventFlag.wait(60000);
				}

				// Exit if Required
				if (mustStop)
				{
					fileOutputStream.close();
					fileOutputStream = null;
					Rotate();
					return;
				}

				// Rotate if Required
				Date now = new Date();
				if (!nextRotationTime.after(now))
				{
					fileOutputStream.close();
					fileOutputStream = null;
					Rotate();
					fileOutputStream = new FileOutputStream(getInterimFilePath());
				}
			}
		}
		catch (IOException | InterruptedException ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		finally
		{
			isRunning = false;
		}

	}

	@SuppressWarnings("deprecation")
	private void Rotate()
	{
		// If the interim file exists
		File iterimFile = new File(getInterimFilePath());
		if (iterimFile.exists())
		{
			// Compose new name
			rotationCounter++;
			Date now = new Date();
			String rotatedFileName = String.format(config.rotatedNameFormat, hostName, simpleTimeFormat.format(lastWriteTime), simpleTimeFormat.format(now), rotationCounter);

			// Rename the file
			File rotatedFile = new File(config.cdrDirectory + "/" + rotatedFileName);
			iterimFile.renameTo(rotatedFile);

			lastWriteTime = now;
		}

		// Calculate next rotation time
		int rotationIntervalSeconds = config.rotationIntervalSeconds;
		nextRotationTime = new Date(nextRotationTime.getTime() + rotationIntervalSeconds * 1000L);
		if (rotationIntervalSeconds >= 3600 && rotationIntervalSeconds % 3600 == 0)
		{
			nextRotationTime.setSeconds(0);
			nextRotationTime.setMinutes(0);
		}

	}

    public boolean getSkipArchival()
    {
        return false;
    }

    public Integer getArchiveAfterDays()
    {
        return null;
    }

    public Integer getDeleteAfterDays()
    {
        return null;
    }

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ITransaction Implementation
	//
	// /////////////////////////////////

	@Override
	public FileOutputStream getFileOutputStream()
	{
		return fileOutputStream;
	}

	@Override
	public <T extends ICdr> Transaction<?> create(T cdr, IDatabaseConnection database)
	{
		cdr.setTransactionID(esb.getNextTransactionNumber(20));
		return new Transaction<T>(this, cdr, database);
	}
}

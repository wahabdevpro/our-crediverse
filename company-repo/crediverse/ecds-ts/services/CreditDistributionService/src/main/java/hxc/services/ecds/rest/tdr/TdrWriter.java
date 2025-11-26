package hxc.services.ecds.rest.tdr;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.utils.calendar.DateTime;

public class TdrWriter extends TdrWriterBase implements Runnable, AutoCloseable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final ReentrantLock cleanupLock = new ReentrantLock();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TdrWriter(ICreditDistribution context)
	{
		super(context);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		for (Entry<Integer, TdrFile> entry : tdrFiles.entrySet())
		{
			TdrFile tdrFile = entry.getValue();
			if (tdrFile == null)
				continue;

			// Get the Transactions Config
			TransactionsConfig config = getConfig(tdrFile.companyID);
			if (config == null)
				continue;

			// Test if too long or too old
			long position = 0;
			try
			{
				position = tdrFile.outputStream.getChannel().position();
				boolean tooLong = position > config.getTdrMaxFileLengthBytes();
				long age = System.currentTimeMillis() - tdrFile.openTime;
				int maxAge = config.getTdrRotationIntervalSeconds() * 1000;
				boolean tooOld = age >= maxAge;
				if (tooLong || tooOld)
				{
					tdrFiles.remove(entry.getKey());

					// Wait for writing to complete
					synchronized (tdrFile)
					{
						tdrFile.outputStream.close();
						tdrFile.outputStream = null;
					}

					if (!cleanupLock.tryLock(1, TimeUnit.SECONDS))
						return; // Cleanup already in progress
					try
					{
						cleanup(config, tdrFile);
					}
					finally
					{
						cleanupLock.unlock();
					}
				}
			}
			catch (Exception e)
			{
				logger.warn("error cleaning up", e);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Zip & Cleanup
	//
	// /////////////////////////////////

	// Cleanup TDR's
	private void cleanup(TransactionsConfig config, TdrFile tdrFile)
	{
		// Get TDR Directory
		int companyID = tdrFile.companyID;
		File tdrDirectory = new File(expand(config.getTdrDirectory(), companyID, (Date) null, null));
		tdrDirectory.mkdirs();

		// Zip and Delete old TDR's and send them to MM
		zipFiles(config.getTdrFilenameFormat(), tdrDirectory, companyID, //
				config.getZipCopyCommand(), config.getZipFilenameFormat(), config.getZipTdrAfterDays());

		// Delete old Tdrs and Zips
		deleteOldFiles(tdrDirectory, config.getTdrFilenameFormat(), config.getDeleteZipAfterDays(), companyID);
		deleteOldFiles(tdrDirectory, config.getZipFilenameFormat(), config.getDeleteZipAfterDays(), companyID);

	}

	private void zipFiles(String tdrFilenameFormat, File tdrDirectory, int companyID, //
			String zipCopyCommand, String zipFilenameFormat, int zipTdrAfterDays)
	{
		// Get TDR files eligible for Zipping
		DateTime now = DateTime.getNow();
		DateTime beforeDate = now.addDays(-zipTdrAfterDays);
		File[] files = getFiles(tdrDirectory, tdrFilenameFormat, companyID, beforeDate);
		if (files == null || files.length == 0)
			return;

		// Create file
		String zipFileName = expand(zipFilenameFormat, companyID, now, null);
		File zipFile = new File(tdrDirectory, zipFileName);
		String zipPath = zipFile.getAbsolutePath();

		// Zip files into it
		try (ZipOutputStream zipper = new ZipOutputStream(new FileOutputStream(zipPath)))
		{
			zipper.setLevel(9);

			MappedByteBuffer buffer;
			for (File file : files)
			{
				// Get the file channel of the file
				try (FileChannel fc = new FileInputStream(file).getChannel())
				{
					// Create a buffer from the file channel
					buffer = fc.map(MapMode.READ_ONLY, 0, fc.size());
					zipper.putNextEntry(new ZipEntry(file.getName()));
					byte b[] = new byte[buffer.capacity()];
					while (buffer.hasRemaining())
					{
						buffer.get(b);
						try
						{
							zipper.write(b);
						}
						catch (IOException e)
						{
							break;
						}
					}
					zipper.closeEntry();
					buffer.clear();
				}
			}

		}
		catch (IOException e)
		{
			logger.error("zipping error", e);
			zipFile.delete();
			return;
		}

		// Delete TDRs
		for (File file : files)
		{
			try
			{
				file.delete();
			}
			catch (Throwable tr)
			{
				logger.error("error deleting TDR's after zipping.", tr);
			}
		}
	}

	private void deleteOldFiles(File tdrDirectory, String filenameFormat, int deleteZipAfterDays, int companyID)
	{
		// Get List of Files
		DateTime now = DateTime.getNow();
		DateTime beforeDate = now.addDays(-deleteZipAfterDays);
		File[] files = getFiles(tdrDirectory, filenameFormat, companyID, beforeDate);
		if (files == null || files.length == 0)
			return;

		// Delete Files
		for (File file : files)
		{
			try
			{
				file.delete();
			}
			catch (Throwable tr)
			{
				logger.error("Error deleting old files", tr);
			}
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Get files matching a format older than a specified date
	private File[] getFiles(File directory, String fileFormat, int companyID, final Date beforeDate)
	{
		// Create a regex for fileFormat
		fileFormat = expand(fileFormat.replaceAll("\\.", "\\\\."), companyID, "\\d{8}T\\d{6}", null);
		final Pattern pattern = Pattern.compile(fileFormat, Pattern.CASE_INSENSITIVE);

		// Filter files
		File files[] = directory.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				if (beforeDate != null)
				{
					Date lastModified = new Date(pathname.lastModified());
					if (lastModified.after(beforeDate))
						return false;
				}

				String name = pathname.getName();
				return pattern.matcher(name).matches();
			}
		});

		return files;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// AutoCloseable
	//
	// /////////////////////////////////
	@Override
	public void close()
	{
		Enumeration<Integer> keys = tdrFiles.keys();
		while (keys.hasMoreElements())
		{
			try
			{
				Integer companyID = keys.nextElement();
				TdrFile tdrFile = tdrFiles.remove(companyID);
				if (tdrFile != null)
				{
					synchronized (tdrFile)
					{
						if (tdrFile.outputStream != null)
							tdrFile.outputStream.close();
					}
					cleanup(getConfig(tdrFile.companyID), tdrFile);
				}
				keys = tdrFiles.keys();
			}
			catch (IOException e)
			{
			}
		}
	}
}

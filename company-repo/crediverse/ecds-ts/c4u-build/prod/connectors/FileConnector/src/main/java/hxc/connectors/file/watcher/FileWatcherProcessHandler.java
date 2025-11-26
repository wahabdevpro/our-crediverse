package hxc.connectors.file.watcher;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import hxc.connectors.file.ConfigRecord;
import hxc.connectors.file.FileConnector.FileProcessHandler;
import hxc.connectors.file.FileProcessorType;
import hxc.connectors.file.IFileProcessorHandler;
import hxc.connectors.file.process.FileProcessor;
import hxc.connectors.file.process.FileProcessorFactory;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;
import hxc.utils.watcher.IFileWatcherProcessHandler;

public class FileWatcherProcessHandler implements IFileWatcherProcessHandler
{
	private FileProcessor processor;
	private HashMap<String, ConfigRecord> records;
	private IFileWatcherMediator mediator;
	private IFileProcessorHandler handler;
	private List<File> processLaterFiles = new LinkedList<File>();
	private TimedThread processLater = new TimedThread("File Watcher Process Hanlder", 60000, TimedThreadType.INTERVAL)
	{

		@Override
		public void action()
		{
			try
			{
				// Process any files that couldn't be processed at a certain time
				Iterator<File> iterator = processLaterFiles.iterator();
				while (iterator.hasNext())
				{
					File file = iterator.next();
					if (process(file))
						iterator.remove();
				}
			}
			catch (Exception e)
			{

			}
		}

	};

	// Constructor for the file processor
	public FileWatcherProcessHandler(FileProcessorType type, HashMap<String, ConfigRecord> records, IFileWatcherMediator mediator, IFileProcessorHandler handler)
	{
		processor = FileProcessorFactory.buildFileProcessor(type);
		this.records = records;
		this.mediator = mediator;
		this.handler = handler;

		this.processLater.start();
	}

	// Process a new file
	@Override
	public void processNewFile(File file)
	{
		if (!process(file))
		{
			processLaterFiles.add(file);
		}
	}

	@Override
	public void processDeletedFile(File file)
	{
	}

	public boolean process(File file)
	{
		if (file == null)
			return true;

		// Use the mediator to check if the file has already been processed
		if (mediator != null && mediator.isCompleted(file))
			return true;

		// Check if the file matches any of the records
		ConfigRecord record = matchesRecord(file.getName());
		if (record == null)
			return true;

		// Get the start time and end time of the processing
		Date startTime = setToCurrentEra(record.getProcessStartTimeOfDay());
		Date endTime = setToCurrentEra(record.getProcessEndTimeOfDay());

		Date now = new Date();

		// Check whether it is time to process the file or not
		if (startTime == null || endTime == null || //
				(startTime.before(endTime) && now.after(startTime) && now.before(endTime)) || //
				(startTime.after(endTime) && !(now.before(startTime) && now.after(endTime))))
		{
			long recordNum = 0;

			// Distribute the file
			if (mediator != null)
			{
				mediator.distribute(file, record.getCopyCommand());
				recordNum = mediator.lastRecord(file);
			}

			// Process the file
			try
			{
				((FileProcessHandler) handler).setOutputDir(record.getOutputDirectory());
				processor.setHandler(handler);
				processor.process(file, record.getFileType().toString(), recordNum);

				return true;
			}
			catch (Exception e)
			{
			}
		}
		return false;
	}

	private ConfigRecord matchesRecord(String filename)
	{
		// Iterate through the records
		for (String regex : records.keySet())
		{
			// Check if the file matches the regex
			if (filename.matches(regex))
			{
				return records.get(regex);
			}
		}
		return null;
	}

	// Set the date to today except for the time
	private Date setToCurrentEra(Date d)
	{
		if (d == null)
			return null;

		Calendar cal = Calendar.getInstance();
		cal.setTime(d);

		Calendar now = Calendar.getInstance();
		cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));

		return cal.getTime();
	}

}

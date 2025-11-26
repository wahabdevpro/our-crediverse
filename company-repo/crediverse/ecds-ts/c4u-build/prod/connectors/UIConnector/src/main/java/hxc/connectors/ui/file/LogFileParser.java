package hxc.connectors.ui.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import hxc.services.logging.LoggingLevels;
import hxc.utils.protocol.uiconnector.logtailer.LogDTO;
import hxc.utils.protocol.uiconnector.logtailer.LogFilterOptions;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileParser
{
	final static Logger logger = LoggerFactory.getLogger(LogFileParser.class);
	private ClassInfo classInfo;
	private LinkedHashMap<String, FieldInfo> fields;

	private String dateFormat = null;
	private LogFilterOptions logFilter = null;
	private char delimiter = '|';
	private SimpleDateFormat dateFormater;
	private int maxRecordsReturned = 1000;
	private int lastPosition = 0;

	private LinkedList<LogDTO> logEntries = null;

	public LogFileParser(String dateFormat, LogFilterOptions logFilter, int maxRecordsReturned)
	{
		this.dateFormat = dateFormat;
		this.logFilter = logFilter;
		this.dateFormater = new SimpleDateFormat(dateFormat);
		this.maxRecordsReturned = maxRecordsReturned;
	}

	public LinkedList<LogDTO> parseFile(File file, String hostName, int fromPosition)
	{
		classInfo = ReflectionHelper.getClassInfo(LogDTO.class);
		fields = classInfo.getFields();
		lastPosition = fromPosition;

		// if start date not null then read in the last line to see if worth processing the whole file
		if (logFilter.getStartDate() != null)
		{
			String line = tail(file);

			if (parseLine(line, hostName, true) == null)
			{
				return null;
			}
		}

		try (FileInputStream stream = new FileInputStream(file))
		{
			try (FileChannel fc = stream.getChannel())
			{
				long size = fc.size();
				if (fromPosition > size)
				{
					fromPosition = 0;
				}
				if (fromPosition < size)
				{
					MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, lastPosition, size - lastPosition);
					byte data[] = new byte[(int) (size - lastPosition)];
					buffer.get(data);
					lastPosition = buffer.position() + lastPosition;
					parseData(data, hostName);
				}
				else
				{
					// Nothing to report
					logEntries = new LinkedList<LogDTO>();
					lastPosition = fromPosition;
				}

			}
		}
		catch (Exception exc)
		{
			logger.error("", exc);
			// logger.error(this, "Could not record: {}", exc.getMessage());
		}

		return logEntries;
	}

	private void parseData(byte[] data, String hostName)
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data))))
		{
			logEntries = new LinkedList<LogDTO>();

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (line.length() > 10)
				{
					LogDTO dto = parseLine(line, hostName, false);
					if (dto != null)
					{
						logEntries.add(dto);
						if (logEntries.size() > maxRecordsReturned)
						{
							logEntries.remove(0);
						}
					}
				}

			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Read the last line from a file (used to determine if there is anything of interest to process)
	 * 
	 * @param file
	 * @return
	 */
	private String tail(File file)
	{
		RandomAccessFile fileHandler = null;
		try
		{
			fileHandler = new RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--)
			{
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA)
				{
					if (filePointer == fileLength)
					{
						continue;
					}
					break;

				}
				else if (readByte == 0xD)
				{
					if (filePointer == fileLength - 1)
					{
						continue;
					}
					break;
				}

				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		}
		catch (java.io.FileNotFoundException e)
		{
			return null;
		}
		catch (java.io.IOException e)
		{
			return null;
		}
		finally
		{
			if (fileHandler != null)
				try
				{
					fileHandler.close();
				}
				catch (IOException e)
				{
					/* ignore */
				}
		}
	}

	private LogDTO parseLine(String line, String hostName, boolean ignoreSeverity)
	{
		if (line == null || (line.length() < dateFormat.length()))
			return null;

		boolean keepRecord = (logFilter.getText() == null || logFilter.getText().length() == 0) ? true : (line.toLowerCase().indexOf(logFilter.getText().toLowerCase()) >= 0);
		if (!keepRecord)
			return null;
		String remaining = line;

		LogDTO result = null;
		try
		{
			loop: for (int fieldNo = 0; fieldNo < fields.values().size(); fieldNo++)
			{
				int index = remaining.indexOf(delimiter);
				if (index < 0 && (fieldNo == 0))
					return null;
				String record = ((index >= 0) ? (remaining.substring(0, index)) : remaining.substring(0)).trim();
				remaining = remaining.substring(index + 1);

				switch (fieldNo)
				{
					case 0:
						// Check Date and posible text
						try
						{
							Date recordTime = dateFormater.parse(record);
							Calendar cal = Calendar.getInstance();
							cal.setTime(recordTime);

							if ((logFilter.getStartDate() != null) && (recordTime.getTime() < logFilter.getStartDate().getTime())
									|| ((logFilter.getEndDate() != null) && (recordTime.getTime() > logFilter.getEndDate().getTime())))
								break loop; // ignore line on date
							else
							{
								result = new LogDTO();
								result.recordTime = record;
							}
						}
						catch (Exception e)
						{
							break loop; // ignore line on date
						}

						break;
					case 1:
						result.severity = record;
						try
						{
							// loggingLevels
							if (!ignoreSeverity)
							{
								LoggingLevels loggingLevel = LoggingLevels.valueOf(record);
								if (logFilter.getLoggingLevels() != null)
								{
									boolean containsLevel = false;
									for (LoggingLevels ll : logFilter.getLoggingLevels())
									{
										if (ll.equals(loggingLevel))
										{
											containsLevel = true;
											break;
										}
									}
									if (!containsLevel)
									{
										result = null;
										break loop;
									}
								}
							}
						}
						catch (Exception ex)
						{
						}
						break;
					case 2:
						if (logFilter.isOnlyNonBlankTID() && (record == null || record.length() == 0))
						{
							result = null;
							break loop;
						}
						result.transactionID = record;
						break;
					case 3:
						result.component = record;
						break;
					case 4:
						result.operation = record;
						break;
					case 5:
						result.returnCode = Integer.parseInt(record);
						break;
					case 6:
						result.text = record;
						break;
				}
			}

			if (result != null)
				result.host = hostName;

		}
		catch (Exception e)
		{
		}

		return result;
	}

	public int getLastPosition()
	{
		return lastPosition;
	}

}

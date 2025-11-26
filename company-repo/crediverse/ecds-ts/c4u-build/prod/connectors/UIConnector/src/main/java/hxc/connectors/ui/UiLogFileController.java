package hxc.connectors.ui;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.servicebus.IServiceBus;
import hxc.services.logging.LoggerService;
import hxc.utils.protocol.uiconnector.logtailer.LogDTO;
import hxc.utils.protocol.uiconnector.logtailer.LogFileRequest;
import hxc.utils.protocol.uiconnector.logtailer.LogFileResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class UiLogFileController
{
	final static Logger logger = LoggerFactory.getLogger(UiLogFileController.class);
	
	public UiLogFileController(IServiceBus esb, UiSessionManager sessionManager)
	{
	}

	public UiBaseResponse processLogFileTail(LogFileRequest logFileRequest)
	{
		return filterLogFileDetails(logFileRequest);
	}

	/**
	 * Note that hosts is NOT part of the request it is assummed that you are at the correct host
	 * 
	 * @param logFileRequest
	 * @return
	 */
	public UiBaseResponse filterLogFileDetails(LogFileRequest logFileRequest)
	{
		final String [] FILE_HEADER = {"recordTime","severity","transactionID","component","operation","returncode","host","text"};
		LogFileResponse response = new LogFileResponse(logFileRequest.getUserId(), logFileRequest.getSessionId());
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

		try
		{

			// Find log file path
			// String hosts[] = logFileRequest.getHost().split(",");

			// FIXME - Search plugins till Ilogger found
			LoggerService loggerService = LoggerService.getService();

			if (loggerService != null)
			{
				
				String history = loggerService.getLogHistory();
				StringReader reader = new StringReader(history);
				CSVParser csvFileParser = new CSVParser(reader, csvFileFormat);
				List<LogDTO> retrievedEntries = new ArrayList<LogDTO>();
				
				try
				{
					List<CSVRecord> csvRecords = csvFileParser.getRecords();
					for (int i = 1; i < csvRecords.size(); i++)
					{
						LogDTO currentLogDTO = new LogDTO();
						CSVRecord record = csvRecords.get(i);
						currentLogDTO.recordTime = record.get("recordTime");
						currentLogDTO.severity = record.get("severity");
						currentLogDTO.transactionID = record.get("transactionID");
						currentLogDTO.component = record.get("component");
						currentLogDTO.operation = record.get("operation");
						
						try
						{
							currentLogDTO.returnCode = Integer.parseInt(record.get("returncode"));
						}
						catch(Exception ex)
						{
							currentLogDTO.returnCode = 0;
						}
						currentLogDTO.text = record.get("text");
						currentLogDTO.host = record.get("host");
						retrievedEntries.add(currentLogDTO);
					}
					response.getLogRecords().addAll(retrievedEntries);
				}
				catch(Exception e)
				{
					logger.error("Error in CsvFileReader !!!", e);
				}
				finally
				{
					try {
						reader.close();
						csvFileParser.close();
					}
					catch (IOException e)
					{
						logger.error("Error while closing reader/csvFileParser !!!", e);
					}
				}
				
			}

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return response;
	}
}

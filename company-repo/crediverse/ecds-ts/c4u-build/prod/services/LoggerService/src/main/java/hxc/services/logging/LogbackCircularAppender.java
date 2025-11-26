package hxc.services.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/*
 * In order to provide a view of the 1000 most recent log entries, 
 * a synchronized circular ring buffer is used.  This implementation 
 * is used as it has constant time for insertions and deletions.
 */
public class LogbackCircularAppender extends AppenderBase<ILoggingEvent>
{
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final Object [] FILE_HEADER = {"recordTime","severity","transactionID","component","operation","returncode","host","text"};
	private Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(1000));
	private SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");

	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
	}
	
	private String formatLogEntry(ILoggingEvent event)
	{
		CSVPrinter csvFilePrinter = null;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		StringBuilder logEntry = new StringBuilder();
		
		/*
		 * 		MDC.put("origin", origin.toString());
		MDC.put("transid", transactionID);
		MDC.put("fatal", fatal);
		MDC.put("timeformat", config.timeFormat);
		
		MDC.put("host", this.hostname);
		 */
		
		try {
			csvFilePrinter = new CSVPrinter(logEntry, csvFileFormat);
			//csvFilePrinter.printRecord(FILE_HEADER);
			
			Map<String, String> contextData = event.getMDCPropertyMap();
			List<String> dataRecord = new ArrayList<String>();
			//yyyyMMdd'T'HHmmss.SSS
			String entryDate = simpleTimeFormat.format(new java.util.Date (event.getTimeStamp()));
			dataRecord.add(entryDate);
			dataRecord.add(event.getLevel().toString());
			dataRecord.add(contextData.get("transid"));
			dataRecord.add(event.getLoggerName());
			//dataRecord.add(event.getThreadName());
			dataRecord.add("");
			dataRecord.add(contextData.get("returncode"));
			dataRecord.add(contextData.get("host"));
			dataRecord.add(event.getFormattedMessage());

			
			csvFilePrinter.printRecord(dataRecord);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logEntry.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void append(ILoggingEvent event)
	{
		fifo.add(formatLogEntry(event));
		//System.err.println("fifoEventevent:: "+event.getFormattedMessage());
		//System.err.println("fifoEventsize:: "+fifo.size());
	}

	// 1511341347332INFOhxc.services.logging.LoggerServiceScheduledServiceBusThreadPool-8run ended
	// 1511341352794DEBUGcom.mchange.v2.async.ThreadPoolAsynchronousRunnerC3P0PooledConnectionPoolManager[identityToken->z8kfsx9r1qypxa11tps2je|6c2e7591]-AdminTaskTimercom.mchange.v2.async.ThreadPoolAsynchronousRunner$DeadlockDetector@23b641ce -- Running DeadlockDetector[Exiting. No pending tasks.]

	@SuppressWarnings("unchecked")
	protected String getLogHistory()
	{
		CSVPrinter csvFilePrinter = null;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		StringBuilder logData = new StringBuilder();
		String[] history = (String[])fifo.toArray(new String[fifo.size()]);
		
		try {
			csvFilePrinter = new CSVPrinter(logData, csvFileFormat);
			csvFilePrinter.printRecord(FILE_HEADER);
			
			for (String currentRecord : history)
			{
				logData.append(currentRecord);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return logData.toString();
	}

	public void setTimeFormat(String simpleTimeFormat)
	{
		this.simpleTimeFormat = new SimpleDateFormat(simpleTimeFormat);
	}
	
	
}

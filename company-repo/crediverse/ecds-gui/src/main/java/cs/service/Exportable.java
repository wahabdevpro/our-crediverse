package cs.service;

import java.io.OutputStream;
import java.nio.charset.Charset;

import cs.controller.AgentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiCsvResponse;
import cs.service.interfaces.IExportable;
import cs.service.interfaces.IQueryStringParameters;
import cs.utility.BatchUtility;

public abstract class Exportable implements IExportable
{

	private static Logger logger = LoggerFactory.getLogger(AgentController.class);

	@Autowired
	protected CorrelationIdService correlationIdService;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Overloaded to accept filter arguments and pass on down the change
	 */
	public void csvExport(String uniqid, OutputStream outputStream, String search, int chunkSize, long total, boolean docount, IQueryStringParameters queryStringParams, String filter) throws Exception
	{
		logger.info("CSV Exporting called with the following params: search {}, queryStringParams {}, filter {}", search, queryStringParams, filter);
		csvExport(uniqid, outputStream, filter, search, chunkSize, total, docount, queryStringParams);
	}

	public void csvExport(String uniqid, OutputStream outputStream, String search, int chunkSize, long total, boolean docount, IQueryStringParameters queryStringParams) throws Exception
	{
		csvExport(uniqid, outputStream, null, search, chunkSize, total, docount, queryStringParams);
	}

	public byte[] getBytes(String csvData, boolean skipHeaders)
	{
		if (skipHeaders)
		{
			String truncatedData = csvData.substring(csvData.indexOf('\n') + 1, csvData.length());
			return truncatedData.getBytes(Charset.forName("UTF-8"));
		}

		return csvData.getBytes(Charset.forName("UTF-8"));
	}

	public void csvExport(String uniqid, OutputStream outputStream, String filter, String search, int chunkSize, long total, boolean docount, IQueryStringParameters queryStringParams) throws Exception
	{
		long runningTotal = 0;
		try
		{
			String csvResponse = this.listAsCsv(filter, search, 0, 0, queryStringParams);
			String headerrow = csvResponse.substring(0, csvResponse.length() > 20 ? 20 : csvResponse.length() - 1);
			outputStream.write(csvResponse.getBytes(Charset.forName("UTF-8")));
			outputStream.flush();
			if (docount)
			{
				while (runningTotal < total)
				{
					String outputResult = this.listAsCsv(filter, search, runningTotal, chunkSize, queryStringParams);
					outputStream.write(getBytes(outputResult, outputResult.startsWith(headerrow)));
					runningTotal+=chunkSize;
				}
			}
			else
			{
				chunkSize = BatchUtility.getRecordsPerChunk();
				boolean moreData = true;
				while (moreData)
				{
					String outputResult = this.listAsCsv(filter, search, runningTotal, chunkSize, queryStringParams);
					if (outputResult != null)
					{
						outputStream.write(getBytes(outputResult, outputResult.startsWith(headerrow)));
						moreData = (outputResult.trim().length() > 0);
						runningTotal+=chunkSize;
					}
					else
					{
						moreData = false;
					}
				}
			}
			outputStream.flush();
			outputStream.close();
			correlationIdService.clearTrackingId(uniqid);
		}
		catch(Exception e)
		{
			logger.error("", e);
			correlationIdService.clearTrackingId(uniqid);
			throw e;
		}
	}

	public void csvExport(UriComponentsBuilder uri, String uniqid, OutputStream outputStream, String search, IQueryStringParameters queryStringParams) throws Exception
	{
		csvExport(uri, uniqid, outputStream, null, search, queryStringParams);
	}

	public void csvExport(UriComponentsBuilder uri, String uniqid, OutputStream outputStream, String filter, String search, IQueryStringParameters queryStringParams) throws Exception
	{
		long runningTotal = 0;
		try
		{
			GuiCsvResponse csvResponse = this.listAsCsv(uri, filter, search, 0, 0, queryStringParams);
			String headerrow = csvResponse.getCsvData().substring(0, 20);
			outputStream.write(csvResponse.getBytes());
			outputStream.flush();
			//runningTotal += 1;

			int recordCount = BatchUtility.getRecordsPerChunk();
			while (recordCount == BatchUtility.getRecordsPerChunk())
			{
				csvResponse = this.listAsCsv(uri, filter, search, runningTotal, queryStringParams);
				runningTotal += csvResponse.getRecordCount();
				if (csvResponse.getRecordCount() > 0) outputStream.write(csvResponse.getBytes(csvResponse.getCsvData().startsWith(headerrow)));
				recordCount = csvResponse.getRecordCount();
				// Skipping Headers?
			}

			outputStream.flush();
			outputStream.close();
			correlationIdService.clearTrackingId(uniqid);
		}
		catch(Exception e)
		{
			correlationIdService.clearTrackingId(uniqid);
			logger.error("", e);
			throw e;
		}
	}

	public void clearTracking(String uniqid)
	{
		correlationIdService.clearTrackingId(uniqid);
	}

	public ObjectNode track(ObjectNode response)
	{
		response.put("uniqid", correlationIdService.getTrackingId());
		return response;
	}

	public ObjectNode track(long count)
	{
		ObjectNode response = mapper.createObjectNode();
		response.put("count", count);

		return track(response);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public String listAsCsv(String filter, String search, boolean withQuery, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, IQueryStringParameters queryStringParams) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		throw new UnsupportedOperationException();
	}
}

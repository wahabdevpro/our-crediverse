package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiBatch;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.BatchService;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.FilterBuilderUtils;
import hxc.ecds.protocol.rest.Batch;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("/api/batch")
public class BatchController
{
	private static Logger logger = LoggerFactory.getLogger(BatchController.class);

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private BatchService batchService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	private String tsSortFromDtSort( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "id": order += "id" + dir; break;
			case "filename": order += "filename" + dir; break;
			case "fileSize": order += "fileSize" + dir; break;
			case "timestamp": order += "timestamp" + dir; break;
			case "type": order += "type" + dir; break;
			case "lineCount": order += "lineCount" + dir; break;
			case "failureCount": order += "failureCount" + dir; break;
			case "totalValue": order += "totalValue" + dir; break;
			case "webUserName": order += "webUserID" + dir; break;
			case "coAuthWebUserName": order += "coAuthWebUserID" + dir; break;
			}
		}
		return order;
	}

	private String addFilter(String filter, String name, String operator, String value)
	{
		if ( !value.equals("") )
		{
			if ( !filter.equals("") )
				filter += "+";
			filter += name + operator + "'" + value + "'";
		}
		return filter;
	}

	private String compileFilter(Map<String, String> params, List<Violation> violations) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "id" ) )
			filter = addFilter( filter, "id", "=", params.get( "id" ).trim() );
		if ( params.containsKey( "type" ) )
			filter = addFilter( filter, "type", "=", params.get( "type" ) );
		if ( params.containsKey( "filename" ) )
			filter = addFilter( filter, "filename", ":", params.get( "filename" ).trim().replaceFirst("[*]$", "%") );

		Date dateFrom = null;
		Date dateTo = null;

		if ( params.containsKey( "dateFrom" ) )
		{
			dateFrom = FilterBuilderUtils.getDate(violations, params.get("dateFrom").trim(), "dateFrom", dateFrom);
			if ( dateFrom != null )
			{
				String dateFromStr = params.get( "dateFrom" );
				dateFromStr = dateFromStr.replace( "-", "" );
				dateFromStr += "T000000";
				filter = addFilter( filter, "timestamp", ">=", dateFromStr );
			}
		}
		if ( params.containsKey( "dateTo" ) )
		{
			dateTo = FilterBuilderUtils.getDate(violations, params.get("dateTo").trim(), "dateTo", dateFrom);
			if ( dateTo != null )
			{
				String dateToStr = params.get( "dateTo" );
				dateToStr = dateToStr.replace( "-", "" );
				dateToStr += "T235959";
				filter = addFilter( filter, "timestamp", "<=", dateToStr );
			}
		}

		return filter;
	}

	@RequestMapping(value="{batchID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiBatch getBatch(@PathVariable("batchID") String batchID) throws Exception
	{
		Batch batch = batchService.getBatch(batchID);
		GuiBatch guiBatch = typeConvertorService.getGuiBatchFromBatch(batch);
		return guiBatch;
	}

	@RequestMapping(value = "download/csv/{batchId}", method = RequestMethod.GET)
	@ResponseBody
	public void getSampleHeader(@PathVariable("batchId") Integer batchId, HttpServletResponse response) throws Exception
	{
		String outputResult = batchService.getCsv(response, batchId);

		OutputStream outputStream = response.getOutputStream();
		outputStream.write(outputResult.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable search(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		Batch[] batches = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long batchCount = batchService.listBatchesCount(filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue());
			batches = batchService.listBatches(filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr));
			return new GuiDataTable(typeConvertorService.getGuiBatchFromBatch(batches), batchCount == null ? dtr.getStart() + ((batches.length < dtr.getLength()) ? batches.length : (dtr.getLength() * 2)) : batchCount.intValue());
		}

		batches = batchService.listBatches(filter, null, null, null, tsSortFromDtSort(dtr));
		return new GuiDataTable(typeConvertorService.getGuiBatchFromBatch(batches));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;

		count = batchService.listBatchesCount(filter, dtr.getSearch().getValue());

		ObjectNode response = mapper.createObjectNode();
		response.put("count", count);
		return response;
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "batchhistory", ".csv"));

		long itemCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		itemCount = batchService.listBatchesCount(null, search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(itemCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		long runningTotal = 0;
		try
		{
			OutputStream outputStream = response.getOutputStream();
			while (runningTotal < itemCount)
			{
				String outputResult = batchService.listBatchesAsCsv(null, search, runningTotal, recordsPerChunk);
				outputStream.write(outputResult.getBytes());
				runningTotal+=recordsPerChunk;
			}
			outputStream.flush();
			outputStream.close();
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Batch[] serverList() throws Exception
	{
		Batch[] batch = batchService.listBatches();
		return batch;
	}
}


package cs.controller;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiAuditEntry;
//import cs.dto.GuiTransaction;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.AuditLogService;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.FilterBuilderUtils;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.AuditEntry;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("/api/auditlog")
public class AuditLogController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired //ask @Configuration-marked class for this
	private AuditLogService auditLogService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private LoginSessionData sessionData;

	private String compileFilter(Map<String, String> params, List<Violation> violations) throws Exception
	{
		String filter = "";

		if ( params.containsKey( "id" ) )
		{
			String id = params.get( "id" ).trim();
			if ( !id.matches("[0-9]+") )
				violations.add(new Violation(Violation.INVALID_VALUE, "id", null, "Must be numeric"));
			else
				filter = addFilter( filter, "id", "=", id );
		}
		if ( params.containsKey( "sequenceNo" ) )
		{
			String seqNo = params.get( "sequenceNo" ).trim().replaceFirst("[*]$", "%");
			filter = addFilter( filter, "sequenceNo", ":", seqNo );
		}
		if ( params.containsKey( "webUserID" ) )
			filter = addFilter( filter, "webUserID", "=", params.get( "webUserID" ) );
		if ( params.containsKey( "action" ) )
		{
			switch(params.get( "action" ))
			{
			case AuditEntry.ACTION_CREATE:
			case AuditEntry.ACTION_UPDATE:
			case AuditEntry.ACTION_DELETE:
				filter = addFilter( filter, "action", "=", params.get( "action" ) );
				break;
			default:
				violations.add(new Violation(Violation.INVALID_VALUE, "action", null, "Invalid action"));
				break;
			}
		}

		Date tsFrom = null;
		Date tsTo = null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		if ( params.containsKey( "timestampFrom" ) )
		{
			String tsFromStr = params.get("timestampFrom").trim();
			tsFrom = FilterBuilderUtils.getDate(violations, tsFromStr, "timestampFrom", tsFrom);
			if ( tsFrom != null )
			{
				tsFrom.setHours(0);
				tsFrom.setMinutes(0);
				tsFrom.setSeconds(0);
				filter = addFilter( filter, "timestamp", ">=", sdf.format(tsFrom) );
			}
		}
		if ( params.containsKey( "timestampTo" ) )
		{
			String tsToStr = params.get("timestampTo").trim();
			tsTo = FilterBuilderUtils.getDate(violations, tsToStr, "timestampTo", tsFrom);
			if ( tsTo != null )
			{
				tsTo.setHours(23);
				tsTo.setMinutes(59);
				tsTo.setSeconds(59);
				filter = addFilter( filter, "timestamp", "<=", sdf.format(tsTo) );
			}
		}

		if ( params.containsKey( "ipAddress" ) )
		{
			String ipAddr = params.get( "ipAddress" ).trim().replaceFirst("[*]$", "%");
			filter = addFilter( filter, "ipAddress", ":", ipAddr );
		}
		if ( params.containsKey( "macAddress" ) )
		{
			String macAddr = params.get( "macAddress" ).trim().replaceFirst("[*]$", "%");
			filter = addFilter( filter, "macAddress", ":", macAddr );
		}
		if ( params.containsKey( "machineName" ) )
		{
			String machine = params.get( "machineName" ).trim().replaceFirst("[*]$", "%");
			filter = addFilter( filter, "machineName", ":", machine );
		}
		if ( params.containsKey( "domainName" ) )
		{
			String domain = params.get( "domainName" ).trim().replaceFirst("[*]$", "%");
			filter = addFilter( filter, "domainName", ":", domain );
		}
		if ( params.containsKey( "dataType" ) )
		{
			filter = addFilter( filter, "dataType", "=", params.get( "dataType" ) );
		}

		return filter;
	}

	@RequestMapping(value="{entryId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAuditEntry getEntry(@PathVariable("entryId") String entryId) throws Exception
	{
		AuditEntry entry = auditLogService.getAuditEntry(entryId);
		return typeConvertorService.getGuiAuditEntryFromAuditEntry(entry);
	}

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
			case "sequenceNo": order += "sequenceNo" + dir; break;
			case "timestamp": order += "timestamp" + dir; break;
			case "webUserID": order += "webUserID" + dir; break;
			case "ipAddress": order += "ipAddress" + dir; break;
			case "macAddress": order += "macAddress" + dir; break;
			case "machineName": order += "machineName" + dir; break;
			case "domainName": order += "domainName" + dir; break;
			case "dataType": order += "dataType" + dir; break;
			case "action": order += "action" + dir; break;
			}
		}
		return order;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String filter = this.compileFilter( params, violations );

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		AuditEntry[] entries = null;
		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long entryCount = auditLogService.listAuditEntriesCount( filter, null );
				Long entryCount = null;
				entries = auditLogService.listAuditEntries( filter, null, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiAuditEntryFromAuditEntry(entries), entryCount == null ? dtr.getStart() + ((entries.length < dtr.getLength()) ? entries.length : (dtr.getLength() * 2)) : entryCount.intValue());
			}

			entries = auditLogService.listAuditEntries(filter, null, null, null, null);
		}
		else
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long entryCount = auditLogService.listAuditEntriesCount(filter, dtr.getSearch().getValue());
				Long entryCount = null;
				entries = auditLogService.listAuditEntries( filter, dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiAuditEntryFromAuditEntry(entries), entryCount == null ? dtr.getStart() + ((entries.length < dtr.getLength()) ? entries.length : (dtr.getLength() * 2)) : entryCount.intValue());
			}

			entries = auditLogService.listAuditEntries(filter, dtr.getSearch().getValue(), null, null, null);
		}
		return new GuiDataTable(typeConvertorService.getGuiAuditEntryFromAuditEntry(entries));
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



	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;
		boolean validSearch = !(dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty());

		if (docount) count = auditLogService.count((validSearch)?dtr.getSearch().getValue():null);

		return auditLogService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.AUDIT, ".csv"));

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long recordCount = 0L;
		boolean validSearch = !(dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty());

		recordCount = auditLogService.count((validSearch)?dtr.getSearch().getValue():null);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(recordCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		auditLogService.csvExport(params.get("uniqid"), outputStream, (validSearch)?dtr.getSearch().getValue():null, recordsPerChunk, recordCount, true, null);
	}

	@RequestMapping(value="search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable search(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.compileFilter(params, violations);
		Integer withcount = ( params.containsKey( "withcount" ) && params.get( "withcount" ).equals("true") )?1:0;

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		AuditEntry[] entries = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			Long entryCount = null;
			if(withcount != null && withcount.intValue() != 0)
				entryCount = auditLogService.count( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
			entries = auditLogService.listAuditEntries( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
			return new GuiDataTable(typeConvertorService.getGuiAuditEntryFromAuditEntry(entries), entryCount == null ? dtr.getStart() + ((entries.length < dtr.getLength()) ? entries.length : (dtr.getLength() * 2)) : entryCount.intValue());
		}

		entries = auditLogService.listAuditEntries( filter, null, null, null, null );
		return new GuiDataTable(typeConvertorService.getGuiAuditEntryFromAuditEntry(entries));
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countSearchResults(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;

		if (docount) count = auditLogService.count(filter, dtr.getSearch().getValue());

		return auditLogService.track(count);
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET)
	@ResponseBody
	public void listCsvSearchResults(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.AUDIT, ".csv"));

		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long recordCount = 0L;

		recordCount = auditLogService.count(filter, dtr.getSearch().getValue());

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(recordCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		auditLogService.csvExport(params.get("uniqid"), outputStream, dtr.getSearch().getValue(), recordsPerChunk, recordCount, true, null);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AuditEntry[] serverList() throws Exception
	{
		AuditEntry[] entries = auditLogService.listAuditEntries( null, null, null, null, null );
		return entries;
	}
}

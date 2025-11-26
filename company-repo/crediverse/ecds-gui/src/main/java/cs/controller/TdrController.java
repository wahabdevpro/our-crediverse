package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

//import cs.dto.GuiTransaction;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiTdrDataTable;
import cs.dto.GuiTransaction;
import cs.dto.GuiTransactionEx;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.export.TdrExportParameters;
import cs.service.ConfigurationService;
import cs.service.TdrService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.Common;
import cs.utility.FilterBuilderUtils;
import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.TransactionEx;
import hxc.ecds.protocol.rest.Violation;

@RestController
@Profile(Common.CONST_ADMIN_PROFILE)
@RequestMapping("/api/tdrs")
public class TdrController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired //ask @Configuration-marked class for this
	private TdrService tdrService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	

	@RequestMapping(value="{tdrId}/{fetchSubscriberState}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction getTransaction(@PathVariable("tdrId") String tdrId, @PathVariable("fetchSubscriberState") boolean fetchSubscriberState) throws Exception
	{
		TransactionEx transactionEx = tdrService.getTransaction(tdrId,fetchSubscriberState);
		//transactionEx.resetDedicatedAccountRefillInfosIterator();
		Long reverseID = transactionEx.getReversedID();
		if (reverseID != null)
		{
			TransactionEx reversedTransactionEx = tdrService.getTransactionExFromReverseId(reverseID);
			transactionEx = reversedTransactionEx == null?transactionEx:reversedTransactionEx;
		}

		GuiTransaction convertedTransaction = typeConvertorService.getGuiTransactionFromTransactionEx(transactionEx, true);
		typeConvertorService.convertTransactionIdsToNames(convertedTransaction);
		return convertedTransaction;
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
			case "number": order += "number" + dir; break;
			case "transactionTypeName": order += "type" + dir; break;
			case "amount": order += "amount" + dir; break;
			case "buyerTradeBonusAmount": order += "buyerTradeBonusAmount" + dir; break;
			case "chargeLevied": order += "chargeLevied" + dir; break;
			case "channelName": order += "channel" + dir; break;
			case "startTimeString": order += "startTime" + dir; break;
			case "endTimeString": order += "endTime" + dir; break;
			case "apartyName": order += "a_Agent.firstName" + dir + "a_Agent.surname" + dir; break;
			case "a_MSISDN": order += "a_MSISDN" + dir; break;
			case "bpartyName": order += "b_Agent.firstName" + dir + "b_Agent.surname" + dir; break;
			case "b_MSISDN": order += "b_MSISDN" + dir; break;
			case "followUp": order += "followUp" + dir; break;
			//case "itemDescription": order += "itemDescription" +dir ; break;
			case "returnCode": order += "returnCode" + dir; break;
			}
		}
		return order;
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = tdrService.listTransactionsCount(search);

		return tdrService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.TDR, ".csv"));

		long itemCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		itemCount = tdrService.listTransactionsCount(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(itemCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		tdrService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, itemCount, true, null);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		boolean advanced = ( params.containsKey( "advanced" ) && params.get( "advanced" ).equals("true") )?true:false;

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getSearch() != null && !dtr.getSearch().getValue().trim().isEmpty() )
		{
			if ( !params.containsKey( "msisdnA" ) )
				params.put( "msisdnA", dtr.getSearch().getValue().trim() + "*");
		}
		String filter = FilterBuilderUtils.compileFilter( params, violations );
		Integer withcount = ( params.containsKey( "withcount" ) && params.get( "withcount" ).equals("true") )?1:0;
		boolean includeQuery = (params.containsKey( "withquery" ) ? (params.get( "withquery" ).equals("true") ? true : false) : false);
		Integer agentID = params.containsKey("agentID") ? Integer.valueOf(params.get("agentID")) : null;

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		ExResult<TransactionEx> transactions = null;
		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long transactionCount = tdrService.listTransactionsCount( filter, null );
				transactions = tdrService.listTransactionsEx(includeQuery, filter, null, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), withcount, advanced, agentID, null );
				return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
			}
			transactions = tdrService.listTransactionsEx(includeQuery, filter, null, null, 1000, null, withcount, advanced, agentID, null);
		}
		else
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long transactionCount = tdrService.listTransactionsCount(filter, dtr.getSearch().getValue());
				transactions = tdrService.listTransactionsEx(includeQuery, filter, null /*dtr.getSearch().getValue()*/, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), withcount, advanced, agentID, null );
				return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
			}
			transactions = tdrService.listTransactionsEx(includeQuery, filter, null /*dtr.getSearch().getValue()*/, null, 1000, null, withcount, advanced, agentID, null);
		}
		//Map<Integer, Tier> tierMap = tierService.tierMap();
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()));
	}

	@RequestMapping(value="search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable search(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		boolean advanced = ( params.containsKey( "advanced" ) && params.get( "advanced" ).equals("true") )?true:false;
		Integer agentID = params.containsKey( "agentID" ) ? Integer.valueOf(params.get( "agentID" )) : null;
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		if ( dtr.getSearch() != null && !dtr.getSearch().getValue().trim().isEmpty() )
		{
			if ( !params.containsKey( "msisdnA" ) )
				params.put( "msisdnA", dtr.getSearch().getValue().trim() + "*");
		}
		String filter = FilterBuilderUtils.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		Integer withcount = ( params.containsKey( "withcount" ) && params.get( "withcount" ).equals("true") )?1:0;
		boolean includeQuery = (params.containsKey( "withquery" ) ? (params.get( "withquery" ).equals("true") ? true : false) : false);
		
		ExResult<TransactionEx> transactions = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			//Long transactionCount = tdrService.listTransactionsCount( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
			transactions = tdrService.listTransactionsEx(includeQuery, filter, null /*dtr.getSearch() == null ? null : dtr.getSearch().getValue()*/, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), withcount, advanced, agentID, null);
			return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
		}

		transactions = tdrService.listTransactionsEx(includeQuery, filter, null, null, null, null, withcount, advanced, agentID, null);
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()));
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countSearchResults(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = FilterBuilderUtils.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;
		if ( dtr.getStart() != null && dtr.getLength() != null && docount)
		{
			count = tdrService.listTransactionsCount( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
		}

		return tdrService.track(count);
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void searchResultsExport(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		Integer agentID = params.containsKey("agentID") ? Integer.valueOf(params.get("agentID")) : null;
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.TDR, ".csv"));

		String filter = FilterBuilderUtils.compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		String search = dtr.getSearch() == null ? null : dtr.getSearch().getValue();

		boolean includeQuery = (params.containsKey( "withquery" ) ? (params.get( "withquery" ).equals("true") ? true : false) : false);
		
		long itemCount = 0L;
		
		if (docount) itemCount = tdrService.listTransactionsCount(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(itemCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		OutputStream outputStream = response.getOutputStream();
		TdrExportParameters additionalParams = new TdrExportParameters();
		additionalParams.setIncludeQuery(includeQuery);
		if(agentID != null)
		{
			additionalParams.setVirtualAgentID_AB(agentID);
		}
		
		tdrService.csvExport(params.get("uniqid"), outputStream, filter, search, recordsPerChunk, itemCount, docount, additionalParams);
	}

	@RequestMapping(value="last", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listLast() throws Exception
	{
		Transaction[] transactions = tdrService.listTransactions(0,5,"id-");
		//Map<Integer, Tier> tierMap = tierService.tierMap();
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransaction(transactions));
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Transaction[] serverList() throws Exception
	{
		Transaction[] transactions = tdrService.listTransactions();
		return transactions;
	}

	@RequestMapping(value="raw", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTdrDataTable transactionList(@RequestParam Map<String, String> params) throws Exception
	{
		int offset = params.containsKey("start")?Integer.parseInt(params.get("start")):0;
		int limit = params.containsKey("length")?Integer.parseInt(params.get("length")):10;

		GuiTransactionEx[] transactions = tdrService.listTransactionsTdr(offset, limit);
		GuiTdrDataTable datatable = new GuiTdrDataTable(transactions);
		datatable.setRecordsTotal(100);
		datatable.setRecordsFiltered(100);
		return datatable;
	}

	@RequestMapping(value="rawheadings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayNode transactionHeadings() throws Exception
	{
		ArrayNode transactions = tdrService.listTransactionsTdrHeadings();
		return transactions;
	}

	@RequestMapping(value = "/template/{version}", method = RequestMethod.GET)
	@ResponseBody
	public void getTDRHeader(@PathVariable("version") String version, HttpServletResponse response) throws Exception
	{
		String outputResult = tdrService.exportHeaderAsCsv(version, response);

		OutputStream outputStream = response.getOutputStream();
		outputStream.write(outputResult.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	@RequestMapping(value="raw/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransactionEx transactionList(@PathVariable(value="id") String tid) throws Exception
	{
		TransactionEx transactions = tdrService.getTransactionExFromNo(tid);
		return transactions;
	}
}

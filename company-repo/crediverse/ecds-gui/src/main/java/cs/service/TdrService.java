package cs.service;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.config.RestServerConfiguration;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiTransactionEx;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.export.TdrExportParameters;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import cs.utility.FilterBuilderUtils;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.TransactionEx;
import hxc.ecds.protocol.rest.Violation;

@Service
public class TdrService extends Exportable
{
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kkmmss");

	@Autowired
	private ConfigurationService configService;

	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private LoginSessionData sessionData;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getTdrUrl();
			configured = true;
		}
	}

	///////////////////// ------- UTILITY FUNCTIONS ------- \\\\\\\\\\\\\\\\\\\\\\\\\\

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

	public String compileFilter(Map<String, String> params, List<Violation> violations) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "msisdnA" ) )
		{
			String msisdn = params.get( "msisdnA" ).trim().replaceFirst("[*]$", "%");
			if ( !msisdn.matches("^[0-9]+[%]?$") )
				violations.add(new Violation(Violation.INVALID_VALUE, "msisdnA", null, "Must be numeric"));
			else
				filter = addFilter( filter, "a_MSISDN", ":", msisdn );
		}
		if ( params.containsKey( "msisdnB" ) )
		{
			String msisdn = params.get( "msisdnB" ).trim().replaceFirst("[*]$", "%");
			if ( !msisdn.matches("^[0-9]+[%]?$") )
				violations.add(new Violation(Violation.INVALID_VALUE, "msisdnB", null, "Must be numeric"));
			else
				filter = addFilter( filter, "b_MSISDN", ":", msisdn );
		}
		if ( params.containsKey( "number" ) )
			filter = addFilter( filter, "number", ":", params.get( "number" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "type" ) )
		{
			switch(params.get( "type" ))
			{
			case Transaction.TYPE_REPLENISH:
			case Transaction.TYPE_TRANSFER:
			case Transaction.TYPE_SELL:
			case Transaction.TYPE_NON_AIRTIME_DEBIT:
			case Transaction.TYPE_NON_AIRTIME_REFUND:
			case Transaction.TYPE_REGISTER_PIN:
			case Transaction.TYPE_CHANGE_PIN:
			case Transaction.TYPE_BALANCE_ENQUIRY:
			case Transaction.TYPE_SELF_TOPUP:
			case Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY:
			case Transaction.TYPE_LAST_TRANSACTION_ENQUIRY:
			case Transaction.TYPE_ADJUST:
			case Transaction.TYPE_SALES_QUERY:
			case Transaction.TYPE_DEPOSITS_QUERY:
			case Transaction.TYPE_REVERSE:
			case Transaction.TYPE_REVERSE_PARTIALLY:
			case Transaction.TYPE_PROMOTION_REWARD:
			case Transaction.TYPE_ADJUDICATE:
				filter = addFilter( filter, "type", "=", params.get( "type" ) );
				break;
			default:
				violations.add(new Violation(Violation.INVALID_VALUE, "type", null, "Unsupported transaction type"));
				break;
			}
		}
		if ( params.containsKey( "channel" ) )
		{
			switch(params.get( "channel" ))
			{
			case "U":
			case "B":
			case "S":
			case "A":
			case "P":
			case "W":
				filter = addFilter( filter, "channel", "=", params.get( "channel" ) );
				break;
			default:
				violations.add(new Violation(Violation.INVALID_VALUE, "channel", null, "Unsupported channel"));
				break;
			}
		}
		if ( params.containsKey( "callerID" ) )
			filter = addFilter( filter, "callerID", "=", params.get( "callerID" ) );
		if ( params.containsKey( "groupIDA" ) && !params.get( "groupIDA" ).isEmpty() )
			filter = addFilter( filter, "a_GroupID", "=", params.get( "groupIDA" ) );
		if ( params.containsKey( "groupIDB" ) && !params.get( "groupIDB" ).isEmpty() )
			filter = addFilter( filter, "b_GroupID", "=", params.get( "groupIDB" ) );
		if ( params.containsKey( "agentIDA" ) && !params.get( "agentIDA" ).isEmpty() )
			filter = addFilter( filter, "a_AgentID", "=", params.get( "agentIDA" ) );
		if ( params.containsKey( "agentIDB" ) && !params.get( "agentIDB" ).isEmpty() )
			filter = addFilter( filter, "b_AgentID", "=", params.get( "agentIDB" ) );
		if ( params.containsKey( "agentID" ) && !params.get( "agentID" ).isEmpty() )
			filter = addFilter( filter, "agentID", "=", params.get( "agentID" ) );

		if ( params.containsKey( "a_TierID" ) && !params.get( "a_TierID" ).isEmpty() )
			filter = addFilter( filter, "a_TierID", "=", params.get( "a_TierID" ) );
		if ( params.containsKey( "b_TierID" ) && !params.get( "b_TierID" ).isEmpty() )
			filter = addFilter( filter, "b_TierID", "=", params.get( "b_TierID" ) );

		if ( params.containsKey( "followUp" ) && params.get( "followUp" ).equals("A") )
			filter = addFilter( filter, "followUp", "=", "true" );
		if ( params.containsKey( "followUp" ) && params.get( "followUp" ).equals("P") )
			filter = addFilter( filter, "UA", "=", "1" );
		if ( params.containsKey( "relation" ) && !params.get( "relation" ).isEmpty() )
			filter = addFilter( filter, "relation", "=", params.get( "relation" ) );

		String timeFrom = "0000";
		String timeTo = "2359";

		if ( params.containsKey( "timeFrom" ) )
		{
			String time = FilterBuilderUtils.getTime(violations, params.get("timeFrom").trim(), "timeFrom" );
			if ( time != null ) timeFrom = time;
		}
		if ( params.containsKey( "timeTo" ) )
		{
			String time = FilterBuilderUtils.getTime(violations, params.get("timeTo").trim(), "timeTo" );
			if ( time != null ) timeTo = time;
		}

		Date dateFrom = null;
		Date dateTo = null;

		if ( params.containsKey( "dateFrom" ) )
		{
			dateFrom = FilterBuilderUtils.getDate(violations, params.get("dateFrom").trim(), "dateFrom", dateFrom);
			if ( dateFrom != null )
			{
				String dateFromStr = params.get( "dateFrom" );
				dateFromStr = dateFromStr.replace( "-", "" );
				dateFromStr += "T"+timeFrom+"00";
				filter = addFilter( filter, "startTime", ">=", dateFromStr );
			}
		}
		if ( params.containsKey( "dateTo" ) )
		{
			dateTo = FilterBuilderUtils.getDate(violations, params.get("dateTo").trim(), "dateTo", dateFrom);
			if ( dateTo != null )
			{
				String dateToStr = params.get( "dateTo" );
				dateToStr = dateToStr.replace( "-", "" );
				dateToStr += "T"+timeTo+"59";
				filter = addFilter( filter, "startTime", "<=", dateToStr );
			}
		}

		BigDecimal amountFrom = new BigDecimal(0);
		BigDecimal amountTo = new BigDecimal(0);
		BigDecimal bonusFrom = new BigDecimal(0);
		BigDecimal bonusTo = new BigDecimal(0);
		BigDecimal chargeFrom = new BigDecimal(0);
		BigDecimal chargeTo = new BigDecimal(0);

		if ( params.containsKey( "amountFrom" ) )
		{
			amountFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("amountFrom").trim(), "amountFrom", amountFrom);
			if (amountFrom != null)
				filter = addFilter( filter, "amount", ">=", params.get( "amountFrom" ) );
		}
		if ( params.containsKey( "amountTo" ) )
		{
			amountTo = FilterBuilderUtils.getBigDecimal(violations, params.get("amountTo").trim(), "amountTo", amountFrom);
			if (amountTo != null)
				filter = addFilter( filter, "amount", "<=", params.get( "amountTo" ) );
		}
		if ( params.containsKey( "bonusAmountFrom" ) )
		{
			bonusFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("bonusAmountFrom").trim(), "bonusAmountFrom", bonusFrom);
			if (bonusFrom != null)
				filter = addFilter( filter, "buyerTradeBonusAmount", ">=", params.get( "bonusAmountFrom" ) );
		}
		if ( params.containsKey( "bonusAmountTo" ) )
		{
			bonusTo = FilterBuilderUtils.getBigDecimal(violations, params.get("bonusAmountTo").trim(), "bonusAmountTo", bonusFrom);
			if (bonusTo != null)
				filter = addFilter( filter, "buyerTradeBonusAmount", "<=", params.get( "bonusAmountTo" ) );
		}
		if ( params.containsKey( "chargeAmountFrom" ) )
		{
			chargeFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("chargeAmountFrom").trim(), "chargeAmountFrom", chargeFrom);
			if (chargeFrom != null)
				filter = addFilter( filter, "chargeLevied", ">=", params.get( "chargeAmountFrom" ) );
		}
		if ( params.containsKey( "chargeAmountTo" ) )
		{
			chargeTo = FilterBuilderUtils.getBigDecimal(violations, params.get("chargeAmountTo").trim(), "chargeAmountTo", chargeFrom);
			if (chargeTo != null)
				filter = addFilter( filter, "chargeLevied", "<=", params.get( "chargeAmountTo" ) );
		}

		return filter;
	}

	public String tsSortFromDtSort( GuiDataTableRequest dtr )
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
			//case "itemDescription": order += "itemDescription" + dir; break;
			case "returnCode": order += "returnCode" + dir; break;
			}
		}
		return order;
	}

	public GuiDataTable createTdrDataTableContent(Map<String, String> params, TypeConvertorService typeConvertorService) throws Exception
	{
		boolean withquery = ( params.containsKey( "withquery" ) && params.get( "withquery" ).equals("true") )?true:false;
		boolean includeQuery = ( params.containsKey( "showQuery" ) && params.get( "showQuery" ).equals("true") )?true:withquery;
		Integer agentID = (params.containsKey( "agentID" ) ? Integer.valueOf(params.get("agentID")) : null);
		String msisdn = params.get("msisdn") != null ? params.get( "msisdn" ).trim() : null;
		ArrayList<Violation> violations = new ArrayList<Violation>();

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String filter = compileFilter( params, violations );
		Integer withcount = ( params.containsKey( "withcount" ) && params.get( "withcount" ).equals("true") )?1:0;
		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		boolean advanced = ( params.containsKey( "advanced" ) && params.get( "advanced" ).equals("true") )?true:false;
		ExResult<TransactionEx> transactions = null;
		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long transactionCount = tdrService.listTransactionsCount( filter, null );
				transactions = listTransactionsEx(includeQuery, filter, null, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), withcount, advanced, agentID, msisdn);
				return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
			}

			transactions = listTransactionsEx(includeQuery, filter, null, null, null, null, null, advanced, agentID, msisdn);
		}
		else
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long transactionCount = tdrService.listTransactionsCount(filter, dtr.getSearch().getValue());
				transactions = listTransactionsEx(includeQuery, filter, dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), null, advanced, agentID, msisdn);
				return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
			}

			transactions = listTransactionsEx(includeQuery, filter, dtr.getSearch().getValue(), null, null, null, null, advanced, agentID, msisdn);
		}
		//Map<Integer, Tier> tierMap = tierService.tierMap();
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()));
	}
	// -----------------------------------------------------------------------------\\
	///////////////

	public ExResult<TransactionEx> listTransactionsEx(boolean includeQuery, String filter, String search, Integer offset, Integer limit, String sort, Integer withcount, boolean advanced, Integer agentID, String msisdnab) throws Exception
	{
		ExResult<TransactionEx> result = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		
		// Not showing the transactions from last N seconds to prevent transactions in progress to be considered as for adjudication.
//		filter = skipTransactionFromLastNSeconds(filter, 1.5f);
		
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		RestRequestUtil.includeQuery(uri, includeQuery);
		RestRequestUtil.withRecordCount(uri, withcount != null && withcount.intValue() != 0);

		if (msisdnab != null && !msisdnab.isEmpty())
			RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_MSISDNAB, msisdnab);

		if (!advanced)
		{
			String userMsisdn = sessionData.getCurrentUser().getUserMsisdn();
			if (userMsisdn != null)
			{
				RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_MSISDNAB, null);
				RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_AGENTIDAB, agentID!=null?agentID.toString():"");
				ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
				result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
			}
			else
			{
				result = new ExResult<TransactionEx>();
			}
		}
		else
		{
			RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_MSISDNAB, null);
			RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_AGENTIDAB, agentID!=null?agentID.toString():"");
			ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
			result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		}
		return result;
	}

	///////////////

	@Override
	public String listAsCsv(String filter, String search, boolean withQuery, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.includeQuery(uri, withQuery);
		//RestRequestUtil.virtualFilter(uri, TransactionEx.VIRTUAL_FILTER_AGENTIDAB, value);
		RestRequestUtil.addCustomParameters(uri, queryStringParams);
		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.addCustomParameters(uri, queryStringParams);
		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}
	
	public Transaction[] listTransactions() throws Exception
	{
		Transaction[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Long listTransactionsCount() throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public Transaction[] listTransactions(int offset, int limit, String sort) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Transaction[] listTransactions(String filter) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Transaction[] listTransactions(String filter, int offset, int limit, String sort) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Long listTransactionsCount(String filter) throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardSearch(uri, filter);
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public Long listTransactionsCount(String filter, String search) throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public Transaction[] listTransactions(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Transaction[] searchTransactions(String filter) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Transaction[] searchTransactions(String filter, int offset, int limit, String sort) throws Exception
	{
		Transaction[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
		return response;
	}

	public Long searchTransactionsCount(String filter) throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		RestRequestUtil.standardFilter(uri, filter);
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public Transaction getTransaction(int transactionId) throws Exception
	{
		return getTransaction(String.valueOf(transactionId), false);
	}

	public Boolean isAdjudicatedTransaction(String transactionId) throws Exception
	{
		StringBuilder filter = new StringBuilder("number='");
		filter.append(transactionId);
		filter.append("'+UA='1'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter.toString());

		Transaction[] transactions =  restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);

		if ( transactions == null ) return null;

		return transactions.length == 0;
	}

	public Transaction getAdjudicationForTransaction(String transactionId) throws Exception
	{
		Transaction[] transactions = getReversalsForTransaction(transactionId);
		if ( transactions == null || transactions.length < 1 ) return null;
		return transactions[0];
	}

	public TransactionEx getAdjudicationForTransactionEx(String transactionId) throws Exception
	{
		TransactionEx[] transactions = getReversalsForTransactionEx(transactionId);
		if ( transactions == null || transactions.length < 1 ) return null;
		return transactions[0];
	}

	/*
	 *
	 */
	public Transaction[] getReversalsForTransaction(String transactionId) throws Exception
	{
		StringBuilder filter = new StringBuilder("reversedID='");
		filter.append(transactionId);
		filter.append("'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter.toString());

		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
	}

	public TransactionEx[] getReversalsForTransactionEx(String transactionId) throws Exception
	{
		StringBuilder filter = new StringBuilder("reversedID='");
		filter.append(transactionId);
		filter.append("'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		RestRequestUtil.standardFilter(uri, filter.toString());
		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		return result.getInstances();
	}

	public Transaction getTransactionFromReverseId(Long reverseID) throws Exception
	{
		Transaction response = null;
		if (reverseID != null)
		{
			Transaction[] results = null;
			UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
			RestRequestUtil.standardFilter(uri, "id='"+reverseID.toString()+"'");
			results = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Transaction[].class);
			if (results != null && results.length >= 1) response = results[0];
		}
		return response;
	}

	public TransactionEx getTransactionExFromReverseId(Long reverseID) throws Exception
	{
		TransactionEx response = null;
		if (reverseID != null)
		{
			UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
			RestRequestUtil.standardFilter(uri, "id='"+reverseID.toString()+"'");
			ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
			ExResult<TransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
			if ( result.getInstances().length >= 0 ) response = result.getInstances()[0];
		}
		return response;
	}

	public TransactionEx getTransaction(String transactionId, boolean fetchSubscriberState) throws Exception
	{
		StringBuilder filter = new StringBuilder("number='");
		filter.append(transactionId);
		filter.append("'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		RestRequestUtil.standardFilter(uri, filter.toString());
		RestRequestUtil.includeFetchSubscriberState(uri, fetchSubscriberState);
		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		if ( result.getInstances().length == 0 )
			return null;
		return result.getInstances()[0];
	}

	public TransactionEx getTransactionEx(String transactionId) throws Exception
	{
		StringBuilder filter = new StringBuilder("id='");
		filter.append(transactionId);
		filter.append("'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		RestRequestUtil.standardFilter(uri, filter.toString());
		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		if ( result.getInstances().length == 0 )
			return null;
		return result.getInstances()[0];
	}

	private ObjectNode createItem(String description, String value)
	{
		ObjectNode item = mapper.createObjectNode();
		item.put("id", value);
		item.put("text", description);
		return item;
	}

	public ArrayNode getTransactionTypes()
	{
		ArrayNode results = mapper.createArrayNode();
		results.add(createItem("REPLENISH", Transaction.TYPE_REPLENISH));
		results.add(createItem("TRANSFER", Transaction.TYPE_TRANSFER));
		results.add(createItem("SELL", Transaction.TYPE_SELL));
		results.add(createItem("NON_AIRTIME_DEBIT", Transaction.TYPE_NON_AIRTIME_DEBIT));
		results.add(createItem("NON_AIRTIME_REFUND", Transaction.TYPE_NON_AIRTIME_REFUND));
		results.add(createItem("REGISTER_PIN", Transaction.TYPE_REGISTER_PIN));
		results.add(createItem("CHANGE_PIN", Transaction.TYPE_CHANGE_PIN));
		results.add(createItem("BALANCE_ENQUIRY", Transaction.TYPE_BALANCE_ENQUIRY));
		results.add(createItem("SELF_TOPUP", Transaction.TYPE_SELF_TOPUP));
		results.add(createItem("TRANSACTION_STATUS_ENQUIRY", Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY));
		results.add(createItem("LAST_TRANSACTION_ENQUIRY", Transaction.TYPE_LAST_TRANSACTION_ENQUIRY));
		results.add(createItem("ADJUST", Transaction.TYPE_ADJUST));
		results.add(createItem("SALES_QUERY", Transaction.TYPE_SALES_QUERY));
		results.add(createItem("DEPOSITS_QUERY", Transaction.TYPE_DEPOSITS_QUERY));
		results.add(createItem("REVERSE", Transaction.TYPE_REVERSE));
		results.add(createItem("REVERSE_PARTIALLY", Transaction.TYPE_REVERSE_PARTIALLY));
		results.add(createItem("PROMOTION_REWARD", Transaction.TYPE_PROMOTION_REWARD));
		results.add(createItem("ADJUDICATE", Transaction.TYPE_ADJUDICATE));
		return results;
	}


	// MOved from Controller
	public Transaction getTransactionOrReversed(String tdrId) throws Exception
	{
		Transaction transaction = getTransaction(tdrId, false);
		Long reverseID = transaction.getReversedID();
		if (reverseID != null)
		{
			Transaction reversedTransaction = getTransactionFromReverseId(reverseID);
			transaction = (reversedTransaction == null)? transaction : reversedTransaction;
		}
		return transaction;
	}


	public ObjectNode transactionCount(Map<String, String> params) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		count = listTransactionsCount(search);

		return this.track(count);
	}

	//NotOverride
	public void listAsCsv(Map<String, String> params, HttpServletResponse response, boolean docount, IQueryStringParameters queryStringParams) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.TDR, ".csv"));

		long itemCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) itemCount = listTransactionsCount(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(itemCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, itemCount, docount, queryStringParams);
	}

	public String exportHeaderAsCsv(String version, HttpServletResponse response) throws Exception {
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "tdr_template_"+version, ".csv"));
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/template/csv");
		RestRequestUtil.standardFilter(uri, version.toString());
		String result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return result;
	}


	public void searchResultsExport(Map<String, String> params, HttpServletResponse response, boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		
		String filter = compileFilter(params, violations);

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.TDR, ".csv"));

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		String search = dtr.getSearch() == null ? null : dtr.getSearch().getValue();

		long itemCount = 0L;
		if (docount)
			itemCount = listTransactionsCount(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(itemCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");
		
		boolean includeQuery = ( params.containsKey( "withquery" ) && params.get( "withquery" ).equals("true") )?true:false;

		OutputStream outputStream = response.getOutputStream();
		TdrExportParameters additionalParams = new TdrExportParameters();
		additionalParams.setIncludeQuery(includeQuery);
		csvExport(params.get("uniqid"), outputStream, filter, search, recordsPerChunk, itemCount, docount, additionalParams);
	}

	public GuiDataTable search(Map<String, String> params, TypeConvertorService typeConvertorService) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = compileFilter(params, violations);
		Integer withcount = ( params.containsKey( "withcount" ) && params.get( "withcount" ).equals("true") )?1:0;
		boolean withquery = ( params.containsKey( "withquery" ) && params.get( "withquery" ).equals("true") )?true:false;
		boolean advanced = ( params.containsKey( "advanced" ) && params.get( "advanced" ).equals("true") )?true:false;
		String msisdn = params.get("msisdn") != null ? params.get( "msisdn" ).trim() : null;
		Integer agentID = params.get("agentID") != null ? Integer.valueOf(params.get("agentID")) : null;
		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		ExResult<TransactionEx> transactions = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			//Long transactionCount = tdrService.listTransactionsCount( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
			transactions = listTransactionsEx(withquery, filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr), withcount, advanced, agentID, msisdn);
			return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()), transactions.getFoundRows() == null ? dtr.getStart() + ((transactions.getInstances().length < dtr.getLength()) ? transactions.getInstances().length : (dtr.getLength() * 2)) : transactions.getFoundRows().intValue());
		}

		transactions = listTransactionsEx(withquery, filter, null, null, null, null, null, advanced, agentID, msisdn);
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()));
	}

	public ObjectNode countSearchResults(Map<String, String> params, boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = compileFilter(params, violations);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;
		if ( dtr.getStart() != null && dtr.getLength() != null && docount)
		{
			count = listTransactionsCount( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
		}

		return this.track(count);
	}

	// GET::http://lab9-ecds-vm1.concurrent.systems:14400/ecds/transactions/ex?max=10&sort=startTime-"
	public TransactionEx getTransactionExFromNo(String transactionId) throws Exception
	{
		StringBuilder filter = new StringBuilder("number='");
		filter.append(transactionId);
		filter.append("'");
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		RestRequestUtil.standardFilter(uri, filter.toString());

		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		if ( result.getInstances().length == 0 )
			return null;
		return result.getInstances()[0];
	}

	public GuiTransactionEx[] listTransactionsTdr(Integer offset, Integer limit) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/ex");
		RestRequestUtil.standardSorting(uri, "startTime-");
		RestRequestUtil.standardPaging(uri, offset, limit);

		ParameterizedTypeReference<ExResult<GuiTransactionEx>> type = new ParameterizedTypeReference<ExResult<GuiTransactionEx>>() {};
		ExResult<GuiTransactionEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		if ( result.getInstances().length == 0 )
			return null;
		return result.getInstances();
	}

	public ArrayNode listTransactionsTdrHeadings() throws Exception
	{
		Field[] fields = TransactionEx.class.getDeclaredFields();
		Field[] fields2 = Transaction.class.getDeclaredFields();
		ArrayNode result = mapper.createArrayNode();
		for (Field fld : fields)
		{
			if (Modifier.isPrivate(fld.getModifiers())) {
				result.add(fld.getName());
			}
		}

		for (Field fld : fields2)
		{
			if (Modifier.isProtected(fld.getModifiers())) {
				result.add(fld.getName());
			}
		}
		return result;
	}
}

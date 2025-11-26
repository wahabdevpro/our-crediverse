package cs.service;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.dto.GuiTransaction;
import cs.dto.GuiTransaction.TransactionStatusEnum;
import cs.dto.GuiTransaction.TransactionTypeEnum;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.RegisterTransactionNotificationRequest;
import hxc.ecds.protocol.rest.SellRequest;
import hxc.ecds.protocol.rest.SellResponse;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.TransactionEx;
import hxc.ecds.protocol.rest.TransactionNotificationCallback;
import hxc.ecds.protocol.rest.TransferRequest;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.SellBundleRequest;

import static cs.utility.Common.checkAgentIdForNull;

@Service
public class TransactionService
{
	private ConcurrentHashMap<String, Object> notificationMap = new ConcurrentHashMap<String, Object>();
	private ConcurrentHashMap<String, List<Transaction> > transactionInbox = new ConcurrentHashMap<String, List<Transaction> >();
	
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	@Autowired
	private CsRestTemplate restTemplate;
	
	@Autowired
	private LoginSessionData sessionData;
	
	@Autowired
	private ApplicationDetailsConfiguration appConfig;
	
	@Autowired
	AgentService agentService;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	private String restServerUrlTdr;
	private String restServerUrlTransaction;
	
	private static Map<String, String> sortFieldNameMap = new HashMap<String, String>();
	static
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("transactionNo", "number");
		map.put("transactionStarted", "startTime");
		map.put("transactionEnded", "endTime");
		map.put("a_msisdn", "A_MSISDN");
		map.put("b_msisdn", "B_MSISDN");
		map.put("bundleId", "bundleID");
		map.put("type", "type");
		map.put("status", "returnCode");
		
		sortFieldNameMap = Collections.unmodifiableMap(map);
	}

	@PostConstruct
	public void configure()
	{
		this.restServerUrlTdr = restServerConfig.getRestServer() + restServerConfig.getTdrUrl();
		this.restServerUrlTransaction = restServerConfig.getRestServer() + restServerConfig.getTransactionsUrl();
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
	
	private String getDateTime(List<Violation> violations, String value, String name) throws GuiValidationException
	{
		String result = null;
		try
		{
			LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
			result = dt.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
		} catch (DateTimeParseException e) {
			violations.add(new Violation(Violation.INVALID_VALUE, name, null, String.format("Date value %s must be in local ISO8601 format.", value)));
			throw new GuiValidationException(String.format("Date value %s must be in local ISO8601 format.", value), e);
		}		
		return result;
	}

	private BigDecimal getBigDecimal(List<Violation> violations, String value, String name, BigDecimal min)
	{
		DecimalFormat format = new DecimalFormat();
		format.setParseBigDecimal(true);
		ParsePosition pos = new ParsePosition(0);
		Number number = format.parse(value, pos);
		if (number == null)
			violations.add(new Violation(Violation.INVALID_VALUE, name, null, "Must be a numeric value"));
		else
		{
			BigDecimal bd = new BigDecimal(number.toString());
			if ((min != null) && (bd.longValue() < min.longValue()))
				violations.add(new Violation(Violation.TOO_SMALL, name, bd.toString(), String.format("Must be greater than or equal to %s", bd.toString())));
			return bd;
		}
		return null;
	}
	
	private String compileFilter(Map<String, String> params, List<Violation> violations) throws GuiValidationException
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
			String value = params.get("type");
			if(TransactionTypeEnum.contains(value))
			{
				TransactionTypeEnum type = TransactionTypeEnum.valueOf(value);
				filter = addFilter(filter, "type", "=", type.getVal());
			} else {
				violations.add(new Violation(Violation.INVALID_VALUE, "type", null, String.format("Unsupported transaction type: %s", value)));
				//throw new IllegalArgumentException(String.format("Invalid parameter value supplied. %s is not a valid value for Transaction Type", typeParam), e);
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

		if ( params.containsKey( "a_TierID" ) && !params.get( "a_TierID" ).isEmpty() )
			filter = addFilter( filter, "a_TierID", "=", params.get( "a_TierID" ) );
		if ( params.containsKey( "b_TierID" ) && !params.get( "b_TierID" ).isEmpty() )
			filter = addFilter( filter, "b_TierID", "=", params.get( "b_TierID" ) );

		if ( params.containsKey( "a_OwnerID" ) && !params.get( "a_OwnerID" ).isEmpty() )
			filter = addFilter( filter, "a_OwnerAgentID", "=", params.get( "a_OwnerID" ) );
		if ( params.containsKey( "b_OwnerID" ) && !params.get( "b_OwnerID" ).isEmpty() )
			filter = addFilter( filter, "b_OwnerAgentID", "=", params.get( "b_OwnerID" ) );

		if ( params.containsKey( "status" ) && !params.get( "status" ).isEmpty() )
		{
			String value = params.get("status");
			if(TransactionStatusEnum.contains(value))
			{
				TransactionStatusEnum status = TransactionStatusEnum.valueOf(value);
				filter = addFilter( filter, "returnCode", "=", status.toString() );
			} else {
				//throw new IllegalArgumentException(String.format("Invalid parameter value supplied. %s is not a valid value for Transaction Status", value));
				violations.add(new Violation(Violation.INVALID_VALUE, "status", null, String.format("Unsupported transaction status: %s", value)));
			}
		}

		if ( params.containsKey( "followUp" ) && params.get( "followUp" ).equals("A") )
			filter = addFilter( filter, "followUp", "=", "true" );
		if ( params.containsKey( "followUp" ) && params.get( "followUp" ).equals("P") )
			filter = addFilter( filter, "UA", "=", "1" );

		if ( params.containsKey( "startDate" ) )
		{
			String time = getDateTime(violations, params.get("startDate").trim(), "startDate" );
			if ( time != null ) 
			{
				filter = addFilter( filter, "startTime", ">=", time );
			}
		}
		if ( params.containsKey( "endDate" ) )
		{
			String time = getDateTime(violations, params.get("endDate").trim(), "endDate" );
			if ( time != null )
			{
				filter = addFilter( filter, "startTime", "<=", time );
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
			amountFrom = getBigDecimal(violations, params.get("amountFrom").trim(), "amountFrom", amountFrom);
			if (amountFrom != null)
				filter = addFilter( filter, "amount", ">=", params.get( "amountFrom" ) );
		}
		if ( params.containsKey( "amountTo" ) )
		{
			amountTo = getBigDecimal(violations, params.get("amountTo").trim(), "amountTo", amountFrom);
			if (amountTo != null)
				filter = addFilter( filter, "amount", "<=", params.get( "amountTo" ) );
		}
		if ( params.containsKey( "bonusAmountFrom" ) )
		{
			bonusFrom = getBigDecimal(violations, params.get("bonusAmountFrom").trim(), "bonusAmountFrom", bonusFrom);
			if (bonusFrom != null)
				filter = addFilter( filter, "buyerTradeBonusAmount", ">=", params.get( "bonusAmountFrom" ) );
		}
		if ( params.containsKey( "bonusAmountTo" ) )
		{
			bonusTo = getBigDecimal(violations, params.get("bonusAmountTo").trim(), "bonusAmountTo", bonusFrom);
			if (bonusTo != null)
				filter = addFilter( filter, "buyerTradeBonusAmount", "<=", params.get( "bonusAmountTo" ) );
		}
		if ( params.containsKey( "chargeAmountFrom" ) )
		{
			chargeFrom = getBigDecimal(violations, params.get("chargeAmountFrom").trim(), "chargeAmountFrom", chargeFrom);
			if (chargeFrom != null)
				filter = addFilter( filter, "chargeLevied", ">=", params.get( "chargeAmountFrom" ) );
		}
		if ( params.containsKey( "chargeAmountTo" ) )
		{
			chargeTo = getBigDecimal(violations, params.get("chargeAmountTo").trim(), "chargeAmountTo", chargeFrom);
			if (chargeTo != null)
				filter = addFilter( filter, "chargeLevied", "<=", params.get( "chargeAmountTo" ) );
		}

		return filter;
	}

	public GuiTransaction[] listTransactions(Map<String, String> params) throws Exception
	{
		Integer offset = params.containsKey("offset")? Integer.parseInt(params.get("offset")) : null;
		Integer limit = params.containsKey("limit")? Integer.parseInt(params.get("limit")) : null;
		String sort = params.containsKey("sort")? params.get("sort") : null;
		
		ArrayList<Violation> violations = new ArrayList<Violation>();
		StringBuilder filter = new StringBuilder();
		//filter.append(String.format("a_AgentID='%d'+", sessionData.getAgentId()));
		filter.append(compileFilter(params, violations));
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(restServerUrlTdr + "/ex");
		if (offset != null || limit != null)
		{
			RestRequestUtil.standardPaging(uriBuilder, offset, limit);
		}
		RestRequestUtil.standardFilter(uriBuilder, filter.toString());
		if(sort != null)
		{
			sort = translateTransactionSortFields(sort);
			RestRequestUtil.standardSorting(uriBuilder, sort.toString());
		}
		RestRequestUtil.withRecordCount(uriBuilder, false);
		checkAgentIdForNull(sessionData.getAgentId());
		Agent agent = agentService.getAgent(sessionData.getAgentId());
		uriBuilder.queryParam("virtual_msisdnab", agent.getMobileNumber());
		//uriBuilder.queryParam("virtual_msisdnab", agent.getMobileNumber());

		URI uri = uriBuilder.build(true).toUri();
		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> transations = restTemplate.execute(uri, HttpMethod.GET, type);
		return getGuiTransactionArrayFromTransactionArray(transations, agent.getMobileNumber());
	}
	
	public GuiTransaction getTransactionByNumber(String transactionNumber) throws Exception
	{
		Transaction transaction = null;
		transaction = restTemplate.execute(restServerUrlTdr+"/"+ transactionNumber, HttpMethod.GET, Transaction.class);
		String agentMsisdn = sessionData.getAgentMsisdn();
		return new GuiTransaction(transaction, agentMsisdn);
	}
	
	public GuiTransaction getTransactionByID(String transactionID) throws Exception
	{
		Transaction transaction = null;
		transaction = restTemplate.execute(restServerUrlTdr+"/id/"+ transactionID, HttpMethod.GET, Transaction.class);
		String agentMsisdn = sessionData.getAgentMsisdn();
		return new GuiTransaction(transaction, agentMsisdn);
	}
	
	public GuiTransaction sellAirtime(SellRequest request) throws Exception
	{		
		ArrayList<Violation> violations = new ArrayList<Violation>();
		GuiTransaction transaction = null;
		request.setSessionID(sessionData.getServerSessionID());
		if(request.getAmount().compareTo(BigDecimal.ZERO) < 0)
		{
			String error = String.format("Amount %.2f cannot be less than zero", request.getAmount());
			violations.add(new Violation(Violation.INVALID_VALUE,"amount", null, error));
		}
		if(Strings.isNullOrEmpty(request.getTargetMSISDN()))
		{
			String error = "Target MSISDN cannot be empty";
			violations.add(new Violation(Violation.INVALID_VALUE, "targetMSISDN", null, error));
		}
		if(violations.size() > 0)
		{
			throw new GuiValidationException(violations);
		}
		SellResponse sellResponse = restTemplate.postForObject(restServerUrlTransaction + "/sell", request, SellResponse.class);
		if(sellResponse.getReturnCode().equals("SUCCESS"))
		{
			String transactionNumber = sellResponse.getTransactionNumber();
			transaction = getTransactionByNumber(transactionNumber);
		}
		return transaction;
	}
	
	public GuiTransaction transferCredit(TransferRequest request) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		GuiTransaction transaction = null;
		request.setSessionID(sessionData.getServerSessionID());
		if(request.getAmount().compareTo(BigDecimal.ZERO) < 0)
		{
			String error = String.format("Transfer amount %.2f cannot be less than zero", request.getAmount());
			violations.add(new Violation(Violation.INVALID_VALUE,"amount", null, error));
		}
		if(request.getTargetMSISDN().isEmpty())
		{
			String error = String.format("Target MSISDN %s cannot be empty", request.getTargetMSISDN());
			violations.add(new Violation(Violation.INVALID_VALUE, "targetMsisdn", null, error));
		}
		if(violations.size() > 0)
		{
			throw new GuiValidationException(violations);
		}
		SellResponse sellResponse = restTemplate.postForObject(restServerUrlTransaction + "/transfer", request, SellResponse.class);
		if(sellResponse.getReturnCode().equals("SUCCESS"))
		{
			String transactionNumber = sellResponse.getTransactionNumber();
			transaction = getTransactionByNumber(transactionNumber);
		}
		return transaction;
	}

	private GuiTransaction[] getGuiTransactionArrayFromTransactionArray(ExResult<TransactionEx> transactions, String msisdn)
	{
		ArrayList<GuiTransaction> guiTransactions = new ArrayList<GuiTransaction>();
		for(TransactionEx transaction:transactions.getInstances())
		{
			String agentMsisdn = sessionData.getAgentMsisdn();
			GuiTransaction guiTransaction = new GuiTransaction(transaction, agentMsisdn);
			guiTransactions.add(guiTransaction);
		}
		return guiTransactions.toArray(new GuiTransaction[guiTransactions.size()]);
	}
	
	public GuiTransaction[] getSubsequentTransactions(String transactionNo, Map<String, String> params) throws Exception
	{
		ArrayList<GuiTransaction> guiTransactions = new ArrayList<GuiTransaction>();
		String sessionID = sessionData.getServerSessionID();

		ArrayList<Violation> violations = new ArrayList<Violation>();
		Integer offset = params.containsKey("offset")? Integer.parseInt(params.get("offset")) : null;
		Integer limit = params.containsKey("limit")? Integer.parseInt(params.get("limit")) : null;
		
		StringBuilder filter = new StringBuilder();
		//filter.append(String.format("a_AgentID='%d'+", sessionData.getAgentId()));
		filter.append(String.format("number>'%s'+", transactionNo));
		filter.append(compileFilter(params, violations));
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(restServerUrlTdr + "/ex");
		if (offset != null || limit != null)
		{
			RestRequestUtil.standardPaging(uriBuilder, offset, limit);
		}
		RestRequestUtil.standardFilter(uriBuilder, filter.toString());
		checkAgentIdForNull(sessionData.getAgentId());
		Agent agent = agentService.getAgent(sessionData.getAgentId());
		uriBuilder.queryParam("virtual_msisdnab", agent.getMobileNumber());
		//uriBuilder.queryParam("virtual_msisdnab", agent.getMobileNumber());
		URI uri = uriBuilder.build(true).toUri();
		ParameterizedTypeReference<ExResult<TransactionEx>> type = new ParameterizedTypeReference<ExResult<TransactionEx>>() {};
		ExResult<TransactionEx> transations = restTemplate.execute(uri, HttpMethod.GET, type);
		if(transations.getInstances().length > 0)
		{
			return getGuiTransactionArrayFromTransactionArray(transations, agent.getMobileNumber());
		} else {
			RegisterTransactionNotificationRequest request = new RegisterTransactionNotificationRequest();
			Object syncObj = new Object();
			
			long timeout = params.containsKey("timeout")? Long.parseLong(params.get("timeout")) : appConfig.getTransactionNotifyTimeout();
			request.setAgentID(sessionData.getAgentId());
			request.setSessionID(sessionData.getServerSessionID());			
			//request.setBaseUri("http://127.0.0.1:9084");
			request.setBaseUri(restServerConfig.getThisServerBaseUri());
			//request.setCallbackUriPath("/api/account/transaction/notify");
			request.setCallbackUriPath(restServerConfig.getTransactionNotifyPath());
			//request.setTokenUriPath("/oauth/token");
			request.setTokenUriPath(restServerConfig.getOAuthTokenPath());
			request.setTransactionNo(transactionNo);
			if (offset != null || limit != null)
			{
				request.setOffset(offset);
				request.setLimit(limit);
			} 
			
			String restServerUrl = restServerUrlTdr + "/register/notification";
			if (offset != null || limit != null)
			{
				RestRequestUtil.standardPaging(uriBuilder, offset, limit);
			}
			restTemplate.execute(restServerUrl, HttpMethod.POST, request, Void.class);
			try
			{
				if(!notificationMap.containsKey(sessionID))
				{
					notificationMap.put(sessionID, syncObj);
				} else {
					syncObj = notificationMap.get(sessionID);
				}
				synchronized (syncObj)
				{
					syncObj.wait(timeout * 1000);
					//Get Transactions from shared inbox buffer
					List<Transaction> transactionList;
					if(transactionInbox.containsKey(sessionID))
						transactionList = transactionInbox.get(sessionID);
					else //Should return 504
						throw new  GuiGeneralException(String.format("Notification request timeout after %d seconds.", timeout));
					String agentMsisdn = sessionData.getAgentMsisdn();
					for(Transaction transaction:transactionList)
					{
						GuiTransaction guiTransaction = new GuiTransaction(transaction, agentMsisdn);
						guiTransactions.add(guiTransaction);
					}
				}
			} finally {
				if(transactionInbox.containsKey(sessionID))
					transactionInbox.remove(sessionID);
				if(notificationMap.containsKey(sessionID))
					notificationMap.remove(sessionID);
			}
			logger.debug("Done");
		}
 
		return guiTransactions.toArray(new GuiTransaction[guiTransactions.size()]);
	}
	
	public void receiveLatestTransactions(TransactionNotificationCallback callback)
	{
		String sessionID = callback.getSessionID();
		//Transaction[] transactions = new Transaction[callback.getTransactions().size()];  
		//callback.getTransactions().toArray(transactions);
		Transaction[] transactions = callback.getTransactions();
		Object syncObj = notificationMap.get(sessionID);
		if(syncObj != null)
		{
			synchronized (syncObj)
			{
				List<Transaction> transactionList =  Arrays.asList(transactions);
				transactionInbox.put(sessionID, transactionList);
				syncObj.notifyAll();
			}
		} else {
			logger.error("Transaction server is pushing transactions to the API for a session that is not registered. sessionID={}", sessionID);
		}
	}
	
	private String translateTransactionSortFields(String sort)
	{
		String[] fields = sort.split("[\\-\\+]");
		char[] operands = sort.replaceAll("[^\\-\\+]", "").toCharArray();
		StringBuilder translated = new StringBuilder();
		for(int i = 0; i < fields.length; i++)
		{
			String field = fields[i];
			char operand;
			if(i >= operands.length)
			{
				operand = '+';
			} else {
				operand = operands[i];
			}
			if(sortFieldNameMap.containsKey(field))
			{
				translated.append(sortFieldNameMap.get(field));
				translated.append(operand);
			}
		}
		return translated.toString(); 
	}

	public GuiTransaction saleBundle(SellBundleRequest request) throws Exception {
		ArrayList<Violation> violations = new ArrayList<Violation>();
		GuiTransaction transaction = null;
		request.setSessionID(sessionData.getServerSessionID());
		request.setBundleID(request.getBundleID());
		
		// Do some checks and populate violations
		if (!violations.isEmpty()) {
			throw new GuiValidationException(violations);
		}

		SellResponse sellResponse = restTemplate.postForObject(
				restServerUrlTransaction + "/sell_bundle",
				request,
				SellResponse.class);

		if (sellResponse.getReturnCode().equals("SUCCESS")) {
			String transactionNumber = sellResponse.getTransactionNumber();
			transaction = getTransactionByNumber(transactionNumber);
		}
		return transaction;
	}
}

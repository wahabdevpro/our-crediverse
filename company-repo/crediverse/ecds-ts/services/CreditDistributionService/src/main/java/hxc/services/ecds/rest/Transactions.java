package hxc.services.ecds.rest;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Context;

import hxc.services.ecds.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.RequestHeader;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.TransactionRequest;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.ICallbackItem;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.notification.INotificationText;
import hxc.utils.calendar.DateTime;

public abstract class Transactions<Treq extends TransactionRequest, Tresp extends TransactionResponse>
{
	final static Logger logger = LoggerFactory.getLogger(Transactions.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	protected ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static String computerName = null;
	private StringExpander<TransactionState<Treq, Tresp>> expander = new StringExpander<TransactionState<Treq, Tresp>>()
	{
		@Override
		protected String expandField(String englishName, Locale locale, TransactionState<Treq, Tresp> state)
		{
			return Transactions.this.expandField(englishName, locale, state);
		}
	};

	private StringExpander<TransactionState<Treq, Tresp>> externalDataExpander = new StringExpander<TransactionState<Treq, Tresp>>()
	{
		@Override
		protected String expandField(String englishName, Locale locale, TransactionState<Treq, Tresp> state)
		{
			return Transactions.this.expandExternalDataField(englishName, state);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public static String getDefaultLanguageID2()
	{
		return "en";
	}

	public static String getDefaultLanguageID3()
	{
		return "eng";
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public Tresp execute(Treq request)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			Date originTimeStamp = new Date();
			return execute(em, request, originTimeStamp);
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			Tresp response = request.createResponse();
			response.setReturnCode(TransactionsConfig.ERR_TECHNICAL_PROBLEM);
			String combined = ResponseUtils.combineAdditionalInformation(
					response.getAdditionalInformation(),
					ex.getMessage(),
					ResponseUtils.getGarnishFlag(request.getRequestOriginInterface())
			);
			response.setAdditionalInformation(combined);
			return response;
		}
	}

	public void setBenchmarksPoint(String message, Treq request)
	{
		long now = System.currentTimeMillis() / 1000L;

		logger.trace("[benchmarks] TransactionID:{} {} {}", request.getInboundTransactionID(), message, now);

	}

	public Tresp execute(EntityManager em, Treq request, Date originTimeStamp) {
		long begin = System.currentTimeMillis() / 1000L;
		context.throttleTps();
		logger.info("Starting {} transaction...", getType());

		// Create response
		TransactionState<Treq, Tresp> state = new TransactionState<Treq, Tresp>();
		state.setContext(context);
		state.setRequest(request);
		Tresp response = request.createResponse();
		response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
		state.setResponse(response);

		getHostName();

		// Entity Manager Scope
		Session session = null;
		try {
			session = context.getSession(request.getSessionID());
			state.setSession(session);
			Transaction transaction = createTransaction(request, state, response, session);
			state.setTransaction(transaction);

			if (!RequestHeader.MODE_NORMAL.equals(request.getMode())) {
				throw new RuleCheckException(StatusCode.FORBIDDEN, "mode", "Only Normal Mode allowed");
			}
			CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
			checkWhetherExpired(em, originTimeStamp, companyInfo);	// Check if request is stale
			String transactionNumber = companyInfo.getNextTransactionNumber(session.getCompanyID());
			transaction.setNumber(transactionNumber);
			logger.info("Link to Financial Transaction {}", transactionNumber);


			try {
				RuleCheck.validate(request);
				doPrevalidationActions(em, state);
				validate(em, state);
				if (response.wasSuccessful()) {
					execute(em, state);
				}
			} catch (RuleCheckException ex) {
				populateResponseOnRuleCheckEx(response, ex, request.getRequestOriginInterface());
				transaction = em.merge(transaction);	// After an exception 'transaction' becomes detached.
			}

			try {
				boolean useNewEntityManager = !em.isOpen();
				if (useNewEntityManager) {
					em = context.getEntityManager();
				}
				try (RequiresTransaction trans = new RequiresTransaction(em)) {
					if (useNewEntityManager) {
						Transaction transaction2 = Transaction.findByID(em, transaction.getId(), transaction.getCompanyID());
						if (transaction2 == null) {
							transaction.persist(em, null, state.getSession(), null);
						} else {
							transaction = transaction2;
						}
						state.setTransaction(transaction);
					} else if (!em.contains(transaction)) {
						transaction.persist(em, null, state.getSession(), null);
					}
	
					transaction
							.setEndTime(new Date())
							.setReturnCode(response.getReturnCode())
							.setAdditionalInformation(response.getAdditionalInformation())
							.setLastTime(transaction.getLastTime() == null ? new Date() : transaction.getLastTime());
	
					if (transaction.getA_Tier() != null) {
						transaction.setA_Tier(Tier.findByID(em, transaction.getA_Tier().getId(), session.getCompanyID()));
					}
					if (transaction.getB_Tier() != null) {
						transaction.setB_Tier(Tier.findByID(em, transaction.getB_Tier().getId(), session.getCompanyID()));
					}
					trans.commit();
				}

				context.writeTDR(transaction);
	
				// Conclude - May not throw or fail anymore
				try {
					if (response.wasSuccessful()) {
						conclude(em, state);
					} else {
						concludeAfterFailure(em, state);
					}
				} catch (Throwable ex) {
					logger.error(ex.getMessage(), ex);
				}
	
				response.setTransactionNumber(transaction.getNumber());
				notifyClientsOfTransaction(em, session, transaction);
			} finally {
				if (em.isOpen()) {
					em.close();
				}
			}
		} catch (RuleCheckException ex) {
			populateResponseOnRuleCheckEx(response, ex, request.getRequestOriginInterface());
		} catch (Throwable ex) {
			popuplateResponseOnThrowable(response, ex, request.getRequestOriginInterface());
		}

		logTheResult(request, begin, response, session);
		return response;
	}

	protected void doPrevalidationActions(EntityManager em, TransactionState<Treq,Tresp> state) throws RuleCheckException {
		/* You can override this method in the subclasses of Transactions, if you need.
		This method is introduced during the creation of DebitService to separate
		real validation from other actions which is done in validate method. */
	}

	private void popuplateResponseOnThrowable(Tresp response, Throwable ex, String requestOriginInterface ) {
		logger.error(ex.getMessage(), ex);
		response.setReturnCode(TransactionsConfig.ERR_TECHNICAL_PROBLEM);
		String combined = ResponseUtils.combineAdditionalInformation(
				response.getAdditionalInformation(),
				ex.getMessage(),
				ResponseUtils.getGarnishFlag(requestOriginInterface)
		);
		response.setAdditionalInformation(combined);

	}

	private void populateResponseOnRuleCheckEx(Tresp response, RuleCheckException ex, String requestOriginInterface) {
		logger.info("rulecheck", ex);
		response.setReturnCode(ex.getError());
		String combined = ResponseUtils.combineAdditionalInformation(
				response.getAdditionalInformation(),
				ex.getMessage(),
				ResponseUtils.getGarnishFlag(requestOriginInterface)

		);
		response.setAdditionalInformation(combined);
		response.setResponse(ex.getMessage());
	}

	private void checkWhetherExpired(EntityManager em, Date originTimeStamp, CompanyInfo companyInfo) throws RuleCheckException {
		TransactionsConfig config = companyInfo.getConfiguration(em, TransactionsConfig.class);
		if (hasExpired(originTimeStamp, config.getChannelRequestTimeoutSeconds())) {
			throw new RuleCheckException(TransactionsConfig.ERR_TIMED_OUT, null, "Request is stale");
		}
	}

	private Transaction createTransaction(Treq request, TransactionState<Treq, Tresp> state, Tresp response, Session session) {
		return new Transaction()
				.setReturnCode(response.getReturnCode())
				.setRolledBack(false)
				.setFollowUp(false)
				.setStartTime(new Date())
				.setEndTime(new Date())
				.setChannel(session.getChannel())
				.setChannelType(session.getChannelType())
				.setHostname(computerName)
				.setInboundTransactionID(request.getInboundTransactionID())
				.setInboundSessionID(request.getInboundSessionID())
				.setRequestMode(request.getMode())
				.setType(getType())
				.setCallerID(session.getDomainAccountName())
				.setRequesterMSISDN(session.getMobileNumber())
				.setRequesterType(state.getRequesterType())
				.setCompanyID(session.getCompanyID())
				.setLastTime(new Date());
	}

	private void logTheResult(Treq request, long begin, Tresp response, Session session) {
		String transactionNo = response.getTransactionNumber();
		if (transactionNo == null) {
			transactionNo = "";
		}

		if (response.wasSuccessful()) {
			logger.info("{} Transaction {} concluded", getType(), transactionNo);
		} else {
			String responseText = response.getResponse();
			if (responseText == null || responseText.isEmpty()) {
				response.setResponse(findErrorText(null, session.getCompanyID(), session.getLanguageID(), response.getReturnCode()));
			}
			logger.info("{} Transaction {} failed", getType(), transactionNo);
			logger.info(response.getResponse());
		}

		long threshold = 15;
		long end = System.currentTimeMillis() / 1000L;

		if ((end - begin) > threshold) {
			logger.info("[benchmarks] [DELAYED] TransactionID:{} end {}", request.getInboundTransactionID(), end);
		}
	}

	private void notifyClientsOfTransaction(EntityManager em, Session session, Transaction transaction)
	{
		try
		{
			HashSet<ICallbackItem> doneItems = new HashSet<ICallbackItem>();
			synchronized (context.getCallbackItemsLock())
			{
				if(transaction.getA_AgentID() != null && context.isAgentTaggedForCallback(transaction.getA_AgentID()))
				{
					HashSet<ICallbackItem> callbackItems = context.getCallbackItems(transaction.getA_AgentID());
					logger.debug("Post back to API server for A_Agent, agentID {} is registered for callback by {} sessions.", transaction.getA_AgentID(), callbackItems.size());
					for(ICallbackItem callbackItem: callbackItems)
					{
						try
						{
							String sessionID = callbackItem.getSessionID();
							String baseUri = callbackItem.getBaseUri();
							String callbackUriPath = callbackItem.getCallbackUriPath();
							String tokenUriPath = callbackItem.getTokenUriPath();
							int offset = callbackItem.getOffset() > 0 ? callbackItem.getOffset() : -1;
							int limit = callbackItem.getLimit() > 0 ? callbackItem.getLimit() : -1 ;
							RestParams params = new RestParams(sessionID, offset, limit, null, null, String.format("number>'%s'+", callbackItem.getTransactionNo()));
							List<Transaction> transactions = Transaction.findAll(em, params, session.getCompanyID(), transaction.getA_AgentID(), transaction.getA_MSISDN());
							if(transactions.isEmpty())
								transactions.add(transaction);
							logger.info("Transaction Notification Pushed to sessionID {}, A_AgentID {}, triggered by transactionID {}", sessionID, transaction.getA_AgentID(), transaction.getId());
							context.pushTransactionNotification(sessionID, transaction.getA_AgentID(), baseUri, tokenUriPath, callbackUriPath, transactions, true);
						} catch (Exception e) {
							logger.error("Ignoring exception in transaction caused by transaction push notification. sessionID {}; A_agentID {}; exception {}", callbackItem.getSessionID(), callbackItem.getAgentID(), e);
						} finally {
							doneItems.add(callbackItem);
						}
					}
				}
				if(transaction.getB_AgentID() != null && context.isAgentTaggedForCallback(transaction.getB_AgentID()))
				{
					HashSet<ICallbackItem> callbackItems = context.getCallbackItems(transaction.getB_AgentID());
					logger.debug("Post back to API server for B_AgentID, agentID {} is registered for callback by {} sessions.", transaction.getB_AgentID(), callbackItems.size());
					for(ICallbackItem callbackItem: callbackItems)
					{
						try {
							String sessionID = callbackItem.getSessionID();
							String baseUri = callbackItem.getBaseUri();
							String callbackUri = callbackItem.getCallbackUriPath();
							String tokenUri = callbackItem.getTokenUriPath();
							int offset = callbackItem.getOffset() > 0 ? callbackItem.getOffset() : -1;
							int limit = callbackItem.getLimit() > 0 ? callbackItem.getLimit() : -1 ;
							RestParams params = new RestParams(sessionID, offset, limit, null, null, String.format("number>'%s'+", callbackItem.getTransactionNo())); 
							List<Transaction> transactions = Transaction.findAll(em, params, session.getCompanyID(), transaction.getB_AgentID(), transaction.getB_MSISDN());
							if(transactions.isEmpty())
								transactions.add(transaction);
							logger.info("Transaction Notification Pushed to sessionID {}, B_AgentID {}, triggered by transactionID {}", sessionID, transaction.getB_AgentID(), transaction.getId());
							context.pushTransactionNotification(sessionID, transaction.getB_AgentID(), baseUri, tokenUri, callbackUri, transactions, false);
						} catch (Exception e) {
							logger.error("Ignoring exception in transaction caused by transaction push notification. sessionID {}; B_AgentID {}; exception {}", callbackItem.getSessionID(), callbackItem.getAgentID(), e.toString());
						} finally {
							doneItems.add(callbackItem);
						}
					}
				}
				for(ICallbackItem callbackItem: doneItems)
				{
					context.deregisterSessionFromCallback(callbackItem.getSessionID(), callbackItem.getAgentID());
				}
			}
		} catch (Exception e) {
			logger.warn("Ignoring exception in transaction caused by transaction push notification.", e);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Abstract Methods
	//
	// /////////////////////////////////
	protected abstract String getType();

	protected abstract void validate(EntityManager em, //
			TransactionState<Treq, Tresp> state) throws RuleCheckException;

	protected abstract void execute(EntityManager em, //
			TransactionState<Treq, Tresp> state) throws RuleCheckException;

	protected abstract void conclude(EntityManager em, //
			TransactionState<Treq, Tresp> state);

	protected void concludeAfterFailure(EntityManager em, //
			TransactionState<Treq, Tresp> state)
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	protected void fillHeader(RequestHeader request, Session session, IInteraction interaction)
	{
		request.setSessionID(session.getSessionID());
		request.setInboundTransactionID(interaction.getInboundTransactionID());
		request.setInboundSessionID(interaction.getInboundSessionID());
		request.setVersion(RequestHeader.VERSION_CURRENT);
		request.setMode(RequestHeader.MODE_NORMAL);
		request.setRequestOriginInterface(interaction.getOriginInterface());
	}

	private boolean hasExpired(Date originTimeStamp, Integer channelRequestTimeoutSeconds)
	{
		if (originTimeStamp == null || channelRequestTimeoutSeconds == null)
			return false;
		DateTime cutoff = DateTime.getNow().addSeconds(-channelRequestTimeoutSeconds);
		return originTimeStamp.before(cutoff);
	}

  private String formatAgentUserStatus(String status) {
	// possible values are "S", "D", "?"
	// Not allowed to trade in User State: %s
	if (status == null) status = "null";
	String result = status;
	switch(status) {
	  case "S":
		result = "Suspended";
		break;
	  case "D":
		result = "Deactivated";
		break;
	  case "?":
	  default:
		result = "Unknown "+status;
		break;
	}
	return result;
  }

  private String formatAgentUserState(String status) {
	String result = status;
	return result;
  }

	public void validateAgentState(IAgentUser user, boolean expectTemporary, String... allowedStates) throws RuleCheckException
	{
		// Test for Temporary PIN
		if (expectTemporary && !user.isTemporaryPin())
			throw new RuleCheckException(TransactionsConfig.ERR_ALREADY_REGISTERED, "temporaryPin", "Agent/User has already been registered");
		else if (!expectTemporary && user.isTemporaryPin())
			throw new RuleCheckException(TransactionsConfig.ERR_NOT_REGISTERED, "temporaryPin", "");

		// Test for Valid Agent Status
		if (user instanceof AgentUser)
		{
			AgentUser agentUser = (AgentUser) user;
			String state = agentUser.getAgent().getState();
			if (!Agent.STATE_ACTIVE.equals(state))
				throw new RuleCheckException(TransactionsConfig.ERR_INVALID_STATE, state, "Not allowed to trade Agent State: %s", formatAgentUserState(state));
		}

		// Test for valid Status
		String status = user == null ? "?" : user.getState();
		for (String allowedState : allowedStates)
		{
			if (status.equals(allowedState))
				return;
		}

		throw new RuleCheckException(TransactionsConfig.ERR_INVALID_STATE, status, "Not allowed to trade in User State: %s", formatAgentUserStatus(status));
	}

	public void validateAgentImsi(EntityManager em, TransactionState<Treq, Tresp> state, IAgentUser agent) throws RuleCheckException
	{
		ICreditDistribution context = state.getContext();
		Session session = state.getSession();
		TransactionsConfig transactionsConfig = state.getConfig(em, TransactionsConfig.class);
		agent.validateAgentImsi(context, em, transactionsConfig, session);
	}

	public String expandNotification(Phrase notification, Phrase[] fields, Locale locale, TransactionState<Treq, Tresp> state)
	{
		return expander.expandNotification(notification, locale, fields, state);
	}

	public String expandField(String englishName, Locale locale, TransactionState<Treq, Tresp> state)
	{
		switch (englishName)
		{
			case TransactionsConfig.TRANSACTION_NO:
				return state.getTransaction().getNumber();

			default:
				return "  ";
		}
	}

	public String expandExternalData(Phrase notification, Phrase[] fields, TransactionState<Treq, Tresp> state)
	{
		return externalDataExpander.expandNotification(notification, null, fields, state);
	}

	public List<String> expandExternalDataList(List<Phrase> externalDataList, Phrase[] fields, TransactionState<Treq, Tresp> state)
	{
		if ( externalDataList == null ) return null;
		List<String> result = new ArrayList<String>(externalDataList.size());
		for( Phrase externalDataPhrase: externalDataList )
		{
			String externalDataString = ( externalDataPhrase != null ? this.expandExternalData(externalDataPhrase, fields, state) : null );
			if ( externalDataString != null )
			{
				if ( externalDataString.length() > 128 ) externalDataString.substring(0,128);
				if ( externalDataString.length() < 1 ) externalDataString = null;
			}
			result.add(externalDataString);
		}
		return result;
	}

	public String expandExternalDataField(String fieldName, TransactionState<Treq, Tresp> state)
	{
		switch (fieldName)
		{
			case TransactionsConfig.TRANSACTION_NO:
				return state.getTransaction().getNumber();
			case TransactionsConfig.SENDER_MSISDN:
				return state.getTransaction().getA_MSISDN();
			case TransactionsConfig.RECIPIENT_MSISDN:
				return state.getTransaction().getB_MSISDN();
			case TransactionsConfig.AMOUNT:
				return state.getTransaction().getAmount().toString();
			default:
				return "  ";
		}
	}

	public String format(Locale locale, BigDecimal amount)
	{
		if (amount == null || locale == null)
		{
			logger.warn("format called with null amount or locale ...");
			return "";
		}

		NumberFormat numberFormat = context.getCurrencyFormat(locale);
		return numberFormat.format(amount);
	}

	public String formatDate(Locale locale, Date date)
	{
		if (date == null)
		{
			logger.warn("formatDate called with null date ...");
			return "";
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		return df.format(date);
	}

	public void sendResponse(Phrase notification, Phrase[] fields, TransactionState<Treq, Tresp> state)
	{
		Session session = state.getSession();
		Locale sessionLocale = session.getLocale();
		logger.trace("Sending response with ( session.languageID = {}, session.countryID = {}, session.locale = {} )", session.getLanguageID(), session.getCountryID(),
				sessionLocale);
		String text = expandNotification(notification, fields, sessionLocale, state);
		state.getResponse().setResponse(text);
		context.sendSMS(session.getMobileNumber(), sessionLocale.getISO3Language(), text);
	}

	public void sendNotification(String mobileNumber, Phrase notification, Phrase[] fields, Locale locale, TransactionState<Treq, Tresp> state)
	{
		logger.trace("Sending notification with locale {}", locale);
		if (mobileNumber == null)
		{
			logger.error("Attempted to send notification with null mobileNumber");
			return;
		}
		if (mobileNumber.isEmpty())
		{
			logger.error("Attempted to send notification with empty mobileNumber");
			return;
		}
		if (notification == null)
		{
			logger.error("Attempted to send notification with null notification");
			return;
		}
		String text = expandNotification(notification, fields, locale, state);
		context.sendSMS(mobileNumber, locale.getISO3Language(), text);
	}

	public void respond(IInteraction interaction, final Session session, final TransactionResponse response)
	{
		if (interaction == null || session == null || response == null)
			return;

		// Only if USSD
		if (!Session.CHANNEL_USSD.equals(session.getChannel()) && response.wasSuccessful())
			return;

		// Handle Errors
		String responseText = response.getResponse();
		boolean empty = responseText == null || responseText.isEmpty();
		final String respondText = !empty ? responseText : //
				findErrorText(null, session.getCompanyID(), session.getLanguageID(), response.getReturnCode());

		// Respond back to Channel
		final INotificationText notificationText = new INotificationText()
		{
			@Override
			public String getText()
			{
				return respondText;
			}

			@Override
			public String getLanguageCode()
			{
				return session.getLocale().getISO3Language();
			}

		};
		interaction.reply(notificationText);

	}

	// Remove USSD Prompt
	protected void disableUssdConfirmation(EntityManager em, Session session, int options)
	{
		hxc.services.ecds.model.Agent agent = session.getAgent();
		if ((options & IMenuProcessor.OPTION_OPT_OUT) != 0 && agent != null && agent.isConfirmUssd())
		{
			try (RequiresTransaction ts = new RequiresTransaction(em))
			{
				if (!em.contains(agent))
				{
					agent = hxc.services.ecds.model.Agent.findByID(em, agent.getId(), agent.getCompanyID());
					session.setAgent(agent);
				}
				agent.setConfirmUssd(false);
				em.persist(agent);
				ts.commit();
				logger.info("Removed USSD Prompt for Agent {}", agent.getId());
			}
		}
	}

	public boolean respond(Session session, IInteraction interaction, String responseCode, int companyID)
	{
		return respond( //
				companyID, //
				session == null ? Phrase.ENG : session.getLanguageID(), //
				session == null ? "eng" : session.getLocale().getISO3Language(), //
				interaction, //
				responseCode);
	}

	// Respond
	public boolean respond(int companyID, String languageID, final String languageID3, IInteraction interaction, String responseCode)
	{
		final boolean returnValue = true; // Always

		if (interaction == null)
			return returnValue;

		// Lookup response code
		final String respondText = findErrorText(null, companyID, languageID, responseCode);

		// Respond back to Channel
		final INotificationText notificationText = new INotificationText()
		{
			@Override
			public String getText()
			{
				return respondText;
			}

			@Override
			public String getLanguageCode()
			{
				return languageID3;
			}

		};
		interaction.reply(notificationText);

		return returnValue;
	}

	public TransactionResponse respondWithError(final String errorCode)
	{
		return new TransactionResponse()
		{
			@Override
			public boolean wasSuccessful()
			{
				return false;
			}

			@Override
			public String getReturnCode()
			{
				return errorCode;
			}
		};
	}

	private String findErrorText(EntityManager em, int companyID, String languageID, String returnCode)
	{
		// Load Company Info
		CompanyInfo company = context.findCompanyInfoByID(companyID);
		if (company == null)
			return null;

		// Load Transactions Config
		TransactionsConfig config = company.getConfiguration(null, TransactionsConfig.class);
		if (config == null && em == null)
		{
			try (EntityManagerEx em2 = context.getEntityManager())
			{
				config = company.getConfiguration(em2, TransactionsConfig.class);
			}
		}
		if (config == null)
			return returnCode;

		return config.findErrorText(languageID, returnCode);

	}

	public void updateAgentImei(EntityManager em, TransactionState<Treq, Tresp> state, IAgentUser agent) throws RuleCheckException
	{
		ICreditDistribution context = state.getContext();
		Session session = state.getSession();
		TransactionsConfig transactionsConfig = state.getConfig(em, TransactionsConfig.class);
		agent.updateAgentImei(context, em, transactionsConfig, session);
	}

	public String mapAirResponseCode(int airResponseCode)
	{
		switch (airResponseCode)
		{
			case 100:
				return TransactionsConfig.ERR_OTHER_ERROR;

			case 102:
				return TransactionsConfig.ERR_INVALID_RECIPIENT;

			case 103:
				return TransactionsConfig.ERR_REFILL_BARRED;

			case 104:
				return TransactionsConfig.ERR_TEMPORARY_BLOCKED;

			case 115:
				return TransactionsConfig.ERR_REFILL_NOT_ACCEPTED;

			case 120:
			case 176:
			case 177:
			case 178:
			case 179:
				return TransactionsConfig.ERR_REFILL_DENIED;

			default:
				return TransactionsConfig.ERR_REFILL_FAILED;
		}

	}

	public static String getHostName()
	{
		if (computerName == null || computerName.isEmpty())
		{
			try
			{
				InetAddress ip = InetAddress.getLocalHost();
				String name = ip.getHostName();
				int index = name.indexOf('.');
				if (index > 0)
					name = name.substring(0, index);
				computerName = name;
			}
			catch (Exception ex)
			{
				logger.warn("Failed to get hostname, defaulting to 'localhost'", ex);
				computerName = "localhost";
			}
		}

		return computerName;
	}

	protected boolean isDeterministic(int[] nonDeterministicErrorCodes, int responseCode)
	{
		if (nonDeterministicErrorCodes == null)
			return true;
		
		for (int index = 0; index < nonDeterministicErrorCodes.length; index++)
		{
			if (responseCode == nonDeterministicErrorCodes[index])
				return false;
		}
		
		return true;
	}
	
}

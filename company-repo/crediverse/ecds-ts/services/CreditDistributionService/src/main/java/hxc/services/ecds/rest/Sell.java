package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.THE_RECIPIENT_NUMBER_AGAIN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_MSISDNS_MISMATCH;
import static hxc.services.ecds.rest.TransactionHelper.createDaRefillInfoList;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.isEmpty;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static hxc.services.ecds.rest.ussd.MenuConstructor.createBNumberConfirmationMenu;
import static hxc.services.ecds.util.MobileNumberFormatHelper.MOBILE_NUMBER_FORMAT_HELPER;
import static hxc.services.ecds.util.MobileNumberFormatHelper.convertBNumber;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.removeBNumberConfirmationFromMenu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import hxc.services.ecds.util.ConfirmationMenuHelper;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.ResponseUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.connectors.IInteraction;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.SellRequest;
import hxc.ecds.protocol.rest.SellResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.IConfirmationMenuConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionExtraData;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuOption;
import hxc.services.ecds.rest.ussd.MenuProcessor;
import hxc.services.transactions.ITransaction;
import hxc.utils.protocol.ucip.RefillResponse;

import hxc.ecds.protocol.rest.SelfTopUpRequest;
import hxc.ecds.protocol.rest.SelfTopUpResponse;

@Path("/transactions")
public class Sell extends Transactions<hxc.ecds.protocol.rest.SellRequest, hxc.ecds.protocol.rest.SellResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(Sell.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_SUBSCRIBER_LANGUAGE = "SUBS_LANG";
	private static final String PROP_RECIPIENT_BALANCE_AFTER = "ACT_RCPT_BALANCE_AFTER";
	private static final String PROP_RECIPIENT_BALANCE_BEFORE = "ACT_RCPT_BALANCE_BEFORE";
	private static final String PROP_PARTIAL_RECOVERY = "PARTIAL_RECOVERY";
	private static final String PROP_FULL_RECOVERY = "FULL_RECOVERY";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Sell()
	{

	}

	public Sell(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/sell")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.SellResponse execute(hxc.ecds.protocol.rest.SellRequest request)
	{
		Session session = null;
		boolean sameMSISDN = false;
		SellResponse sellResponse = request.createResponse();
		try {
			session = context.getSession(request.getSessionID());
			String bMSISDN = context.toMSISDN(request.getTargetMSISDN());
			if (session != null) {

				// Check if it's a self top-up request
				if(bMSISDN.equals(session.getAgent().getMobileNumber())) 
				{
					sameMSISDN = true;
				}
			}
		
			// Redirect to self top-up transaction handler
			if (sameMSISDN) {
				logger.info("Transaction identified as a Self top-up transaction. Redirecting to self top-up endpoint...");
				
				// Create a Self Top-Up request structure from the Sell Request structure
				SelfTopUpRequest selfTopUpRequest = new SelfTopUpRequest();
				selfTopUpRequest.setAmount(request.getAmount());
				selfTopUpRequest.setInboundSessionID(request.getInboundSessionID());
				selfTopUpRequest.setInboundTransactionID(request.getInboundTransactionID());
				selfTopUpRequest.setMode(request.getMode());
				selfTopUpRequest.setSessionID(request.getSessionID());
				selfTopUpRequest.setVersion(request.getVersion());
				selfTopUpRequest.setLatitude(request.getLatitude());
				selfTopUpRequest.setLongitude(request.getLongitude());

				SelfTopUp selfTopUp = new SelfTopUp(context);
				SelfTopUpResponse selfTopUpResponse = selfTopUp.execute(selfTopUpRequest);
				
				// Create a Sell response structure from the Self Top-up Response structure
				sellResponse.setResponse(selfTopUpResponse.getResponse());
				sellResponse.setCharge(selfTopUpResponse.getCharge());
				sellResponse.setTransactionNumber(selfTopUpResponse.getTransactionNumber());
				sellResponse.setReturnCode(selfTopUpResponse.getReturnCode());
				sellResponse.setAdditionalInformation(selfTopUpResponse.getAdditionalInformation());
				return sellResponse;
			} else // Process normal sell transaction
				return super.execute(request);
		
		} catch (RuleCheckException ex) {
			sellResponse.setReturnCode(ex.getError());
			sellResponse.setAdditionalInformation(ex.getMessage());
			sellResponse.setResponse(ex.getMessage());
			return sellResponse;
		} catch (Exception ex) {
			sellResponse.setReturnCode(TransactionsConfig.ERR_TECHNICAL_PROBLEM);
			sellResponse.setAdditionalInformation(ex.getMessage());
			sellResponse.setResponse(ex.getMessage());
			return sellResponse;
		}
	}

	@Override
	protected String getType()
	{
		return hxc.ecds.protocol.rest.Transaction.TYPE_SELL;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<SellRequest, SellResponse> state) throws RuleCheckException
	{
		// Record Request Information
		SellRequest request = state.getRequest();
		Session session = state.getSession();
		String bMSISDN = context.toMSISDN(request.getTargetMSISDN());
		state.setRequestInfo(session.getMobileNumber(), bMSISDN, request.getAmount());

		// Get the Configuration
		SalesConfig salesConfig = state.getConfig(em, SalesConfig.class);

		// Check Permission
		session.check(em, Transaction.MAY_SELL, true);

		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, false);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());
		IAgentUser user = state.getSessionUser(em);

		// Validate the A Agent
		validateAgentState(user, false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE);

		// Validate the A Agent's IMSI
		validateAgentImsi(em, state, user);

		// Update the A Agent's IMEI
		updateAgentImei(em, state, user);

		// Set the A Agent
		state.setAgentA(aAgent);

		// Cannot sell to self
		boolean sellToSelfNotAllowed = salesConfig.getAllowSellToSelf() == null || !salesConfig.getAllowSellToSelf();
		if (sellToSelfNotAllowed && aAgent.getMobileNumber().equals(bMSISDN)) {
			throw new RuleCheckException(TransactionsConfig.ERR_NOT_SELF, "targetMSISDN", "Cannot Sell to Self");
		}

		// Validate the Amount
		state.testAmountDecimalDigits(state.getRequest().getAmount());

		// Save the B_Party MSISDN
		Transaction transaction = state.getTransaction();
		transaction.setB_MSISDN(bMSISDN);
		Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
		transaction.setB_TierID(subscriberTier.getId());

		// Get cached location
		SalesConfig config = state.getConfig(em, SalesConfig.class);
		state.getCachedLocation(em, user, config.isForceLocationOfAgent());

		// Get the Transfer Rule
		TransferRule transferRule = state.findTransferRule(em, //
				state.getRequest().getAmount(), //
				transaction.getStartTime(), //
				user, aAgent, //
				null, //
				subscriberTier.getId());

		transaction.setTransferRuleID(transferRule.getId());
		transaction.setTransferRule(transferRule);

		// Enforce Strict Area
		state.enforceAStrictArea(em, user, aAgent);

		// Obtain Location for Promotional Purposes
		state.obtainALocationForPromotions(em, user);
	}

	@Override
	protected void execute(EntityManager em, TransactionState<SellRequest, SellResponse> state) throws RuleCheckException {
		BigDecimal amount = state.getRequest().getAmount();
		logger.info("SELLING ammount = {}", amount );

		Transaction transaction = state.getTransaction();
		logger.info("SELLING transaction = {}", transaction );
		String requestOrigin = state.getRequest().getRequestOriginInterface();
		String combined = ResponseUtils.combineAdditionalInformation(
				state.getResponse().getAdditionalInformation(),
				ResponseUtils.getGarnishFlag(requestOrigin)
		);

		state.getResponse().setAdditionalInformation(combined);
		ITransaction tx = defineAirTransaction(transaction);
		Subscriber subscriber = new Subscriber(transaction.getB_MSISDN(), context.getAirConnector(), tx);

		BigDecimal subscriberCurrentBalance = null;

		try {
			IAirConnector air = context.getAirConnector();
			subscriberCurrentBalance = air.fromLongAmount(subscriber.getAccountValue1());

			logger.info("Subscriber current balance from AIR: {}", subscriberCurrentBalance);
		}
		catch (AirException ex) {
			logger.error("Failed to get subscriber balance from AIR (ignoring): " + ex.getMessage());
		}

		boolean partialRecovery = false;
		boolean fullRecovery = false;

		if (subscriberCurrentBalance != null && subscriberCurrentBalance.signum() < 0) {
			// Loan exists
			BigDecimal loanAmount = subscriberCurrentBalance.abs();

			if (amount.compareTo(loanAmount) < 0) {
				// Recharge is less than LoanAmount
				partialRecovery = true;
			} else {
				fullRecovery = true;
			}
		}
		logger.trace("Subscriber recovery isPartial: {} , isFull: {}", partialRecovery, fullRecovery);

		state.set(PROP_PARTIAL_RECOVERY, partialRecovery);
		state.set(PROP_FULL_RECOVERY, fullRecovery);

		Account aAccount;
		try(RequiresTransaction trans = new RequiresTransaction(em)) {
			aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);

			// Transact
			aAccount.transact(transaction.getStartTime(), amount, BigDecimal.ZERO, BigDecimal.ZERO, false);
			
			transaction.setBuyerTradeBonusAmount(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusProvision(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusPercentage(BigDecimal.ZERO);

			transaction.setAmount(amount);
			transaction.setGrossSalesAmount(amount);

			Transaction lastSuccessfulTxTransaction = Transaction.findLastSuccessfulTransferToAgent(em, transaction.getA_MSISDN(), transaction.getCompanyID());
			BigDecimal defTradeBonusPct = aAccount.getAgent().getTier().getBuyerDefaultTradeBonusPercentage();
			if(lastSuccessfulTxTransaction != null)
			{				
				MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
				logger.info("Using bonus percentage {}% from last transfer transaction for calculating cost of goods sold", 
				lastSuccessfulTxTransaction.getBuyerTradeBonusPercentage().multiply(new BigDecimal(100.0)).setScale(2, RoundingMode.HALF_UP));
				BigDecimal lastCreditPurchased = lastSuccessfulTxTransaction.getAmount();
				BigDecimal lastBonusAmount = lastSuccessfulTxTransaction.getBuyerTradeBonusAmount();
				BigDecimal lastCreditReceived = lastCreditPurchased.add(lastBonusAmount);
				BigDecimal costPerUnit = lastCreditPurchased.divide(lastCreditReceived, mc);
				transaction.setCostOfGoodsSold(amount.multiply(costPerUnit));
			}
			else if(defTradeBonusPct != null) {
				MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
				logger.info("Using default bonus percentage {}% for calculating cost of goods sold", defTradeBonusPct.setScale(2, RoundingMode.HALF_UP));
				
				transaction.setCostOfGoodsSold(amount.divide((defTradeBonusPct.add(new BigDecimal(100.0)).divide(new BigDecimal(100.0), mc)), mc));
			}
			else {
				logger.warn("Bonus percentage unavailable for calculating cost of goods sold.");
			}

			transaction.testAmlLimitsA(aAccount, amount);


			// Set the follow-up flag and move the money in the ON-HOLD.
			// If unexpected exception occurs the transaction will be in this state and have to be fixed manually.
			transaction.setFollowUp(true);
			aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().add(amount));
						
			setTransactionAAfter(transaction, aAccount);

			transaction.persist(em, null, state.getSession(), null);

			Double longitude = state.getRequest().getLongitude();
			Double latitude = state.getRequest().getLatitude();
			long transactionId = transaction.getId();

			if (longitude != null && latitude != null) {
				transaction.persistTransactionLocation(em, longitude, latitude, transactionId);
			}

			updateInDb(em, trans, transaction, aAccount);
		}

		SalesConfig config = state.getConfig(em, SalesConfig.class);
		ReversalsConfig reversalsConfig = state.getConfig(em, ReversalsConfig.class);
		em.close();
		
		try { // Execute the Refill
			IAirConnector air = context.getAirConnector();
			long longAmount = air.toLongAmount(amount);
			List<String> externalDataList = 
				this.expandExternalDataList(
						Arrays.asList( 
							config.getRefillExternalData1(), 
							config.getRefillExternalData2() , 
							config.getRefillExternalData3() , 
							config.getRefillExternalData4() ), 
						config.listExternalDataFields(), state);
			RefillResponse result = subscriber.refillAccount(config.getRefillProfileID(), longAmount,
					config.isAutoActivateAccounts(), externalDataList, reversalsConfig.isEnableDedicatedAccountReversal());
			
			DedicatedAccountRefillInfoAccounts dARefillInfoList = createDaRefillInfoList(result);
			long balanceBefore = 0;
			long balanceAfter = 0;
			if (result.member == null || result.member.accountBeforeRefill == null || result.member.accountAfterRefill == null) {
				logger.warn("No Account Before/After Refill returned for {}", subscriber.getNationalNumber());
			} else {
				balanceBefore = result.member.accountBeforeRefill.accountValue1;
				balanceAfter = result.member.accountAfterRefill.accountValue1;
			}

			try (EntityManagerEx em2 = context.getEntityManager()) {
				try (RequiresTransaction trans = new RequiresTransaction(em2)) {
					aAccount = findAccount(em2, aAccount.getID());
					transaction = Transaction.findByID(em2, transaction.getId(), transaction.getCompanyID());
					state.setTransaction(transaction);
					if (dARefillInfoList != null && !isEmpty(dARefillInfoList.getDedicatedAccountRefillInfos())) {
						logger.debug(" size dARefillInfoList to save: {}", dARefillInfoList.getDedicatedAccountRefillInfos().size());
						//only save extra data if we have dedicated account info returned in the refill info
						transaction.addExtraDataForKeyType(TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO, dARefillInfoList);
					}
					transaction.persistExtraData(em2);

					transaction.setB_BalanceBefore(air.fromLongAmount(balanceBefore));
					BigDecimal recipientBalanceBefore = air.fromLongAmount(balanceBefore);
					state.set(PROP_RECIPIENT_BALANCE_BEFORE,recipientBalanceBefore );
					transaction.setB_BalanceAfter(air.fromLongAmount(balanceAfter));
					BigDecimal recipientBalanceAfter = air.fromLongAmount(balanceAfter);
					state.set(PROP_RECIPIENT_BALANCE_AFTER, recipientBalanceAfter);
					logger.trace("Subscriber balance before refill: {}", recipientBalanceBefore);
					logger.trace("Subscriber balance after refill: {}", recipientBalanceAfter);

					state.set(PROP_SUBSCRIBER_LANGUAGE, subscriber.getLanguageCode2());
					logger.trace("Setting {} -> {}", PROP_SUBSCRIBER_LANGUAGE, subscriber.getLanguageCode2());

					transaction.setFollowUp(false);
					aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
					
					setTransactionAAfter(transaction, aAccount);
					updateInDb(em2, trans, transaction, aAccount);
				}
			}

		}
		catch (AirException e) {
			logger.warn("$$ Air Exception", e);
			try (EntityManagerEx em2 = context.getEntityManager()) {
				try (RequiresTransaction trans = new RequiresTransaction(em2)) {
					aAccount = findAccount(em2, aAccount.getID());
					transaction = Transaction.findByID(em2, transaction.getId(), transaction.getCompanyID());
					state.setTransaction(transaction);
					if (e.isDeterministic() && isDeterministic(config.getNonDeterministicErrorCodes(), e.getResponseCode())) {
						state.exitWith(mapAirResponseCode(e.getResponseCode()), e.getMessage());
						transaction.setLastExternalResultCode(Integer.toString(e.getResponseCode()));
						transaction.setFollowUp(false);
						aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
						aAccount.adjust(amount, BigDecimal.ZERO, false);
						setTransactionAAfter(transaction, aAccount);
					} else {
						// May have succeeded
						String combinedAdditional = ResponseUtils.combineAdditionalInformation(
								state.getResponse().getAdditionalInformation(),
								String.format("May have failed - Follow Up! Air-Node: [%s]", e.getHost()),
								ResponseUtils.getGarnishFlag(requestOrigin)
						);
						state.getResponse().setAdditionalInformation(combinedAdditional);
						state.getResponse().setFollowUp(true);
						setTransactionAAfter(transaction, aAccount);
					}

					updateInDb(em2, trans, transaction, aAccount);				}
			}
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<SellRequest, SellResponse> state)
	{
		boolean partialRecovery = Boolean.TRUE.equals(state.get(PROP_PARTIAL_RECOVERY));
		boolean fullRecovery    = Boolean.TRUE.equals(state.get(PROP_FULL_RECOVERY));
		// Send Sender's Response
		SalesConfig salesConfig = state.getConfig(em, SalesConfig.class);
		Transaction transaction = state.getTransaction();
		boolean followUp = transaction.isFollowUp();
		Phrase notification;
		if (followUp) {
			notification = salesConfig.getSenderUnknownNotification();
		} else {
			// Default notification
			notification = salesConfig.getSenderNotification();

			// Override only if recovery applies
			if (partialRecovery) {
				notification = salesConfig.getSenderNotificationPartialRecovery();
			} else if (fullRecovery) {
				notification = salesConfig.getSenderNotificationFullRecovery();
			}
		}
		sendResponse(notification, salesConfig.listNotificationFields(), state);

		// Send Subscriber's Response
		String subscriberLanguage = state.get(PROP_SUBSCRIBER_LANGUAGE);
		if (subscriberLanguage == null || subscriberLanguage.isEmpty())
			subscriberLanguage = salesConfig.getDefaultSubscriberLanguageID();
		logger.trace("Using subscriberLanguage {}", subscriberLanguage);
		if (followUp) {
			notification = salesConfig.getRecipientUnknownNotification();
		} else {
			// Default notification
			notification = salesConfig.getRecipientNotification();

			// Override only if recovery applies
			if (partialRecovery) {
				notification = salesConfig.getRecipientNotificationPartialRecovery();
			} else if (fullRecovery) {
				notification = salesConfig.getRecipientNotificationFullRecovery();
			}
		}
		sendNotification(state.getTransaction().getB_MSISDN(), notification, salesConfig.listNotificationFields(), //
				state.getLocale(subscriberLanguage), state);
		
		// Send a Stock Depletion message
		Agent aAgent = transaction.getA_Agent();
		BigDecimal threshold = aAgent.getWarningThreshold();
		if (moreThan(transaction.getA_BalanceBefore(), threshold) //
				&& !moreThan(transaction.getA_BalanceAfter(), threshold))
		{
			AgentsConfig agentConfig = state.getConfig(em, AgentsConfig.class);
			sendNotification(aAgent.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(), //
					state.getLocale(aAgent.getLanguage()), state);
			Agent supplier = aAgent.getSupplier();
			if (supplier != null)
			{
				sendNotification(supplier.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(), //
						state.getLocale(supplier.getLanguage()), state);
			}
		}
	}

	private boolean moreThan(BigDecimal left, BigDecimal right)
	{
		return left != null && right != null && left.compareTo(right) > 0;
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<SellRequest, SellResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case SalesConfig.SENDER_MSISDN:
				return state.getSession().getMobileNumber();

			case AgentsConfig.SENDER_THRESHOLD:
				return format(locale, transaction.getA_Agent().getWarningThreshold());

			case SalesConfig.RECIPIENT_MSISDN:
				return state.getTransaction().getB_MSISDN();

			case SalesConfig.AMOUNT:
				return format(locale, transaction.getAmount());

			case SalesConfig.SENDER_NEW_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case SalesConfig.RECIPIENT_NEW_BALANCE:
				return format(locale, transaction.getB_BalanceAfter());
			case SalesConfig.LOAN_AMOUNT:
				BigDecimal loanAmount = state.get(PROP_RECIPIENT_BALANCE_BEFORE);
				return format(locale, loanAmount.abs());
			default:
				return super.expandField(englishName, locale, state);
		}
	}

	@Override
	public String expandExternalDataField(String englishName, TransactionState<SellRequest, SellResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case SalesConfig.SENDER_MSISDN:
				return state.getSession().getMobileNumber();

			case AgentsConfig.SENDER_THRESHOLD:
				return transaction.getA_Agent().getWarningThreshold().toString();

			case SalesConfig.RECIPIENT_MSISDN:
				return state.getTransaction().getB_MSISDN();

			case SalesConfig.AMOUNT:
				return transaction.getAmount().toString();

			case SalesConfig.SENDER_NEW_BALANCE:
				return transaction.getA_BalanceAfter().toString();

			case SalesConfig.RECIPIENT_NEW_BALANCE:
				return transaction.getB_BalanceAfter().toString();
			case SalesConfig.LOAN_AMOUNT:
				BigDecimal loanAmount = state.get(PROP_RECIPIENT_BALANCE_BEFORE);
				return  loanAmount.abs().toString();

			default:
				return super.expandExternalDataField(englishName, state);
		}
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/sell/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.SalesConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return getConfig(em, session);
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private SalesConfig getConfig(EntityManager em, Session session)
	{
		SalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, SalesConfig.class);

		ConfirmationMenuHelper.constructMenus(this, config, //
				Ussd.COMMAND_SELL, //
				"Confirm Sale of " + SalesConfig.AMOUNT + " Airtime to " + SalesConfig.RECIPIENT_MSISDN + "?", //
				"Sale of " + SalesConfig.AMOUNT + " Airtime to " + SalesConfig.RECIPIENT_MSISDN + " Cancelled.", //
				"You sold " + SalesConfig.AMOUNT + " to " + SalesConfig.RECIPIENT_MSISDN + " less than " //
						+ IConfirmationMenuConfig.MINS_SINCE_LAST + " minute(s) ago, do you wish to proceed with this sale?");

		return config;
	}

	@PUT
	@Path("/sell/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.SalesConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_SALES);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);
			defineChannelFilters(configuration, session.getCompanyID());
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IChannelTarget
	//
	// /////////////////////////////////
	@Override
	public void defineChannelFilters(EntityManager em, ICreditDistribution context, CompanyInfo company)
	{
		this.context = context;
		SalesConfig configuration = company.getConfiguration(em, SalesConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(SalesConfig configuration, int companyID)
	{
		context.defineChannelFilter(this, companyID, configuration.getUssdCommand(), configuration.listCommandFields(), 1);
		context.defineChannelFilter(this, companyID, configuration.getSmsCommand(), configuration.listCommandFields(), 2);
	}

	@Override
	public boolean processChannelRequest(int companyID, IInteraction interaction, Map<String, String> values, int tag)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Get a Session
			Session session = context.getSessions().getAgentSession(em, context, companyID, interaction);
			if (session == null)
				return false;
				
			String error = convertBNumber(values, em);
			if (error != null) {
				MOBILE_NUMBER_FORMAT_HELPER.initErrorMessages(session, em);
				return respond(session, interaction, error, companyID);
			}

			// Validate PIN
			String pinResult = session.offerPIN(em, session, true, values.get(SalesConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Must Confirm?
			SalesConfig config = getConfig(em, session);
			String recipientMSISDN = values.get(SalesConfig.RECIPIENT_MSISDN);
			BigDecimal amount = new BigDecimal(values.get(SalesConfig.AMOUNT));
			List<UssdMenu> confirmationMenu = ConfirmationMenuHelper.triggerConfirmation(this, em, interaction, context, config, session, //
					Ussd.COMMAND_SELL, getType(), recipientMSISDN, amount, null, values);

			// Must confirm B number?
			if (config.getEnableBNumberConfirmation()) {
				confirmationMenu = createBNumberConfirmationMenu(
						Ussd.COMMAND_SELL,
						confirmationMenu,
						config.getNumberConfirmMessage());
			} else {
				if (confirmationMenu != null) {
					removeBNumberConfirmationFromMenu(this, confirmationMenu);
				}
			}
			
			if (confirmationMenu != null) {
				IMenuProcessor menuProcessor = this;
				new MenuProcessor(context) {
					@Override
					protected IMenuProcessor getUssdProcessor(int id) {
						return menuProcessor;
					}
				}.initiate(em, interaction, session, confirmationMenu, values, Ussd.COMMAND_SELL);
				return true;
			}

			// Create the request
			SellRequest request = new SellRequest();
			fillHeader(request, session, interaction);
			request.setTargetMSISDN(recipientMSISDN);
			request.setAmount(amount);

			// Execute the Transaction
			SellResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
			respond(interaction, session, response);

		}
		catch (Throwable tr)
		{
			logger.error(tr.getMessage(), tr);
			respond(companyID, getDefaultLanguageID2(), getDefaultLanguageID3(), interaction, TransactionsConfig.ERR_TECHNICAL_PROBLEM);
		}

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IMenuProcessor
	//
	// /////////////////////////////////

	@Override
	public Phrase menuName()
	{
		return Phrase.en("Sell");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		SalesConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, SalesConfig.class);
		return config.listCommandFields();
	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case SalesConfig.RECIPIENT_MSISDN:
				return "the Recipient's number";
			case RECIPIENT_MSISDN_CONFIRMED:
				return THE_RECIPIENT_NUMBER_AGAIN;
			case SalesConfig.PIN:
				return "your PIN";
			case SalesConfig.AMOUNT:
				return "the Amount";
		}
		return fieldName;
	}

	@Override
	public String menuExpandField(String englishName, Session session, Map<String, String> valueMap)
	{
		switch (englishName)
		{
			case SalesConfig.AMOUNT:
				return format(session.getLocale(), new BigDecimal(valueMap.get(SalesConfig.AMOUNT)));
		}

		return null;
	}

	@Override
	public Phrase[] menuInformationFields(EntityManager em, int companyID)
	{
		return new Phrase[0];
	}

	@Override
	public MenuOption[] menuOptions(EntityManager em, Session session, String field)
	{
		return null;
	}

	@Override
	public boolean menuMayExecute(EntityManager em, Session session) throws RuleCheckException
	{
		// Check Permission
		if (!session.hasPermission(em, Transaction.MAY_SELL, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		String error = convertBNumber(values, em);
		if (error != null) {
			MOBILE_NUMBER_FORMAT_HELPER.initErrorMessages(session, em);
			return respondWithError(error);
		}

		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(SalesConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Remove USSD Prompt
		disableUssdConfirmation(em, session, options);

		// Create the request
		SellRequest request = new SellRequest();
		fillHeader(request, session, interaction);
		String recipientMsisdn = values.get(SalesConfig.RECIPIENT_MSISDN);

		// B MSISDN confirmation
		SalesConfig config = getConfig(em, session);
		if (config.getEnableBNumberConfirmation()) {
			if (!recipientMsisdn.equals(values.get(RECIPIENT_MSISDN_CONFIRMED))) {
				TransactionsConfig transactionsConfig = session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class);
				transactionsConfig.getErrorMessages().put(ERR_MSISDNS_MISMATCH, config.getNumberErrorMessage());
				return respondWithError(ERR_MSISDNS_MISMATCH);
			}
		}

		request.setTargetMSISDN(recipientMsisdn);
		request.setAmount(new BigDecimal(values.get(SalesConfig.AMOUNT)));
		request.setRequestOriginInterface(interaction.getOriginInterface());

		// Execute the Transaction
		SellResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

}

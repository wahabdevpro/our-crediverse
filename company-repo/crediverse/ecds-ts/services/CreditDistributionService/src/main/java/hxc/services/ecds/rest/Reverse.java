package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.ResponseHeader.RETURN_CODE_SUCCESS;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_REFILL_FAILED;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.reverseDedicatedAccounts;
import static hxc.services.ecds.rest.TransactionHelper.reverseMainAndDedicatedAccounts;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionABefore;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionBAfter;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.air.AirException;
import hxc.connectors.air.DedicatedAccountNotFoundException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.ReversalRequest;
import hxc.ecds.protocol.rest.ReversalRequestWithCoAuth;
import hxc.ecds.protocol.rest.ReversalResponse;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.transactions.ITransaction;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponseMember;

@Path("/transactions")
public class Reverse extends Transactions<hxc.ecds.protocol.rest.ReversalRequest, hxc.ecds.protocol.rest.ReversalResponse>
{
	final static Logger logger = LoggerFactory.getLogger(Reverse.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_IS_SALE = "IS_SALE";
	private static final String PROP_CO_AUTH_MSISDN = "CO_AUTH_MSISDN";
	private static final String PROP_CO_AUTH_LANG = "CO_AUTH_LANG";
	private static final String PROP_SUBSCRIBER_LANGUAGE = "SUBS_LANG";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/reverse")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ReversalResponse execute(hxc.ecds.protocol.rest.ReversalRequestWithCoAuth request)
	{
		return super.execute(request);
	}

	@POST
	@Path("/reverse_without_co_auth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.ReversalResponse execute(hxc.ecds.protocol.rest.ReversalRequest request) {
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_REVERSE;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();
		state.getConfig(em, ReversalsConfig.class);

		// Only from WUI
		if (!Session.CHANNEL_WUI.equals(session.getChannel()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_CHANNEL, "channel", "Only Web Channel allowed");

		// Check Permission
		session.agentOrCheck(em, Transaction.MAY_REVERSE, true);

		ReversalRequest request = state.getRequest();

		boolean coAuthEnabled = state.getConfig(em, ReversalsConfig.class).isEnableCoAuthReversal();
		if (coAuthEnabled) {
			if (request instanceof ReversalRequestWithCoAuth) {
				validateCoAuth(em, state, session, (ReversalRequestWithCoAuth) request);
			} else {
				logger.error("Co-authorization enabled but the request is not ReversalRequestWithCoAuth.");
				throw new RuleCheckException(StatusCode.UNAUTHORIZED, "", "Need co-authorization. Check system configuration.");
			}
		}

		// Get Original Transaction
		Transaction original = Transaction.findByNumber(em, request.getTransactionNumber(), session.getCompanyID());
		if (original == null)
			throw new RuleCheckException(TransactionsConfig.ERR_TRANSACTION_NOT_FOUND, "transactionNumber", "%s is not a valid Transaction", request.getTransactionNumber());

		// Valid Amount
		BigDecimal amount = original.getAmount();

		// Right Type
		String type = original.getType();
		boolean isSale;
		if (Transaction.TYPE_SELL.equals(type) || Transaction.TYPE_SELF_TOPUP.equals(type))
			isSale = true;
		else if (Transaction.TYPE_TRANSFER.equals(type))
			isSale = false;
		else
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_TRANSACTION_TYPE, "transactionNumber", "%s is not a valid Transaction Type", request.getTransactionNumber());
		state.set(PROP_IS_SALE, isSale);

		// Already Reversed
		Transaction reversal = Transaction.findByReversedID(em, original.getId(), session.getCompanyID());
		if (reversal != null)
			throw new RuleCheckException(TransactionsConfig.ERR_TRANSACTION_ALREADY_REVERSED, "transactionNumber", "%s has already been Reversed", request.getTransactionNumber());
		Transaction transaction = state.getTransaction();
		transaction.setReversedID(original.getId());
		transaction.setOriginalTransaction(original);

		// Get the A Agent
		Agent aAgent = Agent.findByID(em, original.getA_AgentID(), session.getCompanyID());
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());

		// Validate the A Agent
		validateAgentState(aAgent, false, Agent.STATE_ACTIVE, Agent.STATE_PERMANENT);

		// Set the A Agent
		state.setAgentA(aAgent);

		// Verify if Transfer
		if (!isSale)
		{
			// Get the A Agent
			Agent bAgent = Agent.findByID(em, original.getB_AgentID(), session.getCompanyID());
			if (bAgent == null)
				throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", original.getB_AgentID());

			// Validate the B Agent
			validateAgentState(bAgent, false, Agent.STATE_ACTIVE);

			// Set the B Agent
			state.setAgentB(bAgent);
		}
		else
		{
			Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
			transaction.setB_TierID(subscriberTier.getId());
			transaction.setB_Tier(subscriberTier);
		}

	}

	private void validateCoAuth(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state,
								Session session, ReversalRequestWithCoAuth request) throws RuleCheckException {
		Session coSession = state.getSession(request.getCoSignatorySessionID());

		// Cannot be the same as requestor
		if (coSession == null || coSession.getWebUserID() == session.getWebUserID() || coSession.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

		// Validate coSignable
		coSession.validateCoSignable(context, request, session.getSessionID());

		// Check co-signatory Permission
		coSession.check(em, Transaction.MAY_AUTHORISE_REVERSE, true);

		// Save co-signatory's details
		state.set(PROP_CO_AUTH_LANG, coSession.getLanguageID());
		state.set(PROP_CO_AUTH_MSISDN, coSession.getMobileNumber());
	}

	@Override
	protected void execute(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state) throws RuleCheckException {
		if ((boolean) state.get(PROP_IS_SALE)) {
			reverseSale(em, state);
		} else {
			reverseTransfer(em, state);
		}
	}
	private BigDecimal nullToZero(BigDecimal value){
		if (value == null) return BigDecimal.ZERO;
		else return value;
	}

	private void reverseTransfer(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state) throws RuleCheckException {
		final Account aAccount;
		final Account bAccount;
		int currencyDecimalDigits = context.getMoneyScale();

		// Calculate the Bonus and Bonus Provision to be reversed
		final Transaction transaction = state.getTransaction();
		Transaction original = transaction.getOriginalTransaction();
		BigDecimal amountToReverse = original.getAmount();

		BigDecimal originalBuyerTradeBonusPercentage = this.nullToZero(original.getBuyerTradeBonusPercentage());		
		BigDecimal originalBuyerTradeBonusEarned = this.nullToZero(original.getBuyerTradeBonusAmount());		
		BigDecimal buyerTradeBonusProvisionBefore = this.nullToZero(original.getB_BonusBalanceBefore());
		BigDecimal buyerTradeBonusProvisionAfter = this.nullToZero(original.getB_BonusBalanceAfter());
		BigDecimal buyerBonusProvisionToReverse = buyerTradeBonusProvisionAfter.subtract(buyerTradeBonusProvisionBefore).negate();

		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);
			bAccount = findAccount(em, state.getAgentB().getId());
			state.setBeforeB(bAccount);

			BigDecimal currentBalance = bAccount.getBalance();

			transaction.setAmount(amountToReverse.negate().setScale(currencyDecimalDigits, RoundingMode.CEILING));

			transaction.setBuyerTradeBonusAmount(originalBuyerTradeBonusEarned.negate().setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setBuyerTradeBonusProvision(buyerBonusProvisionToReverse.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setBuyerTradeBonusPercentage(originalBuyerTradeBonusPercentage.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			
			state.getResponse().setReturnCode(ERR_REFILL_FAILED);
			transaction.setReturnCode(ERR_REFILL_FAILED);
			
			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction);
		}
			
		try {
			ReversalsConfig config = state.getConfig(em, ReversalsConfig.class);
			List<String> externalDataList = this.expandExternalDataList(Arrays.asList(config.getUbadExternalData1(), config.getUbadExternalData2()), config.listExternalDataFields(), state);
			ITransaction tx = defineAirTransaction(transaction);
			Subscriber subscriber = new Subscriber(bAccount.getAgent().getMobileNumber(), context.getAirConnector(), tx);
		
			try {
				reverseDedicatedAccounts(em, config, externalDataList, bAccount, transaction, original, subscriber);
			} catch (DedicatedAccountNotFoundException ex) {
				logger.warn("Could not find dedicated account with id {}", ex.getDedicatedAccountId());
				state.exitWith(TransactionsConfig.ERR_ACC_NOT_FOUND, "Could not find dedicated account with id " + ex.getDedicatedAccountId());
				try (RequiresTransaction trans = new RequiresTransaction(em)) {
					transaction.setLastExternalResultCode(String.valueOf(0));
					setTransactionAAfter(transaction, aAccount);
					updateInDb(em, trans, aAccount, transaction);
				}
				return;
			}
			
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				aAccount.reverse(original.getStartTime(), 
						amountToReverse.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
						originalBuyerTradeBonusEarned.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
						buyerBonusProvisionToReverse.setScale(currencyDecimalDigits, RoundingMode.CEILING), false);

				bAccount.adjust(
						amountToReverse.add(originalBuyerTradeBonusEarned).negate().setScale(currencyDecimalDigits, RoundingMode.CEILING), 
						buyerBonusProvisionToReverse.setScale(currencyDecimalDigits, RoundingMode.CEILING), false);
			
				state.getResponse().setReturnCode(RETURN_CODE_SUCCESS);
				transaction.setReturnCode(RETURN_CODE_SUCCESS);
				setTransactionAAfter(transaction, aAccount);
				setTransactionBAfter(transaction, bAccount);
				updateInDb(em, trans, aAccount, bAccount, transaction);
			}
		} catch (AirException e) {
			logger.warn("Air Exception", e);
			state.exitWith(mapAirResponseCode(e.getResponseCode()), e.getMessage());
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				transaction.setLastExternalResultCode(Integer.toString(e.getResponseCode()));
				setTransactionAAfter(transaction, aAccount);
				setTransactionBAfter(transaction, bAccount);
				updateInDb(em, trans, aAccount, bAccount, transaction);
			}
		}
	}


	private void reverseSale(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state) throws RuleCheckException {
		final Transaction transaction = state.getTransaction();
		Transaction original = transaction.getOriginalTransaction();
		BigDecimal transactionAmount = original.getAmount();
		transaction.setBuyerTradeBonusAmount(BigDecimal.ZERO);
		transaction.setBuyerTradeBonusProvision(BigDecimal.ZERO);
		transaction.setBuyerTradeBonusPercentage(BigDecimal.ZERO);
		String type = original.getType();
		String subscriberMSISDN = Transaction.TYPE_SELF_TOPUP.equals(type) ? original.getA_MSISDN() : original.getB_MSISDN();
		transaction.setB_MSISDN(subscriberMSISDN);
		ITransaction tx = defineAirTransaction(transaction);
		final Account aAccount;
		
		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			aAccount = findAccount(em, state.getAgentA().getId());
			setTransactionABefore(transaction, aAccount);
			setTransactionAAfter(transaction, aAccount);
			state.getResponse().setReturnCode(ERR_REFILL_FAILED);
			transaction.setReturnCode(ERR_REFILL_FAILED);
			
			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction);
		}

		// Get a Subscriber Proxy
		Subscriber subscriber = new Subscriber(subscriberMSISDN, context.getAirConnector(), tx);
		BigDecimal transactionAmountToReverse = transactionAmount.add(BigDecimal.ZERO);

		// Execute the UBD to Debit Subscriber
		try {
			IAirConnector air = context.getAirConnector();
			Long maAmountToReverse = air.toLongAmount(transactionAmount);
			subscriber.getBalanceAndDate();
			state.set(PROP_SUBSCRIBER_LANGUAGE, subscriber.getLanguageCode2());
			Long currentBalance = subscriber.getAccountValue1();
			if (currentBalance != null && currentBalance < maAmountToReverse) {
				//insufficient funds for full reversal so revers the remaining balance
				maAmountToReverse = currentBalance;
				transactionAmountToReverse = air.fromLongAmount(currentBalance);
			}

			// set the transaction reversal amount
			transaction.setAmount(transactionAmountToReverse.negate());

			UpdateBalanceAndDateResponseMember result;
			try {
				ReversalsConfig config = state.getConfig(em, ReversalsConfig.class);
				List<String> externalDataList = 
					this.expandExternalDataList(
							Arrays.asList(
								config.getUbadExternalData1(), 
								config.getUbadExternalData2()), 
							config.listExternalDataFields(), state);

				result = reverseMainAndDedicatedAccounts(em, config, externalDataList, transaction, original, subscriber, maAmountToReverse);
			} catch (DedicatedAccountNotFoundException ex) {
				logger.warn("Could not find dedicated account with id {}", ex.getDedicatedAccountId());
				state.exitWith(TransactionsConfig.ERR_ACC_NOT_FOUND, "Could not find dedicated account with id " + ex.getDedicatedAccountId());
				try (RequiresTransaction trans = new RequiresTransaction(em)) {
					transaction.setLastExternalResultCode(String.valueOf(0));
					setTransactionAAfter(transaction, aAccount);
					updateInDb(em, trans, aAccount, transaction);
				}
				return;
			}

			BigDecimal balanceAfter = air.fromLongAmount(result.accountValue1);
			if (balanceAfter != null) {
				transaction.setB_BalanceBefore(balanceAfter.add(transactionAmountToReverse));
			}
			transaction.setB_BalanceAfter(balanceAfter);

			try (RequiresTransaction trans = new RequiresTransaction(em)) {

				aAccount.reverse(
						original.getStartTime(), 
						transactionAmountToReverse ,
						BigDecimal.ZERO,
						BigDecimal.ZERO, 
						true);

				setTransactionAAfter(transaction, aAccount);
				state.getResponse().setReturnCode(RETURN_CODE_SUCCESS);

				transaction.setBuyerTradeBonusProvision(original.getBuyerTradeBonusProvision().negate());

				transaction.setReturnCode(RETURN_CODE_SUCCESS);
				updateInDb(em, trans, aAccount, transaction);
			}
		} catch (AirException e) {
			logger.warn("Air Exception", e);
			state.exitWith(mapAirResponseCode(e.getResponseCode()), e.getMessage());
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				transaction.setLastExternalResultCode(Integer.toString(e.getResponseCode()));
				setTransactionAAfter(transaction, aAccount);
				updateInDb(em, trans, aAccount, transaction);
			}
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<ReversalRequest, ReversalResponse> state)
	{
		ReversalsConfig config = state.getConfig(em, ReversalsConfig.class);
		sendResponse(config.getRequesterNotification(), config.listNotificationFields(), state);
		Transaction transaction = state.getTransaction();

		Agent aAgent = transaction.getA_Agent();
		sendNotification(aAgent.getMobileNumber(), config.getSenderNotification(), config.listNotificationFields(), //
				state.getLocale(aAgent.getLanguage()), state);

		Agent bAgent = transaction.getB_Agent();

		String languageID = null;
		if (bAgent == null)
		{
			languageID = state.get(PROP_SUBSCRIBER_LANGUAGE);
			if (languageID == null || languageID.isEmpty())
			{
				SalesConfig config2 = state.getConfig(em, SalesConfig.class);
				languageID = config2.getDefaultSubscriberLanguageID();
			}
		}
		else
		{
			languageID = bAgent.getLanguage();
		}

		sendNotification(transaction.getB_MSISDN(), config.getRecipientNotification(), config.listNotificationFields(), //
				state.getLocale(languageID), state);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<ReversalRequest, ReversalResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case ReversalsConfig.SENDER_MSISDN:
				return transaction.getA_MSISDN();

			case ReversalsConfig.RECIPIENT_MSISDN:
				return transaction.getB_MSISDN();

			case ReversalsConfig.AMOUNT:
				return format(locale, transaction.getAmount());

			case ReversalsConfig.SENDER_NEW_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case ReversalsConfig.RECIPIENT_NEW_BALANCE:
				return format(locale, transaction.getB_BalanceAfter());

			case ReversalsConfig.ORIGINAL_NO:
			{
				Transaction original = transaction.getOriginalTransaction();
				return original.getNumber();
			}

			default:
				return super.expandField(englishName, locale, state);
		}
	}

	@Override
	public String expandExternalDataField(String englishName, TransactionState<ReversalRequest, ReversalResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case ReversalsConfig.SENDER_MSISDN:
				return transaction.getA_MSISDN();

			case ReversalsConfig.RECIPIENT_MSISDN:
				return transaction.getB_MSISDN();

			case ReversalsConfig.AMOUNT:
				return transaction.getAmount().toString();

			case ReversalsConfig.SENDER_NEW_BALANCE:
				return transaction.getA_BalanceAfter().toString();

			case ReversalsConfig.RECIPIENT_NEW_BALANCE:
				return transaction.getB_BalanceAfter().toString();

			case ReversalsConfig.ORIGINAL_NO:
			{
				Transaction original = transaction.getOriginalTransaction();
				return original.getNumber();
			}

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
	@Path("/reverse/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.ReversalsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.ReversalsConfig.class);
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

	@PUT
	@Path("/reverse/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.ReversalsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_REVERSALS);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);

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

}

package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_REFUND;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;

import java.math.BigDecimal;
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

import hxc.ecds.protocol.rest.AdjudicateRequest;
import hxc.ecds.protocol.rest.AdjudicateResponse;
import hxc.ecds.protocol.rest.config.AdjudicationConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;

@Path("/transactions")
public class Adjudicate extends Transactions<hxc.ecds.protocol.rest.AdjudicateRequest, hxc.ecds.protocol.rest.AdjudicateResponse>
{
	final static Logger logger = LoggerFactory.getLogger(Adjudicate.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_CO_AUTH_MSISDN = "CO_AUTH_MSISDN";
	private static final String PROP_CO_AUTH_LANG = "CO_AUTH_LANG";
	private static final String PROP_SUBSCRIBER_LANGUAGE = "SUBS_LANG";
	private static final String PROP_AIRTIME_NAME = "AIRTIME_NAME";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/adjudicate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.AdjudicateResponse execute(hxc.ecds.protocol.rest.AdjudicateRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_ADJUDICATE;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<AdjudicateRequest, AdjudicateResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();
		state.getConfig(em, AdjudicationConfig.class);

		// Only from WUI
		if (!Session.CHANNEL_WUI.equals(session.getChannel()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_CHANNEL, "channel", "Only Web Channel allowed");

		// Check Permission
		session.agentOrCheck(em, Transaction.MAY_ADJUDICATE, true);

		// Validate co-signatory
		AdjudicateRequest request = state.getRequest();
		Session coSession = state.getSession(request.getCoSignatorySessionID());

		// Cannot be the same as requestor
		if (coSession == null || coSession.getWebUserID() == session.getWebUserID() || coSession.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

		// Validate coSignable
		coSession.validateCoSignable(context, request, session.getSessionID());

		// Check co-signatory Permission
		coSession.check(em, Transaction.MAY_AUTHORISE_ADJUDICATE, true);

		// Save co-signatory's details
		state.set(PROP_CO_AUTH_LANG, coSession.getLanguageID());
		state.set(PROP_CO_AUTH_MSISDN, coSession.getMobileNumber());

		// Get Original Transaction
		Transaction original = Transaction.findByNumber(em, request.getTransactionNumber(), session.getCompanyID());
		if (original == null)
			throw new RuleCheckException(TransactionsConfig.ERR_TRANSACTION_NOT_FOUND, "transactionNumber", "%s is not a valid Transaction", request.getTransactionNumber());

		// Valid Amount
		BigDecimal amount = original.getAmount();

		// Right Type
		String type = original.getType();
		boolean followUp = original.isFollowUp();
		if (!followUp || (!Transaction.TYPE_SELL.equals(type) && !Transaction.TYPE_SELL_BUNDLE.equals(type)
			&& !TYPE_NON_AIRTIME_DEBIT.equals(type) && !TYPE_NON_AIRTIME_REFUND.equals(type)
			&& !Transaction.TYPE_SELF_TOPUP.equals(type)))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_TRANSACTION_TYPE, "transactionNumber", "%s is not a valid Transaction Type", request.getTransactionNumber());

		// Already Adjudicated
		Transaction adjudicated = Transaction.findByReversedID(em, original.getId(), session.getCompanyID());
		if (adjudicated != null)
			throw new RuleCheckException(TransactionsConfig.ERR_TRANSACTION_ALREADY_ADJUDICATED, "transactionNumber", "%s has already been Adjudicated", request.getTransactionNumber());
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

		// Set Subscriber Tier Info
		Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
		transaction.setB_TierID(subscriberTier.getId());
		transaction.setB_Tier(subscriberTier);
		transaction.setB_MSISDN(original.getB_MSISDN());

	}

	@Override
	protected void execute(EntityManager em, TransactionState<AdjudicateRequest, AdjudicateResponse> state) throws RuleCheckException {
		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			final Account aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);
			Transaction original = state.getTransaction().getOriginalTransaction();
			BigDecimal amount = original.getAmount();
			final Transaction transaction = state.getTransaction();
			transaction.setAmount(amount);
			if (AdjudicateRequest.ACTION_CONFIRM_SUCCEEDED.equals(state.getRequest().getAction())) {

				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
			} else if (AdjudicateRequest.ACTION_CONFIRM_FAILED.equals(state.getRequest().getAction())) {
				aAccount.setBalance(aAccount.getBalance().add(amount));
				aAccount.setBonusBalance(aAccount.getBonusBalance());
				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
				transaction.setRolledBack(true);
			}
			state.setAfterA(aAccount);

			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction, aAccount);
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<AdjudicateRequest, AdjudicateResponse> state)
	{
		// To Requester
		AdjudicationConfig config = state.getConfig(em, AdjudicationConfig.class);
		state.set(PROP_AIRTIME_NAME, config.getAirtimeProductName());
		boolean markedSucceeded = AdjudicateRequest.ACTION_CONFIRM_SUCCEEDED.equals(state.getRequest().getAction());
		Phrase notification = markedSucceeded ? config.getRequesterSuccessNotification() : config.getRequesterFailureNotification();
		sendResponse(notification, config.listNotificationFields(), state);

		// To Agent
		Transaction transaction = state.getTransaction();
		Agent aAgent = transaction.getA_Agent();
		notification = markedSucceeded ? config.getAgentSuccessNotification() : config.getAgentFailureNotification();
		sendNotification(aAgent.getMobileNumber(), notification, config.listNotificationFields(), //
				state.getLocale(aAgent.getLanguage()), state);

		// To Subscriber
		Agent bAgent = transaction.getB_Agent();
		
		// If not self topup
		if (aAgent.getMobileNumber().equals(transaction.getB_MSISDN()))
			return;

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
		notification = markedSucceeded ? config.getAgentSuccessNotification() : config.getAgentFailureNotification();
		sendNotification(transaction.getB_MSISDN(), notification, config.listNotificationFields(), //
				state.getLocale(languageID), state);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<AdjudicateRequest, AdjudicateResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case AdjudicationConfig.SENDER_MSISDN:
				return transaction.getA_MSISDN();

			case AdjudicationConfig.RECIPIENT_MSISDN:
				return transaction.getB_MSISDN();

			case AdjudicationConfig.AMOUNT:
				return format(locale, transaction.getAmount());

			case AdjudicationConfig.SENDER_NEW_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case AdjudicationConfig.ORIGINAL_NO:
			{
				Transaction original = transaction.getOriginalTransaction();
				return original.getNumber();
			}

			case AdjudicationConfig.PRODUCT_NAME:
			{
				Transaction original = transaction.getOriginalTransaction();
				if (Transaction.TYPE_SELL_BUNDLE.equals(original.getType()))
				{
					Bundle bundle = original.getBundle();
					return bundle.getName();
				} else if (TYPE_NON_AIRTIME_DEBIT.equals(original.getType()) || TYPE_NON_AIRTIME_REFUND.equals(original.getType())) {
					try (EntityManagerEx em = context.getEntityManager()) {
						return transaction.getNonAirtimeItemDescription(em);
					}
				}
				else
				{
					Phrase airtimeName = state.get(PROP_AIRTIME_NAME);
					return airtimeName.safe(locale.getLanguage(), "Airtime");
				}
			}

			case AdjudicationConfig.TRANSACTION_DATE:
			{
				Transaction original = transaction.getOriginalTransaction();
				return formatDate(locale, original.getStartTime());
			}

			default:
				return super.expandField(englishName, locale, state);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/adjudicate/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.AdjudicationConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.AdjudicationConfig.class);
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
	@Path("/adjudicate/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.AdjudicationConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_ADJUDICATION);
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

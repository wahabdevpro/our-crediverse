package hxc.services.ecds.rest;

import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.AdjustmentResponse;
import hxc.ecds.protocol.rest.config.AdjustmentsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.batch.AdjustmentProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;

@Path("/transactions")
public class Adjust extends Transactions<hxc.ecds.protocol.rest.AdjustmentRequest, hxc.ecds.protocol.rest.AdjustmentResponse>
{
	final static Logger logger = LoggerFactory.getLogger(Adjust.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_CO_AUTH_MSISDN = "CO_AUTH_MSISDN";
	private static final String PROP_CO_AUTH_LANG = "CO_AUTH_LANG";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/adjust")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.AdjustmentResponse execute(hxc.ecds.protocol.rest.AdjustmentRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_ADJUST;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<AdjustmentRequest, AdjustmentResponse> state) throws RuleCheckException
	{
		// Record Request Information
		Transaction transaction = state.getTransaction();
		AdjustmentRequest request = state.getRequest();
		transaction.setAmount(request.getAmount());
		
		// Only from WUI
		Session session = state.getSession();
		if (!Session.CHANNEL_WUI.equals(session.getChannel()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_CHANNEL, "channel", "Only Web Channel allowed");

		// Check Permission
		session.check(em, Transaction.MAY_ADJUST, true);

		// Validate co-signatory
		Session coSession = state.getSession(request.getCoSignatorySessionID());

		// Cannot be the same as requestor
		if (coSession == null || coSession.getWebUserID() == session.getWebUserID() || coSession.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

		// Validate coSignable
		coSession.validateCoSignable(context, request, session.getSessionID());

		// Check co-signatory Permission
		coSession.check(em, Transaction.MAY_AUTHORISE_ADJUST, true);

		// Save co-signatory's details
		state.set(PROP_CO_AUTH_LANG, coSession.getLanguageID());
		state.set(PROP_CO_AUTH_MSISDN, coSession.getMobileNumber());

		// Get the Root Agent
		Agent root = Agent.findRoot(em, session.getCompanyID());
		state.setAgentA(root);

		// Get the Agent
		Agent agent = Agent.findByID(em, request.getAgentID(), session.getCompanyID());
		if (agent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_AGENT, "agentID", "%d is not a valid agentID", request.getAgentID());
		state.setAgentB(agent);

	}

	@Override
	protected void execute(EntityManager em, TransactionState<AdjustmentRequest, AdjustmentResponse> state) throws RuleCheckException {
		int currencyDecimalDigits = context.getMoneyScale();

		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			final Account rootAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(rootAccount);
			final Account bAccount = findAccount(em, state.getAgentB().getId());
			state.setBeforeB(bAccount);

			// Calculate the Bonus and Bonus Provision to be transferred
			BigDecimal amount = state.getRequest().getAmount();
			BigDecimal bonusPercentage = BigDecimal.ZERO;
			BigDecimal bonusAmount = BigDecimal.ZERO;
			BigDecimal bonusProvisionPercentage = state.getAgentB().getTier().getDownStreamPercentage();
			BigDecimal bonusProvision = bAccount.getBalance()
					.add(amount)
					.add(bonusAmount)
					.multiply(bonusProvisionPercentage)
					.setScale(currencyDecimalDigits, RoundingMode.CEILING)
					.subtract(bAccount.getBonusBalance());

			// Adjust
			final Transaction transaction = state.getTransaction();
			transaction.setAmount(amount);
			transaction.setBuyerTradeBonusAmount(bonusAmount);
			transaction.setBuyerTradeBonusProvision(bonusProvision);
			transaction.setBuyerTradeBonusPercentage(bonusPercentage);
			bAccount.adjust(amount, bonusProvision, false);

			transaction.setAdditionalInformation(state.getRequest().getReason());
			rootAccount.adjust(amount.negate(), bonusProvision.negate(), true);

			state.setAfterA(rootAccount);
			state.setAfterB(bAccount);

			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction, rootAccount, bAccount);
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<AdjustmentRequest, AdjustmentResponse> state)
	{
		// To Caller
		AdjustmentsConfig config = state.getConfig(em, AdjustmentsConfig.class);
		sendResponse(config.getNotification(), config.listNotificationFields(), state);

		// To co-signatory
		String notification = expandNotification(config.getNotification(), config.listNotificationFields(), //
				state.getLocale((String) state.get(PROP_CO_AUTH_LANG)), state);
		String coAuthMSISDN = state.get(PROP_CO_AUTH_MSISDN);
		String coAuthLanguageID = state.get(PROP_CO_AUTH_LANG);
		context.sendSMS(coAuthMSISDN, coAuthLanguageID, notification);

		// To Agent
		Agent agent = state.getAgentB();
		notification = expandNotification(config.getAgentNotification(), config.listAgentNotificationFields(), //
				state.getLocale(agent.getLanguage()), state);
		context.sendSMS(agent.getMobileNumber(), agent.getLanguage(), notification);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<AdjustmentRequest, AdjustmentResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case AdjustmentsConfig.WEB_USER:
				return state.getSession().getDomainAccountName();

			case AdjustmentsConfig.AGENT_MSIDN:
				return transaction.getB_MSISDN();

			case AdjustmentsConfig.NEW_BALANCE:
				return super.format(locale, transaction.getB_BalanceAfter());

			case AdjustmentsConfig.NEW_BONUS_BALANCE:
				return super.format(locale, transaction.getB_BonusBalanceAfter());

			case AdjustmentsConfig.NEW_TOTAL_BALANCE:
				return super.format(locale, transaction.getB_BalanceAfter().add(transaction.getB_BonusBalanceAfter()));

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
	@Path("/adjust/config")
	@Produces(MediaType.APPLICATION_JSON)
	public AdjustmentsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, AdjustmentsConfig.class);
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
	@Path("/adjust/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(AdjustmentsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_ADJUSTMENTS);
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
	
	@GET
	@Path("/adjust/inclusive_batch_template")
	@Produces(MediaType.APPLICATION_JSON)
	public String getInclusiveBatchTemplate(@HeaderParam(RestParams.SID) String sessionID)
	{
		return AdjustmentProcessor.getTemplate(true);		
	}
	
	@GET
	@Path("/adjust/exclusive_batch_template")
	@Produces(MediaType.APPLICATION_JSON)
	public String getExclusiveBatchTemplate(@HeaderParam(RestParams.SID) String sessionID)
	{
		return AdjustmentProcessor.getTemplate(false);		
	}
	
	
	
	
	
	
}

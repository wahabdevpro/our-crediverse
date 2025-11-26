package hxc.services.ecds.rest;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

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

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.BalanceEnquiryRequest;
import hxc.ecds.protocol.rest.BalanceEnquiryResponse;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.BalanceEnquiriesConfig;
import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuOption;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;

@Path("/transactions")
public class BalanceEnquiry extends Transactions<hxc.ecds.protocol.rest.BalanceEnquiryRequest, hxc.ecds.protocol.rest.BalanceEnquiryResponse> //
		implements IChannelTarget, IMenuProcessor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_FOR_OTHER = "FOR_OTHER";
	private static final String PROP_AGENT_STATE = "AGENT_STATE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public BalanceEnquiry()
	{

	}

	public BalanceEnquiry(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/balance_enquiry")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.BalanceEnquiryResponse execute(hxc.ecds.protocol.rest.BalanceEnquiryRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_BALANCE_ENQUIRY;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<BalanceEnquiryRequest, BalanceEnquiryResponse> state) throws RuleCheckException
	{
		// Save Request Information
		Session session = state.getSession();
		String targetMSISDN = context.toMSISDN(state.getRequest().getTargetMSISDN());
		state.setRequestInfo(targetMSISDN == null || targetMSISDN.isEmpty() ? session.getMobileNumber() : targetMSISDN, null, BigDecimal.ZERO);
		
		// Get the Configuration
		state.getConfig(em, BalanceEnquiriesConfig.class);

		// Check Permission
		session.check(em, Transaction.MAY_QUERY_BALANCE, true);
		
		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, false);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());
		IAgentUser user = state.getSessionUser(em);

		// Validate the A Agent/User's state
		validateAgentState(user, false, Agent.STATE_ACTIVE);

		// Validate the A Agent/User's IMSI
		validateAgentImsi(em, state, user);

		// Update IMEI the A Agent/User's IMEI
		updateAgentImei(em, state, user);

		// Test if allowed
		BalanceEnquiriesConfig config = state.getConfig(em, BalanceEnquiriesConfig.class);
		Agent targetAgent = aAgent;
		boolean forOtherAgent = false;
		boolean allowed = true;
		if (targetMSISDN != null && !targetMSISDN.isEmpty())
		{
			// Caller's Own Number
			if (targetMSISDN.equals(aAgent.getMobileNumber()))
			{
				allowed = true;
			}
			else
			{
				forOtherAgent = true;

				// Find Target Agent
				targetAgent = Agent.findByMSISDN(em, targetMSISDN, aAgent.getCompanyID());
				if (targetAgent == null)
					throw new RuleCheckException(TransactionsConfig.ERR_INVALID_AGENT, "targetMSISDN", "%s is not a valid Agent MSISDN", targetMSISDN);

				// Caller a Wholesaler?
				if (!Tier.TYPE_WHOLESALER.equals(aAgent.getTier().getType()))
				{
					allowed = false;
				}

				// Owner?
				else if (targetAgent.getOwnerAgentID() != null && aAgent.getId() == targetAgent.getOwnerAgentID())
				{
					allowed = true;
				}

				// Else if Retail and onlyOwnersMayQueryRetailers is not set
				else if (Tier.TYPE_RETAILER.equals(targetAgent.getTier().getType()) && !config.isOnlyOwnersMayQueryRetailers())
				{
					allowed = true;
				}
				
				// Else not allowed
				else
				{
					allowed = false;
				}

			}
		}

		// Save the target agent details
		state.setAgentA(targetAgent);
		state.set(PROP_FOR_OTHER, forOtherAgent);

		// Exit if not allowed
		if (!allowed)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "targetMSISDN", "May not view %s's Balance", targetMSISDN);

		// Get the Agent Account
		Account targetAccount = Account.findByAgentID(em, targetAgent.getId(), false);
		state.setBeforeA(targetAccount);
		state.getResponse().setBalance(targetAccount.getBalance());
		
		// Set State
		AgentsConfig agentConfig = state.getConfig(em, AgentsConfig.class);
		String stateName = agentConfig.toStateName(targetAgent.getState()).safe(session.getLanguageID(), "");
		state.set(PROP_AGENT_STATE, stateName);

	}

	@Override
	protected void execute(EntityManager em, TransactionState<BalanceEnquiryRequest, BalanceEnquiryResponse> state) throws RuleCheckException
	{

	}

	@Override
	protected void conclude(EntityManager em, TransactionState<BalanceEnquiryRequest, BalanceEnquiryResponse> state)
	{
		BalanceEnquiriesConfig config = state.getConfig(em, BalanceEnquiriesConfig.class);
		Boolean forOtherAgent = state.get(PROP_FOR_OTHER);
		Phrase notification = forOtherAgent != null && forOtherAgent ? config.getNotificationForOther() : config.getNotification();
		sendResponse(notification, config.listNotificationFields(), state);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<BalanceEnquiryRequest, BalanceEnquiryResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case BalanceEnquiriesConfig.BALANCE:
				return format(locale, transaction.getA_BalanceBefore());

			case BalanceEnquiriesConfig.BONUS_BALANCE:
				return format(locale, transaction.getA_BonusBalanceBefore());

			case BalanceEnquiriesConfig.ONHOLD_BALANCE:
				return format(locale, transaction.getA_OnHoldBalanceBefore());

			case BalanceEnquiriesConfig.TOTAL_BALANCE:
				return format(locale, transaction.getA_BalanceBefore().add(transaction.getA_BonusBalanceBefore()).add(transaction.getA_OnHoldBalanceBefore()));

			case BalanceEnquiriesConfig.MSISDN:
				return transaction.getA_MSISDN();
				
			case BalanceEnquiriesConfig.AGENT_STATE:
				return state.get(PROP_AGENT_STATE);
				
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
	@Path("/balance_enquiry/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.BalanceEnquiriesConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.BalanceEnquiriesConfig.class);
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
	@Path("/balance_enquiry/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.BalanceEnquiriesConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_BALANCE_ENQUIRIES);
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
		BalanceEnquiriesConfig configuration = company.getConfiguration(em, BalanceEnquiriesConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(BalanceEnquiriesConfig configuration, int companyID)
	{
		context.defineChannelFilter(this, companyID, configuration.getUssdCommand(), configuration.listCommandFields(), 1);
		context.defineChannelFilter(this, companyID, configuration.getSmsCommand(), configuration.listCommandFields(), 2);
		context.defineChannelFilter(this, companyID, configuration.getUssdForOthersCommand(), configuration.listCommandFields(), 3);
		context.defineChannelFilter(this, companyID, configuration.getSmsForOthersCommand(), configuration.listCommandFields(), 4);
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

			// Validate PIN
			String pinResult = session.offerPIN(em, session, true, values.get(BalanceEnquiriesConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			BalanceEnquiryRequest request = new BalanceEnquiryRequest();
			fillHeader(request, session, interaction);
			request.setTargetMSISDN(values.get(BalanceEnquiriesConfig.MSISDN));

			// Execute the Transaction
			BalanceEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Check Balance");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		BalanceEnquiriesConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, BalanceEnquiriesConfig.class);
		return config.listCommandFields();

	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case BalanceEnquiriesConfig.PIN:
				return "your PIN";
		}
		return fieldName;
	}

	@Override
	public String menuExpandField(String englishName, Session session, Map<String, String> valueMap)
	{
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
		if (!session.hasPermission(em, Transaction.MAY_QUERY_BALANCE, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(BundleSalesConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		BalanceEnquiryRequest request = new BalanceEnquiryRequest();
		fillHeader(request, session, interaction);

		// Execute the Transaction
		BalanceEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

}

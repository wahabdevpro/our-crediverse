package hxc.services.ecds.rest;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.RegisterPinRequest;
import hxc.ecds.protocol.rest.RegisterPinResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.RegisterPinsConfig;
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
public class RegisterPin extends Transactions<hxc.ecds.protocol.rest.RegisterPinRequest, //
hxc.ecds.protocol.rest.RegisterPinResponse> implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(RegisterPin.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_KEY = "pinkey";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public RegisterPin()
	{

	}

	public RegisterPin(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/register_pin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.RegisterPinResponse execute(hxc.ecds.protocol.rest.RegisterPinRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_REGISTER_PIN;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<RegisterPinRequest, RegisterPinResponse> state) throws RuleCheckException
	{
		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, true);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "Invalid Agent");
		IAgentUser user = state.getSessionUser(em);

		// Check Permission
		Session session = state.getSession();
		session.check(em, Transaction.MAY_REGISTER_PIN, true);

		// Test if agent/user is in valid State
		validateAgentState(user, true, Agent.STATE_ACTIVE);

		// Validate the A Agent's IMSI
		validateAgentImsi(em, state, user);
		
		// Update the A Agent's IMEI
		updateAgentImei(em, state, user);

		// Set the A Agent
		state.setAgentA(aAgent);

		// Validate the new PIN
		CompanyInfo company = context.findCompanyInfoByID(aAgent.getCompanyID());
		byte[] key = user.validateNewPin(em, company, state.getRequest().getNewPin());
		state.set(PROP_KEY, key);
	}

	@Override
	protected void execute(EntityManager em, TransactionState<RegisterPinRequest, RegisterPinResponse> state) throws RuleCheckException {
		Account aAccount = Account.findByAgentID(em, state.getAgentA().getId(), false);
		state.setBeforeA(aAccount);

		byte[] key = state.get(PROP_KEY);
		state.getSessionUser(em).updatePin(em, key, state.getSession());

		state.setAfterA(aAccount);
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<RegisterPinRequest, RegisterPinResponse> state)
	{
		RegisterPinsConfig config = state.getConfig(em, RegisterPinsConfig.class);
		sendResponse(config.getNotification(), config.listNotificationFields(), state);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/register_pin/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.RegisterPinsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.RegisterPinsConfig.class);
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
	@Path("/register_pin/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.RegisterPinsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_REGISTER_PIN);
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
		RegisterPinsConfig configuration = company.getConfiguration(em, RegisterPinsConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(RegisterPinsConfig configuration, int companyID)
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

			// Validate PIN
			String pinResult = session.offerPIN(em, session, true, values.get(RegisterPinsConfig.TEMPORARY_PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			RegisterPinRequest request = new RegisterPinRequest();
			fillHeader(request, session, interaction);
			request.setNewPin(values.get(RegisterPinsConfig.NEW_PIN));

			// Execute the Transaction
			RegisterPinResponse response = super.execute(em, request, interaction.getOriginTimeStamp());

			// Respond back to Channel
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
	// IUssdProcessor
	//
	// /////////////////////////////////

	@Override
	public Phrase menuName()
	{
		return Phrase.en("Register PIN");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		RegisterPinsConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, hxc.ecds.protocol.rest.config.RegisterPinsConfig.class);
		return config.listCommandFields();
	}
	
	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case RegisterPinsConfig.TEMPORARY_PIN: return "your Temporary PIN";
			case RegisterPinsConfig.NEW_PIN: return "your New PIN";
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
		if (!session.hasPermission(em, Transaction.MAY_REGISTER_PIN, true))
				return false;

		// Test if agent/user is in valid State
		validateAgentState(session.getAgentUser(), true, Agent.STATE_ACTIVE);
		
		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(RegisterPinsConfig.TEMPORARY_PIN));
		if (pinResult != null)
			return new TransactionResponse()
			{
				@Override
				public String getReturnCode()
				{
					return pinResult;
				}
			};

		// Create the request
		RegisterPinRequest request = new RegisterPinRequest();
		fillHeader(request, session, interaction);
		request.setNewPin(values.get(RegisterPinsConfig.NEW_PIN));

		// Execute the Transaction
		return super.execute(em, request, interaction.getOriginTimeStamp());
	}

}

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
import hxc.ecds.protocol.rest.ChangePinRequest;
import hxc.ecds.protocol.rest.ChangePinResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.ChangePinsConfig;
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
public class ChangePin extends Transactions<hxc.ecds.protocol.rest.ChangePinRequest, hxc.ecds.protocol.rest.ChangePinResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(ChangePin.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_KEY = "key";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ChangePin()
	{

	}

	public ChangePin(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/change_pin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.ChangePinResponse execute(hxc.ecds.protocol.rest.ChangePinRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_CHANGE_PIN;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<ChangePinRequest, ChangePinResponse> state) throws RuleCheckException
	{
		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, true);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "Invalid Agent");
		IAgentUser user = state.getSessionUser(em);

		// Check Permission
		Session session = state.getSession();
		session.check(em, Transaction.MAY_CHANGE_PIN, true);

		// Test if agent is in valid State
		validateAgentState(user, false, Agent.STATE_ACTIVE);

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
	protected void execute(EntityManager em, TransactionState<ChangePinRequest, ChangePinResponse> state) throws RuleCheckException
	{
		// Get the Agent again
		Agent aAgent = state.getAgentA();
		IAgentUser user = state.getSessionUser(em);

		// Get the A Agent Account
		Account aAccount = Account.findByAgentID(em, aAgent.getId(), false);
		state.setBeforeA(aAccount);

		// Update the Agent
		byte[] key = state.get(PROP_KEY);
		user.updatePin(em, key, state.getSession());

		// Set Account After
		state.setAfterA(aAccount);
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<ChangePinRequest, ChangePinResponse> state)
	{
		ChangePinsConfig config = state.getConfig(em, ChangePinsConfig.class);
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
	@Path("/change_pin/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.ChangePinsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.ChangePinsConfig.class);
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
	@Path("/change_pin/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.ChangePinsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_CHANGE_PIN);
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
		ChangePinsConfig configuration = company.getConfiguration(em, ChangePinsConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(ChangePinsConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, true, values.get(ChangePinsConfig.OLD_PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			if(!values.get(ChangePinsConfig.NEW_PIN).equals(values.get(ChangePinsConfig.CONFIRM_PIN)))
			{
				pinResult = TransactionsConfig.ERR_CONFIRM_PIN_DIFF;
				return respond(session, interaction, pinResult, companyID);
			}

			// Create the request
			ChangePinRequest request = new ChangePinRequest();
			fillHeader(request, session, interaction);
			request.setNewPin(values.get(ChangePinsConfig.NEW_PIN));

			// Execute the Transaction
			ChangePinResponse response = super.execute(em, request, interaction.getOriginTimeStamp());

			// Respond back to Channel
			respond(interaction, session, response);
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
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
		return Phrase.en("Change PIN"); 
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		ChangePinsConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, ChangePinsConfig.class);
		return config.listCommandFields();

	}
	
	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case ChangePinsConfig.OLD_PIN: return "your Old PIN";
			case ChangePinsConfig.NEW_PIN: return "your New PIN";
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
		if (!session.hasPermission(em, Transaction.MAY_CHANGE_PIN, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(ChangePinsConfig.OLD_PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		ChangePinRequest request = new ChangePinRequest();
		fillHeader(request, session, interaction);
		request.setNewPin(values.get(ChangePinsConfig.NEW_PIN));

		// Execute the Transaction
		ChangePinResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

	
}

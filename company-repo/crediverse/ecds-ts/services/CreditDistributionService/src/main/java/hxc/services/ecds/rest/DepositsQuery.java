package hxc.services.ecds.rest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.DepositsQueryRequest;
import hxc.ecds.protocol.rest.DepositsQueryResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.DepositsQueryConfig;
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
public class DepositsQuery extends Transactions<hxc.ecds.protocol.rest.DepositsQueryRequest, hxc.ecds.protocol.rest.DepositsQueryResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(DepositsQuery.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DepositsQuery()
	{

	}

	public DepositsQuery(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/deposits_query")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.DepositsQueryResponse execute(hxc.ecds.protocol.rest.DepositsQueryRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_DEPOSITS_QUERY;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<DepositsQueryRequest, DepositsQueryResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();

		// Check Permission
		session.check(em, Transaction.MAY_QUERY_DEPOSITS, true);

		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, false);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());
		IAgentUser user = state.getSessionUser(em);

		// Validate the A Agent
		validateAgentState(user, false, Agent.STATE_ACTIVE);

		// Validate the A Agent's IMSI
		validateAgentImsi(em, state, user);

		// Update the A Agent's IMEI
		updateAgentImei(em, state, user);
				
		// Set the A Agent
		state.setAgentA(aAgent);

		// Get the A Agent Account
		Account aAccount = Account.findByAgentID(em, state.getAgentA().getId(), false);
		state.setBeforeA(aAccount);

		// Initialize the response
		DepositsQueryResponse response = state.getResponse();
		Calendar date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		response.setDate(date.getTime());
		response.setCount(0);
		response.setAmount(BigDecimal.ZERO);

		// Process the results
		List<Object[]> results = Transaction.depositsForAgent(em, aAgent.getId(), response.getDate(), session.getCompanyID());
		for (Object[] result : results)
		{
			String type = (String) result[0];
			if (type == null)
				continue;
			long count = (Long) result[1];
			BigDecimal total = (BigDecimal) result[2];

			switch (type.toUpperCase())
			{
				case Transaction.TYPE_TRANSFER:
					response.setCount(response.getCount() + (int) count);
					response.setAmount(response.getAmount().add(total));
					break;
			}
		}
	}

	@Override
	protected void execute(EntityManager em, TransactionState<DepositsQueryRequest, DepositsQueryResponse> state) throws RuleCheckException
	{
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<DepositsQueryRequest, DepositsQueryResponse> state)
	{
		// Optionally suppress the SMS
		Boolean suppressSms = state.getRequest().getSuppressSms();
		if (suppressSms != null && suppressSms)
			return;

		// Send SMS Notification
		DepositsQueryConfig config = state.getConfig(em, DepositsQueryConfig.class);
		String notification = super.expandNotification(config.getNotification(), config.listNotificationFields(), state.getLocale(), state);
		context.sendSMS(state.getAgentA().getMobileNumber(), state.getLocale().getISO3Language(), notification);

		// Set response
		String response = super.expandNotification(config.getResponse(), config.listNotificationFields(), state.getLocale(), state);
		state.getResponse().setResponse(response);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<DepositsQueryRequest, DepositsQueryResponse> state)
	{
		DepositsQueryResponse response = state.getResponse();
		switch (englishName)
		{
			case DepositsQueryConfig.DATE:
				return formatDate(locale, response.getDate());

			case DepositsQueryConfig.COUNT:
				return Integer.toString(response.getCount());

			case DepositsQueryConfig.AMOUNT:
				return format(locale, response.getAmount());

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
	@Path("/deposits_query/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.DepositsQueryConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.DepositsQueryConfig.class);
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
	@Path("/deposits_query/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.DepositsQueryConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_DEPOSITS_QUERY);
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
		DepositsQueryConfig configuration = company.getConfiguration(em, DepositsQueryConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(DepositsQueryConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, false, values.get(DepositsQueryConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			DepositsQueryRequest request = new DepositsQueryRequest();
			fillHeader(request, session, interaction);

			// Execute the Transaction
			DepositsQueryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Query Deposits");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		DepositsQueryConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, DepositsQueryConfig.class);
		return config.listCommandFields();

	}
	
	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case DepositsQueryConfig.PIN: return "your PIN";
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
		if (!session.hasPermission(em, Transaction.MAY_QUERY_DEPOSITS, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(DepositsQueryConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		DepositsQueryRequest request = new DepositsQueryRequest();
		fillHeader(request, session, interaction);

		// Execute the Transaction
		DepositsQueryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}


}

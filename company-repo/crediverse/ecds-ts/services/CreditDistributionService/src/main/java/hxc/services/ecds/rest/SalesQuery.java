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
import hxc.ecds.protocol.rest.SalesQueryRequest;
import hxc.ecds.protocol.rest.SalesQueryResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.SalesQueryConfig;
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
public class SalesQuery extends Transactions<hxc.ecds.protocol.rest.SalesQueryRequest, hxc.ecds.protocol.rest.SalesQueryResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(SalesQuery.class);
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
	public SalesQuery()
	{

	}

	public SalesQuery(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/sales_query")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.SalesQueryResponse execute(hxc.ecds.protocol.rest.SalesQueryRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_SALES_QUERY;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<SalesQueryRequest, SalesQueryResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();

		// Check Permission
		session.check(em, Transaction.MAY_QUERY_SALES, true);

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
		SalesQueryResponse response = state.getResponse();
		Calendar date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		response.setDate(date.getTime());
		response.setSalesCount(0);
		response.setSalesAmount(BigDecimal.ZERO);
		response.setTransfersCount(0);
		response.setTransfersAmount(BigDecimal.ZERO);
		response.setSelfTopUpsCount(0);
		response.setSelfTopUpsAmount(BigDecimal.ZERO);

		// Process the results
		List<Object[]> results = Transaction.totalsForAgent(em, aAgent.getId(), response.getDate(), session.getCompanyID());
		for (Object[] result : results)
		{
			String type = (String) result[0];
			if (type == null)
				continue;
			long count = (Long) result[1];
			BigDecimal total = (BigDecimal) result[2];

			switch (type.toUpperCase())
			{
				case Transaction.TYPE_SELL:
					response.setSalesCount(response.getSalesCount() + (int) count);
					response.setSalesAmount(response.getSalesAmount().add(total));
					break;

				case Transaction.TYPE_TRANSFER:
					response.setTransfersCount(response.getTransfersCount() + (int) count);
					response.setTransfersAmount(response.getTransfersAmount().add(total));
					break;

				case Transaction.TYPE_SELF_TOPUP:
					response.setSelfTopUpsCount(response.getSelfTopUpsCount() + (int) count);
					response.setSelfTopUpsAmount(response.getSelfTopUpsAmount().add(total));
					break;
			}
		}
	}

	@Override
	protected void execute(EntityManager em, TransactionState<SalesQueryRequest, SalesQueryResponse> state) throws RuleCheckException
	{

	}

	@Override
	protected void conclude(EntityManager em, TransactionState<SalesQueryRequest, SalesQueryResponse> state)
	{
		// Optionally suppress the SMS
		Boolean suppressSms = state.getRequest().getSuppressSms();
		if (suppressSms != null && suppressSms)
			return;

		// Send SMS Notification
		SalesQueryConfig config = state.getConfig(em, SalesQueryConfig.class);
		String notification = super.expandNotification(config.getNotification(), config.listNotificationFields(), state.getLocale(), state);
		context.sendSMS(state.getAgentA().getMobileNumber(), state.getLocale().getISO3Language(), notification);

		// Set response
		String response = super.expandNotification(config.getResponse(), config.listNotificationFields(), state.getLocale(), state);
		state.getResponse().setResponse(response);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<SalesQueryRequest, SalesQueryResponse> state)
	{
		SalesQueryResponse response = state.getResponse();
		switch (englishName)
		{
			case SalesQueryConfig.DATE:
				return formatDate(locale, response.getDate());

			case SalesQueryConfig.SALES_COUNT:
				return Integer.toString(response.getSalesCount());

			case SalesQueryConfig.SALES_AMOUNT:
				return super.format(locale, response.getSalesAmount());

			case SalesQueryConfig.TRANSFERS_COUNT:
				return Integer.toString(response.getTransfersCount());

			case SalesQueryConfig.TRANSFERS_AMOUNT:
				return super.format(locale, response.getTransfersAmount());

			case SalesQueryConfig.TOPUPS_COUNT:
				return Integer.toString(response.getSelfTopUpsCount());

			case SalesQueryConfig.TOPUPS_AMOUNT:
				return super.format(locale, response.getSelfTopUpsAmount());

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
	@Path("/sales_query/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.SalesQueryConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.SalesQueryConfig.class);
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
	@Path("/sales_query/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.SalesQueryConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_SALES_QUERY);
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
		SalesQueryConfig configuration = company.getConfiguration(em, SalesQueryConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(SalesQueryConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, false, values.get(SalesQueryConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			SalesQueryRequest request = new SalesQueryRequest();
			fillHeader(request, session, interaction);

			// Execute the Transaction
			SalesQueryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Query Sales"); 
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		SalesQueryConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, SalesQueryConfig.class);
		return config.listCommandFields();

	}
	
	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case SalesQueryConfig.PIN: return "your PIN";
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
		if (!session.hasPermission(em, Transaction.MAY_QUERY_SALES, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(SalesQueryConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		SalesQueryRequest request = new SalesQueryRequest();
		fillHeader(request, session, interaction);

		// Execute the Transaction
		SalesQueryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}


}

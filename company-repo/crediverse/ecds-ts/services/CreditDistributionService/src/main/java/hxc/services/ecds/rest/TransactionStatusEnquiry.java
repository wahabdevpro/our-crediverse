package hxc.services.ecds.rest;

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
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.TransactionStatusEnquiryRequest;
import hxc.ecds.protocol.rest.TransactionStatusEnquiryResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionStatusEnquiriesConfig;
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
public class TransactionStatusEnquiry extends Transactions<hxc.ecds.protocol.rest.TransactionStatusEnquiryRequest, hxc.ecds.protocol.rest.TransactionStatusEnquiryResponse> //
		implements IChannelTarget, IMenuProcessor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_STATUS = "STATUS";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransactionStatusEnquiry()
	{

	}

	public TransactionStatusEnquiry(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/transaction_status_enquiry")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.TransactionStatusEnquiryResponse execute(hxc.ecds.protocol.rest.TransactionStatusEnquiryRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<TransactionStatusEnquiryRequest, TransactionStatusEnquiryResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();

		// Check Permission
		session.check(em, Transaction.MAY_QUERY_STATUS, true);

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

		// Get the HistoricTransaction
		Transaction historicTransaction = Transaction.findByNumber(em, state.getRequest().getTransactionNumber(), state.getSession().getCompanyID());
		String status = historicTransaction == null || historicTransaction.getA_AgentID() != aAgent.getId() ? //
				TransactionsConfig.ERR_TRANSACTION_NOT_FOUND : historicTransaction.getReturnCode();
		TransactionsConfig transactionsConfig = state.getConfig(em, TransactionsConfig.class);
		status = transactionsConfig.findErrorText(state.getLocale().getLanguage(), status);
		state.set(PROP_STATUS, status);
	}

	@Override
	protected void execute(EntityManager em, TransactionState<TransactionStatusEnquiryRequest, TransactionStatusEnquiryResponse> state) throws RuleCheckException
	{

	}

	@Override
	protected void conclude(EntityManager em, TransactionState<TransactionStatusEnquiryRequest, TransactionStatusEnquiryResponse> state)
	{
		TransactionStatusEnquiriesConfig config = state.getConfig(em, TransactionStatusEnquiriesConfig.class);
		sendResponse(config.getNotification(), config.listNotificationFields(), state);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<TransactionStatusEnquiryRequest, TransactionStatusEnquiryResponse> state)
	{
		switch (englishName)
		{
			case TransactionStatusEnquiriesConfig.HISTORIC_TRANSACTION_NO:
				return state.getRequest().getTransactionNumber();

			case TransactionStatusEnquiriesConfig.STATUS:
				return state.get(PROP_STATUS);

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
	@Path("/transaction_status_enquiry/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.TransactionStatusEnquiriesConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.TransactionStatusEnquiriesConfig.class);
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
	@Path("/transaction_status_enquiry/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.TransactionStatusEnquiriesConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_TRANSACTION_STATUS_ENQUIRIES);
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
		TransactionStatusEnquiriesConfig configuration = company.getConfiguration(em, TransactionStatusEnquiriesConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(TransactionStatusEnquiriesConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, false, values.get(TransactionStatusEnquiriesConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			TransactionStatusEnquiryRequest request = new TransactionStatusEnquiryRequest();
			fillHeader(request, session, interaction);
			request.setTransactionNumber(values.get(TransactionStatusEnquiriesConfig.HISTORIC_TRANSACTION_NO));

			// Execute the Transaction
			TransactionStatusEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Query Transaction"); 
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		TransactionStatusEnquiriesConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, TransactionStatusEnquiriesConfig.class);
		return config.listCommandFields();

	}
	
	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case TransactionStatusEnquiriesConfig.HISTORIC_TRANSACTION_NO: return "the Transaction No";
			case TransactionStatusEnquiriesConfig.PIN: return "your PIN";
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
		if (!session.hasPermission(em, Transaction.MAY_QUERY_STATUS, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(TransactionStatusEnquiriesConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		TransactionStatusEnquiryRequest request = new TransactionStatusEnquiryRequest();
		fillHeader(request, session, interaction);
		request.setTransactionNumber(values.get(TransactionStatusEnquiriesConfig.HISTORIC_TRANSACTION_NO));

		// Execute the Transaction
		TransactionStatusEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

}

package hxc.services.ecds.rest;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import hxc.ecds.protocol.rest.LastTransactionEnquiryRequest;
import hxc.ecds.protocol.rest.LastTransactionEnquiryResponse;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.LastTransactionEnquiriesConfig;
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
import hxc.services.ecds.util.StringExpander;

@Path("/transactions")
public class LastTransactionEnquiry extends Transactions<hxc.ecds.protocol.rest.LastTransactionEnquiryRequest, hxc.ecds.protocol.rest.LastTransactionEnquiryResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(LastTransactionEnquiry.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_STATUS = "STATUS";
	private static final String PROP_NUMBER = "NUMBER";
	private static final String PROP_RECIPIENT = "RECIPIENT";
	private static final String PROP_DATE_TIME = "DATE_TIME";
	private static final String PROP_AMOUNT = "AMOUNT";
	private static final String PROP_BONUS_AMOUNT = "BONUS_AMOUNT";
	private static final String PROP_TRANSACTION_LIST = "TRANSACTION_LIST";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public LastTransactionEnquiry()
	{

	}

	public LastTransactionEnquiry(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/last_transaction_enquiry")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.LastTransactionEnquiryResponse execute(hxc.ecds.protocol.rest.LastTransactionEnquiryRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_LAST_TRANSACTION_ENQUIRY;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<LastTransactionEnquiryRequest, LastTransactionEnquiryResponse> state) throws RuleCheckException
	{
		// Get the Configuration
		Session session = state.getSession();

		// Check Permission
		session.check(em, Transaction.MAY_QUERY_LAST, true);

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

		// Get the Last Transactions
		final LastTransactionEnquiriesConfig config = state.getConfig(em, LastTransactionEnquiriesConfig.class);
		List<Transaction> lastTransactions = Transaction.findLastForAgent(em, aAgent.getId(), config.getMaxTransactions(), state.getSession().getCompanyID());

		// Initial Values
		state.set(PROP_STATUS, "-");
		state.set(PROP_NUMBER, "-");
		state.set(PROP_RECIPIENT, "-");
		state.set(PROP_DATE_TIME, "-");
		state.set(PROP_AMOUNT, "-");
		state.set(PROP_BONUS_AMOUNT, "-");
		state.set(PROP_TRANSACTION_LIST, "-");
		boolean last = true;

		Locale locale = state.getLocale();
		String languageID = locale.getLanguage();
		NumberFormat currencyFormat = context.getCurrencyFormat(locale);
		DateFormat timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);

		StringExpander<Detail> lineExpander = new StringExpander<Detail>()
		{

			@Override
			protected String expandField(String englishName, Locale locale, Detail detail)
			{
				switch (englishName)
				{
					case LastTransactionEnquiriesConfig.LAST_TRANSACTION_NO:
						return detail.number;

					case LastTransactionEnquiriesConfig.STATUS:
						return detail.status;

					case LastTransactionEnquiriesConfig.RECIPIENT_MSISDN:
						return detail.recipient;

					case LastTransactionEnquiriesConfig.DATE_TIME:
						return detail.dateTimeText;

					case LastTransactionEnquiriesConfig.AMOUNT:
						return detail.amount;

					case LastTransactionEnquiriesConfig.BONUS_AMOUNT:
						return detail.bonusAmount;

					default:
						return "";
				}
			}

		};

		List<Detail> details = new ArrayList<Detail>();

		for (Transaction lastTransaction : lastTransactions)
		{
			Detail detail = new Detail();
			boolean wasSuccessful = ResponseHeader.RETURN_CODE_SUCCESS.equals(lastTransaction.getReturnCode());
			detail.status = wasSuccessful ? config.getSuccessful().safe(languageID, "") : config.getFailed().safe(languageID, "");
			boolean pending = lastTransaction.isFollowUp();
			if (wasSuccessful && pending)
			{
				Transaction adjudication = Transaction.findByReversedID(em, lastTransaction.getId(), lastTransaction.getCompanyID());
				if (adjudication == null)
				{
					detail.status = config.getPending().safe(languageID, "");
				}
				else if (adjudication.isRolledBack())
				{
					detail.status = config.getFailed().safe(languageID, "");
				}
				else
				{
					detail.status = config.getSuccessful().safe(languageID, "");
				}
			}

			detail.number = lastTransaction.getNumber();
			detail.recipient = lastTransaction.getB_MSISDN();
			detail.dateTime = lastTransaction.getStartTime();
			detail.dateTimeText = timeFormat.format(detail.dateTime);
			detail.amount = currencyFormat.format(lastTransaction.getAmount());
			BigDecimal bonusAmount = lastTransaction.getBuyerTradeBonusAmount();
			detail.bonusAmount = bonusAmount == null || bonusAmount.signum() == 0 ? "" : currencyFormat.format(bonusAmount);
			details.add(detail);

			if (last)
			{
				last = false;
				state.set(PROP_STATUS, detail.status);
				state.set(PROP_NUMBER, detail.number);
				state.set(PROP_RECIPIENT, detail.recipient);
				state.set(PROP_DATE_TIME, detail.dateTimeText);
				state.set(PROP_AMOUNT, detail.amount);
				state.set(PROP_BONUS_AMOUNT, detail.bonusAmount);
			}
		}

		if (details.size() > 0)
		{
			// Sort
			Collections.sort(details, new Comparator<Detail>()
			{

				@Override
				public int compare(Detail d1, Detail d2)
				{
					switch (config.getOrdering())
					{
						case LastTransactionEnquiriesConfig.ORDER_BY_NUMBER:
							return d1.number.compareTo(d2.number);

						case LastTransactionEnquiriesConfig.ORDER_BY_TIME:
							return d1.dateTime.compareTo(d2.dateTime);

						case LastTransactionEnquiriesConfig.ORDER_BY_STATUS:
							return d1.status.compareTo(d2.status);

						default:
							return 0;
					}
				}
			});

			// Collate
			StringBuilder sb = new StringBuilder();
			for (Detail detail : details)
			{
				String line = lineExpander.expandNotification(config.getTransactionLine(), locale, config.listNotificationFields(), detail);
				sb.append(line);
			}
			state.set(PROP_TRANSACTION_LIST, sb.toString());
		}

	}

	@Override
	protected void execute(EntityManager em, TransactionState<LastTransactionEnquiryRequest, LastTransactionEnquiryResponse> state) throws RuleCheckException
	{
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<LastTransactionEnquiryRequest, LastTransactionEnquiryResponse> state)
	{
		Locale locale = state.getLocale();
		boolean isUssd = Session.CHANNEL_USSD.equals(state.getSession().getChannel());
		LastTransactionEnquiriesConfig config = state.getConfig(em, LastTransactionEnquiriesConfig.class);
		boolean single = config.getMaxTransactions() == 1;

		String response = isUssd || single ? //
				expandNotification(config.getNotification(), config.listNotificationFields(), locale, state) : //
				expandNotification(config.getListNotification(), config.listListNotificationFields(), locale, state);
		state.getResponse().setResponse(response);

		// Optionally suppress the SMS
		Boolean suppressSms = state.getRequest().getSuppressSms();
		if (suppressSms != null && suppressSms)
			return;

		String sms = isUssd && !single ? //
				expandNotification(config.getListNotification(), config.listListNotificationFields(), locale, state) : response;
		context.sendSMS(state.getSession().getMobileNumber(), locale.getISO3Language(), sms);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<LastTransactionEnquiryRequest, LastTransactionEnquiryResponse> state)
	{
		switch (englishName)
		{
			case LastTransactionEnquiriesConfig.LAST_TRANSACTION_NO:
				return state.get(PROP_NUMBER);

			case LastTransactionEnquiriesConfig.STATUS:
				return state.get(PROP_STATUS);

			case LastTransactionEnquiriesConfig.RECIPIENT_MSISDN:
				return state.get(PROP_RECIPIENT);

			case LastTransactionEnquiriesConfig.DATE_TIME:
				return state.get(PROP_DATE_TIME);

			case LastTransactionEnquiriesConfig.AMOUNT:
				return state.get(PROP_AMOUNT);

			case LastTransactionEnquiriesConfig.BONUS_AMOUNT:
				return state.get(PROP_BONUS_AMOUNT);

			case LastTransactionEnquiriesConfig.TRANSACTION_LIST:
				return state.get(PROP_TRANSACTION_LIST);

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
	@Path("/last_transaction_enquiry/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.LastTransactionEnquiriesConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.LastTransactionEnquiriesConfig.class);
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
	@Path("/last_transaction_enquiry/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.LastTransactionEnquiriesConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_TRANSACTION_ENQUIRY);
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
		LastTransactionEnquiriesConfig configuration = company.getConfiguration(em, LastTransactionEnquiriesConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(LastTransactionEnquiriesConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, false, values.get(LastTransactionEnquiriesConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			LastTransactionEnquiryRequest request = new LastTransactionEnquiryRequest();
			fillHeader(request, session, interaction);

			// Execute the Transaction
			LastTransactionEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Query Last Transaction");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		LastTransactionEnquiriesConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, LastTransactionEnquiriesConfig.class);
		return config.listCommandFields();

	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case LastTransactionEnquiriesConfig.PIN:
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
		if (!session.hasPermission(em, Transaction.MAY_QUERY_LAST, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(LastTransactionEnquiriesConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Create the request
		LastTransactionEnquiryRequest request = new LastTransactionEnquiryRequest();
		fillHeader(request, session, interaction);

		// Execute the Transaction
		LastTransactionEnquiryResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////

	private class Detail
	{
		private String status;
		private String number;
		private String recipient;
		private String dateTimeText;
		private Date dateTime;
		private String amount;
		private String bonusAmount;

		@Override
		public String toString()
		{
			return String.format("\n%s,%s,%s", status, number, dateTimeText);
		}

	}

}

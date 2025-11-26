package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_REFUND;
import static hxc.ecds.protocol.rest.Transaction.Type.Code.NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.Type.Code.NON_AIRTIME_REFUND;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.air.AirException;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.DedicatedAccountInfo;
import hxc.ecds.protocol.rest.DedicatedAccountRefillInfo;
import hxc.ecds.protocol.rest.DedicatedAccountReverseInfo;
import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.RegisterTransactionNotificationRequest;
import hxc.ecds.protocol.rest.ReplenishRequest;
import hxc.ecds.protocol.rest.ReplenishResponse;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransactionEx;
import hxc.ecds.protocol.rest.config.ReplenishConfig;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionExtraData;
import hxc.services.ecds.model.TransactionLocation;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.tdr.TdrWriter;
import hxc.services.ecds.util.DbUtils;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.transactions.ITransaction;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;

@Path("/transactions")
public class Replenish extends Transactions<hxc.ecds.protocol.rest.ReplenishRequest, hxc.ecds.protocol.rest.ReplenishResponse>
{
	final static Logger logger = LoggerFactory.getLogger(Replenish.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_CO_AUTH_MSISDN = "CO_AUTH_MSISDN";
	private static final String PROP_CO_AUTH_LANG = "CO_AUTH_LANG";

	private static final BigDecimal HUNDRED = new BigDecimal(100);
	private HashMap<String, DedicatedAccountInformation> daInformationHashMap = new HashMap();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/replenish")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.ReplenishResponse execute(hxc.ecds.protocol.rest.ReplenishRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_REPLENISH;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<ReplenishRequest, ReplenishResponse> state) throws RuleCheckException
	{
		// Record Request Information
		Transaction transaction = state.getTransaction();
		ReplenishRequest request = state.getRequest();
		transaction.setAmount(request.getAmount());
		transaction.setBuyerTradeBonusProvision(request.getBonusProvision());

		// Only from WUI
		Session session = state.getSession();
		if (!Session.CHANNEL_WUI.equals(session.getChannel()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_CHANNEL, "channel", "Only Web Channel allowed");

		// Check Permission
		session.check(em, Transaction.MAY_REPLENISH, true);

		// Validate co-signatory
		Session coSession = state.getSession(state.getRequest().getCoSignatorySessionID());

		// Cannot be the same as requester
		if (coSession == null || coSession.getWebUserID() == session.getWebUserID() || coSession.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

		// Validate coSignable
		coSession.validateCoSignable(context, state.getRequest(), session.getSessionID());

		// Check co-signatory Permission Authorise
		coSession.check(em, Transaction.MAY_AUTHORISE_REPLENISH, true);

		// Save co-signatory's details
		state.set(PROP_CO_AUTH_LANG, coSession.getLanguageID());
		state.set(PROP_CO_AUTH_MSISDN, coSession.getMobileNumber());

		// Validate the Amount
		state.testAmountDecimalDigits(state.getRequest().getAmount());

		// Get the Root Agent
		Agent rootAgent = Agent.findRoot(em, session.getCompanyID());
		state.setAgentB(rootAgent);

	}

	@Override
	protected void execute(EntityManager em, TransactionState<ReplenishRequest, ReplenishResponse> state) throws RuleCheckException {
		// Get the Amount and Bonus Provision Amount
		ReplenishRequest request = state.getRequest();
		BigDecimal amount = request.getAmount();
		BigDecimal bonusProvision = request.getBonusProvision();
		BigDecimal bonusProvisionPercentage = state.getAgentB().getTier().getDownStreamPercentage();
		if (bonusProvisionPercentage != null) {
			throw new RuleCheckException(TransactionsConfig.ERR_TECHNICAL_PROBLEM, "a_TierID", "Inconsistent Transfer Rules");
		}

		// Record Transaction Amounts
		final Transaction transaction = state.getTransaction();
		transaction.setAmount(amount);
		transaction.setBuyerTradeBonusProvision(bonusProvision);
		transaction.setBuyerTradeBonusAmount(BigDecimal.ZERO);
		transaction.setChargeLevied(BigDecimal.ZERO);
		transaction.setBuyerTradeBonusPercentage(BigDecimal.ZERO);

		// Update the Root Account
		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			final Account rootAccount = findAccount(em, state.getAgentB().getId());
			state.setBeforeB(rootAccount);
			rootAccount.adjust(amount, bonusProvision, true);
			state.setAfterB(rootAccount);

			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction, rootAccount);
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<ReplenishRequest, ReplenishResponse> state)
	{
		ReplenishConfig config = state.getConfig(em, ReplenishConfig.class);

		// To Caller
		sendResponse(config.getNotification(), config.listNotificationFields(), state);

		// To co-signatory
		String notification = expandNotification(config.getNotification(), config.listNotificationFields(), //
				state.getLocale((String) state.get(PROP_CO_AUTH_LANG)), state);
		context.sendSMS((String) state.get(PROP_CO_AUTH_MSISDN), (String) state.get(PROP_CO_AUTH_LANG), notification);

		// To ROOT agent
		Agent rootAgent = state.getAgentB();
		notification = expandNotification(config.getNotification(), config.listNotificationFields(), //
				state.getLocale(rootAgent.getLanguage()), state);
		context.sendSMS(rootAgent.getMobileNumber(), rootAgent.getLanguage(), notification);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<ReplenishRequest, ReplenishResponse> state)
	{
		Transaction transaction = state.getTransaction();

		switch (englishName)
		{
			case ReplenishConfig.WEB_USER:
				return state.getSession().getDomainAccountName();

			case ReplenishConfig.AMOUNT:
				return super.format(locale, state.getRequest().getAmount());

			case ReplenishConfig.BONUS_PROVISION:
				return super.format(locale, state.getRequest().getBonusProvision());

			case ReplenishConfig.NEW_BALANCE:
				return super.format(locale, transaction.getB_BalanceAfter());

			case ReplenishConfig.NEW_BONUS_BALANCE:
				return super.format(locale, transaction.getB_BonusBalanceAfter());

			case ReplenishConfig.NEW_TOTAL_BALANCE:
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
	@Path("/replenish/config")
	@Produces(MediaType.APPLICATION_JSON)
	public ReplenishConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW_CONFIGURATION);
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, ReplenishConfig.class);
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
	@Path("/replenish/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(ReplenishConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_REPLENISHMENT);
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get (all Transaction Types)
	//
	// /////////////////////////////////
	@GET
	@Path("/{number}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Transaction getTransaction(@PathParam("number") String number, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW);
			Transaction transaction = Transaction.findByNumber(em, number, session.getCompanyID());
			if (transaction == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Transaction Number %s not found", number);
			return transaction;
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
	@Path("/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Transaction getTransactionById(@PathParam("id") int id, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW);
			Transaction transaction = Transaction.findByID(em, id, session.getCompanyID());
			if (transaction == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "TransactionID %d not found", id);
			return transaction;
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
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Transaction[] getTransactions( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_MSISDNAB) String userMsisdn, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_AGENTIDAB) Integer agentID //Emulate an agent query
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			DbUtils.makeReadUncommitted(em);
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW);
			Agent me = (agentID == null ? session.getAgent() : Agent.findByID(em, agentID, session.getCompanyID()));
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<Transaction> transactions = me == null ? Transaction.findAll(em, params, session.getCompanyID(), null, userMsisdn) : Transaction.findMine(em, params, session.getCompanyID(), me.getId(), userMsisdn);
				return transactions.toArray(new Transaction[transactions.size()]);
			}
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

	@Path("ex")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExResult<hxc.ecds.protocol.rest.TransactionEx> getTransactionsEx( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_MSISDNAB) String userMsisdn, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_AGENTIDAB) Integer agentID, //Emulate an agent query
			@QueryParam(RestParams.WITHCOUNT) Integer withcount,
			@QueryParam(RestParams.INCLUDEQUERY) Integer withQuery,
			@DefaultValue("false")@QueryParam(RestParams.FETCH_SUBSCRIBER_STATE) Boolean fetchSubscriberState) {

		try (EntityManagerEx em = context.getEntityManager())
		{
			DbUtils.makeReadUncommitted(em);
			boolean includeQuery = ((withQuery == null) || (withQuery != null && withQuery == 1));
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW);
			Agent me = (agentID == null ? session.getAgent() : Agent.findByID(em, agentID, session.getCompanyID()));

			params.setIncludeQuery(includeQuery);
			// foundRows = QueryBuilder.getFoundRows(em);
			boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
			List<Transaction> transactions = null;
			Long foundRows = null;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				if (me == null)
				{
					transactions = Transaction.findAll(em, params, session.getCompanyID(), null, userMsisdn);
					if (performCount)
						foundRows = Transaction.findCount(em, params, session.getCompanyID(), null, userMsisdn);
				}
				else
				{
					agentID = (agentID == null ? me.getId(): agentID);
					transactions = Transaction.findMine(em, params, session.getCompanyID(), agentID, userMsisdn);
					if (performCount)
						foundRows = Transaction.findMyCount(em, params, session.getCompanyID(), agentID, userMsisdn);
				}
			}

			TransactionEx[] exs = new TransactionEx[transactions.size()];
			int index = 0;
			for (Transaction transaction : transactions)
			{
				TransactionEx ex = new TransactionEx(transaction);
				ex.setA_CellID(transaction.getA_HlrCellID());
				ex.setB_CellID(transaction.getB_HlrCellID());
				ex.setA_CellGroupCode(transaction.getA_CellGroupCode());
				ex.setB_CellGroupCode(transaction.getB_CellGroupCode());
				ex.setACgi(Cell.formatCgi(transaction.getA_Cell()));
				ex.setBCgi(Cell.formatCgi(transaction.getB_Cell()));

				Agent agent = transaction.getA_Agent();
				if (agent != null)
				{
					ex.setA_MSISDN(agent.getMobileNumber());
					ex.setA_FirstName(agent.getFirstName());
					ex.setA_Surname(agent.getSurname());

					Tier tier = agent.getTier();
					if (tier != null)
					{
						ex.setA_TierName(tier.getName());
						ex.setA_TierType(tier.getType());
					}

					Group group = agent.getGroup();
					if (group != null)
					{
						ex.setA_GroupName(group.getName());
					}

					Area area = agent.getArea();
					if (area != null)
					{
						ex.setA_AreaName(area.getName());
						ex.setA_AreaType(area.getType());
					}

					Agent owner = agent.getOwner();
					if (owner != null)
					{
						ex.setA_OwnerFirstName(owner.getFirstName());
						ex.setA_OwnerSurname(owner.getSurname());
					}

				}

				agent = transaction.getB_Agent();
				if (agent != null)
				{
					ex.setB_MSISDN(agent.getMobileNumber());
					ex.setB_FirstName(agent.getFirstName());
					ex.setB_Surname(agent.getSurname());

					Tier tier = agent.getTier();
					if (tier != null)
					{
						ex.setB_TierName(tier.getName());
						ex.setB_TierType(tier.getType());
					}

					Group group = agent.getGroup();
					if (group != null)
					{
						ex.setB_GroupName(group.getName());
					}

					Area area = agent.getArea();
					if (area != null)
					{
						ex.setB_AreaName(area.getName());
						ex.setB_AreaType(area.getType());
					}

					Agent owner = agent.getOwner();
					if (owner != null)
					{
						ex.setB_OwnerFirstName(owner.getFirstName());
						ex.setB_OwnerSurname(owner.getSurname());
					}

				}

				ReversalsConfig reversalsConfig = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, ReversalsConfig.class);
				if (fetchSubscriberState && reversalsConfig != null && reversalsConfig.isEnableDedicatedAccountReversal()) {
					List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos = getDedicatedAccountRefillInfos(em, transaction);
					ex.setDedicatedAccountRefillInfos(dedicatedAccountRefillInfos);
					List<DedicatedAccountReverseInfo> dedicatedAccountReverseInfos = getDedicatedAccountReversals(em, transaction);
					ex.setDedicatedAccountReverseInfo(dedicatedAccountReverseInfos);

					//latest value of DA's in charging system, add to returned object
					populateCurrentMainAccountAndDABalanceInfo(ex, transaction, dedicatedAccountRefillInfos);
					ex.setDABonusReversalEnabled(Boolean.TRUE);
				}
				else {
					ex.setDABonusReversalEnabled(Boolean.FALSE);
				}

				if (asList(TYPE_NON_AIRTIME_DEBIT, TYPE_NON_AIRTIME_REFUND).contains(transaction.getType())) {
					ex.setNonAirtimeItemDescription(transaction.getNonAirtimeItemDescription(em));
				}

				exs[index++] = ex;
			}

			return new ExResult<hxc.ecds.protocol.rest.TransactionEx>(foundRows, exs);
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

	private List<DedicatedAccountRefillInfo> getDedicatedAccountRefillInfos(EntityManagerEx em, Transaction transaction) {
		List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos = new ArrayList<>();

		TransactionExtraData transactionExtraData = TransactionExtraData.findByTransactionIdAndKey(em, transaction.getId(), TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO);
		if(transactionExtraData != null){
			try {
				DedicatedAccountRefillInfoAccounts dedicatedAccountRefillInfoAccounts = (DedicatedAccountRefillInfoAccounts)transactionExtraData.getValueObject();
				for (hxc.services.ecds.model.extra.DedicatedAccountRefillInfo dedicatedAccountRefillInfo : dedicatedAccountRefillInfoAccounts.getDedicatedAccountRefillInfos()) {
					dedicatedAccountRefillInfos.add(new DedicatedAccountRefillInfo(dedicatedAccountRefillInfo.getDedicatedAccountID(), dedicatedAccountRefillInfo.getDedicatedAccountUnitType(), dedicatedAccountRefillInfo.getRefillAmount1()));
				}
			} catch (IOException e) {
				logger.error("Could not deserialize Json for extra data {} for transaction ID: {}", transactionExtraData, transaction.getId());
			} catch (ClassNotFoundException e) {
				logger.error("Could not deserialize Json for extra data {} for transaction ID: {}", transactionExtraData, transaction.getId());
			}
		}

		return dedicatedAccountRefillInfos;
	}

	private List<DedicatedAccountReverseInfo> getDedicatedAccountReversals(EntityManagerEx em, Transaction transaction) {
		List<DedicatedAccountReverseInfo> dedicatedAccountReversalsList = new ArrayList<>();

		TransactionExtraData transactionExtraData = TransactionExtraData.findByTransactionIdAndKey(em, transaction.getId(), TransactionExtraData.Key.DEDICATED_ACCOUNT_REVERSE_INFO);
		if(transactionExtraData != null ){
			try {
				hxc.services.ecds.model.extra.DedicatedAccountReversals dedicatedAccountReversals = (hxc.services.ecds.model.extra.DedicatedAccountReversals)transactionExtraData.getValueObject();
				for (hxc.services.ecds.model.extra.DedicatedAccountReverseInfo dedicatedAccountReverseInfo : dedicatedAccountReversals.getDedicatedAccountReversals()) {
					dedicatedAccountReversalsList.add(new DedicatedAccountReverseInfo(dedicatedAccountReverseInfo.getDedicatedAccountID(), dedicatedAccountReverseInfo.getDedicatedAccountUnitType(), dedicatedAccountReverseInfo.getReversalAmount()));
				}
			} catch (IOException e) {
				logger.error("Could not deserialize Json for extra data {} for transaction ID: {}", transactionExtraData, transaction.getId());
			} catch (ClassNotFoundException e) {
				logger.error("Could not deserialize Json for extra data {} for transaction ID: {}", transactionExtraData, transaction.getId());
			}

		}

		return dedicatedAccountReversalsList;
	}

	@GET
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getTransactionCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_MSISDNAB) String userMsisdn, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_AGENTIDAB) Integer agentID //Emulate an agent query
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			DbUtils.makeReadUncommitted(em);
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_VIEW);
			Agent me = (agentID == null ? session.getAgent() : Agent.findByID(em, agentID, session.getCompanyID()));
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				return me == null ? Transaction.findCount(em, params, session.getCompanyID(), null, userMsisdn) : Transaction.findMyCount(em, params, session.getCompanyID(), me.getId(), userMsisdn);
			}
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
	@Path("/csv")
	@Produces("text/csv")
	public String getTransactionsCsv( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_MSISDNAB) String userMsisdn, //
			@QueryParam(TransactionEx.VIRTUAL_FILTER_AGENTIDAB) Integer agentID, //Emulate an agent query
			@QueryParam(RestParams.INCLUDEQUERY) Integer withQuery
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			boolean includeQuery = ((withQuery == null) || (withQuery != null && withQuery == 1));
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			params.setIncludeQuery(includeQuery);
			Session session = context.getSession(params.getSessionID());
			Agent me = (agentID == null ? session.getAgent() : Agent.findByID(em, agentID, session.getCompanyID()));
			List<Transaction> transactions;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				transactions = me == null ? Transaction.findAll(em, params, session.getCompanyID(), null, userMsisdn) : Transaction.findMine(em, params, session.getCompanyID(), me.getId(), userMsisdn);
			}
			CsvExportProcessor<Transaction> processor = new CsvExportProcessor<Transaction>(TdrWriter.HEADINGS, first)
			{
				@Override
				protected void write(Transaction transaction)
				{
					// 1: Hostname
					put("hostname", CsvExportProcessor.toText(transaction.getHostname()));

					// 2: TransactionID
					put("transaction_no", CsvExportProcessor.toText(transaction.getNumber()));

					// 3: TransactionType
					put("transaction_type", CsvExportProcessor.toText(transaction.getType()));

					// 4: Channel
					String channel = transaction.getChannel();
					switch (channel)
					{
						case Session.CHANNEL_WUI:
							channel = "WUI";
							break;

						case Session.CHANNEL_3PP:
							channel = "API";
							break;

						case Session.CHANNEL_BATCH:
							channel = "BATCH";
							break;

						case Session.CHANNEL_SMART_DEVICE:
							channel = "APP";
							break;

						case Session.CHANNEL_SMS:
							channel = "SMS";
							break;

						case Session.CHANNEL_USSD:
							channel = "USSD";
							break;
					}
					put("channel", CsvExportProcessor.toText(channel));

					// 5: CallerID
					put("caller_id", CsvExportProcessor.toText(transaction.getCallerID()));

					// 6: StartTime
					put("start_time", CsvExportProcessor.toText(transaction.getStartTime()));

					// 7: EndTime
					put("end_time", CsvExportProcessor.toText(transaction.getEndTime()));

					// 8: InboundTransactionID
					put("inbound_transaction_id", CsvExportProcessor.toText(transaction.getInboundTransactionID()));

					// 9: InboundSessionID
					put("inbound_session_id", CsvExportProcessor.toText(transaction.getInboundSessionID()));

					// 10: RequestMode
					put("request_mode", CsvExportProcessor.toText(transaction.getRequestMode()));

					// 11: A_PartyID
					Agent agent = transaction.getA_Agent();
					put("a_party_id", agent == null ? CsvExportProcessor.toText(transaction.getA_MSISDN()) : CsvExportProcessor.toText(agent.getAccountNumber()));

					// 12: A_MSISDN
					put("a_msisdn", CsvExportProcessor.toText(transaction.getA_MSISDN()));

					// 13: A_Tier
					Tier tier = transaction.getA_Tier();
					if (tier != null)
						put("a_tier", CsvExportProcessor.toText(tier.getName()));

					// 14: A_ServiceClass
					ServiceClass sc = transaction.getA_ServiceClass();
					if (sc != null)
						put("a_service_class", CsvExportProcessor.toText(sc.getName()));

					// 15: A_Group
					Group group = transaction.getA_Group();
					if (group != null)
						put("a_group", CsvExportProcessor.toText(group.getName()));

					// 16: A_Owner
					Agent owner = transaction.getA_Owner();
					if (owner != null)
						put("a_owner", CsvExportProcessor.toText(owner.getMobileNumber()));

					// 17: A_Area
					Area area = transaction.getA_Area();
					if (area != null)
						put("a_area", CsvExportProcessor.toText(area.getName()));

					// 18: A_IMSI
					put("a_imsi", CsvExportProcessor.toText(transaction.getA_IMSI()));

					// 19: A_IMEI
					put("a_imei", CsvExportProcessor.toText(transaction.getA_IMEI()));

					// 20: A_CellID
					put("a_cell_id", CsvExportProcessor.toText(transaction.getA_HlrCellID()));

					// 21: A_BalanceBefore
					put("a_balance_before", CsvExportProcessor.toText(transaction.getA_BalanceBefore()));

					// 22: A_BalanceAfter
					put("a_balance_after", CsvExportProcessor.toText(transaction.getA_BalanceAfter()));

					// 23: B_PartyID
					agent = transaction.getB_Agent();
					put("b_party_id", agent == null ? CsvExportProcessor.toText(transaction.getB_MSISDN()) : CsvExportProcessor.toText(agent.getAccountNumber()));

					// 24: B_MSISDN
					put("b_msisdn", CsvExportProcessor.toText(transaction.getB_MSISDN()));

					// 25: B_Tier
					tier = transaction.getB_Tier();
					if (tier != null)
						put("b_tier", CsvExportProcessor.toText(tier.getName()));

					// 26: B_ServiceClass
					sc = transaction.getB_ServiceClass();
					if (sc != null)
						put("b_service_class", CsvExportProcessor.toText(sc.getName()));

					// 27: B_Group
					group = transaction.getB_Group();
					if (group != null)
						put("b_group", CsvExportProcessor.toText(group.getName()));

					// 28: B_Owner
					owner = transaction.getB_Owner();
					if (owner != null)
						put("b_owner", CsvExportProcessor.toText(owner.getMobileNumber()));

					// 29: B_Area
					area = transaction.getB_Area();
					if (area != null)
						put("b_area", CsvExportProcessor.toText(area.getName()));

					// 30: B_IMSI
					put("b_imsi", CsvExportProcessor.toText(transaction.getB_IMSI()));

					// 31: B_IMEI
					put("b_imei", CsvExportProcessor.toText(transaction.getB_IMEI()));

					// 32: B_CellID
					put("b_cellid", CsvExportProcessor.toText(transaction.getB_HlrCellID()));

					// 33: B_BalanceBefore
					put("b_balance_before", CsvExportProcessor.toText(transaction.getB_BalanceBefore()));

					// 34: B_BalanceAfter
					put("b_balance_after", CsvExportProcessor.toText(transaction.getB_BalanceAfter()));

					// 35: Amount
					put("amount", CsvExportProcessor.toText(transaction.getAmount()));

					// 36: Buyer Trade Bonus
					put("buyer_trade_bonus", CsvExportProcessor.toText(transaction.getBuyerTradeBonusAmount()));

					// 37: Buyer Bonus Percentage
					put("buyer_bonus_percentage", CsvExportProcessor.toText(transaction.getBuyerTradeBonusPercentage() == null ? null : transaction.getBuyerTradeBonusPercentage().multiply(HUNDRED)));

					// 38: Buyer Bonus Provision
					put("buyer_bonus_provision", CsvExportProcessor.toText(transaction.getBuyerTradeBonusProvision()));

					// 39: Seller Trade Bonus
					//put("seller_trade_bonus", "");
					put("gross_sales_amount", CsvExportProcessor.toText(transaction.getGrossSalesAmount()));

					// 40: Seller Trade Bonus Percentage
					//put("seller_bonus_percentage", "");
					put("cogs", CsvExportProcessor.toText(transaction.getCostOfGoodsSold()));

					// 41: Seller Bonus Provision
					//put("seller_bonus_provision", "");
					put("future_use_1", "");

					// 42: ChargeLevied
					//put("charge_levied", CsvExportProcessor.toText(transaction.getChargeLevied()));
					put("origin_channel", CsvExportProcessor.toText(transaction.getChannelType()));

					// 43: ReturnCode
					put("return_code", CsvExportProcessor.toText(transaction.getReturnCode()));

					// 44: LastExternalResultCode
					put("last_external_result_code", CsvExportProcessor.toText(transaction.getLastExternalResultCode()));

					// 45: RolledBack
					put("rolled_back", CsvExportProcessor.toText(transaction.isRolledBack()));

					// 46: FollowUp
					put("follow_up", CsvExportProcessor.toText(transaction.isFollowUp()));

					// 47: Requester MSISDN
					put("requester_msisdn", CsvExportProcessor.toText(transaction.getRequesterMSISDN()));

					// 48: Requester Type
					String requesterType = transaction.getRequesterType();
					switch (channel)
					{
						case Transaction.REQUESTER_TYPE_AGENT:
							channel = "Agent";
							break;

						case Transaction.REQUESTER_TYPE_AGENT_USER:
							channel = "Agent User";
							break;

						case Transaction.REQUESTER_TYPE_WEB_USER:
							channel = "Web User";
							break;

					}
					put("requester_type", CsvExportProcessor.toText(requesterType));

					// 49: Bundle
					if (NON_AIRTIME_DEBIT.equals(transaction.getType())  || NON_AIRTIME_REFUND.equals(transaction.getType())  ) {
						try (EntityManagerEx em = context.getEntityManager()) {
							put("bundle", CsvExportProcessor.toText(transaction.getNonAirtimeItemDescription(em)));
						}
					} else {
						put("bundle", CsvExportProcessor.toText(transaction.fullBundleName()));
					}

					// 50: Promotion
					Promotion promotion = transaction.getPromotion();
					if (promotion != null)
						put("promotion", CsvExportProcessor.toText(promotion.getName()));

					// 51: A_OnHoldBalanceBefore
					put("a_hold_balance_before", CsvExportProcessor.toText(transaction.getA_OnHoldBalanceBefore()));

					// 52: A_OnHoldBalanceAfter
					put("a_hold_balance_after", CsvExportProcessor.toText(transaction.getA_OnHoldBalanceAfter()));

					// 53: Reversed/Adjudicated Transaction ID
					Transaction original = transaction.getOriginalTransaction();
					if (original != null)
						put("original_tid", CsvExportProcessor.toText(original.getNumber()));

					// 54: AdditionalInformation
					put("additional_information", CsvExportProcessor.toText(transaction.getAdditionalInformation()));

					// 55: TransferBonusAmount
					put("b_transfer_bonus_amount", CsvExportProcessor.toText(transaction.getB_TransferBonusAmount()));
					
					// 56: TransferBonusProfile
					put("b_transfer_bonus_profile", CsvExportProcessor.toText(transaction.getB_TransferBonusProfile()));

					// 57: CGI
					put("a_cgi", CsvExportProcessor.toText(Cell.formatCgi(transaction.getA_Cell())));

					// 58: GPS
					try (EntityManagerEx em = context.getEntityManager()) {
						put("a_gps", CsvExportProcessor.toText(TransactionLocation.formatLocation(TransactionLocation.findGPSByTransactionId(em, transaction.getId()))));
					}
				}
			};

			return processor.add(transactions);
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
	@Path("/template/csv")
	@Produces("text/csv")
	public String getTDRHeadings( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("1.0.0") @QueryParam(RestParams.FILTER) String tdrStructureVersion
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			List<Transaction> transactions = new ArrayList<Transaction>();
			String tdrHeadingsList[] = TdrWriter.HEADINGS;

			if (tdrStructureVersion.equals(TransactionsConfig.TDR_STRUCTURE_VERSION_1)) {
				tdrHeadingsList = ArrayUtils.removeElement(tdrHeadingsList, "seller_trade_bonus");
				tdrHeadingsList = ArrayUtils.removeElement(tdrHeadingsList, "seller_bonus_percentage");
				tdrHeadingsList = ArrayUtils.removeElement(tdrHeadingsList, "seller_bonus_provision");
			}

			CsvExportProcessor<Transaction> processor = new CsvExportProcessor<Transaction>(tdrHeadingsList, 0, false)
			{
				@Override
				protected void write(Transaction transaction)
				{
				}
			};

			return processor.add(transactions);
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration (all Transaction Types)
	//
	// /////////////////////////////////
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public TransactionsConfig getAllConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransactionsConfig.class);
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
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAllConfig(TransactionsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_TRANSACTIONS);
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
	
	@POST
	@Path("/register/notification")
	@Consumes(MediaType.APPLICATION_JSON)
	public void registerNotification(RegisterTransactionNotificationRequest request, @HeaderParam(RestParams.SID) String sessionID) throws RuleCheckException
	{
		hxc.services.ecds.Sessions sessions = context.getSessions();
		if(sessions.get(request.getSessionID()) != null)
		{
			context.setAgentTaggedForCallback(request);
		}
	}

	private void populateCurrentMainAccountAndDABalanceInfo(TransactionEx transactionEx, Transaction transaction, List<DedicatedAccountRefillInfo> dedicatedAccountRefillInfos) {

		List<DedicatedAccountInfo> currentDABalanceInfo = new ArrayList<>();

		try {
			ITransaction tx = defineAirTransaction(transaction);

			String type = transaction.getType();
			String subscriberMSISDN = Transaction.TYPE_SELF_TOPUP.equals(type) ? transaction.getA_MSISDN() : transaction.getB_MSISDN();
			// Get a Subscriber Proxy
			Subscriber subscriber = new Subscriber(subscriberMSISDN, context.getAirConnector(), tx);
			for (DedicatedAccountRefillInfo transactionDedicatedAccountRefillInfo : dedicatedAccountRefillInfos) {
				String daId = transactionDedicatedAccountRefillInfo.getDedicatedAccountID();
				DedicatedAccountInformation dedicatedAccountInfo = null;
				if (daInformationHashMap.containsKey(subscriberMSISDN.concat(daId))) {
					dedicatedAccountInfo = daInformationHashMap.get(subscriberMSISDN.concat(daId));
				}
				else {
					dedicatedAccountInfo = subscriber.getDedicatedAccount(Integer.valueOf(daId));
					daInformationHashMap.put(subscriberMSISDN.concat(daId), dedicatedAccountInfo);
				}

				if(dedicatedAccountInfo != null){
					String dedicatedAccountType = dedicatedAccountInfo.dedicatedAccountUnitType == null ? "" : dedicatedAccountInfo.dedicatedAccountUnitType.toString();
					currentDABalanceInfo.add(new DedicatedAccountInfo(dedicatedAccountInfo.dedicatedAccountID + "", dedicatedAccountType, BigDecimal.valueOf(dedicatedAccountInfo.dedicatedAccountValue1)));
				}

			}

			if(dedicatedAccountRefillInfos.size() == 0){
				// if there are no DA's tehn we need to call getBalanceAndDate()
				// getBalanceAndDate() is called as part of the call in getDedicatedAccount()
				subscriber.getBalanceAndDate();
			}

			transactionEx.setMainAccountCurrentBalance(BigDecimal.valueOf(subscriber.getAccountValue1()));

		} catch (AirException ex) {
			// Do not restrict the view of transaction when getBalanceAndDate fail
			logger.error(ex.getMessage(), ex);
			transactionEx.setBalanceAndDateFailed(Boolean.TRUE);
//			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

		transactionEx.setDedicatedAccountCurrentBalanceInfos(currentDABalanceInfo);
	}
}

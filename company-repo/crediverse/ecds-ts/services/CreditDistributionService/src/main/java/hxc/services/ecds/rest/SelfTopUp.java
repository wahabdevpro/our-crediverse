package hxc.services.ecds.rest;

import static hxc.services.ecds.rest.TransactionHelper.createDaRefillInfoList;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.isEmpty;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
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
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.SelfTopUpRequest;
import hxc.ecds.protocol.rest.SelfTopUpResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.IConfirmationMenuConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.SelfTopUpsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionExtraData;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuOption;
import hxc.services.ecds.rest.ussd.MenuProcessor;
import hxc.services.ecds.util.ConfirmationMenuHelper;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.transactions.ITransaction;
import hxc.utils.protocol.ucip.RefillResponse;

@Path("/transactions")
public class SelfTopUp extends Transactions<hxc.ecds.protocol.rest.SelfTopUpRequest, hxc.ecds.protocol.rest.SelfTopUpResponse> //
		implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(SelfTopUp.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_AIRTIME = "AIRTIME";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SelfTopUp()
	{

	}

	public SelfTopUp(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/self_topup")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.SelfTopUpResponse execute(hxc.ecds.protocol.rest.SelfTopUpRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return hxc.ecds.protocol.rest.Transaction.TYPE_SELF_TOPUP;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<SelfTopUpRequest, SelfTopUpResponse> state) throws RuleCheckException
	{
		// Record Request Information
		Transaction transaction = state.getTransaction();
		SelfTopUpRequest request = state.getRequest();
		transaction.setAmount(request.getAmount());

		// Get the Configuration
		Session session = state.getSession();
		state.getConfig(em, SelfTopUpsConfig.class);

		// Check Permission
		session.check(em, Transaction.MAY_SELF_TOPUP, true);

		// Get the A Agent
		Agent aAgent = state.getSessionAgent(em, false);
		if (aAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());
		IAgentUser user = state.getSessionUser(em);

		// Validate the A Agent/User
		validateAgentState(user, false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE);

		// Validate the A Agent/Users's IMSI
		validateAgentImsi(em, state, user);

		// Update the A Agent's IMEI
		updateAgentImei(em, state, user);

		// Set the A/B Agent
		state.setAgentA(aAgent);
		state.setAgentB(aAgent);

		// Validate the Amount
		state.testAmountDecimalDigits(state.getRequest().getAmount());

		// Get cached location
		SelfTopUpsConfig config = state.getConfig(em, SelfTopUpsConfig.class);
		state.getCachedLocation(em, user, config.isForceLocationOfAgent());
	}

	@Override
	protected void execute(EntityManager em, TransactionState<SelfTopUpRequest, SelfTopUpResponse> state) throws RuleCheckException {
		final Account aAccount;
		BigDecimal amount = state.getRequest().getAmount();
		final Transaction transaction = state.getTransaction();
		IAgentUser user = state.getSessionUser(em);

		try (RequiresTransaction trans = new RequiresTransaction(em)) {
			aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);
			transaction.setAmount(amount);
			transaction.setGrossSalesAmount(amount);

			Transaction lastSuccessfulTxTransaction = Transaction.findLastSuccessfulTransferToAgent(em, transaction.getA_MSISDN(), transaction.getCompanyID());
			BigDecimal defTradeBonusPct = aAccount.getAgent().getTier().getBuyerDefaultTradeBonusPercentage();
			if(lastSuccessfulTxTransaction != null)
			{				
				MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
				logger.info("Using bonus percentage {}% from last transfer transaction for calculating cost of goods sold", 
				lastSuccessfulTxTransaction.getBuyerTradeBonusPercentage().multiply(new BigDecimal(100.0)).setScale(2, RoundingMode.HALF_UP));
				BigDecimal lastCreditPurchased = lastSuccessfulTxTransaction.getAmount();
				BigDecimal lastBonusAmount = lastSuccessfulTxTransaction.getBuyerTradeBonusAmount();
				BigDecimal lastCreditReceived = lastCreditPurchased.add(lastBonusAmount);
				BigDecimal costPerUnit = lastCreditPurchased.divide(lastCreditReceived, mc);
				transaction.setCostOfGoodsSold(amount.multiply(costPerUnit));
			}
			else if(defTradeBonusPct != null) {
				MathContext mc = new MathContext(8, RoundingMode.HALF_UP);
				logger.info("Using default bonus percentage {}% for calculating cost of goods sold", defTradeBonusPct.setScale(2, RoundingMode.HALF_UP));
				
				transaction.setCostOfGoodsSold(amount.divide((defTradeBonusPct.add(new BigDecimal(100.0)).divide(new BigDecimal(100.0), mc)), mc));
			}
			else {
				logger.warn("Bonus percentage unavailable for calculating cost of goods sold.");
			}


			transaction.setBuyerTradeBonusAmount(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusProvision(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusPercentage(BigDecimal.ZERO);
			aAccount.transact(transaction.getStartTime(), amount, BigDecimal.ZERO, BigDecimal.ZERO, false);

			// Set the follow-up flag and move the money in the ON-HOLD.
			// If unexpected exception occurs the transaction will be in this state and have to be fixed manually.
			transaction.setFollowUp(true);
			aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().add(amount));
			setTransactionAAfter(transaction, aAccount);
			
			transaction.persist(em, null, state.getSession(), null);

			Double longitude = state.getRequest().getLongitude();
			Double latitude = state.getRequest().getLatitude();
			long transactionId = transaction.getId();

			if (longitude != null && latitude != null) {
				transaction.persistTransactionLocation(em, longitude, latitude, transactionId);
			}
			
			updateInDb(em, trans, transaction, aAccount);
		}
		
		ITransaction tx = defineAirTransaction(transaction);
		Subscriber subscriber = new Subscriber(user.getMobileNumber(), context.getAirConnector(), tx);
		SelfTopUpsConfig config = state.getConfig(em, SelfTopUpsConfig.class);
		ReversalsConfig reversalsConfig = state.getConfig(em, ReversalsConfig.class);

		// Execute the Refill
		try {
			IAirConnector air = context.getAirConnector();
			long longAmount = air.toLongAmount(amount);
			List<String> externalDataList = this.expandExternalDataList(Arrays.asList(config.getRefillExternalData1(), config.getRefillExternalData2(), config.getRefillExternalData3(), config.getRefillExternalData4()), config.listExternalDataFields(), state);
			RefillResponse result = subscriber.refillAccount(config.getRefillProfileID(), longAmount,
										 config.isAutoActivateAccounts(), externalDataList, reversalsConfig.isEnableDedicatedAccountReversal());
												 
			state.set(PROP_AIRTIME, air.fromLongAmount(result.member.accountAfterRefill.accountValue1));

			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				if (result.member.refillInformation != null && result.member.refillInformation.refillValueTotal != null
						&& result.member.refillInformation.refillValueTotal.dedicatedAccountRefillInformation != null) {

					DedicatedAccountRefillInfoAccounts dARefillInfoList = createDaRefillInfoList(result);
					if (!isEmpty(dARefillInfoList.getDedicatedAccountRefillInfos())) {
						logger.debug(" size dARefillInfoList to save: {}", dARefillInfoList.getDedicatedAccountRefillInfos().size());
						//only save extra data if we have dedicated account info returned in the refill info
						transaction.addExtraDataForKeyType(TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO, dARefillInfoList);
					}
				}
				transaction.persistExtraData(em);
				transaction.setFollowUp(false);
				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
				setTransactionAAfter(transaction, aAccount);
				updateInDb(em, trans, transaction, aAccount);
			}

		} catch (AirException e) {
			logger.warn("Air Exception", e);
			if (e.isDeterministic() && isDeterministic(config.getNonDeterministicErrorCodes(), e.getResponseCode())) {
				try (RequiresTransaction trans = new RequiresTransaction(em)) {
					state.exitWith(mapAirResponseCode(e.getResponseCode()), e.getMessage());
					transaction.setLastExternalResultCode(Integer.toString(e.getResponseCode()));
					transaction.setFollowUp(false);
					aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(amount));
					aAccount.adjust(amount, BigDecimal.ZERO, false);
					setTransactionAAfter(transaction, aAccount);
					updateInDb(em, trans, transaction, aAccount);
				}
			} else {
				// May have succeeded
				try (RequiresTransaction trans = new RequiresTransaction(em)) {
          state.getResponse().setAdditionalInformation(String.format("May have failed - Follow Up! Air-Node: [%s]", e.getHost()));
          state.getResponse().setFollowUp(true);
					setTransactionAAfter(transaction, aAccount);
					updateInDb(em, trans, transaction);
				}
			}
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<SelfTopUpRequest, SelfTopUpResponse> state)
	{
		SelfTopUpsConfig config = state.getConfig(em, SelfTopUpsConfig.class);
		boolean followUp = state.getTransaction().isFollowUp();
		Phrase notification = followUp ? config.getSenderUnknownNotification() : config.getSenderNotification();
		sendResponse(notification, config.listNotificationFields(), state);
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<SelfTopUpRequest, SelfTopUpResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case SelfTopUpsConfig.MSISDN:
				return state.getSession().getMobileNumber();

			case SelfTopUpsConfig.ECDS_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case SelfTopUpsConfig.AIRIME_BALANCE:
				return format(locale, (BigDecimal) state.get(PROP_AIRTIME));

			case SelfTopUpsConfig.AMOUNT:
				return format(locale, transaction.getAmount());

			default:
				return super.expandField(englishName, locale, state);
		}
	}

	@Override
	public String expandExternalDataField(String englishName, TransactionState<SelfTopUpRequest, SelfTopUpResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case SelfTopUpsConfig.MSISDN:
				return state.getSession().getMobileNumber();

			case SelfTopUpsConfig.ECDS_BALANCE:
				return transaction.getA_BalanceAfter().toString();

			case SelfTopUpsConfig.AIRIME_BALANCE:
				return state.get(PROP_AIRTIME).toString();

			case SelfTopUpsConfig.AMOUNT:
				return transaction.getAmount().toString();

			default:
				return super.expandExternalDataField(englishName, state);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/self_topup/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.SelfTopUpsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return getConfig(em, session);
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

	private SelfTopUpsConfig getConfig(EntityManager em, Session session)
	{
		SelfTopUpsConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, SelfTopUpsConfig.class);

		ConfirmationMenuHelper.constructMenus(this, config, //
				Ussd.COMMAND_SELF_TOPUP, //
				"Confirm Self Topup of " + SelfTopUpsConfig.AMOUNT + " Airtime?", //
				"Self Topup of " + SelfTopUpsConfig.AMOUNT + " Airtime Cancelled.", //
				"You performed Self Topup of " + SelfTopUpsConfig.AMOUNT + " Airtime less than " //
				+ IConfirmationMenuConfig.MINS_SINCE_LAST + " minute(s) ago, do you wish to proceed?");

		return config;
	}

	@PUT
	@Path("/self_topup/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.SelfTopUpsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_SELF_TOPUP);
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
		SelfTopUpsConfig configuration = company.getConfiguration(em, SelfTopUpsConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(SelfTopUpsConfig configuration, int companyID)
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
			String pinResult = session.offerPIN(em, session, true, values.get(SelfTopUpsConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Must Confirm?
			SelfTopUpsConfig config = getConfig(em, session);
			String recipientMSISDN = session.getMobileNumber();
			BigDecimal amount = new BigDecimal(values.get(SelfTopUpsConfig.AMOUNT));
			List<UssdMenu> confirmationMenu = ConfirmationMenuHelper.triggerConfirmation(this, em, interaction, context, config, session, //
					Ussd.COMMAND_SELF_TOPUP, getType(), recipientMSISDN, amount, null, values);
			
			if (confirmationMenu != null) {
				IMenuProcessor menuProcessor = this;
				new MenuProcessor(context) {
					@Override
					protected IMenuProcessor getUssdProcessor(int id) {
						return menuProcessor;
					}
				}.initiate(em, interaction, session, confirmationMenu, values, Ussd.COMMAND_SELL);
				return true;
			}

			// Create the request
			SelfTopUpRequest request = new SelfTopUpRequest();
			fillHeader(request, session, interaction);
			request.setAmount(amount);

			// Execute the Transaction
			SelfTopUpResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Self Top-up");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		SelfTopUpsConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, SelfTopUpsConfig.class);
		return config.listCommandFields();

	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case SelfTopUpsConfig.AMOUNT:
				return "the Amount";
			case SelfTopUpsConfig.PIN:
				return "you PIN";
		}
		return fieldName;
	}

	@Override
	public String menuExpandField(String englishName, Session session, Map<String, String> valueMap)
	{

		switch (englishName)
		{
			case SelfTopUpsConfig.AMOUNT:
				return format(session.getLocale(), new BigDecimal(valueMap.get(SelfTopUpsConfig.AMOUNT)));
		}

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
		if (!session.hasPermission(em, Transaction.MAY_SELF_TOPUP, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(SelfTopUpsConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Remove USSD Prompt
		disableUssdConfirmation(em, session, options);

		// Create the request
		SelfTopUpRequest request = new SelfTopUpRequest();
		fillHeader(request, session, interaction);
		request.setAmount(new BigDecimal(values.get(SelfTopUpsConfig.AMOUNT)));

		// Execute the Transaction
		SelfTopUpResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

}

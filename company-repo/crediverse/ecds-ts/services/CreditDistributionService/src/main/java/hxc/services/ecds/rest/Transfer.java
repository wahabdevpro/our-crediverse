package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.THE_RECIPIENT_NUMBER_AGAIN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_MSISDNS_MISMATCH;
import static hxc.services.ecds.rest.TransactionHelper.createDaRefillInfoList;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.isEmpty;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionBAfter;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static hxc.services.ecds.rest.ussd.MenuConstructor.createBNumberConfirmationMenu;
import static hxc.services.ecds.util.MobileNumberFormatHelper.MOBILE_NUMBER_FORMAT_HELPER;
import static hxc.services.ecds.util.MobileNumberFormatHelper.convertBNumber;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.removeBNumberConfirmationFromMenu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
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

import hxc.connectors.IInteraction;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.TransferRequest;
import hxc.ecds.protocol.rest.TransferResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.IConfirmationMenuConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.TransfersConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionExtraData;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.model.extra.DedicatedAccountRefillInfoAccounts;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuOption;
import hxc.services.ecds.rest.ussd.MenuProcessor;
import hxc.services.ecds.util.ConfirmationMenuHelper;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.transactions.ITransaction;
import hxc.utils.protocol.ucip.RefillResponse;

@Path("/transactions")
public class Transfer extends Transactions<hxc.ecds.protocol.rest.TransferRequest, hxc.ecds.protocol.rest.TransferResponse> //
		implements IChannelTarget, IMenuProcessor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_FROM_ROOT = "FROM_ROOT";
	private static final String PROP_CO_AUTH_MSISDN = "CO_AUTH_MSISDN";
	private static final String PROP_CO_AUTH_LANG = "CO_AUTH_LANG";

	private static final String PROP_SUBSCRIBER_LANGUAGE = "SUBS_LANG";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Transfer()
	{

	}

	public Transfer(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/transfer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.TransferResponse execute(hxc.ecds.protocol.rest.TransferRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<TransferRequest, TransferResponse> state) throws RuleCheckException
	{
		// Record Request Information
		TransferRequest request = state.getRequest();
		Session session = state.getSession();
		String bMSISDN = context.toMSISDN(state.getRequest().getTargetMSISDN());
		state.setRequestInfo(session.getMobileNumber(), bMSISDN, request.getAmount());

		// Get the A Agent from the session
		Agent aAgent = state.getSessionAgent(em, false);

		// Validate the Amount
		state.testAmountDecimalDigits(state.getRequest().getAmount());

		// If it is not null, it may not be from ROOT
		IAgentUser user = null;
		if (aAgent != null)
		{
			// Check Permission
			session.check(em, Transaction.MAY_TRANSFER, true);

			Tier tier = aAgent.getTier();
			if (Tier.TYPE_ROOT.equals(tier.getType()))
				throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "agentID", "%d is not a valid AgentID", session.getAgentID());
			user = state.getSessionUser(em);

			// Validate the A Agent's IMSI and State
			validateAgentImsi(em, state, user);

			// Update the A Agent's IMEI
			updateAgentImei(em, state, user);

			validateAgentState(user, false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE, hxc.ecds.protocol.rest.Agent.STATE_PERMANENT);
		}

		// else, If it is null, this is a WebUser session transferring from ROOT
		else
		{
			if (!Session.CHANNEL_WUI.equals(session.getChannel()))
				throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "channel", "Only Web Channel allowed");

			// Check Permission
			session.check(em, Transaction.MAY_TRANSFER_ROOT, true);

			// Validate co-signatory
			Session coSession = state.getSession(state.getRequest().getCoSignatorySessionID());

			// Cannot be the same as requester
			if (coSession == null || coSession.getWebUserID() == session.getWebUserID() || coSession.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

			// Validate coSignable
			coSession.validateCoSignable(context, state.getRequest(), session.getSessionID());

			// Check co-signatory Permission
			coSession.check(em, Transaction.MAY_AUTHORISE_TRANSFER_ROOT, true);

			// Save co-signatory's details
			state.set(PROP_CO_AUTH_LANG, coSession.getLanguageID());
			state.set(PROP_CO_AUTH_MSISDN, coSession.getMobileNumber());

			// Get the Root Agent
			user = aAgent = Agent.findRoot(em, session.getCompanyID());
		}

		// Validate the A Agent
		validateAgentState(aAgent, false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE, hxc.ecds.protocol.rest.Agent.STATE_PERMANENT);

		// Set the A Agent
		state.setAgentA(aAgent);

		// Get the B Agent
		Agent bAgent = Agent.findByMSISDN(em, bMSISDN, session.getCompanyID());
		if (bAgent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_FORBIDDEN, "targetMSISDN", "%s is not a valid Recipient", bMSISDN);

		// Set the B Agent
		state.setAgentB(bAgent);

		// Bonus Percentage undefined?
		boolean fromRoot = Tier.TYPE_ROOT.equals(aAgent.getTier().getType());
		state.set(PROP_FROM_ROOT, fromRoot);
		if (!fromRoot && aAgent.getTier().getDownStreamPercentage() == null || bAgent.getTier().getDownStreamPercentage() == null)
			throw new RuleCheckException(StatusCode.AMBIGUOUS, "b_TierID", "Incomplete Transfer Rules");

		// Cannot transfer to self
		if (bAgent.getId() == aAgent.getId())
			throw new RuleCheckException(TransactionsConfig.ERR_NOT_SELF, "targetMSISDN", "");

		// Get cached location
		TransfersConfig config = state.getConfig(em, TransfersConfig.class);
		state.getCachedLocation(em, user, config.isForceLocationOfAgent());

		// Get the Applicable TransferRule
		Transaction transaction = state.getTransaction();
		if (aAgent.getTierID() == bAgent.getTierID())
		{
			// Bonus-less intra-tier transfer
			Tier tier = aAgent.getTier();
			if(tier.isAllowIntraTierTransfer())
			{
				transaction.setTransferRuleID(null);
				transaction.setTransferRule(null);
			} else {
				throw new RuleCheckException(TransactionsConfig.ERR_INTRATIER_TRANSFER, "tierID", "Intra-tier transfers are not allowed on this tier %s", tier.getName());
			}
		}
		else
		{
			// Get Transfer Rule
			TransferRule transferRule = state.findTransferRule(em, //
					state.getRequest().getAmount(), //
					transaction.getStartTime(), //
					user, //
					aAgent, //
					state.getAgentB(), //
					state.getAgentB().getTierID());

			transaction.setTransferRuleID(transferRule.getId());
			transaction.setTransferRule(transferRule);
		}

		// Update the B Agent's IMEI
		updateAgentImei(em, state, bAgent);

		// Enforce Strict Area
		state.enforceAStrictArea(em, user, aAgent);

		// Obtain Location for Promotional Purposes
		state.obtainALocationForPromotions(em, user);
	}

	@Override
	protected void execute(EntityManager em, TransactionState<TransferRequest, TransferResponse> state) throws RuleCheckException {

		Transaction transaction = state.getTransaction();

		boolean haveToRefill = false;
		BigDecimal bBonusAmount = null;
		String refProfile = null;
		Account  aAccount = null;
		Account  bAccount = null;
		int currencyDecimalDigits = context.getMoneyScale();
		BigDecimal amount = BigDecimal.ZERO; 
	
		BigDecimal buyerBalanceDelta  = BigDecimal.ZERO;
		BigDecimal buyerTradeBonusProvisionDelta = BigDecimal.ZERO;
		BigDecimal buyerTradeBonusAmountEarned = BigDecimal.ZERO;

		try(RequiresTransaction trans = new RequiresTransaction(em))  // Crediverse update transaction no catch trying for RIIA
		{

			amount = state.getRequest().getAmount();


			TransferRule transferRule = transaction.getTransferRule();

			BigDecimal one = new BigDecimal(1.0);

			bAccount = findAccount(em, state.getAgentB().getId());
			state.setBeforeB(bAccount);

			BigDecimal buyerTradeBonusPercentage = transferRule == null ? BigDecimal.ZERO : transferRule.getBuyerTradeBonusPercentage();

			buyerTradeBonusAmountEarned = buyerTradeBonusPercentage.multiply(amount);

			BigDecimal cumulativeTradeBonusPercentage = state.getAgentB().getTier().getDownStreamPercentage();

			buyerBalanceDelta = amount.add(buyerTradeBonusAmountEarned).setScale(currencyDecimalDigits, RoundingMode.CEILING);

			BigDecimal currentBuyerTradeBonusProvision = bAccount.getBonusBalance().setScale(currencyDecimalDigits, RoundingMode.CEILING);

			BigDecimal currentBuyerBalance = bAccount.getBalance() ; 

			buyerTradeBonusProvisionDelta = currentBuyerBalance.add(buyerBalanceDelta)
				.multiply(cumulativeTradeBonusPercentage)
					.setScale(currencyDecimalDigits, RoundingMode.CEILING).subtract(currentBuyerTradeBonusProvision);


			transaction.setAmount(amount.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setGrossSalesAmount(amount.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setBuyerTradeBonusAmount(buyerTradeBonusAmountEarned.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setBuyerTradeBonusProvision(buyerTradeBonusProvisionDelta.setScale(currencyDecimalDigits, RoundingMode.CEILING));
			transaction.setBuyerTradeBonusPercentage(buyerTradeBonusPercentage) ;

			bAccount.adjust(
					buyerBalanceDelta , 
					buyerTradeBonusProvisionDelta,
					false);

			aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);
			
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

			boolean fromRoot = state.get(PROP_FROM_ROOT);
			aAccount.transact(
					transaction.getStartTime(), 
					amount.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
					BigDecimal.ZERO, 
					(buyerTradeBonusProvisionDelta.add(buyerTradeBonusAmountEarned)).negate()
					.setScale(currencyDecimalDigits, RoundingMode.CEILING).negate(),
					fromRoot);

			transaction.testAmlLimitsA(aAccount, amount.setScale(currencyDecimalDigits, RoundingMode.CEILING));

			haveToRefill = transferRule != null && transferRule.getTargetBonusPercentage() != null;
			
			if (haveToRefill) {
				bBonusAmount = amount.multiply(transferRule.getTargetBonusPercentage()).setScale(0, RoundingMode.UP);
				refProfile = transferRule.getTargetBonusProfile();
				transaction.setB_TransferBonusAmount(bBonusAmount.setScale(currencyDecimalDigits, RoundingMode.CEILING));
				transaction.setB_TransferBonusProfile(refProfile);

				// Set the follow-up flag and move the money in the ON-HOLD.
				// If unexpected exception occurs the transaction will be in this state and have to be fixed manually.
				transaction.setFollowUp(true);
			} 

			setTransactionAAfter(transaction, aAccount);
			setTransactionBAfter(transaction, bAccount);
			
			transaction.persist(em, null, state.getSession(), null);
			
			Double longitude = state.getRequest().getLongitude();
			Double latitude = state.getRequest().getLatitude();
			long transactionId = transaction.getId();

			if (longitude != null && latitude != null) {
				transaction.persistTransactionLocation(em, longitude, latitude, transactionId);
			}

			updateInDb(em, trans, transaction, aAccount, bAccount);
		}
		
		if (!haveToRefill) {
			return;
		}

		ITransaction tx = defineAirTransaction(transaction);
		Subscriber subscriber = new Subscriber(transaction.getB_MSISDN(), context.getAirConnector(), tx);
		TransfersConfig config = state.getConfig(em, TransfersConfig.class);
		ReversalsConfig reversalsConfig = state.getConfig(em, ReversalsConfig.class);

		em.close();
		try  // AIR call for refil
		{
			IAirConnector air = context.getAirConnector();
			long longAmount = air.toLongAmount(bBonusAmount);
			List<String> externalDataList = 
				this.expandExternalDataList(
						Arrays.asList(
							config.getRefillExternalData1(), 
							config.getRefillExternalData2(), 
							config.getRefillExternalData3(), 
							config.getRefillExternalData4()), 
						config.listExternalDataFields(), state);

			RefillResponse result = subscriber.refillAccount(refProfile, longAmount, false,
					externalDataList, reversalsConfig.isEnableDedicatedAccountReversal());

			DedicatedAccountRefillInfoAccounts dARefillInfoList = createDaRefillInfoList(result);
			
			try (EntityManagerEx em2 = context.getEntityManager()) {
				try (RequiresTransaction trans = new RequiresTransaction(em2)) {
					aAccount = findAccount(em2, aAccount.getID());
					bAccount = findAccount(em2, bAccount.getID());
					transaction = Transaction.findByID(em2, transaction.getId(), transaction.getCompanyID());
					state.setTransaction(transaction);

					if (dARefillInfoList != null && !isEmpty(dARefillInfoList.getDedicatedAccountRefillInfos())) {
						logger.debug(" size dARefillInfoList to save: {}", dARefillInfoList.getDedicatedAccountRefillInfos().size());
						//only save extra data if we have dedicated account info returned in the refill info
						transaction.addExtraDataForKeyType(TransactionExtraData.Key.DEDICATED_ACCOUNT_REFILL_INFO, dARefillInfoList);
					}
					transaction.persistExtraData(em2);

					state.set(PROP_SUBSCRIBER_LANGUAGE, subscriber.getLanguageCode2());
					transaction.setFollowUp(false);
					setTransactionAAfter(transaction, aAccount);
					setTransactionBAfter(transaction, bAccount);
					updateInDb(em2, trans, transaction, aAccount, bAccount);
				}
			}
		} 

		catch (AirException e) 
		{
			logger.warn("Air Exception", e);
			try (EntityManagerEx em2 = context.getEntityManager()) {
				try (RequiresTransaction trans = new RequiresTransaction(em2)) {
					aAccount = findAccount(em2, aAccount.getID());
					bAccount = findAccount(em2, bAccount.getID());
					transaction = Transaction.findByID(em2, transaction.getId(), transaction.getCompanyID());
					state.setTransaction(transaction);
					if (e.isDeterministic() && isDeterministic(config.getNonDeterministicErrorCodes(), e.getResponseCode())) {
						state.exitWith(mapAirResponseCode(e.getResponseCode()), e.getMessage());
						transaction.setLastExternalResultCode(Integer.toString(e.getResponseCode()));

						transaction.setBuyerTradeBonusProvision(BigDecimal.ZERO);

						transaction.setFollowUp(false);

						aAccount.adjust(
								amount.setScale(currencyDecimalDigits, RoundingMode.CEILING)	
								.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
								(buyerTradeBonusProvisionDelta.add(buyerTradeBonusAmountEarned)).negate()
								.setScale(currencyDecimalDigits, RoundingMode.CEILING), false);

						bAccount.adjust(
								buyerBalanceDelta.negate()
								.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
								buyerTradeBonusProvisionDelta.negate()
								.setScale(currencyDecimalDigits, RoundingMode.CEILING), 
								false);

					} else {
						state.getResponse().setAdditionalInformation(String.format("Transfer bonus credit may have failed - Follow Up! Air-Node: [%s]", e.getHost()));
            state.getResponse().setFollowUp(true);
					}
					setTransactionAAfter(transaction, aAccount);
					setTransactionBAfter(transaction, bAccount);
					updateInDb(em2, trans, transaction, aAccount);
				}
			}
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<TransferRequest, TransferResponse> state)
	{
		TransfersConfig config = state.getConfig(em, TransfersConfig.class);
		boolean fromRoot = state.get(PROP_FROM_ROOT);
		if (fromRoot)
		{
			// Notify Requester (Web-User)
			sendResponse(config.getRequesterNotification(), config.listNotificationFields(), state);

			// Notify A-Party
			Agent aAgent = state.getAgentA();
			sendNotification(aAgent.getMobileNumber(), config.getSenderNotification(), config.listNotificationFields(), //
					state.getLocale(aAgent.getLanguage()), state);
		}
		else
		{
			// Notify Requester (A-Party)
			sendResponse(config.getSenderNotification(), config.listNotificationFields(), state);
		}

		// Notify B-Party
		Agent bAgent = state.getAgentB();
		sendNotification(bAgent.getMobileNumber(), config.getRecipientNotification(), config.listNotificationFields(), //
				state.getLocale(bAgent.getLanguage()), state);

		// Send a Stock Depletion message
		Transaction transaction = state.getTransaction();
		Agent aAgent = transaction.getA_Agent();
		BigDecimal threshold = aAgent.getWarningThreshold();
		if (moreThan(transaction.getA_BalanceBefore(), threshold) //
				&& !moreThan(transaction.getA_BalanceAfter(), threshold))
		{
			AgentsConfig agentsConfig = state.getConfig(em, AgentsConfig.class);
			sendNotification(aAgent.getMobileNumber(), agentsConfig.getDepletionNotification(), agentsConfig.listDepletionFields(), //
					state.getLocale(aAgent.getLanguage()), state);
			Agent supplier = aAgent.getSupplier();
			if (supplier != null)
				sendNotification(supplier.getMobileNumber(), agentsConfig.getDepletionNotification(), agentsConfig.listDepletionFields(), //
						state.getLocale(supplier.getLanguage()), state);
		}
	}

	private boolean moreThan(BigDecimal left, BigDecimal right)
	{
		return left != null && right != null && left.compareTo(right) > 0;
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<TransferRequest, TransferResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case TransfersConfig.SENDER_MSISDN:
				return transaction.getA_MSISDN();

			case TransfersConfig.RECIPIENT_MSISDN:
				return transaction.getB_MSISDN();

			case TransfersConfig.AMOUNT:
				return format(locale, transaction.getAmount());

			case TransfersConfig.BONUS_PROVISION_AMOUNT:
				return format(locale, transaction.getBuyerTradeBonusProvision());

			case TransfersConfig.TRADE_BONUS:
				return format(locale, transaction.getBuyerTradeBonusAmount());

			case TransfersConfig.TOTAL_AMOUNT:
				return format(locale, transaction.getAmount().add(transaction.getBuyerTradeBonusProvision()).add(transaction.getBuyerTradeBonusAmount()));

			case AgentsConfig.SENDER_THRESHOLD:
				return format(locale, transaction.getA_Agent().getWarningThreshold());

			case TransfersConfig.SENDER_NEW_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case TransfersConfig.SENDER_NEW_BONUS_BALANCE:
				return format(locale, transaction.getA_BonusBalanceAfter());

			case TransfersConfig.SENDER_NEW_TOTAL_BALANCE:
				return format(locale, transaction.getA_BalanceAfter().add(transaction.getA_BonusBalanceAfter()));

			case TransfersConfig.RECIPIENT_NEW_BALANCE:
				return format(locale, transaction.getB_BalanceAfter());

			case TransfersConfig.RECIPIENT_NEW_BONUS_BALANCE:
				return format(locale, transaction.getB_BonusBalanceAfter());

			case TransfersConfig.RECIPIENT_NEW_TOTAL_BALANCE:
				return format(locale, transaction.getB_BalanceAfter().add(transaction.getB_BonusBalanceAfter()));

			default:
				return super.expandField(englishName, locale, state);
		}
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
	@Path("/transfer/config")
	@Produces(MediaType.APPLICATION_JSON)
	public TransfersConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
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

	private TransfersConfig getConfig(EntityManager em, Session session)
	{
		TransfersConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransfersConfig.class);

		ConfirmationMenuHelper.constructMenus(this, config, //
				Ussd.COMMAND_TRANSFER, //
				"Confirm Transfer of " + TransfersConfig.AMOUNT + " to " + TransfersConfig.RECIPIENT_MSISDN + "?", //
				"Transfer of " + TransfersConfig.AMOUNT + " to " + TransfersConfig.RECIPIENT_MSISDN + " Cancelled.", //
				"You transferred " + TransfersConfig.AMOUNT + " to " + TransfersConfig.RECIPIENT_MSISDN + " less than " + IConfirmationMenuConfig.MINS_SINCE_LAST
						+ " minute(s) ago, do you wish to proceed with this transfer ?");

		return config;
	}

	@PUT
	@Path("/transfer/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(TransfersConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_TRANSFERS);
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
		TransfersConfig configuration = company.getConfiguration(em, TransfersConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(TransfersConfig configuration, int companyID)
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
				return respond(session, interaction, TransactionsConfig.ERR_INVALID_AGENT, companyID);
				
			String error = convertBNumber(values, em);
			if (error != null) {
				MOBILE_NUMBER_FORMAT_HELPER.initErrorMessages(session, em);
				return respond(session, interaction, error, companyID);
			}

			// Must Confirm?
			TransfersConfig config = getConfig(em, session);
			String recipientMSISDN = values.get(TransfersConfig.RECIPIENT_MSISDN);
			BigDecimal amount = new BigDecimal(values.get(TransfersConfig.AMOUNT));
			List<UssdMenu> confirmationMenu = ConfirmationMenuHelper.triggerConfirmation(this, em, interaction, context, config, session, //
					Ussd.COMMAND_TRANSFER, getType(), recipientMSISDN, amount, null, values);
			
			// Must confirm B number?
			if (config.getEnableBNumberConfirmation()) {
				confirmationMenu = createBNumberConfirmationMenu(
						Ussd.COMMAND_TRANSFER,
						confirmationMenu,
						config.getNumberConfirmMessage());
			} else {
				if (confirmationMenu != null) {
					removeBNumberConfirmationFromMenu(this, confirmationMenu);
				}
			}
			
			if (confirmationMenu != null) {
				IMenuProcessor menuProcessor = this;
				new MenuProcessor(context) {
					@Override
					protected IMenuProcessor getUssdProcessor(int id) {
						return menuProcessor;
					}
				}.initiate(em, interaction, session, confirmationMenu, values, Ussd.COMMAND_TRANSFER);
				return true;
			}

			// Validate PIN
			String pinResult = session.offerPIN(em, session, true, values.get(TransfersConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			TransferRequest request = new TransferRequest();
			fillHeader(request, session, interaction);
			request.setTargetMSISDN(recipientMSISDN);
			request.setAmount(amount);

			// Execute the Transaction
			TransferResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Transfer");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		TransfersConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, TransfersConfig.class);
		return config.listCommandFields();

	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case TransfersConfig.RECIPIENT_MSISDN:
				return "the Recipient's number";

			case RECIPIENT_MSISDN_CONFIRMED:
				return THE_RECIPIENT_NUMBER_AGAIN;

			case TransfersConfig.PIN:
				return "Your PIN";

			case TransfersConfig.AMOUNT:
				return "the Amount";
		}
		return fieldName;
	}

	@Override
	public String menuExpandField(String englishName, Session session, Map<String, String> valueMap)
	{
		switch (englishName)
		{
			case TransfersConfig.AMOUNT:
				return format(session.getLocale(), new BigDecimal(valueMap.get(TransfersConfig.AMOUNT)));
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
		if (!session.hasPermission(em, Transaction.MAY_TRANSFER, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, hxc.ecds.protocol.rest.Agent.STATE_ACTIVE);

		return true;
	}

	@Override
	public TransactionResponse menuExecute(EntityManager em, Session session, IInteraction interaction, Map<String, String> values, int options) throws RuleCheckException
	{
		String error = convertBNumber(values, em);
		if (error != null) {
			MOBILE_NUMBER_FORMAT_HELPER.initErrorMessages(session, em);
			return respondWithError(error);
		}

		// Validate PIN
		final String pinResult = session.offerPIN(em, session, true, values.get(TransfersConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Remove USSD Prompt
		disableUssdConfirmation(em, session, options);

		// Create the request
		TransferRequest request = new TransferRequest();
		fillHeader(request, session, interaction);
		String recipientMsisdn = values.get(TransfersConfig.RECIPIENT_MSISDN);

		// B MSISDN confirmation
		TransfersConfig config = getConfig(em, session);
		if (config.getEnableBNumberConfirmation()) {
			if (!recipientMsisdn.equals(values.get(RECIPIENT_MSISDN_CONFIRMED))) {
				TransactionsConfig transactionsConfig = session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class);
				transactionsConfig.getErrorMessages().put(ERR_MSISDNS_MISMATCH, config.getNumberErrorMessage());
				return respondWithError(ERR_MSISDNS_MISMATCH);
			}
		}

		request.setTargetMSISDN(recipientMsisdn);
		request.setAmount(new BigDecimal(values.get(TransfersConfig.AMOUNT)));

		// Execute the Transaction
		TransferResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

}

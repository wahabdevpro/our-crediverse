package hxc.services.ecds.rest;

import static hxc.connectors.bundles.IBundleProvider.StatusCode.Failed;
import static hxc.connectors.bundles.IBundleProvider.StatusCode.NotEligible;
import static hxc.connectors.bundles.IBundleProvider.StatusCode.Success;
import static hxc.connectors.bundles.IBundleProvider.StatusCode.Unknown;
import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.RECIPIENT_MSISDN_CONFIRMED;
import static hxc.ecds.protocol.rest.config.IConfirmationMenuConfig.THE_RECIPIENT_NUMBER_AGAIN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_BUNDLE_SALE_FAILED;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_MSISDNS_MISMATCH;
import static hxc.services.ecds.rest.TransactionHelper.defineAirTransaction;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static hxc.services.ecds.rest.ussd.MenuConstructor.createBNumberConfirmationMenu;
import static hxc.services.ecds.util.MobileNumberFormatHelper.MOBILE_NUMBER_FORMAT_HELPER;
import static hxc.services.ecds.util.MobileNumberFormatHelper.convertBNumber;
import static hxc.services.ecds.util.MsisdnBConfirmationHelper.removeBNumberConfirmationFromMenu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.bundles.IBundleProvider;
import hxc.connectors.bundles.IBundleProvider.StatusCode;
import hxc.connectors.hlr.IHlrInformation;
import hxc.ecds.protocol.rest.SellBundleRequest;
import hxc.ecds.protocol.rest.SellBundleResponse;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.IConfirmationMenuConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.BundleLanguage;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuOption;
import hxc.services.ecds.rest.ussd.MenuProcessor;
import hxc.services.ecds.rest.ussd.MenuState;
import hxc.services.ecds.util.ConfirmationMenuHelper;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.transactions.ITransaction;

/* 
 * This is deprecated and not being used.
 * Please don't get confused, whenever someone refers to bundle sale, go to /debit (success) & /refund (failure)!! ;)
 */
@Path("/transactions")
public class SellBundle extends Transactions<hxc.ecds.protocol.rest.SellBundleRequest, //
		hxc.ecds.protocol.rest.SellBundleResponse> implements IChannelTarget, IMenuProcessor
{
	final static Logger logger = LoggerFactory.getLogger(SellBundle.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String PROP_SUBSCRIBER_LANGUAGE = "SUBS_LANG";
	private static final String PROP_RESULT = "RESULT";
	private static final String USSD_BUNDLE_TYPE = "{BundleType}";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SellBundle()
	{

	}

	public SellBundle(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@POST
	@Path("/sell_bundle")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public hxc.ecds.protocol.rest.SellBundleResponse execute(hxc.ecds.protocol.rest.SellBundleRequest request)
	{
		return super.execute(request);
	}

	@Override
	protected String getType()
	{
		return Transaction.TYPE_SELL_BUNDLE;
	}

	@Override
	protected void validate(EntityManager em, TransactionState<SellBundleRequest, SellBundleResponse> state) throws RuleCheckException
	{
		// Record Request Information
		Transaction transaction = state.getTransaction();
		SellBundleRequest request = state.getRequest();
		transaction.setB_MSISDN(request.getTargetMSISDN());

		// Get the Configuration
		Session session = state.getSession();
		state.getConfig(em, BundleSalesConfig.class);

		// Get the Bundle
		int bundleID = state.getRequest().getBundleID();
		Bundle bundle = Bundle.findByID(em, bundleID, session.getCompanyID());
		if (bundle == null || !Bundle.STATE_ACTIVE.equals(bundle.getState()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_BUNDLE, "bundleID", "%d is not a valid, active Bundle", bundleID);
		transaction.setBundleID(bundleID);
		transaction.setBundle(bundle);

		// Check Permission
		session.check(em, Transaction.MAY_SELL_BUNDLES, true);

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

		// Cannot sell to self
		String bMSISDN = context.toMSISDN(state.getRequest().getTargetMSISDN());
//		if (aAgent.getMobileNumber().equals(bMSISDN))
//			throw new RuleCheckException(TransactionsConfig.ERR_NOT_SELF, "targetMSISDN", "Cannot Sell to Self");

		// Acquire B Party's IMSI
		IHlrInformation info = context.getHlrInformation(bMSISDN, false, false, true);
		if (info != null)
			transaction.setB_IMSI(info.getIMSI());
		if (transaction.getB_IMSI() == null || transaction.getB_IMSI().isEmpty())
			throw new RuleCheckException(TransactionsConfig.ERR_NO_IMSI, "targetMSISDN", "No IMSI available for %s", bMSISDN);

		// Save the B_Party MSISDN
		transaction.setB_MSISDN(bMSISDN);
		Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
		transaction.setB_TierID(subscriberTier.getId());

		// Test Eligibility
		boolean isEligible = false;
		try
		{
			IBundleProvider bundleProvider = context.getBundleProvider();
			StatusCode result = bundleProvider.isEligible(transaction.getB_MSISDN(), bundle.getTag(), transaction.getB_IMSI());
			if (result.equals(Success))
				isEligible = true;
		}
		catch (Throwable tr)
		{
			logger.warn("Validation issue", tr);
		}
		if (!isEligible)
			throw new RuleCheckException(TransactionsConfig.ERR_NOT_ELIGIBLE, "bundleID", "Subscriber not eligible to buy this Bundle");

		// Get cached location
		BundleSalesConfig config = state.getConfig(em, BundleSalesConfig.class);
		state.getCachedLocation(em, user, config.isForceLocationOfAgent());

		Cell bCell = state.getLocation(em, bMSISDN, "Forced location for subscriber", config.isForceLocationOfSubscriber(), 
			false, config.isEnableSubscriberLocationCaching(), config.getLocationCachingExpiryMinutes());
		if (bCell != null)
		{
			transaction.setB_CellID(bCell.getId());	
			transaction.setB_Cell(bCell);
		}
			
		// Get the Transfer Rule
		TransferRule transferRule = state.findTransferRule(em, //
				bundle.getPrice(), //
				transaction.getStartTime(), //
				user, //
				aAgent, //
				null, //
				subscriberTier.getId());

		transaction.setTransferRuleID(transferRule.getId());
		transaction.setTransferRule(transferRule);

		// Enforce Strict Area
		state.enforceAStrictArea(em, user, aAgent);

		// Obtain Location for Promotional Purposes
		state.obtainALocationForPromotions(em, user);
	}

	@Override
	protected void execute(EntityManager em, TransactionState<SellBundleRequest, SellBundleResponse> state) throws RuleCheckException {
		final Transaction transaction = state.getTransaction();
		Bundle bundle = transaction.getBundle();
		BigDecimal price = bundle.getPrice();
		int currencyDecimalDigits = context.getMoneyScale();
		BigDecimal discountedPrice = price
				.multiply(BigDecimal.ONE.subtract(bundle.getTradeDiscountPercentage()))
				.setScale(currencyDecimalDigits, RoundingMode.UP);
		final Account aAccount;
		
		try(RequiresTransaction trans = new RequiresTransaction(em)) {
			aAccount = findAccount(em, state.getAgentA().getId());
			state.setBeforeA(aAccount);
			aAccount.transact(transaction.getStartTime(), discountedPrice, BigDecimal.ZERO, BigDecimal.ZERO, false);
			transaction.setBuyerTradeBonusAmount(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusProvision(BigDecimal.ZERO);
			transaction.setBuyerTradeBonusPercentage(BigDecimal.ZERO);
			transaction.setAmount(discountedPrice);

			transaction.testAmlLimitsA(aAccount, discountedPrice);
			
			// Set the follow-up flag and move the money in the ON-HOLD.
			// If unexpected exception occurs the transaction will be in this state and have to be fixed manually.
			transaction.setFollowUp(true);
			aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().add(discountedPrice));
			setTransactionAAfter(transaction, aAccount);
			
			transaction.persist(em, null, state.getSession(), null);
			updateInDb(em, trans, transaction, aAccount);
		}
		
		ITransaction tx = defineAirTransaction(transaction);
		Subscriber subscriber = new Subscriber(transaction.getB_MSISDN(), context.getAirConnector(), tx);

		// Provision the Bundle which is allowed to fail
		state.set(PROP_RESULT, Failed);
		
		try {
			subscriber.getAccountDetails();
		} catch (AirException ex) {
			logger.warn("Air Exception", ex);
			state.set(PROP_RESULT, Failed);
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				transaction.setLastExternalResultCode(Integer.toString(ex.getResponseCode()));
				transaction.setFollowUp(false);
				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(discountedPrice));
				aAccount.adjust(discountedPrice, BigDecimal.ZERO, false);
				setTransactionAAfter(transaction, aAccount);
				updateInDb(em, trans, transaction, aAccount);
			}
			state.exitWith(TransactionsConfig.ERR_BUNDLE_SALE_FAILED, ex.getMessage());
			return;
		}
		
		state.set(PROP_SUBSCRIBER_LANGUAGE, subscriber.getLanguageCode2());

		Cell cellA = transaction.getA_Cell();
		Cell cellB = transaction.getB_Cell();
		Integer cellIdA = cellA == null ? null : cellA.getCellID();
		Integer cellIdB = cellB == null ? null : cellB.getCellID();
		String cellGroupA = cellA == null || cellA.getCellGroups().size() < 1 ? null : cellA.getCellGroups().get(0).getCode();
		String cellGroupB = cellB == null || cellB.getCellGroups().size() < 1 ? null : cellB.getCellGroups().get(0).getCode();
		Integer cellGroupIdA = cellA == null || cellA.getCellGroups().size() < 1 ? null : cellA.getCellGroups().get(0).getId();
		Integer cellGroupIdB = cellB == null || cellB.getCellGroups().size() < 1 ? null : cellB.getCellGroups().get(0).getId();
		transaction.setA_CellGroupID(cellGroupIdA);
		transaction.setB_CellGroupID(cellGroupIdB);

		// Provision
		IBundleProvider bundleProvider = context.getBundleProvider();
		
		// Call to SmartShop which calls AIR, so AirException is not thrown here.
		StatusCode result = bundleProvider.provision(transaction.getB_MSISDN(), bundle.getTag(), transaction.getA_MSISDN(),
													 transaction.getNumber(), transaction.getB_IMSI(), price, cellIdA,
													 cellGroupA, cellIdB, cellGroupB);
		state.set(PROP_RESULT, result);
		if (result.equals(Success)) {
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				transaction.setFollowUp(false);
				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(discountedPrice));
				state.setAfterA(aAccount);
				setTransactionAAfter(transaction, aAccount);
				updateInDb(em, trans, transaction, aAccount);
			}
		} else if (result.equals(Failed) || result.equals(NotEligible)) {
			try (RequiresTransaction trans = new RequiresTransaction(em)) {
				transaction.setFollowUp(false);
				aAccount.setOnHoldBalance(aAccount.getOnHoldBalance().subtract(discountedPrice));
				aAccount.adjust(discountedPrice, BigDecimal.ZERO, false);
				setTransactionAAfter(transaction, aAccount);
				updateInDb(em, trans, transaction, aAccount);
			}
			state.exitWith(ERR_BUNDLE_SALE_FAILED, "Failed");
		} else if (result.equals(Unknown)) {
			state.getResponse().setAdditionalInformation("May have failed - Follow Up!");
      state.getResponse().setFollowUp(true);
		}
	}

	@Override
	protected void conclude(EntityManager em, TransactionState<SellBundleRequest, SellBundleResponse> state)
	{
		// Determine which messages to send
		BundleSalesConfig bundleSalesConfig = state.getConfig(em, BundleSalesConfig.class);
		StatusCode result = state.get(PROP_RESULT);
		Phrase senderNotification;
		Phrase recipientNotification;
		switch (result)
		{
			case Success:
				senderNotification = bundleSalesConfig.getSenderCompleteNotification();
				recipientNotification = bundleSalesConfig.getRecipientCompleteNotification();
				break;

			case Unknown:
				senderNotification = bundleSalesConfig.getSenderUnknownNotification();
				recipientNotification = bundleSalesConfig.getRecipientUnknownNotification();
				break;

			default:
				senderNotification = bundleSalesConfig.getSenderFailedNotification();
				recipientNotification = bundleSalesConfig.getRecipientFailedNotification();
				break;
		}

		// Send Sender Message
		sendResponse(senderNotification, bundleSalesConfig.listNotificationFields(), state);

		// Send Recipient Message
		String subscriberLanguage = state.get(PROP_SUBSCRIBER_LANGUAGE);
		if (subscriberLanguage == null || subscriberLanguage.isEmpty())
		{
			SalesConfig salesConfig = state.getConfig(em, SalesConfig.class);
			subscriberLanguage = salesConfig.getDefaultSubscriberLanguageID();
		}
		logger.trace("Using subscriberLanguage {}", subscriberLanguage);
		sendNotification(state.getTransaction().getB_MSISDN(), recipientNotification, bundleSalesConfig.listNotificationFields(), //
				state.getLocale(subscriberLanguage), state);

		// Send a Stock Depletion message
		Transaction transaction = state.getTransaction();
		Agent aAgent = transaction.getA_Agent();
		BigDecimal threshold = aAgent.getWarningThreshold();
		if (moreThan(transaction.getA_BalanceBefore(), threshold) //
				&& !moreThan(transaction.getA_BalanceAfter(), threshold))
		{
			AgentsConfig agentConfig = state.getConfig(em, AgentsConfig.class);
			sendNotification(aAgent.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(), //
					state.getLocale(aAgent.getLanguage()), state);
			Agent supplier = aAgent.getSupplier();
			if (supplier != null)
			{
				sendNotification(supplier.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(), //
						state.getLocale(supplier.getLanguage()), state);
			}
		}
	}

	@Override
	protected void concludeAfterFailure(EntityManager em, TransactionState<SellBundleRequest, SellBundleResponse> state)
	{
		conclude(em, state);
	}

	private boolean moreThan(BigDecimal left, BigDecimal right)
	{
		return left != null && right != null && left.compareTo(right) > 0;
	}

	@Override
	public String expandField(String englishName, Locale locale, TransactionState<SellBundleRequest, SellBundleResponse> state)
	{
		Transaction transaction = state.getTransaction();
		switch (englishName)
		{
			case BundleSalesConfig.SENDER_MSISDN:
				return state.getSession().getMobileNumber();

			case AgentsConfig.SENDER_THRESHOLD:
				return format(locale, transaction.getA_Agent().getWarningThreshold());

			case BundleSalesConfig.RECIPIENT_MSISDN:
				return state.getTransaction().getB_MSISDN();

			case BundleSalesConfig.PRICE:
				return format(locale, transaction.getBundle().getPrice());

			case BundleSalesConfig.SENDER_NEW_BALANCE:
				return format(locale, transaction.getA_BalanceAfter());

			case BundleSalesConfig.BUNDLE_NAME:
				return nameFromBundle(locale, transaction.getBundle());

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
	@Path("/sell_bundle/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.BundleSalesConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return getConfig(em, session);
		}
		catch (RuleCheckException ex)
		{
			logger.warn("Rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/sell_bundle/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private BundleSalesConfig getConfig(EntityManager em, Session session)
	{
		BundleSalesConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, BundleSalesConfig.class);

		ConfirmationMenuHelper.constructMenus(this, config, //
				Ussd.COMMAND_SELL_BUNDLE, //
				"Confirm Sale of " + BundleSalesConfig.BUNDLE_NAME + " Bundle to " + BundleSalesConfig.RECIPIENT_MSISDN + " for " + BundleSalesConfig.PRICE + "?", //
				"Sale of " + BundleSalesConfig.BUNDLE_NAME + " Bundle to " + BundleSalesConfig.RECIPIENT_MSISDN + " for " + BundleSalesConfig.PRICE + " cancelled", //
				"You sold " + BundleSalesConfig.BUNDLE_NAME + " Bundle to " + BundleSalesConfig.RECIPIENT_MSISDN + " for " + BundleSalesConfig.PRICE + " less than " //
						+ IConfirmationMenuConfig.MINS_SINCE_LAST + " minute(s) ago, do you wish to proceed with this sale?");

		return config;
	}

	@PUT
	@Path("/sell_bundle/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.BundleSalesConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_BUNDLE_SALES);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);
			defineChannelFilters(configuration, session.getCompanyID());
		}
		catch (RuleCheckException ex)
		{
			logger.warn("Rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/sell_bundle/config", ex);
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
		BundleSalesConfig configuration = company.getConfiguration(em, BundleSalesConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(BundleSalesConfig configuration, int companyID)
	{
		context.defineChannelFilter(this, companyID, configuration.getUssdCommand(), configuration.listUssdCommandFields(), 1);
		context.defineChannelFilter(this, companyID, configuration.getSmsCommand(), configuration.listSmsCommandFields(), 2);
	}

	@Override
	public boolean processChannelRequest(int companyID, IInteraction interaction, Map<String, String> values, int tag)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Get a Session
			Session session = context.getSessions().getAgentSession(em, context, companyID, interaction);
			if (session == null)
			{
				logger.warn("processChannelRequest called with null session ID");
				return false;
			}
				
			String error = convertBNumber(values, em);
			if (error != null) {
				MOBILE_NUMBER_FORMAT_HELPER.initErrorMessages(session, em);
				return respond(session, interaction, error, companyID);
			}

			// Validate PIN
			String pinResult = session.offerPIN(em, session, true, values.get(BundleSalesConfig.PIN));
			if (pinResult != null)
				return respond(session, interaction, pinResult, companyID);

			// Create the request
			SellBundleRequest request = new SellBundleRequest();
			fillHeader(request, session, interaction);
			String recipientMSISDN = values.get(BundleSalesConfig.RECIPIENT_MSISDN);
			request.setTargetMSISDN(recipientMSISDN);

			// Get the Bundle from USSD
			String ussdCode = values.get(BundleSalesConfig.USSD_CODE);
			if (ussdCode != null && !ussdCode.isEmpty())
			{
				Bundle bundle = Bundle.findByUssdCode(em, ussdCode, companyID);
				if (bundle == null)
					return respond(session, interaction, TransactionsConfig.ERR_INVALID_BUNDLE, companyID);
				else
				{
					request.setBundleID(bundle.getId());
					values.put(BundleSalesConfig.PRICE, bundle.getPrice().toString());
					values.put(BundleSalesConfig.BUNDLE_NAME, bundle.getName());
				}
			}
			else
			{
				String smsKeyword = values.get(BundleSalesConfig.SMS_KEYWORD);
				if (smsKeyword != null && !smsKeyword.isEmpty())
				{
					Bundle bundle = Bundle.findBySmsKeyword(em, smsKeyword, companyID);
					if (bundle == null)
						return respond(session, interaction, TransactionsConfig.ERR_INVALID_BUNDLE, companyID);
					else
					{
						request.setBundleID(bundle.getId());
						values.put(BundleSalesConfig.PRICE, bundle.getPrice().toString());
						values.put(BundleSalesConfig.BUNDLE_NAME, bundle.getName());
					}
				}
				else
					return respond(session, interaction, TransactionsConfig.ERR_INVALID_BUNDLE, companyID);
			}

			// Must Confirm?
			BundleSalesConfig config = getConfig(em, session);
			BigDecimal amount = new BigDecimal(values.get(BundleSalesConfig.PRICE));
			List<UssdMenu> confirmationMenu = ConfirmationMenuHelper.triggerConfirmation(this, em, interaction, context, config, session, //
					Ussd.COMMAND_SELL_BUNDLE, getType(), recipientMSISDN, amount, request.getBundleID(), values);

			// Must confirm B number?
			if (config.getEnableBNumberConfirmation()) {
				confirmationMenu = createBNumberConfirmationMenu(
						Ussd.COMMAND_SELL_BUNDLE,
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
				}.initiate(em, interaction, session, confirmationMenu, values, Ussd.COMMAND_SELL_BUNDLE);
				return true;
			}

			// Execute the Transaction
			SellBundleResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
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
		return Phrase.en("Sell Bundles");
	}

	@Override
	public Phrase[] menuCommandFields(EntityManager em, int companyID)
	{
		return new Phrase[] { Phrase.en(BundleSalesConfig.RECIPIENT_MSISDN), Phrase.en(USSD_BUNDLE_TYPE), Phrase.en(BundleSalesConfig.USSD_CODE), Phrase.en(BundleSalesConfig.PIN) };
	}

	@Override
	public String menuDescribeField(String fieldName)
	{
		switch (fieldName)
		{
			case BundleSalesConfig.RECIPIENT_MSISDN:
				return "the Recipient's number";
			case RECIPIENT_MSISDN_CONFIRMED:
				return THE_RECIPIENT_NUMBER_AGAIN;
			case USSD_BUNDLE_TYPE:
				return "the Bundle Type";
			case BundleSalesConfig.USSD_CODE:
				return "the Bundle Name";
			case BundleSalesConfig.PIN:
				return "your PIN";
		}
		return fieldName;
	}

	@Override
	public String menuExpandField(String englishName, Session session, Map<String, String> valueMap)
	{

		switch (englishName)
		{
			case BundleSalesConfig.PRICE:
				return format(session.getLocale(), new BigDecimal(valueMap.get(BundleSalesConfig.PRICE)));
		}

		return null;
	}

	@Override
	public Phrase[] menuInformationFields(EntityManager em, int companyID)
	{
		return new Phrase[] { Phrase.en(BundleSalesConfig.PRICE), Phrase.en(BundleSalesConfig.BUNDLE_NAME) };
	}

	@Override
	public MenuOption[] menuOptions(EntityManager em, Session session, String field)
	{
		boolean forBundleType = USSD_BUNDLE_TYPE.equals(field);
		boolean forUssdCode = BundleSalesConfig.USSD_CODE.equals(field);

		if (!forBundleType && !forUssdCode)
			return null;

		// If session is not available, return Empty List
		if (session == null)
			return new MenuOption[0];

		List<Bundle> bundles = Bundle.findForUssdMenu(em, session.getCompanyID());

		// For Bundle Types
		List<MenuOption> options = new ArrayList<MenuOption>();
		if (forBundleType)
		{
			List<String> types = new ArrayList<String>();
			for (Bundle bundle : bundles)
			{
				// Unique types
				String type = bundle.getType();
				if (types.contains(type))
					continue;
				types.add(type);

				// Get Type Name in all Languages
				Phrase name = new Phrase();
				name.set(Phrase.ENG, type);
				for (BundleLanguage bundleLanguage : bundle.getLanguages())
				{
					name.set(bundleLanguage.getLanguage(), bundleLanguage.getType());
				}
				MenuOption option = new MenuOption() //
						.setName(name) //
						.setValue(type);
				options.add(option);
			}
		}

		// Get active bundles
		else if (forUssdCode)
		{
			MenuState state = session.get(MenuState.PROP_USSD_MENU_STATE);
			String type = state.valueMap.get(USSD_BUNDLE_TYPE);
			for (Bundle bundle : bundles)
			{
				// Only for specified Type
				if (type == null || !type.equals(bundle.getType()))
					continue;

				// Get Bundle name in all languages
				Phrase name = new Phrase();
				name.set(Phrase.ENG, bundle.getName());
				for (BundleLanguage bundleLanguage : bundle.getLanguages())
				{
					name.set(bundleLanguage.getLanguage(), bundleLanguage.getName());
				}
				MenuOption option = new MenuOption() //
						.setName(name) //
						.setValue(bundle.getUssdCode());
				options.add(option);
			}
		}
		return options.toArray(new MenuOption[options.size()]);

	}

	@Override
	public boolean menuMayExecute(EntityManager em, Session session) throws RuleCheckException
	{
		// Check Permission
		if (!session.hasPermission(em, Transaction.MAY_SELL_BUNDLES, true))
			return false;

		// Validate the A Agent
		validateAgentState(session.getAgentUser(), false, Agent.STATE_ACTIVE);

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
		final String pinResult = session.offerPIN(em, session, true, values.get(BundleSalesConfig.PIN));
		if (pinResult != null)
			return respondWithError(pinResult);

		// Remove USSD Prompt
		disableUssdConfirmation(em, session, options);

		// Create the request
		SellBundleRequest request = new SellBundleRequest();
		fillHeader(request, session, interaction);
		String recipientMsisdn = values.get(BundleSalesConfig.RECIPIENT_MSISDN);

		// B MSISDN confirmation
		BundleSalesConfig config = getConfig(em, session);
		if (config.getEnableBNumberConfirmation()) {
			if (!recipientMsisdn.equals(values.get(RECIPIENT_MSISDN_CONFIRMED))) {
				TransactionsConfig transactionsConfig = session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class);
				transactionsConfig.getErrorMessages().put(ERR_MSISDNS_MISMATCH, config.getNumberErrorMessage());
				return respondWithError(ERR_MSISDNS_MISMATCH);
			}
		}

		request.setTargetMSISDN(recipientMsisdn);

		// Get the Bundle from USSD
		String ussdCode = values.get(BundleSalesConfig.USSD_CODE);
		int companyID = session.getCompanyID();
		Bundle bundle = Bundle.findByUssdCode(em, ussdCode, companyID);
		if (bundle == null)
			return respondWithError(TransactionsConfig.ERR_INVALID_BUNDLE);
		else
			request.setBundleID(bundle.getId());

		// Execute the Transaction
		SellBundleResponse response = super.execute(em, request, interaction.getOriginTimeStamp());
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private String nameFromBundle(Locale locale, Bundle bundle)
	{
		String type = bundle.getType();
		String name = bundle.getName();
		String languageCode = locale.getLanguage();
		for (BundleLanguage language : bundle.getLanguages())
		{
			if (languageCode.equalsIgnoreCase(language.getLanguage()))
			{
				type = language.getType();
				name = language.getName();
				break;
			}
		}

		return String.format("%s %s", type, name);
	}

}

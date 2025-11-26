package hxc.services.ecds.rest.non_airtime;

import static hxc.ecds.protocol.rest.Transaction.INBOUND_TRANSACTION_MAX_LENGTH;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.AMOUNT;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.CONSUMER_MSISDN;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.ITEM_DESCRIPTION;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.SENDER_NEW_BALANCE;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.SENDER_OLD_BALANCE;
import static hxc.ecds.protocol.rest.config.ReversalsConfig.SENDER_MSISDN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_INVALID_PIN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_PIN_LOCKOUT;
import static hxc.services.ecds.CreditDistribution.fetchImsi;
import static hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails.MAX_CONSUMER_MSISDN_LENGTH;
import static hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails.MAX_ITEM_DESCRIPTION_LENGTH;
import static hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails.findByUserAndClientTransactionId;
import static hxc.services.ecds.rest.Agents.checkForImsiChangeAndUpdateAgent;
import static hxc.services.ecds.rest.Agents.checkImsiLockout;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.isEmptyString;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionABefore;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static hxc.services.ecds.util.StringUtil.isNullOrBlank;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.non_airtime.DebitRequest;
import hxc.ecds.protocol.rest.non_airtime.Request;
import hxc.ecds.protocol.rest.non_airtime.Response;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;
import hxc.services.ecds.rest.Transactions;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;

public class DebitService extends Transactions<DebitRequest, Response> {
    final static Logger logger = LoggerFactory.getLogger(DebitService.class);

    public DebitService(ICreditDistribution context) {
        this.context = context;
    }

    @Override
    protected String getType() {
        return Transaction.TYPE_NON_AIRTIME_DEBIT;
    }

    @Override
    protected void doPrevalidationActions(EntityManager em, TransactionState<DebitRequest, Response> state) throws RuleCheckException {
        Transaction transaction = state.getTransaction();
        Session session = state.getSession();
        DebitRequest debitRequest = state.getRequest();

        Agent aAgent = Agent.findByMSISDN(em, context.toMSISDN(debitRequest.getMsisdn()), session.getCompanyID());
        if (aAgent == null) {
            throw new RuleCheckException(TransactionsConfig.ERR_ACC_NOT_FOUND, "msisdn", "%s is not a valid Agent msisdn", debitRequest.getMsisdn());
        }

        state.setAgentA(aAgent);
        transaction.setB_MSISDN(context.toMSISDN(debitRequest.getConsumerMsisdn()));
        Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
        transaction.setB_TierID(subscriberTier.getId());

        TransferRule transferRule = state.findTransferRule(em,
                                                           debitRequest.getAmount(),
                                                           transaction.getStartTime(),
                                                           aAgent,
                                                           aAgent,
                                                           null,
                                                           subscriberTier.getId());

        transaction.setTransferRuleID(transferRule.getId());
        transaction.setTransferRule(transferRule);
        transaction.setChannelType(debitRequest.getOriginChannel());
    }

    @Override
    protected void validate(EntityManager em, TransactionState<DebitRequest, Response> state) throws RuleCheckException {
        DebitRequest debitRequest = state.getRequest();
        Session session = state.getSession();

        if (isEmptyString(debitRequest.getAgentPin())) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "agentPin", "agentPin cannot be empty.");
        }

        if (debitRequest.getAmount() == null || debitRequest.getAmount().compareTo(ZERO) <= 0) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "amount", "amount must be positive number.");
        }

        if (!isEmptyString(debitRequest.getConsumerMsisdn()) && debitRequest.getConsumerMsisdn().length() > MAX_CONSUMER_MSISDN_LENGTH) {
            state.getTransaction().setB_MSISDN(null);
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "consumerMsisdn",
                                         "Maximum consumerMsisdn length is " + MAX_CONSUMER_MSISDN_LENGTH + ".");
        }

        if (!isEmptyString(debitRequest.getItemDescription()) && debitRequest.getItemDescription().length() > MAX_ITEM_DESCRIPTION_LENGTH) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "itemDescription",
                                         "Maximum itemDescription length is " + MAX_ITEM_DESCRIPTION_LENGTH + ".");
        }

        if (isEmptyString(debitRequest.getClientTransactionId())) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "clientTransactionId", "clientTransactionId cannot be empty.");
        }

        if (debitRequest.getClientTransactionId().length() > INBOUND_TRANSACTION_MAX_LENGTH) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "clientTransactionId",
                                         "Maximum clientTransactionId length is " + INBOUND_TRANSACTION_MAX_LENGTH + ".");
        }
        
        // IMSI 1/2
        checkImsiLockout(session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class), state.getAgentA());

        if (!findByUserAndClientTransactionId(em, session.getWebUserID(), debitRequest.getClientTransactionId()).isEmpty()) {
            throw new RuleCheckException(TransactionsConfig.ERR_INVALID_VALUE, "clientTransactionId",
                                         "clientTransactionId %s already used.", debitRequest.getClientTransactionId());
        }

        // Verify agentA PIN
        String pinCheckErrorMessage = state.getAgentA().offerPIN(em, session, session.getCompanyInfo(), debitRequest.getAgentPin());
        if (pinCheckErrorMessage != null) {
            String message = pinCheckErrorMessage;
            if (pinCheckErrorMessage.equals(ERR_INVALID_PIN)) {
                message = "agentPin is invalid.";
            } else if (pinCheckErrorMessage.equals(ERR_PIN_LOCKOUT)) {
                message = "agentPin lockout.";
            }
            throw new RuleCheckException(pinCheckErrorMessage, "agentPin", message);
        }

        validateAgentState(state.getAgentA(), false, Agent.STATE_ACTIVE);

        // Cannot sell to self
        String bMSISDN = context.toMSISDN(debitRequest.getConsumerMsisdn());
        if (state.getAgentA().getMobileNumber().equals(bMSISDN)) {
            throw new RuleCheckException(TransactionsConfig.ERR_NOT_SELF, "consumerMsisdn", "Cannot Sell to Self");
        }

        // IMSI 2/2
        String imsi = debitRequest.getImsi();
        if (isNullOrBlank(imsi)) {
            imsi = fetchImsi(state.getAgentA().getMobileNumber(), context);
            if (isNullOrBlank(imsi)) {
                imsi = state.getAgentA().getImsi(); // Use old IMSI
            }
        }
        Agent managedEntity = checkForImsiChangeAndUpdateAgent(em, state.getConfig(em, TransactionsConfig.class), session, state.getAgentA(), imsi);
        state.setAgentA(managedEntity);
    }

    @Override
    protected void execute(EntityManager em, TransactionState<DebitRequest, Response> state) throws RuleCheckException {
        final Transaction transaction = state.getTransaction();
        DebitRequest debitRequest = state.getRequest();
        BigDecimal amount = debitRequest.getAmount();
        BigDecimal grossSalesAmount = state.getRequest().getGrossSalesAmount();
        final Account aAccount;

        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            aAccount = findAccount(em, state.getAgentA().getId());

            state.setBeforeA(aAccount);
            setTransactionABefore(transaction, aAccount);

            aAccount.transact(transaction.getStartTime(), amount, ZERO, ZERO, false);

            transaction.setBuyerTradeBonusAmount(ZERO);
            transaction.setBuyerTradeBonusProvision(ZERO);
            transaction.setBuyerTradeBonusPercentage(ZERO);
            transaction.setAmount(amount);
            transaction.setGrossSalesAmount(grossSalesAmount);
			
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

            transaction.setB_MSISDN(context.toMSISDN(debitRequest.getConsumerMsisdn()));

            transaction.testAmlLimitsA(aAccount, amount);
            populateCellGroupsIds(transaction);
            transaction.setInboundTransactionID(debitRequest.getClientTransactionId());

            state.setAfterA(aAccount);
            setTransactionAAfter(transaction, aAccount);

            transaction.persist(em, null, state.getSession(), null);
            em.persist(createTransactionDetails(state, transaction));

            Double longitude = state.getRequest().getLongitude();
			Double latitude = state.getRequest().getLatitude();
			long transactionId = transaction.getId();

			if (longitude != null && latitude != null) {
				transaction.persistTransactionLocation(em, longitude, latitude, transactionId);
			}

            updateInDb(em, trans, transaction, aAccount);
        }

        populateResponse(state.getResponse(), transaction);
    }

    public static void populateResponse(Response response, Transaction transaction) {
        response.setStatus(response.getReturnCode());
        response.setCrediverseTransactionId(transaction.getNumber());
        response.setTransactionEndTimestamp(transaction.getLastTime().getTime());
    }

    public static NonAirtimeTransactionDetails createTransactionDetails(TransactionState<? extends Request, Response> state,
                                                                        Transaction transaction) {
        Request request = state.getRequest();
        NonAirtimeTransactionDetails transactionDetails = new NonAirtimeTransactionDetails();
        transactionDetails.setId(transaction.getId());
        transactionDetails.setClientTransactionId(request.getClientTransactionId());
        transactionDetails.setItemDescription(request.getItemDescription());
        transactionDetails.setServiceUserId(state.getSession().getWebUserID());
        return transactionDetails;
    }

    public static void populateCellGroupsIds(Transaction transaction) {
        Cell cellA = transaction.getA_Cell();
        Cell cellB = transaction.getB_Cell();
        Integer cellGroupIdA = cellA == null || cellA.getCellGroups().size() < 1 ? null : cellA.getCellGroups().get(0).getId();
        Integer cellGroupIdB = cellB == null || cellB.getCellGroups().size() < 1 ? null : cellB.getCellGroups().get(0).getId();
        transaction.setA_CellGroupID(cellGroupIdA);
        transaction.setB_CellGroupID(cellGroupIdB);
    }

    @Override
    protected void conclude(EntityManager em, TransactionState<DebitRequest, Response> state) {
        Transaction transaction = state.getTransaction();
        Agent aAgent = transaction.getA_Agent();
        BundleSalesConfig bundleSalesConfig = state.getConfig(em, BundleSalesConfig.class);

        if (bundleSalesConfig.getEnableDebitNotification()) {
            sendNotification(aAgent.getMobileNumber(), bundleSalesConfig.getSenderDebitNotification(), bundleSalesConfig.listNotificationFields(),
                             state.getLocale(aAgent.getLanguage()), state);
        }

        if (bundleSalesConfig.getEnableDebitBalanceNotification()) {
            sendNotification(aAgent.getMobileNumber(), bundleSalesConfig.getSenderDebitBalanceNotification(), bundleSalesConfig.listNotificationFields(),
                             state.getLocale(aAgent.getLanguage()), state);
        }

        sendStockDepletionMessage(em, state);
    }

    private void sendStockDepletionMessage(EntityManager em, TransactionState<DebitRequest, Response> state) {
        Transaction transaction = state.getTransaction();
        Agent aAgent = transaction.getA_Agent();
        BigDecimal threshold = aAgent.getWarningThreshold();
        if (moreThan(transaction.getA_BalanceBefore(), threshold)
                && !moreThan(transaction.getA_BalanceAfter(), threshold)) {
            AgentsConfig agentConfig = state.getConfig(em, AgentsConfig.class);
            sendNotification(aAgent.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(),
                             state.getLocale(aAgent.getLanguage()), state);
            Agent supplier = aAgent.getSupplier();
            if (supplier != null) {
                sendNotification(supplier.getMobileNumber(), agentConfig.getDepletionNotification(), agentConfig.listDepletionFields(),
                                 state.getLocale(supplier.getLanguage()), state);
            }
        }
    }

    @Override
    protected void concludeAfterFailure(EntityManager em, TransactionState<DebitRequest, Response> state) {
        // Empty body
    }

    @Override
    public String expandField(String englishName, Locale locale, TransactionState<DebitRequest, Response> state) {
        Transaction transaction = state.getTransaction();
        switch (englishName) {
            case SENDER_MSISDN:
                return transaction.getA_MSISDN();
            case SENDER_NEW_BALANCE:
                return format(locale, transaction.getA_BalanceAfter());
            case SENDER_OLD_BALANCE:
                return format(locale, transaction.getA_BalanceBefore());
            case AMOUNT:
                return format(locale, transaction.getAmount());
            case ITEM_DESCRIPTION:
                return state.getRequest().getItemDescription();
            case CONSUMER_MSISDN:
                return transaction.getB_MSISDN();
            default:
                return super.expandField(englishName, locale, state);
        }
    }

    public static boolean moreThan(BigDecimal left, BigDecimal right) {
        return left != null && right != null && left.compareTo(right) > 0;
    }
}

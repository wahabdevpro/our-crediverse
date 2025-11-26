package hxc.services.ecds.rest.non_airtime;

import static hxc.ecds.protocol.rest.Transaction.INBOUND_TRANSACTION_MAX_LENGTH;
import static hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.CONSUMER_MSISDN;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.ITEM_DESCRIPTION;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.SENDER_NEW_BALANCE;
import static hxc.ecds.protocol.rest.config.BundleSalesConfig.SENDER_OLD_BALANCE;
import static hxc.ecds.protocol.rest.config.ReversalsConfig.SENDER_MSISDN;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.*;
import static hxc.services.ecds.CreditDistribution.fetchImsi;
import static hxc.services.ecds.model.Transaction.findByNumber;
import static hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails.*;
import static hxc.services.ecds.rest.Agents.checkForImsiChangeAndUpdateAgent;
import static hxc.services.ecds.rest.Agents.checkImsiLockout;
import static hxc.services.ecds.rest.TransactionHelper.findAccount;
import static hxc.services.ecds.rest.TransactionHelper.isEmptyString;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionAAfter;
import static hxc.services.ecds.rest.TransactionHelper.setTransactionABefore;
import static hxc.services.ecds.rest.TransactionHelper.updateInDb;
import static hxc.services.ecds.rest.non_airtime.DebitService.createTransactionDetails;
import static hxc.services.ecds.rest.non_airtime.DebitService.moreThan;
import static hxc.services.ecds.rest.non_airtime.DebitService.populateResponse;
import static hxc.services.ecds.util.StringUtil.isNullOrBlank;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.non_airtime.RefundRequest;
import hxc.ecds.protocol.rest.non_airtime.Response;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;
import hxc.services.ecds.rest.Transactions;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;

public class RefundService extends Transactions<RefundRequest, Response> {
    final static Logger logger = LoggerFactory.getLogger(RefundService.class);

    public RefundService(ICreditDistribution context) {
        this.context = context;
    }

    @Override
    protected String getType() {
        return Transaction.TYPE_NON_AIRTIME_REFUND;
    }

    @Override
    protected void doPrevalidationActions(EntityManager em, TransactionState<RefundRequest, Response> state) throws RuleCheckException {
        Session session = state.getSession();
        RefundRequest refundRequest = state.getRequest();

        Agent aAgent = Agent.findByMSISDN(em, context.toMSISDN(refundRequest.getMsisdn()), state.getSession().getCompanyID());
        if (aAgent == null) {
            throw new RuleCheckException(ERR_ACC_NOT_FOUND, "msisdn", "%s is not a valid Agent msisdn", refundRequest.getMsisdn());
        }

        if (isEmptyString(refundRequest.getDebitCrediverseTransactionId()) && isEmptyString(refundRequest.getDebitClientTransactionId())) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "debitCrediverseTransactionId",
                                         "At least one of debitCrediverseTransactionId and debitClientTransactionId has to be provided.");
        }

        Transaction originalTransaction;
        if (!isNullOrBlank(refundRequest.getDebitCrediverseTransactionId())) {
            originalTransaction = findByNumber(em, refundRequest.getDebitCrediverseTransactionId(), state.getSession().getCompanyID());
            if (originalTransaction == null) {
                throw new RuleCheckException(ERR_TRANSACTION_NOT_FOUND, "debitCrediverseTransactionId",
                                             "There is no debit transaction with ID: %s", refundRequest.getDebitCrediverseTransactionId());
            }
        } else {
            originalTransaction = findTransactionByClientTransactionId(em, session.getWebUserID(), refundRequest.getDebitClientTransactionId());
            if (originalTransaction == null) {
                throw new RuleCheckException(ERR_TRANSACTION_NOT_FOUND, "debitClientTransactionId",
                                             "There is no debit transaction with clientTransactionId: %s",
                                             refundRequest.getDebitClientTransactionId());
            }
        }

        Transaction existingRefundTransaction = Transaction.findByReversedID(em, originalTransaction.getId(), session.getCompanyID());
        if (existingRefundTransaction != null) {
            String transactionId = refundRequest.getDebitCrediverseTransactionId();
            if (transactionId == null) { transactionId = refundRequest.getDebitClientTransactionId(); }
            throw new RuleCheckException(ERR_INVALID_VALUE, "debitCrediverseTransactionId", "%s has already been refunded", transactionId);
        }
        Transaction transaction = state.getTransaction();
        transaction.setReversedID(originalTransaction.getId());
        transaction.setOriginalTransaction(originalTransaction);

        state.setAgentA(aAgent);
        transaction.setB_MSISDN(context.toMSISDN(refundRequest.getConsumerMsisdn()));

        Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
        transaction.setB_TierID(subscriberTier.getId());
        transaction.setB_Tier(subscriberTier);
        transaction.setChannelType(refundRequest.getOriginChannel());
    }

    @Override
    protected void validate(EntityManager em, TransactionState<RefundRequest, Response> state) throws RuleCheckException {
        RefundRequest refundRequest = state.getRequest();
        Session session = state.getSession();

        if (refundRequest.getAmount() == null || refundRequest.getAmount().compareTo(ZERO) <= 0) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "amount", "amount must be positive number.");
        }

        if (!isEmptyString(refundRequest.getConsumerMsisdn()) && refundRequest.getConsumerMsisdn().length() > MAX_CONSUMER_MSISDN_LENGTH) {
            state.getTransaction().setB_MSISDN(null);
            throw new RuleCheckException(ERR_INVALID_VALUE, "consumerMsisdn",
                                         "Maximum consumerMsisdn length is " + MAX_CONSUMER_MSISDN_LENGTH + ".");
        }

        if (!isEmptyString(refundRequest.getItemDescription()) && refundRequest.getItemDescription().length() > MAX_ITEM_DESCRIPTION_LENGTH) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "itemDescription",
                                         "Maximum itemDescription length is " + MAX_ITEM_DESCRIPTION_LENGTH + ".");
        }

        if (isEmptyString(refundRequest.getClientTransactionId())) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "clientTransactionId", "clientTransactionId cannot be empty.");
        }

        if (refundRequest.getClientTransactionId().length() > INBOUND_TRANSACTION_MAX_LENGTH) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "clientTransactionId",
                                         "Maximum clientTransactionId length is " + INBOUND_TRANSACTION_MAX_LENGTH + ".");
        }
        
        // IMSI 1/2
        checkImsiLockout(session.getCompanyInfo().getConfiguration(em, TransactionsConfig.class), state.getAgentA());

        if (!findByUserAndClientTransactionId(em, session.getWebUserID(), refundRequest.getClientTransactionId()).isEmpty()) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "clientTransactionId",
                                         "clientTransactionId %s already used.", refundRequest.getClientTransactionId());
        }

        Transaction originalTransaction = state.getTransaction().getOriginalTransaction();

        if (!TYPE_NON_AIRTIME_DEBIT.equals(originalTransaction.getType())) {
            throw new RuleCheckException(ERR_INVALID_TRANSACTION_TYPE, "debitCrediverseTransactionId",
                                         "%s is not a debit transaction", refundRequest.getDebitCrediverseTransactionId());
        }

        if (!originalTransaction.getA_MSISDN().equals(context.toMSISDN(refundRequest.getMsisdn()))) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "msisdn",
                                         "%s doesn't match Agent msisdn from the debit transaction", refundRequest.getMsisdn());
        }

        String consumerMsisdn = refundRequest.getConsumerMsisdn();
        if (!isEmptyString(consumerMsisdn) && !context.toMSISDN(consumerMsisdn).equals(originalTransaction.getB_MSISDN())) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "consumerMsisdn",
                                         "%s doesn't match the consumer MSISDN from the debit transaction", consumerMsisdn);
        }

        if (originalTransaction.getAmount().compareTo(refundRequest.getAmount()) != 0) {
            throw new RuleCheckException(ERR_INVALID_VALUE, "amount",
                                         "%s doesn't match the amount from the debit transaction", refundRequest.getAmount());
        }

        validateAgentState(state.getAgentA(), false, Agent.STATE_ACTIVE);

        // IMSI 2/2
        String imsi = refundRequest.getImsi();
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
    protected void execute(EntityManager em, TransactionState<RefundRequest, Response> state) throws RuleCheckException {
        final Transaction transaction = state.getTransaction();
        final Transaction original = transaction.getOriginalTransaction();
        RefundRequest refundRequest = state.getRequest();
        BigDecimal amount = original.getAmount();
        final Account aAccount;

        try (RequiresTransaction trans = new RequiresTransaction(em)) {
            aAccount = findAccount(em, state.getAgentA().getId());

            state.setBeforeA(aAccount);
            setTransactionABefore(transaction, aAccount);

            aAccount.reverse(original.getStartTime(), amount, BigDecimal.ZERO, BigDecimal.ZERO, true);

            transaction.setBuyerTradeBonusAmount(ZERO);
            transaction.setBuyerTradeBonusProvision(ZERO);
            transaction.setBuyerTradeBonusPercentage(ZERO);
            transaction.setAmount(amount);
            transaction.setGrossSalesAmount(original.getGrossSalesAmount());
            transaction.setCostOfGoodsSold(original.getCostOfGoodsSold());
            transaction.setB_MSISDN(original.getB_MSISDN());

            transaction.setInboundTransactionID(refundRequest.getClientTransactionId());

            state.setAfterA(aAccount);
            setTransactionAAfter(transaction, aAccount);

            transaction.persist(em, null, state.getSession(), null);
            em.persist(createTransactionDetails(state, transaction));
            updateInDb(em, trans, transaction, aAccount);
        }

        populateResponse(state.getResponse(), transaction);
    }

    @Override
    protected void conclude(EntityManager em, TransactionState<RefundRequest, Response> state) {
        Transaction transaction = state.getTransaction();
        Agent aAgent = transaction.getA_Agent();
        BundleSalesConfig bundleSalesConfig = state.getConfig(em, BundleSalesConfig.class);

        if (bundleSalesConfig.getEnableRefundNotification()) {
            sendNotification(aAgent.getMobileNumber(), bundleSalesConfig.getSenderRefundNotification(), bundleSalesConfig.listNotificationFields(),
                             state.getLocale(aAgent.getLanguage()), state);
        }

        if (bundleSalesConfig.getEnableRefundBalanceNotification()) {
            sendNotification(aAgent.getMobileNumber(), bundleSalesConfig.getSenderRefundBalanceNotification(), bundleSalesConfig.listNotificationFields(),
                             state.getLocale(aAgent.getLanguage()), state);
        }

        sendStockDepletionMessage(em, state);
    }

    private void sendStockDepletionMessage(EntityManager em, TransactionState<RefundRequest, Response> state) {
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
    public String expandField(String englishName, Locale locale, TransactionState<RefundRequest, Response> state) {
        Transaction transaction = state.getTransaction();
        switch (englishName) {
            case SENDER_MSISDN:
                return transaction.getA_MSISDN();
            case SENDER_NEW_BALANCE:
                return format(locale, transaction.getA_BalanceAfter());
            case SENDER_OLD_BALANCE:
                return format(locale, transaction.getA_BalanceBefore());
            case TransactionsConfig.AMOUNT:
                return format(locale, transaction.getAmount());
            case ITEM_DESCRIPTION:
                return state.getRequest().getItemDescription();
            case CONSUMER_MSISDN:
                return transaction.getB_MSISDN();
            default:
                return super.expandField(englishName, locale, state);
        }
    }

    @Override
    protected void concludeAfterFailure(EntityManager em, TransactionState<RefundRequest, Response> state) {
        // Empty body
    }
}

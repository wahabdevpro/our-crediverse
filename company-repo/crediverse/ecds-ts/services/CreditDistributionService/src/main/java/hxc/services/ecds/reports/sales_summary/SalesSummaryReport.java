package hxc.services.ecds.reports.sales_summary;

import static hxc.ecds.protocol.rest.Transaction.Type.NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.Type.NON_AIRTIME_REFUND;
import static hxc.ecds.protocol.rest.Transaction.Type.SELF_TOPUP;
import static hxc.ecds.protocol.rest.Transaction.Type.SELL;
import static hxc.ecds.protocol.rest.Transaction.Type.SELL_BUNDLE;
import static hxc.ecds.protocol.rest.Transaction.Type.TRANSFER;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import hxc.ecds.protocol.rest.Transaction.Type.Code;
import hxc.ecds.protocol.rest.reports.Report;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportParameters;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportResultEntry;
import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;
import static hxc.ecds.protocol.rest.util.ChannelTypes.MM;
import hxc.services.ecds.olapmodel.OlapTransaction;
import hxc.services.ecds.Session;

import hxc.services.ecds.config.RestServerConfiguration;

public class SalesSummaryReport
        extends hxc.ecds.protocol.rest.reports.SalesSummaryReport {
    private EntityManager em;
    private TimeInterval timeInterval;
    private Report.RelativeTimeRange relativeTimeRange;

    private static final Set<String> AIRTIME_TRANSACTION_CODES;
    private static final Set<String> NON_AIRTIME_TRANSACTION_CODES;

    static {
        AIRTIME_TRANSACTION_CODES = new HashSet<>();
        AIRTIME_TRANSACTION_CODES.add(SELL.getCode());
        AIRTIME_TRANSACTION_CODES.add(SELF_TOPUP.getCode());
        AIRTIME_TRANSACTION_CODES.add(SELL_BUNDLE.getCode());

        NON_AIRTIME_TRANSACTION_CODES = new HashSet<>();
        NON_AIRTIME_TRANSACTION_CODES.add(NON_AIRTIME_DEBIT.getCode());
        NON_AIRTIME_TRANSACTION_CODES.add(NON_AIRTIME_REFUND.getCode());

    }

    public EntityManager getEm() {
        return this.em;
    }

    public TimeInterval getTimeInterval() {
        return this.timeInterval;
    }

    public hxc.services.ecds.reports.Report.RelativeTimeRange getRelativeTimeRange() {
        return relativeTimeRange;
    }

    public String describe(String extra) {
        return String.format("%s@%s(timeInterval = %s, relativeTimeRange = %s%s%s)",
                             this.getClass().getName(), Integer.toHexString(this.hashCode()),
                             this.timeInterval, this.relativeTimeRange,
                             (extra.isEmpty() ? "" : ", "), extra);
    }

    public String describe() {
        return this.describe("");
    }

    public String toString() {
        return this.describe();
    }

    public SalesSummaryReport(EntityManager em, SalesSummaryReportParameters parameters, Date relativeTimeRangeReference) throws Exception {
        this.em = em;
        this.setParameters(parameters, relativeTimeRangeReference);
    }

    public void setParameters(SalesSummaryReportParameters parameters, Date relativeTimeRangeReference) throws Exception {
        this.relativeTimeRange = hxc.services.ecds.reports.Report.RelativeTimeRange.PREVIOUS_HOUR;
        TimeInterval timeInterval = this.relativeTimeRange.resolve(relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
        timeInterval.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
        this.timeInterval = timeInterval;
    }

    private List<Predicate> createDateTimePredicates(CriteriaBuilder cb, TimeInterval timeInterval, Root<OlapTransaction> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(
                root.get("endDate"),
                cb.function("date", Date.class, cb.literal(timeInterval.getStartDate()))
        ));
        if (timeInterval.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("endTime"),
                    cb.function("time", Date.class, cb.literal(timeInterval.getEndDate()))
            ));
        }
        return predicates;
    }

    public List<SalesSummaryReportResultEntry> entries(int first, int max, int companyId) throws Exception {
        if (first == 0 && max == 0) {
            return new ArrayList<>();
        }

        Long airtimeAgentCount = countAirtimeAgents(em, timeInterval, companyId);
        Long nonAirtimeAgentCount = countNonAirtimeAgents(em, timeInterval, companyId);
        Long agentCount = countAllAgents(em, timeInterval, companyId);

        List<StatusTransactionCount> statusAirtimeTrnCountList = countAirtimeTrns(em, timeInterval, companyId);
        long successfulAirtimeTransactionCount = statusAirtimeTrnCountList.stream().
                filter(e -> e.status).findFirst().orElseGet(StatusTransactionCount::new).count;
        long failedAirtimeTransactionCount = statusAirtimeTrnCountList.stream().
                filter(e -> !e.status).findFirst().orElseGet(StatusTransactionCount::new).count;

        List<StatusTransactionCount> statusNonAirtimeTrnCountList = countNonAirtimeTrns(em, timeInterval, companyId);
        long successfulNonAirtimeTransactionCount = statusNonAirtimeTrnCountList.stream().
                filter(e -> e.status).findFirst().orElseGet(StatusTransactionCount::new).count;
        long failedNonAirtimeTransactionCount = statusNonAirtimeTrnCountList.stream().
                filter(e -> !e.status).findFirst().orElseGet(StatusTransactionCount::new).count;

        long successfulTransactionCount = successfulAirtimeTransactionCount + successfulNonAirtimeTransactionCount;

        List<PropertyAmountTuple> totalAmountsPerType = calculateTotalAmounts(em, timeInterval, companyId);

        BigDecimal totalAirtimeAmount = totalAmountsPerType.stream()
                .filter(e -> AIRTIME_TRANSACTION_CODES.contains(e.property))
                .map(e -> e.amount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalAmountNonAirtime = totalAmountsPerType.stream()
                .filter(e -> NON_AIRTIME_DEBIT.getCode().equals(e.property))
                .findFirst()
                .orElseGet(PropertyAmountTuple::new).amount;

        BigDecimal totalAmount = totalAirtimeAmount.add(totalAmountNonAirtime);

        // Averages
        BigDecimal averageAirtimeAmountPerAgent = ZERO;
        if (airtimeAgentCount != 0) {
            averageAirtimeAmountPerAgent = totalAirtimeAmount.divide(new BigDecimal(airtimeAgentCount), RoundingMode.UP);
        }

        BigDecimal averageNonAirtimeAmountPerAgent = ZERO;
        if (nonAirtimeAgentCount != 0) {
            averageNonAirtimeAmountPerAgent = totalAmountNonAirtime.divide(new BigDecimal(nonAirtimeAgentCount), RoundingMode.UP);
        }

        BigDecimal averageAmountPerAgent = ZERO;
        if (agentCount != 0) {
            averageAmountPerAgent = totalAmount.divide(new BigDecimal(agentCount), RoundingMode.UP);
        }

        BigDecimal averageAirtimeAmountPerTransaction = ZERO;
        if (successfulAirtimeTransactionCount != 0) {
            averageAirtimeAmountPerTransaction = totalAirtimeAmount.divide(new BigDecimal(successfulAirtimeTransactionCount), RoundingMode.UP);
        }

        BigDecimal averageNonAirtimeAmountPerTransaction = ZERO;
        if (successfulNonAirtimeTransactionCount != 0) {
            averageNonAirtimeAmountPerTransaction =
                    totalAmountNonAirtime.divide(new BigDecimal(successfulNonAirtimeTransactionCount), RoundingMode.UP);
        }

        BigDecimal averageAmountPerTransaction = ZERO;
        if (successfulTransactionCount != 0) {
            averageAmountPerTransaction = totalAmount.divide(new BigDecimal(successfulTransactionCount), RoundingMode.UP);
        }

        SalesSummaryReportResultEntry report = new SalesSummaryReportResultEntry();
        report.setDate(timeInterval.getStartDate());

        report.setTotalAirtimeAmount(totalAirtimeAmount);
        report.setTotalAmountNonAirtime(totalAmountNonAirtime);
        report.setTotalAmount(totalAmount);

        report.setSuccessfulAirtimeTransactionCount((int) successfulAirtimeTransactionCount);
        report.setSuccessfulNonAirtimeTransactionCount((int) successfulNonAirtimeTransactionCount);
        report.setSuccessfulTransactionCount((int) successfulTransactionCount);

        report.setFailedAirtimeTransactionCount((int) failedAirtimeTransactionCount);
        report.setFailedNonAirtimeTransactionCount((int) failedNonAirtimeTransactionCount);
        report.setFailedTransactionCount((int) (failedAirtimeTransactionCount + failedNonAirtimeTransactionCount));

        report.setAgentCountAirtime(airtimeAgentCount.intValue());
        report.setAgentCountNonAirtime(nonAirtimeAgentCount.intValue());
        report.setAgentCount(agentCount.intValue());

        report.setAverageAirtimeAmountPerAgent(averageAirtimeAmountPerAgent);
        report.setAverageNonAirtimeAmountPerAgent(averageNonAirtimeAmountPerAgent);
        report.setAverageAmountPerAgent(averageAmountPerAgent);
        report.setAverageAirtimeAmountPerTransaction(averageAirtimeAmountPerTransaction);
        report.setAverageNonAirtimeAmountPerTransaction(averageNonAirtimeAmountPerTransaction);
        report.setAverageAmountPerTransaction(averageAmountPerTransaction);

        // Mobile Money
        if(RestServerConfiguration.getInstance().isEnabledMobileMoney())
        {
            List<StatusTransactionCount> statusMobileMoneyTrnCountList = countMobileMoneyTrns(em, timeInterval, companyId);
            long successfulMobileMoneyTransactionCount = statusMobileMoneyTrnCountList.stream().
                    filter(e -> e.status).findFirst().orElseGet(StatusTransactionCount::new).count;
            long failedMobileMoneyTransactionCount = statusMobileMoneyTrnCountList.stream().
                    filter(e -> !e.status).findFirst().orElseGet(StatusTransactionCount::new).count;

            BigDecimal totalMobileMoneyTransferAmount = calculateTotalMobileMoneyTransferAmount(em, timeInterval, companyId);

            long agentsReceivingMobileMoneyCount = countMobileMoneyReceivingAgentsCount(em, timeInterval, companyId);

            BigDecimal averageMMTransferAmountPerAgent = ZERO;
            if (agentsReceivingMobileMoneyCount != 0) {
                averageMMTransferAmountPerAgent = totalMobileMoneyTransferAmount.divide(new BigDecimal(agentsReceivingMobileMoneyCount), RoundingMode.UP);
            }

            BigDecimal averageMMTransferAmountPerTransaction = ZERO;
            if (successfulMobileMoneyTransactionCount != 0) {
                averageMMTransferAmountPerTransaction = totalMobileMoneyTransferAmount.divide(new BigDecimal(successfulMobileMoneyTransactionCount), RoundingMode.UP);
            }

            report.setMMTotalTransferAmount(totalMobileMoneyTransferAmount);
            report.setMMTransferCountSuccess((int)successfulMobileMoneyTransactionCount);
            report.setMMTransferCountFailed((int)failedMobileMoneyTransactionCount);
            report.setMMReceivingAgentCount((int)agentsReceivingMobileMoneyCount);
            report.setMMAverageTransferAmountPerAgent(averageMMTransferAmountPerAgent);
            report.setMMAverageTransferAmountPerTransaction(averageMMTransferAmountPerTransaction);
        }
        return Collections.singletonList(report);
    }

    private List<StatusTransactionCount> countAirtimeTrns(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StatusTransactionCount> cq = cb.createQuery(StatusTransactionCount.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.in(root.get("type")).value(AIRTIME_TRANSACTION_CODES));

        cq.multiselect(root.get("success"), cb.countDistinct(root.get("id"))).
                where(predicates.toArray(new Predicate[0])).groupBy(root.get("success"));

        return em.createQuery(cq).getResultList();
    }

    private List<StatusTransactionCount> countMobileMoneyTrns(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StatusTransactionCount> cq = cb.createQuery(StatusTransactionCount.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.equal(root.get("type"), Code.TRANSFER));
        predicates.add(cb.equal(root.get("channel"), Session.CHANNEL_3PP));
        predicates.add(cb.equal(root.get("channelType"), MM.name()));

        cq.multiselect(root.get("success"), cb.countDistinct(root.get("id"))).
                where(predicates.toArray(new Predicate[0])).groupBy(root.get("success"));

        return em.createQuery(cq).getResultList();
    }

    private List<StatusTransactionCount> countNonAirtimeTrns(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PropertyCountTuple> cq = cb.createQuery(PropertyCountTuple.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.in(root.get("type")).value(NON_AIRTIME_TRANSACTION_CODES));

        cq.multiselect(cb.concat(root.get("success"), root.get("type")), cb.countDistinct(root.get("id"))).
                where(predicates.toArray(new Predicate[0])).groupBy(root.get("success"), root.get("type"));

        List<PropertyCountTuple> resultList = em.createQuery(cq).getResultList();

        long successfulNonAirtimeDebit = resultList.stream().
                filter(e -> ("1" + Code.NON_AIRTIME_DEBIT).equals(e.property)).findFirst().orElseGet(PropertyCountTuple::new).count;

        long notSuccessfulNonAirtimeDebit = resultList.stream().
                filter(e -> ("0" + Code.NON_AIRTIME_DEBIT).equals(e.property)).findFirst().orElseGet(PropertyCountTuple::new).count;

        long successfulNonAirtimeRefund = resultList.stream().
                filter(e -> ("1" + Code.NON_AIRTIME_REFUND).equals(e.property)).findFirst().orElseGet(PropertyCountTuple::new).count;

        List<StatusTransactionCount> statusTransactionCountsList = new ArrayList<>();
        statusTransactionCountsList.add(new StatusTransactionCount(false, notSuccessfulNonAirtimeDebit + successfulNonAirtimeRefund));
        statusTransactionCountsList.add(new StatusTransactionCount(true, successfulNonAirtimeDebit - successfulNonAirtimeRefund));
        return statusTransactionCountsList;
    }

    private List<PropertyAmountTuple> calculateTotalAmounts(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PropertyAmountTuple> cq = cb.createQuery(PropertyAmountTuple.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("success"), true));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        
        Set<String> airtimeAndNonAirtimeDebit = new HashSet<>(AIRTIME_TRANSACTION_CODES);
        airtimeAndNonAirtimeDebit.add(NON_AIRTIME_DEBIT.getCode());
        predicates.add(cb.in(root.get("type")).value(airtimeAndNonAirtimeDebit));

        cq.multiselect(root.get("type"), cb.sum(root.get("amount"))).
                where(predicates.toArray(new Predicate[0])).groupBy(root.get("type"));

        return em.createQuery(cq).getResultList();
    }

    private BigDecimal calculateTotalMobileMoneyTransferAmount(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PropertyAmountTuple> cq = cb.createQuery(PropertyAmountTuple.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("success"), true));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.equal(root.get("type"), Code.TRANSFER));
        predicates.add(cb.equal(root.get("channel"), Session.CHANNEL_3PP));
        predicates.add(cb.equal(root.get("channelType"), MM.name()));

        cq.multiselect(root.get("type"), cb.sum(root.get("amount"))).
                where(predicates.toArray(new Predicate[0])).groupBy(root.get("type"));

        List<PropertyAmountTuple> totalMobileMoneyTransferAmountPerType = em.createQuery(cq).getResultList();
        return totalMobileMoneyTransferAmountPerType.stream()
                    .filter(e -> Code.TRANSFER.equals(e.property))
                    .map(e -> e.amount)
                    .reduce(ZERO, BigDecimal::add);
    }

    private Long countAirtimeAgents(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.or(cb.equal(root.get("type"), Code.SELL), cb.equal(root.get("type"), Code.SELF_TOPUP)));
        cq.select(cb.countDistinct(root.get("a_AgentID"))).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    private Long countNonAirtimeAgents(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.equal(root.get("type"), Code.NON_AIRTIME_DEBIT));
        cq.select(cb.countDistinct(root.get("a_AgentID"))).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    private Long countAllAgents(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.or(cb.equal(root.get("type"), Code.SELL),
                             cb.equal(root.get("type"), Code.SELF_TOPUP),
                             cb.equal(root.get("type"), Code.NON_AIRTIME_DEBIT)));
        cq.select(cb.countDistinct(root.get("a_AgentID"))).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    private Long countMobileMoneyReceivingAgentsCount(EntityManager em, TimeInterval timeInterval, int companyId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OlapTransaction> root = cq.from(OlapTransaction.class);

        List<Predicate> predicates = new ArrayList<>(createDateTimePredicates(cb, timeInterval, root));
        predicates.add(cb.equal(root.get("success"), true));
        predicates.add(cb.equal(root.get("companyID"), companyId));
        predicates.add(cb.equal(root.get("type"), Code.TRANSFER));
        predicates.add(cb.equal(root.get("channel"), Session.CHANNEL_3PP));
        predicates.add(cb.equal(root.get("channelType"), MM.name()));
        cq.select(cb.countDistinct(root.get("b_AgentID"))).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    private static class PropertyCountTuple {
        private String property;
        private long count;

        public PropertyCountTuple() {
        }

        public PropertyCountTuple(String property, long count) {
            this.property = property;
            this.count = count;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    private static class PropertyAmountTuple {
        private String property;
        private BigDecimal amount = ZERO;

        public PropertyAmountTuple() {
        }

        public PropertyAmountTuple(String property, BigDecimal amount) {
            this.property = property;
            this.amount = amount;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    private static class StatusTransactionCount {
        private boolean status;
        private long count;

        public StatusTransactionCount(boolean status, long count) {
            this.status = status;
            this.count = count;
        }

        public StatusTransactionCount() {
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}

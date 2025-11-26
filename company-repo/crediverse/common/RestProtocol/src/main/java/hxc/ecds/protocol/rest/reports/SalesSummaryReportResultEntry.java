package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SalesSummaryReportResultEntry
{
	private Date date;
	private BigDecimal totalAmount;
	private BigDecimal totalAirtimeAmount;
	private BigDecimal totalAmountNonAirtime;
	private Integer agentCount;
	private Integer agentCountAirtime;
	private Integer agentCountNonAirtime;
	private Integer successfulTransactionCount;
	private Integer successfulAirtimeTransactionCount;
	private Integer successfulNonAirtimeTransactionCount;
	private Integer failedTransactionCount;
	private Integer failedAirtimeTransactionCount;
	private Integer failedNonAirtimeTransactionCount;

	private BigDecimal averageAmountPerAgent;
	private BigDecimal averageAirtimeAmountPerAgent;
	private BigDecimal averageNonAirtimeAmountPerAgent;
	private BigDecimal averageAmountPerTransaction;
	private BigDecimal averageAirtimeAmountPerTransaction;
	private BigDecimal averageNonAirtimeAmountPerTransaction;

	private Integer MMTransferCountSuccess;
	private Integer MMTransferCountFailed;
	private BigDecimal MMTotalTransferAmount;
	private Integer MMReceivingAgentCount;
	private BigDecimal MMAverageTransferAmountPerAgent;
	private BigDecimal MMAverageTransferAmountPerTransaction;


	public String describe(String extra)
	{

		return String.format("%s@%s(date = %s," +
									 " totalAmount = '%s'," +
									 " totalAmountAirtime = '%s'," +
									 " totalAmountNonAirtime = '%s'," +
									 " agentCount = '%s'," +
									 " agentCountAirtime = '%s'," +
									 " agentCountNonAirtime = '%s'," +
									 " successfulTransactionCount = '%s'," +
									 " successfulAirtimeTransactionCount = '%s'," +
									 " successfulNonAirtimeTransactionCount = '%s'," +
									 " failedTransactionCount = '%s'," +
									 " failedAirtimeTransactionCount = '%s'," +
									 " failedNonAirtimeTransactionCount = '%s'," +
									 " averageAmountPerAgent = '%s'," +
									 " averageAirtimeAmountPerAgent = '%s'," +
									 " averageNonAirtimeAmountPerAgent = '%s'," +
									 " averageAmountPerTransaction = '%s'," +
									 " averageAirtimeAmountPerTransaction = '%s'," +
									 " averageNonAirtimeAmountPerTransaction = '%s'," +
									 " MMTransferCountSuccess = '%s'," +
									 " MMTotalTransferAmount = '%s'," +
									 " MMTransferCountFailed = '%s'," +
									 " MMReceivingAgentCount = '%s'," +
									 " MMAverageTransferAmountPerAgent = '%s'," +
									 " MMAverageTransferAmountPerTransaction = '%s'%s%s)",
							 this.getClass().getName(),
							 Integer.toHexString(this.hashCode()),
							 new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(date),
							 totalAmount,
							 totalAirtimeAmount,
							 totalAmountNonAirtime,
							 agentCount,
							 agentCountAirtime,
							 agentCountNonAirtime,
							 successfulTransactionCount,
							 successfulAirtimeTransactionCount,
							 successfulNonAirtimeTransactionCount,
							 failedTransactionCount,
							 failedAirtimeTransactionCount,
							 failedNonAirtimeTransactionCount,
							 averageAmountPerAgent,
							 averageAirtimeAmountPerAgent,
							 averageNonAirtimeAmountPerAgent,
							 averageAmountPerTransaction,
							 averageAirtimeAmountPerTransaction,
							 averageNonAirtimeAmountPerTransaction,
							 MMTransferCountSuccess,
							 MMTotalTransferAmount,
							 MMTransferCountFailed,
							 MMReceivingAgentCount,
							 MMAverageTransferAmountPerAgent,
							 MMAverageTransferAmountPerTransaction,
							 (extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

    @Override
    public int hashCode()
    {
        return Objects.hash(
			date,
			totalAmount,
			totalAirtimeAmount,
			totalAmountNonAirtime,
			agentCount,
			agentCountAirtime,
			agentCountNonAirtime,
			successfulTransactionCount,
			successfulAirtimeTransactionCount,
			successfulNonAirtimeTransactionCount,
			failedTransactionCount,
			failedAirtimeTransactionCount,
			failedNonAirtimeTransactionCount,

			averageAmountPerAgent,
			averageAirtimeAmountPerAgent,
			averageNonAirtimeAmountPerAgent,
			averageAmountPerTransaction,
			averageAirtimeAmountPerTransaction,
			averageNonAirtimeAmountPerTransaction,

			MMTransferCountSuccess,
			MMTotalTransferAmount,
			MMTransferCountFailed,
			MMReceivingAgentCount,
			MMAverageTransferAmountPerAgent,
			MMAverageTransferAmountPerTransaction
		);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof SalesSummaryReportResultEntry))
            return false;
        SalesSummaryReportResultEntry otherTyped = (SalesSummaryReportResultEntry) other;
        return ( true
			&& Objects.equals(this.date, otherTyped.date)
			&& Objects.equals(this.totalAmount, otherTyped.totalAmount)
			&& Objects.equals(this.totalAirtimeAmount, otherTyped.totalAirtimeAmount)
			&& Objects.equals(this.totalAmountNonAirtime, otherTyped.totalAmountNonAirtime)
			&& Objects.equals(this.agentCount, otherTyped.agentCount)
			&& Objects.equals(this.agentCountAirtime, otherTyped.agentCountAirtime)
			&& Objects.equals(this.agentCountNonAirtime, otherTyped.agentCountNonAirtime)
			&& Objects.equals(this.successfulTransactionCount, otherTyped.successfulTransactionCount)
			&& Objects.equals(this.successfulAirtimeTransactionCount, otherTyped.successfulAirtimeTransactionCount)
			&& Objects.equals(this.successfulNonAirtimeTransactionCount, otherTyped.successfulNonAirtimeTransactionCount)
			&& Objects.equals(this.failedTransactionCount, otherTyped.failedTransactionCount)
			&& Objects.equals(this.failedAirtimeTransactionCount, otherTyped.failedAirtimeTransactionCount)
			&& Objects.equals(this.failedNonAirtimeTransactionCount, otherTyped.failedNonAirtimeTransactionCount)

			&& Objects.equals(this.averageAmountPerAgent, otherTyped.averageAmountPerAgent)
			&& Objects.equals(this.averageAirtimeAmountPerAgent, otherTyped.averageAirtimeAmountPerAgent)
			&& Objects.equals(this.averageNonAirtimeAmountPerAgent, otherTyped.averageNonAirtimeAmountPerAgent)
			&& Objects.equals(this.averageAmountPerTransaction, otherTyped.averageAmountPerTransaction)
			&& Objects.equals(this.averageAirtimeAmountPerTransaction, otherTyped.averageAirtimeAmountPerTransaction)
			&& Objects.equals(this.averageNonAirtimeAmountPerTransaction, otherTyped.averageNonAirtimeAmountPerTransaction)
			&& Objects.equals(this.MMTotalTransferAmount, otherTyped.MMTotalTransferAmount)
			&& Objects.equals(this.MMTransferCountSuccess, otherTyped.MMTransferCountSuccess)
			&& Objects.equals(this.MMTransferCountFailed, otherTyped.MMTransferCountFailed)
			&& Objects.equals(this.MMReceivingAgentCount, otherTyped.MMReceivingAgentCount)
			&& Objects.equals(this.MMAverageTransferAmountPerAgent, otherTyped.MMAverageTransferAmountPerAgent)
			&& Objects.equals(this.MMAverageTransferAmountPerTransaction, otherTyped.MMAverageTransferAmountPerTransaction)
		);
    }

	public SalesSummaryReportResultEntry()
	{
	}

	public SalesSummaryReportResultEntry(SalesSummaryReportResultEntry other)
	{
		this.date = other.date;
		this.totalAmount = other.totalAmount;
		this.totalAirtimeAmount = other.totalAirtimeAmount;
		this.totalAmountNonAirtime = other.totalAmountNonAirtime;
		this.agentCount = other.agentCount;
		this.agentCountAirtime = other.agentCountAirtime;
		this.agentCountNonAirtime = other.agentCountNonAirtime;
		this.successfulTransactionCount = other.successfulTransactionCount;
		this.successfulAirtimeTransactionCount = other.successfulAirtimeTransactionCount;
		this.successfulNonAirtimeTransactionCount = other.successfulNonAirtimeTransactionCount;
		this.failedTransactionCount = other.failedTransactionCount;
		this.failedAirtimeTransactionCount = other.failedAirtimeTransactionCount;
		this.failedNonAirtimeTransactionCount = other.failedNonAirtimeTransactionCount;

		this.averageAmountPerAgent = other.averageAmountPerAgent;
		this.averageAirtimeAmountPerAgent = other.averageAirtimeAmountPerAgent;
		this.averageNonAirtimeAmountPerAgent = other.averageNonAirtimeAmountPerAgent;
		this.averageAmountPerTransaction = other.averageAmountPerTransaction;
		this.averageAirtimeAmountPerTransaction = other.averageAirtimeAmountPerTransaction;
		this.averageNonAirtimeAmountPerTransaction = other.averageNonAirtimeAmountPerTransaction;

		this.MMTotalTransferAmount = other.MMTotalTransferAmount;
		this.MMTransferCountSuccess = other.MMTransferCountSuccess;
		this.MMTransferCountFailed = other.MMTransferCountFailed;
		this.MMReceivingAgentCount = other.MMReceivingAgentCount;
		this.MMAverageTransferAmountPerAgent = other.MMAverageTransferAmountPerAgent;
		this.MMAverageTransferAmountPerTransaction = other.MMAverageTransferAmountPerTransaction;
	}

	public Date getDate()
	{
		return this.date;
	}
	public SalesSummaryReportResultEntry setDate( Date date )
	{
		this.date = date;
		return this;
	}

	public BigDecimal getTotalAmount()
	{
		return this.totalAmount;
	}

	public BigDecimal getTotalAirtimeAmount() {
		return this.totalAirtimeAmount;
	}

	public BigDecimal getTotalAmountNonAirtime() {
		return this.totalAmountNonAirtime;
	}

	public SalesSummaryReportResultEntry setTotalAmount( BigDecimal totalAmount )
	{
		this.totalAmount = totalAmount;
		return this;
	}

	public SalesSummaryReportResultEntry setTotalAmountNonAirtime(BigDecimal totalAmountNonAirtime) {
		this.totalAmountNonAirtime = totalAmountNonAirtime;
		return this;
	}

	public SalesSummaryReportResultEntry setTotalAirtimeAmount(BigDecimal totalAirtimeAmount) {
		this.totalAirtimeAmount = totalAirtimeAmount;
		return this;
	}

	public Integer getAgentCount()
	{
		return this.agentCount;
	}

	public Integer getAgentCountNonAirtime() {
		return this.agentCountNonAirtime;
	}

	public Integer getAgentCountAirtime() {
		return this.agentCountAirtime;
	}
	
	public SalesSummaryReportResultEntry setAgentCount( Integer agentCount )
	{
		this.agentCount = agentCount;
		return this;
	}

	public SalesSummaryReportResultEntry setAgentCountNonAirtime(Integer agentCountNonAirtime) {
		this.agentCountNonAirtime = agentCountNonAirtime;
		return this;
	}

	public SalesSummaryReportResultEntry setAgentCountAirtime(Integer agentCountAirtime) {
		this.agentCountAirtime = agentCountAirtime;
		return this;
	}

	public Integer getSuccessfulTransactionCount()
	{
		return this.successfulTransactionCount;
	}

	public Integer getSuccessfulNonAirtimeTransactionCount() {
		return this.successfulNonAirtimeTransactionCount;
	}

	public Integer getSuccessfulAirtimeTransactionCount() {
		return this.successfulAirtimeTransactionCount;
	}

	public Integer getFailedNonAirtimeTransactionCount() {
		return this.failedNonAirtimeTransactionCount;
	}

	public Integer getFailedAirtimeTransactionCount() {
		return this.failedAirtimeTransactionCount;
	}
    
	public SalesSummaryReportResultEntry setSuccessfulTransactionCount( Integer successfulTransactionCount )
	{
		this.successfulTransactionCount = successfulTransactionCount;
		return this;
	}

	public SalesSummaryReportResultEntry setSuccessfulNonAirtimeTransactionCount(Integer successfulNonAirtimeTransactionCount) {
		this.successfulNonAirtimeTransactionCount = successfulNonAirtimeTransactionCount;
		return this;
	}

	public SalesSummaryReportResultEntry setSuccessfulAirtimeTransactionCount(Integer successfulAirtimeTransactionCount) {
		this.successfulAirtimeTransactionCount = successfulAirtimeTransactionCount;
		return this;
	}

	public SalesSummaryReportResultEntry setFailedNonAirtimeTransactionCount(Integer failedNonAirtimeTransactionCount) {
		this.failedNonAirtimeTransactionCount = failedNonAirtimeTransactionCount;
		return this;
	}

	public SalesSummaryReportResultEntry setFailedAirtimeTransactionCount(Integer failedAirtimeTransactionCount) {
		this.failedAirtimeTransactionCount = failedAirtimeTransactionCount;
		return this;
	}

	public Integer getFailedTransactionCount()
	{
		return this.failedTransactionCount;
	}
	public SalesSummaryReportResultEntry setFailedTransactionCount( Integer failedTransactionCount )
	{
		this.failedTransactionCount = failedTransactionCount;
		return this;
	}

	public BigDecimal getAverageAmountPerAgent()
	{
		return this.averageAmountPerAgent;
	}

	public BigDecimal getAverageAirtimeAmountPerAgent() {
		return this.averageAirtimeAmountPerAgent;
	}
	public SalesSummaryReportResultEntry setAverageAmountPerAgent( BigDecimal averageAmountPerAgent )
	{
		this.averageAmountPerAgent = averageAmountPerAgent;
		return this;
	}

	public BigDecimal getAverageNonAirtimeAmountPerAgent() {
		return this.averageNonAirtimeAmountPerAgent;
	}

	public SalesSummaryReportResultEntry setAverageNonAirtimeAmountPerAgent(BigDecimal averageNonAirtimeAmountPerAgent) {
		this.averageNonAirtimeAmountPerAgent = averageNonAirtimeAmountPerAgent;
		return this;
	}

	public SalesSummaryReportResultEntry setAverageAirtimeAmountPerAgent(BigDecimal averageAirtimeAmountPerAgent) {
		this.averageAirtimeAmountPerAgent = averageAirtimeAmountPerAgent;
		return this;
	}

	public BigDecimal getAverageAmountPerTransaction()
	{
		return this.averageAmountPerTransaction;
	}
	public SalesSummaryReportResultEntry setAverageAmountPerTransaction( BigDecimal averageAmountPerTransaction )
	{
		this.averageAmountPerTransaction = averageAmountPerTransaction;
		return this;
	}

	public BigDecimal getAverageNonAirtimeAmountPerTransaction() {
		return this.averageNonAirtimeAmountPerTransaction;
	}

	public SalesSummaryReportResultEntry setAverageNonAirtimeAmountPerTransaction(BigDecimal averageNonAirtimeAmountPerTransaction) {
		this.averageNonAirtimeAmountPerTransaction = averageNonAirtimeAmountPerTransaction;
		return this;
	}

	public BigDecimal getAverageAirtimeAmountPerTransaction() {
		return this.averageAirtimeAmountPerTransaction;
	}

	public SalesSummaryReportResultEntry setAverageAirtimeAmountPerTransaction(BigDecimal averageAirtimeAmountPerTransaction) {
		this.averageAirtimeAmountPerTransaction = averageAirtimeAmountPerTransaction;
		return this;
	}

	public SalesSummaryReportResultEntry setMMTransferCountSuccess(Integer MMTransferCountSuccess) {
		this.MMTransferCountSuccess = MMTransferCountSuccess;
		return this;
	}

	public Integer getMMTransferCountSuccess() {
		return this.MMTransferCountSuccess;
	}

	public SalesSummaryReportResultEntry setMMTotalTransferAmount(BigDecimal MMTotalTransferAmount) {
		this.MMTotalTransferAmount = MMTotalTransferAmount;
		return this;
	}

	public BigDecimal getMMTotalTransferAmount() {
		return this.MMTotalTransferAmount;
	}

	public SalesSummaryReportResultEntry setMMTransferCountFailed(Integer MMTransferCountFailed) {
		this.MMTransferCountFailed = MMTransferCountFailed;
		return this;
	}

	public Integer getMMTransferCountFailed() {
		return this.MMTransferCountFailed;
	}

	public SalesSummaryReportResultEntry setMMReceivingAgentCount(Integer MMReceivingAgentCount) {
		this.MMReceivingAgentCount = MMReceivingAgentCount;
		return this;
	}

	public Integer getMMReceivingAgentCount() {
		return this.MMReceivingAgentCount;
	}

	public SalesSummaryReportResultEntry setMMAverageTransferAmountPerAgent(BigDecimal MMAverageTransferAmountPerAgent) {
		this.MMAverageTransferAmountPerAgent = MMAverageTransferAmountPerAgent;
		return this;
	}

	public BigDecimal getMMAverageTransferAmountPerAgent() {
		return this.MMAverageTransferAmountPerAgent;
	}

	public SalesSummaryReportResultEntry setMMAverageTransferAmountPerTransaction(BigDecimal MMAverageTransferAmountPerTransaction) {
		this.MMAverageTransferAmountPerTransaction = MMAverageTransferAmountPerTransaction;
		return this;
	}

	public BigDecimal getMMAverageTransferAmountPerTransaction() {
		return this.MMAverageTransferAmountPerTransaction;
	}
}

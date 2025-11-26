package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Objects;

public class DailyGroupSalesReportResultEntry implements Comparable<DailyGroupSalesReportResultEntry>
{
	private String groupName;
	private Integer agentTotalCount;
	private Integer agentTransactedCount;
	private Integer transactionCount;
	private BigDecimal agentAverageAmount;
	private BigDecimal transactionAverageAmount;
	private BigDecimal totalAmount;

	public String describe(String extra)
	{
		
		return String.format("%s@%s(date = %s, groupName = %s, agentTotalCount = %s, agentTransactedCount = %s, transactionCount = %s, agentAverageAmount = %s, transactionAverageAmount = %s, totalAmount = %s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			groupName, agentTotalCount, agentTransactedCount, transactionCount, agentAverageAmount, transactionAverageAmount, totalAmount, (extra.isEmpty() ? "" : ", "), extra);
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
			groupName,
			agentTotalCount,
			agentTransactedCount,
			transactionCount,
			agentAverageAmount,
			transactionAverageAmount,
			totalAmount
		);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof DailyGroupSalesReportResultEntry))
            return false;
        DailyGroupSalesReportResultEntry otherTyped = (DailyGroupSalesReportResultEntry) other;
        return ( true
			&& Objects.equals(this.groupName, otherTyped.groupName)
			&& Objects.equals(this.agentTotalCount, otherTyped.agentTotalCount)
			&& Objects.equals(this.agentTransactedCount, otherTyped.agentTransactedCount)
			&& Objects.equals(this.transactionCount, otherTyped.transactionCount)
			&& Objects.equals(this.agentAverageAmount, otherTyped.agentAverageAmount)
			&& Objects.equals(this.transactionAverageAmount, otherTyped.transactionAverageAmount)
			&& Objects.equals(this.totalAmount, otherTyped.totalAmount)
		);
    }

	public DailyGroupSalesReportResultEntry()
	{
	}

	public DailyGroupSalesReportResultEntry(DailyGroupSalesReportResultEntry other)
	{
		this.groupName = other.groupName;
		this.agentTotalCount = other.agentTotalCount;
		this.agentTransactedCount = other.agentTransactedCount;
		this.transactionCount = other.transactionCount;
		this.agentAverageAmount = other.agentAverageAmount;
		this.transactionAverageAmount = other.transactionAverageAmount;
		this.totalAmount = other.totalAmount;
	}

	public String getGroupName()
	{
		return this.groupName;
	}

	public DailyGroupSalesReportResultEntry setGroupName(String groupName)
	{
		this.groupName = groupName;
		return this;
	}
	
	public Integer getAgentTotalCount()
	{
		return this.agentTotalCount;
	}

	public DailyGroupSalesReportResultEntry setAgentTotalCount(Integer agentTotalCount)
	{
		this.agentTotalCount = agentTotalCount;
		return this;
	}

	public Integer getAgentTransactedCount()
	{
		return this.agentTransactedCount;
	}

	public DailyGroupSalesReportResultEntry setAgentTransactedCount(Integer agentTransactedCount)
	{
		this.agentTransactedCount = agentTransactedCount;
		return this;
	}

	public Integer getTransactionCount()
	{
		return this.transactionCount;
	}

	public DailyGroupSalesReportResultEntry setTransactionCount(Integer transactionCount)
	{
		this.transactionCount = transactionCount;
		return this;
	}
	
	public BigDecimal getAgentAverageAmount()
	{
		return this.agentAverageAmount;
	}

	public DailyGroupSalesReportResultEntry setAgentAverageAmount(BigDecimal agentAverageAmount)
	{
		this.agentAverageAmount = agentAverageAmount;
		return this;
	}
	
	public BigDecimal getTransactionAverageAmount()
	{
		return this.transactionAverageAmount;
	}

	public DailyGroupSalesReportResultEntry setTransactionAverageAmount(BigDecimal transactionAverageAmount)
	{
		this.transactionAverageAmount = transactionAverageAmount;
		return this;
	}

	public BigDecimal getTotalAmount()
	{
		return this.totalAmount;
	}

	public DailyGroupSalesReportResultEntry setTotalAmount(BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
		return this;
	}

	@Override
	public int compareTo(DailyGroupSalesReportResultEntry other)
	{
		if (this.getGroupName() == null) {
			return -1;
		}
		if (other == null || other.getGroupName() == null) {
			return 1;
		}
		return this.getGroupName().compareTo(other.getGroupName());	
	}
}

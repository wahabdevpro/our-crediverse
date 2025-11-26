package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Objects;

public class MonthlySalesPerformanceReportResultEntry implements Comparable<MonthlySalesPerformanceReportResultEntry>
{
	private String month;
	private String groupName;
	private String ownerMsisdn;
	private String msisdn;
	private BigDecimal totalAmount;

	public String describe(String extra)
	{
		return String.format("%s@%s(month = %s, groupName = %s, ownerMsisdn = %s, agentMsisdn = %s, totalAmount = %s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			month, groupName, ownerMsisdn, msisdn, totalAmount, (extra.isEmpty() ? "" : ", "), extra);
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
        	month,
			groupName,
			ownerMsisdn,
			msisdn,
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
        if (!(other instanceof MonthlySalesPerformanceReportResultEntry))
            return false;
        MonthlySalesPerformanceReportResultEntry otherTyped = (MonthlySalesPerformanceReportResultEntry) other;
        return ( true
        	&& Objects.equals(this.month, otherTyped.month)
			&& Objects.equals(this.groupName, otherTyped.groupName)
			&& Objects.equals(this.ownerMsisdn, otherTyped.ownerMsisdn)
			&& Objects.equals(this.msisdn, otherTyped.msisdn)
			&& Objects.equals(this.totalAmount, otherTyped.totalAmount)
		);
    }

	public MonthlySalesPerformanceReportResultEntry()
	{
	}

	public MonthlySalesPerformanceReportResultEntry(MonthlySalesPerformanceReportResultEntry other)
	{
		this.month = other.month;
		this.groupName = other.groupName;
		this.ownerMsisdn = other.ownerMsisdn;
		this.msisdn = other.msisdn;
		this.totalAmount = other.totalAmount;
	}

	public String getMonth()
	{
		return this.month;
	}

	public MonthlySalesPerformanceReportResultEntry setMonth(String month)
	{
		this.month = month;
		return this;
	}

	public String getGroupName()
	{
		return this.groupName;
	}

	public MonthlySalesPerformanceReportResultEntry setGroupName(String groupName)
	{
		this.groupName = groupName;
		return this;
	}

	public String getOwnerMsisdn()
	{
		return this.ownerMsisdn;
	}

	public MonthlySalesPerformanceReportResultEntry setOwnerMsisdn(String ownerMsisdn)
	{
		this.ownerMsisdn = ownerMsisdn;
		return this;
	}

	public String getMsisdn()
	{
		return this.msisdn;
	}

	public MonthlySalesPerformanceReportResultEntry setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
		return this;
	}	

	public BigDecimal getTotalAmount()
	{
		return this.totalAmount;
	}

	public MonthlySalesPerformanceReportResultEntry setTotalAmount(BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
		return this;
	}

	@Override
	public int compareTo(MonthlySalesPerformanceReportResultEntry other)
	{
		return this.getGroupName().compareTo(other.getGroupName());	
	}
}

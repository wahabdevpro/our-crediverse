package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Objects;

public class DailyPerformanceByAreaResultEntry implements Comparable<DailyPerformanceByAreaResultEntry>
{
	private String areaName;
	private String areaType;
	private Integer transactionCount_SL;
	private Integer successTransactionCount_SL;
	private Integer failTransactionCount_SL;
	private Integer uniqueAgentCount_SL;
	private BigDecimal totalAmount_SL;
	private BigDecimal averageAgentAmount_SL;
	private BigDecimal averageTransactionAmount_SL;

	private Integer transactionCount_ST;
	private Integer successTransactionCount_ST;
	private Integer failTransactionCount_ST;
	private Integer uniqueAgentCount_ST;
	private BigDecimal totalAmount_ST;
	private BigDecimal averageAgentAmount_ST;
	private BigDecimal averageTransactionAmount_ST;

	public String describe(String extra)
	{
		
		return String.format("%s@%s(areaName = %s, areaType = %s, transactionCount_SL = %s, successTransactionCount_SL = %s, failTransactionCount_SL = %s, uniqueAgentCount_SL = %s, totalAmount_SL = %s, averageAgentAmount_SL = %s, averageTransactionAmount_SL = %s, transactionCount_ST = %s, successTransactionCount_ST = %s, failTransactionCount_ST = %s, uniqueAgentCount_ST = %s, totalAmount_ST = %s, averageAgentAmount_ST = %s, averageTransactionAmount_ST = %s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			areaName, areaType, transactionCount_SL, successTransactionCount_SL, failTransactionCount_SL, uniqueAgentCount_SL, totalAmount_SL, averageAgentAmount_SL, averageTransactionAmount_SL, transactionCount_ST, successTransactionCount_ST, failTransactionCount_ST, uniqueAgentCount_ST, totalAmount_ST, averageAgentAmount_ST, averageTransactionAmount_ST, (extra.isEmpty() ? "" : ", "), extra);
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
			areaName,
			areaType,
			transactionCount_SL,
			successTransactionCount_SL,
			failTransactionCount_SL,
			uniqueAgentCount_SL,
			totalAmount_SL,
			averageAgentAmount_SL,
			averageTransactionAmount_SL,
			transactionCount_ST,
			successTransactionCount_ST,
			failTransactionCount_ST,
			uniqueAgentCount_ST,
			totalAmount_ST,
			averageAgentAmount_ST,
			averageTransactionAmount_ST
		);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof DailyPerformanceByAreaResultEntry))
            return false;
        DailyPerformanceByAreaResultEntry otherTyped = (DailyPerformanceByAreaResultEntry) other;
        return ( true
			&& Objects.equals(this.areaName, otherTyped.areaName)
			&& Objects.equals(this.areaType, otherTyped.areaType)
			
			&& Objects.equals(this.transactionCount_SL, otherTyped.transactionCount_SL)
			&& Objects.equals(this.successTransactionCount_SL, otherTyped.successTransactionCount_SL)
			&& Objects.equals(this.failTransactionCount_SL, otherTyped.failTransactionCount_SL)
			&& Objects.equals(this.uniqueAgentCount_SL, otherTyped.uniqueAgentCount_SL)
			&& Objects.equals(this.totalAmount_SL, otherTyped.totalAmount_SL)
			&& Objects.equals(this.averageAgentAmount_SL, otherTyped.averageAgentAmount_SL)
			&& Objects.equals(this.averageTransactionAmount_SL, otherTyped.averageTransactionAmount_SL)
			
			&& Objects.equals(this.transactionCount_ST, otherTyped.transactionCount_ST)
			&& Objects.equals(this.successTransactionCount_ST, otherTyped.successTransactionCount_ST)
			&& Objects.equals(this.failTransactionCount_ST, otherTyped.failTransactionCount_ST)
			&& Objects.equals(this.uniqueAgentCount_ST, otherTyped.uniqueAgentCount_ST)
			&& Objects.equals(this.totalAmount_ST, otherTyped.totalAmount_ST)
			&& Objects.equals(this.averageAgentAmount_ST, otherTyped.averageAgentAmount_ST)
			&& Objects.equals(this.averageTransactionAmount_ST, otherTyped.averageTransactionAmount_ST)
		);
    }

	public DailyPerformanceByAreaResultEntry()
	{
	}

	public DailyPerformanceByAreaResultEntry(DailyPerformanceByAreaResultEntry other)
	{
		this.areaName =  other.areaName;
		this.areaType =  other.areaType;
		this.transactionCount_SL =  other.transactionCount_SL;
		this.successTransactionCount_SL =  other.successTransactionCount_SL;
		this.failTransactionCount_SL =  other.failTransactionCount_SL;
		this.uniqueAgentCount_SL =  other.uniqueAgentCount_SL;
		this.totalAmount_SL =  other.totalAmount_SL;
		this.averageAgentAmount_SL =  other.averageAgentAmount_SL;
		this.averageTransactionAmount_SL =  other.averageTransactionAmount_SL;

		this.transactionCount_ST =  other.transactionCount_ST;
		this.successTransactionCount_ST =  other.successTransactionCount_ST;
		this.failTransactionCount_ST =  other.failTransactionCount_ST;
		this.uniqueAgentCount_ST =  other.uniqueAgentCount_ST;
		this.totalAmount_ST =  other.totalAmount_ST;
		this.averageAgentAmount_ST =  other.averageAgentAmount_ST;
		this.averageTransactionAmount_ST =  other.averageTransactionAmount_ST;
	}

	public String getAreaName()
	{
		return this.areaName;
	}

	public DailyPerformanceByAreaResultEntry setAreaName(String areaName)
	{
		this.areaName = areaName;
		return this;
	}

	public String getAreaType()
	{
		return this.areaType;
	}

	public DailyPerformanceByAreaResultEntry setAreaType(String areaType)
	{
		this.areaType = areaType;
		return this;
	}

	public Integer getTransactionCount_SL()
	{
		return this.transactionCount_SL;
	}

	public DailyPerformanceByAreaResultEntry setTransactionCount_SL(Integer transactionCount_SL)
	{
		this.transactionCount_SL = transactionCount_SL;
		return this;
	}

	public DailyPerformanceByAreaResultEntry setSuccessTransactionCount_SL(Integer successTransactionCount_SL)
	{
		this.successTransactionCount_SL = successTransactionCount_SL;
		return this;
	}

	public Integer getSuccessTransactionCount_SL()
	{
		return this.successTransactionCount_SL;
	}

	public DailyPerformanceByAreaResultEntry setFailTransactionCount_SL(Integer failTransactionCount_SL)
	{
		this.failTransactionCount_SL = failTransactionCount_SL;
		return this;
	}

	public Integer getFailTransactionCount_SL()
	{
		return this.failTransactionCount_SL;
	}

	public DailyPerformanceByAreaResultEntry setUniqueAgentCount_SL(Integer uniqueAgentCount_SL)
	{
		this.uniqueAgentCount_SL = uniqueAgentCount_SL;
		return this;
	}

	public Integer getUniqueAgentCount_SL()
	{
		return this.uniqueAgentCount_SL;
	}

	public DailyPerformanceByAreaResultEntry setAverageAgentAmount_SL(BigDecimal agentAverageAmount_SL)
	{
		this.averageAgentAmount_SL = agentAverageAmount_SL;
		return this;
	}

	public BigDecimal getAverageAgentAmount_SL()
	{
		return this.averageAgentAmount_SL;
	}

	public DailyPerformanceByAreaResultEntry setAverageTransactionAmount_SL(BigDecimal averageTransactionAmount_SL)
	{
		this.averageTransactionAmount_SL = averageTransactionAmount_SL;
		return this;
	}

	public BigDecimal getAverageTransactionAmount_SL()
	{
		return this.averageTransactionAmount_SL;
	}

	public BigDecimal getTotalAmount_SL()
	{
		return this.totalAmount_SL;
	}

	public DailyPerformanceByAreaResultEntry setTotalAmount_SL(BigDecimal totalAmount_SL)
	{
		this.totalAmount_SL = totalAmount_SL;
		return this;
	}

	public Integer getTransactionCount_ST()
	{
		return this.transactionCount_ST;
	}

	public DailyPerformanceByAreaResultEntry setTransactionCount_ST(Integer transactionCount_SL)
	{
		this.transactionCount_ST = transactionCount_SL;
		return this;
	}

	public DailyPerformanceByAreaResultEntry setSuccessTransactionCount_ST(Integer successTransactionCount_ST)
	{
		this.successTransactionCount_ST = successTransactionCount_ST;
		return this;
	}

	public Integer getSuccessTransactionCount_ST()
	{
		return this.successTransactionCount_ST;
	}

	public DailyPerformanceByAreaResultEntry setFailTransactionCount_ST(Integer failTransactionCount_ST)
	{
		this.failTransactionCount_ST = failTransactionCount_ST;
		return this;
	}

	public Integer getFailTransactionCount_ST()
	{
		return this.failTransactionCount_ST;
	}

	public DailyPerformanceByAreaResultEntry setUniqueAgentCount_ST(Integer uniqueAgentCount_ST)
	{
		this.uniqueAgentCount_ST = uniqueAgentCount_ST;
		return this;
	}

	public Integer getUniqueAgentCount_ST()
	{
		return this.uniqueAgentCount_ST;
	}

	public DailyPerformanceByAreaResultEntry setAverageAgentAmount_ST(BigDecimal averageAgentAmount_ST)
	{
		this.averageAgentAmount_ST = averageAgentAmount_ST;
		return this;
	}

	public BigDecimal getAverageAgentAmount_ST()
	{
		return this.averageAgentAmount_ST;
	}

	public DailyPerformanceByAreaResultEntry setAverageTransactionAmount_ST(BigDecimal averageTransactionAmount_ST)
	{
		this.averageTransactionAmount_ST = averageTransactionAmount_ST;
		return this;
	}

	public BigDecimal getAverageTransactionAmount_ST()
	{
		return this.averageTransactionAmount_ST;
	}

	public BigDecimal getTotalAmount_ST()
	{
		return this.totalAmount_ST;
	}

	public DailyPerformanceByAreaResultEntry setTotalAmount_ST(BigDecimal totalAmount_ST)
	{
		this.totalAmount_ST = totalAmount_ST;
		return this;
	}

	@Override
	public int compareTo(DailyPerformanceByAreaResultEntry other)
	{
		if (this.getAreaName() == null) {
			return -1;
		}
		if (other == null || other.getAreaName() == null) {
			return 1;
		}
		return this.getAreaName().compareTo(other.getAreaName());	
	}
}

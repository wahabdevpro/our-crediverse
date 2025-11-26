package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class RetailerPerformanceReportResultEntry
{
	private Date date;

	private String transactionType;
	private Boolean transactionStatus;
	private String followUp;

	private int a_AgentID;
	private String a_AccountNumber;
	private String a_MobileNumber;
	private String a_IMEI;
	private String a_IMSI;
	private String a_Name;

	private String a_TierName;
	private String a_GroupName;
	private String a_ServiceClassName;

	private String a_OwnerImsi;
	private int a_OwnerID;
	private String a_OwnerMobileNumber;
	private String a_OwnerName;

	private BigDecimal totalAmount;
	private BigDecimal totalBonus;
	private Integer transactionCount;

	public String describe(String extra)
	{
		
		return String.format("%s@%s(date = %s, transactionType = %s, transactionStatus = %s, followUp = %s, a_AgentID = %s, a_AccountNumber = %s, a_MobileNumber = %s, a_IMEI = %s, a_IMSI = %s, a_Name = %s, a_TierName = %s, a_GroupName = %s, a_ServiceClassName = %s, a_OwnerImsi = %s, a_OwnerID = %s, a_OwnerMobileNumber = %s, a_OwnerName = %s, totalAmount = %s, totalBonus = %s, transactionCount = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(date), transactionType, transactionStatus, followUp,
			a_AgentID, a_AccountNumber, a_MobileNumber, a_IMEI, a_IMSI, a_Name,
			a_TierName, a_GroupName, a_ServiceClassName,
			a_OwnerImsi, a_OwnerID, a_OwnerMobileNumber, a_OwnerName,
			totalAmount, totalBonus, transactionCount,
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

			transactionType,
			transactionStatus,
			followUp,

			a_AgentID,
			a_AccountNumber,
			a_MobileNumber,
			a_IMEI,
			a_IMSI,
			a_Name,

			a_TierName,
			a_GroupName,
			a_ServiceClassName,

			a_OwnerImsi,
			a_OwnerID,
			a_OwnerMobileNumber,
			a_OwnerName,

			totalAmount,
			totalBonus,
			transactionCount
		);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof RetailerPerformanceReportResultEntry))
            return false;
        RetailerPerformanceReportResultEntry otherTyped = (RetailerPerformanceReportResultEntry) other;
        return ( true
			&& Objects.equals(this.date, otherTyped.date)

			&& Objects.equals(this.transactionType, otherTyped.transactionType)
			&& Objects.equals(this.transactionStatus, otherTyped.transactionStatus)
			&& Objects.equals(this.followUp, otherTyped.followUp)

			&& Objects.equals(this.a_AgentID, otherTyped.a_AgentID)
			&& Objects.equals(this.a_AccountNumber, otherTyped.a_AccountNumber)
			&& Objects.equals(this.a_MobileNumber, otherTyped.a_MobileNumber)
			&& Objects.equals(this.a_IMEI, otherTyped.a_IMEI)
			&& Objects.equals(this.a_IMSI, otherTyped.a_IMSI)
			&& Objects.equals(this.a_Name, otherTyped.a_Name)

			&& Objects.equals(this.a_TierName, otherTyped.a_TierName)
			&& Objects.equals(this.a_GroupName, otherTyped.a_GroupName)
			&& Objects.equals(this.a_ServiceClassName, otherTyped.a_ServiceClassName)

			&& Objects.equals(this.a_OwnerImsi, otherTyped.a_OwnerImsi)
			&& Objects.equals(this.a_OwnerID, otherTyped.a_OwnerID)
			&& Objects.equals(this.a_OwnerMobileNumber, otherTyped.a_OwnerMobileNumber)
			&& Objects.equals(this.a_OwnerName, otherTyped.a_OwnerName)

			&& Objects.equals(this.totalAmount, otherTyped.totalAmount)
			&& Objects.equals(this.totalBonus, otherTyped.totalBonus)
			&& Objects.equals(this.transactionCount, otherTyped.transactionCount)
		);
    }

	public RetailerPerformanceReportResultEntry()
	{
	}

	public RetailerPerformanceReportResultEntry(RetailerPerformanceReportResultEntry other)
	{
		this.date = other.date;
		this.transactionType = other.transactionType;
		this.transactionStatus = other.transactionStatus;
		this.followUp = other.followUp;
		this.a_AccountNumber = other.a_AccountNumber;
		this.a_AgentID = other.a_AgentID;
		this.a_MobileNumber = other.a_MobileNumber;
		this.a_IMEI = other.a_IMEI;
		this.a_IMSI = other.a_IMSI;
		this.a_Name = other.a_Name;
		this.a_TierName = other.a_TierName;
		this.a_GroupName = other.a_GroupName;
		this.a_ServiceClassName = other.a_ServiceClassName;
		this.a_OwnerImsi = other.a_OwnerImsi;
		this.a_OwnerID = other.a_OwnerID;
		this.a_OwnerMobileNumber = other.a_OwnerMobileNumber;
		this.a_OwnerName = other.a_OwnerName;
		this.totalAmount = other.totalAmount;
		this.totalBonus = other.totalBonus;
		this.transactionCount = other.transactionCount;
	}

	public Date getDate()
	{
		return this.date;
	}

	public RetailerPerformanceReportResultEntry setDate(Date date)
	{
		this.date = date;
		return this;
	}

	public String getTransactionType()
	{
		return this.transactionType;
	}

	public RetailerPerformanceReportResultEntry setTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
		return this;
	}

	public Boolean getTransactionStatus()
	{
		return this.transactionStatus;
	}

	public RetailerPerformanceReportResultEntry setTransactionStatus(Boolean transactionStatus)
	{
		this.transactionStatus = transactionStatus;
		return this;
	}

	public String getFollowUp()
	{
		return this.followUp;
	}

	public RetailerPerformanceReportResultEntry setFollowUp(String followUp)
	{
		this.followUp = followUp;
		return this;
	}

	public int getA_AgentID()
	{
		return this.a_AgentID;
	}

	public RetailerPerformanceReportResultEntry setA_AgentID(int a_AgentID)
	{
		this.a_AgentID = a_AgentID;
		return this;
	}

	public String getA_AccountNumber()
	{
		return this.a_AccountNumber;
	}

	public RetailerPerformanceReportResultEntry setA_AccountNumber(String a_AccountNumber)
	{
		this.a_AccountNumber = a_AccountNumber;
		return this;
	}

	public String getA_MobileNumber()
	{
		return this.a_MobileNumber;
	}

	public RetailerPerformanceReportResultEntry setA_MobileNumber(String a_MobileNumber)
	{
		this.a_MobileNumber = a_MobileNumber;
		return this;
	}

	public String getA_IMEI()
	{
		return this.a_IMEI;
	}

	public RetailerPerformanceReportResultEntry setA_IMEI(String a_IMEI)
	{
		this.a_IMEI = a_IMEI;
		return this;
	}

	public String getA_IMSI()
	{
		return this.a_IMSI;
	}

	public RetailerPerformanceReportResultEntry setA_IMSI(String a_IMSI)
	{
		this.a_IMSI = a_IMSI;
		return this;
	}

	public String getA_Name()
	{
		return this.a_Name;
	}

	public RetailerPerformanceReportResultEntry setA_Name(String a_Name)
	{
		this.a_Name = a_Name;
		return this;
	}

	public String getA_TierName()
	{
		return this.a_TierName;
	}

	public RetailerPerformanceReportResultEntry setA_TierName(String a_TierName)
	{
		this.a_TierName = a_TierName;
		return this;
	}

	public String getA_GroupName()
	{
		return this.a_GroupName;
	}

	public RetailerPerformanceReportResultEntry setA_GroupName(String a_GroupName)
	{
		this.a_GroupName = a_GroupName;
		return this;
	}

	public String getA_ServiceClassName()
	{
		return this.a_ServiceClassName;
	}

	public RetailerPerformanceReportResultEntry setA_ServiceClassName(String a_ServiceClassName)
	{
		this.a_ServiceClassName = a_ServiceClassName;
		return this;
	}

	public int getA_OwnerID()
	{
		return this.a_OwnerID;
	}

	public RetailerPerformanceReportResultEntry setA_OwnerID(int a_OwnerID)
	{
		this.a_OwnerID = a_OwnerID;
		return this;
	}

	public String getA_OwnerMobileNumber()
	{
		return this.a_OwnerMobileNumber;
	}

	public RetailerPerformanceReportResultEntry setA_OwnerMobileNumber(String a_OwnerMobileNumber)
	{
		this.a_OwnerMobileNumber = a_OwnerMobileNumber;
		return this;
	}

	public String getA_OwnerImsi()
	{
		return this.a_OwnerImsi;
	}

	public RetailerPerformanceReportResultEntry setA_OwnerImsi(String a_OwnerImsi)
	{
		this.a_OwnerImsi = a_OwnerImsi;
		return this;
	}

	public String getA_OwnerName()
	{
		return this.a_OwnerName;
	}

	public RetailerPerformanceReportResultEntry setA_OwnerName(String a_OwnerName)
	{
		this.a_OwnerName = a_OwnerName;
		return this;
	}

	public BigDecimal getTotalAmount()
	{
		return this.totalAmount;
	}

	public RetailerPerformanceReportResultEntry setTotalAmount(BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
		return this;
	}

	public BigDecimal getTotalBonus()
	{
		return this.totalBonus;
	}

	public RetailerPerformanceReportResultEntry setTotalBonus(BigDecimal totalBonus)
	{
		this.totalBonus = totalBonus;
		return this;
	}

	public Integer getTransactionCount()
	{
		return this.transactionCount;
	}

	public RetailerPerformanceReportResultEntry setTransactionCount(Integer transactionCount)
	{
		this.transactionCount = transactionCount;
		return this;
	}

}

package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class WholesalerPerformanceReportResultEntry
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

	private int b_AgentID;
	private String b_AccountNumber;
	private String b_MobileNumber;
	private String b_IMEI;
	private String b_IMSI;
	private String b_Name;

	private String b_TierName;
	private String b_GroupName;
	private String b_ServiceClassName;

	private String b_OwnerImsi;
	private String b_OwnerMobileNumber;
	private String b_OwnerName;

	private BigDecimal totalAmount;
	private BigDecimal totalBonus;
	private Integer transactionCount;

	public String describe(String extra)
	{
		return String.format(
			"%s@%s(date = %s, transactionType = %s, transactionStatus = %s, followUp = %s,"
			+ " a_AgentID = %s, a_AccountNumber = %s, a_MobileNumber = %s, a_IMEI = %s, a_IMSI = %s, a_Name = %s, a_TierName = %s, a_GroupName = %s, a_ServiceClassName = %s, a_OwnerImsi = %s, a_OwnerID = %s, a_OwnerMobileNumber = %s, a_OwnerName = %s,"
			+ " b_AgentID = %s, b_AccountNumber = %s, b_MobileNumber = %s, b_IMEI = %s, b_IMSI = %s, b_Name = %s, b_TierName = %s, b_GroupName = %s, b_ServiceClassName = %s, b_OwnerImsi = %s, b_OwnerMobileNumber = %s, b_OwnerName = %s,"
			+ " totalAmount = %s, totalBonus = %s, transactionCount = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z").format(date), transactionType, transactionStatus, followUp,
			a_AgentID, a_AccountNumber, a_MobileNumber, a_IMEI, a_IMSI, a_Name,
			a_TierName, a_GroupName, a_ServiceClassName,
			a_OwnerImsi, a_OwnerID, a_OwnerMobileNumber, a_OwnerName,
			b_AgentID, b_AccountNumber, b_MobileNumber, a_IMEI, b_IMSI, b_Name,
			b_TierName, b_GroupName, b_ServiceClassName,
			b_OwnerImsi, b_OwnerMobileNumber, b_OwnerName,
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

			b_AgentID,
			b_AccountNumber,
			b_MobileNumber,
			b_IMEI,
			b_IMSI,
			b_Name,

			b_TierName,
			b_GroupName,
			b_ServiceClassName,

			b_OwnerImsi,
			b_OwnerMobileNumber,
			b_OwnerName,

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
        if (!(other instanceof WholesalerPerformanceReportResultEntry))
            return false;
        WholesalerPerformanceReportResultEntry otherTyped = (WholesalerPerformanceReportResultEntry) other;
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

			&& Objects.equals(this.b_AgentID, otherTyped.b_AgentID)
			&& Objects.equals(this.b_AccountNumber, otherTyped.b_AccountNumber)
			&& Objects.equals(this.b_MobileNumber, otherTyped.b_MobileNumber)
			&& Objects.equals(this.b_IMEI, otherTyped.b_IMEI)
			&& Objects.equals(this.b_IMSI, otherTyped.b_IMSI)
			&& Objects.equals(this.b_Name, otherTyped.b_Name)

			&& Objects.equals(this.b_TierName, otherTyped.b_TierName)
			&& Objects.equals(this.b_GroupName, otherTyped.b_GroupName)
			&& Objects.equals(this.b_ServiceClassName, otherTyped.b_ServiceClassName)

			&& Objects.equals(this.b_OwnerImsi, otherTyped.b_OwnerImsi)
			&& Objects.equals(this.b_OwnerMobileNumber, otherTyped.b_OwnerMobileNumber)
			&& Objects.equals(this.b_OwnerName, otherTyped.b_OwnerName)

			&& Objects.equals(this.totalAmount, otherTyped.totalAmount)
			&& Objects.equals(this.totalBonus, otherTyped.totalBonus)
			&& Objects.equals(this.transactionCount, otherTyped.transactionCount)
		);
    }

	public WholesalerPerformanceReportResultEntry()
	{
	}

	public WholesalerPerformanceReportResultEntry(WholesalerPerformanceReportResultEntry other)
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
		this.b_AccountNumber = other.b_AccountNumber;
		this.b_MobileNumber = other.b_MobileNumber;
		this.b_IMEI = other.b_IMEI;
		this.b_IMSI = other.b_IMSI;
		this.b_Name = other.b_Name;
		this.b_TierName = other.b_TierName;
		this.b_GroupName = other.b_GroupName;
		this.b_ServiceClassName = other.b_ServiceClassName;
		this.b_OwnerImsi = other.b_OwnerImsi;
		this.b_OwnerMobileNumber = other.b_OwnerMobileNumber;
		this.b_OwnerName = other.b_OwnerName;
		this.totalAmount = other.totalAmount;
		this.totalBonus = other.totalBonus;
		this.transactionCount = other.transactionCount;
	}

	public Date getDate()
	{
		return this.date;
	}

	public WholesalerPerformanceReportResultEntry setDate(Date date)
	{
		this.date = date;
		return this;
	}

	public String getTransactionType()
	{
		return this.transactionType;
	}

	public WholesalerPerformanceReportResultEntry setTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
		return this;
	}

	public Boolean getTransactionStatus()
	{
		return this.transactionStatus;
	}

	public WholesalerPerformanceReportResultEntry setTransactionStatus(Boolean transactionStatus)
	{
		this.transactionStatus = transactionStatus;
		return this;
	}

	public String getFollowUp()
	{
		return this.followUp;
	}

	public WholesalerPerformanceReportResultEntry setFollowUp(String followUp)
	{
		this.followUp = followUp;
		return this;
	}

	////////////////////////////////////
	//// A-SIDE fields
	////////////////////////////////////

	public int getA_AgentID()
	{
		return this.a_AgentID;
	}

	public WholesalerPerformanceReportResultEntry setA_AgentID(int a_AgentID)
	{
		this.a_AgentID = a_AgentID;
		return this;
	}

	public String getA_AccountNumber()
	{
		return this.a_AccountNumber;
	}

	public WholesalerPerformanceReportResultEntry setA_AccountNumber(String a_AccountNumber)
	{
		this.a_AccountNumber = a_AccountNumber;
		return this;
	}

	public String getA_MobileNumber()
	{
		return this.a_MobileNumber;
	}

	public WholesalerPerformanceReportResultEntry setA_MobileNumber(String a_MobileNumber)
	{
		this.a_MobileNumber = a_MobileNumber;
		return this;
	}

	public String getA_IMEI()
	{
		return this.a_IMEI;
	}

	public WholesalerPerformanceReportResultEntry setA_IMEI(String a_IMEI)
	{
		this.a_IMEI = a_IMEI;
		return this;
	}

	public String getA_IMSI()
	{
		return this.a_IMSI;
	}

	public WholesalerPerformanceReportResultEntry setA_IMSI(String a_IMSI)
	{
		this.a_IMSI = a_IMSI;
		return this;
	}

	public String getA_Name()
	{
		return this.a_Name;
	}

	public WholesalerPerformanceReportResultEntry setA_Name(String a_Name)
	{
		this.a_Name = a_Name;
		return this;
	}

	public String getA_TierName()
	{
		return this.a_TierName;
	}

	public WholesalerPerformanceReportResultEntry setA_TierName(String a_TierName)
	{
		this.a_TierName = a_TierName;
		return this;
	}

	public String getA_GroupName()
	{
		return this.a_GroupName;
	}

	public WholesalerPerformanceReportResultEntry setA_GroupName(String a_GroupName)
	{
		this.a_GroupName = a_GroupName;
		return this;
	}

	public String getA_ServiceClassName()
	{
		return this.a_ServiceClassName;
	}

	public WholesalerPerformanceReportResultEntry setA_ServiceClassName(String a_ServiceClassName)
	{
		this.a_ServiceClassName = a_ServiceClassName;
		return this;
	}

	public int getA_OwnerID()
	{
		return this.a_OwnerID;
	}

	public WholesalerPerformanceReportResultEntry setA_OwnerID(int a_OwnerID)
	{
		this.a_OwnerID = a_OwnerID;
		return this;
	}

	public String getA_OwnerMobileNumber()
	{
		return this.a_OwnerMobileNumber;
	}

	public WholesalerPerformanceReportResultEntry setA_OwnerMobileNumber(String a_OwnerMobileNumber)
	{
		this.a_OwnerMobileNumber = a_OwnerMobileNumber;
		return this;
	}

	public String getA_OwnerImsi()
	{
		return this.a_OwnerImsi;
	}

	public WholesalerPerformanceReportResultEntry setA_OwnerImsi(String a_OwnerImsi)
	{
		this.a_OwnerImsi = a_OwnerImsi;
		return this;
	}

	public String getA_OwnerName()
	{
		return this.a_OwnerName;
	}

	public WholesalerPerformanceReportResultEntry setA_OwnerName(String a_OwnerName)
	{
		this.a_OwnerName = a_OwnerName;
		return this;
	}

	////////////////////////////////////
	//// B-SIDE fields
	////////////////////////////////////

	public int getB_AgentID()
	{
		return this.b_AgentID;
	}

	public WholesalerPerformanceReportResultEntry setB_AgentID(int b_AgentID)
	{
		this.b_AgentID = b_AgentID;
		return this;
	}

	public String getB_AccountNumber()
	{
		return this.b_AccountNumber;
	}

	public WholesalerPerformanceReportResultEntry setB_AccountNumber(String b_AccountNumber)
	{
		this.b_AccountNumber = b_AccountNumber;
		return this;
	}

	public String getB_MobileNumber()
	{
		return this.b_MobileNumber;
	}

	public WholesalerPerformanceReportResultEntry setB_MobileNumber(String b_MobileNumber)
	{
		this.b_MobileNumber = b_MobileNumber;
		return this;
	}

	public String getB_IMEI()
	{
		return this.b_IMEI;
	}

	public WholesalerPerformanceReportResultEntry setB_IMEI(String b_IMEI)
	{
		this.b_IMEI = b_IMEI;
		return this;
	}

	public String getB_IMSI()
	{
		return this.b_IMSI;
	}

	public WholesalerPerformanceReportResultEntry setB_IMSI(String b_IMSI)
	{
		this.b_IMSI = b_IMSI;
		return this;
	}

	public String getB_Name()
	{
		return this.b_Name;
	}

	public WholesalerPerformanceReportResultEntry setB_Name(String b_Name)
	{
		this.b_Name = b_Name;
		return this;
	}

	public String getB_TierName()
	{
		return this.b_TierName;
	}

	public WholesalerPerformanceReportResultEntry setB_TierName(String b_TierName)
	{
		this.b_TierName = b_TierName;
		return this;
	}

	public String getB_GroupName()
	{
		return this.b_GroupName;
	}

	public WholesalerPerformanceReportResultEntry setB_GroupName(String b_GroupName)
	{
		this.b_GroupName = b_GroupName;
		return this;
	}

	public String getB_ServiceClassName()
	{
		return this.b_ServiceClassName;
	}

	public WholesalerPerformanceReportResultEntry setB_ServiceClassName(String b_ServiceClassName)
	{
		this.b_ServiceClassName = b_ServiceClassName;
		return this;
	}

	public String getB_OwnerMobileNumber()
	{
		return this.b_OwnerMobileNumber;
	}

	public WholesalerPerformanceReportResultEntry setB_OwnerMobileNumber(String b_OwnerMobileNumber)
	{
		this.b_OwnerMobileNumber = b_OwnerMobileNumber;
		return this;
	}

	public String getB_OwnerImsi()
	{
		return this.b_OwnerImsi;
	}

	public WholesalerPerformanceReportResultEntry setB_OwnerImsi(String b_OwnerImsi)
	{
		this.b_OwnerImsi = b_OwnerImsi;
		return this;
	}

	public String getB_OwnerName()
	{
		return this.b_OwnerName;
	}

	public WholesalerPerformanceReportResultEntry setB_OwnerName(String b_OwnerName)
	{
		this.b_OwnerName = b_OwnerName;
		return this;
	}

	////////////////////////////////////
	//// Aggregate fields
	////////////////////////////////////

	public BigDecimal getTotalAmount()
	{
		return this.totalAmount;
	}

	public WholesalerPerformanceReportResultEntry setTotalAmount(BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
		return this;
	}

	public BigDecimal getTotalBonus()
	{
		return this.totalBonus;
	}

	public WholesalerPerformanceReportResultEntry setTotalBonus(BigDecimal totalBonus)
	{
		this.totalBonus = totalBonus;
		return this;
	}

	public Integer getTransactionCount()
	{
		return this.transactionCount;
	}

	public WholesalerPerformanceReportResultEntry setTransactionCount(Integer transactionCount)
	{
		this.transactionCount = transactionCount;
		return this;
	}

}

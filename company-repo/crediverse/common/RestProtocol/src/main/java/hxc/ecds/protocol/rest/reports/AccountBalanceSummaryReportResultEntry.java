package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountBalanceSummaryReportResultEntry
{
	private String msisdn;
	private String name;
	private BigDecimal balance;
	private BigDecimal bonusBalance;
	private BigDecimal holdBalance;
	private String tierName;
	private String groupName;

	public String describe(String extra)
	{
		return String.format("%s@%s(msisdn = %s, name = %s, balance = %s, bonusBalance = %s, holdBalance = %s, tierName = %s, groupName = %s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			msisdn, name, balance, bonusBalance, holdBalance, tierName, groupName, (extra.isEmpty() ? "" : ", "), extra);
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
			msisdn,
			name,
			balance,
			bonusBalance,
			holdBalance,
			tierName,
			groupName
		);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof AccountBalanceSummaryReportResultEntry))
            return false;
        AccountBalanceSummaryReportResultEntry otherTyped = (AccountBalanceSummaryReportResultEntry) other;
        return ( true
			&& Objects.equals(this.msisdn, otherTyped.msisdn)
			&& Objects.equals(this.name, otherTyped.name)
			&& Objects.equals(this.balance, otherTyped.balance)
			&& Objects.equals(this.bonusBalance, otherTyped.bonusBalance)
			&& Objects.equals(this.holdBalance, otherTyped.holdBalance)
			&& Objects.equals(this.tierName, otherTyped.tierName)
			&& Objects.equals(this.groupName, otherTyped.groupName)
		);
    }

	public AccountBalanceSummaryReportResultEntry()
	{
	}

	public AccountBalanceSummaryReportResultEntry(AccountBalanceSummaryReportResultEntry other)
	{
		this.msisdn = other.msisdn;
		this.name = other.name;
		this.balance = other.balance;
		this.bonusBalance = other.bonusBalance;
		this.holdBalance = other.holdBalance;
		//this.tierType = other.tierType;
		this.tierName = other.tierName;
		this.groupName = other.groupName;
	}

	public String getMsisdn()
	{
		return this.msisdn;
	}

	public AccountBalanceSummaryReportResultEntry setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
		return this;
	}
	
	public String getName()
	{
		return this.name;
	}

	public AccountBalanceSummaryReportResultEntry setName(String name)
	{
		this.name = name;
		return this;
	}
	
	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public AccountBalanceSummaryReportResultEntry setBalance(BigDecimal balance)
	{
		this.balance = balance;
		return this;
	}

	public BigDecimal getBonusBalance()
	{
		return this.bonusBalance;
	}

	public AccountBalanceSummaryReportResultEntry setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
		return this;
	}

	public BigDecimal getHoldBalance()
	{
		return this.holdBalance;
	}

	public AccountBalanceSummaryReportResultEntry setHoldBalance(BigDecimal holdBalance)
	{
		this.holdBalance = holdBalance;
		return this;
	}

	public String getTierName()
	{
		return this.tierName;
	}

	public AccountBalanceSummaryReportResultEntry setTierName(String tierName)
	{
		this.tierName = tierName;
		return this;
	}
	
	public String getGroupName()
	{
		return this.groupName;
	}

	public AccountBalanceSummaryReportResultEntry setGroupName(String groupName)
	{
		this.groupName = groupName;
		return this;
	}
	

}

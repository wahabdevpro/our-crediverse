package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;

public class AgentAccountEx extends hxc.ecds.protocol.rest.Agent
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// From Account
	protected BigDecimal balance = BigDecimal.ZERO;
	protected BigDecimal bonusBalance = BigDecimal.ZERO;
	protected Date day;
	protected int dayCount = 0;
	protected BigDecimal dayTotal = BigDecimal.ZERO;
	protected int monthCount = 0;
	protected BigDecimal monthTotal = BigDecimal.ZERO;

	// From Joins
	protected String tierName;
	protected String tierType;
	protected String groupName;
	protected String serviceClassName;
	protected String supplierFirstName;
	protected String supplierSurname;
	protected String ownerFirstName;
	protected String ownerSurname;
	protected String roleName;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public BigDecimal getBalance()
	{
		return balance;
	}

	public AgentAccountEx setBalance(BigDecimal balance)
	{
		this.balance = balance;
		return this;
	}

	public BigDecimal getBonusBalance()
	{
		return bonusBalance;
	}

	public AgentAccountEx setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
		return this;
	}

	public Date getDay()
	{
		return day;
	}

	public AgentAccountEx setDay(Date day)
	{
		this.day = day;
		return this;
	}

	public int getDayCount()
	{
		return dayCount;
	}

	public AgentAccountEx setDayCount(int dayCount)
	{
		this.dayCount = dayCount;
		return this;
	}

	public BigDecimal getDayTotal()
	{
		return dayTotal;
	}

	public AgentAccountEx setDayTotal(BigDecimal dayTotal)
	{
		this.dayTotal = dayTotal;
		return this;
	}

	public int getMonthCount()
	{
		return monthCount;
	}

	public AgentAccountEx setMonthCount(int monthCount)
	{
		this.monthCount = monthCount;
		return this;
	}

	public BigDecimal getMonthTotal()
	{
		return monthTotal;
	}

	public AgentAccountEx setMonthTotal(BigDecimal monthTotal)
	{
		this.monthTotal = monthTotal;
		return this;
	}

	public String getTierName()
	{
		return tierName;
	}

	public AgentAccountEx setTierName(String tierName)
	{
		this.tierName = tierName;
		return this;
	}

	public String getTierType()
	{
		return tierType;
	}

	public AgentAccountEx setTierType(String tierType)
	{
		this.tierType = tierType;
		return this;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public AgentAccountEx setGroupName(String groupName)
	{
		this.groupName = groupName;
		return this;
	}

	public String getServiceClassName()
	{
		return serviceClassName;
	}

	public AgentAccountEx setServiceClassName(String serviceClassName)
	{
		this.serviceClassName = serviceClassName;
		return this;
	}

	public String getSupplierFirstName()
	{
		return supplierFirstName;
	}

	public AgentAccountEx setSupplierFirstName(String supplierFirstName)
	{
		this.supplierFirstName = supplierFirstName;
		return this;
	}

	public String getSupplierSurname()
	{
		return supplierSurname;
	}

	public AgentAccountEx setSupplierSurname(String supplierSurname)
	{
		this.supplierSurname = supplierSurname;
		return this;
	}

	public String getOwnerFirstName()
	{
		return ownerFirstName;
	}

	public AgentAccountEx setOwnerFirstName(String ownerFirstName)
	{
		this.ownerFirstName = ownerFirstName;
		return this;
	}

	public String getOwnerSurname()
	{
		return ownerSurname;
	}

	public AgentAccountEx setOwnerSurname(String ownerSurname)
	{
		this.ownerSurname = ownerSurname;
		return this;
	}
	
	public String getRoleName()
	{
		return roleName;
	}

	public AgentAccountEx setRoleName(String roleName)
	{
		this.roleName = roleName;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default
	public AgentAccountEx()
	{
	}

	public AgentAccountEx(hxc.ecds.protocol.rest.Agent agent, hxc.ecds.protocol.rest.Account account)
	{
		super(agent);
		this.balance = account.getBalance();
		this.bonusBalance = account.getBonusBalance();
		this.day = account.getDay();
		this.dayCount = account.getDayCount();
		this.dayTotal = account.getDayTotal();
		this.monthCount = account.getMonthCount();
		this.monthTotal = account.getMonthTotal();
		this.tamperedWith |= account.isTamperedWith();
	}

}

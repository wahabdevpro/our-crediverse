package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Account implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected Integer id;
	protected Integer agentID;
	protected int version;
	protected BigDecimal balance = BigDecimal.ZERO;
	protected BigDecimal bonusBalance = BigDecimal.ZERO;
	protected BigDecimal onHoldBalance = BigDecimal.ZERO;
	protected long signature;
	protected boolean tamperedWith = false;

	// AML Limits
	protected Date day;
	protected int dayCount = 0;
	protected BigDecimal dayTotal = BigDecimal.ZERO;
	protected int monthCount = 0;
	protected BigDecimal monthTotal = BigDecimal.ZERO;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Integer getID()
	{
		return agentID;
	}

	public Account setID(Integer id)
	{
		this.id = id;
		return this;
	}
	
	public Integer getAgentID()
	{
		return agentID;
	}

	public Account setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Account setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public BigDecimal getBalance()
	{
		return balance;
	}

	public Account setBalance(BigDecimal balance)
	{
		this.balance = balance;
		return this;
	}

	public BigDecimal getBonusBalance()
	{
		return bonusBalance;
	}

	public Account setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
		return this;
	}
	

	public BigDecimal getOnHoldBalance()
	{
		return onHoldBalance;
	}

	public Account setOnHoldBalance(BigDecimal onHoldBalance)
	{
		this.onHoldBalance = onHoldBalance;
		return this;
	}

	public long getSignature()
	{
		return signature;
	}

	public Account setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	public Account setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}

	public Date getDay()
	{
		return day;
	}

	public Account setDay(Date day)
	{
		this.day = day;
		return this;
	}

	public int getDayCount()
	{
		return dayCount;
	}

	public Account setDayCount(int dayCount)
	{
		this.dayCount = dayCount;
		return this;
	}

	public BigDecimal getDayTotal()
	{
		return dayTotal;
	}

	public Account setDayTotal(BigDecimal dayTotal)
	{
		this.dayTotal = dayTotal;
		return this;
	}

	public int getMonthCount()
	{
		return monthCount;
	}

	public Account setMonthCount(int monthCount)
	{
		this.monthCount = monthCount;
		return this;
	}

	public BigDecimal getMonthTotal()
	{
		return monthTotal;
	}

	public Account setMonthTotal(BigDecimal monthTotal)
	{
		this.monthTotal = monthTotal;
		return this;
	}

	@Override
	public List<Violation> validate()
	{
		return new Validator() //
				.isTrue("agentID", agentID > 0, "Invalid AgentID") //

				.notNull("balance", balance) //
				.notNull("bonusBalance", bonusBalance) //
				.notNull("onHoldBalance", onHoldBalance) //
				.notNull("day", day) //
				.notNull("dayTotal", dayTotal) //
				.notNull("monthTotal", monthTotal) //

				.notLess("balance", balance, BigDecimal.ZERO) //
				.notLess("onHoldBalance", onHoldBalance, BigDecimal.ZERO) //
				.notLess("dayTotal", dayTotal, BigDecimal.ZERO) //
				.notLess("monthTotal", monthTotal, BigDecimal.ZERO) //
				.notLess("monthTotal", monthTotal, dayTotal) //
				.notLess("dayCount", dayCount, 0) //
				.notLess("monthCount", monthCount, 0) //
				.notLess("monthCount", monthCount, dayCount) //
				.isMoney("balance", balance) //
				.isMoney("bonusBalance", bonusBalance) //
				.isMoney("onHoldBalance", onHoldBalance) //
				.isMoney("dayTotal", dayTotal) //
				.isMoney("monthTotal", monthTotal) //
				.toList();
	}

}

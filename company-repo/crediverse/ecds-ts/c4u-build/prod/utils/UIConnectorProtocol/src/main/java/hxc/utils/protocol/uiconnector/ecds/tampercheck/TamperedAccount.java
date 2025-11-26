package hxc.utils.protocol.uiconnector.ecds.tampercheck;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import hxc.connectors.ecds.tampercheck.ITamperedAccount;

public class TamperedAccount implements ITamperedAccount, Serializable
{
	private static final long serialVersionUID = -6500704348141964523L;
	protected Integer agentId = 0;
	protected String agentMsisdn = "";
	protected BigDecimal balance = BigDecimal.ZERO;
	protected BigDecimal bonusBalance = BigDecimal.ZERO;
	//protected Integer id; 
	protected Long signature;
	// AML Limits
	protected Date day;
	protected Integer dayCount;
	protected BigDecimal dayTotal;
	protected Integer monthCount;
	protected BigDecimal monthTotal;

	@Override
	public Integer getAgentId() 
	{
		return agentId;
	}
	
	public void setAgentId(Integer agentId)
	{
		this.agentId = agentId;
	}
	
	@Override
	public String getAgentMsisdn() 
	{
		return agentMsisdn;
	}
	
	public void setAgentMsisdn(String agentMsisdn)
	{
		this.agentMsisdn = agentMsisdn;
	}
	
	@Override
	public BigDecimal getBalance() 
	{
		return balance;
	}
	
	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}
	
	@Override
	public BigDecimal getBonusBalance() 
	{
		return bonusBalance;
	}
	
	public void setBonusBalance(BigDecimal bonusBalance)
	{
		this.bonusBalance = bonusBalance;
	}
	
	public TamperedAccount()
	{
	}

	@Override
	public Long getSignature()
	{
		return signature;
	}

	public void setSignature(Long signature)
	{
		this.signature = signature;
	}

	@Override
	public Date getDay()
	{
		return day;
	}

	public void setDay(Date day)
	{
		this.day = day;
	}

	@Override
	public Integer getDayCount()
	{
		return dayCount;
	}

	public void setDayCount(Integer dayCount)
	{
		this.dayCount = dayCount;
	}

	@Override
	public BigDecimal getDayTotal()
	{
		return dayTotal;
	}

	public void setDayTotal(BigDecimal dayTotal)
	{
		this.dayTotal = dayTotal;
	}

	@Override
	public Integer getMonthCount()
	{
		return monthCount;
	}

	public void setMonthCount(Integer monthCount)
	{
		this.monthCount = monthCount;
	}

	@Override
	public BigDecimal getMonthTotal()
	{
		return monthTotal;
	}

	public void setMonthTotal(BigDecimal monthTotal)
	{
		this.monthTotal = monthTotal;
	}	
}

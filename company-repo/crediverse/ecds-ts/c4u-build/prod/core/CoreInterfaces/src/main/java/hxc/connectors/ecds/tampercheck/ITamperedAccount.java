package hxc.connectors.ecds.tampercheck;

import java.math.BigDecimal;
import java.util.Date;

public interface ITamperedAccount { //Change to ITamperedAccount

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	//public abstract Integer getId();
	public abstract Integer getAgentId();
	public abstract String getAgentMsisdn();
	public abstract BigDecimal getBalance();
	public abstract BigDecimal getBonusBalance(); 
	public abstract Long getSignature();
	// AML Limits
	public abstract Date getDay();
	public abstract Integer getDayCount();
	public abstract BigDecimal getDayTotal();
	public abstract Integer getMonthCount();
	public abstract BigDecimal getMonthTotal();
	
	/*
	agentID
	balance
	bonusBalance
	day
	dayCount
	dayTotal
	monthCount
	monthTotal
	*/
}

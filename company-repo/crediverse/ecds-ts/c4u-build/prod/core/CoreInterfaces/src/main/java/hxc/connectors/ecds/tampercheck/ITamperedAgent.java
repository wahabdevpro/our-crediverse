package hxc.connectors.ecds.tampercheck;

import java.math.BigDecimal;
import java.util.Date;

public interface ITamperedAgent 
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract Integer getId();	
	public abstract Integer getCompanyID();
	public abstract String getAccountNumber();
	public abstract String getMobileNumber();
	public abstract String getImei();	
	public abstract String getImsi();
	public abstract Date getLastImsiChange();
	public abstract String getTitle();
	public abstract String getFirstName();
	public abstract String getInitials();
	public abstract String getSurname();
	public abstract String getLanguage();
	public abstract String getDomainAccountName();
	public abstract Integer getTierID();
	public abstract Integer getGroupID();
	public abstract Integer getAreaID();
	public abstract Integer getServiceClassID();
	public abstract String getState();
	public abstract Date getActivationDate();
	public abstract Date getDeactivationDate();
	public abstract Date getExpirationDate();
	public abstract Integer getSupplierAgentID();
	public abstract Integer getAllowedChannels();
	public abstract BigDecimal getWarningThreshold();
	// AML Limits
	public abstract BigDecimal getMaxTransactionAmount();
	public abstract Integer getMaxDailyCount();
	public abstract BigDecimal getMaxDailyAmount();
	public abstract Integer getMaxMonthlyCount();
	public abstract BigDecimal getMaxMonthlyAmount();

	// Security
	public abstract boolean getTemporaryPin();
	public abstract Long getSignature();
	
	
	public abstract byte[] getKey1();
	public abstract byte[] getKey2();	
	public abstract byte[] getKey3();
	/*
	.add(accountNumber)
	.add(mobileNumber)
	.add(imei)
	.add(imsi)
	.add(title)
	.add(firstName)
	.add(initials)
	.add(surname)
	.add(language)
	.add(domainAccountName)
	.add(tierID)
	.add(groupID)
	.add(areaID)
	.add(serviceClassID)
	.add(state)
	.add(activationDate)
	.add(deactivationDate)
	.add(expirationDate)
	.add(supplierAgentID)
	.add(allowedChannels)
	.add(warningThreshold)
	.add(maxTransactionAmount)
	.add(maxDailyCount)
	.add(maxDailyAmount)
	.add(maxMonthlyCount)
	.add(maxMonthlyAmount)
	.add(temporaryPin)
	.add(lastImsiChange)
	.add(key3)
	.add(key1)
	.add(key2)
	.signature();*/
}
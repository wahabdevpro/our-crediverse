package hxc.utils.protocol.uiconnector.ecds.tampercheck;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import hxc.connectors.ecds.tampercheck.ITamperedAgent;

public class TamperedAgent implements ITamperedAgent, Serializable
{
	private static final long serialVersionUID = -6500704348141964522L;

	protected Integer id;
	protected Integer companyID;
	
	protected String accountNumber;//
	protected String mobileNumber;//
	protected String imei;//	
	protected String imsi;//
	protected Date lastImsiChange;//
	protected String title;//
	protected String firstName;//
	protected String initials;//
	protected String surname;//
	protected String language;//
	protected String domainAccountName;//
	
	protected Integer tierID;
	protected Integer groupID;
	protected Integer areaID;
	protected Integer serviceClassID;

	protected String state;
	protected Date activationDate;
	protected Date deactivationDate;
	protected Date expirationDate;
	protected Integer supplierAgentID;
	protected Integer ownerAgentID;
	protected int allowedChannels;
	protected BigDecimal warningThreshold;

	// AML Limits
	protected BigDecimal maxTransactionAmount;
	protected Integer maxDailyCount;
	protected BigDecimal maxDailyAmount;
	protected Integer maxMonthlyCount;
	protected BigDecimal maxMonthlyAmount;

	// Security
	protected boolean temporaryPin = true;
	protected long signature;
	
	protected byte[] key1;	
	protected byte[] key2;
	protected byte[] key3;
	
	
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

	public TamperedAgent()
	{
	}

	@Override
	public Integer getId() 
	{
		return id;
	}

	public void setId(Integer id) 
	{
		this.id = id;
	}

	@Override
	public Integer getCompanyID() 
	{
		return companyID;
	}

	public void setCompanyID(Integer companyID) 
	{
		this.companyID = companyID;
	}

	@Override
	public String getAccountNumber() 
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) 
	{
		this.accountNumber = accountNumber;
	}

	@Override
	public String getMobileNumber() 
	{
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) 
	{
		this.mobileNumber = mobileNumber;
	}

	@Override
	public String getImei() 
	{
		return imei;
	}

	public void setImei(String imei) 
	{
		this.imei = imei;
	}

	@Override
	public String getImsi() 
	{
		return imsi;
	}

	public void setImsi(String imsi) 
	{
		this.imsi = imsi;
	}

	@Override
	public Date getLastImsiChange() 
	{
		return lastImsiChange;
	}

	public void setLastImsiChange(Date lastImsiChange) 
	{
		this.lastImsiChange = lastImsiChange;
	}

	@Override
	public String getTitle() 
	{
		return title;
	}

	public void setTitle(String title) 
	{
		this.title = title;
	}

	@Override
	public String getFirstName() 
	{
		return firstName;
	}

	public void setFirstName(String firstName) 
	{
		this.firstName = firstName;
	}

	@Override
	public String getInitials() 
	{
		return initials;
	}

	public void setInitials(String initials) 
	{
		this.initials = initials;
	}

	@Override
	public String getSurname() 
	{
		return surname;
	}

	public void setSurname(String surname) 
	{
		this.surname = surname;
	}

	@Override
	public String getLanguage() 
	{
		return language;
	}

	public void setLanguage(String language) 
	{
		this.language = language;
	}

	@Override
	public String getDomainAccountName() 
	{
		return domainAccountName;
	}

	public void setDomainAccountName(String domainAccountName) 
	{
		this.domainAccountName = domainAccountName;
	}

	@Override
	public Integer getTierID() 
	{
		return tierID;
	}

	public void setTierID(Integer tierID) 
	{
		this.tierID = tierID;
	}

	@Override
	public Integer getGroupID() 
	{
		return groupID;
	}

	public void setGroupID(Integer IntegerupID) 
	{
		this.groupID = groupID;
	}

	@Override
	public Integer getAreaID() 
	{
		return areaID;
	}

	public void setAreaID(Integer areaID) 
	{
		this.areaID = areaID;
	}

	@Override
	public Integer getServiceClassID() 
	{
		return serviceClassID;
	}

	public void setServiceClassID(Integer serviceClassID) 
	{
		this.serviceClassID = serviceClassID;
	}

	@Override
	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	@Override
	public Date getActivationDate()
	{
		return activationDate;
	}

	public void setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
	}

	@Override
	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	public void setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
	}

	@Override
	public Date getExpirationDate() 
	{
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) 
	{
		this.expirationDate = expirationDate;
	}

	@Override
	public Integer getSupplierAgentID() 
	{
		return supplierAgentID;
	}

	public void setSupplierAgentID(Integer supplierAgentID) 
	{
		this.supplierAgentID = supplierAgentID;
	}

	@Override
	public Integer getAllowedChannels() 
	{
		return allowedChannels;
	}

	public void setAllowedChannels(int allowedChannels) 
	{
		this.allowedChannels = allowedChannels;
	}

	@Override
	public BigDecimal getWarningThreshold() 
	{
		return warningThreshold;
	}

	public void setWarningThreshold(BigDecimal warningThreshold) 
	{
		this.warningThreshold = warningThreshold;
	}

	@Override
	public BigDecimal getMaxTransactionAmount() 
	{
		return maxTransactionAmount;
	}

	public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) 
	{
		this.maxTransactionAmount = maxTransactionAmount;
	}

	@Override
	public Integer getMaxDailyCount() 
	{
		return maxDailyCount;
	}

	public void setMaxDailyCount(Integer maxDailyCount) 
	{
		this.maxDailyCount = maxDailyCount;
	}

	@Override
	public BigDecimal getMaxDailyAmount() 
	{
		return maxDailyAmount;
	}

	public void setMaxDailyAmount(BigDecimal maxDailyAmount) 
	{
		this.maxDailyAmount = maxDailyAmount;
	}

	@Override
	public Integer getMaxMonthlyCount() 
	{
		return maxMonthlyCount;
	}

	public void setMaxMonthlyCount(Integer maxMonthlyCount) 
	{
		this.maxMonthlyCount = maxMonthlyCount;
	}

	@Override
	public BigDecimal getMaxMonthlyAmount() 
	{
		return maxMonthlyAmount;
	}

	public void setMaxMonthlyAmount(BigDecimal maxMonthlyAmount) 
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
	}	

	@Override
	public Long getSignature() 
	{
		return signature;
	}

	public void setSignature(long signature) 
	{
		this.signature = signature;
	}

	@Override
	public boolean getTemporaryPin() 
	{
		return temporaryPin;
	}
	
	public void setTemporaryPin(boolean temporaryPin) 
	{
		this.temporaryPin = temporaryPin;
	}

	@Override
	public byte[] getKey1() 
	{
		return key1;
	}

	public void setKey1(byte[] key1) 
	{
		this.key1 = key1;
	}

	@Override
	public byte[] getKey2() 
	{
		return key2;
	}

	public void setKey2(byte[] key2) 
	{
		this.key2 = key2;
	}

	@Override
	public byte[] getKey3() 
	{
		return key3;
	}

	public void setKey3(byte[] key3) 
	{
		this.key3 = key3;
	}
}

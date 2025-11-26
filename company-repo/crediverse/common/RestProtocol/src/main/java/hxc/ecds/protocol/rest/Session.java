package hxc.ecds.protocol.rest;

import java.util.Date;

public class Session
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String sessionID;
	protected Integer webUserID;
	protected Integer agentID;
	protected Integer agentUserID;
	protected int companyID;
	protected Date expiryTime;
	protected String domainAccountName;
	protected String languageID;
	protected String countryID;
	protected String companyPrefix;
	protected Integer ownerAgentID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getSessionID()
	{
		return sessionID; 
	}

	public Session setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
		return this;
	}

	public Integer getWebUserID()
	{
		return webUserID;
	}

	public Session setWebUserID(Integer webUserID)
	{
		this.webUserID = webUserID;
		return this;
	}

	public Integer getAgentID()
	{
		return agentID;
	}

	public Session setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public Integer getAgentUserID()
	{
		return agentUserID;
	}

	public Session setAgentUserID(Integer agentUserID)
	{
		this.agentUserID = agentUserID;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Session setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public Date getExpiryTime()
	{
		return expiryTime;
	}

	public Session setExpiryTime(Date expiryTime)
	{
		this.expiryTime = expiryTime;
		return this;
	}

	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	public Session setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public String getLanguageID()
	{
		return languageID;
	}

	public Session setLanguageID(String languageID)
	{
		this.languageID = languageID;
		return this;
	}

	public String getCountryID()
	{
		return countryID;
	}

	public Session setCountryID(String countryID)
	{
		this.countryID = countryID;
		return this;
	}

	public String getCompanyPrefix()
	{
		return companyPrefix;
	}

	public Session setCompanyPrefix(String companyPrefix)
	{
		this.companyPrefix = companyPrefix;
		return this;
	}

	public Integer getOwnerAgentID()
	{
		return ownerAgentID;
	}

	public Session setOwnerAgentID(Integer ownerAgentID)
	{
		this.ownerAgentID = ownerAgentID;
		return this;
	}

}

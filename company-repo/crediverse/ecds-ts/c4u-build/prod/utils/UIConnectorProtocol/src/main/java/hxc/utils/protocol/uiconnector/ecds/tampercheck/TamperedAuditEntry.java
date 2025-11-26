package hxc.utils.protocol.uiconnector.ecds.tampercheck;

import java.io.Serializable;
import java.util.Date;

import hxc.connectors.ecds.tampercheck.ITamperedAuditEntry;

public class TamperedAuditEntry implements ITamperedAuditEntry, Serializable
{
	private static final long serialVersionUID = -1511704348141964524L;
	protected Boolean isTampered;
	protected Long id;
	protected Integer companyId;
	protected String sequenceNo;
	protected Date timestamp;
	protected Integer userID;
	protected Integer agentUserId; 
	protected String ipAddress;
	protected String macAddress;
	protected String machineName;
	protected String domainName;
	protected String dataType;
	protected String action;
	protected String oldValue;
	protected String newValue;
	protected Long signature;

	@Override
	public Boolean isTampered() 
	{
		return isTampered;
	}

	public void setTampered(Boolean isTampered) 
	{
		this.isTampered = isTampered;
	}
	
	@Override
	public Long getId() 
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}	
	
	@Override
	public Integer getCompanyId()
	{
		return this.companyId;
	}
	
	public void setCompanyId(Integer companyId)
	{
		this.companyId = companyId;
	}
	
	@Override
	public String getSequenceNo()
	{
		return this.sequenceNo;
	}
	
	public void setSequenceNo(String sequenceNo)
	{
		this.sequenceNo = sequenceNo;
	}

	@Override
	public Date getTimestamp()
	{
		return this.timestamp;
	}
	
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	@Override
	public Integer getUserId()
	{
		return this.userID;
	}
	
	public void setUserId(Integer userID)
	{
		this.userID = userID;
	}
	
	@Override
	public String getIpAddress()
	{
		return this.ipAddress;
	}
	
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	
	@Override
	public String getMacAddress()
	{
		return this.macAddress;
	}
	
	public void setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
	}

	@Override
	public String getMachineName()
	{
		return this.machineName;
	}
	
	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
	
	@Override
	public String getDomainName()
	{
		return this.domainName;
	}
	
	public void setDomainName(String domainName)
	{
		this.domainName = domainName;
	}
	
	@Override
	public String getDataType()
	{
		return this.dataType;
	}
	
	public void setDataType(String dataType)
	{
		this.dataType = dataType;
	}
	
	@Override
	public String getAction()
	{
		return this.action;
	}
	
	public void setAction(String action)
	{
		this.action = action;
	}
	
	@Override
	public String getOldValue()
	{
		return this.oldValue;
	}
	
	public void setOldValue(String oldValue)
	{
		this.oldValue = oldValue;
	}

	@Override
	public String getNewValue()
	{
		return this.newValue;
	}
	
	public void setNewValue(String newValue)
	{
		this.newValue = newValue;
	}

	@Override
	public Long getSignature()
	{
		return this.signature;
	}
	
	public void setSignature(Long signature)
	{
		this.signature = signature;
	}
	
	public TamperedAuditEntry()
	{
	}
}

package hxc.utils.protocol.uiconnector.ecds.tampercheck;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import hxc.connectors.ecds.tampercheck.ITamperedBatch;

public class TamperedBatch implements ITamperedBatch, Serializable
{
	private static final long serialVersionUID = -2722704348141964525L;
	protected Boolean isTampered;
	protected Integer id;
	protected Integer companyID;
	protected String filename;
	protected Integer insertCount;
	protected Integer updateCount;
	protected Integer deleteCount;
	protected Integer failureCount;
	protected BigDecimal totalValue;
	protected String type;
	protected Long fileSize;
	protected Date timestamp;
	protected Integer webUserID;
	protected String ipAddress;
	protected String macAddress;
	protected String machineName;
	protected String domainName;
	protected Long signature;
	
	public TamperedBatch()
	{
	}
	
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
	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	@Override
	public Integer getInsertCount()
	{
		return insertCount;
	}

	public void setInsertCount(Integer insertCount)
	{
		this.insertCount = insertCount;
	}

	@Override
	public Integer getUpdateCount()
	{
		return updateCount;
	}

	public void setUpdateCount(Integer updateCount)
	{
		this.updateCount = updateCount;
	}

	@Override
	public Integer getDeleteCount()
	{
		return deleteCount;
	}

	public void setDeleteCount(Integer deleteCount)
	{
		this.deleteCount = deleteCount;
	}

	@Override
	public Integer getFailureCount()
	{
		return failureCount;
	}

	public void setFailureCount(Integer failureCount)
	{
		this.failureCount = failureCount;
	}

	@Override
	public BigDecimal getTotalValue()
	{
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue)
	{
		this.totalValue = totalValue;
	}

	@Override
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public Long getFileSize()
	{
		return fileSize;
	}

	public void setFileSize(Long fileSize)
	{
		this.fileSize = fileSize;
	}

	@Override
	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public Integer getWebUserID()
	{
		return webUserID;
	}

	public void setWebUserID(Integer webUserID)
	{
		this.webUserID = webUserID;
	}

	@Override
	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	@Override
	public String getMacAddress()
	{
		return macAddress;
	}

	public void setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
	}

	@Override
	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	@Override
	public String getDomainName()
	{
		return domainName;
	}

	public void setDomainName(String domainName)
	{
		this.domainName = domainName;
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
}

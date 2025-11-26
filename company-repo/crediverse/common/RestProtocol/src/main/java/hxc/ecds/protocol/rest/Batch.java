package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;

public class Batch
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int FILENAME_MAX_LENGTH = 40;
	public static final int TYPE_MAX_LENGTH = 10;
	
	// States inferable by TS
	public static final String STATE_PROCESSING = "P";
	public static final String STATE_COMPLETED = "C";
	public static final String STATE_FAILED = "F";
	
	// Other states not inferable by TS which can be set via BatchUploadRequest
	public static final String STATE_UPLOADING = "L";
	public static final String STATE_CANCELLED = "X";
	public static final String STATE_DECLINED = "D";
	public static final String STATE_AUTHORIZING = "A";
	public static final String STATE_SUSPENDED = "S";
	public static final String STATE_UNKNOWN = "U";
	public static final String STATE_TESTING = "T";
	// etc

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String filename;
	protected boolean completed;
	protected int lineCount;
	protected int insertCount;
	protected int updateCount;
	protected int deleteCount;
	protected int failureCount;
	protected BigDecimal totalValue;
	protected BigDecimal totalValue2;
	protected String type;
	protected Long fileSize;
	protected Date timestamp;
	protected int webUserID;
	protected Integer coAuthWebUserID;
	protected String ipAddress;
	protected String macAddress;
	protected String machineName;
	protected String domainName;
	protected long signature;
	protected boolean tamperedWith = false;
	protected String state = STATE_UNKNOWN;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Batch setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Batch setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Batch setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getFilename()
	{
		return filename;
	}

	public Batch setFilename(String filename)
	{
		this.filename = filename;
		return this;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public Batch setCompleted(boolean completed)
	{
		this.completed = completed;
		return this;
	}

	public int getLineCount()
	{
		return lineCount;
	}

	public Batch setLineCount(int lineCount)
	{
		this.lineCount = lineCount;
		return this;
	}

	public int getInsertCount()
	{
		return insertCount;
	}

	public Batch setInsertCount(int insertCount)
	{
		this.insertCount = insertCount;
		return this;
	}

	public int getUpdateCount()
	{
		return updateCount;
	}

	public Batch setUpdateCount(int updateCount)
	{
		this.updateCount = updateCount;
		return this;
	}

	public int getDeleteCount()
	{
		return deleteCount;
	}

	public Batch setDeleteCount(int deleteCount)
	{
		this.deleteCount = deleteCount;
		return this;
	}

	public int getFailureCount()
	{
		return failureCount;
	}

	public Batch setFailureCount(int failureCount)
	{
		this.failureCount = failureCount;
		return this;
	}

	public BigDecimal getTotalValue()
	{
		return totalValue;
	}

	public Batch setTotalValue(BigDecimal totalValue)
	{
		this.totalValue = totalValue;
		return this;
	}
	
	public BigDecimal getTotalValue2()
	{
		return totalValue2;
	}

	public Batch setTotalValue2(BigDecimal totalValue2)
	{
		this.totalValue2 = totalValue2;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Batch setType(String type)
	{
		this.type = type;
		return this;
	}

	public Long getFileSize()
	{
		return fileSize;
	}

	public Batch setFileSize(Long fileSize)
	{
		this.fileSize = fileSize;
		return this;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public Batch setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	public int getWebUserID()
	{
		return webUserID;
	}

	public Batch setWebUserID(int webUserID)
	{
		this.webUserID = webUserID;
		return this;
	}

	public Integer getCoAuthWebUserID()
	{
		return coAuthWebUserID;
	}

	public Batch setCoAuthWebUserID(Integer coAuthWebUserID)
	{
		this.coAuthWebUserID = coAuthWebUserID;
		return this;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public Batch setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
		return this;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public Batch setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
		return this;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public Batch setMachineName(String machineName)
	{
		this.machineName = machineName;
		return this;
	}

	public String getDomainName()
	{
		return domainName;
	}

	public Batch setDomainName(String domainName)
	{
		this.domainName = domainName;
		return this;
	}

	public long getSignature()
	{
		return signature;
	}

	public Batch setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	public Batch setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Batch setState(String state)
	{
		this.state = state;
		return this;
	}
	
	

}

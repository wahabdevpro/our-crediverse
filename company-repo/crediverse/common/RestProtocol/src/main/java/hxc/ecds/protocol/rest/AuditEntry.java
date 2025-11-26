package hxc.ecds.protocol.rest;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class AuditEntry implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String ACTION_CREATE = "C";
	public static final String ACTION_UPDATE = "U";
	public static final String ACTION_DELETE = "D";

	public static final String TYPE_WEB_USER = "WebUser";
	public static final String TYPE_AUDIT_ENTRY = "AuditEntry";
	public static final String TYPE_COMPANY = "Company";
	public static final String TYPE_PERMISSION = "Permission";
	public static final String TYPE_ROLE = "Role";
	public static final String TYPE_TIER = "Tier";
	public static final String TYPE_AGENT = "Agent";
	public static final String TYPE_TRANSFER_RULE = "TransferRule";
	public static final String TYPE_SCLASS = "ServiceClass";
	public static final String TYPE_GROUP = "Group";
	public static final String TYPE_CONFIG = "Configuration";
	public static final String TYPE_BATCH = "Batch";
	public static final String TYPE_AREA = "Area";
	public static final String TYPE_CELL = "Cell";
	public static final String TYPE_CELL_GROUP = "Cell Group";
	public static final String TYPE_WORK_ITEM = "WorkItem";
	public static final String TYPE_DEPT = "Department";
	public static final String TYPE_PROMOTION = "Promotion";
	public static final String TYPE_BUNDLE = "Bundle";
	public static final String TYPE_AGENT_USER = "AgentUser";
	public static final String TYPE_REPORT = "Report";
	public static final String TYPE_ANALYTICS = "Analytics";

	public static final int SEQUENCE_NO_MAX_LENGTH = 17;
	public static final int MACHINE_NAME_MAX_LENGTH = 16;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected long id;
	protected int companyID;
	protected int version;
	protected String sequenceNo;
	protected Date timestamp;
	protected int userID; // +'ve = WebUser, -'ve = and 0 = MRD
	protected Integer agentUserID; 
	protected String ipAddress;
	protected String macAddress;
	protected String machineName;
	protected String domainName;
	protected String dataType;
	protected String action;
	protected String oldValue;
	protected String newValue;
	protected long signature;
	protected boolean tamperedWith = false;
	protected String reason;
	protected String reasonDetail;
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public long getId()
	{
		return id;
	}

	public AuditEntry setId(long id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public AuditEntry setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public AuditEntry setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getSequenceNo()
	{
		return sequenceNo;
	}

	public AuditEntry setSequenceNo(String sequenceNo)
	{
		this.sequenceNo = sequenceNo;
		return this;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public AuditEntry setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	public int getUserID()
	{
		return userID;
	}

	public AuditEntry setUserID(int webUserID)
	{
		this.userID = webUserID;
		return this;
	}
	
	public Integer getAgentUserID()
	{
		return agentUserID;
	}

	public AuditEntry setAgentUserID(Integer agentUserID)
	{
		this.agentUserID = agentUserID;
		return this;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public AuditEntry setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
		return this;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public AuditEntry setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
		return this;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public AuditEntry setMachineName(String machineName)
	{
		this.machineName = machineName;
		return this;
	}

	public String getDomainName()
	{
		return domainName;
	}

	public AuditEntry setDomainName(String domainName)
	{
		this.domainName = domainName;
		return this;
	}

	public String getDataType()
	{
		return dataType;
	}

	public AuditEntry setDataType(String dataType)
	{
		this.dataType = dataType;
		return this;
	}

	public String getAction()
	{
		return action;
	}

	public AuditEntry setAction(String action)
	{
		this.action = action;
		return this;
	}

	public String getOldValue()
	{
		return oldValue;
	}

	public AuditEntry setOldValue(String oldValue)
	{
		this.oldValue = oldValue;
		return this;
	}

	public String getNewValue()
	{
		return newValue;
	}

	public AuditEntry setNewValue(String newValue)
	{
		this.newValue = newValue;
		return this;
	}

	public long getSignature()
	{
		return signature;
	}

	public AuditEntry setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	public AuditEntry setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}
	
	public String getReason()
	{
		return this.reason;
	}
	
	public AuditEntry setReason(String reason)
	{
		this.reason = reason;
		return this;
	}
	
	public String getReasonDetail()
	{
		return this.reasonDetail;
	}
	
	public AuditEntry setReasonDetail(String reasonDetail)
	{
		this.reasonDetail = reasonDetail;
		return this;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////
	private static final String ipv4Pattern = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final String ipv6Pattern = "^([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}$";
	private static final String macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

	protected static Pattern VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
	protected static Pattern VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
	protected static Pattern VALID_MAC_PATTERN = Pattern.compile(macPattern, Pattern.CASE_INSENSITIVE);

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //

				.notEmpty("sequenceNo", sequenceNo, SEQUENCE_NO_MAX_LENGTH) //
				.notLess("companyID", companyID, 1) //
				.notNull("timestamp", timestamp) //
				.notEmpty("domainName", domainName, WebUser.DOMAIN_NAME_MAX_LENGTH) //
				.oneOf("dataType", dataType, TYPE_WEB_USER, TYPE_AUDIT_ENTRY, TYPE_COMPANY, //
						TYPE_PERMISSION, TYPE_ROLE, TYPE_TIER, TYPE_TRANSFER_RULE, //
						TYPE_SCLASS, TYPE_GROUP, TYPE_AREA, TYPE_CELL, TYPE_ANALYTICS) //
				.oneOf("action", action, ACTION_CREATE, ACTION_DELETE, ACTION_UPDATE);

		if (ipAddress == null || !VALID_IPV4_PATTERN.matcher(ipAddress).matches() && !VALID_IPV6_PATTERN.matcher(ipAddress).matches())
		{
			validator.append(Violation.INVALID_VALUE, "ipAddress", null, "Invalid IP Address");
		}

		if (macAddress != null && !VALID_MAC_PATTERN.matcher(macAddress).matches())
		{
			validator.append(Violation.INVALID_VALUE, "macAddress", null, "Invalid MAC Address");
		}

		if (machineName != null)
			validator.notEmpty("machineName", machineName, MACHINE_NAME_MAX_LENGTH);

		switch (action)
		{
			case ACTION_CREATE:
				validator.isNull("oldValue", oldValue) //
						.notNull("newValue", newValue);
				break;

			case ACTION_UPDATE:
				validator.notNull("oldValue", oldValue) //
						.notNull("newValue", newValue);
				break;

			case ACTION_DELETE:
				validator.notNull("oldValue", oldValue) //
						.isNull("newValue", newValue);
				break;
		}

		return validator.toList();
	}
}

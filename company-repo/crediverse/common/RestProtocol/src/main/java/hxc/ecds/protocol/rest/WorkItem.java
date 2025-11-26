package hxc.ecds.protocol.rest;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class WorkItem implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int DESCRIPTION_MAX_LENGTH = 255;
	public static final int URI_MAX_LENGTH = 200;
	public static final int REASON_MAX_LENGTH = 150;
	public static final int WORK_TYPE_MAX_LENGTH = 20;
	public static final int STATUS_MAX_LENGTH = 20;
	public static final int SESSION_MAX_LENGTH = 36;

	public static final String STATE_NEW = "N";
	public static final String STATE_IN_PROGRESS = "I";
	public static final String STATE_ON_HOLD = "O";
	public static final String STATE_COMPLETED = "C";
	public static final String STATE_CANCELLED = "X";
	public static final String STATE_DECLINED = "D";
	public static final String STATE_FAILED = "F";

	public static final String ACTIVE_FILTER = "('" + STATE_NEW + "','" + STATE_IN_PROGRESS + "','" + STATE_ON_HOLD + "')";
	public static final String INACTIVE_FILTER = "('" + STATE_COMPLETED + "','" + STATE_CANCELLED + "','" + STATE_DECLINED + "','" + STATE_FAILED + "')";
	
	public static final String TYPE_AUTHENTICATION_REQUEST = "A";
	public static final String TYPE_REVERSAL_REQUEST = "R";
	public static final String TYPE_SCHEDULED_REPORT = "S";
	public static final String TYPE_EXECUTE = "E";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// Identification
	protected int id;
	protected int companyID;
	protected int version;
	protected String description;
	protected UUID uuid;

	// State
	protected String state = STATE_NEW;
	protected String type;
	protected Date creationTime = new Date();
	protected Date completionTime = null;
	protected boolean smsOnChange = false;
	protected String reason;

	// Parties
	protected Integer createdByWebUserID;
	protected Integer createdByAgentID;
	protected Integer createdForWebUserID;
	protected Integer createdForPermissionID;

	// Payload
	protected String uri;
	protected String request;
	protected String response;
	
	// Additional
	protected String workType; 
	protected String status; 
	protected String ownerSession; 

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getId()
	{
		return id;
	}

	public WorkItem setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public WorkItem setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public WorkItem setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public WorkItem setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public WorkItem setUuid(UUID uuid)
	{
		this.uuid = uuid;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public WorkItem setState(String state)
	{
		this.state = state;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public WorkItem setType(String type)
	{
		this.type = type;
		return this;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public WorkItem setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
		return this;
	}

	public Date getCompletionTime()
	{
		return completionTime;
	}

	public WorkItem setCompletionTime(Date completionTime)
	{
		this.completionTime = completionTime;
		return this;
	}

	public boolean isSmsOnChange()
	{
		return smsOnChange;
	}

	public WorkItem setSmsOnChange(boolean smsOnChange)
	{
		this.smsOnChange = smsOnChange;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public WorkItem setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	public Integer getCreatedByWebUserID()
	{
		return createdByWebUserID;
	}

	public WorkItem setCreatedByWebUserID(Integer createdByWebUserID)
	{
		this.createdByWebUserID = createdByWebUserID;
		return this;
	}

	public Integer getCreatedByAgentID()
	{
		return createdByAgentID;
	}

	public WorkItem setCreatedByAgentID(Integer createdByAgentID)
	{
		this.createdByAgentID = createdByAgentID;
		return this;
	}

	public Integer getCreatedForWebUserID()
	{
		return createdForWebUserID;
	}

	public WorkItem setCreatedForWebUserID(Integer createdForWebUserID)
	{
		this.createdForWebUserID = createdForWebUserID;
		return this;
	}

	public Integer getCreatedForPermissionID()
	{
		return createdForPermissionID;
	}

	public WorkItem setCreatedForPermissionID(Integer createdForPermissionID)
	{
		this.createdForPermissionID = createdForPermissionID;
		return this;
	}

	public String getUri()
	{
		return uri;
	}

	public WorkItem setUri(String uri)
	{
		this.uri = uri;
		return this;
	}

	public String getRequest()
	{
		return request;
	}

	public WorkItem setRequest(String request)
	{
		this.request = request;
		return this;
	}

	public String getResponse()
	{
		return response;
	}

	public WorkItem setResponse(String response)
	{
		this.response = response;
		return this;
	}
		

	public String getWorkType()
	{
		return workType;
	}

	public WorkItem setWorkType(String workType)
	{
		this.workType = workType;
		return this;
	}

	public String getStatus()
	{
		return status;
	}

	public WorkItem setStatus(String status)
	{
		this.status = status;
		return this;
	}

	public String getOwnerSession()
	{
		return ownerSession;
	}

	public WorkItem setOwnerSession(String ownerSession)
	{
		this.ownerSession = ownerSession;
		return this;
	}

	@Override
	public String toString()
	{
		return description;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notEmpty("description", description, DESCRIPTION_MAX_LENGTH)
				.oneOf("state", state, STATE_NEW, STATE_IN_PROGRESS, //
						STATE_ON_HOLD, STATE_COMPLETED, STATE_CANCELLED, STATE_DECLINED, STATE_FAILED)
				.oneOf("type", type, TYPE_AUTHENTICATION_REQUEST, TYPE_REVERSAL_REQUEST, TYPE_SCHEDULED_REPORT, TYPE_EXECUTE) //
				.notLonger("workType", workType, WORK_TYPE_MAX_LENGTH) //
				.notLonger("status", status, STATUS_MAX_LENGTH) //
				.notLonger("ownerSession", ownerSession, SESSION_MAX_LENGTH) //
				.notNull("creationTime", creationTime).notNull("uuid", uuid).notLess("completionTime", completionTime, creationTime).notEmpty("uri", uri, URI_MAX_LENGTH)
				.isTrue("createdByWebUserID", (createdByAgentID == null) ^ (createdByWebUserID == null), "Invalid By WebUser/Agent")
				.isTrue("createdForWebUserID", (createdForPermissionID == null) ^ (createdForWebUserID == null), "Invalid For WebUser/Permission");

		if (STATE_DECLINED.equals(state) || STATE_ON_HOLD.equals(state))
			validator.notEmpty("reason", reason, REASON_MAX_LENGTH);
		else
			validator.notLonger("reason", reason, REASON_MAX_LENGTH);
		
		return validator.toList();
	}

	

}

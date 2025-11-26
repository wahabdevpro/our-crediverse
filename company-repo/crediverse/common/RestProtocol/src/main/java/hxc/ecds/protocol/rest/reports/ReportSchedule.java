package hxc.ecds.protocol.rest.reports;

import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.WebUser;

/*
SCHEDULES:
	LIST: GET ~/reports/{report_type}/{id}/schedule
		returns ExResultList<ReportSchedule>
	CREATE: PUT ~/reports/{report_type}/{id}/schedule
		expects ReportSchedule json as request body
		returns ReportSchedule
	GET: GET ~/reports/{report_type}/{id}/schedule/{id}
		returns ReportSchedule
	UPDATE: PUT ~/reports/{report_type}/{id}/schedule/{id}
		expects ReportSchedule json as request body
		returns ReportSchedule
	DELETE: DELETE ~/reports/{report_type}/{id}/schedule/{id}
		returns void
USERS:
	LIST: GET ~/reports/{report_type}/{id}/schedule/{id}/web_user
		returns ExResultList<WebUser>
	ADD/LINK: PUT ~/reports/{report_type}/{id}/schedule/{id}/web_user/{id}
		returns void?
	REMOVE/UNLINK: DELETE ~/reports/{report_type}/{id}/schedule/{id}/web_user/{id}
		returns void?
*/

public class ReportSchedule implements IValidatable
{
	public static final int DESCRIPTION_MAX_LENGTH = 80;
	public static final int ORIGINATOR_MAX_LENGTH = 64;

	/*
	public enum Originator
	{
		USER,
		MINIMUM_REQUIRED_DATA;
	}
	*/

	public static enum Period
	{
		HOUR,
		DAY,
		WEEK,
		MONTH,
		MINUTE;
	}

	public static enum Channel
	{
		EMAIL("EMAIL"),
		SMS("SMS");

		private String channel;
		Channel(String channel) {
			this.channel = channel;
		}
		public String getChannel() {
			return this.channel;
		}
	}

	protected int id;
	protected int companyID;
	protected int version;
	protected String description;
	protected Period period;
	protected Integer timeOfDay;
	protected Integer startTimeOfDay;
	protected Integer endTimeOfDay;
	protected boolean enabled = true;
	protected Report.Originator originator;
	protected String channels;
	protected List<? extends WebUser> webUsers;
	protected List<? extends AgentUser> agentUsers;
	protected boolean emailToAgent = false;
	protected List<String> recipientEmails = new ArrayList<String>();


	public ReportSchedule amend(ReportSchedule other)
	{
		this.id = other.getId();
		this.companyID = other.getCompanyID();
		this.version = other.getVersion();
		this.description = other.getDescription();
		this.period = other.getPeriod();
		this.timeOfDay = other.getTimeOfDay();
		this.startTimeOfDay = other.getStartTimeOfDay();
		this.endTimeOfDay = other.getEndTimeOfDay();
		this.enabled = other.enabled;
		this.originator = other.getOriginator();
		this.channels = other.channels;
		this.webUsers = other.getWebUsers();
		this.agentUsers = other.getAgentUsers();
		this.emailToAgent = other.getEmailToAgent();
		this.recipientEmails = other.getRecipientEmails();
		return this;
	}

	public int getId()
	{
		return this.id;
	}

	public ReportSchedule setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return this.companyID;
	}

	public ReportSchedule setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return this.version;
	}

	public ReportSchedule setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getDescription()
	{
		return this.description;
	}

	public ReportSchedule setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public Period getPeriod()
	{
		return this.period;
	}

	public ReportSchedule setPeriod( Period period )
	{
		this.period = period;
		return this;
	}

	public Integer getTimeOfDay()
	{
		return this.timeOfDay;
	}

	public ReportSchedule setTimeOfDay( Integer timeOfDay )
	{
		this.timeOfDay = timeOfDay;
		return this;
	}

	public Integer getStartTimeOfDay()
	{
		return this.startTimeOfDay;
	}

	public ReportSchedule setStartTimeOfDay( Integer startTimeOfDay )
	{
		this.startTimeOfDay = startTimeOfDay;
		return this;
	}

	public Integer getEndTimeOfDay()
	{
		return this.endTimeOfDay;
	}

	public ReportSchedule setEndTimeOfDay( Integer endTimeOfDay )
	{
		this.endTimeOfDay = endTimeOfDay;
		return this;
	}

	public boolean getEnabled()
	{
		return this.enabled;
	}
	public ReportSchedule setEnabled( boolean enabled )
	{
		this.enabled = enabled;
		return this;
	}

	public Report.Originator getOriginator()
	{
		return this.originator;
	}

	public ReportSchedule setOriginator( Report.Originator originator )
	{
		this.originator = originator;
		return this;
	}

	public String getChannels()
	//public Set<? extends Channel> getChannels()
	{
		/*
		if ( channels == null )	
			return Collections.emptySet();
		Set<String> temp = new HashSet<String>(Arrays.asList(this.channels.split(",")));	
		Set<Channel> channels = new HashSet<Channel>();
		for (String c : temp) {
			channels.add(Channel.valueOf(c));
		}
		*/
		return this.channels;
	}

	public ReportSchedule setChannels(String channels)
	{
		this.channels = channels;
	/*
		if ( channels == null )
			this.channels = null;
		else 
		{
			Set<String> temp = new HashSet<String>();
			for (Channel c : channels) {
				temp.add(c.getChannel());
			}
			this.channels = String.join(",", temp);
		}
		*/
		return this;
	}

	public List<? extends WebUser> getWebUsers()
	{
		return this.webUsers;
	}

	public ReportSchedule setWebUsers(List<? extends WebUser> webUsers)
	{
		this.webUsers = webUsers;
		return this;
	}
	
	public List<? extends AgentUser> getAgentUsers()
	{
		return this.agentUsers;
	}

	public ReportSchedule setAgentUsers(List<? extends AgentUser> agentUsers)
	{
		this.agentUsers = agentUsers;
		return this;
	}

	public List<String> getRecipientEmails()
	{
		return this.recipientEmails;
	}

	public ReportSchedule setRecipientEmails(List<String> recipientEmails)
	{
		this.recipientEmails = recipientEmails;
		return this;
	}

	public boolean getEmailToAgent()
	{
		return this.emailToAgent;
	}

	public ReportSchedule setEmailToAgent( boolean emailToAgent )
	{
		this.emailToAgent = emailToAgent;
		return this;
	}

	public List<Violation> validate()
	{
		return new ArrayList<Violation>();
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(id = %s, companyID = %s, version = %s, description = '%s', period = '%s', timeOfDay = '%s', starTimeOfDay = '%s', endTimeOfDay = '%s', enabled = '%s', originator = '%s', channel = '%s', webUsers = '%s', agentUsers = '%s', recipientEmails = '%s', emailToAgent = '%s', %s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			id, companyID, version, description,
			period, timeOfDay, startTimeOfDay, endTimeOfDay,
			enabled, originator, channels,
			webUsers, agentUsers, recipientEmails, emailToAgent,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}
}

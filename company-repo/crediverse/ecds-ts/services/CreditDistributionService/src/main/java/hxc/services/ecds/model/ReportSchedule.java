package hxc.services.ecds.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import hxc.services.ecds.model.WebUser;

//import hxc.ecds.protocol.rest.*;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.reports.Report;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Table(name = "er_report_schedule",
	uniqueConstraints = {
		@UniqueConstraint(name = "er_report_schedule_internal_name", columnNames = { "company_id", "internal_name" })
	},
	indexes = {}
)
@Entity
@NamedQueries({ //
		@NamedQuery(name = "ReportSchedule.findByID", query = "SELECT p FROM ReportSchedule p where id = :id and companyID = :companyID"),
		@NamedQuery(name = "ReportSchedule.findByIDAndReportSpecificationID", query = "SELECT p FROM ReportSchedule p where id = :id and reportSpecificationID = :reportSpecificationID and companyID = :companyID"),
		@NamedQuery(name = "ReportSchedule.findByReportSpecificationID", query = "SELECT p FROM ReportSchedule p where reportSpecificationID = :reportSpecificationID and companyID = :companyID"),
})
public class ReportSchedule extends hxc.ecds.protocol.rest.reports.ReportSchedule implements Serializable, ICompanyData<ReportSchedule>
{
	private static final long serialVersionUID = 8980612590467220312L;
	private static final int INTERNAL_NAME_MAX_LENGTH = 50;
	private static final int DELIVERY_CHANNELS_MAX_LENGTH = 64;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////

	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected ReportSpecification reportSpecification;
	@JsonIgnore
	protected Integer reportSpecificationID;
	@JsonIgnore
	protected Date lastExecuted;
	@JsonIgnore
	protected String internalName;

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return this.id;
	}

	@Override
	public ReportSchedule setId(int id)
	{
		this.id = id;
		return this;
	}

	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return this.companyID;
	}

	public ReportSchedule setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return this.version;
	}

	@Override
	public ReportSchedule setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return this.lastUserID;
	}

	@Override
	public ReportSchedule setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return this.lastTime;
	}

	@Override
	public ReportSchedule setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public ReportSchedule setDescription(String description)
	{
		this.description = description;
		return this;
	}


	@Override
	@Column(name = "period", nullable = false)
	@Enumerated(EnumType.STRING)
	public Period getPeriod()
	{
		return this.period;
	}

	@Override
	public ReportSchedule setPeriod( Period period )
	{
		this.period = period;
		return this;
	}

	@Override
	@Column(name = "time_of_day", nullable = true)
	public Integer getTimeOfDay()
	{
		return this.timeOfDay;
	}

	@Override
	public ReportSchedule setTimeOfDay( Integer timeOfDay )
	{
		this.timeOfDay = timeOfDay;
		return this;
	}

	@Override
	@Column(name = "start_time_of_day", nullable = true)
	public Integer getStartTimeOfDay()
	{
		return this.startTimeOfDay;
	}

	@Override
	public ReportSchedule setStartTimeOfDay( Integer startTimeOfDay )
	{
		this.startTimeOfDay = startTimeOfDay;
		return this;
	}

	@Override
	@Column(name = "end_time_of_day", nullable = true)
	public Integer getEndTimeOfDay()
	{
		return this.endTimeOfDay;
	}

	@Override
	public ReportSchedule setEndTimeOfDay( Integer endTimeOfDay )
	{
		this.endTimeOfDay = endTimeOfDay;
		return this;
	}

	@Override
	@Column(name = "enabled", nullable = false, columnDefinition = "TINYINT", length = 1)
	public boolean getEnabled()
	{
		return this.enabled;
	}

	@Override
	public ReportSchedule setEnabled( boolean enabled )
	{
		this.enabled = enabled;
		return this;
	}

	@Column(name = "originator", nullable = true, length = ORIGINATOR_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	public Report.Originator getOriginator()
	{
		return this.originator;
	}

	public ReportSchedule setOriginator( Report.Originator originator )
	{
		this.originator = originator;
		return this;
	}

	@Override
	@Column(name = "delivery_channels", nullable = true, length = DELIVERY_CHANNELS_MAX_LENGTH)
	public String getChannels()
	//public Set<Channel> getChannels()
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

	@Override
	public ReportSchedule setChannels( String channels )
	//public ReportSchedule setChannels( Set<? extends Channel> channels )
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

	public boolean hasChannel( Channel channel )
	{
		if ( this.channels == null ) return false;
		Set<String> channels = new HashSet<String>(Arrays.asList(this.channels.split(",")));	
		return channels.contains(channel.getChannel());
	}

	@Column(name = "internal_name", nullable = true, length = INTERNAL_NAME_MAX_LENGTH)
	public String getInternalName()
	{
		return this.internalName;
	}

	public ReportSchedule setInternalName(String internalName)
	{
		this.internalName = internalName;
		return this;
	}

	@Override
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinTable(name = "er_report_schedule_webuser", joinColumns = { @JoinColumn(name = "report_schedule_id") }, inverseJoinColumns = { @JoinColumn(name = "webuser_id") })
	@SuppressWarnings({"unchecked"})
	public List<WebUser> getWebUsers()
	{
		return (List<WebUser>)this.webUsers;
	}

	@Override
	public ReportSchedule setWebUsers(List<? extends hxc.ecds.protocol.rest.WebUser> webUsers)
	{
		this.webUsers = webUsers;
		return this;
	}
	
	@Override
	@ManyToMany(cascade = CascadeType.DETACH)
	@JoinTable(name = "er_report_schedule_agent_user", joinColumns = { @JoinColumn(name = "report_schedule_id") }, inverseJoinColumns = { @JoinColumn(name = "agent_user_id") })
	@SuppressWarnings({"unchecked"})
	@LazyCollection(LazyCollectionOption.FALSE)
	public List<AgentUser> getAgentUsers()
	{
		return (List<AgentUser>)this.agentUsers;
	}

	@Override
	public ReportSchedule setAgentUsers(List<? extends hxc.ecds.protocol.rest.AgentUser> agentUsers)
	{
		this.agentUsers = agentUsers;
		return this;
	}

	@Override
	@ElementCollection()
	@Column(name="email")
	@CollectionTable(name = "er_report_schedule_recipient_email", joinColumns = { @JoinColumn(name = "report_schedule_id", referencedColumnName = "id") })
	@SuppressWarnings({"unchecked"})
	@LazyCollection(LazyCollectionOption.FALSE)
	public List<String> getRecipientEmails()
	{
		return (List<String>)this.recipientEmails;
	}

	@Override
	public ReportSchedule setRecipientEmails(List<String> recipientEmails)
	{
		this.recipientEmails = recipientEmails;
		return this;
	}
	
	@Override
	@Column(name = "email_to_agent", nullable = false, columnDefinition = "TINYINT", length = 1)
	public boolean getEmailToAgent()
	{
		return this.emailToAgent;
	}

	@Override
	public ReportSchedule setEmailToAgent( boolean emailToAgent )
	{
		this.emailToAgent = emailToAgent;
		return this;
	}


	///////////////////////////////////

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "report_specification_id", nullable = false, insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_ReportSchedule_ReportSpecification"))
	public ReportSpecification getReportSpecification()
	{
		return this.reportSpecification;
	}

	public ReportSchedule setReportSpecification( ReportSpecification reportSpecification )
	{
		this.reportSpecification = reportSpecification;
		return this;
	}

	@Column(name = "report_specification_id", nullable = false)
	public Integer getReportSpecificationID()
	{
		return this.reportSpecificationID;
	}

	public ReportSchedule setReportSpecificationID( Integer reportSpecificationID )
	{
		this.reportSpecificationID = reportSpecificationID;
		return this;
	}

	@Column(name = "last_executed", nullable = true)
	public Date getLastExecuted()
	{
		return this.lastExecuted;
	}

	public ReportSchedule setLastExecuted(Date lastExecuted)
	{
		this.lastExecuted = lastExecuted;
		return this;
	}

	///////////////////////////////////

	@Override
	public List<Violation> validate()
	{
		return new ArrayList<Violation>();
	}

	///////////////////////////////////

	public ReportSchedule()
	{
	}

	public ReportSchedule(ReportSchedule other)
	{
		this.amend(other);
	}

	public ReportSchedule copy(EntityManager em) throws RuleCheckException
	{
		ReportSchedule copy = new ReportSchedule();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(em, this);
		return copy;
	}

	public ReportSchedule amend(ReportSchedule other)
	{
		super.amend(other);
		this.internalName = other.internalName;
		// XXX TODO FIXME
		return this;
	}

    @SuppressWarnings({"unchecked"})
    public ReportSchedule amend(EntityManager em, hxc.ecds.protocol.rest.reports.ReportSchedule other) throws RuleCheckException
    {
        List<WebUser> existingWebUsers = this.getWebUsers();
        List<AgentUser> existingAgentUsers = this.getAgentUsers();
		super.amend(other);
        this.setWebUsers(existingWebUsers);
        this.setAgentUsers(existingAgentUsers);
        // Add new WebUsers
        List<hxc.ecds.protocol.rest.WebUser> newWebUsers = (List<hxc.ecds.protocol.rest.WebUser>) other.getWebUsers();
        if (newWebUsers != null)
        {
			if ( existingWebUsers == null )
			{
				existingWebUsers = new ArrayList<WebUser>();
				this.setWebUsers(existingWebUsers);
			}
            for (hxc.ecds.protocol.rest.WebUser newWebUser : newWebUsers)
            {
                if (!contains(existingWebUsers, newWebUser))
                {
                    WebUser webUser = WebUser.findByID(em, newWebUser.getId(), companyID);
                    if (webUser == null)
                        throw new RuleCheckException(StatusCode.INVALID_VALUE, "webUsers.id", "Invalid WebUserID %d", newWebUser.getId());
                    existingWebUsers.add(webUser);
                }
            }

            // Remove unused WebUsers
            int index = 0;
            while (index < existingWebUsers.size())
            {
                if (!contains(newWebUsers, existingWebUsers.get(index)))
                    existingWebUsers.remove(index);
                else
                    index++;
            }
        }
        // Add new AgentUsers
        List<hxc.ecds.protocol.rest.AgentUser> newAgentUsers = (List<hxc.ecds.protocol.rest.AgentUser>) other.getAgentUsers();
        if (newAgentUsers != null)
        {
			if ( existingAgentUsers == null )
			{
				existingAgentUsers = new ArrayList<AgentUser>();
				this.setAgentUsers(existingAgentUsers);
			}
            for (hxc.ecds.protocol.rest.AgentUser newAgentUser : newAgentUsers)
            {
                if (!contains(existingAgentUsers, newAgentUser))
                {
                    AgentUser agentUser = AgentUser.findByID(em, newAgentUser.getId(), companyID);
                    if (agentUser == null)
                        throw new RuleCheckException(StatusCode.INVALID_VALUE, "agentUsers.id", "Invalid AgentUserID %d", newAgentUser.getId());
                    existingAgentUsers.add(agentUser);
                }
            }

            // Remove unused AgentUsers
            int index = 0;
            while (index < existingAgentUsers.size())
            {
                if (!contains(newAgentUsers, existingAgentUsers.get(index)))
                    existingAgentUsers.remove(index);
                else
                    index++;
            }
        }
        return this;
    }


	///////////////////////////////////

	public static ReportSchedule findByID(EntityManager em, int companyID, int id)
	{
		TypedQuery<ReportSchedule> query = em.createNamedQuery("ReportSchedule.findByID", ReportSchedule.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<ReportSchedule> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static ReportSchedule findByIDAndReportSpecificationID(EntityManager em, int companyID, int id, int reportSpecificationID)
	{
		TypedQuery<ReportSchedule> query = em.createNamedQuery("ReportSchedule.findByIDAndReportSpecificationID", ReportSchedule.class);
		query.setParameter("companyID", companyID);
		query.setParameter("id", id);
		query.setParameter("reportSpecificationID", reportSpecificationID);
		List<ReportSchedule> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<ReportSchedule> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, ReportSchedule.class, params, companyID, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{

		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, ReportSchedule.class, params, companyID, "name", "description");
		return query.getSingleResult();
	}

	public static List<ReportSchedule> findByReportSpecificationID(EntityManager em, RestParams params, int companyID, int reportSpecificationID)
	{
		ReportScheduleExtender px = new ReportScheduleExtender(reportSpecificationID);
		return QueryBuilder.getQueryResultList(em, ReportSchedule.class, params, companyID, px, "name", "description");
	}
	
	public static Long findByReportSpecificationIDCount(EntityManager em, RestParams params, int companyID, int reportSpecificationID)
	{
		ReportScheduleExtender px = new ReportScheduleExtender(reportSpecificationID);
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, ReportSchedule.class, params, companyID, px, "name", "description");
		return query.getSingleResult();
	}

	///////////////////////////////////

	public static List<ReportSchedule> findReady(EntityManager em, int companyID, Date referenceDate)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ReportSchedule> criteriaQuery = criteriaBuilder.createQuery(ReportSchedule.class);
		Root<ReportSchedule> root = criteriaQuery.from(ReportSchedule.class);
		List<Predicate> wherePredicates = new ArrayList<Predicate>();;
		wherePredicates.add(criteriaBuilder.equal(root.get("enabled"),true));
		wherePredicates.add(criteriaBuilder.equal(root.get("companyID"),companyID));
		wherePredicates.add(criteriaBuilder.or(
			criteriaBuilder.and(
				criteriaBuilder.equal(root.get("period"),Period.HOUR),
				criteriaBuilder.or(root.get("lastExecuted").isNull(),
					criteriaBuilder.notEqual(
						criteriaBuilder.function("date_format",String.class,root.get("lastExecuted"), criteriaBuilder.literal("%Y-%m-%dT%H")),
						criteriaBuilder.function("date_format",String.class,criteriaBuilder.literal(referenceDate), criteriaBuilder.literal("%Y-%m-%dT%H"))
					)
				)
			),
			criteriaBuilder.and(
				criteriaBuilder.equal(root.get("period"),Period.DAY),
				criteriaBuilder.or(root.get("lastExecuted").isNull(),
					criteriaBuilder.notEqual(
						criteriaBuilder.function("date_format",String.class,root.get("lastExecuted"), criteriaBuilder.literal("%Y-%m-%d")),
						criteriaBuilder.function("date_format",String.class,criteriaBuilder.literal(referenceDate), criteriaBuilder.literal("%Y-%m-%d"))
					)
				)
			),
			criteriaBuilder.and(
				criteriaBuilder.equal(root.get("period"),Period.WEEK),
				criteriaBuilder.or(root.get("lastExecuted").isNull(),
					criteriaBuilder.notEqual(
						criteriaBuilder.function("date_format",String.class,root.get("lastExecuted"), criteriaBuilder.literal("%Y-W%u")),
						criteriaBuilder.function("date_format",String.class,criteriaBuilder.literal(referenceDate), criteriaBuilder.literal("%Y-W%u"))
					)
				)
			),
			criteriaBuilder.and(
				criteriaBuilder.equal(root.get("period"),Period.MONTH),
				criteriaBuilder.or(root.get("lastExecuted").isNull(),
					criteriaBuilder.notEqual(
						criteriaBuilder.function("date_format",String.class,root.get("lastExecuted"), criteriaBuilder.literal("%Y-%m")),
						criteriaBuilder.function("date_format",String.class,criteriaBuilder.literal(referenceDate), criteriaBuilder.literal("%Y-%m"))
					)
				)
			),
			criteriaBuilder.and(
				criteriaBuilder.equal(root.get("period"),Period.MINUTE),
				criteriaBuilder.or(root.get("lastExecuted").isNull(),
					criteriaBuilder.notEqual(
						criteriaBuilder.function("date_format",String.class,root.get("lastExecuted"), criteriaBuilder.literal("%Y-%m-%dT%H:%M")),
						criteriaBuilder.function("date_format",String.class,criteriaBuilder.literal(referenceDate), criteriaBuilder.literal("%Y-%m-%dT%H:%M"))
					)
				)
			)
		));
		criteriaQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
		criteriaQuery.select(root);
		TypedQuery<ReportSchedule> typedQuery = em.createQuery(criteriaQuery);
		List<ReportSchedule> entries = typedQuery.getResultList();
		return entries;
	}

	///////////////////////////////////

	@Override
	public void validate(ReportSchedule previous) throws RuleCheckException
	{
		// XXX TODO FIXME
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}
	}

	@Override
	public void persist(EntityManager em, ReportSchedule existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_REPORT, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_REPORT, auditEntryContext);
	}

	///////////////////////////////////

	public String describe(String extra)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		return super.describe(String.format("lastUserID = '%s', lastTime = '%s', reportSpecificationID = '%s', lastExecuted = '%s', internalName = '%s'%s%s",
			lastUserID, ( lastTime != null ? dateFormat.format(lastTime) : null ),
			reportSpecificationID, (lastExecuted != null ? dateFormat.format(lastExecuted) : null ), internalName,
			(extra.isEmpty() ? "" : ", "), extra));
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

    private static boolean contains(List<? extends hxc.ecds.protocol.rest.WebUser> webUsers, hxc.ecds.protocol.rest.WebUser webUser)
    {
		Objects.requireNonNull(webUsers, "webUsers may not be null");
		Objects.requireNonNull(webUser, "webUser may not be null");
        for (hxc.ecds.protocol.rest.WebUser webUsr : webUsers)
        {
            if (webUsr.getId() == webUser.getId())
                return true;
        }
        return false;
    }

    private static boolean contains(List<? extends hxc.ecds.protocol.rest.AgentUser> agentUsers, hxc.ecds.protocol.rest.AgentUser agentUser)
    {
		Objects.requireNonNull(agentUsers, "agentUsers may not be null");
		Objects.requireNonNull(agentUser, "agentUser may not be null");
        for (hxc.ecds.protocol.rest.AgentUser agentUsr : agentUsers)
        {
            if (agentUsr.getId() == agentUser.getId())
                return true;
        }
        return false;
    }

	///////////////////////////////////

    static class ReportScheduleExtender extends PredicateExtender<ReportSchedule>
    {
        private int myReportSpecificationID;

        public ReportScheduleExtender(int myReportSpecificationID)
        {
            this.myReportSpecificationID = myReportSpecificationID;
        }

        @Override
        public String getName()
        {
            return "ReportScheduleExtender";
        }

        @Override
        public List<Predicate> extend(CriteriaBuilder cb, Root<ReportSchedule> root, CriteriaQuery<?> query, List<Predicate> predicates)
        {
            Predicate p1 = cb.equal(col(root, "reportSpecificationID"), cb.parameter(Integer.class, "myReportSpecificationID"));
            predicates.add(p1);
            return predicates;
        }

        @Override
        public void addParameters(TypedQuery<?> query)
        {
            query.setParameter("myReportSpecificationID", myReportSpecificationID);
        }
    }
}

package hxc.services.ecds.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.Violation;
import hxc.services.ecds.AuditEntryContext;
//import hxc.ecds.protocol.rest.reports.*;
import hxc.services.ecds.Session;
import hxc.services.ecds.reports.Report;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReportParameters;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReportSpecification;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

@Table(name = "er_report",
	uniqueConstraints = {
		@UniqueConstraint(name = "er_report_name", columnNames = { "company_id", "agent_id", "name" }),
		@UniqueConstraint(name = "er_report_internal_name", columnNames = { "company_id", "agent_id", "internal_name" })
	},
	indexes = {
		@Index(name = "er_report_agent_id", columnList = "agent_id"),
	}	
)
@Entity
@NamedQueries({ //
		@NamedQuery(name = "ReportSpecification.findByName", query = "SELECT p FROM ReportSpecification p where name = :name and companyID = :companyID"),
		@NamedQuery(name = "ReportSpecification.findByID", query = "SELECT p FROM ReportSpecification p where id = :id and companyID = :companyID"),
		@NamedQuery(name = "ReportSpecification.findByAgentID", query = "SELECT p FROM ReportSpecification p where agentID = :agentID and companyID = :companyID"),
		@NamedQuery(name = "ReportSpecification.findByNameAndType", query = "SELECT p FROM ReportSpecification p where name = :name and companyID = :companyID and type = :type"),
		@NamedQuery(name = "ReportSpecification.findByIDAndType", query = "SELECT p FROM ReportSpecification p where id = :id and companyID = :companyID and type = :type"),
})
public class ReportSpecification extends hxc.ecds.protocol.rest.reports.ReportSpecification implements Serializable, ICompanyData<ReportSpecification>
{
	final static Logger logger = LoggerFactory.getLogger(ReportSpecification.class);
	private static final long serialVersionUID = 2918491466431622070L;

	private static final String OLD_SALES_SUMMARY_DEFAULT_NAME = "Default Sales Summary";
	private static final String OLD_SALES_SUMMARY_DEFAULT_DESCRIPTION = "Default Sales Summary";
	private static final String OLD_SALES_SUMMARY_SCHEDULE_DEFAULT_DESCRIPTION = "Default Sales Summary Schedule";

	private static final String SALES_SUMMARY_DEFAULT_NAME = "Sales Summary";
	private static final String SALES_SUMMARY_DEFAULT_DESCRIPTION = "Summary of sales transactions.";
	private static final String SALES_SUMMARY_SCHEDULE_DEFAULT_DESCRIPTION = "Hourly schedule for summary of sales transactions.";

	private static final String INTERNAL_NAME_SALES_SUMMARY = "DEFAULT_SALES_SUMMARY";

	private static final int TYPE_MAX_LENGTH = 50;
	private static final int INTERNAL_NAME_MAX_LENGTH = 50;

	/*
	public static enum Type
	{
		RETAILER_PERFORMANCE,
		WHOLESALER_PERFORMANCE,
		SALES_SUMMARY;
	};
	*/

	public static final Permission MAY_ADD = new Permission(false, true, Permission.GROUP_REPORTS, Permission.PERM_ADD, "May Add Reports");
	public static final Permission MAY_UPDATE = new Permission(false, true, Permission.GROUP_REPORTS, Permission.PERM_UPDATE, "May Update Reports");
	public static final Permission MAY_DELETE = new Permission(false, true, Permission.GROUP_REPORTS, Permission.PERM_DELETE, "May Delete Reports");
	public static final Permission MAY_VIEW = new Permission(false, true, Permission.GROUP_REPORTS, Permission.PERM_VIEW, "May View Reports");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	//protected int id;
	//protected int companyID;

	//protected int version;
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected String internalName;

	//protected String name;
	//protected String description;
	protected String type;
	protected String parameters;

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return this.id;
	}

	@Override
	public ReportSpecification setId(int id)
	{
		this.id = id;
		return this;
	}

	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return this.companyID;
	}

	public ReportSpecification setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "agent_id", nullable = false)
	public Integer getAgentID()
	{
		return this.agentID;
	}

	@Override
	public ReportSpecification setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return this.version;
	}

	@Override
	public ReportSpecification setVersion(int version)
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
	public ReportSpecification setLastUserID(int lastUserID)
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
	public ReportSpecification setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return this.name;
	}

	@Override
	public ReportSpecification setName(String name)
	{
		this.name = name;
		return this;
	}

	@Column(name = "originator", nullable = true, length = ORIGINATOR_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	public Report.Originator getOriginator()
	{
		return this.originator;
	}

	public ReportSpecification setOriginator( Report.Originator originator )
	{
		this.originator = originator;
		return this;
	}

	@Column(name = "internal_name", nullable = true, length = INTERNAL_NAME_MAX_LENGTH)
	public String getInternalName()
	{
		return this.internalName;
	}

	public ReportSpecification setInternalName(String internalName)
	{
		this.internalName = internalName;
		return this;
	}

	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return this.type;
	}

	public ReportSpecification setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public ReportSpecification setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	@OneToMany(mappedBy = "reportSpecification", fetch = FetchType.EAGER, orphanRemoval = true)
	@SuppressWarnings({"unchecked"})
	public List<ReportSchedule> getSchedules()
	{
		return (List<ReportSchedule>) this.schedules;
	}

	@Override
	public ReportSpecification setSchedules(List<? extends hxc.ecds.protocol.rest.reports.ReportSchedule> schedules)
	{
		this.schedules = schedules;
		return this;
	}

	@Column(name = "parameters", nullable = false, columnDefinition = "TEXT")
	public String getParameters()
	{
		return this.parameters;
	}

	public ReportSpecification setParameters(String parameters)
	{
		this.parameters = parameters;
		return this;
	}

	@Override
	public List<Violation> validate()
	{
		return new ArrayList<Violation>();
	}

	///////////////////////////////////

	public ReportSpecification()
	{
	}

	public ReportSpecification(ReportSpecification other)
	{
		this.amend(other);
	}

	public ReportSpecification amend(ReportSpecification other)
	{
		super.amend(other);
		this.parameters = other.parameters;
		this.type = other.type;
		this.internalName = other.internalName;
		return this;
	}

	///////////////////////////////////

	public static ReportSpecification findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<ReportSpecification> query = em.createNamedQuery("ReportSpecification.findByName", ReportSpecification.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<ReportSpecification> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static ReportSpecification findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<ReportSpecification> query = em.createNamedQuery("ReportSpecification.findByID", ReportSpecification.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<ReportSpecification> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<ReportSpecification> findByAgentID(EntityManager em, int agentID, int companyID)
	{
		TypedQuery<ReportSpecification> query = em.createNamedQuery("ReportSpecification.findByAgentID", ReportSpecification.class);
		query.setParameter("agentID", agentID);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static ReportSpecification findByNameAndType(EntityManager em, int companyID, String name, Report.Type type)
	{
		TypedQuery<ReportSpecification> query = em.createNamedQuery("ReportSpecification.findByNameAndType", ReportSpecification.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		query.setParameter("type", type.toString());
		List<ReportSpecification> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static ReportSpecification findByIDAndType(EntityManager em, int id, int companyID, Report.Type type)
	{
		TypedQuery<ReportSpecification> query = em.createNamedQuery("ReportSpecification.findByIDAndType", ReportSpecification.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		query.setParameter("type", type.toString());
		List<ReportSpecification> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<ReportSpecification> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, ReportSpecification.class, params, companyID, "name", "description");
	}

	public static List<ReportSpecification> findType(EntityManager em, RestParams params, int companyID, Report.Type type)
	{
		ReportSpecificationExtender px = new ReportSpecificationExtender(type.toString());
		return QueryBuilder.getQueryResultList(em, ReportSpecification.class, params, companyID, px, "name", "description");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, ReportSpecification.class, params, companyID, "name", "description");
		return query.getSingleResult();
	}

	public static Long findTypeCount(EntityManager em, RestParams params, int companyID, Report.Type type)
	{
		ReportSpecificationExtender px = new ReportSpecificationExtender(type.toString());
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, ReportSpecification.class, params, companyID, px, "name", "description");
		return query.getSingleResult();
	}

	///////////////////////////////////

	@SuppressWarnings({"unchecked"})
	public static void loadMRD(EntityManager em, int companyID, Session session) throws Exception
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);

		// X Find existing ReportSpecification
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		ReportSpecification reportSpecification = null;

		boolean fixSpecificationDefaults = false;

		// X.X Try find specification with originator == MINIMUM_REQUIRED_DATA and internalName == INTERNAL_NAME_SALES_SUMMARY
		if ( reportSpecification == null )
		{
			CriteriaQuery<ReportSpecification> criteriaQuery = criteriaBuilder.createQuery(ReportSpecification.class);
			Root<ReportSpecification> root = criteriaQuery.from(ReportSpecification.class);
			criteriaQuery.where(
				criteriaBuilder.equal(root.get("companyID"),companyID),
				criteriaBuilder.equal(root.get("type"),Report.Type.SALES_SUMMARY.toString()),
				criteriaBuilder.equal(root.get("originator"),Report.Originator.MINIMUM_REQUIRED_DATA),
				criteriaBuilder.equal(root.get("internalName"),INTERNAL_NAME_SALES_SUMMARY)
			);
			criteriaQuery.select(root);
			TypedQuery<ReportSpecification> typedQuery = em.createQuery(criteriaQuery);
			List<ReportSpecification> entries = typedQuery.getResultList();
			if ( !entries.isEmpty() )
			{
				reportSpecification = entries.get(0);
			}
		}

		// X.X Try find specification with originator == null AND ( name == OLD_SALES_SUMMARY_DEFAULT_NAME OR description == OLD_SALES_SUMMARY_DEFAULT_DESCRIPTION OR  )
		if ( reportSpecification == null )
		{
			CriteriaQuery<ReportSpecification> criteriaQuery = criteriaBuilder.createQuery(ReportSpecification.class);
			Root<ReportSpecification> root = criteriaQuery.from(ReportSpecification.class);
			criteriaQuery.where(
				criteriaBuilder.equal(root.get("companyID"),companyID),
				criteriaBuilder.equal(root.get("type"),Report.Type.SALES_SUMMARY.toString()),
				criteriaBuilder.isNull(root.get("originator"))
			);
			criteriaQuery.select(root);
			TypedQuery<ReportSpecification> typedQuery = em.createQuery(criteriaQuery);
			List<ReportSpecification> entries = typedQuery.getResultList();
			if ( !entries.isEmpty() )
			{
				reportSpecification = entries.get(0);
				fixSpecificationDefaults = true;
			}
		}
		logger.info("Found (fixSpecificationDefaults = {}) reportSpecification = {}", fixSpecificationDefaults, reportSpecification);

		// Default sales summary report ...
		//ReportSpecification reportSpecification = findByNameAndType(em, companyID, SALES_SUMMARY_DEFAULT_NAME, Report.Type.SALES_SUMMARY);
		ReportSpecification existingReportSpecification = null;
		SalesSummaryReportSpecification salesSummaryReportSpecification = null;
		if ( reportSpecification == null )
		{
			salesSummaryReportSpecification = new SalesSummaryReportSpecification();
				salesSummaryReportSpecification
				.setName(SALES_SUMMARY_DEFAULT_NAME)
				.setDescription(SALES_SUMMARY_DEFAULT_DESCRIPTION);
			reportSpecification = new ReportSpecification();
		}
		else
		{
			existingReportSpecification = new ReportSpecification(reportSpecification);
			salesSummaryReportSpecification = new SalesSummaryReportSpecification(reportSpecification);
		}
		salesSummaryReportSpecification
			.setOriginator(Report.Originator.MINIMUM_REQUIRED_DATA)
			.setCompanyID(companyID)
		;
		if ( fixSpecificationDefaults )
		{
			if ( Objects.equals(salesSummaryReportSpecification.getName(),OLD_SALES_SUMMARY_DEFAULT_NAME) )	
				salesSummaryReportSpecification.setName(SALES_SUMMARY_DEFAULT_NAME);
			if ( Objects.equals(salesSummaryReportSpecification.getDescription(),OLD_SALES_SUMMARY_DEFAULT_DESCRIPTION) )
				salesSummaryReportSpecification.setDescription(SALES_SUMMARY_DEFAULT_DESCRIPTION);
		}
		if (salesSummaryReportSpecification.getParameters() == null)
		{
			salesSummaryReportSpecification.setParameters(new SalesSummaryReportParameters());
		}
		List<ReportSchedule> reportSchedules = null;
		if (salesSummaryReportSpecification.getSchedules() == null)
		{
			reportSchedules = new ArrayList<ReportSchedule>();
			salesSummaryReportSpecification.setSchedules(reportSchedules);
		}
		else
		{
			reportSchedules = (List<ReportSchedule>)salesSummaryReportSpecification.getSchedules();
		}
		boolean fixScheduleDefaults = false;
		ReportSchedule existingDefaultReportSchedule = null;
		ReportSchedule defaultReportSchedule = null;
		if ( defaultReportSchedule == null )
		{
			for (ReportSchedule reportSchedule: reportSchedules)
			{
				logger.info("Checking (0) reportSchedule = {}", reportSchedule);
				if (
					reportSchedule.getCompanyID() == companyID &&
					Objects.equals(reportSchedule.getOriginator(),Report.Originator.MINIMUM_REQUIRED_DATA) &&
					Objects.equals(reportSchedule.getInternalName(),INTERNAL_NAME_SALES_SUMMARY) )
				{
					defaultReportSchedule = reportSchedule;
				}
			}
		}
		if ( defaultReportSchedule == null )
		{
			for (ReportSchedule reportSchedule: reportSchedules)
			{
				logger.info("Checking (1) reportSchedule = {}", reportSchedule);
				if (Objects.equals(reportSchedule.getCompanyID(),companyID))
				{
					defaultReportSchedule = reportSchedule;
					fixScheduleDefaults = true;
				}
			}
		}
		logger.info("Found (fixScheduleDefaults = {}) defaultReportSchedule = {}", fixScheduleDefaults, defaultReportSchedule);
		if ( defaultReportSchedule == null )
		{
			defaultReportSchedule = new ReportSchedule()
				.setDescription(SALES_SUMMARY_SCHEDULE_DEFAULT_DESCRIPTION)
				.setPeriod(ReportSchedule.Period.HOUR)
				.setStartTimeOfDay( 10 * 60 * 60 )
				.setEndTimeOfDay( 20 * 60 * 60 )
			;
			reportSchedules.add( defaultReportSchedule );
		}
		else
		{
			existingDefaultReportSchedule = new ReportSchedule(defaultReportSchedule);
		}
		defaultReportSchedule
			.setOriginator(Report.Originator.MINIMUM_REQUIRED_DATA)
			.setCompanyID(companyID)
			.setEnabled(true);
		logger.info("Checking defaultReportSchedule = {}", defaultReportSchedule);
		if (fixScheduleDefaults)
		{
			if (Objects.equals(defaultReportSchedule.getDescription(),OLD_SALES_SUMMARY_SCHEDULE_DEFAULT_DESCRIPTION))
				defaultReportSchedule.setDescription(SALES_SUMMARY_SCHEDULE_DEFAULT_DESCRIPTION);
		}
		reportSpecification.amend(salesSummaryReportSpecification.toReportSpecification());
		reportSpecification.setInternalName(INTERNAL_NAME_SALES_SUMMARY);
		defaultReportSchedule.setReportSpecification(reportSpecification);
		defaultReportSchedule.setInternalName(INTERNAL_NAME_SALES_SUMMARY);
		logger.info("Persisting salesSummaryReportSpecification = {}", salesSummaryReportSpecification);
		logger.info("Persisting (salesSummary) reportSpecification = {}", reportSpecification);
		AuditEntryContext auditContext = new AuditEntryContext("LOAD_MRD_REPORT_SPECIFICATION", reportSpecification.getName());
		reportSpecification.persist(em, existingReportSpecification, session, auditContext);
		defaultReportSchedule.setReportSpecificationID(reportSpecification.getId());
		logger.info("Persisting defaultReportSchedule = {}", defaultReportSchedule);
		auditContext = new AuditEntryContext("LOAD_MRD_REPORT_SCHEDULE", defaultReportSchedule.getInternalName());
		defaultReportSchedule.persist(em, existingDefaultReportSchedule, session, auditContext);
	}

	@Override
	public void validate(ReportSpecification previous) throws RuleCheckException
	{
		// XXX TODO FIXME
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
			RuleCheck.noChange("type", type, previous.type);
			if (!type.equals(previous.getType()))
				RuleCheck.oneOf("type", type, Report.Type.RETAILER_PERFORMANCE.toString(), Report.Type.WHOLESALER_PERFORMANCE.toString());
		}
	}

	@Override
	public void persist(EntityManager em, ReportSpecification existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
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
		return super.describe(String.format("type = '%s', parameters = '%s', lastUserID = '%s', lastTime = '%s', internalName = '%s'%s%s",
			type, parameters,
			lastUserID, (lastTime != null ? dateFormat.format(lastTime) : null), internalName,
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

	///////////////////////////////////

	static class ReportSpecificationExtender extends PredicateExtender<ReportSpecification>
	{
		private String myType;

		public ReportSpecificationExtender(String myType)
		{
			this.myType = myType;
		}

		@Override
		public String getName()
		{
			return "ReportSpecificationExtender";
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<ReportSpecification> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			Predicate p1 = cb.equal(col(root, "type"), cb.parameter(String.class, "myType"));
			predicates.add(p1);
			return predicates;
		}

		@Override
		public void addParameters(TypedQuery<?> query)
		{
			query.setParameter("myType", myType);
		}
	}

}

package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Company Table - Used for Multi-Tenancy
//
// /////////////////////////////////

@Table(name = "es_company")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Company.findByName", query = "SELECT c FROM Company c where name = :name"), //
		@NamedQuery(name = "Company.findAll", query = "SELECT c FROM Company c"), //
		@NamedQuery(name = "Company.findByID", query = "SELECT c FROM Company c where id = :id") })
public class Company implements Serializable, IMasterData<Company>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String STATE_ACTIVE = "A";
	public static final String STATE_SUSPENDED = "S";
	public static final String STATE_DELETED = "D";

	public static final String NAME_SUPPLIER = "Concurrent Systems";
	public static final String PREFIX_SUPPLIER = "cs";

	private static final long serialVersionUID = 7742407971545638860L;

	public static final Permission MAY_ADD = new Permission(true, false, Permission.GROUP_COMPANIES, Permission.PERM_ADD, "May Add Companies");
	public static final Permission MAY_UPDATE = new Permission(true, false, Permission.GROUP_COMPANIES, Permission.PERM_UPDATE, "May Update Companies");
	public static final Permission MAY_DELETE = new Permission(true, false, Permission.GROUP_COMPANIES, Permission.PERM_DELETE, "May Delete Companies");

	private static final int NAME_MAX_LENGTH = 50;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Version
	protected int version;

	@Column(name = "name", nullable = false, unique = true, length = NAME_MAX_LENGTH)
	private String name;

	@Column(name = "state", nullable = false, unique = false, length = 1)
	private String state;

	@Column(name = "country", nullable = false, unique = false, length = 2)
	private String country;

	@Column(name = "prefix", nullable = false, unique = false, length = 2)
	private String prefix;

	@Column(name = "lm_userid", nullable = false)
	protected int lastUserID;

	@Column(name = "lm_time", nullable = false)
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public String getName()
	{
		return name;
	}

	public Company setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public String getCountry()
	{
		return country;
	}

	public Company setCountry(String country)
	{
		this.country = country;
		return this;
	}

	public Company setState(String state)
	{
		this.state = state;
		return this;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public Company setPrefix(String prefix)
	{
		this.prefix = prefix;
		return this;
	}

	@Override
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Company setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Company setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return name;
	}

	public static Company findByName(EntityManager em, String name)
	{
		TypedQuery<Company> query = em.createNamedQuery("Company.findByName", Company.class);
		query.setParameter("name", name);
		List<Company> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Company findByID(EntityManager em, int id)
	{
		TypedQuery<Company> query = em.createNamedQuery("Company.findByID", Company.class);
		query.setParameter("id", id);
		List<Company> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Company> findAll(EntityManager em)
	{
		TypedQuery<Company> query = em.createNamedQuery("Company.findAll", Company.class);
		return query.getResultList();
	}

	public static void loadMRD(EntityManager em, Session session) throws RuleCheckException
	{
		Company company = findByName(em, NAME_SUPPLIER);
		if (company == null)
		{
			company = new Company() //
					.setName(NAME_SUPPLIER) //
					.setCountry("ZA") //
					.setPrefix(PREFIX_SUPPLIER) //
					.setState(STATE_ACTIVE);
			AuditEntryContext auditContext = new AuditEntryContext("LOADED_MRD_COMPANY", company.getName());
			company.persist(em, null, session, auditContext);
		}

		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
	}

	@Override
	public void persist(EntityManager em, Company existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, AuditEntry.TYPE_COMPANY, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete Companies");
	}

	@Override
	public void validate(Company existing) throws RuleCheckException
	{
		RuleCheck.notEmpty("name", name, NAME_MAX_LENGTH);
		RuleCheck.notEmpty("country", country, NAME_MAX_LENGTH);
		RuleCheck.oneOf("state", state, STATE_ACTIVE, STATE_SUSPENDED, STATE_DELETED);
	}
}

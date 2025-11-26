package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

// //////////////////////////////////////////////////////////////////////////////////////
//
// State Table - Used for Various persistent states
//
// /////////////////////////////////

@Table(name = "ec_state", uniqueConstraints = { //
		@UniqueConstraint(name = "ec_state_name", columnNames = { "company_id", "name" }) }) //
@Entity
@NamedQueries({ //
		@NamedQuery(name = "State.findByName", query = "SELECT c FROM State c where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "State.findByNameForAll", query = "SELECT c FROM State c where name = :name and companyID is null"), //
		@NamedQuery(name = "State.findByID", query = "SELECT c FROM State c where id = :id and companyID = :companyID") })
public class State implements Serializable, IMasterData<State>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String NAME_DB_VERSION = "DB Version";

	public static final long CURRENT_DB_VERSION = 97;

	private static final int NAME_MAX_LENGTH = 50;

	private static final long serialVersionUID = 912637567237923375L;
	
	final static Logger logger = LoggerFactory.getLogger(State.class);
	
	private static final String configFileName = "database-settings-oltp.xml";
	
	private static final String oltpDdlPersistenceUnit = "ecdsddl";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int id;

	@Column(name = "company_id", nullable = true)
	protected Integer companyID;

	@Version
	protected int version;

	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	protected String name;

	@Column(name = "value", nullable = false)
	protected Long value;

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

	public State setId(int id)
	{
		this.id = id;
		return this;
	}

	public Integer getCompanyID()
	{
		return companyID;
	}

	public State setCompanyID(Integer companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public State setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public State setName(String name)
	{
		this.name = name;
		return this;
	}

	public Long getValue()
	{
		return value;
	}

	public State setValue(Long value)
	{
		this.value = value;
		return this;
	}

	@Override
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public State setLastUserID(int lastUserID)
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
	public State setLastTime(Date lastTime)
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static State findByName(EntityManager em, String name, int companyID)
	{
		TypedQuery<State> query = em.createNamedQuery("State.findByName", State.class);
		query.setParameter("name", name);
		query.setParameter("companyID", companyID);
		List<State> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static State findByName(EntityManager em, String name)
	{
		TypedQuery<State> query = em.createNamedQuery("State.findByNameForAll", State.class);
		query.setParameter("name", name);
		List<State> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static State findByID(EntityManager em, int id, Integer companyID)
	{
		TypedQuery<State> query = em.createNamedQuery("State.findByID", State.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<State> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////

	public static void loadMRD(EntityManager em, Session session, int companyID, String name, Long value) throws RuleCheckException
	{
		State state = State.findByName(em, name, companyID);
		if (state == null)
		{
			state = new State() //
					.setName(name) //
					.setCompanyID(companyID) //
					.setValue(value); //
			AuditEntryContext auditContext = new AuditEntryContext("LOADED_MRD_STATE", state.getName(), state.getValue());
			state.persist(em, null, session, auditContext);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IMasterData
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, State existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			setLastTime(new Date());
			setLastUserID(session.getUserID());
			em.persist(this);
			transaction.commit();
		}
		catch (PersistenceException ex)
		{
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
		}
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete State");
	}

	@Override
	public void validate(State existing) throws RuleCheckException
	{
		RuleCheck.notEmpty("name", name, NAME_MAX_LENGTH);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static boolean verifyDatabaseVersion(ICreditDistribution context)
	{
		EntityManagerFactory factory = null;
		EntityManager em = null;
		try
		{
			factory = context.createOltpEntityManagerFactory(oltpDdlPersistenceUnit, null);
			em = factory.createEntityManager();
			State state = State.findByName(em, NAME_DB_VERSION);
			if (state == null)
			{
				state = new State() //
						.setCompanyID(null) // All
						.setLastTime(new Date()) //
						.setLastUserID(0) //
						.setName(NAME_DB_VERSION) //
						.setValue(CURRENT_DB_VERSION);
				em.getTransaction().begin();
				em.persist(state);
				em.getTransaction().commit();
				String message = String.format("Didn't find Database version descriptor. Assuming version {}", String.valueOf(CURRENT_DB_VERSION));
				logger.warn(message, CURRENT_DB_VERSION);
			}
			else if (state.getValue() != CURRENT_DB_VERSION)
			{
				String message = String.format("Expected database version {} but found {}", String.valueOf(CURRENT_DB_VERSION), String.valueOf(state.getValue()));
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error(message, CURRENT_DB_VERSION, String.valueOf(state.getValue()));
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				return false;
			}
			else
			{
				String message = String.format("Found database version {} as expected", String.valueOf(state.getValue()));
				logger.info(message, CURRENT_DB_VERSION);
				return true;
			}
		}
		catch (Throwable tr)
		{
			logger.error("Failure verifying database version", tr);
			return false;
		}
		finally
		{
			if (em != null)
				em.close();
			if (factory != null)
				factory.close();
		}

		return true;
	}

	public static long getNextNumber(EntityManager em, int companyID, String name, int batchSize)
	{
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			Query query = em.createNativeQuery("UPDATE ec_state SET value = LAST_INSERT_ID(value + :batchSize) WHERE company_id = :companyID and name = :name");
			query.setParameter("companyID", companyID);
			query.setParameter("batchSize", batchSize);
			query.setParameter("name", name);
			query.executeUpdate();
			query = em.createNativeQuery("SELECT LAST_INSERT_ID()");
			long result = ((BigInteger) query.getSingleResult()).longValue();
			transaction.commit();
			return result;
		}

	}

}

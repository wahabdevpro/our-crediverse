package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Table(name = "ec_number", uniqueConstraints = { //
		@UniqueConstraint(name = "ec_number_company", columnNames = { "company_id" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "TransactionNumber.find", query = "SELECT p FROM TransactionNumber p where companyID = :companyID"), //
		@NamedQuery(name = "TransactionNumber.findByID", query = "SELECT p FROM TransactionNumber p where id = :id and companyID = :companyID"), //
		// @NamedQuery(name = "TransactionNumber.update", query = "UPDATE TransactionNumber SET nextValue = LAST_INSERT_ID(nextValue + :batchSize) WHERE companyID = :companyID"), //
})

public class TransactionNumber implements Serializable, ICompanyData<TransactionNumber>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	private static final long serialVersionUID = -9170708289782258394L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected long nextValue;
	protected int lastUserID;
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	public TransactionNumber setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	public TransactionNumber setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	public TransactionNumber setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Column(name = "next_value", nullable = false)
	public long getNextValue()
	{
		return nextValue;
	}

	public TransactionNumber setNextValue(long nextValue)
	{
		this.nextValue = nextValue;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public TransactionNumber setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public TransactionNumber setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public TransactionNumber()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public String toString()
	{
		return String.format("%08d", nextValue);
	}

	public static TransactionNumber find(EntityManager em, int companyID, boolean forUpdate)
	{
		TypedQuery<TransactionNumber> query = em.createNamedQuery("TransactionNumber.find", TransactionNumber.class);
		query.setParameter("companyID", companyID);
		if (forUpdate)
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		List<TransactionNumber> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static TransactionNumber findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<TransactionNumber> query = em.createNamedQuery("TransactionNumber.findByID", TransactionNumber.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<TransactionNumber> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICompanyData
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, TransactionNumber existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		this.lastUserID = session.getUserID();
		this.lastTime = new Date();
		em.persist(this);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete TransactionNumbers");
	}

	@Override
	public void validate(TransactionNumber existing) throws RuleCheckException
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		TransactionNumber number = find(em, companyID, false);
		if (number == null)
		{
			number = new TransactionNumber() //
					.setCompanyID(companyID) //
					.setNextValue(1);
			number.persist(em, null, session, null);
		}

	}

	@Transient
	public String getNext()
	{
		return String.format("%09d", nextValue++);
	}

	public static long getNextBatch(EntityManager em, long batchSize, int companyID)
	{
		Query query = em.createNativeQuery("UPDATE ec_number SET next_value = LAST_INSERT_ID(next_value + :batchSize) WHERE company_id = :companyID");
		query.setParameter("companyID", companyID);
		query.setParameter("batchSize", batchSize);
		query.executeUpdate();
		query = em.createNativeQuery("SELECT LAST_INSERT_ID()");
		return ((BigInteger) query.getSingleResult()).longValue();
	}

}

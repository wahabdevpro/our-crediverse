package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// QualifyingTransaction Table - Used for pending reward transactions
//
///////////////////////////////////

@Table(name = "ep_qualify", indexes = { //
		@Index(name = "ep_qualify_ix1", columnList = "company_id,evaluated,agent_id,start_time"), //
		@Index(name = "ep_qualify_ix2", columnList = "company_id,start_time"), //
		@Index(name = "ep_qualify_ix3", columnList = "company_id,amount_left DESC"), //
})
@Entity
@NamedQueries({
		@NamedQuery(name = "QualifyingTransaction.findCandidates", //
				query = "select t from Transaction t join t.a_Agent a left join t.qualifyingTransaction q " //
						+ "where t.id > :minID and t.companyID = :companyID and a.state <> '" + Agent.STATE_PERMANENT + "'" //
						+ "and t.type in ('" + Transaction.TYPE_SELL_BUNDLE + "','" 
						+ Transaction.TYPE_NON_AIRTIME_DEBIT + "','"
						+ Transaction.TYPE_NON_AIRTIME_REFUND + "','"
						+ Transaction.TYPE_SELL + "','" + Transaction.TYPE_TRANSFER + "') " //
						+ "and t.returnCode = '" + ResponseHeader.RETURN_CODE_SUCCESS + "' and q.id is null order by t.id"), //

		@NamedQuery(name = "QualifyingTransaction.findAgents", query = "select distinct agentID from QualifyingTransaction where " //
				+ "evaluated = 0 and agentID > :minAgent and amountLeft > 0 and companyID = :companyID order by agentID"), //

		@NamedQuery(name = "QualifyingTransaction.findQualifyingForAgent", query = "select q from QualifyingTransaction q where " //
				+ "agentID = :agentID and amountLeft > 0 and companyID = :companyID order by startTime"), //

		@NamedQuery(name = "QualifyingTransaction.cleanout1", query = "delete from QualifyingTransaction p where p.startTime < :before and p.companyID = :companyID"), //
		@NamedQuery(name = "QualifyingTransaction.cleanout2", query = "delete from QualifyingTransaction p where p.amountLeft <= 0 and p.companyID = :companyID"), //

		@NamedQuery(name = "QualifyingTransaction.referenceBundle", query = "SELECT p FROM QualifyingTransaction p where bundleID = :bundleID"), 
		@NamedQuery(name = "QualifyingTransaction.referenceTransferRule", query = "SELECT p FROM QualifyingTransaction p where transferRuleID = :transferRuleID"), 
		@NamedQuery(name = "QualifyingTransaction.referenceServiceClass", query = "SELECT p FROM QualifyingTransaction p where serviceClassID = :serviceClassID"), 
		@NamedQuery(name = "QualifyingTransaction.referenceCell", query = "SELECT p FROM QualifyingTransaction p where cellID = :cellID"), 
})

public class QualifyingTransaction implements Serializable, ICompanyData<QualifyingTransaction>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6156846818800175341L;

	public static final int MAX_DAYS_TO_LIVE = 40;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	protected long id;
	protected int companyID;
	protected int version;
	protected Date startTime;
	protected int agentID;
	protected Integer transferRuleID;
	protected Integer cellID;
	protected Integer serviceClassID;
	protected Integer bundleID;
	protected boolean evaluated = false;
	protected boolean blocked = false;
	protected BigDecimal amountLeft;
	protected int lastUserID;
	protected Date lastTime;

	protected Cell cell;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Id
	public long getId()
	{
		return id;
	}

	public QualifyingTransaction setId(long id)
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

	public QualifyingTransaction setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Column(name = "start_time", nullable = false)
	public Date getStartTime()
	{
		return startTime;
	}

	public QualifyingTransaction setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	@Column(name = "agent_id", nullable = false)
	public int getAgentID()
	{
		return agentID;
	}

	public QualifyingTransaction setAgentID(int agentID)
	{
		this.agentID = agentID;
		return this;
	}

	@Column(name = "rule_id", nullable = true)
	public Integer getTransferRuleID()
	{
		return transferRuleID;
	}

	public QualifyingTransaction setTransferRuleID(Integer transferRuleID)
	{
		this.transferRuleID = transferRuleID;
		return this;
	}

	@Column(name = "cell_id",  nullable = true, insertable = false, updatable = false)
	public Integer getCellID()
	{
		return cellID;
	}

	public QualifyingTransaction setCellID(Integer cellID)
	{
		this.cellID = cellID;
		return this;
	}

	@Column(name = "sc_id", nullable = true)
	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	public QualifyingTransaction setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	@Column(name = "bundle_id", nullable = true)
	public Integer getBundleID()
	{
		return bundleID;
	}

	public QualifyingTransaction setBundleID(Integer bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	@Column(name = "evaluated", nullable = false)
	public boolean isEvaluated()
	{
		return evaluated;
	}

	public QualifyingTransaction setEvaluated(boolean evaluated)
	{
		this.evaluated = evaluated;
		return this;
	}
	
	@Column(name = "blocked", nullable = false)
	public boolean isBlocked()
	{
		return blocked;
	}

	public QualifyingTransaction setBlocked(boolean blocked)
	{
		this.blocked = blocked;
		return this;
	}

	@Column(name = "amount_left", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getAmountLeft()
	{
		return amountLeft;
	}

	public QualifyingTransaction setAmountLeft(BigDecimal amountLeft)
	{
		this.amountLeft = amountLeft;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public QualifyingTransaction setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	public QualifyingTransaction setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public QualifyingTransaction setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "cell_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Cell getCell()
	{
		return cell;
	}

	public QualifyingTransaction setCell(Cell cell)
	{
		this.cell = cell;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public QualifyingTransaction()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static List<Transaction> findCandidates(EntityManager em, long fromTransactionID, int companyID, int maxResults)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("QualifyingTransaction.findCandidates", Transaction.class);
		query.setParameter("minID", fromTransactionID);
		query.setParameter("companyID", companyID);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	public static List<Integer> findUnEvaluatedAgents(EntityManager em, int fromAgentID, int companyID, int maxResults)
	{
		TypedQuery<Integer> query = em.createNamedQuery("QualifyingTransaction.findAgents", Integer.class);
		query.setParameter("minAgent", fromAgentID);
		query.setParameter("companyID", companyID);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	public static List<QualifyingTransaction> findForAgent(EntityManager em, int agentID, int companyID)
	{
		TypedQuery<QualifyingTransaction> query = em.createNamedQuery("QualifyingTransaction.findQualifyingForAgent", QualifyingTransaction.class);
		query.setParameter("agentID", agentID);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static int cleanout(EntityManager em, Date before, int companyID)
	{
		Query query = em.createNamedQuery("QualifyingTransaction.cleanout1");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		int result = query.executeUpdate();
		query = em.createNamedQuery("QualifyingTransaction.cleanout2");
		query.setParameter("companyID", companyID);
		result += query.executeUpdate();
		return result;
	}

	@Override
	public void persist(EntityManager em, QualifyingTransaction existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		em.persist(this);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		em.remove(this);
	}

	@Override
	public void validate(QualifyingTransaction previous) throws RuleCheckException
	{
		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}
	}
	
	public static boolean referencesBundle(EntityManager em, int bundleID)
	{
		TypedQuery<QualifyingTransaction> query = em.createNamedQuery("QualifyingTransaction.referenceBundle", QualifyingTransaction.class);
		query.setParameter("bundleID", bundleID);
		query.setMaxResults(1);
		List<QualifyingTransaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}
	
	public static boolean referencesTransferRule(EntityManager em, int transferRuleID)
	{
		TypedQuery<QualifyingTransaction> query = em.createNamedQuery("QualifyingTransaction.referenceTransferRule", QualifyingTransaction.class);
		query.setParameter("transferRuleID", transferRuleID);
		query.setMaxResults(1);
		List<QualifyingTransaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}
	
	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<QualifyingTransaction> query = em.createNamedQuery("QualifyingTransaction.referenceServiceClass", QualifyingTransaction.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<QualifyingTransaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}
	
	public static boolean referencesCell(EntityManager em, int cellID)
	{
		TypedQuery<QualifyingTransaction> query = em.createNamedQuery("QualifyingTransaction.referenceCell", QualifyingTransaction.class);
		query.setParameter("cellID", cellID);
		query.setMaxResults(1);
		List<QualifyingTransaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}


}

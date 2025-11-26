package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// Batch Table - Used for batch uploading
//
///////////////////////////////////
@Table(name = "eb_batch", uniqueConstraints = { //
		@UniqueConstraint(name = "eb_batch_filename", columnNames = { "company_id", "filename" }) })
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Batch.findByFilename", query = "SELECT p FROM Batch p where filename = :filename and companyID = :companyID"), //
		@NamedQuery(name = "Batch.findByID", query = "SELECT p FROM Batch p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Batch.cleanout", query = "delete from Batch p where p.timestamp < :before and companyID = :companyID"), //
		@NamedQuery(name = "Batch.findOldestID", query = "SELECT min(p.id) FROM Batch p where companyID = :companyID"), //
		@NamedQuery(name = "Batch.findLatest", query = "SELECT p FROM Batch p where companyID = :companyID order by time_stamp desc"), //
})
public class Batch extends hxc.ecds.protocol.rest.Batch implements Serializable, ICompanyData<Batch>, ISecured<Batch>
{
	final static Logger logger = LoggerFactory.getLogger(Batch.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 4499672954456150331L;

	public static final Permission MAY_UPLOAD_CSV = new Permission(false, false, Permission.GROUP_BATCHES, Permission.PERM_UPLOAD, "May Upload CSV Batches");
	public static final Permission MAY_DOWNLOAD_CSV = new Permission(false, true, Permission.GROUP_BATCHES, Permission.PERM_DOWNLOAD, "May Download CSV Batches");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@JsonIgnore
	protected long nextExpectedOffset;
	@JsonIgnore
	protected byte[] residualCryptoBytes;
	@JsonIgnore
	protected String residualText;
	@JsonIgnore
	protected String headings;
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	@Override
	public Batch setId(int id)
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

	@Override
	public Batch setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "filename", nullable = false, length = FILENAME_MAX_LENGTH)
	public String getFilename()
	{
		return filename;
	}

	@Override
	public Batch setFilename(String filename)
	{
		this.filename = filename;
		return this;
	}

	@Override
	@Column(name = "insert_count", nullable = false)
	public int getInsertCount()
	{
		return insertCount;
	}

	@Override
	public Batch setInsertCount(int insertCount)
	{
		this.insertCount = insertCount;
		return this;
	}

	@Override
	@Column(name = "update_count", nullable = false)
	public int getUpdateCount()
	{
		return updateCount;
	}

	@Override
	public Batch setUpdateCount(int updateCount)
	{
		this.updateCount = updateCount;
		return this;
	}

	@Override
	@Column(name = "delete_count", nullable = false)
	public int getDeleteCount()
	{
		return deleteCount;
	}

	@Override
	public Batch setDeleteCount(int deleteCount)
	{
		this.deleteCount = deleteCount;
		return this;
	}

	@Override
	@Column(name = "failure_count", nullable = false)
	public int getFailureCount()
	{
		return failureCount;
	}

	@Override
	public Batch setFailureCount(int failureCount)
	{
		this.failureCount = failureCount;
		return this;
	}

	@Override
	@Column(name = "total_value", nullable = true)
	public BigDecimal getTotalValue()
	{
		return totalValue;
	}

	@Override
	public Batch setTotalValue(BigDecimal totalValue)
	{
		this.totalValue = totalValue;
		return this;
	}
	
	@Override
	@Column(name = "total_value2", nullable = true)
	public BigDecimal getTotalValue2()
	{
		return totalValue2;
	}

	@Override
	public Batch setTotalValue2(BigDecimal totalValue2)
	{
		this.totalValue2 = totalValue2;
		return this;
	}

	@Override
	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public Batch setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "filesize", nullable = true)
	public Long getFileSize()
	{
		return fileSize;
	}

	@Override
	public Batch setFileSize(Long fileSize)
	{
		this.fileSize = fileSize;
		return this;
	}

	@Override
	@Column(name = "webuser_id", nullable = false)
	public int getWebUserID()
	{
		return webUserID;
	}

	@Override
	public Batch setWebUserID(int webUserID)
	{
		this.webUserID = webUserID;
		return this;
	}

	@Override
	@Column(name = "coauth_id", nullable = true)
	public Integer getCoAuthWebUserID()
	{
		return coAuthWebUserID;
	}

	@Override
	public Batch setCoAuthWebUserID(Integer coAuthWebUserID)
	{
		this.coAuthWebUserID = coAuthWebUserID;
		return this;
	}

	@Override
	@Column(name = "time_stamp", nullable = false)
	public Date getTimestamp()
	{
		return timestamp;
	}

	@Override
	public Batch setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	@Override
	@Column(name = "ip_address", nullable = true, length = 45)
	public String getIpAddress()
	{
		return ipAddress;
	}

	@Override
	public Batch setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
		return this;
	}

	@Override
	@Column(name = "mac_address", nullable = true, length = 17)
	public String getMacAddress()
	{
		return macAddress;
	}

	@Override
	public Batch setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
		return this;
	}

	@Override
	@Column(name = "machine_name", nullable = true, length = AuditEntry.MACHINE_NAME_MAX_LENGTH)
	public String getMachineName()
	{
		return machineName;
	}

	@Override
	public Batch setMachineName(String machineName)
	{
		this.machineName = machineName;
		return this;
	}

	@Override
	@Column(name = "domain_name", nullable = false, length = WebUser.DOMAIN_NAME_MAX_LENGTH)
	public String getDomainName()
	{
		return domainName;
	}

	@Override
	public Batch setDomainName(String domainName)
	{
		this.domainName = domainName;
		return this;
	}

	@Column(name = "residual_crypto", nullable = true, length = 256)
	public byte[] getResidualCryptoBytes()
	{
		return residualCryptoBytes;
	}

	public void setResidualCryptoBytes(byte[] residualCryptoBytes)
	{
		this.residualCryptoBytes = residualCryptoBytes;
	}

	@Column(name = "residual_text", nullable = true, length = 1025)
	public String getResidualText()
	{
		return residualText;
	}

	public void setResidualText(String residualText)
	{
		this.residualText = residualText;
	}

	@Column(name = "headings", nullable = true, length = 2048)
	public String getHeadings()
	{
		return headings;
	}

	public void setHeadings(String headings)
	{
		this.headings = headings;
	}

	@Override
	@Column(name = "completed", nullable = false)
	public boolean isCompleted()
	{
		return completed;
	}

	@Override
	public Batch setCompleted(boolean completed)
	{
		this.completed = completed;
		return this;
	}

	@Column(name = "next_offset", nullable = false)
	public long getNextExpectedOffset()
	{
		return nextExpectedOffset;
	}

	public void setNextExpectedOffset(long nextExpectedOffset)
	{
		this.nextExpectedOffset = nextExpectedOffset;
	}

	@Override
	@Column(name = "line_count", nullable = false)
	public int getLineCount()
	{
		return lineCount;
	}

	@Override
	public Batch setLineCount(int lineCount)
	{
		this.lineCount = lineCount;
		return this;
	}

	@Override
	@Column(name = "signature", nullable = false)
	public long getSignature()
	{
		return signature;
	}

	@Override
	public Batch setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	@Transient
	@Override
	public boolean isTamperedWith()
	{
//		tamperedWith = signature != calcSecuritySignature();
		return tamperedWith;
	}

	@Override
	public Batch setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Batch setLastUserID(int lastUserID)
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

	@Override
	public Batch setVersion(int version)
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
	public Batch setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Batch setState(String state)
	{
		this.state = state;
		return this;
	}
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISecured
	//
	// ////////////////////////////////


	@Override
	public long calcSecuritySignature()
	{
		return 0;
//		return new SignCheck() //				
//				.add("companyID", companyID) //
//				.add("filename", filename) //
//				.add("insertCount", insertCount) //
//				.add("updateCount", updateCount) //
//				.add("deleteCount", deleteCount) //
//				.add("failureCount", failureCount) //
//				.add("totalValue", totalValue) //
//				.add("type", type) //
//				.add("fileSize", fileSize) //
//				.add("timestamp", timestamp) //
//				.add("webUserID", webUserID) //
//				.add("ipAddress", ipAddress) //
//				.add("macAddress", macAddress) //
//				.add("machineName", machineName) //
//				.add("domainName", domainName) //
//				.signature();
	}

	@PreUpdate
	@PrePersist
	@Override
	public void onPrePersist()
	{
//		signature = calcSecuritySignature();
//		tamperedWith = false;
	}

	@PostLoad
	@Override
	public void onPostLoad()
	{
//		tamperedWith = calcSecuritySignature() != signature;
//		//TODO: Find a way to write to the logger and not to stderr.
//		if (tamperedWith)
//			logger.warn("Tampering Detected on Batch! BatchID=[{}]", getId());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Batch()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static Batch findByFilename(EntityManager em, int companyID, String filename)
	{
		TypedQuery<Batch> query = em.createNamedQuery("Batch.findByFilename", Batch.class);
		query.setParameter("companyID", companyID);
		query.setParameter("filename", filename);
		List<Batch> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Batch findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Batch> query = em.createNamedQuery("Batch.findByID", Batch.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Batch> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Batch> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Batch.class, params, companyID, "filename");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Batch.class, params, companyID, "filename");
		return query.getSingleResult();
	}

	public static Integer findOldestID(EntityManager em, int companyID)
	{
		TypedQuery<Integer> query = em.createNamedQuery("Batch.findOldestID", Integer.class);
		query.setParameter("companyID", companyID);
		return query.getSingleResult();
	}
	
	public static List<Batch> findLatest(EntityManager em, int count, int companyID)
	{
		TypedQuery<Batch> query = em.createNamedQuery("Batch.findLatest", Batch.class);
		query.setMaxResults(count);		
		query.setParameter("companyID", companyID);
		List<Batch> results = query.getResultList();
		return results;
	}

	// Clean-out old Batch Entries
	public static int cleanout(EntityManager em, Date before, int companyID)
	{
		Query query = em.createNamedQuery("Batch.cleanout");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_UPLOAD_CSV, session);
		Permission.loadMRD(em, MAY_DOWNLOAD_CSV, session);
	}

	@Override
	public void persist(EntityManager em, Batch existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		this.lastTime = new Date();
		this.lastUserID = session.getUserID();
		em.persist(this);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		// Not Allowed
	}

	@Override
	public void validate(Batch previous) throws RuleCheckException
	{
		RuleCheck.notLonger("type", type, TYPE_MAX_LENGTH);
		RuleCheck.notLonger("filename", filename, FILENAME_MAX_LENGTH);
		RuleCheck.notLonger("machineName", machineName, AuditEntry.MACHINE_NAME_MAX_LENGTH);
		RuleCheck.notLonger("domainName", domainName, AuditEntry.MACHINE_NAME_MAX_LENGTH);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}
	}

}

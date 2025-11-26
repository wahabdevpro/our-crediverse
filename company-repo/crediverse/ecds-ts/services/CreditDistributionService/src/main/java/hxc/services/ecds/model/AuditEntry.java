package hxc.services.ecds.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.AuditEntryContextTranslations;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

////////////////////////////////////////////////////////////////////////////////////////
//
// AuditEntry Table - Used for Security checks
//
///////////////////////////////////

@Table(name = "es_audit")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "AuditEntry.findByID", query = "SELECT p FROM AuditEntry p where id = :id and companyID = :companyID"), //

		@NamedQuery(name = "AuditEntry.referenceAgent", query = "SELECT p FROM AuditEntry p where -userID = :agentID"), //

		@NamedQuery(name = "AuditEntry.referenceWebUser", query = "SELECT p FROM AuditEntry p where userID = :webUserID"), //

		@NamedQuery(name = "AuditEntry.referenceAgentUser", query = "SELECT p FROM AuditEntry p where agentUserID = :agentUserID"), //
		
		@NamedQuery(name = "AuditEntry.findLatest", query = "SELECT p FROM AuditEntry p WHERE companyID = :companyID ORDER BY time_stamp desc"), //

		@NamedQuery(name = "AuditEntry.cleanout", query = "delete from AuditEntry p where p.timestamp < :before and companyID = :companyID"), //
})

public class AuditEntry extends hxc.ecds.protocol.rest.AuditEntry //
		implements Serializable, ICompanyData<AuditEntry>, ISecured<AuditEntry>
{
	final static Logger logger = LoggerFactory.getLogger(AuditEntry.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6725682717348990288L;

	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_AUDIT_LOG, Permission.PERM_VIEW, "May View Audit Entries");

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
	protected String reasonCode;
	@JsonIgnore
	protected String reasonAttributes;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId()
	{
		return id;
	}

	@Override
	public AuditEntry setId(long id)
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
	public AuditEntry setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "seq_no", nullable = false, length = SEQUENCE_NO_MAX_LENGTH)
	public String getSequenceNo()
	{
		return this.sequenceNo;
	}

	@Override
	public AuditEntry setSequenceNo(String sequenceNo)
	{
		this.sequenceNo = sequenceNo;
		return this;
	}

	@Override
	@Column(name = "webuser_id", nullable = false)
	public int getUserID()
	{
		return userID;
	}

	@Override
	public AuditEntry setUserID(int userID)
	{
		this.userID = userID;
		return this;
	}
	
		@Override
	@Column(name = "agentuser_id", nullable = true)
	public Integer getAgentUserID()
	{
		return agentUserID;
	}

	@Override
	public AuditEntry setAgentUserID(Integer agentUserID)
	{
		this.agentUserID = agentUserID;
		return this;
	}

	@Override
	@Column(name = "time_stamp", nullable = false)
	public Date getTimestamp()
	{
		return timestamp;
	}

	@Override
	public AuditEntry setTimestamp(Date timestamp)
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
	public AuditEntry setIpAddress(String ipAddress)
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
	public AuditEntry setMacAddress(String macAddress)
	{
		this.macAddress = macAddress;
		return this;
	}

	@Override
	@Column(name = "machine_name", nullable = true, length = MACHINE_NAME_MAX_LENGTH)
	public String getMachineName()
	{
		return machineName;
	}

	@Override
	public AuditEntry setMachineName(String machineName)
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
	public AuditEntry setDomainName(String domainName)
	{
		this.domainName = domainName;
		return this;
	}

	@Override
	@Column(name = "data_type", nullable = false, length = 15)
	public String getDataType()
	{
		return dataType;
	}

	@Override
	public AuditEntry setDataType(String dataType)
	{
		this.dataType = dataType;
		return this;
	}

	@Override
	@Column(name = "action", nullable = false, length = 1)
	public String getAction()
	{
		return action;
	}

	@Override
	public AuditEntry setAction(String action)
	{
		this.action = action;
		return this;
	}

	@Override
	@Column(name = "old_value", nullable = true, columnDefinition = "LONGTEXT")
	public String getOldValue()
	{
		return oldValue;
	}

	@Override
	public AuditEntry setOldValue(String oldValue)
	{
		this.oldValue = oldValue;
		return this;
	}

	@Override
	@Column(name = "new_value", nullable = true, columnDefinition = "LONGTEXT")
	public String getNewValue()
	{
		return newValue;
	}

	@Override
	public AuditEntry setNewValue(String newValue)
	{
		this.newValue = newValue;
		return this;
	}

	@Override
	@Column(name = "signature", nullable = false)
	public long getSignature()
	{
		return signature;
	}

	@Override
	public AuditEntry setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	@Column(name = "lm_userid", nullable = false)
	@Override
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public AuditEntry setLastUserID(int lastUserID)
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
	@Version
	public AuditEntry setVersion(int version)
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
	public AuditEntry setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Transient
	@Override
	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	@Override
	public AuditEntry setTamperedWith(boolean tamperedWith)
	{
		this.tamperedWith = tamperedWith;
		return this;
	}
	
	@Column(name = "reason_code", nullable = true, length = 64)
	public String getReasonCode()
	{
		return this.reasonCode;
	}
	
	public AuditEntry setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
		return this;
	}
	
	@Column(name = "reason_attributes", nullable = true, columnDefinition = "TEXT")
	public String getReasonAttributes()
	{
		return this.reasonAttributes;
	}

	public AuditEntry setReasonAttributes(String reasonAttributes)
	{
		this.reasonAttributes = reasonAttributes;
		return this;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public AuditEntry()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Minimum Required Data
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static AuditEntry findByID(EntityManager em, long id, int companyID, String languageID)
	{
		TypedQuery<AuditEntry> query = em.createNamedQuery("AuditEntry.findByID", AuditEntry.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<AuditEntry> results = query.getResultList();
		AuditEntry auditEntry = results.size() == 0 ? null : results.get(0);
		AuditEntry.applyTranslation(auditEntry, languageID);
		return auditEntry;
	}

	public static List<AuditEntry> findAll(EntityManager em, RestParams params, int companyID, String languageID)
	{
		List<AuditEntry> auditEntries = QueryBuilder.getQueryResultList(em, AuditEntry.class, params, companyID, //
				"sequenceNo", "dataType", "domainName", "machineName", "ipAddress", "macAddress");
		AuditEntry.applyTranslations(auditEntries, languageID);
		return auditEntries;
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, AuditEntry.class, params, companyID, //
				"sequenceNo", "dataType", "domainName", "machineName", "ipAddress", "macAddress");
		return query.getSingleResult();
	}

	public static boolean referencesAgent(EntityManager em, int agentID)
	{
		TypedQuery<AuditEntry> query = em.createNamedQuery("AuditEntry.referenceAgent", AuditEntry.class);
		query.setParameter("agentID", agentID);
		query.setMaxResults(1);
		List<AuditEntry> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesWebUser(EntityManager em, int webUserID)
	{
		TypedQuery<AuditEntry> query = em.createNamedQuery("AuditEntry.referenceWebUser", AuditEntry.class);
		query.setParameter("webUserID", webUserID);
		query.setMaxResults(1);
		List<AuditEntry> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesAgentUser(EntityManager em, int agentUserID)
	{
		TypedQuery<AuditEntry> query = em.createNamedQuery("AuditEntry.referenceAgentUser", AuditEntry.class);
		query.setParameter("agentUserID", agentUserID);
		query.setMaxResults(1);
		List<AuditEntry> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static List<AuditEntry> findLatest(EntityManager em, int count, int companyID, String languageID)
	{
		TypedQuery<AuditEntry> query = em.createNamedQuery("AuditEntry.findLatest", AuditEntry.class);
		query.setMaxResults(count);		
		query.setParameter("companyID", companyID);
		List<AuditEntry> results = query.getResultList();
		//AuditEntry.applyTranslations(auditEntries, languageCode);
		return results;
	}
	
	// Clean-out old AuditEntries
	public static int cleanout(EntityManager em, Date before, int companyID)
	{
		Query query = em.createNamedQuery("AuditEntry.cleanout");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

	@Override
	public void persist(EntityManager em, AuditEntry previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Audit Entries cannot be persisted directly");
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Audit Entries cannot be deleted");
	}

	@Override
	public void validate(AuditEntry previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
			throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Audit Entries cannot be changed");

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISecured
	//
	// /////////////////////////////////
	@Override
	public long calcSecuritySignature()
	{
		return 0;
//		return new SignCheck() //
//				.add("companyID", companyID) //
//				.add("sequenceNo", sequenceNo) //
//				.add("userID", userID) //
//				.add("timestamp", timestamp) //
//				.add("ipAddress", ipAddress) //
//				.add("macAddress", macAddress) //
//				.add("machineName", machineName) //
//				.add("domainName", domainName) //
//				.add("dataType", dataType) //
//				.add("action", action) //
//				.add("oldValue", oldValue) //
//				.add("newValue", newValue) //
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
//		long newSignature = calcSecuritySignature();		
//		tamperedWith = newSignature != signature;
//		//TODO: Find a way to write to the logger and not to stderr.
//		if (tamperedWith)
//			logger.warn("Tampering Detected on AuditEntry! AuditEntryId=[{}] (newSignature = {}, signature = {})!", getId(), newSignature, signature);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// Create an Audit Entry for a new/updates/deleted IMasterData object
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public static <T> void log(EntityManager em, Object oldContent, IMasterData<T> newValue, Session session, String dataType,
							   AuditEntryContext auditEntryContext) throws RuleCheckException {
		log(em, oldContent, newValue, session, dataType, newValue, auditEntryContext);
	}

	public static <T> void log(EntityManager em, Object oldValue, IMasterData<T> newValue, Session session, String dataType,
							   Object newContent, AuditEntryContext auditEntryContext) throws RuleCheckException {
		// Null dataType means ignore
		if (dataType == null || dataType.isEmpty() || auditEntryContext.isSkipAuditLog())
			return;
		
		// Determine the Action
		String action;
		if (oldValue == null)
		{
			if (newValue == null)
				throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, null, "Both old and new values cannot be null");
			action = ACTION_CREATE;
		}
		else if (newValue != null)
		{
			action = ACTION_UPDATE;
		}
		else
		{
			action = ACTION_DELETE;
		}

		// Convert old/new values to Json
		String oldJson = null, newJson = null, reasonAttributes = null;
		try
		{
			if (oldValue != null)
			{
				if (oldValue instanceof ISecured<?>)
				{
					inititalizeTamperingFlag((ISecured<?>) oldValue);
				}
				oldJson = mapper.writeValueAsString(oldValue);
			}
			if (newContent != null)
			{
				if (newContent instanceof ISecured<?>)
				{
					inititalizeTamperingFlag((ISecured<?>) newContent);
				}
				newJson = mapper.writeValueAsString(newContent);
			}
			if(auditEntryContext.getAttributes() != null)
			{
				reasonAttributes = mapper.writeValueAsString(auditEntryContext.getAttributes());
			}
		}
		catch (JsonProcessingException e)
		{
			logger.error("", e);
			// Best Effort
		}

		// Create an Audit Entry
		AuditEntry auditEntry = new AuditEntry() //
				.setCompanyID(session.getCompanyID()) //
				.setSequenceNo(getSequenceNumber()) //
				.setTimestamp(newValue != null ? newValue.getLastTime() : new Date()) //
				.setUserID(session.isUserIDValid()?session.getUserID():0) //The userID in the session is not valid pre-authentication for incrementing attempts
				.setIpAddress(session.getIpAddress()) //
				.setMacAddress(session.getMacAddress()) //
				.setMachineName(session.getMachineName()) //
				.setDomainName(session.getDomainAccountName()) //
				.setDataType(dataType) //
				.setAction(action) //
				.setOldValue(oldJson) //
				.setNewValue(newJson)
				.setReasonCode(auditEntryContext.getReason())
				.setReasonAttributes(reasonAttributes);
		
		
		
		IAgentUser sessionAgentUser = session.getAgentUser();
		if (sessionAgentUser != null && sessionAgentUser instanceof AgentUser)
			auditEntry.setAgentUserID(sessionAgentUser.getId());

		// Set the Security Signature
		auditEntry //
				.setLastUserID(auditEntry.getUserID()) //
				.setLastTime(auditEntry.getTimestamp()); //

		// Persist to table
		em.persist(auditEntry);
	}
	
	public static <T> AuditEntry forBatch(Object oldValue, IMasterData<T> newValue, Batch batch, String dataType, Object newContent, AuditEntryContext auditEntryContext) 
	{
		// Null dataType means ignore
		if (dataType == null || dataType.isEmpty())
			return null;
		
		// Determine the Action
		String action;
		if (oldValue == null)
		{
			action = ACTION_CREATE;
		}
		else if (newValue != null)
		{
			action = ACTION_UPDATE;
		}
		else
		{
			action = ACTION_DELETE;
		}

		// Convert old/new values to Json
		String oldJson = null, newJson = null, reasonAttributes = null;
		try
		{
			if (oldValue != null)
			{
				if (oldValue instanceof ISecured<?>)
				{
					inititalizeTamperingFlag((ISecured<?>) oldValue);
				}
				oldJson = mapper.writeValueAsString(oldValue);
			}
			if (newContent != null)
			{
				if (newContent instanceof ISecured<?>)
				{
					inititalizeTamperingFlag((ISecured<?>) newContent);
				}
				newJson = mapper.writeValueAsString(newContent);
			}
			if(auditEntryContext.getAttributes() != null)
			{
				reasonAttributes = mapper.writeValueAsString(auditEntryContext.getAttributes());
			}
		}
		catch (JsonProcessingException e)
		{
			logger.error("", e);
			// Best Effort
		}

		// Create an Audit Entry
		AuditEntry auditEntry = new AuditEntry() //
				.setCompanyID(batch.getCompanyID()) //
				.setSequenceNo(getSequenceNumber()) //
				.setTimestamp(batch.getTimestamp()) //
				.setUserID(batch.getWebUserID()) //
				.setIpAddress(batch.getIpAddress()) //
				.setMacAddress(batch.getMacAddress()) //
				.setMachineName(batch.getMachineName()) //
				.setDomainName(batch.getDomainName()) //
				.setDataType(dataType) //
				.setAction(action) //
				.setOldValue(oldJson) //
				.setNewValue(newJson)
				.setReasonCode(auditEntryContext.getReason())
				.setReasonAttributes(reasonAttributes);

		// Set the Security Signature
		auditEntry //
				.setLastUserID(auditEntry.getUserID()) //
				.setLastTime(auditEntry.getTimestamp()); //
		auditEntry.onPrePersist();
		
		return auditEntry;
	}



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private static final AtomicInteger counter = new AtomicInteger(1);

	private static String getSequenceNumber()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		return String.format("%s%05d", sdf.format(new Date()), counter.getAndIncrement() % 100000);
	}

	public static boolean isValidMacAddress(String macAddress)
	{
		if (macAddress == null || macAddress.isEmpty())
			return false;

		return VALID_MAC_PATTERN.matcher(macAddress).matches();

	}

	public static boolean isValidIpAddress(String ipAddress)
	{
		if (ipAddress == null || ipAddress.isEmpty())
			return false;

		return VALID_IPV4_PATTERN.matcher(ipAddress).matches() //
				|| VALID_IPV6_PATTERN.matcher(ipAddress).matches();
	}

	public static boolean isValidMachineName(String machineName)
	{
		if (machineName == null || machineName.isEmpty())
			return false;

		return machineName.length() <= MACHINE_NAME_MAX_LENGTH;
	}

	private static void inititalizeTamperingFlag(ISecured<?> entity)
	{
		if (entity == null || !entity.isTamperedWith())
			return;

//		if (entity.getSignature() == 0)
//			entity.setTamperedWith(false);
//		else {
//			long currentSignature = entity.getSignature();
//			long calculatedSignature = entity.calcSecuritySignature();
//			entity.setTamperedWith(currentSignature != calculatedSignature);
			entity.setTamperedWith(false);
//		}
	}
	
	private static void applyTranslations(List<AuditEntry> auditEntries, String languageCode)
	{
		auditEntries.forEach(auditEntry->{
			applyTranslation(auditEntry, languageCode);
		});
	}
	
	private static void applyTranslation(AuditEntry auditEntry, String languageCode)
	{
		String reason = AuditEntryContextTranslations.translateReason(auditEntry.getReasonCode(), languageCode);
		Object[] attributes;
		if(auditEntry.getReasonAttributes() != null && !auditEntry.getReasonAttributes().isEmpty())
		{
			try {
				attributes = mapper.readValue(auditEntry.getReasonAttributes(), Object[].class);
				String reasonDetail = AuditEntryContextTranslations.translateReasonDetail(auditEntry.getReasonCode(), languageCode, attributes);
				auditEntry.setReason(reason);
				auditEntry.setReasonDetail(reasonDetail);
			} catch (IOException e) {
				logger.error(String.format("Failed to read and parse audit entry context attributes. reasonAttributes=%s", auditEntry.getReasonAttributes()),e);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Debugging ...
	//
	// /////////////////////////////////
    public String describe()
    {
        return describe("");
    }

    public String describe(String extra)
    {
        return String.format("%s(id = '%s', companyID = '%s', version = '%s', sequenceNo = '%s', timestamp = '%s', reason = '%s', reasonAttributes = '%s', userID = '%s', ipAddress = '%s', macAddress = '%s', machineName = '%s', domainName = '%s', dataType = '%s', action = '%s', oldValue = '%s', newValue = '%s', signature = '%s', tamperedWith = '%s', lastUserID = '%s', lastTime = '%s'%s%s)",
            this, id, companyID, version, sequenceNo, Describer.describe(timestamp), reason, reasonAttributes, userID, ipAddress, macAddress, machineName, domainName, dataType, action, oldValue, newValue, signature, tamperedWith, lastUserID, Describer.describe(lastTime),
            (extra.isEmpty() ? "" : ", "), extra);
    }
}

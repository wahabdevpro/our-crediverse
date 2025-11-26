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
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.TransferRule;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// //////////////////////////////////////////////////////////////////////////////////////
//
// Batch Stage Table - Used for hold temporary data
//
// /////////////////////////////////

@Table(name = "eb_stage")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Stage.expected", query = "select count(s) from Stage s where batchID = :batchID and action = :action and tableID = :tableID"), //
		@NamedQuery(name = "Stage.findByRecords", query = "select s from Stage s where batchID = :batchID and action = :action and tableID = :tableID and companyID = :companyID order by lineNo"),
		@NamedQuery(name = "Stage.findCell", query = "select s from Stage s where batchID = :batchID and action = :action and tableID = :tableID and i1 = :i1 and i2 = :i2 and i3 = :i3 and i4 = :i4"),
		@NamedQuery(name = "Stage.cleanout", query = "delete from Stage p where p.batchID < :oldestBatchID and companyID = :companyID"), //
})
public class Stage implements Serializable, IMasterData<Stage>
{
	final static Logger logger = LoggerFactory.getLogger(Stage.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int ACTION_INSERT = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_DELETE = 3;

	public static final int NAME_MAX_LENGTH = TransferRule.NAME_MAX_LENGTH;
	public static final int DESCRIPTION_MAX_LENGTH = AdjustmentRequest.REASON_MAX_LENGTH;
	public static final int TYPE_MAX_LENGTH = 1;
	public static final int STATE_MAX_LENGTH = 1;
	public static final int ACCOUNT_NUMBER_MAX_LENGTH = Agent.ACCOUNT_NO_MAX_LENGTH;
	public static final int ALT_PHONE_NUMBER_MAX_LENGTH = Agent.PHONE_NUMBER_MAX_LENGTH;
	public static final int AUTH_METHOD_MAX_LENGTH = 1;
	public static final int DOMAIN_ACCOUNT_NAME_MAX_LENGTH = Agent.DOMAIN_NAME_MAX_LENGTH;
	public static final int IMEI_MAX_LENGTH = Agent.IMEI_MAX_LENGTH;
	public static final int IMSI_MAX_LENGTH = Agent.IMSI_MAX_LENGTH;
	public static final int INITIALS_MAX_LENGTH = Agent.INITIALS_MAX_LENGTH;
	public static final int LANGUAGE_MAX_LENGTH = Agent.LANGUAGE_MAX_LENGTH;
	public static final int MOBILE_NUMBER_MAX_LENGTH = Agent.PHONE_NUMBER_MAX_LENGTH;
	public static final int POSTAL_ADDRESS_CITY_MAX_LENGTH = Agent.PLACE_MAX_LENGTH;
	public static final int POSTAL_ADDRESS_LINE_1_MAX_LENGTH = Agent.ADDRESS_LINE_MAX_LENGTH;
	public static final int POSTAL_ADDRESS_LINE_2_MAX_LENGTH = Agent.ADDRESS_LINE_MAX_LENGTH;
	public static final int POSTAL_ADDRESS_SUBURB_MAX_LENGTH = Agent.PLACE_MAX_LENGTH;
	public static final int POSTAL_ADDRESS_ZIP_MAX_LENGTH = Agent.ZIP_MAX_LENGTH;
	public static final int STREET_ADDRESS_CITY_MAX_LENGTH = Agent.PLACE_MAX_LENGTH;
	public static final int STREET_ADDRESS_LINE_1_MAX_LENGTH = Agent.ADDRESS_LINE_MAX_LENGTH;
	public static final int STREET_ADDRESS_LINE_2_MAX_LENGTH = Agent.ADDRESS_LINE_MAX_LENGTH;
	public static final int STREET_ADDRESS_SUBURB_MAX_LENGTH = Agent.PLACE_MAX_LENGTH;
	public static final int STREET_ADDRESS_ZIP_MAX_LENGTH = Agent.ZIP_MAX_LENGTH;
	public static final int TITLE_MAX_LENGTH = Agent.TITLE_MAX_LENGTH;
	public static final int ZIP_MAX_LENGTH = 10;
	public static final int KEY_MAX_LENGTH = Agent.KEY_MAX_LENGTH;
	public static final int EMAIL_MAX_LENGTH = Agent.EMAIL_MAX_LENGTH;
	public static final int SEQUENCE_NO_MAX_LENGTH = AuditEntry.SEQUENCE_NO_MAX_LENGTH;

	private static final long serialVersionUID = 8913594932894253752L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int id;

	@Column(name = "action", nullable = false)
	protected int action;

	@Column(name = "batch_id", nullable = false)
	protected int batchID;

	@Column(name = "line_no", nullable = false)
	protected int lineNo;

	@Version
	protected int version;

	@Column(name = "table_id", nullable = false)
	protected int tableID;

	@Column(name = "b1", nullable = true)
	protected Boolean b1;

	@Column(name = "b2", nullable = true)
	protected Boolean b2;

	@Column(name = "bd1", nullable = true)
	protected BigDecimal bd1;

	@Column(name = "bd2", nullable = true)
	protected BigDecimal bd2;

	@Column(name = "bd3", nullable = true)
	protected BigDecimal bd3;

	@Column(name = "bd4", nullable = true)
	protected BigDecimal bd4;

	@Column(name = "bd5", nullable = true)
	protected BigDecimal bd5;

	@Column(name = "d1", nullable = true)
	protected Date d1;

	@Column(name = "d2", nullable = true)
	protected Date d2;

	@Column(name = "d3", nullable = true)
	protected Date d3;

	@Column(name = "d4", nullable = true)
	protected Date d4;

	@Column(name = "d5", nullable = true)
	protected Date d5;
	
	@Column(name = "s1", nullable = true, length = 10)
	protected String s1;

	@Column(name = "company_id", nullable = false)
	protected int companyID;

	@Column(name = "description", nullable = true, length = DESCRIPTION_MAX_LENGTH)
	protected String description;

	@Column(name = "entity_id", nullable = true)
	protected Integer entityID;

	@Column(name = "entity_version", nullable = true)
	protected Integer entityVersion;

	@Column(name = "i1", nullable = true)
	protected Integer i1;

	@Column(name = "i2", nullable = true)
	protected Integer i2;

	@Column(name = "i3", nullable = true)
	protected Integer i3;

	@Column(name = "i4", nullable = true)
	protected Integer i4;

	@Column(name = "i5", nullable = true)
	protected Integer i5;

	@Column(name = "l1", nullable = true)
	protected Long l1;

	@Column(name = "r1", nullable = true)
	protected Double r1;

	@Column(name = "r2", nullable = true)
	protected Double r2;

	@Column(name = "lm_time", nullable = true)
	protected Date lastTime;

	@Column(name = "lm_userid", nullable = false)
	protected int lastUserID;

	@Column(name = "code", nullable = true, length = NAME_MAX_LENGTH)
	protected String code;

	@Column(name = "name", nullable = true, length = NAME_MAX_LENGTH)
	protected String name;

	@Column(name = "state", nullable = true, length = STATE_MAX_LENGTH)
	protected String state;

	@Column(name = "tier_id1", nullable = true)
	protected Integer tierID1;

	@Column(name = "tier_id2", nullable = true)
	protected Integer tierID2;

	@Column(name = "type", nullable = true, length = TYPE_MAX_LENGTH)
	protected String type;

	@Column(name = "area_id", nullable = true)
	protected Integer areaID;

	@Column(name = "group_id", nullable = true)
	protected Integer groupID;

	@Column(name = "sc_id", nullable = true)
	protected Integer serviceClassID;

	@Column(name = "signature", nullable = true)
	protected Long signature;

	@Column(name = "agent_id", nullable = true)
	protected Integer agentID;

	@Column(name = "acc_no", nullable = true, length = ACCOUNT_NUMBER_MAX_LENGTH)
	protected String accountNumber;

	@Column(name = "alt_phone", nullable = true, length = ALT_PHONE_NUMBER_MAX_LENGTH)
	protected String altPhoneNumber;

	@Column(name = "auth_method", nullable = true, length = AUTH_METHOD_MAX_LENGTH)
	protected String authenticationMethod;

	@Column(name = "domain_account", nullable = true, length = DOMAIN_ACCOUNT_NAME_MAX_LENGTH)
	protected String domainAccountName;

	@Column(name = "imei", nullable = true, length = IMEI_MAX_LENGTH)
	protected String imei;

	@Column(name = "imsi", nullable = true, length = IMSI_MAX_LENGTH)
	protected String imsi;

	@Column(name = "intitials", nullable = true, length = INITIALS_MAX_LENGTH)
	protected String initials;

	@Column(name = "language", nullable = true, length = LANGUAGE_MAX_LENGTH)
	protected String language;

	@Column(name = "msisdn", nullable = true, length = MOBILE_NUMBER_MAX_LENGTH)
	protected String mobileNumber;

	/*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
	 */
	/*@Column(name = "msisdn_recycled", nullable = true, length = 1)
	protected Boolean msisdnRecycled;*/

	@Column(name = "postal_city", nullable = true, length = POSTAL_ADDRESS_CITY_MAX_LENGTH)
	protected String postalAddressCity;

	@Column(name = "postal1", nullable = true, length = POSTAL_ADDRESS_LINE_1_MAX_LENGTH)
	protected String postalAddressLine1;

	@Column(name = "postal2", nullable = true, length = POSTAL_ADDRESS_LINE_2_MAX_LENGTH)
	protected String postalAddressLine2;

	@Column(name = "postal_suburb", nullable = true, length = POSTAL_ADDRESS_SUBURB_MAX_LENGTH)
	protected String postalAddressSuburb;

	@Column(name = "postal_zip", nullable = true, length = POSTAL_ADDRESS_ZIP_MAX_LENGTH)
	protected String postalAddressZip;

	@Column(name = "street_city", nullable = true, length = STREET_ADDRESS_CITY_MAX_LENGTH)
	protected String streetAddressCity;

	@Column(name = "street1", nullable = true, length = STREET_ADDRESS_LINE_1_MAX_LENGTH)
	protected String streetAddressLine1;

	@Column(name = "street2", nullable = true, length = STREET_ADDRESS_LINE_2_MAX_LENGTH)
	protected String streetAddressLine2;

	@Column(name = "street_suburb", nullable = true, length = STREET_ADDRESS_SUBURB_MAX_LENGTH)
	protected String streetAddressSuburb;

	@Column(name = "street_zip", nullable = true, length = STREET_ADDRESS_ZIP_MAX_LENGTH)
	protected String streetAddressZip;

	@Column(name = "title", nullable = true, length = TITLE_MAX_LENGTH)
	protected String title;

	@Column(name = "zip", nullable = true, length = ZIP_MAX_LENGTH)
	protected String zip;

	@Column(name = "key1", nullable = true, length = KEY_MAX_LENGTH)
	protected byte[] key;

	@Column(name = "email", nullable = true, length = EMAIL_MAX_LENGTH)
	protected String email;

	@Column(name = "seq_no", nullable = true, length = SEQUENCE_NO_MAX_LENGTH)
	protected String sequenceNo;

	@Column(name = "audit_action", nullable = true, length = 1)
	protected String auditAction;

	@Column(name = "old_value", nullable = true, columnDefinition = "TEXT")
	protected String oldValue;

	@Column(name = "new_value", nullable = true, columnDefinition = "TEXT")
	protected String newValue;

	@Column(name = "audit_signature", nullable = true)
	protected Long auditSignature;

	@Column(name = "max_report_count", nullable = true)
	protected Integer reportCountLimit;
	
	@Column(name = "max_report_daily_schedule_count", nullable = true)
	protected Integer reportDailyScheduleLimit;
	
	@Column(name = "area_ids", nullable = true, length = 65535, columnDefinition = "TEXT")
	protected String areaIds;
	
	@Column(name = "cell_group_ids", nullable = true, length = 65535, columnDefinition = "TEXT")
	protected String cellGroupIds;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Stage setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getAction()
	{
		return action;
	}

	public Stage setAction(int action)
	{
		this.action = action;
		return this;
	}

	public int getBatchID()
	{
		return batchID;
	}

	public Stage setBatchID(int batchID)
	{
		this.batchID = batchID;
		return this;
	}

	public int getLineNo()
	{
		return lineNo;
	}

	public Stage setLineNo(int lineNo)
	{
		this.lineNo = lineNo;
		return this;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public Stage setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public int getTableID()
	{
		return tableID;
	}

	public Stage setTableID(int tableID)
	{
		this.tableID = tableID;
		return this;
	}

	public Boolean getB1()
	{
		return b1;
	}

	public Stage setB1(Boolean b1)
	{
		this.b1 = b1;
		return this;
	}

	public Boolean getB2()
	{
		return b2;
	}

	public Stage setB2(Boolean b2)
	{
		this.b2 = b2;
		return this;
	}

	public Double getR1()
	{
		return r1;
	}

	public Stage setR1(Double r1)
	{
		this.r1 = r1;
		return this;
	}

	public Double getR2()
	{
		return r2;
	}

	public Stage setR2(Double r2)
	{
		this.r2 = r2;
		return this;
	}

	public BigDecimal getBd1()
	{
		return bd1;
	}

	public Stage setBd1(BigDecimal bd1)
	{
		this.bd1 = bd1;
		return this;
	}

	public BigDecimal getBd2()
	{
		return bd2;
	}

	public Stage setBd2(BigDecimal bd2)
	{
		this.bd2 = bd2;
		return this;
	}

	public BigDecimal getBd3()
	{
		return bd3;
	}

	public Stage setBd3(BigDecimal bd3)
	{
		this.bd3 = bd3;
		return this;
	}

	public BigDecimal getBd4()
	{
		return bd4;
	}

	public Stage setBd4(BigDecimal bd4)
	{
		this.bd4 = bd4;
		return this;
	}

	public BigDecimal getBd5()
	{
		return bd5;
	}

	public Stage setBd5(BigDecimal bd5)
	{
		this.bd5 = bd5;
		return this;
	}

	public Date getD1()
	{
		return d1;
	}

	public Stage setD1(Date d1)
	{
		this.d1 = d1;
		return this;
	}

	public Date getD2()
	{
		return d2;
	}

	public Stage setD2(Date d2)
	{
		this.d2 = d2;
		return this;
	}

	public Date getD3()
	{
		return d3;
	}

	public Stage setD3(Date d3)
	{
		this.d3 = d3;
		return this;
	}

	public Date getD4()
	{
		return d4;
	}

	public Stage setD4(Date d4)
	{
		this.d4 = d4;
		return this;
	}

	public Date getD5()
	{
		return d5;
	}

	public Stage setD5(Date d5)
	{
		this.d5 = d5;
		return this;
	}
	
	public String getS1()
	{
		return s1;
	}

	public Stage setS1(String s1)
	{
		this.s1 = s1;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Stage setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Stage setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public Integer getEntityID()
	{
		return entityID;
	}

	public Stage setEntityID(Integer entityID)
	{
		this.entityID = entityID;
		return this;
	}

	public Integer getEntityVersion()
	{
		return entityVersion;
	}

	public Stage setEntityVersion(Integer entityVersion)
	{
		this.entityVersion = entityVersion;
		return this;
	}

	public Integer getI1()
	{
		return i1;
	}

	public Stage setI1(Integer i1)
	{
		this.i1 = i1;
		return this;
	}

	public Integer getI2()
	{
		return i2;
	}

	public Stage setI2(Integer i2)
	{
		this.i2 = i2;
		return this;
	}

	public Integer getI3()
	{
		return i3;
	}

	public Stage setI3(Integer i3)
	{
		this.i3 = i3;
		return this;
	}

	public Integer getI4()
	{
		return i4;
	}

	public Stage setI4(Integer i4)
	{
		this.i4 = i4;
		return this;
	}

	public Integer getI5()
	{
		return i5;
	}

	public Stage setI5(Integer i5)
	{
		this.i5 = i5;
		return this;
	}

	public Long getL1()
	{
		return l1;
	}

	public Stage setL1(Long l1)
	{
		this.l1 = l1;
		return this;
	}

	@Override
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Stage setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Stage setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	public String getCode()
	{
		return code;
	}

	public Stage setCode(String code)
	{
		this.code = code;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Stage setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Stage setState(String state)
	{
		this.state = state;
		return this;
	}

	public Integer getTierID1()
	{
		return tierID1;
	}

	public Stage setTierID1(Integer tierID1)
	{
		this.tierID1 = tierID1;
		return this;
	}

	public Integer getTierID2()
	{
		return tierID2;
	}

	public Stage setTierID2(Integer tierID2)
	{
		this.tierID2 = tierID2;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Stage setType(String type)
	{
		this.type = type;
		return this;
	}

	public Integer getAreaID()
	{
		return areaID;
	}

	public Stage setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	public Integer getGroupID()
	{
		return groupID;
	}

	public Stage setGroupID(Integer groupID)
	{
		this.groupID = groupID;
		return this;
	}

	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	public Stage setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	public Long getSignature()
	{
		return signature;
	}

	public Stage setSignature(Long signature)
	{
		this.signature = signature;
		return this;
	}

	public Integer getAgentID()
	{
		return agentID;
	}

	public Stage setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public Stage setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	public String getAltPhoneNumber()
	{
		return altPhoneNumber;
	}

	public Stage setAltPhoneNumber(String altPhoneNumber)
	{
		this.altPhoneNumber = altPhoneNumber;
		return this;
	}

	public String getAuthenticationMethod()
	{
		return authenticationMethod;
	}

	public Stage setAuthenticationMethod(String authenticationMethod)
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	public Stage setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	public String getImei()
	{
		return imei;
	}

	public Stage setImei(String imei)
	{
		this.imei = imei;
		return this;
	}

	public String getImsi()
	{
		return imsi;
	}

	public Stage setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}

	public String getInitials()
	{
		return initials;
	}

	public Stage setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	public String getLanguage()
	{
		return language;
	}

	public Stage setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public Stage setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	/*
	 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
	 */
	/*public Boolean getMsisdnRecycled() {
		return msisdnRecycled;
	}

	public Stage setMsisdnRecycled(Boolean msisdnRecycled) {
		this.msisdnRecycled = msisdnRecycled;
		return this;
	}*/

	public String getPostalAddressCity()
	{
		return postalAddressCity;
	}

	public Stage setPostalAddressCity(String postalAddressCity)
	{
		this.postalAddressCity = postalAddressCity;
		return this;
	}

	public String getPostalAddressLine1()
	{
		return postalAddressLine1;
	}

	public Stage setPostalAddressLine1(String postalAddressLine1)
	{
		this.postalAddressLine1 = postalAddressLine1;
		return this;
	}

	public String getPostalAddressLine2()
	{
		return postalAddressLine2;
	}

	public Stage setPostalAddressLine2(String postalAddressLine2)
	{
		this.postalAddressLine2 = postalAddressLine2;
		return this;
	}

	public String getPostalAddressSuburb()
	{
		return postalAddressSuburb;
	}

	public Stage setPostalAddressSuburb(String postalAddressSuburb)
	{
		this.postalAddressSuburb = postalAddressSuburb;
		return this;
	}

	public String getPostalAddressZip()
	{
		return postalAddressZip;
	}

	public Stage setPostalAddressZip(String postalAddressZip)
	{
		this.postalAddressZip = postalAddressZip;
		return this;
	}

	public String getStreetAddressCity()
	{
		return streetAddressCity;
	}

	public Stage setStreetAddressCity(String streetAddressCity)
	{
		this.streetAddressCity = streetAddressCity;
		return this;
	}

	public String getStreetAddressLine1()
	{
		return streetAddressLine1;
	}

	public Stage setStreetAddressLine1(String streetAddressLine1)
	{
		this.streetAddressLine1 = streetAddressLine1;
		return this;
	}

	public String getStreetAddressLine2()
	{
		return streetAddressLine2;
	}

	public Stage setStreetAddressLine2(String streetAddressLine2)
	{
		this.streetAddressLine2 = streetAddressLine2;
		return this;
	}

	public String getStreetAddressSuburb()
	{
		return streetAddressSuburb;
	}

	public Stage setStreetAddressSuburb(String streetAddressSuburb)
	{
		this.streetAddressSuburb = streetAddressSuburb;
		return this;
	}

	public String getStreetAddressZip()
	{
		return streetAddressZip;
	}

	public Stage setStreetAddressZip(String streetAddressZip)
	{
		this.streetAddressZip = streetAddressZip;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public Stage setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getZip()
	{
		return zip;
	}

	public Stage setZip(String zip)
	{
		this.zip = zip;
		return this;
	}

	public byte[] getKey()
	{
		return key;
	}

	public Stage setKey(byte[] key)
	{
		this.key = key;
		return this;
	}

	public String getEmail()
	{
		return email;
	}

	public Stage setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public String getSequenceNo()
	{
		return sequenceNo;
	}

	public Stage setSequenceNo(String sequenceNo)
	{
		this.sequenceNo = sequenceNo;
		return this;
	}

	public String getAuditAction()
	{
		return auditAction;
	}

	public Stage setAuditAction(String auditAction)
	{
		this.auditAction = auditAction;
		return this;
	}

	public String getOldValue()
	{
		return oldValue;
	}

	public Stage setOldValue(String oldValue)
	{
		this.oldValue = oldValue;
		return this;
	}

	public String getNewValue()
	{
		return newValue;
	}

	public Stage setNewValue(String newValue)
	{
		this.newValue = newValue;
		return this;
	}

	public Long getAuditSignature()
	{
		return auditSignature;
	}

	public Stage setAuditSignature(Long auditSignature)
	{
		this.auditSignature = auditSignature;
		return this;
	}

	public Integer getReportCountLimit()
	{
		return reportCountLimit;
	}

	public Stage setReportCountLimit(Integer reportCountLimit)
	{
		this.reportCountLimit = reportCountLimit;
		return this;
	}

	public Integer getReportDailyScheduleLimit()
	{
		return reportDailyScheduleLimit;
	}

	public Stage setReportDailyScheduleLimit(Integer reportDailyScheduleLimit)
	{
		this.reportDailyScheduleLimit = reportDailyScheduleLimit;
		return this;
	}

	public String getAreaIds()
	{
		return areaIds;
	}

	public Stage setAreaIds(String areaIds)
	{
		this.areaIds = areaIds;
		return this;
	}

	public String getCellGroupIds()
	{
		return cellGroupIds;
	}

	public Stage setCellGroupIds(String cellGroupIds)
	{
		this.cellGroupIds = cellGroupIds;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// ////////////////////////////////
	public static List<Stage> findRecords(EntityManager em, int companyID, int batchID, int tableID, int action, Integer startPosition, Integer maxResults)
	{
		TypedQuery<Stage> query = em.createNamedQuery("Stage.findByRecords", Stage.class);
		query.setParameter("companyID", companyID);
		query.setParameter("batchID", batchID);
		query.setParameter("tableID", tableID);
		query.setParameter("action", action);
		if (startPosition != null)
			query.setFirstResult(startPosition);
		if (maxResults != null)
			query.setMaxResults(maxResults);
		return query.getResultList();
	}

	public static Integer[] findDuplicates(EntityManager em, int batchID, int tableID, String column)
	{
		String format = "select distinct s1.line_no from eb_stage s1 inner join eb_stage s2 on " + //
				"s1.batch_id = s2.batch_id and s1.table_id = s2.table_id and s1.%1$s = s2.%1$s and s1.line_no > s2.line_no " + //
				"where s1.%1$s is not null and s1.%1$s <> '' and s1.batch_id = :batchID and s1.table_id = :tableID";
		String sqlString = String.format(format, column);
		Query query = em.createNativeQuery(sqlString);
		query.setParameter("batchID", batchID);
		query.setParameter("tableID", tableID);
		@SuppressWarnings("unchecked")
		List<Integer> lines = (List<Integer>)query.getResultList();
		if (lines.size() == 0)
			return null;
		
		return lines.toArray(new Integer[lines.size()]);
	}

	public static Integer[] findDuplicates(EntityManager em, int batchID, int tableID, String column1, String column2)
	{
		String format = "select distinct s1.line_no from eb_stage s1 inner join eb_stage s2 on " + //
				"s1.batch_id = s2.batch_id and s1.table_id = s2.table_id and s1.%1$s = s2.%1$s and s1.%2$s = s2.%2$s and s1.line_no > s2.line_no " + //
				"where s1.%1$s is not null and s1.%1$s <> '' and s1.%2$s is not null and s1.%2$s <> '' and s1.batch_id = :batchID and s1.table_id = :tableID";
		String sqlString = String.format(format, column1, column2);
		Query query = em.createNativeQuery(sqlString);
		query.setParameter("batchID", batchID);
		query.setParameter("tableID", tableID);
		@SuppressWarnings("unchecked")
		List<Integer> lines = (List<Integer>)query.getResultList();
		if (lines.size() == 0)
			return null;

		return lines.toArray(new Integer[lines.size()]);
	}

	public static int expected(EntityManager em, int batchID, int action, int tableID)
	{
		Query query = em.createNamedQuery("Stage.expected");
		query.setParameter("batchID", batchID);
		query.setParameter("action", action);
		query.setParameter("tableID", tableID);
		Object result = query.getSingleResult();
		int count = ((Number) result).intValue();
		return count;
	}

	// Clean-out old Staging Entries
	public static int cleanout(EntityManager em, int companyID)
	{
		Query query = em.createNamedQuery("Stage.cleanout");
		Integer oldestBatchID = Batch.findOldestID(em, companyID);
		if (oldestBatchID == null)
			return 0;
		query.setParameter("oldestBatchID", oldestBatchID);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, Stage existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		try
		{
			em.persist(this);
		}
		catch (Throwable tr)
		{
			logger.error("", tr);
			throw new RuleCheckException(tr, StatusCode.FAILED_TO_SAVE, null, tr.getMessage());
		}
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete Batch Entries");
	}

	@Override
	public void validate(Stage existing) throws RuleCheckException
	{
		RuleCheck.oneOf("action", action, ACTION_INSERT, ACTION_UPDATE, ACTION_DELETE);
	}

	public static int delete(EntityManager em, String tableName, int batchID, int tableID)
	{
		String sql = String.format("delete s.* from %s as s inner join eb_stage as b on b.batch_id = %d and b.entity_id = s.id and b.action = %d and b.table_id = %d", tableName, batchID,
				ACTION_DELETE, tableID);
		Query query = em.createNativeQuery(sql);
		return query.executeUpdate();
	}

	// Wipe Stage records for a particular Batch
	public static void purge(EntityManager em, int batchID)
	{
		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaDelete<Stage> criteria = builder.createCriteriaDelete(Stage.class);
			Root<Stage> root = criteria.from(Stage.class);
			criteria.where(builder.equal(root.get("batchID"), batchID));
			em.createQuery(criteria).executeUpdate();
			ts.commit();
		}
	}

	public static Stage findCell(EntityManager em, int mcc, int mnc, int lac, int cid, int batchID, int tableID, int action)
	{
		TypedQuery<Stage> query = em.createNamedQuery("Stage.findCell", Stage.class);
		query.setParameter("batchID", batchID);
		query.setParameter("action", action);
		query.setParameter("tableID", tableID);
		query.setParameter("i1", mcc);
		query.setParameter("i2", mnc);
		query.setParameter("i3", lac);
		query.setParameter("i4", cid);
		List<Stage> results = query.getResultList();
		return results == null || results.size() == 0 ? null : results.get(0);
	}

}

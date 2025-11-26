package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.Agents;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.IBatchEnabled;
import hxc.services.ecds.util.AuthenticationHelper;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

////////////////////////////////////////////////////////////////////////////////////////
//
// Agent Table - Used for Agent slow changing fields
//
///////////////////////////////////

@Table(name = "ea_agent", uniqueConstraints = { //
		@UniqueConstraint(name = "es_agent_acc_no", columnNames = { "comp_id", "acc_no" }), //
		@UniqueConstraint(name = "es_agent_domain_account", columnNames = { "comp_id", "domain_account" }), //
},
		indexes = {
				@Index(name = "es_agent_msisdn", columnList = "msisdn"),
})
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = "Agent.getGoodCount", query = "SELECT COUNT(id) FROM ea_agent WHERE LENGTH(msisdn) = :len " +
                " AND SUBSTR(msisdn, 1, 2) IN (SELECT old_code FROM mobile_numbers_format_mapping);"),

        @NamedNativeQuery(name = "Agent.getWrongNumbersCount", query = "SELECT COUNT(id) FROM ea_agent WHERE LENGTH(msisdn) != :len " +
                " OR SUBSTR(msisdn, 1, 2) NOT IN (SELECT old_code FROM mobile_numbers_format_mapping);"),

        @NamedNativeQuery(name = "Agent.getAllGoodIds", query = "SELECT id FROM ea_agent WHERE LENGTH(msisdn) = :len " +
                " AND SUBSTR(msisdn, 1, 2) IN (SELECT old_code FROM mobile_numbers_format_mapping) ORDER BY id ASC;"),

        @NamedNativeQuery(name = "Agent.transformNumbers",
                query = "UPDATE ea_agent SET msisdn = " +
                        " CONCAT((SELECT new_prefix FROM mobile_numbers_format_mapping WHERE old_code = SUBSTR(msisdn, 1, 2)), msisdn) " +
                        "	WHERE id BETWEEN :id_start AND :id_end " +
                        "		AND LENGTH(msisdn) = :len " +
                        "		AND SUBSTR(msisdn, 1, 2) IN (SELECT old_code FROM mobile_numbers_format_mapping);"),

        @NamedNativeQuery(name = "Agent.getNLengthCount", query = "SELECT COUNT(id) FROM ea_agent WHERE LENGTH(msisdn) = :len ;"),

        @NamedNativeQuery(name = "Agent.getWrongPrefixCount", query = "SELECT COUNT(id) FROM ea_agent " +
                " WHERE SUBSTR(msisdn, 1, 2) NOT IN (SELECT new_prefix FROM mobile_numbers_format_mapping);"),
})
@NamedQueries({ //
		@NamedQuery(name = "Agent.findByID", query = "SELECT p FROM Agent p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Agent.findByIdWithZeroBalance", query = "SELECT agent FROM Agent as agent, Account as account where agent.id = :id AND account.agentID = :id AND agent.companyID = :companyID AND account.balance = 0 AND account.onHoldBalance = 0 AND account.bonusBalance = 0"), //
		@NamedQuery(name = "Agent.findActiveByMSISDN", query =
			"SELECT p FROM Agent p where mobileNumber = :msisdn and companyID = :companyID "+
			"and p.state = '" + hxc.ecds.protocol.rest.Agent.STATE_ACTIVE + "'"), //

		@NamedQuery(name = "Agent.findSuspendedByMSISDN", query =
			"SELECT p FROM Agent p where mobileNumber = :msisdn and companyID = :companyID "+
			"and p.state = '" + hxc.ecds.protocol.rest.Agent.STATE_SUSPENDED+ "'"), //

		@NamedQuery(name = "Agent.findDeactivatedByMSISDN", query =
			"SELECT p FROM Agent p where mobileNumber = :msisdn and companyID = :companyID "+
			"and p.state = '" + hxc.ecds.protocol.rest.Agent.STATE_DEACTIVATED + "' "+
			"order by p.deactivationDate desc"), //
    
    @NamedQuery(name = "Agent.findAllAgentsByMSISDN", query =
			"SELECT p FROM Agent p where mobileNumber = :msisdn and companyID = :companyID "+
			//"and p.state = '" + hxc.ecds.protocol.rest.Agent.STATE_DEACTIVATED + "' "+
			"order by p.deactivationDate desc"), //
		/*
		 *	Functionality on hold MSISDN-RECYCLING - replace Agent.findByMSISDN above
		 *  with the commented out version below
		 */
		//@NamedQuery(name = "Agent.findByMSISDN", query = "SELECT p FROM Agent p where mobileNumber = :msisdn and companyID = :companyID and msisdnRecycled = false"), //
		@NamedQuery(name = "Agent.findRoot", query = "SELECT p FROM Agent p where tier.name = '" + Tier.TIER_ROOT_NAME + "' and companyID = :companyID"), //
		@NamedQuery(name = "Agent.findByAccountNo", query = "SELECT p FROM Agent p where accountNumber = :accountNumber and companyID = :companyID"), //
		@NamedQuery(name = "Agent.findByDomainAccountName", query = "SELECT p FROM Agent p where companyID = :companyID and domainAccountName = :domainAccountName"), //
		@NamedQuery(name = "Agent.referenceServiceClass", query = "SELECT p FROM Agent p where serviceClass.id = :serviceClassID"), //
		@NamedQuery(name = "Agent.referenceGroup", query = "SELECT p FROM Agent p where group.id = :groupID"), //
		@NamedQuery(name = "Agent.referenceTier", query = "SELECT p FROM Agent p where tier.id = :tierID"), //
		@NamedQuery(name = "Agent.referenceAgent", query = "SELECT p FROM Agent p where supplier.id = :agentID or owner.id = :agentID"), //
		@NamedQuery(name = "Agent.findByIdWithAccountInfo", query = "SELECT agent, account FROM Agent as agent, Account as account where agent.id = :id AND account.agentID = :id AND agent.companyID = :companyID"),

		/*
		@NamedQuery(name = "Transaction.totalsForAgent", //
				query = "SELECT p.type, count(*) as count, sum(p.amount) as total FROM Transaction p " + //
						"where a_AgentID = :agentID and companyID = :companyID " + //
						"and startTime >= :startTime " + //
						"and returnCode = '" + ResponseHeader.RETURN_CODE_SUCCESS + "' " + //
						"and type in ('" + //
						hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER + "','" + //
						hxc.ecds.protocol.rest.Transaction.TYPE_SELL + "','" + //
						hxc.ecds.protocol.rest.Transaction.TYPE_SELF_TOPUP + //
						"') GROUP BY p.type"), //
		 */

})
@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
public class Agent extends hxc.ecds.protocol.rest.Agent //
		implements Serializable, ICompanyData<Agent>, IAntiLaunder<Agent>, ISecured<Agent>, IBatchEnabled<Agent>, IAgentUser, IAuthenticatable
{
	final static Logger logger = LoggerFactory.getLogger(Agent.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 8434490127537244634L;

	private static final String ROOT_ACCOUNT_NAME = "ROOT";

	public static final String NEXT_AGENT_ACC_NO = "NextAgentAccNo";

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_AGENTS, Permission.PERM_ADD, "May Add Agents");
	public static final Permission MAY_UPDATE = new Permission(false, true, Permission.GROUP_AGENTS, Permission.PERM_UPDATE, "May Update Agents");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_AGENTS, Permission.PERM_DELETE, "May Delete Agents");
	public static final Permission MAY_VIEW = new Permission(false, true, Permission.GROUP_AGENTS, Permission.PERM_VIEW, "May View Agents");
	public static final Permission MAY_CONFIGURE = new Permission(false, false, Permission.GROUP_AGENTS, Permission.PERM_CONFIGURE, "May Configure Agent Parameters");
	public static final Permission MAY_RESET_IMSI_LOCK = new Permission(false, true, Permission.GROUP_AGENTS, Permission.PERM_RESET_IMSI, "May reset IMSI Lockout");
	public static final Permission MAY_RESET_PIN = new Permission(false, true, Permission.GROUP_AGENTS, Permission.PERM_RESET_PIN, "May reset PIN");
	public static final Permission MAY_UPDATE_OWN = new Permission(false, true, Permission.GROUP_WEBUSERS, Permission.PERM_UPDATE_OWN, "May Update Own Profile");
	public static final Permission MAY_RESET_PASSWORDS = new Permission(false, true, Permission.GROUP_WEBUSERS, Permission.PERM_RESET_PASSWORDS, "May reset Passwords");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////

	@JsonIgnore
	protected byte[] key1;
	@JsonIgnore
	protected byte[] key2;
	@JsonIgnore
	protected byte[] key3;
	@JsonIgnore
	protected byte[] key4;

	@JsonIgnore
	protected Tier tier;
	@JsonIgnore
	protected Agent supplier;
	@JsonIgnore
	protected Agent owner;
	@JsonIgnore
	protected Group group;
	@JsonIgnore
	protected Area area;
	@JsonIgnore
	protected ServiceClass serviceClass;
	@JsonIgnore
	protected Role role;
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;

	@JsonIgnore
	protected Integer lastCellID;
	@JsonIgnore
	protected Cell lastCell;
	@JsonIgnore
	protected Date lastCellExpiryTime;

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
	public Agent setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public Agent setCompanyID(int companyID)
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

	@Override
	public Agent setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "acc_no", nullable = true, length = ACCOUNT_NO_MAX_LENGTH)
	public String getAccountNumber()
	{
		return accountNumber;
	}

	@Override
	public Agent setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	@Override
	@Column(name = "msisdn", nullable = false, length = PHONE_NUMBER_MAX_LENGTH)
	public String getMobileNumber()
	{
		return mobileNumber;
	}

	@Override
	public Agent setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	/*
	 * Functionality on hold MSISDN-RECYCLING - uncomment when re-instating
	 */
	/*@Column(name = "msisdn_recycled", nullable = false, length = 1)
	public Boolean getMsisdnRecycled()
	{
		return msisdnRecycled;
	}

	public Agent setMsisdnRecycled(Boolean msisdnRecycled)
	{
		this.msisdnRecycled = msisdnRecycled;
		return this;
	}*/

	@Override
	@Column(name = "imei", nullable = true, length = IMEI_MAX_LENGTH)
	public String getImei()
	{
		return imei;
	}

	@Override
	public Agent setImei(String imei)
	{
		this.imei = imei;
		return this;
	}

	@Override
	@Column(name = "last_imei_update", nullable = true)
	public Date getLastImeiUpdate()
	{
		return lastImeiUpdate;
	}

	@Override
	public Agent setLastImeiUpdate(Date lastImeiUpdate)
	{
		this.lastImeiUpdate = lastImeiUpdate;
		return this;
	}

	@Override
	@Column(name = "imsi", nullable = true, length = IMSI_MAX_LENGTH)
	public String getImsi()
	{
		return imsi;
	}

	@Override
	public Agent setImsi(String imsi)
	{
		this.imsi = imsi;
		return this;
	}

	@Override
	@Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
	public String getTitle()
	{
		return title;
	}

	@Override
	public Agent setTitle(String title)
	{
		this.title = title;
		return this;
	}

	@Override
	@Column(name = "first_name", nullable = false, length = FIRST_NAME_MAX_LENGTH)
	public String getFirstName()
	{
		return firstName;
	}

	@Override
	public Agent setFirstName(String firstName)
	{
		this.firstName = firstName;
		return this;
	}

	@Override
	@Column(name = "intitials", nullable = true, length = INITIALS_MAX_LENGTH)
	public String getInitials()
	{
		return initials;
	}

	@Override
	public Agent setInitials(String initials)
	{
		this.initials = initials;
		return this;
	}

	@Override
	@Column(name = "surname", nullable = false, length = LAST_NAME_MAX_LENGTH)
	public String getSurname()
	{
		return surname;
	}

	@Override
	public Agent setSurname(String surname)
	{
		this.surname = surname;
		return this;
	}

	@Override
	@Column(name = "language", nullable = false, length = LANGUAGE_MAX_LENGTH)
	public String getLanguage()
	{
		return language;
	}

	@Override
	public Agent setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	@Override
	@Column(name = "domain_account", nullable = true, length = DOMAIN_NAME_MAX_LENGTH)
	public String getDomainAccountName()
	{
		return domainAccountName;
	}

	@Override
	public Agent setDomainAccountName(String domainAccountName)
	{
		this.domainAccountName = domainAccountName;
		return this;
	}

	@Override
	@Column(name = "gender", nullable = true, length = 1)
	public String getGender()
	{
		return gender;
	}

	@Override
	public Agent setGender(String gender)
	{
		this.gender = gender;
		return this;
	}

	@Override
	@Column(name = "dob", nullable = true)
	public Date getDateOfBirth()
	{
		return dateOfBirth;
	}

	@Override
	public Agent setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
		return this;
	}

	@Override
	@Column(name = "street1", nullable = true, length = ADDRESS_LINE_MAX_LENGTH)
	public String getStreetAddressLine1()
	{
		return streetAddressLine1;
	}

	@Override
	public Agent setStreetAddressLine1(String streetAddressLine1)
	{
		this.streetAddressLine1 = streetAddressLine1;
		return this;
	}

	@Override
	@Column(name = "street2", nullable = true, length = ADDRESS_LINE_MAX_LENGTH)
	public String getStreetAddressLine2()
	{
		return streetAddressLine2;
	}

	@Override
	public Agent setStreetAddressLine2(String streetAddressLine2)
	{
		this.streetAddressLine2 = streetAddressLine2;
		return this;
	}

	@Override
	@Column(name = "street_suburb", nullable = true, length = PLACE_MAX_LENGTH)
	public String getStreetAddressSuburb()
	{
		return streetAddressSuburb;
	}

	@Override
	public Agent setStreetAddressSuburb(String streetAddressSuburb)
	{
		this.streetAddressSuburb = streetAddressSuburb;
		return this;
	}

	@Override
	@Column(name = "street_city", nullable = true, length = PLACE_MAX_LENGTH)
	public String getStreetAddressCity()
	{
		return streetAddressCity;
	}

	@Override
	public Agent setStreetAddressCity(String streetAddressCity)
	{
		this.streetAddressCity = streetAddressCity;
		return this;
	}

	@Override
	@Column(name = "street_zip", nullable = true, length = ZIP_MAX_LENGTH)
	public String getStreetAddressZip()
	{
		return streetAddressZip;
	}

	@Override
	public Agent setStreetAddressZip(String streetAddressZip)
	{
		this.streetAddressZip = streetAddressZip;
		return this;
	}

	@Override
	@Column(name = "postal1", nullable = true, length = ADDRESS_LINE_MAX_LENGTH)
	public String getPostalAddressLine1()
	{
		return postalAddressLine1;
	}

	@Override
	public Agent setPostalAddressLine1(String postalAddressLine1)
	{
		this.postalAddressLine1 = postalAddressLine1;
		return this;
	}

	@Override
	@Column(name = "postal2", nullable = true, length = ADDRESS_LINE_MAX_LENGTH)
	public String getPostalAddressLine2()
	{
		return postalAddressLine2;
	}

	@Override
	public Agent setPostalAddressLine2(String postalAddressLine2)
	{
		this.postalAddressLine2 = postalAddressLine2;
		return this;
	}

	@Override
	@Column(name = "postal_suburb", nullable = true, length = PLACE_MAX_LENGTH)
	public String getPostalAddressSuburb()
	{
		return postalAddressSuburb;
	}

	@Override
	public Agent setPostalAddressSuburb(String postalAddressSuburb)
	{
		this.postalAddressSuburb = postalAddressSuburb;
		return this;
	}

	@Override
	@Column(name = "postal_city", nullable = true, length = PLACE_MAX_LENGTH)
	public String getPostalAddressCity()
	{
		return postalAddressCity;
	}

	@Override
	public Agent setPostalAddressCity(String postalAddressCity)
	{
		this.postalAddressCity = postalAddressCity;
		return this;
	}

	@Override
	@Column(name = "postal_zip", nullable = true, length = ZIP_MAX_LENGTH)
	public String getPostalAddressZip()
	{
		return postalAddressZip;
	}

	@Override
	public Agent setPostalAddressZip(String postalAddressZip)
	{
		this.postalAddressZip = postalAddressZip;
		return this;
	}

	@Override
	@Column(name = "alt_phone", nullable = true, length = PHONE_NUMBER_MAX_LENGTH)
	public String getAltPhoneNumber()
	{
		return altPhoneNumber;
	}

	@Override
	public Agent setAltPhoneNumber(String altPhoneNumber)
	{
		this.altPhoneNumber = altPhoneNumber;
		return this;
	}

	@Override
	@Column(name = "email", nullable = true, length = EMAIL_MAX_LENGTH)
	public String getEmail()
	{
		return email;
	}

	@Override
	public Agent setEmail(String email)
	{
		this.email = email;
		return this;
	}

	@Override
	@Column(name = "tier_id", nullable = false, insertable = false, updatable = false)
	public int getTierID()
	{
		return tierID;
	}

	@Override
	public Agent setTierID(int tierID)
	{
		this.tierID = tierID;
		return this;
	}

	@Override
	@Column(name = "group_id", nullable = true, insertable = false, updatable = false)
	public Integer getGroupID()
	{
		return groupID;
	}

	@Override
	public Agent setGroupID(Integer groupID)
	{
		this.groupID = groupID;
		return this;
	}

	@Override
	@Column(name = "area_id", nullable = true, insertable = false, updatable = false)
	public Integer getAreaID()
	{
		return areaID;
	}

	@Override
	public Agent setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	@Override
	@Column(name = "sc_id", nullable = true, insertable = false, updatable = false)
	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	@Override
	public Agent setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	@Override
	@Column(name = "role_id", nullable = false, insertable = false, updatable = false)
	public int getRoleID()
	{
		return roleID;
	}

	@Override
	public Agent setRoleID(int roleID)
	{
		this.roleID = roleID;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Agent setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "a_date", nullable = true)
	public Date getActivationDate()
	{
		return activationDate;
	}

	@Override
	public Agent setActivationDate(Date activationDate)
	{
		this.activationDate = activationDate;
		return this;
	}

	@Override
	@Column(name = "d_date", nullable = true)
	public Date getDeactivationDate()
	{
		return deactivationDate;
	}

	@Override
	public Agent setDeactivationDate(Date deactivationDate)
	{
		this.deactivationDate = deactivationDate;
		return this;
	}

	@Override
	@Column(name = "e_date", nullable = true)
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	@Override
	public Agent setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
		return this;
	}

	@Override
	@Column(name = "supplier_id", nullable = true, insertable = false, updatable = false)
	public Integer getSupplierAgentID()
	{
		return supplierAgentID;
	}

	@Override
	public Agent setSupplierAgentID(Integer supplierAgentID)
	{
		this.supplierAgentID = supplierAgentID;
		return this;
	}

	@Override
	@Column(name = "owner_id", nullable = true, insertable = false, updatable = false)
	public Integer getOwnerAgentID()
	{
		return ownerAgentID;
	}

	@Override
	public Agent setOwnerAgentID(Integer ownerAgentID)
	{
		this.ownerAgentID = ownerAgentID;
		return this;
	}

	@Override
	@Column(name = "channels", nullable = false)
	public int getAllowedChannels()
	{
		return allowedChannels;
	}

	@Override
	public Agent setAllowedChannels(int allowedChannels)
	{
		this.allowedChannels = allowedChannels;
		return this;
	}

	@Override
	@Column(name = "warn_level", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getWarningThreshold()
	{
		return warningThreshold;
	}

	@Override
	public Agent setWarningThreshold(BigDecimal warningThreshold)
	{
		this.warningThreshold = warningThreshold;
		return this;
	}

	@Override
	@Column(name = "max_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	@Override
	public Agent setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	@Override
	@Column(name = "max_daily_count", nullable = true)
	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	@Override
	public Agent setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	@Override
	@Column(name = "max_daily_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	@Override
	public Agent setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	@Override
	@Column(name = "max_monthly_count", nullable = true)
	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	@Override
	public Agent setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	@Override
	@Column(name = "max_monthly_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	@Override
	public Agent setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}

	@Override
	@Column(name = "max_report_count", nullable = true)
	public Integer getReportCountLimit()
	{
		return reportCountLimit;
	}

	@Override
	public Agent setReportCountLimit(Integer reportCountLimit)
	{
		this.reportCountLimit = reportCountLimit;
		return this;
	}

	@Override
	@Column(name = "max_report_daily_schedule_count", nullable = true)
	public Integer getReportDailyScheduleLimit()
	{
		return reportDailyScheduleLimit;
	}

	@Override
	public Agent setReportDailyScheduleLimit(Integer reportDailyScheduleLimit)
	{
		this.reportDailyScheduleLimit = reportDailyScheduleLimit;
		return this;
	}

	@Override
	@Column(name = "temp_pin", nullable = false)
	public boolean isTemporaryPin()
	{
		return temporaryPin;
	}

	@Override
	public Agent setTemporaryPin(boolean temporaryPin)
	{
		this.temporaryPin = temporaryPin;
		return this;
	}

	@Override
	@Column(name = "confirm_ussd", nullable = false)
	public boolean isConfirmUssd()
	{
		return confirmUssd;
	}

	@Override
	public Agent setConfirmUssd(boolean confirmUssd)
	{
		this.confirmUssd = confirmUssd;
		return this;
	}

	@Override
	@Column(name = "send_daily_bundle_commission_report", nullable = false)
	public boolean isSendBundleCommissionReport()
	{
		return sendBundleCommissionReport;
	}

	@Override
	public Agent setSendBundleCommissionReport(boolean sendBundleCommissionReport)
	{
		this.sendBundleCommissionReport = sendBundleCommissionReport;
		return this;
	}

	@Override
	@Column(name = "auth_method", nullable = false, unique = false, length = 1)
	public String getAuthenticationMethod()
	{
		return authenticationMethod;
	}

	@Override
	public Agent setAuthenticationMethod(String authenticationMethod)
	{
		this.authenticationMethod = authenticationMethod;
		return this;
	}

	@Column(name = "key1", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey1()
	{
		return key1;
	}

	public Agent setKey1(byte[] key1)
	{
		this.key1 = key1;
		return this;
	}

	@Column(name = "key2", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey2()
	{
		return key2;
	}

	public Agent setKey2(byte[] key2)
	{
		this.key2 = key2;
		return this;
	}

	@Column(name = "key3", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey3()
	{
		return key3;
	}

	public Agent setKey3(byte[] key3)
	{
		this.key3 = key3;
		return this;
	}

	@Column(name = "key4", nullable = true, unique = false, length = KEY_MAX_LENGTH)
	public byte[] getKey4()
	{
		return key4;
	}

	public Agent setKey4(byte[] key4)
	{
		this.key4 = key4;
		return this;
	}

	@Column(name = "attempts", nullable = true)
	public Integer getConsecutiveAuthFailures()
	{
		return consecutiveAuthFailures;
	}

	public Agent setConsecutiveAuthFailures(Integer consecutiveAuthFailures)
	{
		this.consecutiveAuthFailures = consecutiveAuthFailures;
		return this;
	}

	@Override
	@Column(name = "signature", nullable = false)
	public long getSignature()
	{
		return signature;
	}

	@Override
	public Agent setSignature(long signature)
	{
		this.signature = signature;
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "tier_id", foreignKey = @ForeignKey(name = "FK_Agent_Tier"))
	public Tier getTier()
	{
		return tier;
	}

	public Agent setTier(Tier tier)
	{
		this.tier = tier;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "supplier_id", foreignKey = @ForeignKey(name = "FK_Agent_Supplier"))
	public Agent getSupplier()
	{
		return supplier;
	}

	public Agent setSupplier(Agent supplier)
	{
		this.supplier = supplier;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "FK_Agent_Owner"))
	public Agent getOwner()
	{
		return owner;
	}

	public Agent setOwner(Agent owner)
	{
		this.owner = owner;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "FK_Agent_Group"))
	public Group getGroup()
	{
		return group;
	}

	public Agent setGroup(Group group)
	{
		this.group = group;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id", foreignKey = @ForeignKey(name = "FK_Agent_Area"))
	public Area getArea()
	{
		return area;
	}

	public void setArea(Area area)
	{
		this.area = area;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "sc_id", foreignKey = @ForeignKey(name = "FK_Agent_SClass"))
	public ServiceClass getServiceClass()
	{
		return serviceClass;
	}

	public Agent setServiceClass(ServiceClass serviceClass)
	{
		this.serviceClass = serviceClass;
		return this;
	}

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_Agent_Role"))
	public Role getRole()
	{
		return role;
	}

	public Agent setRole(Role role)
	{
		this.role = role;
		return this;
	}

	@Transient
	@Override
	public boolean isTamperedWith()
	{
		return tamperedWith;
	}

	@Override
	public Agent setTamperedWith(boolean tamperedWith)
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
	public Agent setLastUserID(int lastUserID)
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
	public Agent setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Column(name = "last_imsi", nullable = true)
	public Date getLastImsiChange()
	{
		return lastImsiChange;
	}

	@Override
	public Agent setLastImsiChange(Date lastImsiChange)
	{
		this.lastImsiChange = lastImsiChange;
		return this;
	}

	@Override
	@Transient
	public boolean isImsiLockedOut()
	{
		return imsiLockedOut;
	}

	@Override
	public Agent setImsiLockedOut(boolean imsiLockedOut)
	{
		this.imsiLockedOut = imsiLockedOut;
		return this;
	}

	@Override
	@Transient
	public boolean isPinLockedOut()
	{
		return consecutiveAuthFailures != null && consecutiveAuthFailures < 0;
	}

	@Override
	public Agent setPinLockedOut(boolean pinLockedOut)
	{
		this.pinLockedOut = isPinLockedOut();
		return this;
	}

	@Override
	@Column(name = "pin_version", nullable = false)
	public int getPinVersion()
	{
		return pinVersion;
	}

	@Override
	public Agent setPinVersion(int pinVersion)
	{
		this.pinVersion = pinVersion;
		return this;
	}

	// Location Caching
	@Override
	@Column(name = "last_cell_id", nullable = true, insertable = false, updatable = false)
	public Integer getLastCellID()
	{
		return lastCellID;
	}

	@Override
	public Agent setLastCellID(Integer lastCellID)
	{
		this.lastCellID = lastCellID;
		return this;
	}

	@Override
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "last_cell_id", foreignKey = @ForeignKey(name = "FK_Agent_Cell"))
	public Cell getLastCell()
	{
		return lastCell;
	}

	@Override
	public Agent setLastCell(Cell lastCell)
	{
		this.lastCell = lastCell;
		return this;
	}

	@Override
	@Column(name = "last_cell_expires", nullable = true)
	public Date getLastCellExpiryTime()
	{
		return lastCellExpiryTime;
	}

	@Override
	public Agent setLastCellExpiryTime(Date lastCellExpiryTime)
	{
		this.lastCellExpiryTime = lastCellExpiryTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Agent()
	{

	}

	public Agent(Agent agent, boolean calculateSignature)
	{
		this.lastUserID = agent.lastUserID;
		this.lastTime = agent.lastTime;
		this.key1 = agent.key1;
		this.key2 = agent.key2;
		this.key3 = agent.key3;
		this.key4 = agent.key4;
		amend(agent, calculateSignature);
	}

	public void amend(hxc.ecds.protocol.rest.Agent agent, boolean calculateSignature)
	{
		this.id = agent.getId();
		this.companyID = agent.getCompanyID();
		this.version = agent.getVersion();
		this.accountNumber = agent.getAccountNumber();
		this.mobileNumber = agent.getMobileNumber();
		this.title = agent.getTitle();
		this.firstName = agent.getFirstName();
		this.initials = agent.getInitials();
		this.surname = agent.getSurname();
		this.language = agent.getLanguage();
		this.domainAccountName = agent.getDomainAccountName();
		this.gender = agent.getGender();
		this.dateOfBirth = agent.getDateOfBirth();
		this.streetAddressLine1 = agent.getStreetAddressLine1();
		this.streetAddressLine2 = agent.getStreetAddressLine2();
		this.streetAddressSuburb = agent.getStreetAddressSuburb();
		this.streetAddressCity = agent.getStreetAddressCity();
		this.streetAddressZip = agent.getStreetAddressZip();
		this.postalAddressLine1 = agent.getPostalAddressLine1();
		this.postalAddressLine2 = agent.getPostalAddressLine2();
		this.postalAddressSuburb = agent.getPostalAddressSuburb();
		this.postalAddressCity = agent.getPostalAddressCity();
		this.postalAddressZip = agent.getPostalAddressZip();
		this.altPhoneNumber = agent.getAltPhoneNumber();
		this.email = agent.getEmail();
		this.tierID = agent.getTierID();
		this.groupID = agent.getGroupID();
		this.areaID = agent.getAreaID();
		this.serviceClassID = agent.getServiceClassID();
		this.roleID = agent.getRoleID();
		this.state = agent.getState();
		this.activationDate = agent.getActivationDate();
		this.deactivationDate = agent.getDeactivationDate();
		this.expirationDate = agent.getExpirationDate();
		this.supplierAgentID = agent.getSupplierAgentID();
		this.ownerAgentID = agent.getOwnerAgentID();
		this.allowedChannels = agent.getAllowedChannels();
		this.warningThreshold = agent.getWarningThreshold();
		this.maxTransactionAmount = agent.getMaxTransactionAmount();
		this.maxDailyCount = agent.getMaxDailyCount();
		this.maxDailyAmount = agent.getMaxDailyAmount();
		this.maxMonthlyCount = agent.getMaxMonthlyCount();
		this.maxMonthlyAmount = agent.getMaxMonthlyAmount();
		this.reportCountLimit = agent.getReportCountLimit();
		this.reportDailyScheduleLimit = agent.getReportDailyScheduleLimit();
		this.temporaryPin = agent.isTemporaryPin();
		this.imsi = agent.getImsi();
		this.imei = agent.getImei();
		this.lastImeiUpdate = agent.getLastImeiUpdate();
		this.pinVersion = agent.getPinVersion();
		this.confirmUssd = agent.isConfirmUssd();
		this.authenticationMethod = agent.getAuthenticationMethod();
		this.consecutiveAuthFailures = agent.getConsecutiveAuthFailures();
		this.sendBundleCommissionReport = agent.isSendBundleCommissionReport();
//		if(calculateSignature)
//			this.signature = calcSecuritySignature();
//		else
			this.signature = agent.getSignature();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Minimum Required Data
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{

		// Load the 'ROOT' agent
		Agent root = Agent.findByAccountNumber(em, ROOT_ACCOUNT_NAME, companyID);
		if (root == null)
		{
			// Require Tier MRD
			Tier.loadMRD(em, companyID, session);

			// Get root Tier
			Tier tier = Tier.findRoot(em, companyID);

			// Require Role MRD
			Role.loadMRD(em, companyID, session);

			// Get AgentAll Role
			Role agentAll = Role.findAgentAll(em, companyID);

			// Create Root Agent
			final String mobileNumber = String.format("%08d", companyID);
			root = new Agent() //
					.setCompanyID(companyID) //
					.setAccountNumber(ROOT_ACCOUNT_NAME) //
					.setMobileNumber(mobileNumber) //
					.setImei(null) //
					.setImsi(null) //
					.setTitle(ROOT_ACCOUNT_NAME) //
					.setFirstName(ROOT_ACCOUNT_NAME) //
					.setInitials(null) //
					.setSurname(ROOT_ACCOUNT_NAME) //
					.setLanguage(Phrase.ENG) //
					.setDomainAccountName(null) //
					.setGender(null) //
					.setDateOfBirth(null) //
					.setStreetAddressLine1(null) //
					.setStreetAddressLine2(null) //
					.setStreetAddressSuburb(null) //
					.setStreetAddressCity(null) //
					.setStreetAddressZip(null) //
					.setPostalAddressLine1(null) //
					.setPostalAddressLine2(null) //
					.setPostalAddressSuburb(null) //
					.setPostalAddressCity(null) //
					.setPostalAddressZip(null) //
					.setAltPhoneNumber(null) //
					.setEmail(null) //
					.setTierID(tier.getId()) //
					.setGroupID(null) //
					.setAreaID(null) //
					.setServiceClassID(null) //
					.setRoleID(agentAll.getId()) //
					.setRole(agentAll) //
					.setState(STATE_PERMANENT) //
					.setActivationDate(new Date()) //
					.setDeactivationDate(null) //
					.setExpirationDate(null) //
					.setSupplierAgentID(null) //
					.setOwnerAgentID(null) //
					.setAllowedChannels(ALLOWED_WUI) //
					.setWarningThreshold(null) //
					.setMaxTransactionAmount(null) //
					.setMaxDailyCount(null) //
					.setMaxDailyAmount(null) //
					.setMaxMonthlyCount(null) //
					.setMaxMonthlyAmount(null) //
					.setTemporaryPin(false) //
					.setTier(tier) //
					.setKey1(null); //
			AuditEntryContext auditEntryContext = new AuditEntryContext("LOADED_MRD_ROOT");
			root.persist(em, null, session, auditEntryContext);

		}

		State.loadMRD(em, session, companyID, NEXT_AGENT_ACC_NO, 1000L);

		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
		Permission.loadMRD(em, MAY_CONFIGURE, session);
		Permission.loadMRD(em, MAY_RESET_IMSI_LOCK, session);
		Permission.loadMRD(em, MAY_RESET_PIN, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////
	public static Agent findByID(EntityManager em, int agentID, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findByID", Agent.class);
		query.setParameter("id", agentID);
		query.setParameter("companyID", companyID);
		List<Agent> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	// Remove once we know for sure that this is not needed
	public static Agent findByIDWithZeroBalance(EntityManager em, int agentID, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findByIdWithZeroBalance", Agent.class);
		query.setParameter("id", agentID);
		query.setParameter("companyID", companyID);
		List<Agent> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	/**
	 * Find the agent and account data for a particular agent.
	 *
	 * use keys:
	 * 			"agent"
	 * 		 	"account"
	 * @param em
	 * @param agentID
	 * @param companyID
	 * @return map with agent and account object for the agent, if no agent is found return null
	 * 			use keys:
	 * 						"agent"
	 * 					 	"account"
	 */
	public static Map<String, Object> findByIDWithAccountInfo(EntityManager em, int agentID, int companyID)
	{

		Map<String, Object> resultsMap = null;

		Query query = em.createNamedQuery("Agent.findByIdWithAccountInfo");
		query.setParameter("id", agentID);
		query.setParameter("companyID", companyID);

		List<Object[]> results = query.getResultList();
		Object[] agentInfoResult = results.size() == 0 ? null : results.get(0);
		if(agentInfoResult != null){
			resultsMap = new HashMap<>();

			Agent agent = (Agent) agentInfoResult[0];
			Account account = (Account) agentInfoResult[1];

			resultsMap.put("agent", agent);
			resultsMap.put("account", account);
		}
		return resultsMap;
	}


	public static Agent findByAccountNumber(EntityManager em, String accountNumber, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findByAccountNo", Agent.class);
		query.setParameter("accountNumber", accountNumber);
		query.setParameter("companyID", companyID);
		List<Agent> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

  public static List<Agent> findAllAgentsByMSISDN(EntityManager em, String msisdn, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findAllAgentsByMSISDN", Agent.class);
		query.setParameter("msisdn", msisdn);
		query.setParameter("companyID", companyID);
		List<Agent> results = query.getResultList();
		return results;
	}

	public static Agent findByMSISDN(EntityManager em, String msisdn, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findActiveByMSISDN", Agent.class);
			query.setParameter("msisdn", msisdn);
			query.setParameter("companyID", companyID);
			List<Agent> results = query.getResultList();
		boolean hasActiveAgent = results.size() != 0;
	
		if (hasActiveAgent) {
				return results.get(0);
		}
     
		query = em.createNamedQuery("Agent.findSuspendedByMSISDN", Agent.class);
			query.setParameter("msisdn", msisdn);
			query.setParameter("companyID", companyID);
			results = query.getResultList();
		boolean hasSuspendedAgent = results.size() != 0;
	
		if (hasSuspendedAgent) {
				return results.get(0);
		}

		query = em.createNamedQuery("Agent.findDeactivatedByMSISDN", Agent.class);
		query.setParameter("msisdn", msisdn);
		query.setParameter("companyID", companyID);
		results = query.getResultList();
		boolean hasDeactivatedAgent = results.size() != 0;
     
		if (hasDeactivatedAgent) {
			return results.get(0);
		}
		return null;
	}

	public static Agent findByDomainAccountName(EntityManager em, int companyID, String domainAccountName)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findByDomainAccountName", Agent.class);
		query.setParameter("companyID", companyID);
		query.setParameter("domainAccountName", domainAccountName);
		List<Agent> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Agent> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Agent.class, params, companyID, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Agent.class, params, companyID, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
		return query.getSingleResult();
	}

	public static List<Agent> findMine(EntityManager em, RestParams params, int companyID, int myID)
	{
		AgentExtender px = new Agent.AgentExtender(myID);
		return QueryBuilder.getQueryResultList(em, Agent.class, params, companyID, px, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
	}

	public static Long findMyCount(EntityManager em, RestParams params, int companyID, int myID)
	{
		AgentExtender px = new Agent.AgentExtender(myID);
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Agent.class, params, companyID, px, "accountNumber", "mobileNumber", "imsi", "firstName", "surname", "domainAccountName");
		return query.getSingleResult();
	}

	public static Agent findRoot(EntityManager em, int companyID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.findRoot", Agent.class);
		query.setParameter("companyID", companyID);
		List<Agent> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.referenceServiceClass", Agent.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<Agent> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesGroup(EntityManager em, int groupID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.referenceGroup", Agent.class);
		query.setParameter("groupID", groupID);
		query.setMaxResults(1);
		List<Agent> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTier(EntityManager em, int tierID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.referenceTier", Agent.class);
		query.setParameter("tierID", tierID);
		query.setMaxResults(1);
		List<Agent> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesAgent(EntityManager em, int agentID)
	{
		TypedQuery<Agent> query = em.createNamedQuery("Agent.referenceAgent", Agent.class);
		query.setParameter("agentID", agentID);
		query.setMaxResults(1);
		List<Agent> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	//Need to put this somewhere common.
	private static <T> List<Order> getSortOrderList(CriteriaBuilder cb, Root<T> root, String sortString)
	{
		List<Order> orderList = new ArrayList<Order>();
		int index;
		while(sortString.length() > 0)
		{
			index = sortString.indexOf("+", 0) > 0 ? sortString.indexOf("+", 0) : sortString.indexOf("-", 0);
			int mode = sortString.charAt(index) == '+' ? 1 : -1;
			String column = sortString.substring(0, index).substring(sortString.indexOf(".") + 1);
			sortString = sortString.substring(index + 1);
			if(mode > 0)
				orderList.add(cb.asc(root.get(column)));
			else
				orderList.add(cb.desc(root.get(column)));
		}
		return orderList;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public long calcSecuritySignature()
	{
		return 0;
//		SignCheck signCheck = new SignCheck() //
//				.add("accountNumber", accountNumber) //
//				.add("mobileNumber", mobileNumber) //
//				.add("imei", imei) //
//				.add("imsi", imsi) //
//				.add("title", title) //
//				.add("firstName", firstName) //
//				.add("initials", initials) //
//				.add("surname", surname) //
//				.add("language", language) //
//				.add("domainAccountName", domainAccountName) //
//				.add("tierID", tierID) //
//				.add("groupID", groupID) //
//				.add("areaID", areaID) //
//				.add("serviceClassID", serviceClassID) //
//				.add("state", state) //
//				.add("activationDate", activationDate) //
//				.add("deactivationDate", deactivationDate) //
//				.add("expirationDate", expirationDate) //
//				.add("supplierAgentID", supplierAgentID) //
//				.add("allowedChannels", allowedChannels) //
//				.add("warningThreshold", warningThreshold) //
//				.add("maxTransactionAmount", maxTransactionAmount) //
//				.add("maxDailyCount", maxDailyCount) //
//				.add("maxDailyAmount", maxDailyAmount) //
//				.add("maxMonthlyCount", maxMonthlyCount) //
//				.add("maxMonthlyAmount", maxMonthlyAmount) //
//				.add("temporaryPin", temporaryPin) //
//				.add("lastImsiChange", lastImsiChange) //
//				.add("key3", key3) //
//				.add("key1", key1) //
//				.add("key2", key2);
//		String debug = signCheck.getByteArray();
//		String debug2 = signCheck.getDebug();
//		long signature = signCheck.signature();
//		return signature;
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
//		// TODO: Find a way to write to the logger and not to stderr.
//		if (tamperedWith)
//			logger.warn("Tampering Detected on Agent! AgentID=[{}]", getId());
	}

	// Add Default Account Number
	public void autoNumber(EntityManager em, int companyID)
	{
		if (accountNumber == null || accountNumber.isEmpty() || accountNumber.equals(Agent.AUTO_NUMBER))
		{
			while (true)
			{
				String nextAccountNumber = String.format("A%d", State.getNextNumber(em, companyID, Agent.NEXT_AGENT_ACC_NO, 1));
				Agent agent = findByAccountNumber(em, nextAccountNumber, companyID);
				if (agent != null)
					continue;
				accountNumber = nextAccountNumber;
				break;
			}
		}
	}




	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICompanyData
	//
	// /////////////////////////////////

	/**
	 * @deprecated
	 * After execution of this method oldValue becomes unmanaged if a merge is done.
	 * Use persistOrMergeAndReturnManagedEntity() instead.
	 */
	@Override
	@Deprecated
	public void persist(EntityManager em, Agent oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		// Set (De)Activation Date
		if (STATE_ACTIVE.equals(state) && (oldValue == null || !STATE_ACTIVE.equals(oldValue.state)))
			activationDate = new Date();
		if (STATE_DEACTIVATED.equals(state) && (oldValue == null || !STATE_DEACTIVATED.equals(oldValue.state)))
			deactivationDate = new Date();

		validate(oldValue);
		boolean isNew = id == 0;
		if (!isNew)
		{
			QueryBuilder.merge(em, oldValue, this, session, AuditEntry.TYPE_AGENT, auditEntryContext);
			return;
		}
		else
		{
			try (RequiresTransaction transaction = new RequiresTransaction(em))
			{
				// Persist Agent
				QueryBuilder.persist(em, oldValue, this, session, AuditEntry.TYPE_AGENT, auditEntryContext);

				// Create Account Automatically
				Account account = new Account() //
						.setBalance(BigDecimal.ZERO) //
						.setBonusBalance(BigDecimal.ZERO) //
						.setOnHoldBalance(BigDecimal.ZERO) //
						.setDay(new Date()) //
						.setDayCount(0) //
						.setDayTotal(BigDecimal.ZERO) //
						.setMonthCount(0) //
						.setMonthTotal(BigDecimal.ZERO) //
						.setAgent(this);
				account.persist(em, null, session, auditEntryContext);

				transaction.commit();

			}
		}
	}

	public Agent persistOrMergeAndReturnManagedEntity(EntityManager em, Agent oldValue, Session session,
													  AuditEntryContext auditEntryContext) throws RuleCheckException {
		if (STATE_ACTIVE.equals(state) && (oldValue == null || !STATE_ACTIVE.equals(oldValue.state))) {
			activationDate = new Date();
		}
		if (STATE_DEACTIVATED.equals(state) && (oldValue == null || !STATE_DEACTIVATED.equals(oldValue.state))) {
			deactivationDate = new Date();
		}

		validate(oldValue);
		boolean isNew = id == 0;
		Agent managedEntity = oldValue;
		if (!isNew) {
			managedEntity = (Agent) QueryBuilder.mergeAndReturnManagedEntity(em, oldValue, this, session, AuditEntry.TYPE_AGENT, auditEntryContext);
		} else {
			try (RequiresTransaction transaction = new RequiresTransaction(em)) {
				QueryBuilder.persist(em, oldValue, this, session, AuditEntry.TYPE_AGENT, auditEntryContext);
				
				Account account = new Account()
						.setBalance(BigDecimal.ZERO)
						.setBonusBalance(BigDecimal.ZERO)
						.setOnHoldBalance(BigDecimal.ZERO)
						.setDay(new Date())
						.setDayCount(0)
						.setDayTotal(BigDecimal.ZERO)
						.setMonthCount(0)
						.setMonthTotal(BigDecimal.ZERO)
						.setAgent(this);
				account.persist(em, null, session, auditEntryContext);
				transaction.commit();
			}
		}
		return managedEntity;
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		RuleCheck.isFalse(null, STATE_PERMANENT.equals(state), "Cannot delete Permanent Agent");

		// Both or neither Agent and Account must be deleted
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			// Delete Account First
			Account account = Account.findByAgentID(em, id, false);
			if (account != null)
				account.remove(em, session, auditEntryContext);

			QueryBuilder.remove(em, this, session, AuditEntry.TYPE_AGENT, auditEntryContext);

			transaction.commit();
		}
	}

	@Override
	public void validate(Agent oldValue) throws RuleCheckException
	{
		RuleCheck.validate(this);

		RuleCheck.notNull("tier", tier);
		RuleCheck.equals("tierID", tierID, tier.getId());

		RuleCheck.notLonger("key1", key1, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key2", key2, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key3", key3, KEY_MAX_LENGTH);
		RuleCheck.notLonger("key4", key4, KEY_MAX_LENGTH);

		/*
		Causes Problems with lazy initialization when Agent is a detached copy.
		// Only one Root Agent
		if (ROOT_ACCOUNT_NAME.equals(accountNumber))
			RuleCheck.oneOf("tier.type", tier.getType(), Tier.TYPE_ROOT);
		else
			RuleCheck.oneOf("tier.type", tier.getType(), Tier.TYPE_STORE, Tier.TYPE_WHOLESALER, Tier.TYPE_RETAILER);
		*/
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////
	static class AgentExtender extends PredicateExtender<Agent>
	{
		private int myID;

		public AgentExtender(int myID)
		{
			this.myID = myID;
		}

		@Override
		public String getName()
		{
			return "MyAgents";
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<Agent> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			Predicate p1 = cb.equal(col(root, "id"), cb.parameter(Integer.class, "myID"));
			Predicate p2 = cb.isNotNull(col(root, "ownerAgentID"));
			Predicate p3 = cb.equal(col(root, "ownerAgentID"), cb.parameter(Integer.class, "myID"));

			predicates.add(cb.or(p1, cb.and(p2, p3)));

			return predicates;
		}

		@Override
		public void addParameters(TypedQuery<?> query)
		{
			query.setParameter("myID", myID);
		}

	}
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAuthenticable
	//
	// /////////////////////////////////
	@Override
	public byte[] validateNewPin(EntityManager em, CompanyInfo company, String newPin) throws RuleCheckException
	{
		return Agents.validateNewPin(em, company, this, newPin);
	}
	
	@Override
	public String offerPIN(EntityManager em, Session session, CompanyInfo company, String pin) throws RuleCheckException
	{
		int attempts = getConsecutiveAuthFailures() == null ? 0 : getConsecutiveAuthFailures();
		if (attempts < 0)
			return TransactionsConfig.ERR_PIN_LOCKOUT;

		// Test if Same
		boolean same = AuthenticationHelper.testIfSamePin(this, pin);
		if (same && attempts == 0)
			return null;

		// Update Agent
		Agent original = Agent.findByID(em, getId(), session.getCompanyID());
		if (original == null || getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", getId());
		original = new Agent(original, false);
		if (same)
			setConsecutiveAuthFailures(0);
		else
		{
			attempts++;
			AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);
			if (attempts >= config.getMaxPinRetriesBeforeLockout())
				attempts = -1;
			setConsecutiveAuthFailures(attempts);
		}
		AuditEntryContext auditEntryContext = new AuditEntryContext("AGENT_AUTH", this.id); 
		persist(em, original, session, auditEntryContext);
		
		// Return result
		if (attempts < 0)
			return TransactionsConfig.ERR_PIN_LOCKOUT;
		else if (!same)
			return TransactionsConfig.ERR_INVALID_PIN;
		else
			return null;
	}
		
	@Override
	public void updatePin(EntityManager em, byte[] key, Session session) throws RuleCheckException
	{
		Agent oldAgent = new Agent(this, false);
		AuthenticationHelper.updatePin(this, em, key, session);
		AuditEntryContext auditEntryContext = new AuditEntryContext("AGENT_PIN_CHANGE", this.getId());   
		persist(em, oldAgent, session, auditEntryContext);
	}
			
	@Override
	public boolean testIfSamePin(String pin) 
	{
		return AuthenticationHelper.testIfSamePin(this, pin);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IUserAgent
	//
	// /////////////////////////////////
	@Override
	public void validateAgentImsi(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session) throws RuleCheckException
	{
		Agents.validateAgentImsi(context, em, transactionsConfig, session, this);
	}
	
	@Override
	public void updateAgentImei(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session) throws RuleCheckException
	{
		Agents.updateAgentImei(context, em, transactionsConfig, session, this);
	}

	public Agent(Agent agent) {
		super(agent);
		this.key1 = agent.key1;
		this.key2 = agent.key2;
		this.key3 = agent.key3;
		this.key4 = agent.key4;
		this.tier = agent.tier;
		this.supplier = agent.supplier;
		this.owner = agent.owner;
		this.group = agent.group;
		this.area = agent.area;
		this.serviceClass = agent.serviceClass;
		this.role = agent.role;
		this.lastUserID = agent.lastUserID;
		this.lastTime = agent.lastTime;
		this.lastCellID = agent.lastCellID;
		this.lastCell = agent.lastCell;
		this.lastCellExpiryTime = agent.lastCellExpiryTime;
	}
}

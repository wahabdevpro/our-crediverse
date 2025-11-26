package hxc.services.ecds.olapmodel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.ConstructorResult;
import javax.persistence.ColumnResult;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.ICompanyData;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.FormatHelper;
import hxc.services.ecds.util.RuleCheckException;

@SqlResultSetMappings({
	@SqlResultSetMapping(name="OlapTransaction.AggregationForSalesByArea", classes = {
		@ConstructorResult(targetClass = OlapResultByArea.RowMapping.class, 
			columns = {
				@ColumnResult(name="a_cell", type=Integer.class),
				@ColumnResult(name="a_agent_id", type=Integer.class),
				@ColumnResult(name="count",type=Long.class),
				@ColumnResult(name="sum", type=BigDecimal.class),
				@ColumnResult(name="type", type=String.class),
				@ColumnResult(name="success", type=Boolean.class)
		})
	})
})

@Table(name = "ap_transact", uniqueConstraints = { @UniqueConstraint(name = "ap_transact_no", columnNames = { "comp_id", "no" }) },
	indexes = {
		@Index(name = "ap_transact_ended", columnList = "ended_date,ended_time"),

		@Index(name = "ap_transact_type", columnList = "type"),
		@Index(name = "ap_transact_a_tier_type", columnList = "a_tier_type"),

		@Index(name = "ap_transact_a_msisdn", columnList = "a_msisdn"),
		@Index(name = "ap_transact_a_owner_msisdn", columnList = "a_owner_msisdn"),
		@Index(name = "ap_transact_a_tier_name", columnList = "a_tier_name"),
		@Index(name = "ap_transact_a_group_name", columnList = "a_group_name"),
		@Index(name = "ap_transact_a_sc_name", columnList = "a_sc_name"),

		@Index(name = "ap_transact_b_msisdn", columnList = "b_msisdn"),
		@Index(name = "ap_transact_b_owner_msisdn", columnList = "b_owner_msisdn"),
		@Index(name = "ap_transact_b_tier_name", columnList = "b_tier_name"),
		@Index(name = "ap_transact_b_group_name", columnList = "b_group_name"),
		@Index(name = "ap_transact_b_sc_name", columnList = "b_sc_name"),
		@Index(name = "ap_transact_follow_up", columnList = "follow_up"),

		// For Wholesaler and Retailer performance reports
		@Index(name = "ap_transact_aggregate000", columnList = "ended_date,a_msisdn,type,success,b_msisdn"),
		@Index(name = "ap_transact_aggregate001", columnList = "a_owner_id,ended_date,a_msisdn,type,success,b_msisdn"),
		@Index(name = "ap_transact_aggregate002", columnList = "b_owner_id,ended_date,a_msisdn,type,success,b_msisdn")
	}
)
@Entity
@NamedQueries({
	@NamedQuery(name = "OlapTransaction.deleteOlderThan", query = "DELETE FROM OlapTransaction transaction WHERE transaction.endDate < date(:refTime)"),
})
public class OlapTransaction implements Serializable
{
	private static final long serialVersionUID = -8100937649042703237L;
	
	final static Logger logger = LoggerFactory.getLogger(OlapTransaction.class);
	
	public static final Permission ANALYTICS_MAY_VIEW = new Permission(false, true, Permission.GROUP_ANALYTICS, Permission.PERM_VIEW, "May view Analytics");
	public static final Permission ANALYTICS_MAY_CONFIGURE = new Permission(false, true, Permission.GROUP_ANALYTICS, Permission.PERM_CONFIGURE, "May configure Analytics");
	
/*
	public static enum FollowUp
	{
		NONE("NONE"),
		PENDING("PENDING"),
		ADJUDICATED("ADJUDICATED");

		private final String name;       

		private FollowUp(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}
*/


	public static class FollowUp
	{
		public static final String NONE = "NONE";
		public static final String PENDING = "PENDING";
		public static final String ADJUDICATED = "ADJUDICATED";
	}	

	protected long id;
	protected int companyID;
	protected int version;
	protected String number;

	protected String type;
	protected String channel;
	protected String channelType;
	protected Date startTime;
	protected Date endDate;
	protected Date endTime;
	protected String requestMode;

	protected String a_TierType;
	protected String a_TierName;
	protected String a_ServiceClassName;
	protected Integer a_CellID;

	protected String b_TierType;
	protected String b_TierName;
	protected String b_ServiceClassName;
	protected Integer b_CellID;

	protected BigDecimal amount;
	protected BigDecimal grossSalesAmount;
	protected BigDecimal costOfGoodsSold;
	protected BigDecimal buyerTradeBonusAmount;
	protected BigDecimal buyerTradeBonusProvision;
	protected BigDecimal buyerTradeBonusPercentage;
	protected BigDecimal chargeLevied;

	protected String bundleName;
	protected String promotionName;

	protected boolean success;
	protected Boolean rolledBack;
	protected String followUp;
	protected Long relatedID;

	protected Integer a_AgentID;
	protected String a_AgentName;
	protected String a_AgentAccountNumber;
	protected String a_MSISDN;
	protected String a_IMSI;
	protected String a_IMEI;
	protected Integer a_GroupID;
	protected String a_GroupName;
	protected String a_OwnerMobileNumber;
	protected String a_OwnerIMSI;
	protected String a_OwnerName;
	protected Integer a_OwnerID;
	protected String a_AreaName;
	protected BigDecimal a_BalanceBefore;
	protected BigDecimal a_BalanceAfter;
	protected BigDecimal a_BonusBalanceBefore;
	protected BigDecimal a_BonusBalanceAfter;
	protected BigDecimal a_OnHoldBalanceBefore;
	protected BigDecimal a_OnHoldBalanceAfter;

	protected Integer b_AgentID;
	protected String b_AgentName;
	protected String b_AgentAccountNumber;
	protected String b_MSISDN;
	protected String b_IMSI;
	protected String b_IMEI;
	protected Integer b_GroupID;
	protected String b_GroupName;
	protected String b_OwnerMobileNumber;
	protected String b_OwnerIMSI;
	protected String b_OwnerName;
	protected Integer b_OwnerID;
	protected String b_AreaName;
	protected BigDecimal b_BalanceBefore;
	protected BigDecimal b_BalanceAfter;
	protected BigDecimal b_BonusBalanceBefore;
	protected BigDecimal b_BonusBalanceAfter;
	protected BigDecimal b_TransferBonusAmount;
	protected String b_TransferBonusProfile;

	@Id
	public long getId()
	{
		return this.id;
	}

	public OlapTransaction setId(long id)
	{
		this.id = id;
		return this;
	}

	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return this.companyID;
	}

	public OlapTransaction setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return this.version;
	}

	public OlapTransaction setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Column(name = "no", nullable = false, length = Transaction.TRANSACTION_NUMBER_MAX_LENGTH)
	public String getNumber()
	{
		return this.number;
	}

	public OlapTransaction setNumber(String number)
	{
		this.number = number;
		return this;
	}

	@Column(name = "type", nullable = false, length = Transaction.TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	public OlapTransaction setType(String type)
	{
		this.type = type;
		return this;
	}

	@Column(name = "channel", nullable = false, length = Transaction.CHANNEL_MAX_LENGTH)
	public String getChannel()
	{
		return channel;
	}

	public OlapTransaction setChannel(String channel)
	{
		this.channel = channel;
		return this;
	}

	@Column(name = "channel_type", nullable = true, length = 2)
	public String getChannelType()
	{
		return channelType;
	}

	public OlapTransaction setChannelType(String channelType)
	{
		this.channelType = channelType;
		return this;
	}

	@Column(name = "started", nullable = false)
	public Date getStartTime()
	{
		return startTime;
	}

	public OlapTransaction setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	// This is nullable for upgrade that splits it ...
	@Column(name = "ended_date", nullable = true)
	@Temporal(TemporalType.DATE)
	public Date getEndDate()
	{
		return this.endDate;
	}

	public OlapTransaction setEndDate(Date endDate)
	{
		this.endDate = endDate;
		return this;
	}

	// This is nullable for upgrade that splits it ...
	@Column(name = "ended_time", nullable = true)
	@Temporal(TemporalType.TIME)
	public Date getEndTime()
	{
		return this.endTime;
	}

	public OlapTransaction setEndTime(Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	@Column(name = "mode", nullable = false, length = Transaction.MODE_MAX_LENGTH)
	public String getRequestMode()
	{
		return requestMode;
	}

	public OlapTransaction setRequestMode(String requestMode)
	{
		this.requestMode = requestMode;
		return this;
	}

	@Column(name = "a_tier_type", nullable = true, length = Tier.TYPE_MAX_LENGTH)
	public String getA_TierType()
	{
		return this.a_TierType;
	}

	public OlapTransaction setA_TierType(String a_TierType)
	{
		this.a_TierType = a_TierType;
		return this;
	}

	@Column(name = "a_tier_name", nullable = true, length = Tier.NAME_MAX_LENGTH)
	public String getA_TierName()
	{
		return this.a_TierName;
	}

	public OlapTransaction setA_TierName(String a_TierName)
	{
		this.a_TierName = a_TierName;
		return this;
	}

	@Column(name = "a_sc_name", nullable = true, length = ServiceClass.NAME_MAX_LENGTH)
	public String getA_ServiceClassName()
	{
		return this.a_ServiceClassName;
	}

	public OlapTransaction setA_ServiceClassName(String a_ServiceClassName)
	{
		this.a_ServiceClassName = a_ServiceClassName;
		return this;
	}

	@Column(name = "a_cell", nullable = true)
	public Integer getA_CellID()
	{
		return this.a_CellID;
	}

	public OlapTransaction setA_CellID(Integer a_CellID)
	{
		this.a_CellID = a_CellID;
		return this;
	}

	@Column(name = "b_tier_type", nullable = true, length = Tier.TYPE_MAX_LENGTH)
	public String getB_TierType()
	{
		return this.b_TierType;
	}

	public OlapTransaction setB_TierType(String b_TierType)
	{
		this.b_TierType = b_TierType;
		return this;
	}

	@Column(name = "b_tier_name", nullable = true, length = Tier.NAME_MAX_LENGTH)
	public String getB_TierName()
	{
		return this.b_TierName;
	}

	public OlapTransaction setB_TierName(String b_TierName)
	{
		this.b_TierName = b_TierName;
		return this;
	}

	@Column(name = "b_sc_name", nullable = true, length = ServiceClass.NAME_MAX_LENGTH)
	public String getB_ServiceClassName()
	{
		return this.b_ServiceClassName;
	}

	public OlapTransaction setB_ServiceClassName(String b_ServiceClassName)
	{
		this.b_ServiceClassName = b_ServiceClassName;
		return this;
	}

	@Column(name = "b_cell", nullable = true)
	public Integer getB_CellID()
	{
		return this.b_CellID;
	}

	public OlapTransaction setB_CellID(Integer b_CellID)
	{
		this.b_CellID = b_CellID;
		return this;
	}

	@Column(name = "amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getAmount()
	{
		return this.amount;
	}

	public OlapTransaction setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	@Column(name = "gross_sales_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getGrossSalesAmount()
	{
		return this.grossSalesAmount;
	}

	public OlapTransaction setGrossSalesAmount(BigDecimal grossSalesAmount)
	{
		this.grossSalesAmount = grossSalesAmount;
		return this;
	}

	@Column(name = "cost_of_goods_sold", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getCostOfGoodsSold()
	{
		return this.costOfGoodsSold;
	}

	public OlapTransaction setCostOfGoodsSold(BigDecimal costOfGoodsSold)
	{
		this.costOfGoodsSold = costOfGoodsSold;
		return this;
	}

	@Column(name = "bonus", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusAmount()
	{
		return this.buyerTradeBonusAmount;
	}

	public OlapTransaction setBuyerTradeBonusAmount(BigDecimal buyerTradeBonusAmount)
	{
		this.buyerTradeBonusAmount = buyerTradeBonusAmount;
		return this;
	}

	@Column(name = "bonus_prov", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusProvision()
	{
		return this.buyerTradeBonusProvision;
	}

	public OlapTransaction setBuyerTradeBonusProvision(BigDecimal buyerTradeBonusProvision)
	{
		this.buyerTradeBonusProvision= buyerTradeBonusProvision;
		return this;
	}

	@Column(name = "bonus_pct", nullable = true, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusPercentage()
	{
		return this.buyerTradeBonusPercentage;
	}

	public OlapTransaction setBuyerTradeBonusPercentage(BigDecimal buyerTradeBonusPercentage)
	{
		this.buyerTradeBonusPercentage = buyerTradeBonusPercentage;
		return this;
	}

	@Column(name = "charge", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getChargeLevied()
	{
		return this.chargeLevied;
	}

	public OlapTransaction setChargeLevied(BigDecimal chargeLevied)
	{
		this.chargeLevied = chargeLevied;
		return this;
	}

	@Column(name = "bundle_name", nullable = true, length = Bundle.NAME_MAX_LENGTH)
	public String getBundleName()
	{
		return this.bundleName;
	}

	public OlapTransaction setBundleName( String bundleName )
	{
		this.bundleName = bundleName;
		return this;
	}

	@Column(name = "promotion_name", nullable = true, length = Promotion.NAME_MAX_LENGTH)
	public String getPromotionName()
	{
		return this.promotionName;
	}

	public OlapTransaction setPromotionName( String promotionName )
	{
		this.promotionName = promotionName;
		return this;
	}


	@Column(name = "success", nullable = false, columnDefinition = "TINYINT", length = 1)
	public Boolean getSuccess()
	{
		return this.success;
	}

	public OlapTransaction setSuccess(Boolean success)
	{
		this.success = success;
		return this;
	}

	@Column(name = "rolled_back", nullable = true, columnDefinition = "TINYINT", length = 1)
	public Boolean getRolledBack()
	{
		return this.rolledBack;
	}

	public OlapTransaction setRolledBack(Boolean rolledBack)
	{
		this.rolledBack = rolledBack;
		return this;
	}

	//@Enumerated(EnumType.STRING)
	@Column(name = "follow_up", nullable = true)
	public String getFollowUp()
	{
		return this.followUp;
	}

	public OlapTransaction setFollowUp(String followUp)
	{
		this.followUp = followUp;
		return this;
	}
	
	@Column(name = "related_id", nullable = true/*, columnDefinition = "BIGINT"*/)
	public Long getRelatedID()
	{
		return this.relatedID;
	}

	public OlapTransaction setRelatedID(Long relatedID)
	{
		this.relatedID = relatedID;
		return this;
	}


	@Column(name = "a_agent_id", nullable = true)
	public Integer getA_AgentID()
	{
		return this.a_AgentID;
	}

	public OlapTransaction setA_AgentID( Integer a_AgentID )
	{
		this.a_AgentID = a_AgentID;
		return this;
	}

	@Column(name = "a_agent_name", nullable = true, length = (Agent.TITLE_MAX_LENGTH + 1 + Agent.FIRST_NAME_MAX_LENGTH + 1 + Agent.LAST_NAME_MAX_LENGTH))
	public String getA_AgentName()
	{
		return this.a_AgentName;
	}

	public OlapTransaction setA_AgentName( String a_AgentName )
	{
		this.a_AgentName = a_AgentName;
		return this;
	}

	@Column(name = "a_agent_acc_no", nullable = true, length = Agent.ACCOUNT_NO_MAX_LENGTH)
	public String getA_AgentAccountNumber()
	{
		return this.a_AgentAccountNumber;
	}

	public OlapTransaction setA_AgentAccountNumber(String a_AgentAccountNumber)
	{
		this.a_AgentAccountNumber = a_AgentAccountNumber;
		return this;
	}

	@Column(name = "a_msisdn", nullable = true, length = Transaction.MSISDN_MAX_LENGTH)
	public String getA_MSISDN()
	{
		return this.a_MSISDN;
	}

	public OlapTransaction setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
		return this;
	}

	@Column(name = "a_imsi", nullable = true, length = Transaction.IMSI_MAX_LENGTH)
	public String getA_IMSI()
	{
		return this.a_IMSI;
	}

	public OlapTransaction setA_IMSI(String a_IMSI)
	{
		this.a_IMSI = a_IMSI;
		return this;
	}
	
	@Column(name = "a_imei", nullable = true, length = Transaction.IMEI_MAX_LENGTH)
	public String getA_IMEI()
	{
		return this.a_IMEI;
	}

	public OlapTransaction setA_IMEI(String a_IMEI)
	{
		this.a_IMEI = a_IMEI;
		return this;
	}

	@Transient
	public Integer getA_GroupID()
	{
		return this.a_GroupID;
	}

	public OlapTransaction setA_GroupID(Integer a_GroupID)
	{
		this.a_GroupID = a_GroupID;
		return this;
	}

	@Column(name = "a_group_name", nullable = true, length = Group.NAME_MAX_LENGTH)
	public String getA_GroupName()
	{
		return this.a_GroupName;
	}

	public OlapTransaction setA_GroupName(String a_GroupName)
	{
		this.a_GroupName = a_GroupName;
		return this;
	}

	@Column(name = "a_owner_msisdn", nullable = true, length = Agent.PHONE_NUMBER_MAX_LENGTH)
	public String getA_OwnerMobileNumber()
	{
		return this.a_OwnerMobileNumber;
	}

	public OlapTransaction setA_OwnerMobileNumber(String a_OwnerMobileNumber)
	{
		this.a_OwnerMobileNumber = a_OwnerMobileNumber;
		return this;
	}

	@Column(name = "a_owner_imsi", nullable = true, length = Transaction.IMSI_MAX_LENGTH)
	public String getA_OwnerIMSI()
	{
		return this.a_OwnerIMSI;
	}

	public OlapTransaction setA_OwnerIMSI( String a_OwnerIMSI )
	{
		this.a_OwnerIMSI = a_OwnerIMSI;
		return this;
	}

	@Column(name = "a_owner_name", nullable = true, length = (Agent.TITLE_MAX_LENGTH + 1 + Agent.FIRST_NAME_MAX_LENGTH + 1 + Agent.LAST_NAME_MAX_LENGTH))
	public String getA_OwnerName()
	{
		return this.a_OwnerName;
	}

	public OlapTransaction setA_OwnerName( String a_OwnerName )
	{
		this.a_OwnerName = a_OwnerName;
		return this;
	}

	@Column(name = "a_owner_id", nullable = true)
	public Integer getA_OwnerID()
	{
		return this.a_OwnerID;
	}

	public OlapTransaction setA_OwnerID( Integer a_OwnerID )
	{
		this.a_OwnerID = a_OwnerID;
		return this;
	}

	// XXX TODO FIXME ... rework after legnth is set for Area
	@Column(name = "a_area_name", nullable = true, length = 128)
	public String getA_AreaName()
	{
		return this.a_AreaName;
	}

	public OlapTransaction setA_AreaName(String a_AreaName)
	{
		this.a_AreaName = a_AreaName;
		return this;
	}

	@Column(name = "a_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BalanceBefore()
	{
		return this.a_BalanceBefore;
	}

	public OlapTransaction setA_BalanceBefore(BigDecimal a_BalanceBefore)
	{
		this.a_BalanceBefore = a_BalanceBefore;
		return this;
	}

	@Column(name = "a_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BalanceAfter()
	{
		return this.a_BalanceAfter;
	}

	public OlapTransaction setA_BalanceAfter(BigDecimal a_BalanceAfter)
	{
		this.a_BalanceAfter = a_BalanceAfter;
		return this;
	}

	@Transient
	public BigDecimal getA_BonusBalanceBefore()
	{
		return this.a_BonusBalanceBefore;
	}

	public OlapTransaction setA_BonusBalanceBefore(BigDecimal a_BonusBalanceBefore)
	{
		this.a_BonusBalanceBefore = a_BonusBalanceBefore;
		return this;
	}

	@Transient
	public BigDecimal getA_BonusBalanceAfter()
	{
		return this.a_BonusBalanceAfter;
	}

	public OlapTransaction setA_BonusBalanceAfter(BigDecimal a_BonusBalanceAfter)
	{
		this.a_BonusBalanceAfter = a_BonusBalanceAfter;
		return this;
	}

	@Transient
	public BigDecimal getA_OnHoldBalanceBefore()
	{
		return this.a_OnHoldBalanceBefore;
	}

	public OlapTransaction setA_OnHoldBalanceBefore(BigDecimal a_OnHoldBalanceBefore)
	{
		this.a_OnHoldBalanceBefore = a_OnHoldBalanceBefore;
		return this;
	}

	@Transient
	public BigDecimal getA_OnHoldBalanceAfter()
	{
		return this.a_OnHoldBalanceAfter;
	}

	public OlapTransaction setA_OnHoldBalanceAfter(BigDecimal a_OnHoldBalanceAfter)
	{
		this.a_OnHoldBalanceAfter = a_OnHoldBalanceAfter;
		return this;
	}

	@Column(name = "b_agent_id", nullable = true)
	public Integer getB_AgentID()
	{
		return this.b_AgentID;
	}

	public OlapTransaction setB_AgentID( Integer b_AgentID )
	{
		this.b_AgentID = b_AgentID;
		return this;
	}

	@Column(name = "b_agent_name", nullable = true, length = (Agent.TITLE_MAX_LENGTH + 1 + Agent.FIRST_NAME_MAX_LENGTH + 1 + Agent.LAST_NAME_MAX_LENGTH))
	public String getB_AgentName()
	{
		return this.b_AgentName;
	}

	public OlapTransaction setB_AgentName( String b_AgentName )
	{
		this.b_AgentName = b_AgentName;
		return this;
	}

	@Column(name = "b_agent_acc_no", nullable = true, length = Agent.ACCOUNT_NO_MAX_LENGTH)
	public String getB_AgentAccountNumber()
	{
		return this.b_AgentAccountNumber;
	}

	public OlapTransaction setB_AgentAccountNumber(String b_AgentAccountNumber)
	{
		this.b_AgentAccountNumber = b_AgentAccountNumber;
		return this;
	}

	@Column(name = "b_msisdn", nullable = true, length = Transaction.MSISDN_MAX_LENGTH)
	public String getB_MSISDN()
	{
		return this.b_MSISDN;
	}

	public OlapTransaction setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
		return this;
	}

	@Column(name = "b_imsi", nullable = true, length = Transaction.IMSI_MAX_LENGTH)
	public String getB_IMSI()
	{
		return this.b_IMSI;
	}

	public OlapTransaction setB_IMSI(String b_IMSI)
	{
		this.b_IMSI = b_IMSI;
		return this;
	}
	
	@Column(name = "b_imei", nullable = true, length = Transaction.IMEI_MAX_LENGTH)
	public String getB_IMEI()
	{
		return this.b_IMEI;
	}

	public OlapTransaction setB_IMEI(String b_IMEI)
	{
		this.b_IMEI = b_IMEI;
		return this;
	}

	@Transient
	public Integer getB_GroupID()
	{
		return this.b_GroupID;
	}

	public OlapTransaction setB_GroupID(Integer b_GroupID)
	{
		this.b_GroupID = b_GroupID;
		return this;
	}

	@Column(name = "b_group_name", nullable = true, length = Group.NAME_MAX_LENGTH)
	public String getB_GroupName()
	{
		return this.b_GroupName;
	}

	public OlapTransaction setB_GroupName(String b_GroupName)
	{
		this.b_GroupName = b_GroupName;
		return this;
	}

	@Column(name = "b_owner_msisdn", nullable = true, length = Agent.PHONE_NUMBER_MAX_LENGTH)
	public String getB_OwnerMobileNumber()
	{
		return this.b_OwnerMobileNumber;
	}

	public OlapTransaction setB_OwnerMobileNumber(String b_OwnerMobileNumber)
	{
		this.b_OwnerMobileNumber = b_OwnerMobileNumber;
		return this;
	}

	@Column(name = "b_owner_imsi", nullable = true, length = Transaction.IMSI_MAX_LENGTH)
	public String getB_OwnerIMSI()
	{
		return this.b_OwnerIMSI;
	}

	public OlapTransaction setB_OwnerIMSI( String b_OwnerIMSI )
	{
		this.b_OwnerIMSI = b_OwnerIMSI;
		return this;
	}

	@Column(name = "b_owner_name", nullable = true, length = (Agent.TITLE_MAX_LENGTH + 1 + Agent.FIRST_NAME_MAX_LENGTH + 1 + Agent.LAST_NAME_MAX_LENGTH))
	public String getB_OwnerName()
	{
		return this.b_OwnerName;
	}

	public OlapTransaction setB_OwnerName( String b_OwnerName )
	{
		this.b_OwnerName = b_OwnerName;
		return this;
	}

	@Column(name = "b_owner_id", nullable = true)
	public Integer getB_OwnerID()
	{
		return this.b_OwnerID;
	}

	public OlapTransaction setB_OwnerID( Integer b_OwnerID )
	{
		this.b_OwnerID = b_OwnerID;
		return this;
	}

	// XXX TODO FIXME ... rework after legnth is set for Area
	@Column(name = "b_area_name", nullable = true, length = 128)
	public String getB_AreaName()
	{
		return this.b_AreaName;
	}

	public OlapTransaction setB_AreaName(String b_AreaName)
	{
		this.b_AreaName = b_AreaName;
		return this;
	}

	@Column(name = "b_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BalanceBefore()
	{
		return this.b_BalanceBefore;
	}

	public OlapTransaction setB_BalanceBefore(BigDecimal b_BalanceBefore)
	{
		this.b_BalanceBefore = b_BalanceBefore;
		return this;
	}

	@Column(name = "b_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BalanceAfter()
	{
		return this.b_BalanceAfter;
	}

	public OlapTransaction setB_BalanceAfter(BigDecimal b_BalanceAfter)
	{
		this.b_BalanceAfter = b_BalanceAfter;
		return this;
	}

	@Transient
	public BigDecimal getB_BonusBalanceBefore()
	{
		return this.b_BonusBalanceBefore;
	}

	public OlapTransaction setB_BonusBalanceBefore(BigDecimal b_BonusBalanceBefore)
	{
		this.b_BonusBalanceBefore = b_BonusBalanceBefore;
		return this;
	}

	@Transient
	public BigDecimal getB_BonusBalanceAfter()
	{
		return this.b_BonusBalanceAfter;
	}

	public OlapTransaction setB_BonusBalanceAfter(BigDecimal b_BonusBalanceAfter)
	{
		this.b_BonusBalanceAfter = b_BonusBalanceAfter;
		return this;
	}

	@Column(name = "b_transfer_bonus_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_TransferBonusAmount()
	{
		return this.b_TransferBonusAmount;
	}

	public OlapTransaction setB_TransferBonusAmount(BigDecimal b_TransferBonusAmount)
	{
		this.b_TransferBonusAmount = b_TransferBonusAmount;
		return this;
	}

	@Column(name = "b_transfer_bonus_profile", nullable = true, length = Transaction.BONUS_PROFILE_MAX_LENGTH)
	public String getB_TransferBonusProfile()
	{
		return this.b_TransferBonusProfile;
	}

	public OlapTransaction setB_TransferBonusProfile( String b_TransferBonusProfile )
	{
		this.b_TransferBonusProfile = b_TransferBonusProfile;
		return this;
	}

	// Constructors

	public OlapTransaction()
	{
	}

	public OlapTransaction(Transaction transaction)
	{
		this(
			transaction, transaction.getBundle(), transaction.getPromotion(),
			transaction.getA_Tier(), transaction.getA_ServiceClass(),
			transaction.getB_Tier(), transaction.getB_ServiceClass(),
			transaction.getA_Agent(), transaction.getA_Group(), transaction.getA_Owner(),
			transaction.getB_Agent(), transaction.getB_Group(), transaction.getB_Owner()
		);
	}

	public OlapTransaction(
		Transaction transaction, Bundle bundle, Promotion promotion,

		Tier a_Tier, ServiceClass a_ServiceClass,
		Tier b_Tier, ServiceClass b_ServiceClass,

		Agent a_Agent, Group a_Group, Agent a_Owner,
		Agent b_Agent, Group b_Group, Agent b_Owner
	)
	{
		this(
			transaction, (bundle != null ? bundle.getName() : null), (bundle != null ? promotion.getName() : null),

			(a_Tier != null ? a_Tier.getType() : null), (a_Tier != null ? a_Tier.getName() : null), (a_ServiceClass != null ? a_ServiceClass.getName() : null),
			(b_Tier != null ? b_Tier.getType() : null), (b_Tier != null ? b_Tier.getName() : null), (b_ServiceClass != null ? b_ServiceClass.getName() : null),

			(a_Agent != null ? a_Agent.getTitle() : null), (a_Agent != null ? a_Agent.getFirstName() : null), (a_Agent != null ? a_Agent.getSurname() : null),
			(a_Agent != null ? a_Agent.getAccountNumber() : null), (a_Group != null ? a_Group.getName() : null),
			(a_Owner != null ? a_Owner.getMobileNumber() : null), (a_Owner != null ? a_Owner.getImsi() : null),
			(a_Owner != null ? a_Owner.getTitle() : null), (a_Owner != null ? a_Owner.getFirstName() : null), (a_Owner != null ? a_Owner.getSurname() : null), (a_Owner != null ? a_Owner.getId() : null),

			(b_Agent != null ? b_Agent.getTitle() : null), (b_Agent != null ? b_Agent.getFirstName() : null), (b_Agent != null ? b_Agent.getSurname() : null),
			(b_Agent != null ? b_Agent.getAccountNumber() : null), (b_Group != null ? b_Group.getName() : null),
			(b_Owner != null ? b_Owner.getMobileNumber() : null), (b_Owner != null ? b_Owner.getImsi() : null),
			(b_Owner != null ? b_Owner.getTitle() : null), (b_Owner != null ? b_Owner.getFirstName() : null), (b_Owner != null ? b_Owner.getSurname() : null), (b_Owner != null ? b_Owner.getId() : null)
		);
	}

	public OlapTransaction(
		Transaction transaction, String bundleName, String promotionName,

		String a_TierType, String a_TierName, String a_ServiceClassName,
		String b_TierType, String b_TierName, String b_ServiceClassName,

		String a_AgentTitle, String a_AgentFirstName, String a_AgentLastName,
		String a_AgentAccountNumber, String a_GroupName,
		String a_OwnerMobileNumber, String a_OwnerIMSI,
		String a_OwnerTitle, String a_OwnerFirstName, String a_OwnerLastName, Integer a_OwnerID,

		String b_AgentTitle, String b_AgentFirstName, String b_AgentLastName,
		String b_AgentAccountNumber, String b_GroupName,
		String b_OwnerMobileNumber, String b_OwnerIMSI,
		String b_OwnerTitle, String b_OwnerFirstName, String b_OwnerLastName, Integer b_OwnerID
	)
	{
		this(
				transaction.getId(), transaction.getCompanyID(), transaction.getVersion(), transaction.getNumber(),
				transaction.getType(), transaction.getChannel(), transaction.getChannelType(), transaction.getStartTime(), transaction.getEndTime(), transaction.getRequestMode(),

				a_TierType, a_TierName, a_ServiceClassName, transaction.getA_CellID(),
				b_TierType, b_TierName, b_ServiceClassName, transaction.getB_CellID(),

				transaction.getAmount(), transaction.getGrossSalesAmount(), transaction.getCostOfGoodsSold(),
				transaction.getBuyerTradeBonusAmount(), transaction.getBuyerTradeBonusProvision(), transaction.getBuyerTradeBonusPercentage(), transaction.getChargeLevied(),
				bundleName, promotionName,
				transaction.getReturnCode(),
				transaction.isRolledBack(),
				transaction.isFollowUp(),
				transaction.getReversedID(),

				transaction.getA_AgentID(), a_AgentTitle, a_AgentFirstName, a_AgentLastName,
				a_AgentAccountNumber, transaction.getA_MSISDN(), transaction.getA_IMSI(), transaction.getA_IMEI(), transaction.getA_GroupID(), a_GroupName,
				a_OwnerMobileNumber, a_OwnerIMSI,
				a_OwnerTitle, a_OwnerFirstName, a_OwnerLastName, a_OwnerID,
				transaction.getA_BalanceBefore(), transaction.getA_BalanceAfter(),
				transaction.getA_BonusBalanceBefore(), transaction.getA_BonusBalanceAfter(),
				transaction.getA_OnHoldBalanceBefore(), transaction.getA_OnHoldBalanceAfter(),

				transaction.getB_AgentID(), b_AgentTitle, b_AgentFirstName, b_AgentLastName,
				b_AgentAccountNumber, transaction.getB_MSISDN(), transaction.getB_IMSI(), transaction.getB_IMEI(), transaction.getB_GroupID(), b_GroupName,
				b_OwnerMobileNumber, b_OwnerIMSI,
				b_OwnerTitle, b_OwnerFirstName, b_OwnerLastName, b_OwnerID,
				transaction.getB_BalanceBefore(), transaction.getB_BalanceAfter(),
				transaction.getB_BonusBalanceBefore(), transaction.getB_BonusBalanceAfter(),
				transaction.getB_TransferBonusAmount(), transaction.getB_TransferBonusProfile()
		);
	}

	// XXX TODO FIXME area names ...
	public OlapTransaction(
			long id, int companyID, int version, String number,
			String type, String channel, String channelType, Date startTime, Date endTime, String requestMode,

			String a_TierType, String a_TierName, String a_ServiceClassName, Integer a_CellID,
			String b_TierType, String b_TierName, String b_ServiceClassName, Integer b_CellID,

			BigDecimal amount, BigDecimal grossSalesAmount, BigDecimal costOfGoodsSold,
			BigDecimal bonusAmount, BigDecimal bonusProvision, BigDecimal bonusPercentage, BigDecimal chargeLevied,
			String bundleName, String promotionName,
			String returnCode,
			Boolean rolledBack,
			Boolean followUp,
			Long relatedID,

			Integer a_AgentID, String a_AgentTitle, String a_AgentFirstName, String a_AgentLastName,
			String a_AgentAccountNumber, String a_MSISDN, String a_IMSI, String a_IMEI, Integer a_GroupID, String a_GroupName,
			String a_OwnerMobileNumber, String a_OwnerIMSI,
			String a_OwnerTitle, String a_OwnerFirstName, String a_OwnerLastName, Integer a_OwnerID,
			BigDecimal a_BalanceBefore, BigDecimal a_BalanceAfter,
			BigDecimal a_BonusBalanceBefore, BigDecimal a_BonusBalanceAfter,
			BigDecimal a_OnHoldBalanceBefore, BigDecimal a_OnHoldBalanceAfter,

			Integer b_AgentID, String b_AgentTitle, String b_AgentFirstName, String b_AgentLastName,
			String b_AgentAccountNumber, String b_MSISDN, String b_IMSI, String b_IMEI, Integer b_GroupID, String b_GroupName,
			String b_OwnerMobileNumber, String b_OwnerIMSI,
			String b_OwnerTitle, String b_OwnerFirstName, String b_OwnerLastName, Integer b_OwnerID,
			BigDecimal b_BalanceBefore, BigDecimal b_BalanceAfter,
			BigDecimal b_BonusBalanceBefore, BigDecimal b_BonusBalanceAfter,
			BigDecimal b_TransferBonusAmount, String b_TransferBonusProfile
	)
	{
		this(
				id, companyID, version, number,
				type, channel, channelType, startTime, endTime, requestMode,

				a_TierType, a_TierName, a_ServiceClassName, a_CellID,
				b_TierType, b_TierName, b_ServiceClassName, b_CellID,

				amount, grossSalesAmount, costOfGoodsSold,
				bonusAmount, bonusProvision, bonusPercentage, chargeLevied,
				bundleName, promotionName,
				(returnCode != null && returnCode.equals(ResponseHeader.RETURN_CODE_SUCCESS)),
				rolledBack,
				followUp,
				relatedID,

				a_AgentID, a_AgentTitle + " " + a_AgentFirstName + " " + a_AgentLastName,
				a_AgentAccountNumber, a_MSISDN, a_IMSI, a_IMEI, a_GroupID, a_GroupName,
				a_OwnerMobileNumber, a_OwnerIMSI, ( a_OwnerMobileNumber != null ? a_OwnerTitle + " " + a_OwnerFirstName + " " + a_OwnerLastName : null ), a_OwnerID,
				null/*a_AreaName*/,
				a_BalanceBefore, a_BalanceAfter,
				a_BonusBalanceBefore, a_BonusBalanceAfter,
				a_OnHoldBalanceBefore, a_OnHoldBalanceAfter,

				b_AgentID, b_AgentTitle + " " + b_AgentFirstName + " " + b_AgentLastName,
				b_AgentAccountNumber, b_MSISDN, b_IMSI, b_IMEI, b_GroupID, b_GroupName,
				b_OwnerMobileNumber, b_OwnerIMSI, ( b_OwnerMobileNumber != null ? b_OwnerTitle + " " + b_OwnerFirstName + " " + b_OwnerLastName : null ), b_OwnerID,
				null/*b_AreaName*/,
				b_BalanceBefore, b_BalanceAfter,
				b_BonusBalanceBefore, b_BonusBalanceAfter,
				b_TransferBonusAmount, b_TransferBonusProfile
				);
	}

	// s/.*\s\+\([^ ]\+\)\s\+\([^ ]\+\);/\1 \2,/gc
	// s/\(\S,\)\n\(\S\)/\1 \2/gc
	public OlapTransaction(
		long id, int companyID, int version, String number,
		String type, String channel, String channelType, Date startTime, Date endTime, String requestMode,

		String a_TierType, String a_TierName, String a_ServiceClassName, Integer a_CellID,
		String b_TierType, String b_TierName, String b_ServiceClassName, Integer b_CellID,

		BigDecimal amount, BigDecimal grossSalesAmount, BigDecimal costOfGoodsSold,
		BigDecimal bonusAmount, BigDecimal bonusProvision, BigDecimal bonusPercentage, BigDecimal chargeLevied,
		String bundleName, String promotionName,
		boolean success, boolean rolledBack, boolean followUp, Long relatedID,

		Integer a_AgentID, String a_AgentName, String a_AgentAccountNumber, String a_MSISDN, String a_IMSI, String a_IMEI, Integer a_GroupID, String a_GroupName,
		String a_OwnerMobileNumber, String a_OwnerIMSI, String a_OwnerName, Integer a_OwnerID, String a_AreaName, BigDecimal a_BalanceBefore, BigDecimal a_BalanceAfter,
		BigDecimal a_BonusBalanceBefore, BigDecimal a_BonusBalanceAfter, BigDecimal a_OnHoldBalanceBefore, BigDecimal a_OnHoldBalanceAfter,

		Integer b_AgentID, String b_AgentName, String b_AgentAccountNumber, String b_MSISDN, String b_IMSI, String b_IMEI, Integer b_GroupID, String b_GroupName,
		String b_OwnerMobileNumber, String b_OwnerIMSI, String b_OwnerName, Integer b_OwnerID, String b_AreaName, BigDecimal b_BalanceBefore, BigDecimal b_BalanceAfter,
		BigDecimal b_BonusBalanceBefore, BigDecimal b_BonusBalanceAfter,
		BigDecimal b_TransferBonusAmount, String b_TransferBonusProfile
	)
	{
		this();
		// s/.* \([^ ]\+\);/\1/gc
		// s/.\+/\t\tthis.& = &;/gc
		this.id = id;
		this.companyID = companyID;
		this.version = version;
		this.number = number;

		this.type = type;
		this.channel = channel;
		this.channelType = channelType;
		this.startTime = startTime;

		this.endDate = endTime;
		this.endTime = endTime;

		this.requestMode = requestMode;

		this.a_TierType = a_TierType;
		this.a_TierName = a_TierName;
		this.a_ServiceClassName = a_ServiceClassName;
		this.a_CellID = a_CellID;

		this.b_TierType = b_TierType;
		this.b_TierName = b_TierName;
		this.b_ServiceClassName = b_ServiceClassName;
		this.b_CellID = b_CellID;

		this.amount = amount;
		this.grossSalesAmount = grossSalesAmount;
		this.costOfGoodsSold = costOfGoodsSold;
		this.buyerTradeBonusAmount = bonusAmount;
		this.buyerTradeBonusProvision = bonusProvision;
		this.buyerTradeBonusPercentage = bonusPercentage;
		this.chargeLevied = chargeLevied;

		this.bundleName = bundleName;
		this.promotionName = promotionName;

		this.success = success;
		this.rolledBack = rolledBack;
		this.followUp = followUp ? FollowUp.PENDING : FollowUp.NONE;
		this.relatedID = relatedID;

		this.a_AgentID = a_AgentID;
		this.a_AgentName = a_AgentName;
		this.a_AgentAccountNumber = a_AgentAccountNumber;
		this.a_MSISDN = a_MSISDN;
		this.a_IMSI = a_IMSI;
		this.a_IMEI = a_IMEI;
		this.a_GroupID = a_GroupID;
		this.a_GroupName = a_GroupName;
		this.a_OwnerMobileNumber = a_OwnerMobileNumber;
		this.a_OwnerIMSI = a_OwnerIMSI;
		this.a_OwnerName = a_OwnerName;
		this.a_OwnerID = a_OwnerID;
		this.a_AreaName = a_AreaName;
		this.a_BalanceBefore = a_BalanceBefore;
		this.a_BalanceAfter = a_BalanceAfter;
		this.a_BonusBalanceBefore = a_BonusBalanceBefore;
		this.a_BonusBalanceAfter = a_BonusBalanceAfter;
		this.a_OnHoldBalanceBefore = a_OnHoldBalanceBefore;
		this.a_OnHoldBalanceAfter = a_OnHoldBalanceAfter;

		this.b_AgentID = b_AgentID;
		this.b_AgentName = b_AgentName;
		this.b_AgentAccountNumber = b_AgentAccountNumber;
		this.b_MSISDN = b_MSISDN;
		this.b_IMSI = b_IMSI;
		this.b_IMEI = b_IMEI;
		this.b_GroupID = b_GroupID;
		this.b_GroupName = b_GroupName;
		this.b_OwnerMobileNumber = b_OwnerMobileNumber;
		this.b_OwnerIMSI = b_OwnerIMSI;
		this.b_OwnerName = b_OwnerName;
		this.b_OwnerID = b_OwnerID;
		this.b_AreaName = b_AreaName;
		this.b_BalanceBefore = b_BalanceBefore;
		this.b_BalanceAfter = b_BalanceAfter;
		this.b_BonusBalanceBefore = b_BonusBalanceBefore;
		this.b_BonusBalanceAfter = b_BonusBalanceAfter;

		this.b_TransferBonusAmount = b_TransferBonusAmount;
		this.b_TransferBonusProfile = b_TransferBonusProfile;
	}

	// Helpers

	public static long getNextId(EntityManager apEm)
	{
		TypedQuery<Long> query = apEm.createQuery("SELECT olap_transaction.id FROM OlapTransaction olap_transaction ORDER BY olap_transaction.id DESC", Long.class);
		List<Long> resultList = query.setMaxResults(1).getResultList();
		if (resultList.size() >= 1)
		{
			return resultList.get(0).longValue() + 1;
		}
		else
		{
			return 0;
		}
	}
	
	public static List<OlapResultByArea.RowMapping> getAggregationByArea(EntityManager apEm, String txCode, Date start, Date end) throws ParseException
	{
		String from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(start);
		String to = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(end);

		String nativeQuery = "SELECT a_cell, a_agent_id, COUNT(id) count, SUM(amount) sum, type, success FROM ap_transact " +
						"WHERE a_agent_id IS NOT NULL AND type = '" + txCode + "' " +
						"AND started >= '" + from + "' AND started <= '" + to + "' " +
						"GROUP BY a_cell, a_agent_id, type, success";

		Query query = apEm.createNativeQuery(nativeQuery, "OlapTransaction.AggregationForSalesByArea");
		@SuppressWarnings("unchecked")
		List<OlapResultByArea.RowMapping> result = query.getResultList();

		return result;
	}


	public static int deleteOlderThan(EntityManager apEm, int days)
	{
		Query query = apEm.createNamedQuery("OlapTransaction.deleteOlderThan");

		// Get start of day ...
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.HOUR, days * -24);

		query.setParameter("refTime", calendar.getTime());
		return query.executeUpdate();
	}

	// Entity manager should refer to OLTP database and not OLAP database for this method only ...
	public static List<OlapTransaction> createFromTransactions(EntityManager em, long minId, Integer limit)
	{
		TypedQuery<OlapTransaction> query = em.createQuery("SELECT NEW "
				+ "hxc.services.ecds.olapmodel.OlapTransaction("
				+ " transact.id, transact.companyID, transact.version, transact.number, "
				+ " transact.type, transact.channel, transact.channelType, transact.startTime, transact.endTime, transact.requestMode, "

				+ " a_tier.type, a_tier.name, a_sc.name, transact.a_CellID, "
				+ " b_tier.type, b_tier.name, b_sc.name, transact.b_CellID, "

				+ " transact.amount, transact.grossSalesAmount, transact.costOfGoodsSold, "
				+ " transact.buyerTradeBonusAmount, transact.buyerTradeBonusProvision, "
				+ " transact.buyerTradeBonusPercentage, transact.chargeLevied, "
				+ " bundle.name, promotion.name, "
				+ " transact.returnCode, transact.rolledBack, transact.followUp, transact.reversedID,"

				+ " transact.a_AgentID, a_agent.title, a_agent.firstName, a_agent.surname, "
				+ " a_agent.accountNumber, transact.a_MSISDN, transact.a_IMSI, transact.a_IMEI, a_group.id, a_group.name, "
				+ " a_owner.mobileNumber, a_owner.imsi, "
				+ " a_owner.title, a_owner.firstName, a_owner.surname, a_owner.id, "
				+ " transact.a_BalanceBefore, transact.a_BalanceAfter, "
				+ " transact.a_BonusBalanceBefore, transact.a_BonusBalanceAfter, "
				+ " transact.a_OnHoldBalanceBefore, transact.a_OnHoldBalanceAfter, "

				+ " transact.b_AgentID, b_agent.title, b_agent.firstName, b_agent.surname, "
				+ " b_agent.accountNumber, transact.b_MSISDN, transact.b_IMSI, transact.b_IMEI, b_group.id, b_group.name, "
				+ " b_owner.mobileNumber, b_owner.imsi, "
				+ " b_owner.title, b_owner.firstName, b_owner.surname, b_owner.id, "
				+ " transact.b_BalanceBefore, transact.b_BalanceAfter, "
				+ " transact.b_BonusBalanceBefore, transact.b_BonusBalanceAfter, "
				+ " transact.b_TransferBonusAmount, transact.b_TransferBonusProfile) "

				+ " FROM hxc.services.ecds.model.Transaction transact"

				+ " LEFT OUTER JOIN transact.bundle bundle"
				+ " LEFT OUTER JOIN transact.promotion promotion"

				+ " LEFT OUTER JOIN transact.a_Tier a_tier"
				+ " LEFT OUTER JOIN transact.a_ServiceClass a_sc"
				+ " LEFT OUTER JOIN transact.b_Tier b_tier"
				+ " LEFT OUTER JOIN transact.b_ServiceClass b_sc"

				+ " LEFT OUTER JOIN transact.a_Agent a_agent"
				+ " LEFT OUTER JOIN transact.a_Group a_group"
				+ " LEFT OUTER JOIN transact.a_Owner a_owner"

				+ " LEFT OUTER JOIN transact.b_Agent b_agent"
				+ " LEFT OUTER JOIN transact.b_Group b_group"
				+ " LEFT OUTER JOIN transact.b_Owner b_owner"

				+ " WHERE transact.id >= :id"
				+ " ORDER BY transact.id ASC", OlapTransaction.class);
		query.setParameter("id", minId);
		if (limit != null && limit > 0)
		{
			query.setMaxResults(limit);
		}
		return query.getResultList();
	}

	public static void createView(EntityManager em, String viewNamePrototype, int companyID) throws Exception
	{
		Query query = em.createNativeQuery(""
				+ "CREATE OR REPLACE VIEW "
				+ String.format(viewNamePrototype, companyID)
				+ " AS SELECT "
				+ "type AS TransactionType, "
				+ "channel AS Channel, "
				+ "channel_type AS ChannelType, "
				+ "started AS StartTime, "
				+ "mode AS RequestMode, "
				+ ""
				+ "a_tier_name AS A_Tier, "
				+ "a_sc_name AS A_ServiceClass, "
				+ "a_cell AS A_CellID, "
				+ ""
				+ "b_tier_name AS B_Tier, "
				+ "b_sc_name AS B_ServiceClass, "
				+ "b_cell AS B_CellID, "
				+ ""
				+ "amount AS Amount, "
				+ "bonus AS BuyerTradeBonus, "
				+ "charge AS ChargeLevied, "

				+ "bundle_name AS BundleName, "
				+ "promotion_name AS PromotionName, "

				+ "success AS Success, "
				+ "rolled_back AS RolledBack, "
				+ "follow_up AS FollowUp, "
				+ "related_id AS RelatedID, "
				+ ""
				+ "a_agent_acc_no AS A_Account_Name, "
				+ "a_msisdn AS A_MSISDN, "
				+ "a_imsi AS A_IMSI, "
				+ "a_imei AS A_IMEI, "
				+ "a_group_name AS A_Group_Name, "
				+ "a_owner_msisdn AS A_Owner_MSISDN, "
				+ "a_before AS A_Balance_Before, "
				+ "a_after AS A_Balance_After, "
				+ ""
				+ "b_agent_acc_no AS B_Account_Name, "
				+ "b_msisdn AS B_MSISDN, "
				+ "b_imsi AS B_IMSI, "
				+ "b_imei AS B_IMEI, "
				+ "b_group_name AS B_Group_Name, "
				+ "b_owner_msisdn AS B_Owner_MSISDN, "
				+ "b_before AS B_Balance_Before, "
				+ "b_after AS B_Balance_After, "
				+ "b_transfer_bonus_amount AS B_Transfer_Bonus_Amount, "
				+ "b_transfer_bonus_profile AS B_Transfer_Bonus_Profile, "
				+ ""
				+ "bonus_prov AS TradeBonusProvision, "
				+ "bonus_pct AS TradeBonusPercentage, "
				+ ""
				+ "a_area_name AS A_Area_Name, "
				+ "b_area_name AS B_Area_Name "
				+ ""
				+ "FROM ap_transact WHERE comp_id = :companyID");
		query.setParameter("companyID", companyID);
		query.executeUpdate();
	}

	public static void loadMRD(EntityManager em, int companyID, Session session) throws Exception
	{
		if (companyID == 2)
			createView(em, "bi_transaction_view", companyID);
		createView(em, "ap_transact_view_%d", companyID);
	}


	public String describe(String extra)
	{
		return String.format("%s@%s("
			+ "id = '%s', companyID = '%s', version = '%s', number = '%s', "
			+ "type = '%s', channel = '%s', channelType = '%s', startTime = '%s', endDate = '%s', endTime = '%s', requestMode = '%s', "
			+ "a_TierType = '%s', a_TierName = '%s', a_ServiceClassName = '%s', a_CellID = '%s', "
			+ "b_TierType = '%s', b_TierName = '%s', b_ServiceClassName = '%s', b_CellID = '%s', "
			+ "amount = '%s', grossSalesAmount = '%s', costOfGoodsSold = '%s', "
			+ "buyerTradeBonusAmount = '%s', buyerTradeBonusProvision = '%s', buyerTradeBonusPercentage = '%s', "
			+ "chargeLevied = '%s', "
			+ "bundleName = '%s', promotionName = '%s', "
			+ "success = '%s', rolledBack = '%s', followUp = '%s', relatedID = '%s', "
			+ "a_AgentID = '%s', a_AgentName = '%s', a_AgentAccountNumber = '%s', a_MSISDN = '%s', a_IMSI = '%s', a_IMEI = '%s', a_GroupName = '%s', "
			+ "a_OwnerMobileNumber = '%s', a_OwnerIMSI = '%s', a_OwnerName = '%s', a_OwnerID = '%s', "
			+ "a_AreaName = '%s', a_BalanceBefore = '%s', a_BalanceAfter = '%s', "
			+ "b_AgentID = '%s', b_AgentName = '%s', b_AgentAccountNumber = '%s', b_MSISDN = '%s', b_IMSI = '%s', b_IMEI = '%s', b_GroupName = '%s', "
			+ "b_OwnerMobileNumber = '%s', b_OwnerIMSI = '%s', b_OwnerName = '%s', b_OwnerID = '%s', "
			+ "b_AreaName = '%s', b_BalanceBefore = '%s', b_BalanceAfter = '%s', "
			+ "b_TransferBonusAmount = '%s', b_TransferBonusProfile = '%s'"
			+ "%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			id, companyID, version, number,
			type, channel, channelType, startTime, endDate, endTime, requestMode,
			a_TierType, a_TierName, a_ServiceClassName, a_CellID,
			b_TierType, b_TierName, b_ServiceClassName, b_CellID,
			amount, grossSalesAmount, costOfGoodsSold,
			buyerTradeBonusAmount, buyerTradeBonusProvision,buyerTradeBonusPercentage, 
			chargeLevied,
			bundleName, promotionName,
			success, rolledBack, followUp, relatedID,
			a_AgentID, a_AgentName, a_AgentAccountNumber, a_MSISDN, a_IMSI, a_IMEI, a_GroupName,
			a_OwnerMobileNumber, a_OwnerIMSI, a_OwnerName, a_OwnerID,
			a_AreaName, a_BalanceBefore, a_BalanceAfter,
			b_AgentID, b_AgentName, b_AgentAccountNumber, b_MSISDN, b_IMSI, b_IMEI, b_GroupName,
			b_OwnerMobileNumber, b_OwnerIMSI, b_OwnerName, b_OwnerID,
			b_AreaName, b_BalanceBefore, b_BalanceAfter,
			b_TransferBonusAmount, b_TransferBonusProfile,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

		// ----------
		// ---- Analytics query for LIVE data
		// ----
	
	public static long analyticsQuery(EntityManagerEx apEm, OlapAnalyticsHistory.QueryData queryData) throws ParseException
	{
		return OlapTransaction.analyticsQuery(apEm, queryData, 0, 1);
	}
	
	public static long analyticsQuery(EntityManagerEx apEm, OlapAnalyticsHistory.QueryData queryData, int daysAgo) throws ParseException
	{
		return OlapTransaction.analyticsQuery(apEm, queryData, daysAgo, 1);
	}
	
	public static long analyticsQuery(EntityManagerEx apEm, OlapAnalyticsHistory.QueryData queryData, int daysAgo, int success) throws ParseException
	{
		String endedDateString				= FormatHelper.longDateDaysAgo( daysAgo );
		SimpleDateFormat sdf				= new SimpleDateFormat("yyyy-MM-dd");
		Date endedDate						= sdf.parse(endedDateString);
		
		CriteriaBuilder cb					= apEm.getCriteriaBuilder();
        CriteriaQuery<Long> criteria		= cb.createQuery(Long.class);
        Root<OlapTransaction> root			= criteria.from(OlapTransaction.class);

        logger.trace("Beginning analyticsQuery...");
        
        if ( OlapAnalyticsHistory.QueryData.UACOUNT.txTypeToString().compareTo(queryData.txTypeToString()) == 0 )
        {
        	logger.trace("Found Unique Agents query request. Where Criteria is, endedDate: {}, success: {}", endedDate, success);
        	criteria.where(
        			cb.equal(root.get("endDate"),endedDate),
        			cb.equal(root.get("success"),success)
        		);
        }
        else // Implies Unique Agents request when no transactionType is specified
        {
        	logger.trace("Found non-UACOUNT query request. Where Criteria is, type: {}, endedDate: {}, success: {}", endedDate, success);

        		// Here we ignore the empty transaction type and we will still get for "today" as if normal circumstances
            criteria.where(
        			cb.equal(root.get("type"),queryData.txTypeToString()),
            		cb.equal(root.get("endDate"),endedDate), 
            		cb.equal(root.get("success"),success)
            	);
        }
        
        if ( OlapAnalyticsHistory.DataType.VALUE.compareTo(queryData.dataType()) == 0 )
        {
        	logger.trace("Found VALUE query request. Select Criteria is: sum({})", queryData.columnNameToString());
        	criteria.select(cb.sumAsLong(root.get(queryData.columnNameToString())));
        }
        else if ( OlapAnalyticsHistory.DataType.COUNT.compareTo(queryData.dataType()) == 0 )
        {
        	logger.trace("Found COUNT query request. Select Criteria is: count(*)");
        	criteria.select(cb.count(root));
        }
        else if ( OlapAnalyticsHistory.DataType.COUNT_DISTINCT.compareTo(queryData.dataType()) == 0 )
        {
        	logger.trace("Found COUNT_DISTINCT query request. Select Criteria is: count(distinct({}))", queryData.columnNameToString());
            criteria.select(cb.countDistinct(root.get(queryData.columnNameToString())));
        }
        else
        {
        	logger.error("NO SELECT CRITERIA FOR THIS REQUEST!!");
			// FIXME:: Better to throw an exception in this case as data has not been retrieved, DB query not even run?
        	return 0L;
        }

        TypedQuery<Long> query	= apEm.createQuery(criteria);
		List<Long> results		= query.getResultList();
        
			//
			// Force "0" as return, we DO NOT want "NULL" returns
			// Problems in graphs on the front-end are more readily understood when we get "ZERO" data and were expecting something else
			// It is better to give "0" than causing null pointer exceptions or unrecognisable data in the Front-End
			//
        return (results.size() == 0 || results.get(0) == null) ? 0L : results.get(0);
	}
	
	public static void loadMRD(EntityManager em, EntityManager emAp, Session session) throws RuleCheckException
	{
		Permission.loadMRD(emAp, ANALYTICS_MAY_VIEW, session);
		Permission.loadMRD(emAp, ANALYTICS_MAY_CONFIGURE, session);
	}
}

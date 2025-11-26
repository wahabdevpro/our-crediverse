package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.rest.batch.AgentProcessor;
import hxc.services.ecds.util.PredicateExtender;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

////////////////////////////////////////////////////////////////////////////////////////
//
// Agent Table - Used for Agent slow changing fields
//
///////////////////////////////////

@Table(name = "ec_transact", uniqueConstraints = { //
		@UniqueConstraint(name = "ec_transact_no", columnNames = { "comp_id", "no" }) }, //
		indexes = { //
				@Index(name = "ix_ec_transact_started", columnList = "comp_id,started"), //
				@Index(name = "ix_ec_transact_rev", columnList = "reversed_id"), //
				@Index(name = "ix_ec_transact_amsisdn", columnList = "comp_id,a_msisdn"), //
				@Index(name = "ix_ec_transact_bmsisdn", columnList = "comp_id,b_msisdn"), //
				@Index(name = "ix_ec_transact_aagent", columnList = "a_agent"), //
				@Index(name = "ix_ec_transact_bagent", columnList = "b_agent"), //
				@Index(name = "ix_ec_transact_aowner", columnList = "a_owner"), //
				@Index(name = "ix_ec_transact_bowner", columnList = "b_owner"), //
				@Index(name = "ix_ec_transact_ended", columnList = "comp_id,ended"), //
		})

@Entity
@NamedQueries({ //
		@NamedQuery(name = "Transaction.findByID", query = "SELECT p FROM Transaction p where id = :id and companyID = :companyID"), //

		@NamedQuery(name = "Transaction.findByNumber", query = "SELECT p FROM Transaction p where number = :number and companyID = :companyID"), //

		@NamedQuery(name = "Transaction.findByReversedID", query = "SELECT p FROM Transaction p where reversedID = :reversedID and companyID = :companyID and returnCode = '"
				+ ResponseHeader.RETURN_CODE_SUCCESS + "'"),

		@NamedQuery(name = "Transaction.findLastForAgent", query = "SELECT p FROM Transaction p where a_AgentID = :agentID and companyID = :companyID and type in ('" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_SELL + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_SELL_BUNDLE + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_DEBIT + "','" +
				hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_REFUND + "','" +
				hxc.ecds.protocol.rest.Transaction.TYPE_SELF_TOPUP + //
				"') order by id desc"), //

		@NamedQuery(name = "Transaction.findLastSuccessfulTransferToAgent", query = "SELECT p FROM Transaction p where b_msisdn = :bMSISDN and companyID = :companyID and type ='" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER + //
				"' and ret_code='SUCCESS' order by id desc"), //

		@NamedQuery(name = "Transaction.findLast", query = "SELECT p FROM Transaction p where companyID = :companyID and type in ('" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_SELL + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_SELL_BUNDLE + "','" + //
				hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_DEBIT + "','" +
				hxc.ecds.protocol.rest.Transaction.TYPE_NON_AIRTIME_REFUND + "','" +
				hxc.ecds.protocol.rest.Transaction.TYPE_SELF_TOPUP + //
				hxc.ecds.protocol.rest.Transaction.TYPE_REVERSE + //
				hxc.ecds.protocol.rest.Transaction.TYPE_REVERSE_PARTIALLY + //
				"') order by id desc"), //

		@NamedQuery(name = "Transaction.findDuplicate", //
		query = "SELECT p FROM Transaction p where a_AgentID = :agentID and companyID = :companyID and " + //
				"type = :type and b_MSISDN = :b_MSISDN and startTime >= :since and returnCode = '" + ResponseHeader.RETURN_CODE_SUCCESS + "' " + //
				"and amount = :amount order by id desc"), //

		@NamedQuery(name = "Transaction.findDuplicateBundle", //
		query = "SELECT p FROM Transaction p where a_AgentID = :agentID and companyID = :companyID and " + //
				"type = :type and b_MSISDN = :b_MSISDN and startTime >= :since and returnCode = '" + ResponseHeader.RETURN_CODE_SUCCESS + "' " + //
				"and bundleID = :bundleID order by id desc"), //

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

		@NamedQuery(name = "Transaction.depositsForAgent", //
				query = "SELECT p.type, count(*) as count, sum(p.amount) as total FROM Transaction p " + //
						"where b_AgentID = :agentID and companyID = :companyID " + //
						"and startTime >= :startTime " + //
						"and returnCode = '" + ResponseHeader.RETURN_CODE_SUCCESS + "' " + //
						"and type = '" + hxc.ecds.protocol.rest.Transaction.TYPE_TRANSFER + "' " +//
						"GROUP BY p.type"), //

		@NamedQuery(name = "Transaction.recentRewards", query = "SELECT p FROM Transaction p where type = '" //
				+ hxc.ecds.protocol.rest.Transaction.TYPE_PROMOTION_REWARD //
				+ "' and b_AgentID = :agentID and startTime >= :startTime and companyID = :companyID "), //

		@NamedQuery(name = "Transaction.cleanout", query = "delete from Transaction p where p.endTime < :before and companyID = :companyID"), //

		@NamedQuery(name = "Transaction.referenceBundle", query = "SELECT p FROM Transaction p where bundleID = :bundleID"), //

		@NamedQuery(name = "Transaction.referenceTier", query = "SELECT p FROM Transaction p where a_TierID = :tierID or b_TierID = :tierID"), //

		@NamedQuery(name = "Transaction.referenceServiceClass", query = "SELECT p FROM Transaction p where a_ServiceClassID = :serviceClassID or b_ServiceClassID = :serviceClassID"), //

		@NamedQuery(name = "Transaction.referencePromotion", query = "SELECT p FROM Transaction p where promotionID = :promotionID"), //

		@NamedQuery(name = "Transaction.referenceArea", query = "SELECT p FROM Transaction p where a_AreaID = :areaID or b_AreaID = :areaID"), //

		@NamedQuery(name = "Transaction.referenceWebUser", query = "SELECT p FROM Transaction p where requesterMSISDN = :msisdn"), //

		@NamedQuery(name = "Transaction.referenceCell", query = "SELECT p FROM Transaction p where a_CellID = :cellID or b_CellID = :cellID"), //

		@NamedQuery(name = "Transaction.referenceGroup", query = "SELECT p FROM Transaction p where a_GroupID = :groupID or b_GroupID = :groupID"), //

		@NamedQuery(name = "Transaction.referenceTransferRule", query = "SELECT p FROM Transaction p where transferRuleID = :transferRuleID"), //

		@NamedQuery(name = "Transaction.referenceAgentUser", query = "SELECT p FROM Transaction p where requesterMSISDN = :msisdn"), //

		@NamedQuery(name = "Transaction.referenceAgent", query = "SELECT p FROM Transaction p where a_AgentID = :agentID or b_AgentID = :agentID or a_OwnerAgentID = :agentID or b_OwnerAgentID = :agentID"), //

})
public class Transaction extends hxc.ecds.protocol.rest.Transaction //
		implements Serializable, ICompanyData<Transaction>
{

	final static Logger logger = LoggerFactory.getLogger(AgentProcessor.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -7409216206638810002L;

	@Transient
	public static final Permission MAY_REPLENISH = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_REPLENISH, "May Replenish");
	@Transient
	public static final Permission MAY_AUTHORISE_REPLENISH = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REPLENISH, "May Authorise Replenish");
	@Transient
	public static final Permission MAY_TRANSFER = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_TRANSFER, "May Transfer");
	@Transient
	public static final Permission MAY_SELL = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_SELL, "May Sell");
	@Transient
	public static final Permission MAY_ADJUST = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_ADJUST, "May Adjust");
	@Transient
	public static final Permission MAY_AUTHORISE_ADJUST = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUST, "May Authorise Adjust");
	@Transient
	public static final Permission MAY_REVERSE = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_REVERSE, "May Reverse");
	@Transient
	public static final Permission MAY_AUTHORISE_REVERSE = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_REVERSE, "May Authorise Reverse");
	@Transient
	public static final Permission MAY_ADJUDICATE = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_ADJUDICATE, "May Adjudicate");
	@Transient
	public static final Permission MAY_AUTHORISE_ADJUDICATE = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_ADJUDICATE, "May Authorise Adjudication");

	@Transient
	public static final Permission MAY_REGISTER_PIN = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_REGISTER_PIN, "May Register PIN");
	@Transient
	public static final Permission MAY_CHANGE_PIN = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_CHANGE_PIN, "May Change PIN");
	@Transient
	public static final Permission MAY_SELF_TOPUP = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_SELF_TOPUP, "May Perform Self Topup");
	@Transient
	public static final Permission MAY_SELL_BUNDLES = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_SELL_BUNDLES, "May Sell Bundles");

	@Transient
	public static final Permission MAY_QUERY_BALANCE = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_QUERY_BALANCE, "May Query Balance");
	@Transient
	public static final Permission MAY_QUERY_DEPOSITS = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_QUERY_DEPOSITS, "May Query Deposits");
	@Transient
	public static final Permission MAY_QUERY_LAST = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_QUERY_LAST, "May Query Last Transaction");
	@Transient
	public static final Permission MAY_QUERY_SALES = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_QUERY_SALES, "May Query Sales");
	@Transient
	public static final Permission MAY_QUERY_STATUS = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_QUERY_STATUS, "May Query Transaction Status");

	@Transient
	public static final Permission MAY_TRANSFER_ROOT = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_TRANSFER_FROM_ROOT_ACCOUNT, "May Transfer from Root Account");
	@Transient
	public static final Permission MAY_AUTHORISE_TRANSFER_ROOT = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_AUTHORISE_TRANSFER_FROM_ROOT_ACCOUNT,
			"May Authorise Transfer from Root Account");

	@Transient
	public static final Permission MAY_VIEW = new Permission(false, true, Permission.GROUP_TRANSACTIONS, Permission.PERM_VIEW, "May View Transactions");
	@Transient
	public static final Permission MAY_VIEW_CONFIGURATION = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_VIEW_CONFIGURATIONS, "May View Configurations");

	@Transient
	public static final Permission MAY_CONFIG_TRANSACTION_STATUS_ENQUIRIES = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_TRANSACTION_STATUS_ENQUIRIES,
			"Configure Transaction Status Enquiries");
	@Transient
	public static final Permission MAY_CONFIG_ADJUSTMENTS = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_ADJUSTMENTS, "May Configure Adjustments");
	@Transient
	public static final Permission MAY_CONFIG_BATCH = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_BATCH, "May Configure Batch Processing");
	@Transient
	public static final Permission MAY_CONFIG_REVERSALS = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_REVERSALS, "May Configure Reversals");
	@Transient
	public static final Permission MAY_CONFIG_WUI = new Permission(false, false, Permission.GROUP_WEB_UI, Permission.PERM_CONFIGURE_WEB_UI, "May Configure Web UI");
	@Transient
	public static final Permission MAY_CONFIG_TRANSACTIONS = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_TRANSACTIONS, "May Configure Transactions");
	@Transient
	public static final Permission MAY_CONFIG_BALANCE_ENQUIRIES = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_BALANCE_ENQUIRIES,
			"May Configure Balance Enquiries");
	@Transient
	public static final Permission MAY_CONFIG_REPLENISHMENT = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_REPLENISHMENT, "May Configure Replenishment");
	@Transient
	public static final Permission MAY_CONFIG_TRANSFERS = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_TRANSFERS, "May Configure Transfers");
	@Transient
	public static final Permission MAY_CONFIG_SALES = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_SALES, "May Configure Sales");
	@Transient
	public static final Permission MAY_CONFIG_REGISTER_PIN = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_PIN_REGISTRATION, "May Configure Pin Registration");
	@Transient
	public static final Permission MAY_CONFIG_CHANGE_PIN = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_PIN_CHANGE, "May Configure Pin Change");
	@Transient
	public static final Permission MAY_CONFIG_SALES_QUERY = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_SALES_QUERY, "May Configure Sale Queries");
	@Transient
	public static final Permission MAY_CONFIG_DEPOSITS_QUERY = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_DEPOSITS_QUERY, "May Configure Deposit Queries");
	@Transient
	public static final Permission MAY_CONFIG_TRANSACTION_ENQUIRY = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_TRANSACTION_ENQUIRY,
			"May Configure Transaction Enquiries");
	@Transient
	public static final Permission MAY_CONFIG_SELF_TOPUP = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_SELF_TOPUP, "May Configure Self-Topups");
	@Transient
	public static final Permission MAY_CONFIG_BUNDLE_SALES = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_BUNDLE_SALES, "May Configure Bundle Sales");
	@Transient
	public static final Permission MAY_CONFIG_USSD = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_USSD, "May Configure USSD");
	@Transient
	public static final Permission MAY_CONFIG_REWARDS = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_REWARDS, "May Configure Rewards");
	@Transient
	public static final Permission MAY_CONFIG_REPORTING = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_REPORTING, "May Configure Reporting");
	@Transient
	public static final Permission MAY_CONFIG_ADJUDICATION = new Permission(false, false, Permission.GROUP_TRANSACTIONS, Permission.PERM_CONFIGURE_ADJUDICATION, "May Configure Adjudication");

	// ////////////////////////////////////////////////////////////////////////////////////// Transaction Enquiries
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected TransferRule transferRule;
	@JsonIgnore
	protected Agent a_Agent;
	@JsonIgnore
	protected Agent b_Agent;
	@JsonIgnore
	private Tier a_Tier;
	@JsonIgnore
	private Tier b_Tier;
	@JsonIgnore
	private Group a_Group;
	@JsonIgnore
	private Agent a_Owner;
	@JsonIgnore
	private Group b_Group;
	@JsonIgnore
	private Agent b_Owner;
	@JsonIgnore
	private Area a_Area;
	@JsonIgnore
	private Area b_Area;
	@JsonIgnore
	private Cell a_Cell;
	@JsonIgnore
	private Cell b_Cell;
	@JsonIgnore
	private ServiceClass a_ServiceClass;
	@JsonIgnore
	private ServiceClass b_ServiceClass;
	@JsonIgnore
	private Bundle bundle;
	@JsonIgnore
	private Promotion promotion;
	@JsonIgnore
	private QualifyingTransaction qualifyingTransaction;
	@JsonIgnore
	private Transaction originalTransaction;

	@JsonIgnore
	private Set<TransactionExtraData> transactionExtraDataSet;

	@JsonIgnore
	private Map<String, Object> extraDataForKeyType;

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	///////////////////////////////////

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId()
	{
		return id;
	}

	@Override
	public Transaction setId(long id)
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
	public Transaction setCompanyID(int companyID)
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
	public Transaction setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public Transaction setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "no", nullable = false, length = TRANSACTION_NUMBER_MAX_LENGTH)
	public String getNumber()
	{
		return this.number;
	}

	@Override
	public Transaction setNumber(String number)
	{
		this.number = number;
		return this;
	}

	@Override
	@Column(name = "amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getAmount()
	{
		return amount;
	}

	@Override
	public Transaction setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	@Override
	@Column(name = "gross_sales_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getGrossSalesAmount()
	{
		return grossSalesAmount;
	}

	@Override
	public Transaction setGrossSalesAmount(BigDecimal grossSalesAmount)
	{
		this.grossSalesAmount = grossSalesAmount;
		return this;
	}

	@Override
	@Column(name = "cost_of_goods_sold", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getCostOfGoodsSold()
	{
		return costOfGoodsSold;
	}

	@Override
	public Transaction setCostOfGoodsSold(BigDecimal costOfGoodsSold)
	{
		this.costOfGoodsSold = costOfGoodsSold;
		return this;
	}

	@Override
	@Column(name = "bonus", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusAmount()
	{
		return buyerTradeBonusAmount;
	}

	@Override
	public Transaction setBuyerTradeBonusAmount(BigDecimal buyerTradeBonusAmount)
	{
		this.buyerTradeBonusAmount = buyerTradeBonusAmount;
		return this;
	}


	@Override
	@Column(name = "bonus_prov", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusProvision()
	{
		return buyerTradeBonusProvision;
	}

	@Override
	public Transaction setBuyerTradeBonusProvision(BigDecimal buyerTradeBonusProvision)
	{
		this.buyerTradeBonusProvision = buyerTradeBonusProvision ;
		return this;
	}

	@Override
	@Column(name = "charge", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getChargeLevied()
	{
		return chargeLevied;
	}

	@Override
	public Transaction setChargeLevied(BigDecimal chargeLevied)
	{
		this.chargeLevied = chargeLevied;
		return this;
	}

	@Override
	@Column(name = "bonus_pct", nullable = true, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getBuyerTradeBonusPercentage()
	{
		return buyerTradeBonusPercentage;
	}

	@Override
	public Transaction setBuyerTradeBonusPercentage(BigDecimal buyerTradeBonusPercentage)
	{
		this.buyerTradeBonusPercentage = buyerTradeBonusPercentage;
		return this;
	}

	@Override
	@Column(name = "host", nullable = false, length = MACHINE_NAME_MAX_LENGTH)
	public String getHostname()
	{
		return hostname;
	}

	@Override
	public Transaction setHostname(String hostname)
	{
		this.hostname = hostname;
		return this;
	}

	@Override
	@Column(name = "started", nullable = false)
	public Date getStartTime()
	{
		return startTime;
	}

	@Override
	public Transaction setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	@Override
	@Column(name = "ended", nullable = false)
	public Date getEndTime()
	{
		return endTime;
	}

	@Override
	public Transaction setEndTime(Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	@Override
	@Column(name = "channel", nullable = false, length = CHANNEL_MAX_LENGTH)
	public String getChannel()
	{
		return channel;
	}

	@Override
	public Transaction setChannel(String channel)
	{
		this.channel = channel;
		return this;
	}

	@Override
	@Column(name = "channel_type", nullable = true, length = 2)
	public String getChannelType()
	{
		return channelType;
	}

	@Override
	public Transaction setChannelType(String channelType)
	{
		this.channelType = mapChannelType(channelType);
		return this;
	}

	@Override
	@Column(name = "caller", nullable = false, length = MSISDN_MAX_LENGTH)
	public String getCallerID()
	{
		return callerID;
	}

	@Override
	public Transaction setCallerID(String callerID)
	{
		this.callerID = callerID;
		return this;
	}

	@Override
	@Column(name = "in_transact", nullable = true, length = INBOUND_TRANSACTION_MAX_LENGTH)
	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	@Override
	public Transaction setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
		return this;
	}

	@Override
	@Column(name = "in_session", nullable = true, length = INBOUND_SESSION_MAX_LENGTH)
	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	@Override
	public Transaction setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
		return this;
	}

	@Override
	@Column(name = "mode", nullable = false, length = MODE_MAX_LENGTH)
	public String getRequestMode()
	{
		return requestMode;
	}

	@Override
	public Transaction setRequestMode(String requestMode)
	{
		this.requestMode = requestMode;
		return this;
	}

	@Override
	@Column(name = "rule_id", nullable = true)
	public Integer getTransferRuleID()
	{
		return transferRuleID;
	}

	@Override
	public Transaction setTransferRuleID(Integer transferRuleID)
	{
		this.transferRuleID = transferRuleID;
		return this;
	}

	@Override
	@Column(name = "a_agent", nullable = true)
	public Integer getA_AgentID()
	{
		return a_AgentID;
	}

	@Override
	public Transaction setA_AgentID(Integer a_AgentID)
	{
		this.a_AgentID = a_AgentID;
		return this;
	}

	@Override
	@Column(name = "a_msisdn", nullable = true, length = MSISDN_MAX_LENGTH)
	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	@Override
	public Transaction setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
		return this;
	}

	@Override
	@Column(name = "a_tier", nullable = true)
	public Integer getA_TierID()
	{
		return a_TierID;
	}

	@Override
	public Transaction setA_TierID(Integer a_TierID)
	{
		this.a_TierID = a_TierID;
		return this;
	}

	@Override
	@Column(name = "req_msisdn", nullable = false, length = WebUser.MOBILE_NUMBER_MAX_LENGTH)
	public String getRequesterMSISDN()
	{
		return requesterMSISDN;
	}

	@Override
	public Transaction setRequesterMSISDN(String requesterMSISDN)
	{
		this.requesterMSISDN = requesterMSISDN;
		return this;
	}

	@Override
	@Column(name = "req_type", nullable = false, length = 1)
	public String getRequesterType()
	{
		return requesterType;
	}

	@Override
	public Transaction setRequesterType(String requesterType)
	{
		this.requesterType = requesterType;
		return this;
	}

	@Override
	@Column(name = "bundle_id", nullable = true)
	public Integer getBundleID()
	{
		return bundleID;
	}

	@Override
	public Transaction setBundleID(Integer bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	@Override
	@Column(name = "prom_id", nullable = true)
	public Integer getPromotionID()
	{
		return promotionID;
	}

	@Override
	public Transaction setPromotionID(Integer promotionID)
	{
		this.promotionID = promotionID;
		return this;
	}

	@Override
	@Column(name = "a_sc", nullable = true)
	public Integer getA_ServiceClassID()
	{
		return a_ServiceClassID;
	}

	@Override
	public Transaction setA_ServiceClassID(Integer a_ServiceClassID)
	{
		this.a_ServiceClassID = a_ServiceClassID;
		return this;
	}

	@Override
	@Column(name = "a_group", nullable = true)
	public Integer getA_GroupID()
	{
		return a_GroupID;
	}

	@Override
	public Transaction setA_GroupID(Integer a_GroupID)
	{
		this.a_GroupID = a_GroupID;
		return this;
	}

	@Override
	@Column(name = "a_owner", nullable = true)
	public Integer getA_OwnerAgentID()
	{
		return a_OwnerAgentID;
	}

	@Override
	public Transaction setA_OwnerAgentID(Integer a_OwnerAgentID)
	{
		this.a_OwnerAgentID = a_OwnerAgentID;
		return this;
	}

	@Override
	@Column(name = "a_area", nullable = true)
	public Integer getA_AreaID()
	{
		return a_AreaID;
	}

	@Override
	public Transaction setA_AreaID(Integer a_AreaID)
	{
		this.a_AreaID = a_AreaID;
		return this;
	}

	@Override
	@Column(name = "a_imsi", nullable = true, length = IMSI_MAX_LENGTH)
	public String getA_IMSI()
	{
		return a_IMSI;
	}

	@Override
	public Transaction setA_IMSI(String a_IMSI)
	{
		this.a_IMSI = a_IMSI;
		return this;
	}

	@Override
	@Column(name = "a_imei", nullable = true, length = IMEI_MAX_LENGTH)
	public String getA_IMEI()
	{
		return a_IMEI;
	}

	@Override
	public Transaction setA_IMEI(String a_IMEI)
	{
		this.a_IMEI = a_IMEI;
		return this;
	}

	@Override
	@Column(name = "a_cell", nullable = true)
	public Integer getA_CellID()
	{
		return a_CellID;
	}

	@Override
	public Transaction setA_CellID(Integer a_CellID)
	{
		this.a_CellID = a_CellID;
		return this;
	}

	@Override
	@Column(name = "a_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BalanceBefore()
	{
		return a_BalanceBefore;
	}

	@Override
	public Transaction setA_BalanceBefore(BigDecimal a_BalanceBefore)
	{
		this.a_BalanceBefore = a_BalanceBefore;
		return this;
	}

	@Override
	@Column(name = "a_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BalanceAfter()
	{
		return a_BalanceAfter;
	}

	@Override
	public Transaction setA_BalanceAfter(BigDecimal a_BalanceAfter)
	{
		this.a_BalanceAfter = a_BalanceAfter;
		return this;
	}

	@Override
	@Column(name = "a_bonus_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BonusBalanceBefore()
	{
		return a_BonusBalanceBefore;
	}

	@Override
	public Transaction setA_BonusBalanceBefore(BigDecimal a_BonusBalanceBefore)
	{
		this.a_BonusBalanceBefore = a_BonusBalanceBefore;
		return this;
	}

	@Override
	@Column(name = "a_bonus_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_BonusBalanceAfter()
	{
		return a_BonusBalanceAfter;
	}

	@Override
	public Transaction setA_BonusBalanceAfter(BigDecimal a_BonusBalanceAfter)
	{
		this.a_BonusBalanceAfter = a_BonusBalanceAfter;
		return this;
	}

	@Override
	@Column(name = "a_hold_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_OnHoldBalanceBefore()
	{
		return a_OnHoldBalanceBefore;
	}

	@Override
	public Transaction setA_OnHoldBalanceBefore(BigDecimal a_OnHoldBalanceBefore)
	{
		this.a_OnHoldBalanceBefore = a_OnHoldBalanceBefore;
		return this;
	}

	@Override
	@Column(name = "a_hold_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getA_OnHoldBalanceAfter()
	{
		return a_OnHoldBalanceAfter;
	}

	@Override
	public Transaction setA_OnHoldBalanceAfter(BigDecimal a_OnHoldBalanceAfter)
	{
		this.a_OnHoldBalanceAfter = a_OnHoldBalanceAfter;
		return this;
	}

	@Override
	@Column(name = "b_agent", nullable = true)
	public Integer getB_AgentID()
	{
		return b_AgentID;
	}

	@Override
	public Transaction setB_AgentID(Integer b_AgentID)
	{
		this.b_AgentID = b_AgentID;
		return this;
	}

	@Override
	@Column(name = "b_msisdn", nullable = true, length = MSISDN_MAX_LENGTH)
	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	@Override
	public Transaction setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
		return this;
	}

	@Override
	@Column(name = "b_tier", nullable = true)
	public Integer getB_TierID()
	{
		return b_TierID;
	}

	@Override
	public Transaction setB_TierID(Integer b_TierID)
	{
		this.b_TierID = b_TierID;
		return this;
	}
	
	@Override
	@Column(name = "b_transfer_bonus_amount", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_TransferBonusAmount()
	{
		return b_TransferBonusAmount;
	}

	@Override
	public Transaction setB_TransferBonusAmount(BigDecimal b_TransferBonusAmount)
	{
		this.b_TransferBonusAmount = b_TransferBonusAmount;
		return this;
	}

	@Override
	@Column(name = "b_transfer_bonus_profile", nullable = true, length = BONUS_PROFILE_MAX_LENGTH)
	public String getB_TransferBonusProfile()
	{
		return b_TransferBonusProfile;
	}

	@Override
	public Transaction setB_TransferBonusProfile(String b_TransferBonusProfile)
	{
		this.b_TransferBonusProfile = b_TransferBonusProfile;
		return this;
	}

	@Override
	@Column(name = "b_sc", nullable = true)
	public Integer getB_ServiceClassID()
	{
		return b_ServiceClassID;
	}

	@Override
	public Transaction setB_ServiceClassID(Integer b_ServiceClassID)
	{
		this.b_ServiceClassID = b_ServiceClassID;
		return this;
	}

	@Override
	@Column(name = "b_group", nullable = true)
	public Integer getB_GroupID()
	{
		return b_GroupID;
	}

	@Override
	public Transaction setB_GroupID(Integer b_GroupID)
	{
		this.b_GroupID = b_GroupID;
		return this;
	}

	@Override
	@Column(name = "b_owner", nullable = true)
	public Integer getB_OwnerAgentID()
	{
		return b_OwnerAgentID;
	}

	@Override
	public Transaction setB_OwnerAgentID(Integer b_OwnerAgentID)
	{
		this.b_OwnerAgentID = b_OwnerAgentID;
		return this;
	}

	@Override
	@Column(name = "b_area", nullable = true)
	public Integer getB_AreaID()
	{
		return b_AreaID;
	}

	@Override
	public Transaction setB_AreaID(Integer b_AreaID)
	{
		this.b_AreaID = b_AreaID;
		return this;
	}

	@Override
	@Column(name = "b_imsi", nullable = true, length = IMSI_MAX_LENGTH)
	public String getB_IMSI()
	{
		return b_IMSI;
	}

	@Override
	public Transaction setB_IMSI(String b_IMSI)
	{
		this.b_IMSI = b_IMSI;
		return this;
	}

	@Override
	@Column(name = "b_imei", nullable = true, length = IMEI_MAX_LENGTH)
	public String getB_IMEI()
	{
		return b_IMEI;
	}

	@Override
	public Transaction setB_IMEI(String b_IMEI)
	{
		this.b_IMEI = b_IMEI;
		return this;
	}

	@Override
	@Column(name = "b_cell", nullable = true)
	public Integer getB_CellID()
	{
		return b_CellID;
	}

	@Override
	public Transaction setB_CellID(Integer b_CellID)
	{
		this.b_CellID = b_CellID;
		return this;
	}
	
	@Override
	@Column(name = "a_cell_group_id", nullable = true)
	public Integer getA_CellGroupID()
	{
		return a_CellGroupID;
	}

	@Override
	public Transaction setA_CellGroupID(Integer a_CellGroupID)
	{
		this.a_CellGroupID = a_CellGroupID;
		return this;
	}

	@Override
	@Column(name = "b_cell_group_id", nullable = true)
	public Integer getB_CellGroupID()
	{
		return b_CellGroupID;
	}

	@Override
	public Transaction setB_CellGroupID(Integer b_CellGroupID)
	{
		this.b_CellGroupID = b_CellGroupID;
		return this;
	}

	@Override
	@Column(name = "b_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BalanceBefore()
	{
		return b_BalanceBefore;
	}

	@Override
	public Transaction setB_BalanceBefore(BigDecimal b_BalanceBefore)
	{
		this.b_BalanceBefore = b_BalanceBefore;
		return this;
	}

	@Override
	@Column(name = "b_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BalanceAfter()
	{
		return b_BalanceAfter;
	}

	@Override
	public Transaction setB_BalanceAfter(BigDecimal b_BalanceAfter)
	{
		this.b_BalanceAfter = b_BalanceAfter;
		return this;
	}

	@Override
	@Column(name = "b_bonus_before", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BonusBalanceBefore()
	{
		return b_BonusBalanceBefore;
	}

	@Override
	public Transaction setB_BonusBalanceBefore(BigDecimal b_BonusBalanceBefore)
	{
		this.b_BonusBalanceBefore = b_BonusBalanceBefore;
		return this;
	}

	@Override
	@Column(name = "b_bonus_after", nullable = true, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getB_BonusBalanceAfter()
	{
		return b_BonusBalanceAfter;
	}

	@Override
	public Transaction setB_BonusBalanceAfter(BigDecimal b_BonusBalanceAfter)
	{
		this.b_BonusBalanceAfter = b_BonusBalanceAfter;
		return this;
	}
	
	@Override
	@Column(name = "ret_code", nullable = false, length = RETURN_CODE_MAX_LENGTH)
	public String getReturnCode()
	{
		return returnCode;
	}

	@Override
	public Transaction setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
		return this;
	}

	@Override
	@Column(name = "ext_code", nullable = true, length = EXTERNAL_CODE_MAX_LENGTH)
	public String getLastExternalResultCode()
	{
		return lastExternalResultCode;
	}

	@Override
	public Transaction setLastExternalResultCode(String lastExternalResultCode)
	{
		this.lastExternalResultCode = lastExternalResultCode;
		return this;
	}

	@Override
	@Column(name = "rolled_back", nullable = false)
	public boolean isRolledBack()
	{
		return rolledBack;
	}

	@Override
	public Transaction setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
		return this;
	}

	@Override
	@Column(name = "follow_up", nullable = false)
	public boolean isFollowUp()
	{
		return followUp;
	}

	@Override
	public Transaction setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
		return this;
	}

	@Override
	@Column(name = "additional", nullable = true, length = ADDITIONAL_INFORMATION_MAX_LENGTH)
	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	@Override
	public Transaction setAdditionalInformation(String additionalInformation)
	{
		if (additionalInformation != null && additionalInformation.length() > ADDITIONAL_INFORMATION_MAX_LENGTH)
			additionalInformation = additionalInformation.substring(0, ADDITIONAL_INFORMATION_MAX_LENGTH - 1);
		this.additionalInformation = additionalInformation;
		return this;
	}

	@Override
	@Column(name = "reversed_id", nullable = true/*, columnDefinition = "bigint(20)"*/)
	public Long getReversedID()
	{
		return reversedID;
	}

	@Override
	public Transaction setReversedID(Long reversedID)
	{
		this.reversedID = reversedID;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Transaction setLastUserID(int lastUserID)
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
	public Transaction setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Properties
	//
	// /////////////////////////////////

	@Transient
	public TransferRule getTransferRule()
	{
		return transferRule;
	}

	public Transaction setTransferRule(TransferRule transferRule)
	{
		this.transferRule = transferRule;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_agent", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Agent getA_Agent()
	{
		return a_Agent;
	}

	public Transaction setA_Agent(Agent a_Agent)
	{
		this.a_Agent = a_Agent;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_agent", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Agent getB_Agent()
	{
		return b_Agent;
	}

	public Transaction setB_Agent(Agent b_Agent)
	{
		this.b_Agent = b_Agent;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_tier", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Tier getA_Tier()
	{
		return this.a_Tier;
	}

	public Transaction setA_Tier(Tier a_Tier)
	{
		this.a_Tier = a_Tier;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "reversed_id", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Transaction getOriginalTransaction()
	{
		return originalTransaction;
	}

	public void setOriginalTransaction(Transaction originalTransaction)
	{
		this.originalTransaction = originalTransaction;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "bundle_id", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Bundle getBundle()
	{
		return bundle;
	}

	public Transaction setBundle(Bundle bundle)
	{
		this.bundle = bundle;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "prom_id", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Promotion getPromotion()
	{
		return promotion;
	}

	public Transaction setPromotion(Promotion promotion)
	{
		this.promotion = promotion;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY) // Actually @OneToOne
	@JoinColumn(name = "id", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public QualifyingTransaction getQualifyingTransaction()
	{
		return qualifyingTransaction;
	}

	public Transaction setQualifyingTransaction(QualifyingTransaction qualifyingTransaction)
	{
		this.qualifyingTransaction = qualifyingTransaction;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_tier", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Tier getB_Tier()
	{
		return this.b_Tier;
	}

	public Transaction setB_Tier(Tier b_Tier)
	{
		this.b_Tier = b_Tier;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_group", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Group getA_Group()
	{
		return this.a_Group;
	}

	public Transaction setA_Group(Group a_Group)
	{
		this.a_Group = a_Group;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_owner", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Agent getA_Owner()
	{
		return this.a_Owner;
	}

	public Transaction setA_Owner(Agent a_Owner)
	{
		this.a_Owner = a_Owner;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_area", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Area getA_Area()
	{
		return this.a_Area;
	}

	public Transaction setA_Area(Area a_Area)
	{
		this.a_Area = a_Area;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_cell", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Cell getA_Cell()
	{
		return a_Cell;
	}

	public Transaction setA_Cell(Cell a_Cell)
	{
		this.a_Cell = a_Cell;
		return this;
	}
	
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_cell", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Cell getB_Cell()
	{
		return b_Cell;
	}

	public Transaction setB_Cell(Cell b_Cell)
	{
		this.b_Cell = b_Cell;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_group", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Group getB_Group()
	{
		return this.b_Group;
	}

	public Transaction setB_Group(Group b_Group)
	{
		this.b_Group = b_Group;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_owner", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Agent getB_Owner()
	{
		return this.b_Owner;
	}

	public Transaction setB_Owner(Agent b_Owner)
	{
		this.b_Owner = b_Owner;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_area", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public Area getB_Area()
	{
		return this.b_Area;
	}

	public Transaction setB_Area(Area b_Area)
	{
		this.b_Area = b_Area;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "a_sc", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public ServiceClass getA_ServiceClass()
	{
		return this.a_ServiceClass;
	}

	public Transaction setA_ServiceClass(ServiceClass a_ServiceClass)
	{
		this.a_ServiceClass = a_ServiceClass;
		return this;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "b_sc", nullable = true, insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	public ServiceClass getB_ServiceClass()
	{
		return this.b_ServiceClass;
	}

	public Transaction setB_ServiceClass(ServiceClass b_ServiceClass)
	{
		this.b_ServiceClass = b_ServiceClass;
		return this;
	}

	// this properties are sent back for transaction/ex requests (see Replenish.java)

	@Transient
	@JsonIgnore
	public String getA_CellGroupCode()
	{
		return a_Cell == null || a_Cell.getCellGroups().size() < 1 ? null : a_Cell.getCellGroups().get(0).getCode();
	}

	@Transient
	@JsonIgnore
	public String getB_CellGroupCode()
	{
		return b_Cell == null || b_Cell.getCellGroups().size() < 1 ? null : b_Cell.getCellGroups().get(0).getCode();
	}

	@Transient
	@JsonIgnore
	public Integer getA_HlrCellID()
	{
		return a_CellID == null || a_Cell == null ? null : a_Cell.getCellID();
	}

	@Transient
	@JsonIgnore
	public Integer getB_HlrCellID()
	{
		return b_CellID == null || b_Cell == null ? null : b_Cell.getCellID();
	}


	public void addExtraDataForKeyType(TransactionExtraData.Key transactionExtraDataKey, Object value) {
		if(extraDataForKeyType == null){
			extraDataForKeyType = new HashMap<>();
		}

		extraDataForKeyType.put(transactionExtraDataKey.name(), value);
	}




	@Transient
	@JsonIgnore
	public Set<TransactionExtraData> getTransactionExtraDataSet() {
		return transactionExtraDataSet;
	}


	public void addTransactionExtraData(TransactionExtraData transactionExtraData) {

		if(transactionExtraDataSet == null){
			this.transactionExtraDataSet = new HashSet<>();
		}

		this.transactionExtraDataSet.add(transactionExtraData);
	}


	private TransactionExtraData toTransactionExtraData(TransactionExtraData.Key key, Object value) throws JsonProcessingException {
		TransactionExtraData.TransactionExtraDataId transactionExtraDataId = new TransactionExtraData.TransactionExtraDataId();
		transactionExtraDataId.setTransactionId(id);
		transactionExtraDataId.setKey(key.name());
		String json = TransactionExtraData.toJson(value);
		logger.info("Transaction extra data json: {}", json);
		return new TransactionExtraData(transactionExtraDataId, json);
	}



	// end

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Minimum Required Data
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_REPLENISH, session);
		Permission.loadMRD(em, MAY_AUTHORISE_REPLENISH, session);
		Permission.loadMRD(em, MAY_TRANSFER, session);
		Permission.loadMRD(em, MAY_SELL, session);
		Permission.loadMRD(em, MAY_ADJUST, session);
		Permission.loadMRD(em, MAY_AUTHORISE_ADJUST, session);
		Permission.loadMRD(em, MAY_REVERSE, session);
		Permission.loadMRD(em, MAY_AUTHORISE_REVERSE, session);
		Permission.loadMRD(em, MAY_ADJUDICATE, session);
		Permission.loadMRD(em, MAY_AUTHORISE_ADJUDICATE, session);

		Permission.loadMRD(em, MAY_REGISTER_PIN, session);
		Permission.loadMRD(em, MAY_CHANGE_PIN, session);
		Permission.loadMRD(em, MAY_SELF_TOPUP, session);
		Permission.loadMRD(em, MAY_SELL_BUNDLES, session);
		Permission.loadMRD(em, MAY_QUERY_BALANCE, session);
		Permission.loadMRD(em, MAY_QUERY_DEPOSITS, session);
		Permission.loadMRD(em, MAY_QUERY_LAST, session);
		Permission.loadMRD(em, MAY_QUERY_SALES, session);
		Permission.loadMRD(em, MAY_QUERY_STATUS, session);

		Permission.loadMRD(em, MAY_CONFIG_TRANSACTIONS, session);
		Permission.loadMRD(em, MAY_CONFIG_BALANCE_ENQUIRIES, session);
		Permission.loadMRD(em, MAY_CONFIG_REPLENISHMENT, session);
		Permission.loadMRD(em, MAY_CONFIG_TRANSFERS, session);
		Permission.loadMRD(em, MAY_CONFIG_SALES, session);
		Permission.loadMRD(em, MAY_CONFIG_REGISTER_PIN, session);
		Permission.loadMRD(em, MAY_CONFIG_CHANGE_PIN, session);
		Permission.loadMRD(em, MAY_TRANSFER_ROOT, session);
		Permission.loadMRD(em, MAY_AUTHORISE_TRANSFER_ROOT, session);
		Permission.loadMRD(em, MAY_CONFIG_TRANSACTION_STATUS_ENQUIRIES, session);
		Permission.loadMRD(em, MAY_CONFIG_ADJUSTMENTS, session);
		Permission.loadMRD(em, MAY_CONFIG_BATCH, session);
		Permission.loadMRD(em, MAY_CONFIG_REVERSALS, session);
		Permission.loadMRD(em, MAY_CONFIG_WUI, session);
		Permission.loadMRD(em, MAY_CONFIG_ADJUDICATION, session);

		Permission.loadMRD(em, MAY_VIEW, session);
		Permission.loadMRD(em, MAY_VIEW_CONFIGURATION, session);

		Permission.loadMRD(em, MAY_CONFIG_SALES_QUERY, session);
		Permission.loadMRD(em, MAY_CONFIG_DEPOSITS_QUERY, session);
		Permission.loadMRD(em, MAY_CONFIG_TRANSACTION_ENQUIRY, session);
		Permission.loadMRD(em, MAY_CONFIG_SELF_TOPUP, session);
		Permission.loadMRD(em, MAY_CONFIG_BUNDLE_SALES, session);
		Permission.loadMRD(em, MAY_CONFIG_USSD, session);
		Permission.loadMRD(em, MAY_CONFIG_REPORTING, session);
		Permission.loadMRD(em, MAY_CONFIG_REWARDS, session);

		TransactionNumber.loadMRD(em, companyID, session);

	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// ICompanyData
	//
	///////////////////////////////////

	@Override
	public void persist(EntityManager em, Transaction oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		if (lastTime == null) {
			lastTime = new Date();
		}
		validate(null);
		RuleCheck.isNull("oldValue", oldValue);
		em.persist(this);

		//now check if we have extra data and persist that too
		persistExtraData(em);
	}

	public void persistTransactionLocation(EntityManager em, Double longitude, Double latitude, long transactionID) {
		try {
			TransactionLocation transactionLocation = new TransactionLocation(transactionID, latitude, longitude);
			em.persist(transactionLocation);
		} catch (Exception e) {
			// Do not throw the exception, we still want to save the transaction and do not want to
			// stop the transaction
			logger.error("Could not save Location Data for transaction id {} latitude {} and longitude {}", transactionID, latitude, longitude, e);
		}
	}

	public void persistExtraData(EntityManager em) {
		if (this.extraDataForKeyType != null && !this.extraDataForKeyType.isEmpty()) {
			for (String key : this.extraDataForKeyType.keySet()) {
				Object value = extraDataForKeyType.get(key);
				try {
					TransactionExtraData transactionExtraData = toTransactionExtraData(TransactionExtraData.Key.valueOf(key), value);
					em.persist(transactionExtraData);
				} catch (JsonProcessingException e) {
					// Do not throw the exception, we still want to save teh transaction and do not want to
					// stop the transaction
					logger.error("Could not save transaction extra data for transaction id {} key type {} and value {}", id, key, value, e);
				}

			}
		}
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot remove Transactions");
	}

	@Override
	public void validate(Transaction oldValue) throws RuleCheckException
	{
		RuleCheck.isNull("oldValue", oldValue);

	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	///////////////////////////////////
	public static Transaction findByID(EntityManager em, long id, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findByID", Transaction.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Transaction findByNumber(EntityManager em, String number, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findByNumber", Transaction.class);
		query.setParameter("number", number);
		query.setParameter("companyID", companyID);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Transaction findDuplicateForAgent(EntityManager em, String type, Integer agentID, String recipientMSISDN, BigDecimal amount, Date since, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findDuplicate", Transaction.class);
		query.setParameter("agentID", agentID);
		query.setParameter("companyID", companyID);
		query.setParameter("type", type);
		query.setParameter("b_MSISDN", recipientMSISDN);
		query.setParameter("amount", amount);
		query.setParameter("since", since);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}
	
	public static Transaction findDuplicateBundleForAgent(EntityManager em, String type, Integer agentID, String recipientMSISDN, int bundleID, Date since, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findDuplicateBundle", Transaction.class);
		query.setParameter("agentID", agentID);
		query.setParameter("companyID", companyID);
		query.setParameter("type", type);
		query.setParameter("b_MSISDN", recipientMSISDN);
		query.setParameter("bundleID", bundleID);
		query.setParameter("since", since);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Transaction> findAll(EntityManager em, RestParams params, int companyID, Integer myID, String myMsisdn)
	{
		if( myID != null )
		{
			TransactionExtender px = new Transaction.TransactionExtender(myID, myMsisdn);
			px.setIncludeQuery(params.isIncludeQuery());
			return QueryBuilder.getQueryResultList(em, Transaction.class, params, companyID, px, //
					"number", "a_MSISDN", "b_MSISDN");
		} else {
			SearchExtender sx = new SearchExtender();
			sx.setMyMsisdn(myMsisdn);
			sx.setIncludeQuery(params.isIncludeQuery());
			return QueryBuilder.getQueryResultList(em, Transaction.class, params, companyID, sx, //
					"number", "a_MSISDN", "b_MSISDN");
		}
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID, Integer myID, String myMsisdn)
	{
		if( myID != null )
		{
			TransactionExtender px = new Transaction.TransactionExtender(myID, myMsisdn);
			px.setIncludeQuery(params.isIncludeQuery());
			TypedQuery<Long> query =  QueryBuilder.getCountQuery(em, Transaction.class, params, companyID, px, //
					"number", "a_MSISDN", "b_MSISDN");
			return query.getSingleResult();
		} else {
			SearchExtender sx = new SearchExtender();
			sx.setMyMsisdn(myMsisdn);
			sx.setIncludeQuery(params.isIncludeQuery());
			TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Transaction.class, params, companyID, sx, //
					"number", "a_MSISDN", "b_MSISDN");
			return query.getSingleResult();
		}
	}

	public static List<Transaction> findMine(EntityManager em, RestParams params, int companyID, int myID, String myMsisdn)
	{
		TransactionExtender px = new Transaction.TransactionExtender(myID, myMsisdn);
		px.setIncludeQuery(params.isIncludeQuery());
		return QueryBuilder.getQueryResultList(em, Transaction.class, params, companyID, px, //
				"number", "a_MSISDN", "b_MSISDN");
	}

	public static Long findMyCount(EntityManager em, RestParams params, int companyID, int myID, String myMsisdn)
	{
		TransactionExtender px = new Transaction.TransactionExtender(myID, myMsisdn);
		px.setIncludeQuery(params.isIncludeQuery());
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Transaction.class, params, companyID, px, //
				"number", "a_MSISDN", "b_MSISDN");
		return query.getSingleResult();
	}

	public static List<Transaction> findLastForAgent(EntityManager em, int agentId, int maxTransactions, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findLastForAgent", Transaction.class);
		query.setParameter("agentID", agentId);
		query.setParameter("companyID", companyID);
		query.setMaxResults(maxTransactions);
		return query.getResultList();
	}
	
	public static Transaction findLastSuccessfulTransferToAgent(EntityManager em, String agentMSISDN, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findLastSuccessfulTransferToAgent", Transaction.class);
		query.setParameter("bMSISDN", agentMSISDN);
		query.setParameter("companyID", companyID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Transaction> findLast(EntityManager em, int maxTransactions, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findLast", Transaction.class);
		query.setParameter("companyID", companyID);
		query.setMaxResults(maxTransactions);
		return query.getResultList();
	}

	public static Transaction findByReversedID(EntityManager em, long reversedID, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findByReversedID", Transaction.class);
		query.setParameter("reversedID", reversedID);
		query.setParameter("companyID", companyID);
		List<Transaction> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	@SuppressWarnings("unchecked")
	public static List<Object[]> totalsForAgent(EntityManager em, int agentId, Date startTime, int companyID)
	{
		Query query = em.createNamedQuery("Transaction.totalsForAgent");
		query.setParameter("agentID", agentId);
		query.setParameter("companyID", companyID);
		query.setParameter("startTime", startTime);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<Object[]> depositsForAgent(EntityManager em, int agentId, Date startTime, int companyID)
	{
		Query query = em.createNamedQuery("Transaction.depositsForAgent");
		query.setParameter("agentID", agentId);
		query.setParameter("companyID", companyID);
		query.setParameter("startTime", startTime);
		return query.getResultList();
	}

	public static List<Transaction> findRecentRewards(EntityManager em, int agentID, Date cutOff, int companyID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.recentRewards", Transaction.class);
		query.setParameter("agentID", agentID);
		query.setParameter("companyID", companyID);
		query.setParameter("startTime", cutOff);
		return query.getResultList();
	};

	public static boolean referencesArea(EntityManager em, int areaID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceArea", Transaction.class);
		query.setParameter("areaID", areaID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesServiceClass(EntityManager em, int serviceClassID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceServiceClass", Transaction.class);
		query.setParameter("serviceClassID", serviceClassID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTier(EntityManager em, int tierID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceTier", Transaction.class);
		query.setParameter("tierID", tierID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesAgent(EntityManager em, int agentID)
	{
		logger.info("START time FIND TRANSACTIONS: " + new Date());
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceAgent", Transaction.class);

		query.setParameter("agentID", agentID);

		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;

	}

	public static boolean referencesAgentUser(EntityManager em, String msisdn)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceAgentUser", Transaction.class);
		query.setParameter("msisdn", msisdn);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesGroup(EntityManager em, int groupID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceGroup", Transaction.class);
		query.setParameter("groupID", groupID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesPromotion(EntityManager em, int promotionID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referencePromotion", Transaction.class);
		query.setParameter("promotionID", promotionID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesBundle(EntityManager em, int bundleID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceBundle", Transaction.class);
		query.setParameter("bundleID", bundleID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesCell(EntityManager em, int cellID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceCell", Transaction.class);
		query.setParameter("cellID", cellID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesTransferRule(EntityManager em, int transferRuleID)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceTransferRule", Transaction.class);
		query.setParameter("transferRuleID", transferRuleID);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	public static boolean referencesWebUser(EntityManager em, String msisdn)
	{
		TypedQuery<Transaction> query = em.createNamedQuery("Transaction.referenceWebUser", Transaction.class);
		query.setParameter("msisdn", msisdn);
		query.setMaxResults(1);
		List<Transaction> results = query.getResultList();
		return results != null && results.size() > 0;
	}

	// Clean-out old Transactions
	public static int cleanout(EntityManager em, Date before, int companyID)
	{
		Query query = em.createNamedQuery("Transaction.cleanout");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

	// Test Aml Limits
	public void testAmlLimitsA(Account aAccount, BigDecimal amount) throws RuleCheckException
	{
		aAccount.testAmlLimits(a_Agent, amount);
		aAccount.testAmlLimits(a_Agent.getTier(), amount);
		aAccount.testAmlLimits(a_Agent.getGroup(), amount);
		aAccount.testAmlLimits(a_Agent.getServiceClass(), amount);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////
	static class TransactionExtender extends SearchExtender
	{
		private int myID;
		private String myMsisdn;

		public TransactionExtender(int myID, String myMsisdn)
		{
			this.myID = myID;
			this.myMsisdn = myMsisdn;
		}
		
		public TransactionExtender(String myMsisdn)
		{
			this.myMsisdn = myMsisdn;
		}

		@Override
		public String getName()
		{
			StringBuilder sb = new StringBuilder("MyTransactions");
			if (this.myMsisdn != null && this.myMsisdn.length() > 0)
			{
				sb.append("$");
				sb.append(this.myMsisdn);
				sb.append("$");
			}
			return sb.toString();
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<Transaction> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			super.extend(cb, root, query, predicates);

			ParameterExpression<Integer> me = cb.parameter(Integer.class, "myID");
/*
			Subquery<Integer> sqA = query.subquery(Integer.class);
			Subquery<Integer> sqB = query.subquery(Integer.class);
			Subquery<String> sqMsisdn = query.subquery(String.class);
			Root<Agent> rootAgentA = sqA.from(Agent.class);
			Root<Agent> rootAgentB = sqB.from(Agent.class);

			Predicate aAgentIdPredicate = cb.equal(col(rootAgentA, "id"), me);
			Predicate bAgentIdPredicate = cb.equal(col(rootAgentB, "id"), me);
			Predicate aOwnerPredicate = cb.equal(col(rootAgentA, "owner"), me);
			Predicate bOwnerPredicate = cb.equal(col(rootAgentB, "owner"), me);

			Expression<Integer> aAgentId = rootAgentA.get("id");
			sqA.select(aAgentId).where(cb.or(aAgentIdPredicate, aOwnerPredicate));
			Expression<Integer> aAgentExpr = root.get("a_AgentID");

			Expression<Integer> bAgentId = rootAgentB.get("id");
			sqB.select(bAgentId).where(cb.or(bAgentIdPredicate, bOwnerPredicate));
			Expression<Integer> bAgentExpr = root.get("b_AgentID");

			Predicate aAgentPredicate = aAgentExpr.in(sqA);
			Predicate bAgentPredicate = bAgentExpr.in(sqB);
			predicates.add(cb.or(aAgentPredicate, bAgentPredicate));
*/
			if (relation.equals(RELATION_OWN))
			{
				predicates.add(cb.or(cb.equal(root.get("a_AgentID"), me), cb.equal(root.get("b_AgentID"), me)));
			}
			else if (relation.equals(RELATION_OWN_A))
			{
				predicates.add(cb.equal(root.get("a_AgentID"), me));
			}
			else if (relation.equals(RELATION_OWN_B))
			{
				predicates.add(cb.equal(root.get("b_AgentID"), me));
			}
			else if (relation.equals(RELATION_OWNER_A))
			{
				predicates.add(cb.equal(root.get("a_OwnerAgentID"), me));
				//Predicate aOwnerPredicate = cb.equal(col(rootAgentA, "ownerAgentID"), me);
				//Expression<Integer> aAgentId = rootAgentA.get("id");
				//sqA.select(aAgentId).where(aOwnerPredicate);
				//Expression<Integer> aAgentExpr = root.get("a_AgentID");
				//predicates.add(aAgentExpr.in(sqA));
			}
			else if (relation.equals(RELATION_OWNER_B))
			{
				predicates.add(cb.equal(root.get("b_OwnerAgentID"), me));
				//Predicate bOwnerPredicate = cb.equal(col(rootAgentB, "ownerAgentID"), me);
				//Expression<Integer> bAgentId = rootAgentB.get("id");
				//sqB.select(bAgentId).where(bOwnerPredicate);
				//Expression<Integer> bAgentExpr = root.get("b_AgentID");
				//predicates.add(bAgentExpr.in(sqB));
			}
			else if (relation.equals(RELATION_ALL))
			{
				predicates.add(cb.or(	cb.equal(root.get("a_AgentID"), me), 
										cb.equal(root.get("b_AgentID"), me), 
										cb.equal(root.get("b_OwnerAgentID"), me), 
										cb.equal(root.get("a_OwnerAgentID"), me)
									)
							  );
				//Predicate bOwnerPredicate = cb.equal(col(rootAgentB, "ownerAgentID"), me);
				//Expression<Integer> bAgentId = rootAgentB.get("id");
				//sqB.select(bAgentId).where(bOwnerPredicate);
				//Expression<Integer> bAgentExpr = root.get("b_AgentID");
				//predicates.add(bAgentExpr.in(sqB));
			}
			
			/*
			 * If myMsisdn is set, then only return transactions where the a_msisdn = mymsisdn or b_msisdn = mymsisdn
			 */
			if (this.myMsisdn != null && this.myMsisdn.length() > 0)
			{
				Expression<String> myMsisdn = cb.literal(this.myMsisdn);
				Predicate aMsisdn = cb.equal(col(root, "a_MSISDN"), myMsisdn);
				Predicate bMsisdn = cb.equal(col(root, "b_MSISDN"), myMsisdn);
				
				predicates.add(cb.and(cb.or(aMsisdn, bMsisdn)));
			}
			return predicates;
		}

		@Override
		public void addParameters(TypedQuery<?> query)
		{
			Integer agentID = this.getAgentID();
			query.setParameter("myID", agentID != null ? agentID : myID);
		}

		private Predicate subAgent(CriteriaBuilder cb, Root<Transaction> root, CriteriaQuery<?> query, String agentIdName, ParameterExpression<Integer> me)
		{
			Path<Integer> agentId = root.get(agentIdName);
			Subquery<Agent> sq = query.subquery(Agent.class);
			Root<Agent> agent = sq.from(Agent.class);
			sq.select(agent) //
					.where(cb.equal(agent.get("id"), agentId), cb.equal(agent.get("ownerAgentID"), me));
			return cb.exists(sq);
		}

	};

	static class SearchExtender extends PredicateExtender<Transaction>
	{
		private boolean unAdjudicatedOnly = false;
		protected String relation = RELATION_ALL;
		private String myMsisdn;
		private Integer agentID = null;
		private boolean includeQuery;
		
		public void setMyMsisdn(String msisdn)
		{
			this.myMsisdn = msisdn;
		}

		public void setIncludeQuery(boolean includeQuery) {
			this.includeQuery = includeQuery;
		}

		public Integer getAgentID()
		{
			return this.agentID;
		}

		@Override
		public String getName()
		{
			StringBuilder sb = new StringBuilder("MySearch");
			if (this.myMsisdn != null && this.myMsisdn.length() > 0)
			{
				sb.append("$");
				sb.append(this.myMsisdn);
				sb.append("$");
			}
			return sb.toString();
		}

		@Override
		public boolean preProcessFilter(String column, String operator, String value)
		{
			if (UN_ADJUDICATED.equalsIgnoreCase(column))
			{
				unAdjudicatedOnly = "=".equals(operator) && "1".equals(value);
				return false;
			}	

			if (RELATION.equalsIgnoreCase(column))
			{
				relation = value;
				return false;
			}	

			if (column.equals("agentID"))
			{
				agentID = Integer.parseInt(value);
				return false;
			}

			return true;
		}

		@Override
		public String getExtendCacheToken()
		{
			// this is only used for the retarded QueryBuilder QueryCache 
			// so it doesn't used cached queries for queries that are not the same
			if (!this.includeQuery)
			{
				return String.format(
					"type IN ('%s','%s','%s','%s','%s')",
					Transaction.TYPE_SALES_QUERY,
					Transaction.TYPE_DEPOSITS_QUERY,
					Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY,
					Transaction.TYPE_LAST_TRANSACTION_ENQUIRY,
					Transaction.TYPE_BALANCE_ENQUIRY);
			}
			return "";
		}

		@Override
		public List<Predicate> extend(CriteriaBuilder cb, Root<Transaction> root, CriteriaQuery<?> query, List<Predicate> predicates)
		{
			if (!this.includeQuery)
			{
				Expression<String> sa = cb.literal(Transaction.TYPE_SALES_QUERY);
				Predicate p1 = cb.notEqual(col(root, "type"), sa);
				
				Expression<String> sb = cb.literal(Transaction.TYPE_DEPOSITS_QUERY);
				Predicate p2 = cb.notEqual(col(root, "type"), sb);
				
				Expression<String> sc = cb.literal(Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY);
				Predicate p3 = cb.notEqual(col(root, "type"), sc);
				
				Expression<String> sd = cb.literal(Transaction.TYPE_LAST_TRANSACTION_ENQUIRY);
				Predicate p4 = cb.notEqual(col(root, "type"), sd);
				
				Expression<String> se = cb.literal(Transaction.TYPE_BALANCE_ENQUIRY);
				Predicate p5 = cb.notEqual(col(root, "type"), se);
				
				predicates.add(cb.and(p1, p2, p3, p4, p5));
			}

			if (!unAdjudicatedOnly)
				return predicates;

			// Add Predicates for un-adjudicated sales
			Expression<String> sb = cb.literal(Transaction.TYPE_SELL_BUNDLE);
			Predicate p1 = cb.equal(col(root, "type"), sb);
			Expression<String> sl = cb.literal(Transaction.TYPE_SELL);
			Predicate p2 = cb.equal(col(root, "type"), sl);
			Expression<String> st = cb.literal(Transaction.TYPE_SELF_TOPUP);
			Predicate p5 = cb.equal(col(root, "type"), st);

			predicates.add(cb.or(p1, p2, p5));

			Expression<Integer> one = cb.literal(1);
			Predicate p3 = cb.equal(col(root, "followUp"), one);
			predicates.add(p3);

			Path<Integer> id = root.get("id");
			Subquery<Transaction> sq = query.subquery(Transaction.class);
			Root<Transaction> reverse = sq.from(Transaction.class);
			sq.select(reverse) //
					.where(cb.equal(reverse.get("reversedID"), id));
			Predicate p4 = cb.not(cb.exists(sq));
			predicates.add(p4);
			if (this.myMsisdn != null && this.myMsisdn.length() > 0)
			{
				Expression<String> myMsisdn = cb.literal(this.myMsisdn);
				Predicate aMsisdn = cb.equal(col(root, "a_MSISDN"), myMsisdn);
				Predicate bMsisdn = cb.equal(col(root, "b_MSISDN"), myMsisdn);
				
				predicates.add(cb.and(cb.or(aMsisdn, bMsisdn)));
			}

			return predicates;
		}

		@Override
		public List<Predicate> addSearches(List<Predicate> searches, Root<Transaction> root, CriteriaBuilder cb, String parameterName)
		{
			ParameterExpression<String> parameter = cb.parameter(String.class, parameterName);
			Join<Transaction, Agent> join1 = root.join("a_Agent", JoinType.LEFT);
			Path<String> col = join1.get("firstName");
			searches.add(cb.like(col, parameter));
			col = join1.get("surname");
			searches.add(cb.like(col, parameter));

			Join<Transaction, Agent> join2 = root.join("b_Agent", JoinType.LEFT);
			col = join2.get("firstName");
			searches.add(cb.like(col, parameter));
			col = join2.get("surname");
			searches.add(cb.like(col, parameter));

			return searches;
		}
	}

	@JsonIgnore
	@Transient
	public String fullBundleName()
	{
		if (bundleID == null)
			return "";

		try
		{
			Bundle bundle = getBundle();
			return bundle.getType() + " " + bundle.getName();
		}
		catch (Throwable tr)
		{
			logger.error("", tr);
			return String.format("Bundle %s", bundleID);
		}

	}
	
	@JsonIgnore
	@Transient
	public String getNonAirtimeItemDescription(EntityManager em) {
		try {
			NonAirtimeTransactionDetails nonAirtimeTransactionDetails = NonAirtimeTransactionDetails.findById(em, id);
			return nonAirtimeTransactionDetails.getItemDescription();
		} catch (NoResultException ex) {
			logger.error("Cannot find Non-airtime transaction details for id: " + id, ex);
			return "";
		}
	}

	public static String mapChannelType(String channelType) {
		if( channelType == null ) 
			return null;

		String result = channelType.toUpperCase();
		switch (result) {
			case "USSD":
				return "US";
			case "SMARTAPP":
				return "SA";
			default:
				return result.substring(0, Math.min(result.length(), 2));
		}
	}
}

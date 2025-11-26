package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Transaction
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int TYPE_MAX_LENGTH = 2;
	public static final int TRANSACTION_NUMBER_MAX_LENGTH = 11;
	public static final int MACHINE_NAME_MAX_LENGTH = 16;
	public static final int CHANNEL_MAX_LENGTH = 1;
	public static final int INBOUND_TRANSACTION_MAX_LENGTH = 32;
	public static final int INBOUND_SESSION_MAX_LENGTH = 32;
	public static final int MODE_MAX_LENGTH = 1;
	public static final int MSISDN_MAX_LENGTH = 30;
	public static final int IMSI_MAX_LENGTH = 15;
	public static final int IMEI_MAX_LENGTH = 16;
	public static final int RETURN_CODE_MAX_LENGTH = 20;
	public static final int EXTERNAL_CODE_MAX_LENGTH = 15;
	public static final int ADDITIONAL_INFORMATION_MAX_LENGTH = 100;
	public static final int BONUS_PROFILE_MAX_LENGTH = 10;

	public static final String REQUESTER_TYPE_WEB_USER = "W";
	public static final String REQUESTER_TYPE_AGENT = "A";
	public static final String REQUESTER_TYPE_AGENT_USER = "U";
	public static final String REQUESTER_TYPE_SERVICE_USER = "S";

	public static final String UN_ADJUDICATED = "UA";
	public static final String RELATION = "relation";
	
	public static final String RELATION_ALL = "all";
	public static final String RELATION_OWN = "own";
	public static final String RELATION_OWN_A = "ownA";
	public static final String RELATION_OWN_B = "ownB";
	public static final String RELATION_OWNER_A = "ownerA";
	public static final String RELATION_OWNER_B = "ownerB";

	public static final String TYPE_REPLENISH = Type.Code.REPLENISH;
	public static final String TYPE_TRANSFER = Type.Code.TRANSFER;
	public static final String TYPE_SELL = Type.Code.SELL;
	public static final String TYPE_SELL_BUNDLE = Type.Code.SELL_BUNDLE;
	public static final String TYPE_REGISTER_PIN = Type.Code.REGISTER_PIN;
	public static final String TYPE_CHANGE_PIN = Type.Code.CHANGE_PIN;
	public static final String TYPE_BALANCE_ENQUIRY = Type.Code.BALANCE_ENQUIRY;
	public static final String TYPE_SELF_TOPUP = Type.Code.SELF_TOPUP;
	public static final String TYPE_TRANSACTION_STATUS_ENQUIRY = Type.Code.TRANSACTION_STATUS_ENQUIRY;
	public static final String TYPE_LAST_TRANSACTION_ENQUIRY = Type.Code.LAST_TRANSACTION_ENQUIRY;
	public static final String TYPE_ADJUST = Type.Code.ADJUST;
	public static final String TYPE_SALES_QUERY = Type.Code.SALES_QUERY;
	public static final String TYPE_DEPOSITS_QUERY = Type.Code.DEPOSITS_QUERY;
	public static final String TYPE_REVERSE = Type.Code.REVERSE;
	public static final String TYPE_REVERSE_PARTIALLY = Type.Code.REVERSE_PARTIALLY;
	public static final String TYPE_PROMOTION_REWARD = Type.Code.PROMOTION_REWARD;
	public static final String TYPE_ADJUDICATE = Type.Code.ADJUDICATE;
	public static final String TYPE_NON_AIRTIME_DEBIT = Type.Code.NON_AIRTIME_DEBIT;
	public static final String TYPE_NON_AIRTIME_REFUND = Type.Code.NON_AIRTIME_REFUND;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Type Factory
	//
	// /////////////////////////////////
	public static interface IType
	{
		public String getCode();
	}

	public static interface ITypeFactory
	{
		public Class<? extends IType> getTypeClass();

		public IType fromCode(String code) throws NullPointerException, IllegalArgumentException;
	}

	public static enum Type implements IType
	{
		REPLENISH(Code.REPLENISH), TRANSFER(Code.TRANSFER), SELL(Code.SELL), SELL_BUNDLE(Code.SELL_BUNDLE), REGISTER_PIN(Code.REGISTER_PIN), CHANGE_PIN(Code.CHANGE_PIN), BALANCE_ENQUIRY(
				Code.BALANCE_ENQUIRY), SELF_TOPUP(Code.SELF_TOPUP), TRANSACTION_STATUS_ENQUIRY(Code.TRANSACTION_STATUS_ENQUIRY), LAST_TRANSACTION_ENQUIRY(Code.LAST_TRANSACTION_ENQUIRY), ADJUST(
						Code.ADJUST), SALES_QUERY(Code.SALES_QUERY), DEPOSITS_QUERY(
								Code.DEPOSITS_QUERY), REVERSE(Code.REVERSE), REVERSE_PARTIALLY(Code.REVERSE_PARTIALLY), PROMOTION_REWARD(Code.PROMOTION_REWARD), ADJUDICATE(Code.ADJUDICATE),
								NON_AIRTIME_DEBIT(Code.NON_AIRTIME_DEBIT), NON_AIRTIME_REFUND(Code.NON_AIRTIME_REFUND);

		private String code;

		@Override
		public String getCode()
		{
			return this.code;
		}

		private Type(String code)
		{
			this.code = code;
		}

		public static class Code
		{
			public static final String REPLENISH = "RP";
			public static final String TRANSFER = "TX";
			public static final String SELL = "SL";
			public static final String SELL_BUNDLE = "SB";
			public static final String REGISTER_PIN = "PR";
			public static final String CHANGE_PIN = "CP";
			public static final String BALANCE_ENQUIRY = "BE";
			public static final String SELF_TOPUP = "ST";
			public static final String TRANSACTION_STATUS_ENQUIRY = "TS";
			public static final String LAST_TRANSACTION_ENQUIRY = "LT";
			public static final String ADJUST = "AJ";
			public static final String SALES_QUERY = "SQ";
			public static final String DEPOSITS_QUERY = "DQ";
			public static final String REVERSE = "FR";
			public static final String REVERSE_PARTIALLY = "PA";
			public static final String PROMOTION_REWARD = "RW";
			public static final String ADJUDICATE = "AD";
			public static final String NON_AIRTIME_DEBIT = "ND";
			public static final String NON_AIRTIME_REFUND = "NR";
		}

		private static final Map<String, Type> codeMap = new HashMap<String, Type>();

		public static Map<String, Type> getCodeMap()
		{
			return Collections.unmodifiableMap(codeMap);
		}

		public static Type fromCode(String code)
		{
			if (code == null)
				throw new NullPointerException("code may not be null");
			Type result = codeMap.get(code);
			if (result == null)
				throw new IllegalArgumentException(String.format("No Type associated with code '%s'", code));
			return result;
		}

		static
		{
			for (Type type : values())
			{
				codeMap.put(type.getCode(), type);
			}
		}
	}

	public static class TypeFactory implements ITypeFactory
	{
		@Override
		public Class<Type> getTypeClass()
		{
			return Type.class;
		}

		@Override
		public Type fromCode(String code) throws NullPointerException, IllegalArgumentException
		{
			return Type.fromCode(code);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// Identification
	protected long id;
	protected int companyID;
	protected int version;
	protected String type;
	protected String number;
	protected Long reversedID;

	// Amounts
	protected BigDecimal amount;
	protected BigDecimal grossSalesAmount;
	protected BigDecimal costOfGoodsSold;
	protected BigDecimal buyerTradeBonusAmount;
	protected BigDecimal buyerTradeBonusProvision;
	protected BigDecimal buyerTradeBonusPercentage; 
	protected BigDecimal chargeLevied;

	// Environment
	protected String hostname;
	protected Date startTime;
	protected Date endTime;
	protected String channel;
	protected String channelType;
	protected String callerID;
	protected String inboundTransactionID;
	protected String inboundSessionID;
	protected String requestMode;
	protected Integer transferRuleID;

	// A Party
	protected Integer a_AgentID;
	protected String a_MSISDN;
	protected Integer a_TierID;
	protected Integer a_ServiceClassID;
	protected Integer a_GroupID;
	protected Integer a_AreaID;
	protected Integer a_OwnerAgentID;
	protected String a_IMSI;
	protected String a_IMEI;
	protected Integer a_CellID;
	protected Integer a_CellGroupID;
	protected BigDecimal a_BalanceBefore;
	protected BigDecimal a_BalanceAfter;
	protected BigDecimal a_BonusBalanceBefore;
	protected BigDecimal a_BonusBalanceAfter;
	protected BigDecimal a_OnHoldBalanceBefore;
	protected BigDecimal a_OnHoldBalanceAfter;
	// B Party
	protected Integer b_AgentID;
	protected String b_MSISDN;
	protected Integer b_TierID;
	protected Integer b_ServiceClassID;
	protected Integer b_GroupID;
	protected Integer b_AreaID;
	protected Integer b_OwnerAgentID;
	protected String b_IMSI;
	protected String b_IMEI;
	protected Integer b_CellID;
	protected Integer b_CellGroupID;
	protected BigDecimal b_TransferBonusAmount;
	protected String b_TransferBonusProfile;
	protected BigDecimal b_BalanceBefore;
	protected BigDecimal b_BalanceAfter;
	protected BigDecimal b_BonusBalanceBefore;
	protected BigDecimal b_BonusBalanceAfter;

	// Other
	protected String requesterMSISDN;
	protected String requesterType;
	protected Integer bundleID;
	protected Integer promotionID;

	// Result
	protected String returnCode;
	protected String lastExternalResultCode;
	protected boolean rolledBack;
	protected boolean followUp;
	protected String additionalInformation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public long getId()
	{
		return id;
	}

	public Transaction setId(long id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Transaction setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Transaction setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Transaction setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getNumber()
	{
		return number;
	}

	public Transaction setNumber(String number)
	{
		this.number = number;
		return this;
	}

	public Long getReversedID()
	{
		return reversedID;
	}

	public Transaction setReversedID(Long reversedID)
	{
		this.reversedID = reversedID;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public Transaction setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public BigDecimal getGrossSalesAmount()
	{
		return grossSalesAmount;
	}

	public Transaction setGrossSalesAmount(BigDecimal grossSalesAmount)
	{
		this.grossSalesAmount = grossSalesAmount;
		return this;
	}

	public BigDecimal getCostOfGoodsSold()
	{
		return costOfGoodsSold;
	}

	public Transaction setCostOfGoodsSold(BigDecimal costOfGoodsSold)
	{
		this.costOfGoodsSold = costOfGoodsSold;
		return this;
	}

	public BigDecimal getBuyerTradeBonusAmount()
	{
		return buyerTradeBonusAmount;
	}

	public Transaction setBuyerTradeBonusAmount(BigDecimal buyerTradeBonusAmount)
	{
		this.buyerTradeBonusAmount= buyerTradeBonusAmount;
		return this;
	}

	public BigDecimal getBuyerTradeBonusProvision()
	{
		return buyerTradeBonusProvision;
	}

	public Transaction setBuyerTradeBonusProvision(BigDecimal buyerTradeBonusProvision)
	{
		this.buyerTradeBonusProvision = buyerTradeBonusProvision;
		return this;
	}

	public BigDecimal getChargeLevied()
	{
		return chargeLevied;
	}

	public Transaction setChargeLevied(BigDecimal chargeLevied)
	{
		this.chargeLevied = chargeLevied;
		return this;
	}

	public BigDecimal getBuyerTradeBonusPercentage()
	{
		return buyerTradeBonusPercentage;
	}

	public Transaction setBuyerTradeBonusPercentage(BigDecimal buyerTradeBonusPercentage)
	{
		this.buyerTradeBonusPercentage = buyerTradeBonusPercentage;
		return this;
	}

	public String getHostname()
	{
		return hostname;
	}

	public Transaction setHostname(String hostname)
	{
		this.hostname = hostname;
		return this;
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public Transaction setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	public Date getEndTime()
	{
		return endTime;
	}

	public Transaction setEndTime(Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	public String getChannel()
	{
		return channel;
	}

	public Transaction setChannel(String channel)
	{
		this.channel = channel;
		return this;
	}

	public String getChannelType()
	{
		return channelType;
	}

	public Transaction setChannelType(String channelType)
	{
		this.channelType = channelType;
		return this;
	}

	public String getCallerID()
	{
		return callerID;
	}

	public Transaction setCallerID(String callerID)
	{
		this.callerID = callerID;
		return this;
	}

	public String getInboundTransactionID()
	{
		return inboundTransactionID;
	}

	public Transaction setInboundTransactionID(String inboundTransactionID)
	{
		this.inboundTransactionID = inboundTransactionID;
		return this;
	}

	public String getInboundSessionID()
	{
		return inboundSessionID;
	}

	public Transaction setInboundSessionID(String inboundSessionID)
	{
		this.inboundSessionID = inboundSessionID;
		return this;
	}

	public String getRequestMode()
	{
		return requestMode;
	}

	public Transaction setRequestMode(String requestMode)
	{
		this.requestMode = requestMode;
		return this;
	}

	public String getRequesterMSISDN()
	{
		return requesterMSISDN;
	}

	public Transaction setRequesterMSISDN(String requesterMSISDN)
	{
		this.requesterMSISDN = requesterMSISDN;
		return this;
	}

	public String getRequesterType()
	{
		return requesterType;
	}

	public Transaction setRequesterType(String requesterType)
	{
		this.requesterType = requesterType;
		return this;
	}

	public Integer getBundleID()
	{
		return bundleID;
	}

	public Transaction setBundleID(Integer bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	public Integer getPromotionID()
	{
		return promotionID;
	}

	public Transaction setPromotionID(Integer promotionID)
	{
		this.promotionID = promotionID;
		return this;
	}

	public Integer getTransferRuleID()
	{
		return transferRuleID;
	}

	public Transaction setTransferRuleID(Integer transferRuleID)
	{
		this.transferRuleID = transferRuleID;
		return this;
	}

	public Integer getA_AgentID()
	{
		return a_AgentID;
	}

	public Transaction setA_AgentID(Integer a_AgentID)
	{
		this.a_AgentID = a_AgentID;
		return this;
	}

	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	public Transaction setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
		return this;
	}

	public Integer getA_TierID()
	{
		return a_TierID;
	}

	public Transaction setA_TierID(Integer a_TierID)
	{
		this.a_TierID = a_TierID;
		return this;
	}

	public Integer getA_ServiceClassID()
	{
		return a_ServiceClassID;
	}

	public Transaction setA_ServiceClassID(Integer a_ServiceClassID)
	{
		this.a_ServiceClassID = a_ServiceClassID;
		return this;
	}

	public Integer getA_GroupID()
	{
		return a_GroupID;
	}

	public Transaction setA_GroupID(Integer a_GroupID)
	{
		this.a_GroupID = a_GroupID;
		return this;
	}

	public Integer getA_AreaID()
	{
		return a_AreaID;
	}

	public Transaction setA_AreaID(Integer a_AreaID)
	{
		this.a_AreaID = a_AreaID;
		return this;
	}

	public Integer getA_OwnerAgentID()
	{
		return a_OwnerAgentID;
	}

	public Transaction setA_OwnerAgentID(Integer a_OwnerAgentID)
	{
		this.a_OwnerAgentID = a_OwnerAgentID;
		return this;
	}

	public String getA_IMSI()
	{
		return a_IMSI;
	}

	public Transaction setA_IMSI(String a_IMSI)
	{
		this.a_IMSI = a_IMSI;
		return this;
	}

	public String getA_IMEI()
	{
		return a_IMEI;
	}

	public Transaction setA_IMEI(String a_IMEI)
	{
		this.a_IMEI = a_IMEI;
		return this;
	}

	public Integer getA_CellID()
	{
		return a_CellID;
	}

	public Transaction setA_CellID(Integer a_CellID)
	{
		this.a_CellID = a_CellID;
		return this;
	}

	public Integer getA_CellGroupID()
	{
		return a_CellGroupID;
	}

	public Transaction setA_CellGroupID(Integer a_CellGroupID)
	{
		this.a_CellGroupID = a_CellGroupID;
		return this;
	}

	public BigDecimal getA_BalanceBefore()
	{
		return a_BalanceBefore;
	}

	public Transaction setA_BalanceBefore(BigDecimal a_BalanceBefore)
	{
		this.a_BalanceBefore = a_BalanceBefore;
		return this;
	}

	public BigDecimal getA_BalanceAfter()
	{
		return a_BalanceAfter;
	}

	public Transaction setA_BalanceAfter(BigDecimal a_BalanceAfter)
	{
		this.a_BalanceAfter = a_BalanceAfter;
		return this;
	}

	public BigDecimal getA_BonusBalanceBefore()
	{
		return a_BonusBalanceBefore;
	}

	public Transaction setA_BonusBalanceBefore(BigDecimal a_BonusBalanceBefore)
	{
		this.a_BonusBalanceBefore = a_BonusBalanceBefore;
		return this;
	}

	public BigDecimal getA_BonusBalanceAfter()
	{
		return a_BonusBalanceAfter;
	}

	public Transaction setA_BonusBalanceAfter(BigDecimal a_BonusBalanceAfter)
	{
		this.a_BonusBalanceAfter = a_BonusBalanceAfter;
		return this;
	}

	public BigDecimal getA_OnHoldBalanceBefore()
	{
		return a_OnHoldBalanceBefore;
	}

	public Transaction setA_OnHoldBalanceBefore(BigDecimal a_OnHoldBalanceBefore)
	{
		this.a_OnHoldBalanceBefore = a_OnHoldBalanceBefore;
		return this;
	}

	public BigDecimal getA_OnHoldBalanceAfter()
	{
		return a_OnHoldBalanceAfter;
	}

	public Transaction setA_OnHoldBalanceAfter(BigDecimal a_OnHoldBalanceAfter)
	{
		this.a_OnHoldBalanceAfter = a_OnHoldBalanceAfter;
		return this;
	}

	public Integer getB_AgentID()
	{
		return b_AgentID;
	}

	public Transaction setB_AgentID(Integer b_AgentID)
	{
		this.b_AgentID = b_AgentID;
		return this;
	}

	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	public Transaction setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
		return this;
	}

	public Integer getB_TierID()
	{
		return b_TierID;
	}

	public Transaction setB_TierID(Integer b_TierID)
	{
		this.b_TierID = b_TierID;
		return this;
	}

	public BigDecimal getB_TransferBonusAmount()
	{
		return b_TransferBonusAmount;
	}

	public Transaction setB_TransferBonusAmount(BigDecimal b_TransferBonusAmount)
	{
		this.b_TransferBonusAmount = b_TransferBonusAmount;
		return this;
	}

	public String getB_TransferBonusProfile()
	{
		return b_TransferBonusProfile;
	}

	public Transaction setB_TransferBonusProfile(String b_TransferBonusProfile)
	{
		this.b_TransferBonusProfile = b_TransferBonusProfile;
		return this;
	}

	public Integer getB_ServiceClassID()
	{
		return b_ServiceClassID;
	}

	public Transaction setB_ServiceClassID(Integer b_ServiceClassID)
	{
		this.b_ServiceClassID = b_ServiceClassID;
		return this;
	}

	public Integer getB_GroupID()
	{
		return b_GroupID;
	}

	public Transaction setB_GroupID(Integer b_GroupID)
	{
		this.b_GroupID = b_GroupID;
		return this;
	}

	public Integer getB_AreaID()
	{
		return b_AreaID;
	}

	public Transaction setB_AreaID(Integer b_AreaID)
	{
		this.b_AreaID = b_AreaID;
		return this;
	}

	public Integer getB_OwnerAgentID()
	{
		return b_OwnerAgentID;
	}

	public Transaction setB_OwnerAgentID(Integer b_OwnerAgentID)
	{
		this.b_OwnerAgentID = b_OwnerAgentID;
		return this;
	}

	public String getB_IMSI()
	{
		return b_IMSI;
	}

	public Transaction setB_IMSI(String b_IMSI)
	{
		this.b_IMSI = b_IMSI;
		return this;
	}

	public String getB_IMEI()
	{
		return b_IMEI;
	}

	public Transaction setB_IMEI(String b_IMEI)
	{
		this.b_IMEI = b_IMEI;
		return this;
	}

	public Integer getB_CellID()
	{
		return b_CellID;
	}

	public Transaction setB_CellID(Integer b_CellID)
	{
		this.b_CellID = b_CellID;
		return this;
	}

	public Integer getB_CellGroupID()
	{
		return b_CellGroupID;
	}

	public Transaction setB_CellGroupID(Integer b_CellGroupID)
	{
		this.b_CellGroupID = b_CellGroupID;
		return this;
	}

	public BigDecimal getB_BalanceBefore()
	{
		return b_BalanceBefore;
	}

	public Transaction setB_BalanceBefore(BigDecimal b_BalanceBefore)
	{
		this.b_BalanceBefore = b_BalanceBefore;
		return this;
	}

	public BigDecimal getB_BalanceAfter()
	{
		return b_BalanceAfter;
	}

	public Transaction setB_BalanceAfter(BigDecimal b_BalanceAfter)
	{
		this.b_BalanceAfter = b_BalanceAfter;
		return this;
	}

	public BigDecimal getB_BonusBalanceBefore()
	{
		return b_BonusBalanceBefore;
	}

	public Transaction setB_BonusBalanceBefore(BigDecimal b_BonusBalanceBefore)
	{
		this.b_BonusBalanceBefore = b_BonusBalanceBefore;
		return this;
	}

	public BigDecimal getB_BonusBalanceAfter()
	{
		return b_BonusBalanceAfter;
	}

	public Transaction setB_BonusBalanceAfter(BigDecimal b_BonusBalanceAfter)
	{
		this.b_BonusBalanceAfter = b_BonusBalanceAfter;
		return this;
	}

	public String getReturnCode()
	{
		return returnCode;
	}

	public Transaction setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
		return this;
	}

	public String getLastExternalResultCode()
	{
		return lastExternalResultCode;
	}

	public Transaction setLastExternalResultCode(String lastExternalResultCode)
	{
		this.lastExternalResultCode = lastExternalResultCode;
		return this;
	}

	public boolean isRolledBack()
	{
		return rolledBack;
	}

	public Transaction setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
		return this;
	}

	public boolean isFollowUp()
	{
		return followUp;
	}

	public Transaction setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
		return this;
	}

	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	public Transaction setAdditionalInformation(String additionalInformation)
	{
		this.additionalInformation = additionalInformation;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Transaction()
	{
	}

	public Transaction(Transaction transaction)
	{
		this.id = transaction.id;
		this.companyID = transaction.companyID;
		this.version = transaction.version;
		this.type = transaction.type;
		this.number = transaction.number;
		this.reversedID = transaction.reversedID;
		this.amount = transaction.amount;
		this.buyerTradeBonusAmount = transaction.buyerTradeBonusAmount;
		this.buyerTradeBonusProvision = transaction.buyerTradeBonusProvision;
		this.buyerTradeBonusPercentage = transaction.buyerTradeBonusPercentage ;
		this.chargeLevied = transaction.chargeLevied;
		this.hostname = transaction.hostname;
		this.startTime = transaction.startTime;
		this.endTime = transaction.endTime;
		this.channel = transaction.channel;
		this.channelType = transaction.channelType;
		this.callerID = transaction.callerID;
		this.inboundTransactionID = transaction.inboundTransactionID;
		this.inboundSessionID = transaction.inboundSessionID;
		this.requestMode = transaction.requestMode;
		this.transferRuleID = transaction.transferRuleID;
		this.a_AgentID = transaction.a_AgentID;
		this.a_MSISDN = transaction.a_MSISDN;
		this.a_TierID = transaction.a_TierID;
		this.requesterMSISDN = transaction.requesterMSISDN;
		this.requesterType = transaction.requesterType;
		this.bundleID = transaction.bundleID;
		this.promotionID = transaction.promotionID;
		this.a_ServiceClassID = transaction.a_ServiceClassID;
		this.a_GroupID = transaction.a_GroupID;
		this.a_AreaID = transaction.a_AreaID;
		this.a_OwnerAgentID = transaction.a_OwnerAgentID;
		this.a_IMSI = transaction.a_IMSI;
		this.a_IMEI = transaction.a_IMEI;
		this.a_CellID = transaction.a_CellID;
		this.a_BalanceBefore = transaction.a_BalanceBefore;
		this.a_BalanceAfter = transaction.a_BalanceAfter;
		this.a_BonusBalanceBefore = transaction.a_BonusBalanceBefore;
		this.a_BonusBalanceAfter = transaction.a_BonusBalanceAfter;
		this.a_OnHoldBalanceBefore = transaction.a_OnHoldBalanceBefore;
		this.a_OnHoldBalanceAfter = transaction.a_OnHoldBalanceAfter;
		this.b_AgentID = transaction.b_AgentID;
		this.b_MSISDN = transaction.b_MSISDN;
		this.b_TierID = transaction.b_TierID;
		this.b_ServiceClassID = transaction.b_ServiceClassID;
		this.b_GroupID = transaction.b_GroupID;
		this.b_AreaID = transaction.b_AreaID;
		this.b_OwnerAgentID = transaction.b_OwnerAgentID;
		this.b_IMSI = transaction.b_IMSI;
		this.b_IMEI = transaction.b_IMEI;
		this.b_CellID = transaction.b_CellID;
		this.b_TransferBonusAmount = transaction.b_TransferBonusAmount;
		this.b_TransferBonusProfile = transaction.b_TransferBonusProfile;
		this.b_BalanceBefore = transaction.b_BalanceBefore;
		this.b_BalanceAfter = transaction.b_BalanceAfter;
		this.b_BonusBalanceBefore = transaction.b_BonusBalanceBefore;
		this.b_BonusBalanceAfter = transaction.b_BonusBalanceAfter;
		this.returnCode = transaction.returnCode;
		this.lastExternalResultCode = transaction.lastExternalResultCode;
		this.rolledBack = transaction.rolledBack;
		this.followUp = transaction.followUp;
		this.additionalInformation = transaction.additionalInformation;
	}

}

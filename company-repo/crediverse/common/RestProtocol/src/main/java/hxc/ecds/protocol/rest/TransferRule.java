package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TransferRule implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String STATE_ACTIVE = "A";
	public static final String STATE_INACTIVE = "I";

	public static final int DOW_SUNDAYS = 1;
	public static final int DOW_MONDAYS = 2;
	public static final int DOW_TUESDAYS = 4;
	public static final int DOW_WEDNESDAYS = 8;
	public static final int DOW_THURSDAYS = 16;
	public static final int DOW_FRIDAYS = 32;
	public static final int DOW_SATURDAYS = 64;
	public static final int DOW_ALL = 127;

	public static final int NAME_MAX_LENGTH = 80;
	public static final int BONUS_PROFILE_MAX_LENGTH = 10;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected int sourceTierID;
	protected int targetTierID;
	protected BigDecimal buyerTradeBonusPercentage = BigDecimal.ZERO;
	protected BigDecimal targetBonusPercentage = BigDecimal.ZERO;
	protected String targetBonusProfile;
	protected Integer areaID;
	protected Integer groupID;
	protected Integer serviceClassID;
	protected BigDecimal minimumAmount;
	protected BigDecimal maximumAmount;
	protected Integer daysOfWeek;
	protected Date startTimeOfDay;
	protected Date endTimeOfDay;
	protected boolean strictSupplier;
	protected boolean strictArea;
	protected String state;
	protected Integer targetGroupID;
	protected Integer targetServiceClassID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public TransferRule setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public TransferRule setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public TransferRule setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public TransferRule setName(String name)
	{
		this.name = name;
		return this;
	}

	public int getSourceTierID()
	{
		return sourceTierID;
	}

	public TransferRule setSourceTierID(int sourceTierID)
	{
		this.sourceTierID = sourceTierID;
		return this;
	}

	public int getTargetTierID()
	{
		return targetTierID;
	}

	public TransferRule setTargetTierID(int targetTierID)
	{
		this.targetTierID = targetTierID;
		return this;
	}

	public BigDecimal getBuyerTradeBonusPercentage()
	{
		return buyerTradeBonusPercentage;
	}

	public TransferRule setBuyerTradeBonusPercentage(BigDecimal buyerTradeBonusPercentage)
	{
		this.buyerTradeBonusPercentage = buyerTradeBonusPercentage;
		return this;
	}

	public BigDecimal getTargetBonusPercentage()
	{
		return targetBonusPercentage;
	}

	public TransferRule setTargetBonusPercentage(BigDecimal targetBonusPercentage)
	{
		this.targetBonusPercentage = targetBonusPercentage;
		return this;
	}

	public String getTargetBonusProfile()
	{
		return targetBonusProfile;
	}

	public TransferRule setTargetBonusProfile(String targetBonusProfile)
	{
		this.targetBonusProfile = targetBonusProfile;
		return this;
	}

	public Integer getAreaID()
	{
		return areaID;
	}

	public TransferRule setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	public Integer getGroupID()
	{
		return groupID;
	}

	public TransferRule setGroupID(Integer groupID)
	{
		this.groupID = groupID;
		return this;
	}

	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	public TransferRule setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	public Integer getTargetGroupID()
	{
		return targetGroupID;
	}

	public TransferRule setTargetGroupID(Integer targetGroupID)
	{
		this.targetGroupID = targetGroupID;
		return this;
	}

	public Integer getTargetServiceClassID()
	{
		return targetServiceClassID;
	}

	public TransferRule setTargetServiceClassID(Integer targetServiceClassID)
	{
		this.targetServiceClassID = targetServiceClassID;
		return this;
	}

	public BigDecimal getMinimumAmount()
	{
		return minimumAmount;
	}

	public TransferRule setMinimumAmount(BigDecimal minimumAmount)
	{
		this.minimumAmount = minimumAmount;
		return this;
	}

	public BigDecimal getMaximumAmount()
	{
		return maximumAmount;
	}

	public TransferRule setMaximumAmount(BigDecimal maximumAmount)
	{
		this.maximumAmount = maximumAmount;
		return this;
	}

	public Integer getDaysOfWeek()
	{
		return daysOfWeek;
	}

	public TransferRule setDaysOfWeek(Integer daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
		return this;
	}

	public Date getStartTimeOfDay()
	{
		return startTimeOfDay;
	}

	public TransferRule setStartTimeOfDay(Date startTimeOfDay)
	{
		this.startTimeOfDay = startTimeOfDay;
		return this;
	}

	public Date getEndTimeOfDay()
	{
		return endTimeOfDay;
	}

	public TransferRule setEndTimeOfDay(Date endTimeOfDay)
	{
		this.endTimeOfDay = endTimeOfDay;
		return this;
	}

	public boolean isStrictSupplier()
	{
		return strictSupplier;
	}

	public TransferRule setStrictSupplier(boolean strictSupplier)
	{
		this.strictSupplier = strictSupplier;
		return this;
	}

	public boolean isStrictArea()
	{
		return strictArea;
	}

	public TransferRule setStrictArea(boolean strictArea)
	{
		this.strictArea = strictArea;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public TransferRule setState(String state)
	{
		this.state = state;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Exported Business Rules
	//
	// /////////////////////////////////
	public static boolean mayTransferFrom(Tier sourceTier)
	{
		return sourceTier != null & mayTransferFrom(sourceTier.getType());
	}

	public static boolean mayTransferFrom(String sourceTierType)
	{
		return Tier.TYPE_ROOT.equals(sourceTierType) //
				|| Tier.TYPE_STORE.equals(sourceTierType) //
				|| Tier.TYPE_WHOLESALER.equals(sourceTierType) //
				|| Tier.TYPE_RETAILER.equals(sourceTierType);
	}

	public static boolean mayTransferTo(Tier targetTier)
	{
		return targetTier != null && mayTransferTo(targetTier.getType());
	}

	public static boolean mayTransferTo(String targetTierType)
	{
		return Tier.TYPE_SUBSCRIBER.equals(targetTierType) //
				|| Tier.TYPE_STORE.equals(targetTierType) //
				|| Tier.TYPE_WHOLESALER.equals(targetTierType) //
				|| Tier.TYPE_RETAILER.equals(targetTierType);
	}

	public static boolean mayTransferBetween(Tier sourceTier, Tier targetTier)
	{
		return sourceTier != null && targetTier != null && mayTransferBetween(sourceTier.getType(), targetTier.getType());
	}

	public static boolean mayTransferBetween(String sourceTierType, String targetTierType)
	{
		if (sourceTierType == null || targetTierType == null)
			return false;

		switch (sourceTierType)
		{
			case Tier.TYPE_ROOT:
				return Tier.TYPE_STORE.equals(targetTierType) || Tier.TYPE_WHOLESALER.equals(targetTierType) || Tier.TYPE_RETAILER.equals(targetTierType);
			case Tier.TYPE_STORE:
				return Tier.TYPE_STORE.equals(targetTierType) || Tier.TYPE_WHOLESALER.equals(targetTierType) || Tier.TYPE_RETAILER.equals(targetTierType);
			case Tier.TYPE_WHOLESALER:
				return Tier.TYPE_WHOLESALER.equals(targetTierType) || Tier.TYPE_RETAILER.equals(targetTierType);
			case Tier.TYPE_RETAILER:
				return Tier.TYPE_RETAILER.equals(targetTierType) || Tier.TYPE_SUBSCRIBER.equals(targetTierType);
			case Tier.TYPE_SUBSCRIBER:
				return false;
			default:
				return false;
		}
	}

	public static boolean mayReceiveBuyerTradeBonus(Tier targetTier)
	{
		return targetTier != null & mayReceiveBuyerTradeBonus(targetTier.getType());
	}

	public static boolean mayReceiveBuyerTradeBonus(String targetTierType)
	{
		return Tier.TYPE_WHOLESALER.equals(targetTierType) || Tier.TYPE_RETAILER.equals(targetTierType);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //

				// Name Length
				.notEmpty("name", name, NAME_MAX_LENGTH) //

				// Bonus Limits
				.notNull("buyerTradeBonusPercentage", buyerTradeBonusPercentage) //
				.notLess("buyerTradeBonusPercentage", buyerTradeBonusPercentage, BigDecimal.ZERO) //
				.notLess("buyerTradeBonusPercentage", BigDecimal.ONE, buyerTradeBonusPercentage) //
				.maxDigitsAfterDecimalPoint("buyerTradeBonusPercentage", buyerTradeBonusPercentage.multiply(new BigDecimal(100)), 3) //

				// Min/Max Amounts
				.notLess("minimumAmount", minimumAmount, BigDecimal.ZERO) //
				.notLess("maximumAmount", maximumAmount, BigDecimal.ZERO) //
				.notLess("maximumAmount", maximumAmount, minimumAmount) //
				.isMoney("minimumAmount", minimumAmount) //
				.isMoney("maximumAmount", maximumAmount) //

				// State
				.notLess("companyID", companyID, 1) //
				.oneOf("state", state, STATE_ACTIVE, STATE_INACTIVE);

		// Cannot transfer to Self
		if (sourceTierID == targetTierID)
			validator.append(Violation.RECURSIVE, "targetTier.type", null, "Cannot transfer to same Tier");

		// Days of Week
		if (daysOfWeek != null)
			validator.equals("daysOfWeek", daysOfWeek & DOW_ALL, daysOfWeek);

		// Time of Day
		if (startTimeOfDay != null && endTimeOfDay != null)
			validator.notLess("endTimeOfDay", endTimeOfDay.getTime(), startTimeOfDay.getTime());

		return validator.toList();
	}

}

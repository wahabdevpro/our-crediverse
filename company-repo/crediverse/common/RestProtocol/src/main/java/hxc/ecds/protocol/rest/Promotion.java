package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Promotion implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int NAME_MAX_LENGTH = 50;

	public static final String STATE_ACTIVE = "A";
	public static final String STATE_DEACTIVATED = "D";

	public static final int PER_DAY = 1;
	public static final int PER_WEEK = 2;
	public static final int PER_MONTH = 3;
	public static final int PER_CALENDAR_DAY = 11;
	public static final int PER_CALENDAR_WEEK = 12;
	public static final int PER_CALENDAR_MONTH = 13;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected String state = STATE_ACTIVE;
	protected Date startTime;
	protected Date endTime;
	protected Integer transferRuleID;
	protected Integer areaID;
	protected Integer serviceClassID;
	protected Integer bundleID;
	protected BigDecimal targetAmount;
	protected int targetPeriod;
	protected BigDecimal rewardPercentage;
	protected BigDecimal rewardAmount;
	protected boolean retriggerable = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return name;
	}

	public int getId()
	{
		return id;
	}

	public Promotion setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Promotion setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Promotion setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Promotion setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Promotion setState(String state)
	{
		this.state = state;
		return this;
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public Promotion setStartTime(Date startTime)
	{
		this.startTime = startTime;
		return this;
	}

	public Date getEndTime()
	{
		return endTime;
	}

	public Promotion setEndTime(Date endTime)
	{
		this.endTime = endTime;
		return this;
	}

	public Integer getTransferRuleID()
	{
		return transferRuleID;
	}

	public Promotion setTransferRuleID(Integer transferRuleID)
	{
		this.transferRuleID = transferRuleID;
		return this;
	}

	public Integer getAreaID()
	{
		return areaID;
	}

	public Promotion setAreaID(Integer areaID)
	{
		this.areaID = areaID;
		return this;
	}

	public Integer getServiceClassID()
	{
		return serviceClassID;
	}

	public Promotion setServiceClassID(Integer serviceClassID)
	{
		this.serviceClassID = serviceClassID;
		return this;
	}

	public Integer getBundleID()
	{
		return bundleID;
	}

	public Promotion setBundleID(Integer bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	public BigDecimal getTargetAmount()
	{
		return targetAmount;
	}

	public Promotion setTargetAmount(BigDecimal targetAmount)
	{
		this.targetAmount = targetAmount;
		return this;
	}

	public int getTargetPeriod()
	{
		return targetPeriod;
	}

	public Promotion setTargetPeriod(int targetPeriod)
	{
		this.targetPeriod = targetPeriod;
		return this;
	}

	public BigDecimal getRewardPercentage()
	{
		return rewardPercentage;
	}

	public Promotion setRewardPercentage(BigDecimal rewardPercentage)
	{
		this.rewardPercentage = rewardPercentage;
		return this;
	}

	public BigDecimal getRewardAmount()
	{
		return rewardAmount;
	}

	public Promotion setRewardAmount(BigDecimal rewardAmount)
	{
		this.rewardAmount = rewardAmount;
		return this;
	}

	public boolean isRetriggerable()
	{
		return retriggerable;
	}

	public Promotion setRetriggerable(boolean retriggerable)
	{
		this.retriggerable = retriggerable;
		return this;
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
				.notEmpty("name", name, NAME_MAX_LENGTH) //
				.notLess("companyID", companyID, 1) //
				.oneOf("state", state, STATE_ACTIVE, STATE_DEACTIVATED) //
				.notNull("startTime", startTime) //
				.notNull("endTime", endTime) //
				.notLess("endTime", endTime, startTime) //
				.notNull("targetAmount", targetAmount) //
				.isMoney("targetAmount", targetAmount) //
				.notLess("targetAmount", targetAmount, BigDecimal.ONE) //
				.oneOf("targetPeriod", targetPeriod, PER_DAY, PER_WEEK, PER_MONTH, PER_CALENDAR_DAY, PER_CALENDAR_WEEK, PER_CALENDAR_MONTH) //
				.notNull("rewardPercentage", rewardPercentage) //
				.notLess("rewardPercentage", rewardPercentage, BigDecimal.ZERO) //
				.notMore("rewardPercentage", rewardPercentage, BigDecimal.ONE) //
				.notNull("rewardAmount", rewardAmount) //
				.notLess("rewardAmount", rewardAmount, BigDecimal.ZERO) //
		;

		// WS: Promotions with Area must also have TransferRule ????
		if (areaID != null)
			validator.notNull("transferRuleID", transferRuleID);

		// Cannot create back-dated
		if (this.id <= 0)
			validator.notLess("startTime", startTime, new Date());

		return validator.toList();
	}

}

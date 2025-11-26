package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

public class Group implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	public static final int NAME_MAX_LENGTH = 50;
	public static final int DESCRIPTION_MAX_LENGTH = 80;

	public static final String STATE_ACTIVE = "A";
	public static final String STATE_DEACTIVATED = "D";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected String description;
	protected int tierID;
	protected String state = STATE_ACTIVE;

	// AML Limits
	protected BigDecimal maxTransactionAmount;
	protected Integer maxDailyCount;
	protected BigDecimal maxDailyAmount;
	protected Integer maxMonthlyCount;
	protected BigDecimal maxMonthlyAmount;

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

	public Group setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Group setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Group setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Group setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Group setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public int getTierID()
	{
		return tierID;
	}

	public Group setTierID(int tierID)
	{
		this.tierID = tierID;
		return this;
	}

	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	public Group setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	public Group setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	public Group setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	public Group setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	public Group setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Group setState(String state)
	{
		this.state = state;
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
				.notEmpty("description", description, DESCRIPTION_MAX_LENGTH) //
				.notLess("companyID", companyID, 1) //
				.notLess("tierID", tierID, 1) //

				.notLess("maxTransactionAmount", maxTransactionAmount, BigDecimal.ZERO) //
				.notLess("maxDailyCount", maxDailyCount, 0) //
				.notLess("maxDailyAmount", maxDailyAmount, BigDecimal.ZERO) //
				.notLess("maxMonthlyCount", maxMonthlyCount, 0) //
				.notLess("maxMonthlyAmount", maxMonthlyAmount, BigDecimal.ZERO) //

				.notLess("maxDailyAmount", maxDailyAmount, maxTransactionAmount) //
				.notLess("maxMonthlyAmount", maxMonthlyAmount, maxTransactionAmount) //
				.notLess("maxMonthlyAmount", maxMonthlyAmount, maxDailyAmount) //
				.notLess("maxMonthlyCount", maxMonthlyCount, maxDailyCount) //
				.isMoney("maxDailyAmount", maxDailyAmount) //
				.isMoney("maxMonthlyAmount", maxMonthlyAmount) //
				.oneOf("state", state, STATE_ACTIVE, STATE_DEACTIVATED) //
		;

		return validator.toList();
	}

}

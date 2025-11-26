package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

public class ServiceClass implements IValidatable
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

	public ServiceClass setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public ServiceClass setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public ServiceClass setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public ServiceClass setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public ServiceClass setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public ServiceClass setState(String state)
	{
		this.state = state;
		return this;
	}

	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	public ServiceClass setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	public ServiceClass setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	public ServiceClass setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	public ServiceClass setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	public ServiceClass setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
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

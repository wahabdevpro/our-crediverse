package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tier implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
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
		ROOT(Code.ROOT),
		STORE(Code.STORE),
		WHOLESALER(Code.WHOLESALER),
		RETAILER(Code.RETAILER),
		SUBSCRIBER(Code.SUBSCRIBER);

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
			public static final String ROOT = ".";
			public static final String STORE = "T";
			public static final String WHOLESALER = "W";
			public static final String RETAILER = "R";
			public static final String SUBSCRIBER = "S";
		}

		private static final Map<String,Type> codeMap = new HashMap<String,Type>();

		public static Map<String,Type> getCodeMap()
		{
			return Collections.unmodifiableMap(codeMap);
		}

		public static Type fromCode(String code)
		{
			if (code == null) throw new NullPointerException("code may not be null");
			Type result = codeMap.get(code);
			if ( result == null ) throw new IllegalArgumentException(String.format("No Type associated with code '%s'", code));
			return result;
		}

		static
		{
			for (Type type : values())
			{
				codeMap.put(type.getCode(),type);
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

	public static final String TYPE_ROOT = Type.Code.ROOT;
	public static final String TYPE_STORE = Type.Code.STORE;
	public static final String TYPE_WHOLESALER = Type.Code.WHOLESALER;
	public static final String TYPE_RETAILER = Type.Code.RETAILER;
	public static final String TYPE_SUBSCRIBER = Type.Code.SUBSCRIBER;

	public static final int TYPE_MAX_LENGTH = 1;
	public static final int NAME_MAX_LENGTH = 50;
	public static final int DESCRIPTION_MAX_LENGTH = 80;

	public static final String STATE_ACTIVE = "A";
	public static final String STATE_INACTIVE = "I";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String name;
	protected String type;
	protected String description;
	protected boolean permanent;
	protected String state = STATE_ACTIVE;
	protected BigDecimal downStreamPercentage = BigDecimal.ZERO;
	protected boolean allowIntraTierTransfer;
	protected BigDecimal buyerDefaultTradeBonusPercentage = BigDecimal.ZERO;

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
	public int getId()
	{
		return id;
	}

	public Tier setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Tier setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Tier setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Tier setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Tier setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Tier setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public boolean isPermanent()
	{
		return permanent;
	}

	public Tier setPermanent(boolean permanent)
	{
		this.permanent = permanent;
		return this;
	}

	public BigDecimal getDownStreamPercentage()
	{
		return downStreamPercentage;
	}

	// Treat as read-only
	public Tier setDownStreamPercentage(BigDecimal downStreamPercentage)
	{
		this.downStreamPercentage = downStreamPercentage;
		return this;
	}

	public BigDecimal getMaxTransactionAmount()
	{
		return maxTransactionAmount;
	}

	public Tier setMaxTransactionAmount(BigDecimal maxTransactionAmount)
	{
		this.maxTransactionAmount = maxTransactionAmount;
		return this;
	}

	public Integer getMaxDailyCount()
	{
		return maxDailyCount;
	}

	public Tier setMaxDailyCount(Integer maxDailyCount)
	{
		this.maxDailyCount = maxDailyCount;
		return this;
	}

	public BigDecimal getMaxDailyAmount()
	{
		return maxDailyAmount;
	}

	public Tier setMaxDailyAmount(BigDecimal maxDailyAmount)
	{
		this.maxDailyAmount = maxDailyAmount;
		return this;
	}

	public Integer getMaxMonthlyCount()
	{
		return maxMonthlyCount;
	}

	public Tier setMaxMonthlyCount(Integer maxMonthlyCount)
	{
		this.maxMonthlyCount = maxMonthlyCount;
		return this;
	}

	public BigDecimal getMaxMonthlyAmount()
	{
		return maxMonthlyAmount;
	}

	public Tier setMaxMonthlyAmount(BigDecimal maxMonthlyAmount)
	{
		this.maxMonthlyAmount = maxMonthlyAmount;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Tier setState(String state)
	{
		this.state = state;
		return this;
	}
	
	public boolean isAllowIntraTierTransfer()
	{
		return allowIntraTierTransfer;
	}
	
	public Tier setAllowIntraTierTransfer(boolean allowIntraTierTransfer)
	{
		this.allowIntraTierTransfer = allowIntraTierTransfer;
		return this;
	}

	public BigDecimal getBuyerDefaultTradeBonusPercentage()
	{
		return buyerDefaultTradeBonusPercentage;
	}

	public Tier setBuyerDefaultTradeBonusPercentage(BigDecimal buyerDefaultTradeBonusPercentage)
	{
		this.buyerDefaultTradeBonusPercentage = buyerDefaultTradeBonusPercentage;
		return this;
	}

	@Override
	public String toString()
	{
		return name;
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
				.oneOf("state", state, STATE_ACTIVE, STATE_INACTIVE) //

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
		;

		validator.oneOf("type", type, TYPE_ROOT, TYPE_SUBSCRIBER, TYPE_STORE, TYPE_WHOLESALER, TYPE_RETAILER); //

		return validator.toList();
	}

}

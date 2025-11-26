package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Bundle implements IValidatable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int NAME_MAX_LENGTH = 30;
	public static final int TYPE_MAX_LENGTH = 30;
	public static final int TAG_MAX_LENGTH = 15;
	public static final int DESCRIPTION_MAX_LENGTH = 100;
	public static final int SMS_KEYWORD_MAX_LENGTH = 20;
	public static final int USSD_CODE_MAX_LENGTH = 6;

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
	protected String type;
	protected String state = STATE_ACTIVE;
	protected String tag;
	protected BigDecimal price;
	protected BigDecimal tradeDiscountPercentage;
	protected int menuPosition;
	protected String ussdCode;
	protected String smsKeyword;

	protected List<? extends BundleLanguage> languages = new ArrayList<BundleLanguage>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	public int getId()
	{
		return id;
	}

	public Bundle setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Bundle setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Bundle setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Bundle setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Bundle setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public Bundle setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getState()
	{
		return state;
	}

	public Bundle setState(String state)
	{
		this.state = state;
		return this;
	}

	public String getTag()
	{
		return tag;
	}

	public Bundle setTag(String tag)
	{
		this.tag = tag;
		return this;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public Bundle setPrice(BigDecimal price)
	{
		this.price = price;
		return this;
	}

	public BigDecimal getTradeDiscountPercentage()
	{
		return tradeDiscountPercentage;
	}

	public Bundle setTradeDiscountPercentage(BigDecimal tradeDiscountPercentage)
	{
		this.tradeDiscountPercentage = tradeDiscountPercentage;
		return this;
	}

	public int getMenuPosition()
	{
		return menuPosition;
	}

	public Bundle setMenuPosition(int menuPosition)
	{
		this.menuPosition = menuPosition;
		return this;
	}

	public String getUssdCode()
	{
		return ussdCode;
	}

	public Bundle setUssdCode(String ussdCode)
	{
		this.ussdCode = ussdCode;
		return this;
	}

	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	public Bundle setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
		return this;
	}

	public List<? extends BundleLanguage> getLanguages()
	{
		return languages;
	}

	public Bundle setLanguages(List<? extends BundleLanguage> languages)
	{
		this.languages = languages;
		return this;
	}

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		return new Validator() //
				.notEmpty("name", name, NAME_MAX_LENGTH) //
				.notEmpty("description", description, DESCRIPTION_MAX_LENGTH) //
				.notLess("companyID", companyID, 1) //
				.notEmpty("type", type, TYPE_MAX_LENGTH) //
				.oneOf("state", state, STATE_ACTIVE, STATE_DEACTIVATED) //
				.notEmpty("tag", tag, TAG_MAX_LENGTH) //
				.notNull("price", price) //
				.notLess("price", price, BigDecimal.ONE.movePointLeft(4)) //
				.notNull("tradeDiscountPercentage", tradeDiscountPercentage) //
				.notLess("tradeDiscountPercentage", tradeDiscountPercentage, BigDecimal.ZERO) //
				.notMore("tradeDiscountPercentage", tradeDiscountPercentage, BigDecimal.ONE) //
				.notLess("menuPosition", menuPosition, 1) //
				.notLonger("ussdCode", ussdCode, USSD_CODE_MAX_LENGTH) //
				.notLonger("smsKeyword", smsKeyword, SMS_KEYWORD_MAX_LENGTH) //
				.toList();

	}
}

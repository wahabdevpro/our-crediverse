package hxc.services.ecds.rest.batch;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.Violation;
import hxc.services.ecds.util.RuleCheckException;

public class Adjustment implements IBatchEnabled<Adjustment>
{
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	///////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	///////////////////////////////////
	protected int id;
	protected int companyID;
	protected String accountNumber;
	protected String mobileNumber;

	private String coSignatorySessionID;
	private int agentID;
	private BigDecimal amount;
	private String reason;
	private BigDecimal downStreamPercentage;

	protected int lastUserID;
	protected int version;
	protected Date lastTime;

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	///////////////////////////////////
	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public Adjustment setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public Adjustment setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public Adjustment setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
		return this;
	}

	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public Adjustment setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	public String getCoSignatorySessionID()
	{
		return coSignatorySessionID;
	}

	public Adjustment setCoSignatorySessionID(String coSignatorySessionID)
	{
		this.coSignatorySessionID = coSignatorySessionID;
		return this;
	}

	public int getAgentID()
	{
		return agentID;
	}

	public Adjustment setAgentID(int agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public Adjustment setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public Adjustment setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	public BigDecimal getDownStreamPercentage()
	{
		return downStreamPercentage;
	}

	public Adjustment setDownStreamPercentage(BigDecimal downStreamPercentage)
	{
		this.downStreamPercentage = downStreamPercentage;
		return this;
	}

	@Override
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Adjustment setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public Adjustment setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Adjustment setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	///////////////////////////////////

	public Adjustment()
	{

	}

	public Adjustment(Adjustment that)
	{
		this.id = that.id;
		this.companyID = that.companyID;
		this.accountNumber = that.accountNumber;
		this.mobileNumber = that.mobileNumber;

		this.coSignatorySessionID = that.coSignatorySessionID;
		this.agentID = that.agentID;
		this.amount = that.amount;
		this.reason = that.reason;
		this.downStreamPercentage = that.downStreamPercentage;

		this.lastUserID = that.lastUserID;
		this.version = that.version;
		this.lastTime = that.lastTime;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	///////////////////////////////////
	@Override
	public void validate(Adjustment previous) throws RuleCheckException
	{

	}

	@Override
	public List<Violation> validate()
	{
		AdjustmentRequest request = new AdjustmentRequest();
		request.setCoSignatorySessionID(coSignatorySessionID);
		request.setAgentID(agentID);
		request.setAmount(amount);
		request.setReason(reason);

		return request.validate();
	}

}

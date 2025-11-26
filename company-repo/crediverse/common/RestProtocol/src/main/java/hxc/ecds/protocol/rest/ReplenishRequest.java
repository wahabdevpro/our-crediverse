package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/replenish
public class ReplenishRequest extends TransactionRequest implements ICoSignable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int CO_SIGNATORY_SESSION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_SESSION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_OTP_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_OTP_MAX_LENGTH;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String coSignatorySessionID;
	private String coSignatoryTransactionID;
	private String coSignatoryOTP;
	private BigDecimal amount;
	private BigDecimal bonusProvision;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getCoSignatorySessionID()
	{
		return coSignatorySessionID;
	}

	@Override
	public ReplenishRequest setCoSignatorySessionID(String coSignatorySessionID)
	{
		this.coSignatorySessionID = coSignatorySessionID;
		return this;
	}

	@Override
	public String getCoSignatoryTransactionID()
	{
		return this.coSignatoryTransactionID;
	}

	@Override
	public ReplenishRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	@Override
	public String getCoSignatoryOTP()
	{
		return this.coSignatoryOTP;
	}

	@Override
	public ReplenishRequest setCoSignatoryOTP(String coSignatoryOTP)
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public ReplenishRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public BigDecimal getBonusProvision()
	{
		return bonusProvision;
	}

	public ReplenishRequest setBonusProvision(BigDecimal bonusProvision)
	{
		this.bonusProvision = bonusProvision;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public ReplenishResponse createResponse()
	{
		return new ReplenishResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator(super.validate()) //
				.notNull("amount", amount) //
				.notLess("amount", amount, BigDecimal.ZERO) //
				.isMoney("amount", amount) //
				.notNull("bonusProvision", bonusProvision) //
				.notLess("bonusProvision", bonusProvision, BigDecimal.ZERO) //
				.isMoney("bonusProvision", bonusProvision) //
				;
		validator = CoSignableUtils.validate(validator, this, true);
		return validator.toList();
	}

}

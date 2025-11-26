package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/adjust
public class AdjustmentRequest extends TransactionRequest implements ICoSignable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int REASON_MAX_LENGTH = 100;
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
	private int agentID;
	private BigDecimal amount;
	private String reason;

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
	public AdjustmentRequest setCoSignatorySessionID(String coSignatorySessionID)
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
	public AdjustmentRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
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
	public AdjustmentRequest setCoSignatoryOTP(String coSignatoryOTP)
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}

	public int getAgentID()
	{
		return agentID;
	}

	public AdjustmentRequest setAgentID(int agentID)
	{
		this.agentID = agentID;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public AdjustmentRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public AdjustmentRequest setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public AdjustmentResponse createResponse()
	{
		return new AdjustmentResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator(super.validate()) //
				.notNull("amount", amount) //
				.isMoney("amount", amount) //
				.notEmpty("reason", reason, REASON_MAX_LENGTH) //
				;
		validator = CoSignableUtils.validate(validator, this, true);
		return validator.toList();
	}

}

package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/transfer
public class TransferRequest extends TransactionRequest implements ICoSignable
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
	private String targetMSISDN;
	private BigDecimal amount;
	private Double latitude;
	private Double longitude;

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
	public TransferRequest setCoSignatorySessionID(String coSignatorySessionID)
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
	public TransferRequest setCoSignatoryTransactionID( String coSignatoryTransactionID )
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
	public TransferRequest setCoSignatoryOTP( String coSignatoryOTP )
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}

	public String getTargetMSISDN()
	{
		return targetMSISDN;
	}

	public TransferRequest setTargetMSISDN(String targetMSISDN)
	{
		this.targetMSISDN = targetMSISDN;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public TransferRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public Double getLatitude() 
	{
		return this.latitude;
	}

	public TransferRequest setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude() 
	{
		return this.longitude;
	}

	public TransferRequest setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// TransactionRequest
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator(super.validate()) //
				.notEmpty("targetMSISDN", targetMSISDN, MSISDN_MAX_LENGTH) //
				.notNull("amount", amount) //
				.isMoney("amount", amount) //
				.notLess("amount", amount, BigDecimal.ZERO) //
				;
		validator = CoSignableUtils.validate(validator, this, false);
		return validator.toList();
	}

	@Override
	public TransferResponse createResponse()
	{
		return new TransferResponse(this);
	}

}

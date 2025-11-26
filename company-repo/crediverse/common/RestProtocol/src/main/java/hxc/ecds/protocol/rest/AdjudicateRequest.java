package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/adjudicate
public class AdjudicateRequest extends TransactionRequest implements ICoSignable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int MAX_TRANSACTION_NUMBER_LENGTH = 20;
	public static final int CO_SIGNATORY_SESSION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_SESSION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_OTP_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_OTP_MAX_LENGTH;
	
	public static final String ACTION_CONFIRM_SUCCEEDED = "S";
	public static final String ACTION_CONFIRM_FAILED = "F";
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String coSignatorySessionID;
	protected String coSignatoryTransactionID;
	protected String coSignatoryOTP;
	protected String transactionNumber;
	protected String action;

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
	public AdjudicateRequest setCoSignatorySessionID(String coSignatorySessionID)
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
	public AdjudicateRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	@Override
	public String getCoSignatoryOTP()
	{
		return this.coSignatoryOTP;
	}

	public AdjudicateRequest setCoSignatoryOTP(String coSignatoryOTP)
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public AdjudicateRequest setTransactionNumber(String transactionNumber)
	{
		this.transactionNumber = transactionNumber;
		return this;
	}


	public String getAction()
	{
		return action;
	}

	public AdjudicateRequest setAction(String action)
	{
		this.action = action;
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
				.notEmpty("transactionNumber", transactionNumber, MAX_TRANSACTION_NUMBER_LENGTH) //
				.oneOf("action", action, ACTION_CONFIRM_FAILED, ACTION_CONFIRM_SUCCEEDED) //
				;
		validator = CoSignableUtils.validate(validator, this, true);
		return validator.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public AdjudicateResponse createResponse()
	{
		return new AdjudicateResponse(this);
	}

}

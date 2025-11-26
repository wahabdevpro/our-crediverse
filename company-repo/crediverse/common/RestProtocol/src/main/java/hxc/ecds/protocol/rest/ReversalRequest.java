package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/reverse
public class ReversalRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int MAX_TRANSACTION_NUMBER_LENGTH = 20;
	public static final int MIN_REASON_LENGTH = 2;
	public static final int MAX_REASON_LENGTH = Transaction.ADDITIONAL_INFORMATION_MAX_LENGTH;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String transactionNumber;
	protected String reason;

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public ReversalRequest setTransactionNumber(String transactionNumber)
	{
		this.transactionNumber = transactionNumber;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public ReversalRequest setReason(String reason)
	{
		this.reason = reason;
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
				.notEmpty("reason", reason, MIN_REASON_LENGTH, MAX_REASON_LENGTH) //
				;
		return validator.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReversalResponse createResponse()
	{
		return new ReversalResponse(this);
	}

}

package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/partially_reverse
public class PartialReversalRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int MAX_TRANSACTION_NUMBER_LENGTH = ReversalRequest.MAX_TRANSACTION_NUMBER_LENGTH;
	public static final int MIN_REASON_LENGTH = 2;
	public static final int MAX_REASON_LENGTH = Transaction.ADDITIONAL_INFORMATION_MAX_LENGTH;
	public static final int CO_SIGNATORY_SESSION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_SESSION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_OTP_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_OTP_MAX_LENGTH;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String transactionNumber;
	protected BigDecimal amount;
	protected BigDecimal da1ReverseAmount;
	protected BigDecimal da2ReverseAmount;
	protected String reason;

	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public PartialReversalRequest setTransactionNumber(String transactionNumber)
	{
		this.transactionNumber = transactionNumber;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public PartialReversalRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public BigDecimal getDa1ReverseAmount() {
		return da1ReverseAmount;
	}

	public void setDa1ReverseAmount(BigDecimal da1ReverseAmount) {
		this.da1ReverseAmount = da1ReverseAmount;
	}

	public BigDecimal getDa2ReverseAmount() {
		return da2ReverseAmount;
	}

	public void setDa2ReverseAmount(BigDecimal da2ReverseAmount) {
		this.da2ReverseAmount = da2ReverseAmount;
	}

	public String getReason()
	{
		return reason;
	}

	public PartialReversalRequest setReason(String reason)
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
				.notEmpty("reason", reason, MIN_REASON_LENGTH, MAX_REASON_LENGTH).isMoney("amount", amount) //
				.notLess("amount", amount, BigDecimal.ZERO) //
				;
		return validator.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PartialReversalResponse createResponse()
	{
		return new PartialReversalResponse(this);
	}

}

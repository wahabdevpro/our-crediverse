package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/transaction_status_enquiry
public class TransactionStatusEnquiryRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String transactionNumber;

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
	public String getTransactionNumber()
	{
		return transactionNumber;
	}

	public TransactionStatusEnquiryRequest setTransactionNumber(String transactionNumber)
	{
		this.transactionNumber = transactionNumber;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public TransactionStatusEnquiryResponse createResponse()
	{
		return new TransactionStatusEnquiryResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		return new Validator(super.validate()) //
				.toList();
	}

}

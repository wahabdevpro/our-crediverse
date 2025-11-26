package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/last_transaction_enquiry
public class LastTransactionEnquiryRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected Boolean suppressSms = false;

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
	public Boolean getSuppressSms()
	{
		return suppressSms;
	}

	public LastTransactionEnquiryRequest setSuppressSms(Boolean suppressSms)
	{
		this.suppressSms = suppressSms;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public LastTransactionEnquiryResponse createResponse()
	{
		return new LastTransactionEnquiryResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		return new Validator(super.validate()) //
				.toList();
	}

}

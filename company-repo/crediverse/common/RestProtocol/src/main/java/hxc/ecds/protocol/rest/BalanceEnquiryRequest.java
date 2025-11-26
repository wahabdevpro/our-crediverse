package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/balance_enquiry
public class BalanceEnquiryRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String targetMSISDN; // Optional - Only required if someone else's balance is queried

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
	public String getTargetMSISDN()
	{
		return targetMSISDN;
	}

	public BalanceEnquiryRequest setTargetMSISDN(String targetMSISDN)
	{
		this.targetMSISDN = targetMSISDN;
		return this;
	}	
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public BalanceEnquiryResponse createResponse()
	{
		return new BalanceEnquiryResponse(this);
	}



	@Override
	public List<Violation> validate()
	{
		return new Validator(super.validate()) //
				.toList();
	}

}

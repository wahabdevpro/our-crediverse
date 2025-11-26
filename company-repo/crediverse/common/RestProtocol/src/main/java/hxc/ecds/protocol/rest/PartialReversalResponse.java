package hxc.ecds.protocol.rest;

public class PartialReversalResponse extends TransactionResponse
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public PartialReversalResponse()
	{

	}

	public PartialReversalResponse(TransactionRequest reversalRequest)
	{
		super(reversalRequest);
	}

}

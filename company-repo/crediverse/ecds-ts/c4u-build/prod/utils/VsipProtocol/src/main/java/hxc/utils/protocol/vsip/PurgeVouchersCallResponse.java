package hxc.utils.protocol.vsip;

// The message PurgeVouchers is used to schedule a purge voucher task. The
// purge voucher task purges all voucher that match the specified criteria.

public class PurgeVouchersCallResponse implements IVsipCallResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private PurgeVouchersResponse response;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public PurgeVouchersResponse getResponse()
	{
		return response;
	}

	public void setResponse(PurgeVouchersResponse response)
	{
		this.response = response;
	}
}

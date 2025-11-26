package hxc.services.vssim.engine;

import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.VsipCalls;

public class InjectedResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int responseCode;
	private int skipCount;
	private int failCount;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public InjectedResponse(VsipCalls vsipCall, int responseCode, int skipCount, int failCount)
	{
		this.responseCode = responseCode;
		this.skipCount = skipCount;
		this.failCount = failCount;
	}

	public int getResponse()
	{
		if (skipCount > 0)
		{
			skipCount--;
			return Protocol.RESPONSECODE_SUCCESS;
		}

		if (failCount > 0)
		{
			failCount--;
			return responseCode;
		}

		return Protocol.RESPONSECODE_SUCCESS;
	}

}

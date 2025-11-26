package hxc.services.airsim;

import hxc.services.airsim.protocol.AirCalls;

public class InjectedResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private AirCalls airCall;
	private Integer responseCode;
	private int delay_ms;
	private int skipCount = 0;
	private int failCount = Integer.MAX_VALUE;
	private int count = 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public AirCalls getAirCall()
	{
		return airCall;
	}

	public void setAirCall(AirCalls airCall)
	{
		this.airCall = airCall;
	}

	public Integer getResponseCode()
	{
		return responseCode;
	}

	public void setResponseCode(Integer responseCode)
	{
		this.responseCode = responseCode;
	}

	public int getDelay_ms()
	{
		return delay_ms;
	}

	public void setDelay_ms(int delay_ms)
	{
		this.delay_ms = delay_ms;
	}

	public int getSkipCount()
	{
		return skipCount;
	}

	public void setSkipCount(int skipCount)
	{
		this.skipCount = skipCount;
	}

	public int getFailCount()
	{
		return failCount;
	}

	public void setFailCount(int failCount)
	{
		this.failCount = failCount;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public InjectedResponse()
	{

	}

	public InjectedResponse(AirCalls airCall, Integer responseCode, int delay_ms)
	{
		this.airCall = airCall;
		this.responseCode = responseCode;
		this.delay_ms = delay_ms;
	}

	public InjectedResponse(AirCalls airCall, Integer responseCode, int delay_ms, int skipCount, int failCount)
	{
		this.airCall = airCall;
		this.responseCode = responseCode;
		this.skipCount = skipCount;
		this.failCount = failCount;
		this.delay_ms = delay_ms;
	}

}

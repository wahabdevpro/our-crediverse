package hxc.services.caisim;

public class InjectedSelectiveResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private Integer responseCode = Integer.valueOf(0);
	private Integer failCount = Integer.valueOf(0);
	private Integer skipCount = Integer.valueOf(0);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public InjectedSelectiveResponse()
	{
		
	}
	
	public InjectedSelectiveResponse(Integer responseCode, Integer failCount, Integer skipCount)
	{
		if (responseCode >= 0)
			this.responseCode = responseCode;
		else
			this.responseCode = 0;
		
		if (failCount >= 0)
			this.failCount = failCount;
		else
			this.failCount = 0;
		
		if (skipCount >= 0)
			this.skipCount = skipCount;
		else
			this.skipCount = 0;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public Integer getResponseCode()
	{
		return responseCode;
	}
	
	public void setResponseCode(Integer responseCode)
	{
		if (responseCode >= 0)
			this.responseCode = responseCode;
		else
			this.responseCode = 0;
	}
	
	public Integer getFailCount()
	{
		return failCount;
	}
	
	public void setFailCount(Integer failCount)
	{
		if (failCount >= 0)
			this.failCount = failCount;
		else
			this.failCount = 0;
	}
	
	public Integer getSkipCount()
	{
		return skipCount;
	}
	
	public void setSkipCount(Integer skipCount)
	{
		if (skipCount >= 0)
			this.skipCount = skipCount;
		else
			this.skipCount = 0;
	}
}

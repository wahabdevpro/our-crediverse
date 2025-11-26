package com.concurrent.soap;

import java.util.Date;

import hxc.connectors.Channels;
import hxc.servicebus.ReturnCodes;

public class ServiceHistory
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String processID;
	private Date startTime;
	private String a_MSISDN;
	private String b_MSISDN;
	private Channels channel;
	private int chargeLevied;
	private ReturnCodes returnCode;
	private boolean rolledBack;
	private boolean followUp;
	private String param1;
	private String param2;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String getProcessID()
	{
		return processID;
	}

	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	public void setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
	}

	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	public void setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
	}

	public Channels getChannel()
	{
		return channel;
	}

	public void setChannel(Channels channel)
	{
		this.channel = channel;
	}

	public int getChargeLevied()
	{
		return chargeLevied;
	}

	public void setChargeLevied(int chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	public boolean isRolledBack()
	{
		return rolledBack;
	}

	public void setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
	}

	public boolean isFollowUp()
	{
		return followUp;
	}

	public void setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
	}

	public String getParam1()
	{
		return param1;
	}

	public void setParam1(String param1)
	{
		this.param1 = param1;
	}

	public String getParam2()
	{
		return param2;
	}

	public void setParam2(String param2)
	{
		this.param2 = param2;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ServiceHistory()
	{

	}

}

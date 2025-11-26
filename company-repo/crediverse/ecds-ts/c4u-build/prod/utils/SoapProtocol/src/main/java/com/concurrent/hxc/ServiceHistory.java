package com.concurrent.hxc;

import java.util.Date;

import hxc.connectors.Channels;
import hxc.connectors.datawarehouse.ICdrHistory;
import hxc.servicebus.ReturnCodes;

public class ServiceHistory implements ICdrHistory
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
	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getProcessID()
	{
		return processID;
	}

	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	@Override
	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	@Override
	public String getA_MSISDN()
	{
		return a_MSISDN;
	}

	public void setA_MSISDN(String a_MSISDN)
	{
		this.a_MSISDN = a_MSISDN;
	}

	@Override
	public String getB_MSISDN()
	{
		return b_MSISDN;
	}

	public void setB_MSISDN(String b_MSISDN)
	{
		this.b_MSISDN = b_MSISDN;
	}

	@Override
	public Channels getChannel()
	{
		return channel;
	}

	public void setChannel(Channels channel)
	{
		this.channel = channel;
	}

	@Override
	public int getChargeLevied()
	{
		return chargeLevied;
	}

	public void setChargeLevied(int chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	@Override
	public ReturnCodes getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(ReturnCodes returnCode)
	{
		this.returnCode = returnCode;
	}

	@Override
	public boolean isRolledBack()
	{
		return rolledBack;
	}

	public void setRolledBack(boolean rolledBack)
	{
		this.rolledBack = rolledBack;
	}

	@Override
	public boolean isFollowUp()
	{
		return followUp;
	}

	public void setFollowUp(boolean followUp)
	{
		this.followUp = followUp;
	}

	@Override
	public String getParam1()
	{
		return param1;
	}

	public void setParam1(String param1)
	{
		this.param1 = param1;
	}

	@Override
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

	public ServiceHistory(ICdrHistory history)
	{
		this.serviceID = history.getServiceID();
		this.variantID = history.getVariantID();
		this.processID = history.getProcessID();
		this.startTime = history.getStartTime();
		this.a_MSISDN = history.getA_MSISDN();
		this.b_MSISDN = history.getB_MSISDN();
		this.channel = history.getChannel();
		this.chargeLevied = history.getChargeLevied();
		this.returnCode = history.getReturnCode();
		this.rolledBack = history.isRolledBack();
		this.followUp = history.isFollowUp();
		this.param1 = history.getParam1();
		this.param2 = history.getParam2();
	}

}

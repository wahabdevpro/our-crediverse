package hxc.services.airsim.protocol;

import java.util.Date;

public class TemporalTrigger implements ITemporalTrigger
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serviceID;
	private String variantID;
	private String msisdnA;
	private String msisdnB;
	private String keyValue;
	private Date nextDateTime;
	private boolean beingProcessed;
	private int state;

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
	public String getMsisdnA()
	{
		return msisdnA;
	}

	public void setMsisdn(String msisdnA)
	{
		this.msisdnA = msisdnA;
	}

	@Override
	public String getMsisdnB()
	{
		return msisdnB;
	}

	public void setMsisdnB(String msisdnB)
	{
		this.msisdnB = msisdnB;
	}

	@Override
	public Date getNextDateTime()
	{
		return nextDateTime;
	}

	public void setNextDateTime(Date nextDateTime)
	{
		this.nextDateTime = nextDateTime;
	}

	@Override
	public boolean isBeingProcessed()
	{
		return beingProcessed;
	}

	public void setBeingProcessed(boolean beingProcessed)
	{
		this.beingProcessed = beingProcessed;
	}

	@Override
	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	@Override
	public String getKeyValue()
	{
		return keyValue;
	}

	public void setKeyValue(String keyValue)
	{
		this.keyValue = keyValue;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TemporalTrigger()
	{
	}

	public TemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		this.serviceID = temporalTrigger.getServiceID();
		this.variantID = temporalTrigger.getVariantID();
		this.msisdnA = temporalTrigger.getMsisdnA();
		this.msisdnB = temporalTrigger.getMsisdnB();
		this.nextDateTime = temporalTrigger.getNextDateTime();
		this.beingProcessed = temporalTrigger.isBeingProcessed();
		this.state = temporalTrigger.getState();
		this.keyValue = temporalTrigger.getKeyValue();
	}

}

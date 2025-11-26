package hxc.services.credittransfer;

import java.util.Date;

import hxc.connectors.lifecycle.ITemporalTrigger;

public class CreditTransferUsageTrigger implements ITemporalTrigger
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date nextDateTime;
	private Date dateTime1;
	private Date dateTime2;
	private Date dateTime3;
	private Date dateTime4;
	private String serviceID;
	private String variantID;
	private String msisdnA;
	private String msisdnB;

	private boolean beingProcessed = false;

	private int state = 0;

	// //////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// ///////////////////////////////////

	public CreditTransferUsageTrigger(String msisdnA, String msisdnB, String serviceID, String variantID, Date nextDateTime)
	{
		super();
		this.msisdnA = msisdnA;
		this.msisdnB = msisdnB;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.nextDateTime = nextDateTime;

		// Dates default to now always
		this.dateTime1 = this.dateTime2 = this.dateTime3 = this.dateTime4 = new Date();

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Getters and setters
	//
	// //////////////////////////////////////

	@Override
	public String getServiceID()
	{
		return serviceID;
	}

	@Override
	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public String getVariantID()
	{
		return variantID;
	}

	@Override
	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	@Override
	public String getMsisdnA()
	{
		return this.msisdnA;
	}

	@Override
	public void setMsisdnA(String msisdnA)
	{
		this.msisdnA = msisdnA;
	}

	@Override
	public String getMsisdnB()
	{
		return this.msisdnB;
	}

	@Override
	public void setMsisdnB(String msisdnB)
	{
		this.msisdnB = msisdnB;
	}

	@Override
	public Date getNextDateTime()
	{
		return nextDateTime;
	}

	@Override
	public void setNextDateTime(Date nextDateTime)
	{
		this.nextDateTime = nextDateTime;
	}

	@Override
	public boolean isBeingProcessed()
	{
		return beingProcessed;
	}

	@Override
	public void setBeingProcessed(boolean beingProcessed)
	{
		this.beingProcessed = beingProcessed;
	}

	@Override
	public int getState()
	{
		return state;
	}

	@Override
	public void setState(int state)
	{
		this.state = state;
	}

	@Override
	public Date getDateTime1()
	{
		return dateTime1;
	}

	@Override
	public void setDateTime1(Date dateTime1)
	{
		this.dateTime1 = dateTime1;

	}

	@Override
	public Date getDateTime2()
	{
		return dateTime2;
	}

	@Override
	public void setDateTime2(Date dateTime2)
	{
		this.dateTime2 = dateTime2;
	}

	@Override
	public Date getDateTime3()
	{
		return dateTime3;
	}

	@Override
	public void setDateTime3(Date dateTime3)
	{
		this.dateTime3 = dateTime3;
	}

	@Override
	public Date getDateTime4()
	{
		return dateTime4;
	}

	@Override
	public void setDateTime4(Date dateTime4)
	{
		this.dateTime4 = dateTime4;
	}

	@Override
	public String getKeyValue()
	{
		return null;
	}

	@Override
	public void setKeyValue(String key)
	{

	}

}

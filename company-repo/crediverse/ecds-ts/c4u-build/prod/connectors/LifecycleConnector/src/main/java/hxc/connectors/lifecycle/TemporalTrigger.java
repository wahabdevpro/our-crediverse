package hxc.connectors.lifecycle;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "lc_timetrigger")
public class TemporalTrigger implements ITemporalTrigger
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String variantID;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdnA;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdnB;

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String keyValue;

	@Column(nullable = false)
	private Date nextDateTime;

	@Column(nullable = false)
	private boolean beingProcessed;

	@Column(nullable = false)
	private int state;

	@Column(nullable = true)
	private Date dateTime1;

	@Column(nullable = true)
	private Date dateTime2;

	@Column(nullable = true)
	private Date dateTime3;

	@Column(nullable = true)
	private Date dateTime4;

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
		return msisdnA;
	}

	@Override
	public void setMsisdnA(String msisdnA)
	{
		this.msisdnA = msisdnA;
	}

	@Override
	public String getMsisdnB()
	{
		return msisdnB;
	}

	@Override
	public void setMsisdnB(String msisdnB)
	{
		this.msisdnB = msisdnB;
	}

	@Override
	public String getKeyValue()
	{
		return keyValue;
	}

	@Override
	public void setKeyValue(String keyValue)
	{
		this.keyValue = keyValue;
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public TemporalTrigger()
	{
	}

	public TemporalTrigger(ITemporalTrigger trigger)
	{
		this.serviceID = trigger.getServiceID();
		this.variantID = trigger.getVariantID();
		this.msisdnA = trigger.getMsisdnA();
		this.msisdnB = trigger.getMsisdnB();
		this.nextDateTime = trigger.getNextDateTime();
		this.beingProcessed = trigger.isBeingProcessed();
		this.state = trigger.getState();
		this.dateTime1 = trigger.getDateTime1();
		this.dateTime2 = trigger.getDateTime2();
		this.dateTime3 = trigger.getDateTime3();
		this.dateTime4 = trigger.getDateTime4();
		this.keyValue = trigger.getKeyValue();

	}

}

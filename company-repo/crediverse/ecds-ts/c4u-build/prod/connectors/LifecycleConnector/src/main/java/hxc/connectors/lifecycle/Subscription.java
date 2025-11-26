package hxc.connectors.lifecycle;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "lc_subscription")
public class Subscription implements ISubscription
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true, maxLength = 28, nullable = false)
	private String msisdn;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String serviceID;

	@Column(primaryKey = true, maxLength = 16, nullable = false)
	private String variantID;

	@Column(nullable = false)
	private int serviceClass;

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
	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

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
	public int getServiceClass()
	{
		return serviceClass;
	}

	@Override
	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
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
	public Subscription()
	{
	}

	public Subscription(ISubscription subscription)
	{
		this.msisdn = subscription.getMsisdn();
		this.serviceID = subscription.getServiceID();
		this.variantID = subscription.getVariantID();
		this.serviceClass = subscription.getServiceClass();
		this.nextDateTime = subscription.getNextDateTime();
		this.state = subscription.getState();
		this.dateTime1 = subscription.getDateTime1();
		this.dateTime2 = subscription.getDateTime2();
		this.dateTime3 = subscription.getDateTime3();
		this.dateTime4 = subscription.getDateTime4();

	}

}

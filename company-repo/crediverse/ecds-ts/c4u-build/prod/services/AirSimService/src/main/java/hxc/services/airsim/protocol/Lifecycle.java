package hxc.services.airsim.protocol;

import java.util.Date;

public class Lifecycle implements ILifecycle
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// C4U / SVCCORE / PCC

	// msisdn / ownerId / msisdn
	private String msisdn;

	// serviceID / profileId / ??
	private String serviceID;

	// variantID / profileId / ??
	private String variantID;

	// State/state/status
	private String state;

	// nextDateTime / expireDateTime / time_expiry
	private Date dateTime0;

	// dateTime1 / createDateTime / time_created
	private Date dateTime1;

	// dateTime2 / startDateTime / time_subscribe
	private Date dateTime2;

	// dateTime3 / removeDateTime / time_removal
	private Date dateTime3;

	// dateTime4 / / time_last_renewal
	private Date dateTime4;

	// beingProcessed / /
	private boolean beingProcessed;

	// / cancelled /
	private boolean cancelled;

	// / mode /
	private String mode;

	// Additional Information
	private String[] additionalInformation;

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
	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	@Override
	public Date getDateTime0()
	{
		return dateTime0;
	}

	public void setDateTime0(Date dateTime0)
	{
		this.dateTime0 = dateTime0;
	}

	@Override
	public Date getDateTime1()
	{
		return dateTime1;
	}

	public void setDateTime1(Date dateTime1)
	{
		this.dateTime1 = dateTime1;
	}

	@Override
	public Date getDateTime2()
	{
		return dateTime2;
	}

	public void setDateTime2(Date dateTime2)
	{
		this.dateTime2 = dateTime2;
	}

	@Override
	public Date getDateTime3()
	{
		return dateTime3;
	}

	public void setDateTime3(Date dateTime3)
	{
		this.dateTime3 = dateTime3;
	}

	@Override
	public Date getDateTime4()
	{
		return dateTime4;
	}

	public void setDateTime4(Date dateTime4)
	{
		this.dateTime4 = dateTime4;
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
	public boolean isCancelled()
	{
		return cancelled;
	}

	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	@Override
	public String getMode()
	{
		return mode;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	@Override
	public String[] getAdditionalInformation()
	{
		return additionalInformation;
	}

	public void setAdditionalInformation(String[] additionalInformation)
	{
		this.additionalInformation = additionalInformation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public Lifecycle()
	{

	}

	public Lifecycle(ILifecycle lifecycle)
	{
		this.msisdn = lifecycle.getMsisdn();
		this.serviceID = lifecycle.getServiceID();
		this.variantID = lifecycle.getVariantID();
		this.state = lifecycle.getState();
		this.dateTime0 = lifecycle.getDateTime0();
		this.dateTime1 = lifecycle.getDateTime1();
		this.dateTime2 = lifecycle.getDateTime2();
		this.dateTime3 = lifecycle.getDateTime3();
		this.dateTime4 = lifecycle.getDateTime4();
		this.beingProcessed = lifecycle.isBeingProcessed();
		this.cancelled = lifecycle.isCancelled();
		this.mode = lifecycle.getMode();
		this.additionalInformation = lifecycle.getAdditionalInformation();
	}

}

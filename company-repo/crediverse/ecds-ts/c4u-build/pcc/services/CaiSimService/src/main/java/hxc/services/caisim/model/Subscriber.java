package hxc.services.caisim.model;

import hxc.services.numberplan.INumberPlan;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.SapcSubscription;

/**
 * Represents a subscriber in CAISIM.
 * 
 * A subscriber contains both HLR and SAPC subscriptions.
 * 
 * @author petar
 *
 */
public class Subscriber
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private String msisdn;
	private HlrSubscription hlrSubscription;
	private SapcSubscription sapcSubscription;
	private INumberPlan numberPlan;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////

	public Subscriber()
	{

	}

	public Subscriber(String msisdn, INumberPlan numberPlan)
	{
		this.msisdn = msisdn;
		this.numberPlan = numberPlan;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public String getInternationalNumber()
	{
		return numberPlan.getInternationalFormat(msisdn);
	}

	public String getNationalNumber()
	{
		return numberPlan.getNationalFormat(msisdn);
	}
	
	public boolean hasHlrSubscription()
	{
		return hlrSubscription != null;
	}

	public HlrSubscription getHlrSubscription()
	{
		return hlrSubscription;
	}

	public void setHlrSubscription(HlrSubscription hlrSubscription)
	{
		this.hlrSubscription = hlrSubscription;
	}
	
	public boolean hasSapcSubscription()
	{
		return sapcSubscription != null;
	}
	
	public SapcSubscription getSapcSubscription()
	{
		return sapcSubscription;
	}

	public void setSapcSubscription(SapcSubscription sapcSubscription)
	{
		this.sapcSubscription = sapcSubscription;
	}

	// Checks whether a number is equal to this number
	public boolean isSubscriber(String msisdn)
	{
		return numberPlan.getInternationalFormat(msisdn).equals(getInternationalNumber());
	}
}

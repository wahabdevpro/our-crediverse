package com.concurrent.creditsharing;

import java.io.Serializable;

import com.concurrent.soap.ContactInfo;
import com.concurrent.soap.ServiceQuota;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.soap.Number;

public class SharingState implements Serializable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private String serviceID;
	private String serviceName;
	private String variantID;
	private String variantName;
	private String msisdn;
	private String owner;
	private boolean subscribed = false;
	private boolean beneficiary = false;
	private VasServiceInfo[] allVariants;
	private ServiceQuota[] quotas;
	private long charge;
	private boolean mustExit = false;
	private Number[] members;
	private ContactInfo[] contactInfo;

	private static final long serialVersionUID = 899677388073956115L;
	public static final String OK = "OK";
	public static final String TECHNICAL_PROBLEM = "Technical Problem"; // TODO

	public static final String SHARE_STATE = "SHARESTATE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String getVariantName()
	{
		return variantName;
	}

	public void setVariantName(String variantName)
	{
		this.variantName = variantName;
	}

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public boolean isSubscribed()
	{
		return subscribed;
	}

	public void setSubscribed(boolean subscribed)
	{
		this.subscribed = subscribed;
	}

	public boolean isBeneficiary()
	{
		return beneficiary;
	}

	public void setBeneficiary(boolean beneficiary)
	{
		this.beneficiary = beneficiary;
	}

	public VasServiceInfo[] getAllVariants()
	{
		return allVariants;
	}

	public void setAllVariants(VasServiceInfo[] allVariants)
	{
		this.allVariants = allVariants;
	}

	public long getCharge()
	{
		return charge;
	}

	public void setCharge(long charge)
	{
		this.charge = charge;
	}

	public boolean getMustExit()
	{
		return mustExit;
	}

	public void setMustExit(boolean mustExit)
	{
		this.mustExit = mustExit;
	}

	public Number[] getMembers()
	{
		return members;
	}

	public void setMembers(Number[] members)
	{
		this.members = members;
	}

	public ServiceQuota[] getQuotas()
	{
		return quotas;
	}

	public void setQuotas(ServiceQuota[] quotas)
	{
		this.quotas = quotas;
	}

	public ContactInfo[] getContactInfo()
	{
		return contactInfo;
	}

	public void setContactInfo(ContactInfo[] contactInfo)
	{
		this.contactInfo = contactInfo;
	}

}

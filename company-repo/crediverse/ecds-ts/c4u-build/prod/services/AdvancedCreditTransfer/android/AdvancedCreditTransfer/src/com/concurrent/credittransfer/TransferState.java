package com.concurrent.credittransfer;

import java.io.Serializable;

import com.concurrent.soap.CreditTransfer;
import com.concurrent.soap.VasServiceInfo;

import android.content.Intent;
import android.os.Bundle;

public class TransferState implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String msisdn;
	private String serviceID;
	private String serviceName;
	private String subscribedVariantID;
	private String subscribedVariantName;
	private String selectedNumber;
	private String selectedName;
	private VasServiceInfo[] serviceInfo;
	private CreditTransfer[] transfers;
	private long charge;

	public static final String TRANSFER_STATE = "TRANSFERSTATE";

	private static final long serialVersionUID = -5805245901497529424L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
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

	public String getSubscribedVariantID()
	{
		return subscribedVariantID;
	}

	public void setSubscribedVariantID(String subscribedVariantID)
	{
		this.subscribedVariantID = subscribedVariantID;
	}

	public String getSubscribedVariantName()
	{
		return subscribedVariantName;
	}

	public void setSubscribedVariantName(String subscribedVariantName)
	{
		this.subscribedVariantName = subscribedVariantName;
	}

	public VasServiceInfo[] getServiceInfo()
	{
		return serviceInfo;
	}

	public void setServiceInfo(VasServiceInfo[] serviceInfo)
	{
		this.serviceInfo = serviceInfo;
	}

	public CreditTransfer[] getTransfers()
	{
		return transfers;
	}

	public void setTransfers(CreditTransfer[] transfers)
	{
		this.transfers = transfers;
	}

	public long getCharge()
	{
		return charge;
	}

	public void setCharge(long charge)
	{
		this.charge = charge;
	}

	public String getSelectedNumber()
	{
		return selectedNumber;
	}

	public void setSelectedNumber(String selectedNumber)
	{
		this.selectedNumber = selectedNumber;
	}

	public String getSelectedName()
	{
		return selectedName;
	}

	public void setSelectedName(String selectedName)
	{
		this.selectedName = selectedName;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public void put(Intent intent)
	{
		intent.putExtra(TRANSFER_STATE, this);
	}

	public static TransferState get(Intent intent)
	{
		TransferState state = (TransferState) intent.getSerializableExtra(TRANSFER_STATE);
		return state;
	}

}

package hxc.services.credittransfer;

import java.io.Serializable;

import hxc.configuration.Configurable;
import hxc.services.notification.Texts;

@SuppressWarnings("serial")
@Configurable
public class ServiceClass implements Serializable, Comparable<ServiceClass>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private int serviceClassID;
	private Texts name;
	private boolean postPaid;

	// Charging is unique per service class
	// private TransactionCharge[] chargingProfiles = { new TransactionCharge() };

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ServiceClass()
	{
	}

	public ServiceClass(int serviceClassID, String englishName, String frenchName, boolean postPaid)
	{
		this.serviceClassID = serviceClassID;
		this.name = new Texts(englishName, frenchName, englishName);
		this.postPaid = postPaid;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	// public long getTransactionCharge(long transferAmount)
	// {
	// long transactionCharge = 0L;
	// long lowerBound = 0L;
	// long upperBound = 0L;
	//
	// // In the array of TransactionCharge objects, search for the entry whose range
	// // bounds 'transferAmount'
	// for (TransactionCharge chargingProfile : chargingProfiles)
	// {
	// lowerBound = chargingProfile.getAmountRange().getMinValue();
	// upperBound = chargingProfile.getAmountRange().getMaxValue();
	//
	// if ((lowerBound <= transferAmount*10000) && (transferAmount*10000 < upperBound))
	// {
	// long variableCharge = transferAmount * ( (long) (chargingProfile.getPercentageCharge() * 10000) )/ 100;
	// transactionCharge = (long) (chargingProfile.getFixedCharge() * 10000) + variableCharge;
	// break;
	// }
	// }
	//
	// return transactionCharge;
	// }

	// public TransactionCharge[] getChargingProfiles()
	// {
	// return chargingProfiles;
	// }

	// public void setChargingProfiles(TransactionCharge[] chargingProfiles)
	// {
	// this.chargingProfiles = chargingProfiles;
	// }

	public int getServiceClassID()
	{
		return serviceClassID;
	}

	public void setServiceClassID(int serviceClassID)
	{
		this.serviceClassID = serviceClassID;
	}

	public Texts getName()
	{
		return name;
	}

	public void setName(Texts name)
	{
		this.name = name;
	}

	public boolean isPostPaid()
	{
		return postPaid;
	}

	public void setPostPaid(boolean postPaid)
	{
		this.postPaid = postPaid;
	}

	@Override
	public int compareTo(ServiceClass sc)
	{
		if (this.serviceClassID < sc.serviceClassID)
		{
			return -1;
		}
		else if (this.serviceClassID == sc.serviceClassID)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
}

package hxc.services.credittransfer;

public class ChargingProfile
{
	private int[] serviceClasses;
	private TransactionCharge[] chargingBands;

	public int[] getServiceClasses()
	{
		return serviceClasses;
	}

	public void setServiceClasses(int[] serviceClasses)
	{
		this.serviceClasses = serviceClasses;
	}

	public TransactionCharge[] getChargingBands()
	{
		return chargingBands;
	}

	public void setChargingBands(TransactionCharge[] chargingBands)
	{
		this.chargingBands = chargingBands;
	}

}

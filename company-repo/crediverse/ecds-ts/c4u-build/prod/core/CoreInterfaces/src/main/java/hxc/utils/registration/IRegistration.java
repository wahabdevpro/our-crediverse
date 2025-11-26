package hxc.utils.registration;

import java.util.Date;

public interface IRegistration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getIssuer();

	public abstract Date getIssueDate();

	public abstract int getMaxTPS();

	public abstract int getMaxPeakTPS();

	public abstract int getMaxNodes();

	public abstract String getSupplierKey();

	public abstract IFacilityRegistration[] getFacilities();

}
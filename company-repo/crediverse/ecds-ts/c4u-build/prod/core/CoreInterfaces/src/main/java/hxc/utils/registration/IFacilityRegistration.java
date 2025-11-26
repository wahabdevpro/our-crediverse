package hxc.utils.registration;

import java.util.Date;

public interface IFacilityRegistration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getFacilityID();

	public abstract Date getExpiryDate();

	public abstract int getMaxSubscribers();

}
package hxc.utils.registration;

import java.util.Date;

public class FacilityRegistration implements IFacilityRegistration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String facilityID;
	private Date expiryDate;
	private int maxSubscribers;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getFacilityID()
	{
		return facilityID;
	}

	public void setFacilityID(String facilityID)
	{
		this.facilityID = facilityID;
	}

	@Override
	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	@Override
	public int getMaxSubscribers()
	{
		return maxSubscribers;
	}

	public void setMaxSubscribers(int maxSubscribers)
	{
		this.maxSubscribers = maxSubscribers;
	}

}

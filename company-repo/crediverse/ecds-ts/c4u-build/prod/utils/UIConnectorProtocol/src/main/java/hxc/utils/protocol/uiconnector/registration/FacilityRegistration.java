package hxc.utils.protocol.uiconnector.registration;

import java.io.Serializable;
import java.util.Date;

import hxc.utils.registration.IFacilityRegistration;

public class FacilityRegistration implements IFacilityRegistration, Serializable
{

	private static final long serialVersionUID = 9196680706903598630L;
	private String facilityID;
	private Date expiryDate;
	private int maxSubscribers;

	public FacilityRegistration(IFacilityRegistration facilityRegistration)
	{
		this.facilityID = facilityRegistration.getFacilityID();
		this.expiryDate = facilityRegistration.getExpiryDate();
		this.maxSubscribers = facilityRegistration.getMaxSubscribers();
	}

	@Override
	public String getFacilityID()
	{
		return facilityID;
	}

	@Override
	public Date getExpiryDate()
	{
		return expiryDate;
	}

	@Override
	public int getMaxSubscribers()
	{
		return maxSubscribers;
	}

}

package hxc.connectors.hmx;

import hxc.connectors.hlr.IHlrInformation;

public class HlrInformation implements IHlrInformation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String IMSI;
	private Integer mnpStatus;
	private Integer mobileCountryCode;
	private Integer mobileNetworkCode;
	private Integer locationAreaCode;
	private Integer cellIdentity;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getIMSI()
	{
		return IMSI;
	}

	public void setIMSI(String imsi)
	{
		this.IMSI = imsi;
	}

	public Integer getMnpStatus()
	{
		return mnpStatus;
	}

	public void setMnpStatus(Integer mnpStatus)
	{
		this.mnpStatus = mnpStatus;
	}

	public Integer getMobileCountryCode()
	{
		return mobileCountryCode;
	}

	public void setMobileCountryCode(Integer mobileCountryCode)
	{
		this.mobileCountryCode = mobileCountryCode;
	}

	public Integer getMobileNetworkCode()
	{
		return mobileNetworkCode;
	}

	public void setMobileNetworkCode(Integer mobileNetworkCode)
	{
		this.mobileNetworkCode = mobileNetworkCode;
	}

	public Integer getLocationAreaCode()
	{
		return locationAreaCode;
	}

	public void setLocationAreaCode(Integer locationAreaCode)
	{
		this.locationAreaCode = locationAreaCode;
	}

	public Integer getCellIdentity()
	{
		return cellIdentity;
	}

	public void setCellIdentity(Integer cellIdentity)
	{
		this.cellIdentity = cellIdentity;
	}

}

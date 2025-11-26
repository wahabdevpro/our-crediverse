package com.concurrent.hxc;

public class GetLocationResponse extends ResponseHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Integer mcc;
	private Integer mnc;
	private Integer lac;
	private Integer cellID;
	private Double latitude;
	private Double longitude;
	private Integer accuracy; // In Meters
	private String address;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Integer getMCC()
	{
		return mcc;
	}

	public void setMCC(Integer mcc)
	{
		this.mcc = mcc;
	}

	public Integer getMNC()
	{
		return mnc;
	}

	public void setMNC(Integer mnc)
	{
		this.mnc = mnc;
	}

	public Integer getLAC()
	{
		return lac;
	}

	public void setLAC(Integer lac)
	{
		this.lac = lac;
	}

	public Integer getCellID()
	{
		return cellID;
	}

	public void setCellID(Integer cellID)
	{
		this.cellID = cellID;
	}

	public Double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}

	public Integer getAccuracy()
	{
		return accuracy;
	}

	public void setAccuracy(Integer accuracy)
	{
		this.accuracy = accuracy;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	/**
	 * Constructor from Request
	 */
	public GetLocationResponse(GetLocationRequest request)
	{
		super(request);
	}

}

package hxc.ecds.protocol.rest;

import java.util.ArrayList;
import java.util.List;

public class Cell implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected int mobileCountryCode;
	protected int mobileNetworkCode;
	protected int localAreaCode;
	protected int cellID;
	protected Double latitude;
	protected Double longitude;
	private String cellGlobalIdentity;
	protected List<? extends Area> areas = new ArrayList<Area>();
	protected List<? extends CellGroup> cellGroups = new ArrayList<CellGroup>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getId()
	{
		return id;
	}

	public Cell setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getCompanyID()
	{
		return companyID;
	}

	public Cell setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Cell setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public int getMobileCountryCode()
	{
		return mobileCountryCode;
	}

	public Cell setMobileCountryCode(int mobileCountryCode)
	{
		this.mobileCountryCode = mobileCountryCode;
		return this;
	}

	public int getMobileNetworkCode()
	{
		return mobileNetworkCode;
	}

	public Cell setMobileNetworkCode(int mobileNetworkCode)
	{
		this.mobileNetworkCode = mobileNetworkCode;
		return this;
	}

	public int getLocalAreaCode()
	{
		return localAreaCode;
	}

	public Cell setLocalAreaCode(int localAreaCode)
	{
		this.localAreaCode = localAreaCode;
		return this;
	}

	public int getCellID()
	{
		return cellID;
	}

	public Cell setCellID(int cellID)
	{
		this.cellID = cellID;
		return this;
	}
	
	public String getCellGlobalIdentity() 
	{
		cellGlobalIdentity = formatCgi(this); 
		return cellGlobalIdentity;
	}
	
	public static String formatCgi(Cell cell) {
		if (cell == null) {
			return "";
		}
		
		return String.format("%d-%d-%d-%d", cell.getMobileCountryCode(), cell.getMobileNetworkCode(), cell.getLocalAreaCode(), cell.getCellID());
	}
	
	public Double getLatitude()
	{
		return latitude;
	}

	public Cell setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public Cell setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}
	

	public List<? extends Area> getAreas()
	{
		return areas;
	}

	public Cell setAreas(List<? extends Area> areas)
	{
		this.areas = areas;
		return this;
	}

	public List<? extends CellGroup> getCellGroups()
	{
		return cellGroups;
	}

	public Cell setCellGroups(List<? extends CellGroup> cellGroups)
	{
		this.cellGroups = cellGroups;
		return this;
	}

	@Override
	public List<Violation> validate()
	{
		return new Validator() //
				.notLess("companyID", companyID, 1) //
				.notLess("mobileCountryCode", mobileCountryCode, 1) //
				.notLess("mobileNetworkCode", mobileNetworkCode, 1) //
				.notLess("localAreaCode", localAreaCode, 1) //
				.notLess("cellID", cellID, 1) //
				.notLess("latitude", latitude, -90.0) //
				.notMore("latitude", latitude, +90.0) //
				.notLess("longitude", longitude, -180.0) //
				.notMore("longitude", longitude, +180.0) //
				.toList();
	}

}

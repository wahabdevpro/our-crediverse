package com.concurrent.hxc.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "visitor")
public class Visitor
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The origin of the visitor
	private String ip;

	// The country of the visitor
	private String country;

	// The region of the visitor
	private String region;

	// The city of the visitor
	private String city;

	// The postal code of the visitor
	private String postal;

	// The location of the visitor
	private String location;

	// The ISP of the visitor
	private String isp;

	// The number of visits the visitor has visited
	private int visits;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	@Id
	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getRegion()
	{
		return region;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getPostal()
	{
		return postal;
	}

	public void setPostal(String postal)
	{
		this.postal = postal;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getIsp()
	{
		return isp;
	}

	public void setIsp(String isp)
	{
		this.isp = isp;
	}

	public int getVisits()
	{
		return visits;
	}

	public void setVisits(int visits)
	{
		this.visits = visits;
	}

}

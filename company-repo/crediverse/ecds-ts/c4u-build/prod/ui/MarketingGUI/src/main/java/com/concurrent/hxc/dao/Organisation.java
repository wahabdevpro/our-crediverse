package com.concurrent.hxc.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "organisation")
public class Organisation
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// Email as the primary key
	private String email;

	// Password for credentials
	private String password;

	// Name of the organisation
	private String name;

	// The IP that the organisation used. Just for extra information
	private String ip;

	// Their number range that will be added to AIR sim
	private String numberRange;

	// Whether their account is active
	private boolean active;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	@Id
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public String getNumberRange()
	{
		return numberRange;
	}

	public void setNumberRange(String numberRange)
	{
		this.numberRange = numberRange;
	}

}

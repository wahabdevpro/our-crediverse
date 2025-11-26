package com.concurrent.hxc.objects;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class User
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Status Codes
	//
	// /////////////////////////////////
	public static final int SUCCESS = 0;
	public static final int USER_AUTHENTICATION_FAILED = 1;
	public static final int USER_NOT_ACTIVE = 2;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private int status = USER_AUTHENTICATION_FAILED;
	private String name;
	private String email;
	private String simobiNumber;
	private String numberRange;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////
	
	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getSimobiNumber()
	{
		return simobiNumber;
	}

	public void setSimobiNumber(String simobiNumber)
	{
		this.simobiNumber = simobiNumber;
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

package hxc.ecds.protocol.rest;

import jakarta.xml.ws.Response;

import javax.ws.rs.core.Response.Status;

public class TransferRuleIssue extends Violation
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected Integer id;
	protected String name;
	protected Response rsp;
	protected Status httpStatus;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Integer getId()
	{
		return id;
	}

	public TransferRuleIssue setId(Integer id)
	{
		this.id = id;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public TransferRuleIssue setName(String name)
	{
		this.name = name;
		return this;
	}

	public Status getHttpStatus()
	{
		return httpStatus;
	}

	public TransferRuleIssue setHttpStatus(Status httpStatus)
	{
		this.httpStatus = httpStatus;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public TransferRuleIssue(Integer id, String name, String returnCode, Status httpStatus, String property, Object criterium, String additionalInformation)
	{
		super(returnCode, property, criterium, additionalInformation);
		this.id = id;
		this.name = name;
		this.httpStatus = httpStatus;
	}
	
	public TransferRuleIssue() {
	}

}

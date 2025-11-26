package com.concurrent.hxc;

public class GetMembersResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number[] members;
	private ContactInfo[] contactInfo;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Number[] getMembers()
	{
		return members;
	}

	public void setMembers(Number[] members)
	{
		this.members = members;
	}

	public ContactInfo[] getContactInfo()
	{
		return contactInfo;
	}

	public void setContactInfo(ContactInfo[] contactInfo)
	{
		this.contactInfo = contactInfo;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	/**
	 * Constructor from Request
	 */
	public GetMembersResponse(GetMembersRequest request)
	{
		super(request);
	}

}
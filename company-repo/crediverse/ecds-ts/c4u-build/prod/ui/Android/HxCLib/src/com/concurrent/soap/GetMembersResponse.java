package com.concurrent.soap;

import com.concurrent.util.IDeserialisable;
import com.concurrent.util.ISerialiser;

public class GetMembersResponse extends ResponseHeader implements IDeserialisable
{

	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Number[] members;
	private ContactInfo[] contactInfo;
	
	private static final long serialVersionUID = -4164641271595388511L;
	
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
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IDeserialisable
	//
	// /////////////////////////////////
	@Override
	public void deserialise(ISerialiser serialiser)
	{
		super.deserialise(serialiser);
		members = serialiser.getArray("members", Number.class, Number.PROPERTY_COUNT);
		contactInfo = serialiser.getArray("contactInfo", ContactInfo.class, ContactInfo.PROPERTY_COUNT);
	}

	
}
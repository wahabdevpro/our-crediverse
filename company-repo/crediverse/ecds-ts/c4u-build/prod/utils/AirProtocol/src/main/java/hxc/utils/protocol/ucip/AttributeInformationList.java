package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * AttributeInformationList
 * 
 * The struct attributeInformationList contains information about attributes. It is enclosed in a <struct> of its own. Structs are placed in an <array>. Note: Attributes are not supported for products
 * (offer instances). This information will be returned as response to GetAccountDetails, GetBalanceAndDate and GetOffers if requested with requestAttributesFlag and if any attributes exits. It will
 * also be included as response to DeleteOffer and DeleteSubscriber if any attributes is removed.
 */
public class AttributeInformationList
{

	/*
	 * The attributeName contains the name of the attribute.
	 */
	@Air(CAP = "CAP:1, CAP:16", Mandatory = true, Length = "1:128", Format = "Alphanumeric")
	public String attributeName;

	/*
	 * The attributeValueString contains the string value of an attribute.
	 */
	@Air(CAP = "CAP:1, CAP:16", Length = "1:128", Format = "Extendedaddress")
	public String attributeValueString;

	/*
	 * The attributeValueDate contains the date value of an attribute.
	 */
	@Air(CAP = "CAP:16", Range = "DateMin:DateMax,DateInfinite")
	public Date attributeValueDate;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AttributeInformationList()
	{

	}

	public AttributeInformationList(AttributeInformationList attributeInformationList)
	{
		this.attributeName = attributeInformationList.attributeName;
		this.attributeValueString = attributeInformationList.attributeValueString;
		this.attributeValueDate = attributeInformationList.attributeValueDate;
	}

}

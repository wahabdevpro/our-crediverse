package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * AttributeUpdateInformationList
 * 
 * The struct attributeUpdateInformationList contains information about the changes made to attributes. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
public class AttributeUpdateInformationList
{
	/*
	 * The attributeName contains the name of the attribute.
	 */
	@Air(CAP = "CAP:1, CAP:16", Length = "1:128", Format = "Alphanumeric")
	public String attributeName;

	/*-
	 * The attributeUpdateAction parameter contains the requested action for
	 * an attribute.
	 *
	 * Possible Values:
	 * ----------------
	 * ADD:	Add an entry to the set (for attributes storing a set of values).
	 * DELETE:	Delete an entry from the set (for attributes storing a set of values).
	 * CLEAR:	Clear all entries from the set (for attributes storing a set of values) or removes the value for a single value attribute.
	 */
	@Air(CAP = "CAP:1, CAP:16")
	public String attributeUpdateAction;

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

}

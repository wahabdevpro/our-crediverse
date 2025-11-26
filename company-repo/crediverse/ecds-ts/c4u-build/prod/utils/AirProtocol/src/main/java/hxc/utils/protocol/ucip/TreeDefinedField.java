package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * TreeDefinedField
 * 
 * The following parameters can be received in a Refill response (PC:10804) or set in a Refill request (PC:10797). The struct treeDefinedField contains information about the Tree Defined Field values.
 */
public class TreeDefinedField
{
	/*
	 * The treeDefinedFieldName parameter holds the defined name of the TDF.
	 */
	@Air(PC = "PC:10804, PC:10797", CAP = "CAP:7,CAP:11", Format = "Alphanumeric")
	public String treeDefinedFieldName;

	/*-
	 * The treeDefinedFieldType parameter holds the defined type of the TDF.
	 *
	 * Possible Values:
	 * ----------------
	 * Boolean:	
	 * String:	
	 * Long:	
	 * Amount:	
	 */
	@Air(CAP = "CAP:7,CAP:11", Format = "Alphanumeric")
	public String treeDefinedFieldType;

	/*-
	 * The treeDefinedFieldValue parameter contains the defined value of the
	 * TDF.
	 *
	 * Possible Values:
	 * ----------------
	 * Boolean:	"true", "false"
	 * String:	Allowed characters: All
	 * Long:	Numerical
	 * Amount:	Response Decimal value and currency code to hold a monetary amount.
	 */
	@Air(CAP = "CAP:7,CAP:11", Format = "Alphanumeric")
	public String treeDefinedFieldValue;

}

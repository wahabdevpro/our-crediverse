package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * DedicatedAccountIdentifier
 * 
 * The struct dedicatedAccountIdentifier contains information to delete a specific dedicated account. Structs are placed in an <array>.
 */
public class DedicatedAccountIdentifier
{
	/*
	 * The dedicatedAccountID parameter contains the identity of the dedicated account in order to be able to distinguish between the various dedicated accounts in an array of dedicated accounts.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int dedicatedAccountID;

	/*
	 * The expiryDate parameter contains the expiry date for a dedicated account.
	 */
	@Air(Range = "DateMin:DateMax,DateInfinite")
	public Date expiryDate;

	/*
	 * The productID parameter contains the identity of a product.
	 */
	@Air(PC = "PC:09847", CAP = "CAP:6")
	public Integer productID;

}

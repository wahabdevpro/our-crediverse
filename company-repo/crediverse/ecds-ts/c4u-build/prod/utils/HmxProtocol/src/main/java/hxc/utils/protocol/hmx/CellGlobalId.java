package hxc.utils.protocol.hmx;

public class CellGlobalId
{
	// Three digit Mobile Country Code (MCC) number.
	// Mandatory
	public int mobileCountryCode;

	// Three digit Mobile Network Code (MNC) number.
	// Mandatory
	public int mobileNetworkCode;

	// The location area code (LAC) of the subscribers current position
	// Mandatory
	public int locationAreaCode;

	// Mandatory
	// The identity of the subscribers current cell.
	public int cellIdentity;
}

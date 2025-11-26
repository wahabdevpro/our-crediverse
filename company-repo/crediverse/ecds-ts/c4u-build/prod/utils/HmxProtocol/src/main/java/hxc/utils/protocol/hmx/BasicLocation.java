package hxc.utils.protocol.hmx;

import hxc.utils.protocol.hsx.Number;

public class BasicLocation
{
	// This parameter indicates the age in seconds of the returned location information.
	// Optional
	public Integer ageInSeconds;

	// Optional
	public CellGlobalId cellGlobalId;

	// Optional
	public Number vlrNumber;

	// Optional
	public Number mscNumber;
}

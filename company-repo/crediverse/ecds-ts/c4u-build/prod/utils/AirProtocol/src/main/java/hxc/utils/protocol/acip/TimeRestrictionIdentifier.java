package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * TimeRestrictionIdentifier
 * 
 * The timeRestrictionIdentifier contains information about a time restriction. It is enclosed in a <struct> of its own. Structs are placed in an <array>.
 */
@Air(PC = "PC:07061")
public class TimeRestrictionIdentifier
{
	/*
	 * The timeRestrictionID parameter identifies the specific time restriction.
	 */
	@Air(PC = "PC:07061", Mandatory = true, Range = "1:255")
	public int timeRestrictionID;

}

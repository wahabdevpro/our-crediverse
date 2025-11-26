package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * TimeRestrictionInformation
 * 
 * The timeRestrictionInformation parameter contains information about a time restriction. It is enclosed in a <struct> of it own. The structs are placed in an <array>.
 */
@Air(PC = "PC:07061")
public class TimeRestrictionInformation
{
	/*
	 * The timeRestrictionID parameter identifies the specific time restriction.
	 */
	@Air(PC = "PC:07061", Mandatory = true, Range = "1:255")
	public int timeRestrictionID;

	@Air(PC = "PC:07061")
	public TimeRestrictionFlags timeRestrictionFlags;

	/*
	 * The start of the time restriction given as seconds since midnight.
	 */
	@Air(PC = "PC:07061", Mandatory = true, Range = "0:86399")
	public int timeRestrictionStartTime;

	/*
	 * The end of the time restriction given as seconds since midnight.
	 */
	@Air(PC = "PC:07061", Mandatory = true, Range = "0:86399")
	public int timeRestrictionEndTime;

	/*
	 * The offerID parameter contains the identity of an offer.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int offerID;

}

package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * TimeRestrictionFlags
 * 
 * The struct timeRestrictionFlags contains information about how and when a time restriction should be applied. It is enclosed in a <struct> of its own.
 */
@Air(PC = "PC:07061")
public class TimeRestrictionFlags
{
	/*
	 * The mondayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean mondayFlag;

	/*
	 * The tuesdayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean tuesdayFlag;

	/*
	 * The wednesdayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean wednesdayFlag;

	/*
	 * The thursdayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean thursdayFlag;

	/*
	 * The fridayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean fridayFlag;

	/*
	 * The saturdayFlag indicates restrictions for the day.
	 */
	public Boolean saturdayFlag;

	/*
	 * The sundayFlag indicates restrictions for the day.
	 */
	@Air(PC = "PC:07061")
	public Boolean sundayFlag;

	/*
	 * The flag indicates if the restriction is suspended or not. If the flag is set to 1 the restriction is suspended.
	 */
	@Air(PC = "PC:07061")
	public Boolean timeRestrictionSuspendedFlag;

}

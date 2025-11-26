package hxc.ecds.protocol.rest.util;

import java.util.Calendar;
import java.util.Date;

public class DateHelper
{
	public static Calendar toCalendar( Date date )
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}


	public static Calendar mergeCalendars( Calendar dateCalendar, Calendar timeCalendar )
	{
		Calendar calendar = (Calendar)timeCalendar.clone();
		calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
		return calendar;
	}

	public static Date mergeDates( Date date, Date time )
	{
		Calendar dateCalendar = Calendar.getInstance();
		dateCalendar.setTime(date);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);

		calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
		return calendar.getTime();
	}

	@SuppressWarnings("fallthrough")
	public static Calendar mutateStartOf(Calendar reference, int field)
	{
		Calendar calendar = reference;
		switch( field )
		{
			case Calendar.WEEK_OF_YEAR:
				//calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
				{
					calendar.add(Calendar.DATE, -1);
				}
				mutateStartOf(calendar, Calendar.DAY_OF_MONTH);
				break;
			case Calendar.YEAR:
				calendar.set(Calendar.MONTH, calendar.getActualMinimum(Calendar.MONTH));
			case Calendar.MONTH:
				calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
			case Calendar.DAY_OF_MONTH:
				calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
			case Calendar.HOUR_OF_DAY:
				calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
			case Calendar.MINUTE:
				calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
			case Calendar.SECOND:
				calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
			case Calendar.MILLISECOND:
				break;
			default:
				throw new IllegalArgumentException(String.format("Invalid field %s", field));
		}
		return calendar;
	}

	@SuppressWarnings("fallthrough")
	public static Calendar mutateEndOf(Calendar reference, int field)
	{

		Calendar calendar = reference;
		switch( field )
		{
			case Calendar.WEEK_OF_YEAR:
				while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
				{
					calendar.add(Calendar.DATE, -1);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 6);
				mutateEndOf(calendar, Calendar.DAY_OF_MONTH);
				break;
			case Calendar.YEAR:
				calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
			case Calendar.MONTH:
				calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
			case Calendar.DAY_OF_MONTH:
				calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
			case Calendar.HOUR_OF_DAY:
				calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
			case Calendar.MINUTE:
				calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
			case Calendar.SECOND:
				calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
			case Calendar.MILLISECOND:
				break;
			default:
				throw new IllegalArgumentException(String.format("Invalid field %s", field));
		}
		return calendar;
	}

	/////

	public static Calendar startOf(Calendar reference, int field)
	{
		Calendar calendar = (Calendar)reference.clone();
		return mutateStartOf(calendar, field);
	}

	public static Calendar startOf(Date reference, int field)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reference);
		return mutateStartOf(calendar, field);
	}

	public static Calendar endOf(Calendar reference, int field)
	{
		Calendar calendar = (Calendar)reference.clone();
		return mutateEndOf(calendar, field);
	}

	public static Calendar endOf(Date reference, int field)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reference);
		return mutateEndOf(calendar, field);
	}
}

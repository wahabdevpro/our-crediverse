package hxc.utils.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTime extends Date
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final String IMMUTABLE = "Immutable";
	private static final long serialVersionUID = 7523967970034938905L;

	public static final long MILLIS_PER_SECOND = 1000;
	public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
	public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
	public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

	public final static int SUNDAY = 1;
	public final static int MONDAY = 2;
	public final static int TUESDAY = 3;
	public final static int WEDNESDAY = 4;
	public final static int THURSDAY = 5;
	public final static int FRIDAY = 6;
	public final static int SATURDAY = 7;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DateTime()
	{
	}

	public DateTime(long date)
	{
		super(date);
	}

	public DateTime(DateTime date)
	{
		super(date.getTime());
	}

	public DateTime(Calendar cal)
	{
		super(cal.getTime().getTime());
	}

	public DateTime(Date date)
	{
		super(date.getTime());
	}

	public DateTime(int year, int month, int day)
	{
		super(create(year, month, day, 0, 0, 0, 0).getTime());
	}

	public DateTime(int year, int month, int day, int hour, int minute, int seconds)
	{
		super(create(year, month, day, hour, minute, seconds, 0).getTime());
	}

	public DateTime(int year, int month, int day, int hour, int minute, int seconds, int millis)
	{
		super(create(year, month, day, hour, minute, seconds, millis).getTime());
	}

	public static Date create(int year, int month, int day)
	{
		return create(year, month, day, 0, 0, 0, 0);
	}

	public static Date create(int year, int month, int day, int hour, int minute, int seconds, int millis)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, millis);
		return cal.getTime();
	}

	@Override
	public DateTime clone()
	{
		return new DateTime(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Casting
	//
	// /////////////////////////////////
	public Date toDate()
	{
		return new Date(super.getTime());
	}

	public long toLong()
	{
		return super.getTime();
	}

	public Calendar toCalendar()
	{
		Calendar result = Calendar.getInstance();
		result.setTimeInMillis(super.getTime());
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Now and Today
	//
	// /////////////////////////////////
	public static DateTime getNow()
	{
		return new DateTime();
	}

	public static DateTime getToday()
	{
		return getNow().getDatePart();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Date Parts
	//
	// /////////////////////////////////
	public DateTime getTimePart()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return new DateTime(cal);
	}

	public DateTime getDatePart()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new DateTime(cal);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Formatting
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return defaultFormat.format(this);
	}

	public String toString(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Getters
	//
	// /////////////////////////////////

	@Override
	public int getYear()
	{
		return toCalendar().get(Calendar.YEAR);
	}

	@Override
	public int getMonth()
	{
		return toCalendar().get(Calendar.MONTH) + 1;
	}

	@Override
	public int getDay()
	{
		return toCalendar().get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public int getHours()
	{
		return toCalendar().get(Calendar.HOUR_OF_DAY);
	}

	@Override
	public int getMinutes()
	{
		return toCalendar().get(Calendar.MINUTE);
	}

	@Override
	public int getSeconds()
	{
		return toCalendar().get(Calendar.SECOND);
	}

	public int getMillis()
	{
		return toCalendar().get(Calendar.MILLISECOND);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setters
	//
	// /////////////////////////////////

	@Override
	public void setYear(int year)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	@Override
	public void setMonth(int month)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	@Override
	public void setDate(int date)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	@Override
	public void setHours(int hours)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	@Override
	public void setMinutes(int minutes)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	@Override
	public void setSeconds(int seconds)
	{
		throw new RuntimeException(IMMUTABLE);
	}

	public void setMillis()
	{
		throw new RuntimeException(IMMUTABLE);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Month Length
	//
	// /////////////////////////////////
	public int getMonthLength()
	{
		return toCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static int getMonthLength(int year, int month)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);

		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Offset
	//
	// /////////////////////////////////
	public DateTime addYears(int years)
	{
		return addMonths(years * 12);
	}

	public DateTime addMonths(int months)
	{
		Calendar cal = toCalendar();
		int totalMonths = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH) + months;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, totalMonths % 12);
		cal.set(Calendar.YEAR, totalMonths / 12);
		int monthLength = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, day > monthLength ? monthLength : day);
		return new DateTime(cal);
	}

	public DateTime addDays(long days)
	{
		return addHours(days * 24L);
	}

	public DateTime addHours(long hours)
	{
		return addMinutes(hours * 60L);
	}

	public DateTime addMinutes(long minutes)
	{
		return addSeconds(minutes * 60L);
	}

	public DateTime addSeconds(long seconds)
	{
		return addMillis(seconds * 1000L);
	}

	public DateTime addMillis(long millis)
	{
		return new DateTime(toLong() + millis);
	}

	public long millisTo(Date expiryDate)
	{
		return expiryDate.getTime() - this.getTime();
	}

	public DateTime add(int interval, TimeUnits units)
	{
		switch (units)
		{
			case Seconds:
				return addSeconds(interval);
			case Minutes:
				return addMinutes(interval);
			case Hours:
				return addHours(interval);
			case Days:
				return addDays(interval);
			case Weeks:
				return addDays(interval * 7);
			case Months:
				return addMonths(interval);
			case Years:
				return addYears(interval);
		}

		return this;
	}

	public DateTime add(Date time)
	{
		DateTime t0 = new DateTime(1970, 1, 1);
		return new DateTime(this.getTime() + time.getTime() - t0.getTime());
	}

	public int getDayOfWeek()
	{
		Calendar c = Calendar.getInstance();
		c.setTime(this);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	public int getSecondsSinceMidnight()
	{
		Calendar c = Calendar.getInstance();
		c.setTime(this);
		c.set(Calendar.YEAR, 2000);
		long now = c.getTimeInMillis();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long passed = now - c.getTimeInMillis();
		return (int)(passed / 1000);
	}
	
}
